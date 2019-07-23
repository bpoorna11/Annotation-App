package english.android.com.guess_the_audio;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import english.android.com.guess_the_audio.models.User;

public class LoginDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String USERS = "users";

    @BindView(R.id.nativeLanguagesAutoComplete)
    AutoCompleteTextView mAcNativeLanguageTextView;
    @BindView(R.id.qualLanguagesAutoComplete)
    AutoCompleteTextView mAcQualLanguageTextView;
   // @BindView(R.id.full_name)
   // EditText mFullNameEditText;
    @BindView(R.id.age)
    EditText mAgeEditText;
    @BindView(R.id.save)
    Button mSaveButton;
    @BindView(R.id.genderRadioGroup)
    RadioGroup mGenderRadioGroup;
    RadioButton mGenderRadioButton;
    @BindView(R.id.spinner)
    Spinner spinner;
    private DatabaseReference mRef;
    private FirebaseUser mUser;
    private ArrayAdapter<String> mAdapter,mAdapter1;
    String personName="",emailid="";
    String item="Beginner";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_details);
        ButterKnife.bind(this);

        mRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //Set autocomplete feature by setting the adapter to the autocomplete textview
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            personName = acct.getDisplayName();
            emailid=acct.getEmail();
        }
        spinner.setOnItemSelectedListener(LoginDetailsActivity.this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Beginner");
        categories.add("Intermediate");
        categories.add("Professional");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        String[] qualifications = getResources().getStringArray(R.array.qualification);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, qualifications);
        mAcQualLanguageTextView.setAdapter(mAdapter);
        mAcQualLanguageTextView.setThreshold(2);

        String[] nativelanguages = getResources().getStringArray(R.array.native_languages);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nativelanguages);
        mAcNativeLanguageTextView.setAdapter(mAdapter);
        mAcNativeLanguageTextView.setThreshold(2);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    String name =personName;// mFullNameEditText.getText().toString();
                    System.out.println("Name of person "+name);
                    int age = Integer.parseInt(mAgeEditText.getText().toString());
                    String qualification = mAcQualLanguageTextView.getText().toString();
                    String nativeLanguage = mAcNativeLanguageTextView.getText().toString();
                    int selectedId = mGenderRadioGroup.getCheckedRadioButtonId();
                    mGenderRadioButton = findViewById(selectedId);
                    String gender = mGenderRadioButton.getText().toString();
                    String model = Build.MODEL;
                    String email = mUser.getEmail();
                    if(email=="") email=emailid;
                    String phoneNo=OtpActivity.phone;
                    String proficiency=item;
                    System.out.println("Email id :"+email);
                    System.out.println("Uid :"+mUser.getUid());
                    User user = new User(name, email, age, gender, qualification, model,phoneNo,nativeLanguage,proficiency);
                    email=email.replaceAll("\\.", "_");
                    mRef.child(USERS).child(email).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {

                                startActivity(new Intent(LoginDetailsActivity.this,
                                        PronouncerNavigationDrawerActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginDetailsActivity.this,
                                        getString(R.string.firebase_login_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    }
            }
        });
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
         item = parent.getItemAtPosition(position).toString();
        // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private boolean validate() {
        boolean isInfoCorrect = true;
        if (TextUtils.isEmpty(mAgeEditText.getText())) {
            mAgeEditText.setError("Enter the age!");
            isInfoCorrect = false;
        } else {
            if (Integer.parseInt(mAgeEditText.getText().toString()) < 5 ||
                    Integer.parseInt(mAgeEditText.getText().toString()) > 100) {
                Toast.makeText(this, getString(R.string.min_max_age), Toast.LENGTH_SHORT).show();
                isInfoCorrect = false;
            }
        }
        if (TextUtils.isEmpty(mAcQualLanguageTextView.getText())) {
            Toast.makeText(this, "Please enter your qualification", Toast.LENGTH_SHORT).show();
            isInfoCorrect = false;
        }
        if (TextUtils.isEmpty(mAcNativeLanguageTextView.getText())) {
            Toast.makeText(this, getString(R.string.native_language_error), Toast.LENGTH_SHORT).show();
            isInfoCorrect = false;
        }

        if (mGenderRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.select_gender), Toast.LENGTH_SHORT).show();
            isInfoCorrect = false;
        }

        return isInfoCorrect;
    }
}
