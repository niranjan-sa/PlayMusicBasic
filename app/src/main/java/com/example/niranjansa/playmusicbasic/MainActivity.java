package com.example.niranjansa.playmusicbasic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;


    //
    private boolean paused=false, playbackPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Here I go

        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        //Getting the songs
        getSongList();

        //Sorting Alphabetically
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        setController();


    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        /*musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected

        switch (item.getItemId()) {

            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList() {
        //retrieve song info

        /*
        * Getting the songs from the external storage
        *
        * */

        boolean isSdPresent=android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        //Common variables
        ContentResolver musicResolver;
        Uri musicUri;
        Cursor musicCursor;
        int songs=0;
        musicResolver = getContentResolver();


        //Checking sd card presence
        if(isSdPresent) {

            //Uri for the internal storage
            musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            //Cursor for ext storage
            musicCursor = musicResolver.query(musicUri, null, null, null, null);


            if(musicCursor!=null && musicCursor.moveToFirst()){
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                    songs++;
                }
                while (musicCursor.moveToNext());
            }
        }

        //Quering the internal memory

        musicUri=MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        musicCursor = musicResolver.query(musicUri, null, null, null, null);
        int i=0;
        final String THU="hermit";
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            Log.i(THU, "I came here");
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            //retriving the flags to avoid showing the ring tones and other system files
            int isAlarm=musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM);
            int isRingtone=musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE);
            int isPodcast=musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST);
            int isNotification=musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION);

            //add songs to list from the internal memory
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                if((musicCursor.getInt(isAlarm) | musicCursor.getInt(isRingtone) | musicCursor.getInt(isPodcast) | musicCursor.getInt(isNotification))==0) {
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                    songs++;
                    i++;
                }
            }
            while (musicCursor.moveToNext());
        }

        Toast.makeText(this, "Total songs queried :- "+songs+" Int - " +i+" Ext :- "+(songs-i),Toast.LENGTH_LONG).show();


        if(i==0) {
            Toast.makeText(this, "No songs present on the device :- ",Toast.LENGTH_LONG).show();
        }

    }


    //Helper method

    private void setController() {
        //setting the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }
    /*Media Player Controller
    * These are the control methods
    *
    */
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;

    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        //initially false
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        /*Originally false*/
        return true;
    }

    @Override
    public boolean canSeekForward() {
        /*Originally false*/
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /*Service triggering methods*/
    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
}
