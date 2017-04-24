package com.cs656chatapp.client;


import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.cs656chatapp.client.MainActivity.requests;


/**
 * Created by shereen on 4/12/2017.
 */

public class BuddyListFragment extends ListFragment {

    Intent intent;
    Context context;
    ListView buddyListView;
    public ArrayList<String> buddies = MainActivity.buddies;

    String buddy_list, requests_list;

    private BroadcastReceiver receiver;

    public BuddyListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_buddy_list, container, false);

        context = getActivity().getApplicationContext();
        intent = getActivity().getIntent();
        buddy_list = intent.getStringExtra("Buddies");
        requests_list = intent.getStringExtra("Requests");

      receiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
              String operation = intent.getStringExtra(serverListener.serverOperation);
              String message = intent.getStringExtra(serverListener.serverMessage);
              if (operation.equals("Response to Friend Request")) {
                  System.out.println("Just from BUDDYLISTFRAG: message="+message);
                  String[] mes = message.split(",");
                  if(mes[1].equals("Accept")) {
                  loadBuddyList();
                  }
              }else if(operation.equals("Remove from Buddy List")){
                  loadBuddyList();
              }
          }
      };

        buddyListView = (ListView) rootView.findViewById(R.id.buddyListView);

        System.out.println("BUDDY LIST Buddy List recieved: " + buddy_list);
        System.out.println("BUDDY LIST Requests received: " + requests_list);
        if (!requests.isEmpty())
            Toast.makeText(getActivity(), "You have requests!", Toast.LENGTH_LONG).show();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver((receiver),
                new IntentFilter(serverListener.serverResult)
        );
        loadBuddyList();
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        super.onStop();
    }

    public void loadBuddyList() {
        ListAdapter myListAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.buddy_list_item,
                R.id.bud_text1,
                buddies);

        buddyListView.setAdapter(myListAdapter);

        buddyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle bundle = new Bundle();
                bundle.putString("friendName", (String) buddyListView.getItemAtPosition(position));
                ChatFragment chatFragment = new ChatFragment();
                chatFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frag_container,chatFragment).addToBackStack("chatFrag").commit();
            }
        });

    }
}
