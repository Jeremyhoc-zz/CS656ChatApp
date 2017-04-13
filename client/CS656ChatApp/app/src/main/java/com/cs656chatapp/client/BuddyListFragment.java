package com.cs656chatapp.client;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.app.ListFragment;
import com.cs656chatapp.common.UserObject;


/**
 * Created by shereen on 4/12/2017.
 */

public class BuddyListFragment extends ListFragment {

    static int i=0;
    View listView;
    String[] buddies= new String[]{"These","are" ,"sample","values"};
    int mCurPosition;
    Intent intent;
    Context context;
    UserObject user;
    String buddy_list,requests;

    private BroadcastReceiver receiver;

    public BuddyListFragment(){

    }

  @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_buddy_list, container, false);

      context = getActivity().getApplicationContext();
      intent = getActivity().getIntent();
      buddy_list = getArguments().getString("from Main");
      if(i==0) buddy_list = intent.getStringExtra("Buddies");
      i++;
      requests = intent.getStringExtra("Requests");

      receiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
              String operation = intent.getStringExtra(serverListener.serverOperation);
              String message = intent.getStringExtra(serverListener.serverMessage);
              user.setMessage(message);
              if (operation.equals("User Does Not Exist")) {
                  //change this
              }else if (operation.equals("Take Buddy List")) {
                  buddy_list=user.getMessage();
                  System.out.println("BuddyFrag: The buddy list from reciever is: "+buddy_list);
              }
          }
      };


      System.out.println("BUDDY LIST Buddy List recieved: " + buddy_list);
      System.out.println("BUDDY LIST Requests recieved: " + requests);
      if (!requests.equals("none"))
              Toast.makeText(getActivity(), "You have requests!", Toast.LENGTH_LONG).show();
      if(!(buddy_list==null)) buddies = buddy_list.split(",");
      System.out.println("FROM BUDDY LIST FRAG: "+buddy_list);

        return rootView;
    }
    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(context).registerReceiver((receiver),
                new IntentFilter(serverListener.serverResult)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

       setListAdapter(new ArrayAdapter<String>(getActivity(),R.layout.buddy_list_item,R.id.bud_text1,buddies));

    }

    protected void getBuddyList(){
        user.setOperation("Get Buddy List");
        user = serverConnection.sendToServer(user);
    }

}
