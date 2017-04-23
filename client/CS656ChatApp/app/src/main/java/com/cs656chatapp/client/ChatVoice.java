package com.cs656chatapp.client;


import android.text.SpannableStringBuilder;

import java.io.FileDescriptor;

/**
 * Created by shereen on 4/22/2017.
 */

public class ChatVoice {
    public boolean left;
    public SpannableStringBuilder builder;
    public FileDescriptor path;

    public ChatVoice(boolean left, SpannableStringBuilder builder,FileDescriptor path) {
        super();
        this.left = left;
        this.builder = builder;
        this.path = path;
    }
}
