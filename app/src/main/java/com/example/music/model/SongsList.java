package com.example.music.model;

public class SongsList {

    private String title;
    private String subTitle;
    private String path;
    private int image;
    private int countOfPlay;
    private int isLike;

    public SongsList(String title, String subTitle, String path, int image,int countOfPlay, int isLike) {
        this.title = title;
        this.subTitle = subTitle;
        this.path = path;
        this.image = image;
        this.countOfPlay = countOfPlay;
        this.isLike = isLike;
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}

