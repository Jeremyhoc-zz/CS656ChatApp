package com.cs656chatapp.client;


import android.content.Intent;
import android.os.Bundle;
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

    View listView;
    String[] buddies= new String[]{"These","are" ,"sample","values"};
    int mCurPosition;
    Intent intent;
    UserObject user;
    String buddy_list,requests;


    public BuddyListFragment(){

    }

  @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_buddy_list, container, false);

      intent = getActivity().getIntent();
      user =  (UserObject) intent.getSerializableExtra("userObject");
      buddy_list = intent.getStringExtra("Buddies");
      requests = intent.getStringExtra("Requests");

          System.out.println("BUDDY LIST Buddy List recieved: " + buddy_list);
          // if(!buddy_list.isEmpty()) loadBuddyList(buddy_list.split(","));

          System.out.println("BUDDY LIST Requests recieved: " + requests);
          if (!requests.equals("none"))
              Toast.makeText(getActivity(), "You have requests!", Toast.LENGTH_LONG).show();

      //String bud = getArguments().getString("from Main");



      if(!(buddy_list==null)) buddies = buddy_list.split(",");

      System.out.println("FROM BUDDY LIST FRAG: "+buddy_list);

        return rootView;
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
