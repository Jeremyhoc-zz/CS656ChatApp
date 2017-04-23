package com.cs656chatapp.client;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 4/13/2017.
 */

class ChatArrayAdapter extends ArrayAdapter {

    private TextView chatText;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private List<ChatPicture> chatPictureList = new ArrayList<ChatPicture>();
    private List<ChatVoice> chatVoiceList = new ArrayList<ChatVoice>();
    private Context context;
    private String friendName;
    private int i = 0,j = 0;

    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public void add(ChatPicture object) {
        //System.out.printf("sharpic%s", i);
        add(new ChatMessage(object.left, ("sharepic" + i++)));
        chatPictureList.add(object);
        super.add(object);
    }

    public void add(ChatVoice object) {
        //System.out.printf("sharpic%s", i);
        add(new ChatMessage(object.left, ("sharevoice" + j++)));
        chatVoiceList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public String getFriendName() {
        return this.friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getMessageItem(int index) {
        return this.chatMessageList.get(index);
    }

    public ChatPicture getPictureItem(int index) {
        return this.chatPictureList.get(index);
    }

    public ChatVoice getVoiceItem(int index) {
        return this.chatVoiceList.get(index);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getMessageItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.right, parent, false);
        } else {
            row = inflater.inflate(R.layout.left, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        if (chatMessageObj.message.contains("sharepic")) {
            System.out.printf("Before: %s", position);
            position = Integer.parseInt(chatMessageObj.message.replace("sharepic", ""));
            System.out.printf("After: %s", position);
            ChatPicture chatPictureObj = getPictureItem(position);
            chatText.setText(chatPictureObj.picture);
            chatText.setTypeface(null, Typeface.NORMAL);
        } else if(chatMessageObj.message.contains("sharevoice")){
            System.out.printf("Before: %s", position);
            position = Integer.parseInt(chatMessageObj.message.replace("sharevoice", ""));
            System.out.printf("After: %s", position);
            ChatVoice chatVoiceObj = getVoiceItem(position);
            chatText.setText(chatVoiceObj.builder);
            chatText.setTypeface(null, Typeface.BOLD);
        } else{
            chatText.setText(chatMessageObj.message);
            chatText.setTypeface(null, Typeface.NORMAL);
        }

        return row;
    }
}
