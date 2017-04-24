package com.cs656chatapp.client;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

import java.util.ArrayList;

import static com.cs656chatapp.client.MainActivity.buddies;
import static com.cs656chatapp.client.MainActivity.requests;
import static com.cs656chatapp.client.MainActivity.sent;

/**
 * Created by shereen on 4/12/2017.
 */

public class ChangeBuddiesFragment extends Fragment {

    ListView listView;

    Button friendButton,findButton,requestsButton,sentButton;
    UserObject user;
    String buddy_list, requests_list,sent_list;
    EditText FindUsername;
    String findUsername = "";
    AlertDialog.Builder builder;
    CharSequence choices[] = new CharSequence[]{"Delete","Keep"};
    CharSequence choices1[] = new CharSequence[]{"Accept","Reject"};
    Intent intent;
    Context context;
    View rootView;

    private BroadcastReceiver receiver;

    public ChangeBuddiesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        context = getActivity().getApplicationContext();
        rootView = inflater.inflate(R.layout.fragment_change_buddies, container, false);
        listView = (ListView) rootView.findViewById(R.id.senderList);

        user = new UserObject();

        buddy_list = intent.getStringExtra("Buddies");
        requests_list = intent.getStringExtra("Requests");
        user = (UserObject) intent.getSerializableExtra("userObject");
        sent_list = intent.getStringExtra("Sent");


        System.out.println("CHANGE BUDDIES Username: " + user.getUsername());
        System.out.println("CHANGE BUDDIES Buddy List received: " + buddy_list);
        System.out.println("CHANGE BUDDIES Requests received: " + requests_list);
        System.out.println("CHANGE BUDDIES Sent list received: " + sent_list);
        System.out.println("This too buddies: " + buddies);
        System.out.println("This too requests: " + requests);
        System.out.println("This too sent: " + sent);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                if (operation.equals("User Does Not Exist")) {
                    Toast.makeText(context,user.getMessage(),Toast.LENGTH_LONG).show();
                }else if (operation.equals("Friend Request Sent")) {                  //<--CHANGE user exists
                    Toast.makeText(getActivity().getApplicationContext(),user.getMessage(),Toast.LENGTH_LONG).show();
                    sent.add(findUsername); loadSentList();
                }else if (operation.equals("New Friend Request")) {        //in use
                    loadRequestList();
                }else if (operation.equals("Response to Friend Request")) {
                   loadRequestList(); loadFriendsList();
                }else if(operation.equals("Remove from Buddy List")){
                    loadFriendsList();
                }else if(operation.equals("Remove from Request List")){
                    loadRequestList();
                }
            }
        };


        requestsButton = (Button) rootView.findViewById(R.id.requests_button);
        requestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            loadRequestList();
            }
        });
        friendButton = (Button) rootView.findViewById(R.id.friends_button);
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFriendsList();
            }
        });
        sentButton = (Button) rootView.findViewById(R.id.sent_button);
        sentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadSentList();
            }
        });

        findButton = (Button) rootView.findViewById(R.id.friend_request_button1);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindUsername = (EditText) rootView.findViewById(R.id.friend_request_username1);
                findUsername = FindUsername.getText().toString();
                if(findUsername.equals(user.getUsername())){
                    Toast.makeText(getActivity().getApplicationContext(),"You can't friend yourself!",Toast.LENGTH_SHORT).show();
                }
                else if(buddies.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),"You are already friends with "+findUsername,Toast.LENGTH_SHORT).show();
                }
                else if(requests.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),findUsername+" has already sent you a request.",Toast.LENGTH_SHORT).show();
                }
                else if(sent.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),"Friend request to "+findUsername+" already sent!",Toast.LENGTH_SHORT).show();
                }
                else{
                    user.setOperation("Friend Request");
                    user.setMessage("Send Friend Request,"+findUsername);
                    user = serverConnection.sendToServer(user);
                }
            }
        });

        loadRequestList();

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



    protected void loadRequestList(){

        changeButtons(1);

            ArrayAdapter<String>adapter = new ArrayAdapter<String>(context, R.layout.request_list_item, R.id.req_text1, requests);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String itemValue = (String) listView.getItemAtPosition(position);
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Accept Friend Request from "+itemValue+"?");
                    builder.setItems(choices1,new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            if(which==0){
                                System.out.println("Result: "+choices[0]);
                                user.setOperation("Friend Request");
                                user.setMessage("Respond to Friend Request,"+itemValue+",Accept");
                                user = serverConnection.sendToServer(user);
                                requests.remove(itemValue);
                                buddies.add(itemValue);
                                loadRequestList();
                            }
                            if(which==1){
                                System.out.println("Result: "+choices[1]);
                                user.setOperation("Friend Request");
                                user.setMessage("Respond to Friend Request,"+itemValue+",Reject");
                                user = serverConnection.sendToServer(user);
                                requests.remove(itemValue);
                                loadRequestList();
                            }
                        }
                    });builder.show();
                    }
        });
    }


    protected void loadSentList(){

        changeButtons(3);
        listView = (ListView)rootView.findViewById(R.id.senderList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,R.layout.request_list_item,R.id.req_text1,sent);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                final String itemValue = (String) listView.getItemAtPosition(position);
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete request sent to "+itemValue+"?");
                builder.setItems(choices,new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        if(which==0){
                            System.out.println("Result: "+choices[0]);
                            user.setMessage(itemValue);
                            user.setOperation("Delete Request");
                            sent.remove(itemValue); loadSentList();
                            user = serverConnection.sendToServer(user);
                        }
                        if(which==1){
                            System.out.println("Result: "+choices[1]);

                        }
                    }
                });builder.show();
            }
        });
    }

    protected void loadFriendsList(){

        changeButtons(2);

        listView = (ListView)rootView.findViewById(R.id.senderList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,R.layout.request_list_item,R.id.req_text1,buddies);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                final String itemValue = (String) listView.getItemAtPosition(position);
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Remove "+itemValue+" from Friends list?");
                builder.setItems(choices,new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        if(which==0){       //DELETE FRIEND
                            System.out.println("Result: "+choices[0]);
                            user.setMessage(itemValue);
                            user.setOperation("Delete Friend");
                            user = serverConnection.sendToServer(user);
                            buddies.remove(itemValue); loadFriendsList();
                        }
                        if(which==1){
                            System.out.println("Result: "+choices[1]);

                        }
                    }
                });builder.show();
            }
        });
    }

    void changeButtons(int a){ //1- Requests 2-Friends 3-Sent
        if(a==1) {
            requestsButton.setBackgroundColor(requestsButton.getContext().getResources().getColor(R.color.LimeGreen));
            friendButton.setBackgroundColor(friendButton.getContext().getResources().getColor(R.color.MyGray));
            sentButton.setBackgroundColor(sentButton.getContext().getResources().getColor(R.color.MyGray));
        }else if(a==2){
            friendButton.setBackgroundColor(friendButton.getContext().getResources().getColor(R.color.LimeGreen));
            requestsButton.setBackgroundColor(requestsButton.getContext().getResources().getColor(R.color.MyGray));
            sentButton.setBackgroundColor(sentButton.getContext().getResources().getColor(R.color.MyGray));
        }else{  //a=3
            sentButton.setBackgroundColor(sentButton.getContext().getResources().getColor(R.color.LimeGreen));
            friendButton.setBackgroundColor(friendButton.getContext().getResources().getColor(R.color.MyGray));
            requestsButton.setBackgroundColor(requestsButton.getContext().getResources().getColor(R.color.MyGray));

        }
    }

}
