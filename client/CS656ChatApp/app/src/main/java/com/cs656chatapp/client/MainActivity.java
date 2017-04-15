package com.cs656chatapp.client;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends Activity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks  {

    Intent intent;
    UserObject user;
    String buddy_list, requests_list,sent_list;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------Get userObject and load buddy list-------------Get same UserObject from previous intent to load the buddy list.
        intent = getIntent();
        buddies.clear(); requests.clear(); sent.clear();
        user = (UserObject)intent.getSerializableExtra("userObject0");
        buddy_list = intent.getStringExtra("Buddies0");
        for (String bl : buddy_list.split(",")) buddies.add(bl);
        if(buddies.contains("No friends")) buddies.clear();
        requests_list = intent.getStringExtra("Requests0");
        for (String req : requests_list.split(",")) requests.add(req);
        if(requests.contains("none")) requests.clear();
        sent_list = intent.getStringExtra("Sent0");
        for (String sen : sent_list.split(",")) sent.add(sen);
        if(sent.contains("nobody")) sent.clear();
        System.out.println("From MAINACTIVITY: username= " + user.getUsername() +
                "\nBuddies= "+buddy_list+"\nRequests= "+requests_list+"\nSent= "+sent_list);
        System.out.println("Also from Main:\nBuddies="+buddies+" Requests="+requests+" Sent="+sent);
        intent.putExtra("userObject", user);
        intent.putExtra("Requests",requests_list);
        intent.putExtra("Buddies",buddy_list);
        intent.putExtra("Sent",sent_list);

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
                } else if (operation.equals("Text Received")) {
                    interceptText(message);
                } else if (operation.equals("Pic Received")) {
                    interceptPic(message);
                } else if (operation.equals("Voice Received")) {
                    interceptVoice(message);
                } else if (operation.equals("Friend Logged On")) {
                    addFriendToList(message);
                } else if (operation.equals("Friend Logged Off")) {
                    removeFriendFromList(message);
                } else if (operation.equals("New Friend Request")) {        //in use
                    System.out.println("FINALLLLY!");
                    receiveFriendRequest(message);
                } else if (operation.equals("Response to Friend Request")) {
                    responseToFriendRequest(message);
                } else if (operation.equals("Take Buddy List")) {
                    buddy_list = user.getMessage();
                    intent.putExtra("Buddies",buddy_list);
                }  else if (operation.equals("Friend Logged On")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    buddies.add(friend);
                } else if (operation.equals("Friend Logged Off")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    for (Iterator<String> iterator = buddies.iterator(); iterator.hasNext();) {
                        String string = iterator.next();
                        if (string.equalsIgnoreCase(friend)) {
                            // Remove the current element from the iterator and buddies.
                            iterator.remove();
                        }
                    }
                }  else if (operation.equals("Update Buddy List")) {
                    String friend = user.getMessage(); //Who to apply the action on
                    buddies.add(friend);
                }  else if (operation.equals("Update Sent List")) {
                    sent.add(user.getMessage());
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
/*
    @Override
    public void keepUp(ArrayList<String> b,ArrayList<String> r,ArrayList<String> s){
        buddies = b;
        requests = r;
        sent = s;
    }*/

    /*   //Code to make a listview item bold

    @Override

    public View adapter.getView(int position, View convertView, ViewGroup parent){
       // View returnedView = super.getView;
        View returnedView = super.getView(position, convertView, parent);

        TextView text = (TextView)returnedView.findViewById(R.id.text);
        text.setTypeface(null, Typeface.BOLD);
    }
    */

    //UNUSED
    protected void getRequests(){
        user.setOperation("Get Request List");
        user = serverConnection.sendToServer(user);
    }

    protected void interceptText(String friend) {
        String[] msgSplit = friend.split(",");
        String friendName = msgSplit[0];
        String message = msgSplit[1];
        //Update conversation between you and friend here with new message
    }

    protected void interceptPic(String friend) {
        String[] msgSplit = friend.split(",");
        String friendName = msgSplit[0];
        String message = msgSplit[1];
        //Update conversation between you and friend here with new pic
    }

    protected void interceptVoice(String friend) {
        String[] msgSplit = friend.split(",");
        String friendName = msgSplit[0];
        String message = msgSplit[1];
        //Update conversation between you and friend here with new voice
    }

    protected void receiveFriendRequest(String friend) {
        if(!requests.contains("none")) requests.add(friend);
        else{
            requests.clear();
            requests.add(friend);
        }
        Toast.makeText(MainActivity.this, "New Friend Request from "+friend, Toast.LENGTH_LONG).show();
       // String strangerUsername = friend;
        //Create an area where we can accept/reject the friendship request from strangerUsername
    }

    protected void responseToFriendRequest(String friend) {
        String[] msgSplit = friend.split(",");
        String possiblyMyFriendName = msgSplit[0]; //Who is responding
        String acceptedOrRejected = msgSplit[1]; //Was it accepted or rejected?
        if (acceptedOrRejected.equals("accepted")) {
            addFriendToList(possiblyMyFriendName);
            //Remove friend request with a positive or make it green.
        } else if (acceptedOrRejected.equals("rejected")) {
            //Remove friend request with a negative or make it red.
        }
        //Create an area where we can view whether friendship requests have been accepted or rejected.
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

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container,
                        PlaceholderFragment.newInstance(position + 1)).commit();
        if(position == 0) {
            BuddyListFragment buddyListFragment = new BuddyListFragment();
            buddyListFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frag_container,buddyListFragment).commit();
        } if(position == 1) {
            ChangeBuddiesFragment changeBuddiesFragment = new ChangeBuddiesFragment();
            changeBuddiesFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frag_container,changeBuddiesFragment).commit();
        }if(position == 2) {
            ProfileFragment firstFragment = new ProfileFragment();
            //  firstFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().replace(R.id.frag_container,firstFragment).commit();
            // getFragmentManager().beginTransaction().add(R.id.frag_container, firstFragment).commit();
        } if(position == 3) {
            bundle.putString("Buddies", buddy_list);
            bundle.putString("Requests", requests_list);
            ChangeBuddiesFragment changeBuddiesFragment = new ChangeBuddiesFragment();
            changeBuddiesFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frag_container,changeBuddiesFragment).commit();
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

    /*public boolean onCreateBuddyList() {
        String friends = user.getMessage();
        String friend = friends.split(",");
        loadBuddyList(friend);
    }*/

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
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

    }

}
