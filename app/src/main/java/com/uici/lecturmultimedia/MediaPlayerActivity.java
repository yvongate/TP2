package com.uici.lecturmultimedia;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.uici.lecturmultimedia.databinding.ActivityMediaPlayerBinding;
import java.io.IOException;

public class MediaPlayerActivity extends AppCompatActivity {

    private ActivityMediaPlayerBinding binding;
    private MediaFile mediaFile;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeekBar;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaFile = getIntent().getParcelableExtra("media_file");
        if (mediaFile == null) {
            Toast.makeText(this, "Erreur: Fichier introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupMediaPlayer();
        setupControls();
    }

    private void setupUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mediaFile.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.textViewMediaName.setText(mediaFile.getName());
        binding.textViewMediaType.setText(mediaFile.isAudio() ? "Audio" : "Video");
        binding.textViewMediaSize.setText(mediaFile.getFormattedSize());

        if (mediaFile.isVideo()) {
            binding.videoView.setVisibility(View.VISIBLE);
            binding.imageViewAudioPlaceholder.setVisibility(View.GONE);
        } else {
            binding.videoView.setVisibility(View.GONE);
            binding.imageViewAudioPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void setupMediaPlayer() {
        try {
            Uri uri = Uri.parse(mediaFile.getPath());

            if (mediaFile.isVideo()) {
                binding.videoView.setVideoURI(uri);
                binding.videoView.setOnPreparedListener(mp -> {
                    mediaPlayer = mp;
                    int duration = mediaPlayer.getDuration();
                    binding.seekBar.setMax(duration);
                    binding.textViewDuration.setText(formatTime(duration));
                    binding.textViewCurrentTime.setText("00:00");
                    startSeekBarUpdate();
                });

                binding.videoView.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    binding.buttonPlayPause.setImageResource(R.drawable.ic_play);
                    stopSeekBarUpdate();
                });

                binding.videoView.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(this, "Erreur de lecture", Toast.LENGTH_SHORT).show();
                    return true;
                });
            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.setOnPreparedListener(mp -> {
                    int duration = mediaPlayer.getDuration();
                    binding.seekBar.setMax(duration);
                    binding.textViewDuration.setText(formatTime(duration));
                    binding.textViewCurrentTime.setText("00:00");
                });

                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    binding.buttonPlayPause.setImageResource(R.drawable.ic_play);
                    stopSeekBarUpdate();
                });

                mediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors du chargement du fichier", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupControls() {
        binding.buttonPlayPause.setOnClickListener(v -> togglePlayPause());

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mediaFile.isVideo()) {
                        binding.videoView.seekTo(progress);
                    } else if (mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
                    binding.textViewCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        handler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    int currentPosition = 0;
                    if (mediaFile.isVideo()) {
                        currentPosition = binding.videoView.getCurrentPosition();
                    } else if (mediaPlayer != null) {
                        currentPosition = mediaPlayer.getCurrentPosition();
                    }
                    binding.seekBar.setProgress(currentPosition);
                    binding.textViewCurrentTime.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 100);
                }
            }
        };
    }

    private void togglePlayPause() {
        if (mediaFile.isVideo()) {
            if (isPlaying) {
                binding.videoView.pause();
                binding.buttonPlayPause.setImageResource(R.drawable.ic_play);
                stopSeekBarUpdate();
            } else {
                binding.videoView.start();
                binding.buttonPlayPause.setImageResource(R.drawable.ic_pause);
                startSeekBarUpdate();
            }
        } else {
            if (mediaPlayer != null) {
                if (isPlaying) {
                    mediaPlayer.pause();
                    binding.buttonPlayPause.setImageResource(R.drawable.ic_play);
                    stopSeekBarUpdate();
                } else {
                    mediaPlayer.start();
                    binding.buttonPlayPause.setImageResource(R.drawable.ic_pause);
                    startSeekBarUpdate();
                }
            }
        }
        isPlaying = !isPlaying;
    }

    private void startSeekBarUpdate() {
        handler.post(updateSeekBar);
    }

    private void stopSeekBarUpdate() {
        handler.removeCallbacks(updateSeekBar);
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying) {
            togglePlayPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacks(updateSeekBar);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
