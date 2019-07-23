package english.android.com.guess_the_audio;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.LineBarVisualizer;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import english.android.com.guess_the_audio.models.User;
import english.android.com.guess_the_audio.models.UserData;
import english.android.com.guess_the_audio.models.UserEnteredText;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PronouncerNavigationDrawerActivity extends AppCompatActivity
        implements  MediaPlayer.OnPreparedListener {

    public static final int REQUEST_PERMISSION_CODE = 1;
    private static final String TAG = "record_audio_player_dialog";

    //ButterKnife View Injection
    @BindView(R.id.textView2)
    TextView tv2;
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
    String emailid="";
    private List<PronounceData> lists = new ArrayList<>();
    private List<UserData> lists1 = new ArrayList<>();
    public static int sendPressed=0,editedAudio=0;
    private int mCurrentTextPosition = 0;
    private boolean isPlayerPressed = false;
    private boolean isPlayerPressedFirstTime = true;
    private String childPath = "audio_data";
    private int c=0;public static int songPos=0;boolean check=false;
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
      //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        //Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        System.out.println("Email of current user "+mAuth.getCurrentUser().getEmail());
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        System.out.println("Email of current user "+mAuth.getCurrentUser().getEmail());
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

      //  mLineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        mLineBarVisualizer.setDensity(70);
        getDataFromFirebase(childPath);



        mHandler = new Handler();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            //    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       // drawer.addDrawerListener(toggle);
       // toggle.syncState();

      //  NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
      //  navigationView.setNavigationItemSelectedListener(this);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mp) {
               // mPlayButton.setText(R.string.play);
                mPlayButton.setBackground(getApplicationContext().getDrawable(R.drawable.play1));
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
        emailid=mAuth.getCurrentUser().getEmail();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            emailid=acct.getEmail();
        }

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c=0;
                final String audioText = mEnterEditText.getText().toString();
                if(audioText.length() == 0) {
                    mEnterEditText.setError("Enter the correct text");
                } else {

                    PronounceData pronounceData = lists.get(mCurrentTextPosition);
                    final String englishText = pronounceData.getAudioId();
                    final String audioPath = pronounceData.getAudioPath();
                                System.out.println("Value of english text "+englishText);
                                UserData userData = new UserData(audioPath,emailid,audioText);
                                mRef.child("users_entered_text").child(englishText).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            mCurrentTextPosition++;
                                            mMediaPlayer.reset();
                                            PronounceData pronounceData1 = lists.get(mCurrentTextPosition);
                                            String englishText = pronounceData1.getAudioId();
                                            String audion="";
                                            for(int i=0;i<englishText.length();i++){
                                                if(Character.isAlphabetic(englishText.codePointAt(i))) audion+=englishText.charAt(i);
                                            }
                                            tv2.setText("Audio: "+audion);
                                            mEnterEditText.setText(englishText);
                                            Toast.makeText(PronouncerNavigationDrawerActivity.this, "Text sent successfully!", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(PronouncerNavigationDrawerActivity.this, "Text sent failed! Try again!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });



                   // final String uid = mUser.getUid();
                   // System.out.println("UID :"+uid);
                    System.out.println("English text :"+englishText+" Audio path :"+audioPath);

                }
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (isPlayerPressed) {
                    isPlayerPressed = false;
                } else {
                    isPlayerPressed = true;
                }
                if (isPlayerPressed) {
                   // mPlayButton.setText(R.string.pause);
                    mPlayButton.setBackground(getApplicationContext().getDrawable(R.drawable.pause1));
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
                   // mPlayButton.setText(R.string.play);
                    mPlayButton.setBackground(getApplicationContext().getDrawable(R.drawable.play1));
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
                System.out.println("Send pressed ,No of songs edited "+mCurrentTextPosition);
                PronounceData pronounceData = lists.get(mCurrentTextPosition);
                String englishText = pronounceData.getAudioId();
                mEnterEditText.setText(englishText);
                String audion="";
                for(int i=0;i<englishText.length();i++){
                    if(Character.isAlphabetic(englishText.codePointAt(i))) audion+=englishText.charAt(i);
                }
                tv2.setText("Audio: "+audion);
               /* DatabaseReference mDatabaseRef1 = FirebaseDatabase.getInstance().getReference().child("users_entered_text").child(englishText);
                mDatabaseRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //System.out.println("Send pressed ,No of songs edited "+sendPressed);

                        System.out.println("get edited audio "+dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mDatabaseRef1.addChildEventListener(new ChildEventListener() {
                                                        @Override
                                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                            c++;
                                                            System.out.println("ON child added: "+dataSnapshot.getValue());
                                                            if(c==2) {

                                                                check=true;
                                                                songPos=Integer.parseInt(dataSnapshot.getValue().toString());

                                                                PronounceData pronounceData = lists.get(songPos);
                                                                String englishText = pronounceData.getAudioId();
                                                                 mEnterEditText.setText(englishText);
                                                                String audion="";
                                                                for(int i=0;i<englishText.length();i++){
                                                                    if(Character.isAlphabetic(englishText.codePointAt(i))) audion+=englishText.charAt(i);
                                                                }
                                                                tv2.setText("Audio: "+audion);
                                                                System.out.println("Value of Edited Audio " + songPos);
                                                                System.out.println("Value of english text "+englishText);
                                                            }

                                                        }

                                                        @Override
                                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                            // System.out.println("ON child changed method 2:getdatafromuser");

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

                                                    });*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("ON child added method");
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("ON child changed method");
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
