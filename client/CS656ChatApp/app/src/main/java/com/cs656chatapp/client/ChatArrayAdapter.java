package com.cs656chatapp.client;

import android.content.Context;
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
    private Context context;
    private String friendName;
    private int i = 0;

    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public void add(ChatPicture object) {
        add(new ChatMessage(object.left, ("sharepic" + i++)));
        chatPictureList.add(object);
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
            ChatPicture chatPictureObj = getPictureItem(position);
            chatText.setText(chatPictureObj.picture);
        } else {
            chatText.setText(chatMessageObj.message);
        }

        return row;
    }
}
