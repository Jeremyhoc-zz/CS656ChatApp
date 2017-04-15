package com.cs656chatapp.client;

/**
 * Created by Jeremy on 4/13/2017.
 */

public class ChatMessage {
    public boolean left;
    public String message;

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }
}