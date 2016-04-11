package com.example.niranjansa.playmusicbasic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class SongControlActivity extends AppCompatActivity {

    private static MusicService myservice;
    private static boolean musicBound=false;
    private Intent playIntent=null;
    private TextView start;
    private TextView end;
    private SeekBar seekBar;
    private String start_id_, end_id_;


    //connect to the service
    private  ServiceConnection musicConnection=new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            myservice = binder.getService();
            musicBound = true;
            start_id_=""+MusicService.position;
            end_id_=""+MusicService.duration;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_control);
        if (myservice==null) {
           Toast.makeText(this, "null bhi",Toast.LENGTH_SHORT).show();
        }
        start=(TextView)findViewById(R.id.start_id);
        end=(TextView)findViewById(R.id.end_id);
        seekBar=(SeekBar)findViewById(R.id.seekOne);



        end.setText(""+MusicService.duration);
        seekBar.setMax(((int)MusicService.duration));

        Thread posUpdater=new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long seconds=MusicService.duration;
                            long secondsPos=MusicService.position;
                            long s = seconds % 60;
                            long m = (seconds / 60) % 60;
                            long h = (seconds / (60 * 60)) % 24;
                            end.setText(String.format("%d:%02d:%02d", h,m,s));

                            secondsPos=MusicService.position;
                            long sm = secondsPos % 60;
                            long mm = (secondsPos / 60) % 60;
                            long hm = (secondsPos / (60 * 60)) % 24;
                            start.setText(String.format("%d:%02d:%02d", hm,mm,sm));
                            seekBar.setProgress(((int)MusicService.position));
                        }
                    });
                }
            }
        });
        posUpdater.start();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        MusicService.MusicBinder binder=MainActivity.bindo;
        myservice=binder.getService();
    }

    public void play(View view) {
        if(myservice==null) {
            Toast.makeText(this, "jimmy",Toast.LENGTH_SHORT).show();
        }
        myservice.go();
        start.setText(myservice.foo());
    }

    public void playprev(View view) {
        myservice.playPrev();
    }

    public void pausep(View view) {
        myservice.pausePlayer();
    }
}
