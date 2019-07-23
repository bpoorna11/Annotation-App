package english.android.com.guess_the_audio.models;

public class PronounceData {

    private String audioId;
    private String audioPath;

    //Needs default constructor for firebase
    public PronounceData() {}


    public String getAudioId() {
        return audioId;
    }

    public void setEnglishText(String audioId) {
        this.audioId = audioId;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
