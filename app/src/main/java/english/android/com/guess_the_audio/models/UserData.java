package english.android.com.guess_the_audio.models;

public class UserData {
    private String audioPath;
    private String email;
    private String userText;
    public UserData() {
    }

    public UserData(String audioPath,String email,String userText) {

        this.audioPath = audioPath;
        this.email=email;
        this.userText=userText;

    }


    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
