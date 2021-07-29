package com.bkav.music.model;

public class SongsList {

    private String title;
    private String subTitle;
    private String duration;
    private String path;
    private long image;
    private int countOfPlay;
    private int isLike;
    private int play;

    public SongsList(String title, String subTitle, String duration, String path, long image, int countOfPlay, int isLike, int play) {
        this.title = title;
        this.subTitle = subTitle;
        this.duration = duration;
        this.path = path;
        this.image = image;
        this.countOfPlay = countOfPlay;
        this.isLike = isLike;
        this.play = play;
    }

    public int getPlay() {
        return play;
    }

    public void setPlay(int play) {
        this.play = play;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getCountOfPlay() {
        return countOfPlay;
    }

    public void setCountOfPlay(int countOfPlay) {
        this.countOfPlay = countOfPlay;
    }

    public int isLike() {
        return isLike;
    }

    public void setLike(int like) {
        isLike = like;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public long getImage() {
        return image;
    }

    public void setImage(long image) {
        this.image = image;
    }
}

