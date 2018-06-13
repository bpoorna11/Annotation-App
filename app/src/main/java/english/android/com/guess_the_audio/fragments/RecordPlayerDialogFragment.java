package english.android.com.guess_the_audio.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import english.android.com.guess_the_audio.R;
import english.android.com.guess_the_audio.utils.FileUtils;

public class RecordPlayerDialogFragment extends DialogFragment {

    TextView mAudioTextView;
    ImageButton mRecordPlayerButton;
    SeekBar mRecordPlayerSeekBar;

    MediaPlayer mMediaPlayer;
    Handler mHandler;
    Runnable mRunnable;

    private String audioText;
    private boolean isRecordAudioPlayerPressed = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mHandler = new Handler();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mRecordPlayerButton.setImageResource(R.drawable.ic_alert_play);
                mRecordPlayerSeekBar.setMax(mMediaPlayer.getDuration());
                isRecordAudioPlayerPressed = true;
            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_record_player_layout, null);

        mAudioTextView = view.findViewById(R.id.alert_audio_text);
        mRecordPlayerButton = view.findViewById(R.id.alert_record_player);
        mRecordPlayerSeekBar = view.findViewById(R.id.alert_seekbar);

        audioText = getArguments().getString("audio_text");
        mAudioTextView.setText(audioText);

        String recordFilePath = FileUtils.getFilename();
        try {
            mMediaPlayer.setDataSource(recordFilePath);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecordPlayerButton.setImageResource(R.drawable.ic_alert_pause);
        mRecordPlayerSeekBar.setMax(mMediaPlayer.getDuration());
        mMediaPlayer.start();
        playRecordProgress();

        mRecordPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecordAudioPlayerPressed) {
                    isRecordAudioPlayerPressed = false;
                } else {
                    isRecordAudioPlayerPressed = true;
                }
                if (isRecordAudioPlayerPressed) {
                    mRecordPlayerButton.setImageResource(R.drawable.ic_alert_play);
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.pause();
                } else {
                    mRecordPlayerButton.setImageResource(R.drawable.ic_alert_pause);
                    if (!mMediaPlayer.isPlaying())
                        mMediaPlayer.start();
                        playRecordProgress();
                }
            }
        });

        mRecordPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialogBuilder.setCancelable(true);
            }
        });

        alertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //method for sending audio to the database
            }
        });

        return alertDialogBuilder.create();
    }

    private void playRecordProgress() {
        mRecordPlayerSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

        if (mMediaPlayer.isPlaying()) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    playRecordProgress();
                }
            };
            mHandler.postDelayed(mRunnable, 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mHandler.removeCallbacks(mRunnable);
    }
}
