package com.krito.io.rscout.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Ahmed Ali on 7/30/2018.
 */

public class Scout implements Serializable{

    @SerializedName("error")
    @Expose
    private boolean error;


    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("img_url")
    @Expose
    private String imgUrl;

    @SerializedName("actions")
    @Expose
    private Action[] actions;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Action[] getActions() {
        return actions;
    }

    public void setActions(Action[] actions) {
        this.actions = actions;
    }
}
