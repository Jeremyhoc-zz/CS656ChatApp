package com.cs656chatapp.client;

import com.cs656chatapp.common.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import android.content.IntentFilter;

public class MainActivity extends Activity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks  {

    ListView listView;
    Intent intent;
    UserObject user;
    String buddy_list;
    TextView textView;
    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        //--------------Get userObject and load buddy list-------------Get same UserObject from previous intent to load the buddy list.
        intent = getIntent();

        user = (UserObject)intent.getSerializableExtra("userObject");
        buddy_list = user.getMessage();
        getIntent().putExtra("userObject", user);

        loadBuddyList(buddy_list.split(","));

        //--------------End buddy list load-------------

        Intent i = new Intent(MainActivity.this, serverListener.class);
        this.startService(i);

        receiver = new BroadcastReceiver() {
        @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
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
                } else if (operation.equals("New Friend Request")) {
                    receiveFriendRequest(message);
                } else if (operation.equals("Response to Friend Request")) {
                    responseToFriendRequest(message);
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

    /*   //Code to make a listview item bold

    @Override

    public View adapter.getView(int position, View convertView, ViewGroup parent){
       // View returnedView = super.getView;
        View returnedView = super.getView(position, convertView, parent);

        TextView text = (TextView)returnedView.findViewById(R.id.text);
        text.setTypeface(null, Typeface.BOLD);
    }
    */

    protected void loadBuddyList(String[] buddies){
        listView = (ListView)findViewById(R.id.buddyList);

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(this,R.layout.bud_list_item,R.id.bud_text1,buddies);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),"Position: "+itemPosition+" ListItem: "+itemValue,Toast.LENGTH_SHORT).show();

            }
        });
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
        String strangerUsername = friend;
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

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container,
                        PlaceholderFragment.newInstance(position + 1)).commit();
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
