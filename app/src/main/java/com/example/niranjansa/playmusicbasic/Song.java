package com.example.niranjansa.playmusicbasic;

/**
 * Created by niranjansa on 18/3/16.
 *
 * This class represents a single sound track!!
 * This will represent a song.
 */
public class Song {

    private long id;
    private String title;
    private String artist;

    //Constructors
    public Song() {}
    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    //get methods
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}



}
