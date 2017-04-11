package com.cs656chatapp.client;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

public class ChangeBuddiesActivity extends AppCompatActivity {

    ListView listView;
    Button newButton,findButton;
    UserObject user;
    String buddy_list;
    Intent intent;
    EditText FindUsername;
    String findUsername="",findUsernameOld, requests, recipients;
    Intent i;
    AlertDialog.Builder builder;
    CharSequence choices[] = new CharSequence[]{"Delete","Keep"};

    private BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_buddies);

        user = new UserObject();
        intent = getIntent();
        user = (UserObject)intent.getSerializableExtra("userObject1");
        buddy_list= (String)intent.getSerializableExtra("buddyList1");
        requests= (String)intent.getSerializableExtra("Requests1");

        System.out.println("I have these buddies and my name is "+user.getUsername()+"\n"+buddy_list+"\n"+requests);

        //   i = new Intent(ChangeBuddiesActivity.this, serverListener.class);
        //  this.startService(i);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                if (operation.equals("User Does Not Exist")) {
                    Toast.makeText(getApplicationContext(),user.getMessage(),Toast.LENGTH_LONG).show();
                }else if (operation.equals("Take List")) {
                    Toast.makeText(getApplicationContext(),"List Updated",Toast.LENGTH_LONG).show();
                    recipients = user.getMessage();
                    System.out.println("Recipients recieved: "+recipients);
                    if(!user.getMessage().equals("nobody")) loadSentList(recipients.split(","));
                }else if (operation.equals("Sent Request Deleted")) {
                    Toast.makeText(getApplicationContext(),"Request Deleted",Toast.LENGTH_SHORT).show();
                    getSentRequests();
                }
            }
        };

        getSentRequests();

        findButton = (Button) findViewById(R.id.friend_request_button1);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindUsername = (EditText) findViewById(R.id.friend_request_username1);
                findUsername = FindUsername.getText().toString();
                if(findUsername.equals(user.getUsername())){
                    Toast.makeText(getApplicationContext(),"You can't friend yourself!",Toast.LENGTH_SHORT).show();
                }
                else if(buddy_list.contains(findUsername)){
                   Toast.makeText(getApplicationContext(),"You are already friends with "+findUsername,Toast.LENGTH_SHORT).show();
                }
                else if(requests.contains(findUsername)){
                    Toast.makeText(getApplicationContext(),findUsername+" has already sent you a request.",Toast.LENGTH_SHORT).show();
                }
                else if(recipients.contains(findUsername)){
                    Toast.makeText(getApplicationContext(),"Friend request to "+findUsername+" already sent!",Toast.LENGTH_LONG).show();
                }
                else{
                    user.setOperation("Request Friend");
                    user.setMessage(findUsername);
                    user = serverConnection.sendToServer(user);
                }
            }
        });

        newButton = (Button) findViewById(R.id.go_back1);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ChangeBuddiesActivity.this, MainActivity.class);
                intent.putExtra("userObject0", user);
                startActivity(intent);
                ChangeBuddiesActivity.this.finish();
            }
        });

        builder = new AlertDialog.Builder(this);
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

    protected void deleteSentRequest(String recipientDel){
        user.setMessage(recipientDel);
        user.setOperation("Delete Request");
        user = serverConnection.sendToServer(user);
    }
    protected void onClick(final String val){

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
        listView = (ListView)findViewById(R.id.senderList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.request_list_item,R.id.req_text1,str);
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
