package english.android.com.guess_the_audio;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class VerifyOtpActivity extends AppCompatActivity {
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    public static String codeSent,phone="";
    String typedOtp="";
    TextView tv1,tv2;
    EditText et1,et2,et3,et4,et5,et6;
    String s1="",s2="",s3="",s4="",s5="",s6="";
    FirebaseAuth mAuth;
    private int progress = 0;
    private final int pBarMax = 60;
    @BindView(R.id.progressBar1)
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sotp);
        mAuth = FirebaseAuth.getInstance();
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);
        tv1 = findViewById(R.id.you_will_re);
        tv2 = findViewById(R.id.progressBarinsideText);
        tv1.setText("You will receive an OTP on number "+"9425678077"+" in \n60 sec. If you don't receive click ");
        mProgressBar=findViewById(R.id.progressBar1);

        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s1=s.toString();
                System.out.println("text of ed1 "+s1);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    et2.requestFocus();
                }
                else if(s.length()==0)
                {
                    et1.clearFocus();
                }
            }
        });

        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s2=s.toString();
                System.out.println("text of ed2 "+s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    et3.requestFocus();
                }
                else if(s.length()==0)
                {
                    et1.requestFocus();
                }
            }
        });

        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s3=s.toString();
                System.out.println("text of ed3 "+s);

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    et4.requestFocus();
                }
                else if(s.length()==0)
                {
                    et2.requestFocus();
                }
            }
        });

        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s4=s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    et5.requestFocus();
                }
                else if(s.length()==0)
                {
                    et3.requestFocus();
                }
            }
        });

        et5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s5=s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    et6.requestFocus();
                }
                else if(s.length()==0)
                {
                    et4.requestFocus();
                }
            }
        });

        et6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s6=s.toString();
                //check();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1)
                {
                    //et6.clearFocus();
                }
                else if(s.length()==0)
                {
                    et5.requestFocus();
                }
            }
        });
        findViewById(R.id.verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  verifySignInCode();
                mProgressBar.setVisibility(View.VISIBLE);
                startTimer(1);
                check();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(OtpActivity.phone,mResendToken);
            }
        });
    }
    CountDownTimer countDownTimer;
    private void startTimer(final int minuti) {
        countDownTimer = new CountDownTimer(60 * minuti * 1000, 500) {
            // 500 means, onTick function will be called at every 500 milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                mProgressBar.setProgress((int)seconds);
                tv2.setText(String.format("%02d", seconds%60));
                // format the textview to show the easily readable format

            }
            @Override
            public void onFinish() {
                if(tv2.getText().equals("00")){
                    tv2.setText("");
                }
                else{
                    tv2.setText("59");
                    mProgressBar.setProgress(60*minuti);
                }
            }
        }.start();

    }
    /*  private void verifySignInCode(){
        String code = typedOtp;
        if(typedOtp==null) Toast.makeText(getApplicationContext(),
                "Enter valid OTP", Toast.LENGTH_LONG).show();
       else{

            try {

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OtpActivity.codeSent, code);
                signInWithPhoneAuthCredential(credential);
            }catch (Exception e){
               // Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
              //  toast.setGravity(Gravity.CENTER,0,0);
              //  toast.show();
                Intent in = new Intent(getApplicationContext(), PronouncerNavigationDrawerActivity.class);
                startActivity(in);
            }
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //here you can open new activity
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                            Intent in = new Intent(getApplicationContext(), PronouncerNavigationDrawerActivity.class);
                            startActivity(in);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }*/
  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

      @Override
      public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
          Toast.makeText(VerifyOtpActivity.this,"Verified",Toast.LENGTH_SHORT).show();
          Intent in=new Intent(VerifyOtpActivity.this,LoginDetailsActivity.class);
          startActivity(in);
          finish();
      }

      @Override
      public void onVerificationFailed(FirebaseException e) {
          // Toast.makeText(OtpActivity.this,"verification fialed",Toast.LENGTH_SHORT).show();
          if (e instanceof FirebaseAuthInvalidCredentialsException) {
              Toast.makeText(VerifyOtpActivity.this,"invalid req",Toast.LENGTH_SHORT).show();
              // Invalid request
              // ...
          } else if (e instanceof FirebaseTooManyRequestsException) {
              Toast.makeText(VerifyOtpActivity.this,"sms quota exceeded",Toast.LENGTH_SHORT).show();
              // The SMS quota for the project has been exceeded
              // ...
          }
      }

      @Override
      public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
          super.onCodeSent(s, forceResendingToken);
          System.out.println("code sent!!!!!");
          codeSent = s;
          mResendToken=forceResendingToken;
          Toast.makeText(VerifyOtpActivity.this,"Code sent",Toast.LENGTH_SHORT).show();

      }
  };
  private void resendVerificationCode(String phoneNumber,
                                      PhoneAuthProvider.ForceResendingToken token) {
      PhoneAuthProvider.getInstance().verifyPhoneNumber(
              "+91"+phoneNumber,        // Phone number to verify
              60,                 // Timeout duration
              TimeUnit.SECONDS,   // Unit of timeout
              this,               // Activity (for callback binding)
              mCallbacks,         // OnVerificationStateChangedCallbacks
              token);             // ForceResendingToken from callbacks
  }
    private void check() {
        typedOtp = "";
        if (s1 != null && s2 != null && s3 != null && s4 != null && s5 != null && s6 != null) {
            typedOtp = s1 + s2 + s3 + s4 + s5 + s6;
            System.out.println("Typed otp " + typedOtp);
        }
        System.out.println("Otp sent " + OtpActivity.codeSent);
        try{
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OtpActivity.codeSent, typedOtp);
        signInWithPhoneAuthCredential(credential);
      }  catch (Exception e){
        Toast toast = Toast.makeText(this, "Verification Code is wrong", Toast.LENGTH_SHORT);
       // toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //here you can open new activity
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_SHORT).show();
                            Intent in=new Intent(VerifyOtpActivity.this,LoginDetailsActivity.class);
                            startActivity(in);
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
    }



