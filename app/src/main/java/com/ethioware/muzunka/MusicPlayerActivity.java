package com.ethioware.muzunka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ethioware.muzunka.Services.OnClearFromRecentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity implements Playable {

    TextView titleTv,currentTimeTv,totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay,nextBtn,previousBtn,musicIcon;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    NotificationManager notificationManager;
    List<Track> tracks;
    int position = 0;
    boolean isPlaying = false;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    int x=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(mediaPlayer!=null){
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));
                        if(mediaPlayer.isPlaying()){
                            pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                            populateTracks();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                createChannel();
                                registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
                                startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
                            }
                            // TODO when play button of a track is clicked
                            pausePlay.setOnClickListener(view -> {
                                if (isPlaying){
                                    onTrackPause();
                                } else {
                                    onTrackPlay();
                                }
                            });

                            musicIcon.setRotation(x++);

                        }else{
                            pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                            musicIcon.setRotation(0);
                        }
                    }
                    new Handler().postDelayed(this,100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
    public void populateTracks(){
        try {
            tracks = new ArrayList<>();
            // for loop over files adding each track name accordingly
            for (int i=0;i<songsList.size();i++){
                tracks.add(new Track(songsList.get(i).title, R.drawable.background));
            /*tracks.add(new Track("Track 3", R.drawable.background));
            tracks.add(new Track("Track 4", R.drawable.background));*/
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying){
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {

        position--;
        CreateNotification.createNotification(MusicPlayerActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        titleTv.setText(tracks.get(position).getTitle());

    }

    @Override
    public void onTrackPlay() {

        CreateNotification.createNotification(MusicPlayerActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        pausePlay.setImageResource(R.drawable.ic_pause_black_24dp);
        titleTv.setText(tracks.get(position).getTitle());
        isPlaying = true;

    }
    // TODO Paste the same track functionality here
    @Override
    public void onTrackPause() {

        CreateNotification.createNotification(MusicPlayerActivity.this, tracks.get(position),
                R.drawable.ic_play_arrow_black_24dp, position, tracks.size()-1);
        pausePlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        titleTv.setText(tracks.get(position).getTitle());
        isPlaying = false;

    }

    @Override
    public void onTrackNext() {

        position++;
        CreateNotification.createNotification(MusicPlayerActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        titleTv.setText(tracks.get(position).getTitle());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        unregisterReceiver(broadcastReceiver);
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "KOD Dev", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    void setResourcesWithMusic(){
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        titleTv.setText(currentSong.getTitle());

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());

        playMusic();


    }


    private void playMusic(){

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void playNextSong(){

        if(MyMediaPlayer.currentIndex== songsList.size()-1)
            return;
        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourcesWithMusic();

    }

    private void playPreviousSong(){
        if(MyMediaPlayer.currentIndex== 0)
            return;
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void pausePlay(){
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }


    public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}