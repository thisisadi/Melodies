package com.example.musicplayer;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.PorterDuff;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;



import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import android.widget.VideoView;

import java.io.File;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlaySong extends AppCompatActivity {

    TextView textView;
    ImageView play,previous,next;
    SeekBar seekBar;
    ArrayList<File>songs;
    MediaPlayer mediaPlayer;
    VideoView VideoView;
    String textContent;
    int position;
    Thread updateSeek;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        updateSeek.interrupt();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

        // To change the color of the seekbar thumb and, the progress bar to any desired color.
        seekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        VideoView = findViewById(R.id.videoView);
        VideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vid));
        VideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            VideoView.start();
        });

        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        textContent = intent.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);
        position = intent.getIntExtra("position", 0);

        mediaPlayer = new MediaPlayer();
        try{mediaPlayer.setDataSource(PlaySong.this,Uri.parse(songs.get(position).toString()));} catch (Exception e) {}
        try{mediaPlayer.prepare();} catch(Exception e){}

        mediaPlayer.start();

        seekBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        updateSeek = new Thread() {
            @Override
            public void run() {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(() -> {
                    int currentPosition = 0;
                    try {
                        while (currentPosition < mediaPlayer.getDuration()) {
                            currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 0, 800, TimeUnit.MILLISECONDS);
            }
        };
        updateSeek.start();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    play.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                    VideoView.pause();
                } else {
                    play.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                    VideoView.start();
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        nextSong();
                    }
                });
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                if (position != 0) {
                    position -= 1;
                } else {
                    position = songs.size() - 1;
                }
                repeat();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                      nextSong();
                    }
                });
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                if (position != songs.size() - 1) {
                    position += 1;
                } else {
                    position = 0;
                }
                repeat();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        nextSong();
                    }
                });
            }
        });
    }

    // Tries to play the next song upon the completion of the current song. :)
    public void nextSong(){
        position = (position+1)%songs.size();
        mediaPlayer.stop();
        mediaPlayer.reset();
        try{mediaPlayer.setDataSource(PlaySong.this,Uri.parse(songs.get(position).toString()));}catch (Exception e) {}
        try{mediaPlayer.prepare();}catch(Exception e){}
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
        textContent = songs.get(position).getName();
        textView.setText(textContent);
    }

    // Updates the seekbar duration and the name of the song when the previous or next button is clicked.
    public void repeat(){
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
        play.setImageResource(R.drawable.pause);
        seekBar.setMax(mediaPlayer.getDuration());
        textContent = songs.get(position).getName();
        textView.setText(textContent);
    }
}
