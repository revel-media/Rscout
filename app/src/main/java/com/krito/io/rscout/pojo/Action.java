package com.krito.io.rscout.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Mona Abdallh on 7/30/2018.
 */

public class Action {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("action")
    @Expose
    private String action;

    @SerializedName("attempt")
    @Expose
    private int attempt;

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
