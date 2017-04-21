package com.cs656chatapp.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    private static String root = null;
    private static String imageFolderPath = null;
    private String imageName = null;
    private static Uri fileUri = null;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public ChatFragment() {
    }

    public static boolean verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        verifyStoragePermissions(getActivity());
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
                        if (which == 0) { //Take a new picture
                            root = Environment.getExternalStorageDirectory().toString()
                                    + "/cs656chatapp";

                            // Creating folders for Image
                            imageFolderPath = root + "/saved_images";
                            File imagesFolder = new File(imageFolderPath);
                            imagesFolder.mkdirs();

                            // Generating file name
                            imageName = "to" + friendsName + ".png";

                            // Creating image here
                            File image = new File(imageFolderPath, imageName);

                            fileUri = Uri.fromFile(image);
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            startActivityForResult(takePictureIntent, 0);
                        } else if (which == 1) { //Pick a photo from gallery
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, 1);
                        } else if (which == 2) {
                            //TODO: Voice recording here
                        }
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

    protected String getFriendName() {
        return chatArrayAdapter.getFriendName();
    }

    protected void printChatText(boolean left, String text) {
        chatArrayAdapter.add(new ChatMessage(left, text));
    }

    protected void printChatPic(boolean left, Bitmap imageBitmap) {
        SpannableStringBuilder ssb = new SpannableStringBuilder("picture");
        BitmapDrawable bmpDrawable = new BitmapDrawable(imageBitmap);
        //System.out.printf("Width: %s\nHeight: %s\n", bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
        bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth() * 10, bmpDrawable.getIntrinsicHeight() * 10);
        // create and set imagespan
        ssb.setSpan(new ImageSpan(bmpDrawable, "picture", ImageSpan.ALIGN_BASELINE), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        chatArrayAdapter.add(new ChatPicture(left, ssb));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                Bitmap imageBitmap = null;
                if (requestCode == 0) { //Took a picture
                    try {
                        GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
                        imageBitmap = getImageThumbnail.getThumbnail(fileUri, context);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.printf("imageBitmap: %s\nimageBitmap size: %s\n", imageBitmap, imageBitmap.getByteCount());
                } else if (requestCode == 1) { //Uploading a picture
                    Uri pickedImage = data.getData();
                    GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
                    imageBitmap = getImageThumbnail.getThumbnail(pickedImage, context);
                }
                printChatPic(true, imageBitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                System.out.printf("encodedImage size: %s\n", encodedImage.length());

                UserObject user = new UserObject();
                user.setOperation("Send Pic:" + friendsName);
                user.setMessage("Picture");
                user.setEncodedImage(encodedImage);
                serverConnection.sendToServer(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}