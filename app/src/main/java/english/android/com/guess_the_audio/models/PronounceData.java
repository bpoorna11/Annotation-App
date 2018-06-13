package english.android.com.guess_the_audio.models;

public class PronounceData {

    private String englishText;
    private String audioPath;

    //Needs default constructor for firebase
    public PronounceData() {}


    public String getEnglishText() {
        return englishText;
    }

    public void setEnglishText(String englishText) {
        this.englishText = englishText;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
