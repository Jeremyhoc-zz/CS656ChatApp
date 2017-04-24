package com.cs656chatapp.client;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class MainActivity extends Activity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    Intent intent;
    UserObject user;
    String buddy_list, requests_list, sent_list;
    Intent i;

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment changeBuddiesFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private BroadcastReceiver receiver;
    static public ArrayList<String> buddies = new ArrayList<String>();
    static public ArrayList<String> requests = new ArrayList<String>();
    static public ArrayList<String> sent = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (buddies.size() > 0) buddies.clear();
        if (requests.size() > 0) requests.clear();
        //--------------Get userObject and load buddy list-------------Get same UserObject from previous intent to load the buddy list.
        intent = getIntent();
        buddies.clear();
        requests.clear();
        sent.clear();
        user = (UserObject) intent.getSerializableExtra("userObject0");
        try {
            if (!(user.getMessage()).equals("New User")) {
                buddy_list = intent.getStringExtra("Buddies0");
                for (String bl : buddy_list.split(",")) buddies.add(bl);
                if (buddies.contains("No friends")) buddies.clear();
                requests_list = intent.getStringExtra("Requests0");
                for (String req : requests_list.split(",")) requests.add(req);
                if (requests.contains("none")) requests.clear();
                sent_list = intent.getStringExtra("Sent0");
                for (String sen : sent_list.split(",")) sent.add(sen);
                if (sent.contains("nobody")) sent.clear();
            }
        } catch (Exception e) {
            UserObject user = new UserObject();
            user.setOperation("Log Out");
            serverConnection.sendToServer(user);
            this.stopService(i);
            e.printStackTrace();
        }
        System.out.println("From MAINACTIVITY: username= " + user.getUsername() +
                "\nBuddies= " + buddy_list + "\nRequests= " + requests_list + "\nSent= " + sent_list);
        System.out.println("Also from Main:\nBuddies=" + buddies + " Requests=" + requests + " Sent=" + sent);
        intent.putExtra("userObject", user);
        intent.putExtra("Requests", requests_list);
        intent.putExtra("Buddies", buddy_list);
        intent.putExtra("Sent", sent_list);

        //getBuddyList();
        //--------------End buddy list load-------------

        i = new Intent(MainActivity.this, serverListener.class);
        this.startService(i);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                if (operation.equals("Text")) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
                            .show();
                } else if (operation.contains("Receive Text:")) {
                    String from = operation.split(":")[1];
                    interceptText(from, message);
                } else if (operation.contains("Receive Pic:")) {
                    String encodedImage = intent.getStringExtra(serverListener.serverEncodedImage);
                    String from = operation.split(":")[1];
                    interceptPic(from, encodedImage);
                } else if (operation.contains("Receive Voice:")) {
                    String encodedVoice = intent.getStringExtra(serverListener.serverEncodedVoice);
                    String from = operation.split(":")[1];
                    try{interceptVoice(from,encodedVoice);}
                    catch(Exception e){ e.printStackTrace();}
                } else if (operation.equals("Friend Logged On")) {
                    addFriendToList(message);
                } else if (operation.equals("Friend Logged Off")) {
                    removeFriendFromList(message);
                } else if (operation.equals("New Friend Request")) {        //in use
                    receiveFriendRequest(message);
                } else if (operation.equals("Response to Friend Request")) {
                    responseToFriendRequest(message);
                } else if (operation.equals("Remove from Buddy List")) {        //in use
                    buddies.remove(message);
                    Toast.makeText(MainActivity.this, message + " has removed you from their list.", Toast.LENGTH_LONG).show();
                } else if (operation.equals("Remove from Request List")) {        //in use
                    requests.remove(message);
                    Toast.makeText(MainActivity.this, message + " has taken back their request.", Toast.LENGTH_LONG).show();
                } else if (operation.equals("Take Buddy List")) {
                    buddy_list = user.getMessage();
                    intent.putExtra("Buddies", buddy_list);
                } else if (operation.equals("Friend Logged On")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    buddies.add(friend);
                } else if (operation.equals("Friend Logged Off")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    for (Iterator<String> iterator = buddies.iterator(); iterator.hasNext(); ) {
                        String string = iterator.next();
                        if (string.equalsIgnoreCase(friend)) {
                            // Remove the current element from the iterator and buddies.
                            iterator.remove();
                        }
                    }
                } else if (operation.equals("Update Buddy List")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    buddies.add(friend);
                } else if (operation.equals("Update Sent List")) {
                    sent.add(user.getMessage());
                } else if (operation.contains("Chat History:")) {
                    String encodedImages[] = intent.getStringArrayExtra(serverListener.serverEncodedImages);
                    String friendName = operation.split(":")[1];
                    chatHistory(friendName, message, encodedImages);
                }
            }
        };

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(serverListener.serverResult)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    protected void chatHistory(String friendName, String message, String[] encodedImages) {
        if (message != null) {
            System.out.printf("%s\n", message);
            ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.frag_container);
            String[] msgSplit = message.split(",,,");
            System.out.printf("Chat History with %s\n", friendName);
            int clientID = user.getUserID();
            int j = 0;
            for (int i = 0; i < msgSplit.length - 1; ) {
                String from_uid = msgSplit[i++];
                String message_type = msgSplit[i++];
                boolean left = false;
                //System.out.printf("from_uid=%s\nmessage_type=%s\ncontent=%s\n", from_uid, message_type, content);

                if (Integer.parseInt(from_uid) == clientID) { //from_uid
                    left = true;
                }
                if (message_type.equals("text")) { //message_type
                    //post text content
                    String content = msgSplit[i++];
                    fragment.printChatText(left, content);
                } else if (message_type.equals("pic")) {
                    //post pic content
                    String encodedImage = encodedImages[j++];
                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    fragment.printChatPic(left, bitmap);
                } else if (message_type.equals("voice")) {
                    //post voice content
                }
            }
        }
    }

    protected void interceptText(String from, String msg) {
        System.out.printf("Incoming message from %s: %s\n", from, msg);
        ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.frag_container);
        if (fragment.getFriendName().equals(from)) {
            fragment.printChatText(false, msg);
        } else {
            Toast.makeText(MainActivity.this, "New message from " + from, Toast.LENGTH_LONG).show();
        }
    }

    protected void interceptPic(String from, String encodedImage) {
        //Update conversation between you and friend here with new pic
        System.out.printf("Incoming picture from %s\n%s\n", from, encodedImage);
        ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.frag_container);
        if (fragment.getFriendName().equals(from)) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            fragment.printChatPic(false, bitmap);
        } else {
            Toast.makeText(MainActivity.this, "New message from " + from, Toast.LENGTH_LONG).show();
        }
    }

    protected void interceptVoice(String from, String encodedVoice) throws IOException, DataFormatException{
        //Update conversation between you and friend here with new voice
        System.out.println("Incoming voice from "+ from + " this is it: \n" + encodedVoice);
        ChatFragment fragment = (ChatFragment) getFragmentManager().findFragmentById(R.id.frag_container);
        if (fragment.getFriendName().equals(from)) {
            byte[] decodedString = Base64.decode(encodedVoice, Base64.DEFAULT);
            byte[] furtherDecodedString = decompress(decodedString);
            fragment.printChatVoice(false, furtherDecodedString);
        } else {
            Toast.makeText(MainActivity.this, "New message from " + from, Toast.LENGTH_LONG).show();
        }

    }

    protected void receiveFriendRequest(String friend) {
        requests.add(friend);
        Toast.makeText(MainActivity.this, "New Friend Request from " + friend, Toast.LENGTH_LONG).show();
    }

    protected void responseToFriendRequest(String friend) {
        String[] mes = friend.split(",");
        if (mes[1].equals("Accept")) {
            sent.remove(mes[0]);
            buddies.add(mes[0]);
            Toast.makeText(MainActivity.this, mes[0] + " has accepted your Friend Request!", Toast.LENGTH_LONG).show();
        } else {
            sent.remove(mes[0]);
            Toast.makeText(MainActivity.this, mes[0] + " has rejected your Friend Request.", Toast.LENGTH_LONG).show();

        }
    }

    protected void addFriendToList(String friend) {
        //Simply add this friend's name to the buddy list.
    }

    protected void removeFriendFromList(String friend) {
        //Simply remove this friend from the buddy list.
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Bundle bundle = new Bundle();
        bundle.putString("Buddies", buddy_list);
        bundle.putString("Requests", requests_list);

        if (position == 0) {
            BuddyListFragment buddyListFragment = new BuddyListFragment();
            buddyListFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frag_container, buddyListFragment).commit();
        }
        if (position == 1) {
            ChangeBuddiesFragment changeBuddiesFragment = new ChangeBuddiesFragment();
            changeBuddiesFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frag_container, changeBuddiesFragment).addToBackStack("requestFrag").commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
/*
        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
 /*   public static class PlaceholderFragment extends Fragment {
        *//**
     * The fragment argument representing the section number for this
     * fragment.
     *//*
        private static final String ARG_SECTION_NUMBER = "section_number";

        *//**
     * Returns a new instance of this fragment for the given section number.
     *//*
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(
                    ARG_SECTION_NUMBER));
        }

    }*/

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        System.out.println("Original: " + data.length);
        System.out.println("Compressed: " + output.length);
        return output;
    }

}
