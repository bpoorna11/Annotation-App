package english.android.com.guess_the_audio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "facebook_login";

    @BindView(R.id.btn_facebook)
    Button mFacebookLoginButton;
    @BindView(R.id.btn_google)
    Button mGoogleLoginButton;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser mFirebaseUser;

    CallbackManager mCallbackManager;

    @Override
    protected void onStart() {
        super.onStart();
        if (mFirebaseUser != null) {
            startActivity(new Intent(this, PronouncerNavigationDrawerActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Firebase auth instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Create Callback Manager
        mCallbackManager = CallbackManager.Factory.create();

        configGoogleSignIn();

        mFacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithFacebook();
            }
        });

        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithFacebook() {
        // Initialize Facebook Login button
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        mProgressBar.setVisibility(View.VISIBLE);
        mGoogleLoginButton.setEnabled(false);
        mFacebookLoginButton.setEnabled(false);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mGoogleLoginButton.setEnabled(true);
                        mFacebookLoginButton.setEnabled(true);
                    }
                });
    }


    // [START signin]
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    // [START config_signin]
    // Configure Google Sign In
    private void configGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);


        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Google SignIn failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        //make progress bar visible if auth is starts
        mProgressBar.setVisibility(View.VISIBLE);
        mFacebookLoginButton.setEnabled(false);
        mGoogleLoginButton.setEnabled(false);

        Log.d("id", account.getEmail());
        mFirebaseAuth.fetchSignInMethodsForEmail(account.getEmail())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(task.getResult().getSignInMethods().isEmpty()) {
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            mFirebaseAuth.signInWithCredential(authCredential)
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                                updateUI(user);
                                            } else {
                                                updateUI(null);
                                            }
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            mFacebookLoginButton.setEnabled(true);
                                            mGoogleLoginButton.setEnabled(true);
                                        }
                                    });
                        }

                        else {
                            startActivity(new Intent(LoginActivity.this, PronouncerNavigationDrawerActivity.class));
                            finish();
                        }

                    }
                });

    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginDetailsActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Authentication Failed! Please try again!", Toast.LENGTH_SHORT).show();
        }
    }
}
