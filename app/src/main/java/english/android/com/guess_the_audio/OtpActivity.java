package english.android.com.guess_the_audio;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
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

public class OtpActivity extends AppCompatActivity  {

    EditText editTextPhone, editTextCode;

    FirebaseAuth mAuth;
    public static String codeSent,phone="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();

        editTextCode = findViewById(R.id.editTextCode);
        editTextPhone = findViewById(R.id.editTextPhone);

        findViewById(R.id.buttonGetVerificationCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });


        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });
    }

    private void verifySignInCode(){
        String code = editTextCode.getText().toString();
        try{
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
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
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode(){

         phone = editTextPhone.getText().toString();

        if(phone.isEmpty()){
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            editTextPhone.setError("Please enter a valid phone");
            editTextPhone.requestFocus();
            return;
        }

        System.out.println(phone);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                OtpActivity.this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }



    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(OtpActivity.this,"Verified",Toast.LENGTH_SHORT).show();
            Intent in=new Intent(OtpActivity.this,LoginDetailsActivity.class);
            startActivity(in);
            finish();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // Toast.makeText(OtpActivity.this,"verification fialed",Toast.LENGTH_SHORT).show();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(OtpActivity.this,"invalid req",Toast.LENGTH_SHORT).show();
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(OtpActivity.this,"sms quota exceeded",Toast.LENGTH_SHORT).show();
                // The SMS quota for the project has been exceeded
                // ...
            }
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            System.out.println("code sent!!!!!");
            codeSent = s;
            Toast.makeText(OtpActivity.this,"Code sent",Toast.LENGTH_SHORT).show();
            Intent in=new Intent(OtpActivity.this,VerifyOtpActivity.class);
            startActivity(in);
            finish();
        }
    };


}