package com.centralway.player.android.videoplayer.utilities;

/**
 * PoJo class Video list getting from sdcard
 */

public class VideoAlbum {
    private String name;
    private int numberOfSongs;
    private String imagePath;
    private long id;

    public VideoAlbum(){

    }

    public VideoAlbum(String name, int numberOfSongs, String image, long id) {
        this.name = name;
        this.numberOfSongs = numberOfSongs;
        this.imagePath = image;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String image) {
        this.imagePath = image;
    }

    public long getId() {return id; }

    public void setId(long id) {this.id = id; }

}
