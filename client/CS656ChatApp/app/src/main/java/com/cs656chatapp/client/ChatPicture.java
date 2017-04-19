package com.cs656chatapp.client;

import android.text.SpannableStringBuilder;

/**
 * Created by Jeremy on 4/13/2017.
 */

public class ChatPicture {
    public boolean left;
    public SpannableStringBuilder picture;

    public ChatPicture(boolean left, SpannableStringBuilder picture) {
        super();
        this.left = left;
        this.picture = picture;
    }
}