package com.krito.io.rscout.pojo;

import java.io.Serializable;

/**
 * Created by Ahmed Ali on 7/31/2018.
 */

public class ActionItem implements Serializable{

    public static final int QUESTION = 0;
    public static final int ANSWER = 1;
    public static final int SUBMITTING = 2;

    private int type;
    private int questionId;
    private String questionString;
    private int attempts;
    private String answerString;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionString() {
        return questionString;
    }

    public void setQuestionString(String questionString) {
        this.questionString = questionString;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public String getAnswerString() {
        return answerString;
    }

    public void setAnswerString(String answerString) {
        this.answerString = answerString;
    }
}
