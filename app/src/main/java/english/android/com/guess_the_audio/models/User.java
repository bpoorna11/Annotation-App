package english.android.com.guess_the_audio.models;

public class User {
    private String fullName;
    private int age;
    private String gender;
    private String nativeLanguage;
    private String phoneModel;
    private String email;

    //Needed for firebase json parsing.
    public User() {
    }

    public User(String fullName, String email, int age, String gender, String nativeLanguage, String phoneModel) {
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.nativeLanguage = nativeLanguage;
        this.phoneModel = phoneModel;
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

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public String getPhoneModel() {
        return phoneModel;
    }
}
