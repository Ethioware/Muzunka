package com.ethioware.muzunka;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;


import com.ethioware.muzunka.Services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements Playable {

    ImageButton play;
    TextView title;

    NotificationManager notificationManager;

    List<Track> tracks;

    int position = 0;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_notification);

        play = findViewById(R.id.play);
        title = findViewById(R.id.title);

        populateTracks();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
        // TODO when play button of a track is clicked
        play.setOnClickListener(view -> {
            if (isPlaying){
                onTrackPause();
            } else {
                onTrackPlay();
            }
        });
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

    // TODO populate list with tracks and set background to app icon
    public void populateTracks(){
        tracks = new ArrayList<>();
        // for loop over files adding each track name accordingly
        tracks.add(new Track("Track 1", R.drawable.background));
        tracks.add(new Track("Track 2", R.drawable.background));
        tracks.add(new Track("Track 3", R.drawable.background));
        tracks.add(new Track("Track 4", R.drawable.background));
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
        CreateNotification.createNotification(NotificationActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        title.setText(tracks.get(position).getTitle());

    }

    @Override
    public void onTrackPlay() {

        CreateNotification.createNotification(NotificationActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        play.setImageResource(R.drawable.ic_pause_black_24dp);
        title.setText(tracks.get(position).getTitle());
        isPlaying = true;

    }
    // TODO Paste the same track functionality here
    @Override
    public void onTrackPause() {

        CreateNotification.createNotification(NotificationActivity.this, tracks.get(position),
                R.drawable.ic_play_arrow_black_24dp, position, tracks.size()-1);
        play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        title.setText(tracks.get(position).getTitle());
        isPlaying = false;

    }

    @Override
    public void onTrackNext() {

        position++;
        CreateNotification.createNotification(NotificationActivity.this, tracks.get(position),
                R.drawable.ic_pause_black_24dp, position, tracks.size()-1);
        title.setText(tracks.get(position).getTitle());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        unregisterReceiver(broadcastReceiver);
    }
}
