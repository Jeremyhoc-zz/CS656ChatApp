package com.cs656chatapp.client;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Fragment;
import android.widget.EditText;

import com.cs656chatapp.common.UserObject;

/**
 * Created by shereen on 4/12/2017.
 */

public class ChangeBuddiesFragment extends Fragment {

    ListView listView;
    Button unFriendButton,findButton;
    UserObject user;
    String buddy_list;
    String friends[];
    EditText FindUsername;
    String findUsername="",findUsernameOld, requests, recipients;
    Intent i;
    AlertDialog.Builder builder;
    CharSequence choices[] = new CharSequence[]{"Delete","Keep"};
    Intent intent;
    Context context;
    View rootView;

    private BroadcastReceiver receiver;

    public ChangeBuddiesFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        context = getActivity().getApplicationContext();
        rootView = inflater.inflate(R.layout.fragment_change_buddies, container, false);
        listView = (ListView)rootView.findViewById(R.id.senderList);

       // user = new UserObject();
        buddy_list = getArguments().getString("Buddies");
        requests = getArguments().getString("Requests");
        user =  (UserObject) intent.getSerializableExtra("userObject");

      //  buddy_list= (String)intent.getSerializableExtra("buddyList1");
      //  requests= (String)intent.getSerializableExtra("Requests1");

        System.out.println("I have these buddies and my name is "+user.getUsername()+"\n"+buddy_list+"\n"+requests);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                if (operation.equals("User Does Not Exist")) {
                    Toast.makeText(getActivity().getApplicationContext(),user.getMessage(),Toast.LENGTH_LONG).show();
                }else if (operation.equals("Take List")) {
                    Toast.makeText(getActivity().getApplicationContext(),"List Updated",Toast.LENGTH_LONG).show();
                    recipients = user.getMessage();
                    System.out.println("Recipients recieved: "+recipients);
                    if(!user.getMessage().equals("nobody")) loadSentList(recipients.split(","));
                }else if (operation.equals("Sent Request Deleted")) {
                    Toast.makeText(getActivity().getApplicationContext(),"Request Deleted",Toast.LENGTH_SHORT).show();
                    getSentRequests();
                }else if (operation.equals("Take Buddy List")) {
                    Toast.makeText(getActivity().getApplicationContext(),"Deleted Successfully",Toast.LENGTH_SHORT).show();
                    buddy_list=user.getMessage();
                }
            }
        };

        getSentRequests();

        unFriendButton = (Button) rootView.findViewById(R.id.unfriend_button);
        unFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friends = buddy_list.split(",");
                builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Tap on a friend to remove them.");
                builder.setItems(friends,new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        deleteFriend(friends[which]);
                        Toast.makeText(getActivity().getApplicationContext(),"Attempting to unfriend "+friends[which],Toast.LENGTH_SHORT).show();
                    }
                });builder.show();
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
                else if(buddy_list.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),"You are already friends with "+findUsername,Toast.LENGTH_SHORT).show();
                }
                else if(requests.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),findUsername+" has already sent you a request.",Toast.LENGTH_SHORT).show();
                }
                else if(recipients.contains(findUsername)){
                    Toast.makeText(getActivity().getApplicationContext(),"Friend request to "+findUsername+" already sent!",Toast.LENGTH_LONG).show();
                }
                else{
                    user.setOperation("Request Friend");
                    user.setMessage(findUsername);
                    user = serverConnection.sendToServer(user);
                }
            }
        });

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

    protected void deleteFriend(String recipientDel){
        user.setMessage(recipientDel);
        user.setOperation("Delete Friend");
        user = serverConnection.sendToServer(user);
    }

    protected void deleteSentRequest(String recipientDel){
        user.setMessage(recipientDel);
        user.setOperation("Delete Request");
        user = serverConnection.sendToServer(user);
    }
    protected void onClick(final String val){

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete request sent to "+val+"?");
        builder.setItems(choices,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                if(which==0){
                    System.out.println("Result: "+choices[0]);
                    deleteSentRequest(val);
                }
                if(which==1){
                    System.out.println("Result: "+choices[1]);

                }
            }
        });builder.show();
    }

    protected void getSentRequests(){

        user.setOperation("Get Sent List");
        user = serverConnection.sendToServer(user);
    }
    protected void loadSentList(String[] str){

        System.out.println("This is the first on list: "+str[0]);
        listView = (ListView)rootView.findViewById(R.id.senderList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,R.layout.request_list_item,R.id.req_text1,str);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) listView.getItemAtPosition(position);
                onClick(itemValue);
            }
        });
    }



}
