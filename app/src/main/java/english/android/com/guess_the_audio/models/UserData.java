package english.android.com.guess_the_audio.models;

public class UserData {
    private String audioPath;

    public UserData() {
    }

    public UserData(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
