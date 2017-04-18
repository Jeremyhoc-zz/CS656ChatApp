package com.cs656chatapp.client;

import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.cs656chatapp.common.UserObject;

/**
 * Created by Jeremy on 4/13/2017.
 */

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatActivity";

    View rootView;
    Context context;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String friendsName;
    private boolean side = false;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        listView = (ListView) rootView.findViewById(R.id.msgview);

        friendsName = getArguments().getString("friendName");
        UserObject user = new UserObject();
        user.setOperation("Retrieve Messages");
        user.setMessage(friendsName);
        serverConnection.sendToServer(user);
        chatArrayAdapter = new ChatArrayAdapter(context, R.layout.right);
        chatArrayAdapter.add(new ChatMessage(true, "Message 1"));
        chatArrayAdapter.add(new ChatMessage(false, "Message 2"));
        chatArrayAdapter.add(new ChatMessage(false, "Message 3"));
        chatArrayAdapter.add(new ChatMessage(true, "Message 4"));
        listView.setAdapter(chatArrayAdapter);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setSelection(chatArrayAdapter.getCount());

        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        chatText = (EditText) rootView.findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        buttonSend = (Button) rootView.findViewById(R.id.send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!chatText.getText().toString().equals("")) {
                    sendChatMessage();
                }
            }
        });

        return rootView;
    }

    protected boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        UserObject user = new UserObject();
        user.setOperation("Send Text:" + friendsName);
        user.setMessage(chatText.getText().toString());
        serverConnection.sendToServer(user);

        chatText.setText("");
        side = !side;
        return true;
    }
}