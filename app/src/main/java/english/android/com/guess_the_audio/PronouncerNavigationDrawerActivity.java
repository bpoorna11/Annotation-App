package english.android.com.guess_the_audio;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.chibde.visualizer.LineBarVisualizer;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import english.android.com.guess_the_audio.models.PronounceData;
import english.android.com.guess_the_audio.models.UserData;
import english.android.com.guess_the_audio.models.UserEnteredText;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PronouncerNavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaPlayer.OnPreparedListener {

    public static final int REQUEST_PERMISSION_CODE = 1;
    private static final String TAG = "record_audio_player_dialog";

    //ButterKnife View Injection
    @BindView(R.id.player)
    Button mPlayButton;
    @BindView(R.id.audio_indicator)
    LineBarVisualizer mLineBarVisualizer;
    @BindView(R.id.audio_seek_bar)
    SeekBar mSeekBar;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.et_enter_text)
    EditText mEnterEditText;
    @BindView(R.id.send)
    Button mSendButton;

    //Firebase variables
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser mUser;
    private DatabaseReference mRef;

    private List<PronounceData> lists = new ArrayList<>();

    private int mCurrentTextPosition = 0;
    private boolean isPlayerPressed = false;
    private boolean isPlayerPressedFirstTime = true;
    private String childPath = "intonation";

    private MediaPlayer mMediaPlayer;
    private Runnable mRunnable;
    private Handler mHandler;

    @Override
    protected void onStart() {
        super.onStart();
        mPlayButton.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prononcer_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        //Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //MediaPlayer
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        /*
         * Check Permission.
         */
        if (!checkPermission()) {
            requestPermission();
        } else {
            mLineBarVisualizer.setPlayer(mMediaPlayer.getAudioSessionId());
        }

        mLineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        mLineBarVisualizer.setDensity(70);

        getDataFromFirebase(childPath);

        mHandler = new Handler();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayButton.setText(R.string.play);
                mMediaPlayer.reset();
                isPlayerPressed = false;
                isPlayerPressedFirstTime = true;
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String audioText = mEnterEditText.getText().toString();
                if(audioText.length() == 0) {
                    mEnterEditText.setError("Enter the correct text");
                } else {
                    PronounceData pronounceData = lists.get(mCurrentTextPosition);
                    final String englishText = pronounceData.getEnglishText();
                    final String audioPath = pronounceData.getAudioPath();
                    final String uid = mUser.getUid();
                    UserData userData = new UserData(audioPath);
                    mRef.child("users_entered_text").child(englishText).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                UserEnteredText userEnteredText = new UserEnteredText();
                                userEnteredText.setUserText(audioText);
                                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
                                mDatabaseRef.child("users_entered_text").child(englishText).child(uid).setValue(userEnteredText).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mCurrentTextPosition++;
                                                    mMediaPlayer.reset();
                                                    PronounceData pronounceData1 = lists.get(mCurrentTextPosition);
                                                    String englishText = pronounceData1.getEnglishText();
                                                    mEnterEditText.setText(englishText);
                                                    Toast.makeText(PronouncerNavigationDrawerActivity.this, "Text sent successfully!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(PronouncerNavigationDrawerActivity.this, "Text sent failed! Try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                );
                            } else {
                                Toast.makeText(PronouncerNavigationDrawerActivity.this, "Text sent failed! Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlayerPressed) {
                    isPlayerPressed = false;
                } else {
                    isPlayerPressed = true;
                }
                if (isPlayerPressed) {
                    mPlayButton.setText(R.string.pause);
                    if (!isPlayerPressedFirstTime) {
                        if (!mMediaPlayer.isPlaying())
                            mMediaPlayer.start();
                        playProgress();
                    } else {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mPlayButton.setEnabled(false);
                        PronounceData pronounceData = lists.get(mCurrentTextPosition);
                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pronounceData.getAudioPath());
                        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
//                                try {
//                                    // Download url of file
//                                    final String url = uri.toString();
//                                    mMediaPlayer.setDataSource(url);
//                                    // wait for media player to get prepare
//                                    mMediaPlayer.setOnPreparedListener(PronouncerNavigationDrawerActivity.this);
//                                    mMediaPlayer.prepareAsync();
                                isPlayerPressedFirstTime = false;
                                new Player().execute(uri);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        });
                    }
                } else {
                    mPlayButton.setText(R.string.play);
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.pause();
                    Log.d("Song duration", "" + mMediaPlayer.getDuration());
                    Log.d("Song position", "" + mMediaPlayer.getCurrentPosition());
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.prononcer_navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.phoeneme) {
            childPath = "Phoeneme";
            changeData(childPath);

        } else if (id == R.id.stress) {
            childPath = "Stress";
            changeData(childPath);

        } else if (id == R.id.intonation) {
            childPath = "Intonation";
            changeData(childPath);

        } else if (id == R.id.sentence) {
            childPath = "Sentence";
            changeData(childPath);

        } else if (id == R.id.logout) {
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity(new Intent(PronouncerNavigationDrawerActivity.this, LoginActivity.class));
                }
            });


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //method for changing data and changing firebase reference path
    private void changeData(String childPath) {
        mCurrentTextPosition = 0;
        lists.clear();
        getDataFromFirebase(childPath);
    }

    private void getDataFromFirebase(String childPath) {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(childPath);

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PronounceData pronounceData = lists.get(mCurrentTextPosition);
                String englishText = pronounceData.getEnglishText();
                mEnterEditText.setText(englishText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        PronounceData pronounceData = dataSnapshot.getValue(PronounceData.class);

        lists.add(pronounceData);
        mPlayButton.setEnabled(true);
    }

    private void playProgress() {
        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

        if (mMediaPlayer.isPlaying()) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    playProgress();
                }
            };
            mHandler.postDelayed(mRunnable, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPlayerPressed = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * Permission methods
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(PronouncerNavigationDrawerActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readstoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (storagePermission && recordPermission && readstoragePermission) {
                        Toast toast = Toast.makeText(PronouncerNavigationDrawerActivity.this, "Permission Granted", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        mLineBarVisualizer.setPlayer(mMediaPlayer.getAudioSessionId());
                    } else {
                        mPlayButton.setEnabled(false);
                        mLineBarVisualizer.setEnabled(false);
                        Toast toast = Toast.makeText(PronouncerNavigationDrawerActivity.this, "Permission Denied. Buttons Disabled!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayButton.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
        mSeekBar.setMax(mp.getDuration());
        mp.start();
        playProgress();
    }

    private class Player extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];

            // Download url of file
            final String url = uri.toString();

            try {
                mMediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // wait for media player to get prepare
            mMediaPlayer.setOnPreparedListener(PronouncerNavigationDrawerActivity.this);
            mMediaPlayer.prepareAsync();
            return null;
        }
    }
}
