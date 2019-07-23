package english.android.com.guess_the_audio.models;

public class User {
    private String fullName;
    private int age;
    private String gender;
    private String qualification;
    private String phoneModel;
    private String email;
    private String phoneNo;
    private String nativeLanguage;
    private String proficiency;
    //Needed for firebase json parsing.
    public User() {
    }

    public User(String fullName, String email, int age, String gender, String qualification, String phoneModel,String phoneNo,String nativeLanguage,String proficiency) {
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.qualification = qualification;
        this.phoneModel = phoneModel;
        this.phoneNo = phoneNo;
        this.nativeLanguage=nativeLanguage;
        this.proficiency=proficiency;
    }

    public String getProficiency() {
        return proficiency;
    }

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getQualification() {
        return qualification;
    }

    public String getPhoneModel() {
        return phoneModel;
    }
}
