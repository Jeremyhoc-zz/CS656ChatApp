package com.cs656chatapp.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

/**
 * Created by Jeremy on 4/13/2017.
 */

public class ChatFragment extends Fragment {
    private final String TAG = "ChatActivity";

    View rootView;
    Context context;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private ImageView attachIcon;
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
        chatArrayAdapter.setFriendName(friendsName);
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

        attachIcon = (ImageView) rootView.findViewById(R.id.attach);

        attachIcon.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                final CharSequence[] items = new CharSequence[]{"Take picture", "Attach picture", "Record voice"};
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Pick an option");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            startActivityForResult(intent, 1);
                        } else if (which == 1) {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            //photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, 1);
                        } else if (which == 2) {

                        }
                        // the user clicked on colors[which]
                    }
                });
                builder.show();
            }
        });

        chatText = (EditText) rootView.findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener()

        {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        buttonSend = (Button) rootView.findViewById(R.id.send);
        buttonSend.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View arg0) {
                if (!chatText.getText().toString().equals("")) {
                    sendChatMessage();
                } else {
                    Toast.makeText(context, "Type message before sending ", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    protected boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(true, chatText.getText().toString()));
        UserObject user = new UserObject();
        user.setOperation("Send Text:" + friendsName);
        user.setMessage(chatText.getText().toString());
        serverConnection.sendToServer(user);

        chatText.setText("");
        return true;
    }

    protected void printChatText(boolean left, String text) {
        chatArrayAdapter.add(new ChatMessage(left, text));
    }

    protected String getFriendName() {
        return chatArrayAdapter.getFriendName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                System.out.println(extras.get("data"));
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                SpannableStringBuilder ssb = new SpannableStringBuilder("picture");
                BitmapDrawable bmpDrawable = new BitmapDrawable(imageBitmap);
                System.out.printf("Width: %s\nHeight: %s\n", bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
                bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth()*10, bmpDrawable.getIntrinsicHeight()*10);
                // create and set imagespan
                ssb.setSpan(new ImageSpan(bmpDrawable, "picture", ImageSpan.ALIGN_BASELINE), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                chatArrayAdapter.add(new ChatPicture(true, ssb));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}