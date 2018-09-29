package com.bsit.linhai605.model;

/**
 * Created by DELL on 2017/9/6.
 */

public class CommonBackJson<T> {
    private String message;
    private String code;
    private T content;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
