package com.example.abdlkdr.livequestion.Model;

/**
 * Created by abdlkdr on 30.03.2018.
 */

public class Video {
    private String id;
    private String url;
    private String status;
    private int videoLength;
    private int tempValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(int videoLength) {
        this.videoLength = videoLength;
    }

    public int getTempValue() {
        return tempValue;
    }

    public void setTempValue(int tempValue) {
        this.tempValue = tempValue;
    }

    public Video() {
    }

    public Video(String id, String url, String status, int videoLength, int tempValue) {
        this.id = id;
        this.url = url;
        this.status = status;
        this.videoLength = videoLength;
        this.tempValue = tempValue;
    }
}
