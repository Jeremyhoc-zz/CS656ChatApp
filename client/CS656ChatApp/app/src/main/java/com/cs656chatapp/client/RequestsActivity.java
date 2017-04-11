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
import android.widget.ListView;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

public class RequestsActivity extends AppCompatActivity {

    UserObject user;
    String requests;
    Button backButton;
    ListView listView;
    AlertDialog.Builder builder;

    private BroadcastReceiver receiver;
    CharSequence choices[] = new CharSequence[]{"Accept","Reject"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        user= new UserObject();
        Intent intent = getIntent();
        user = (UserObject)intent.getSerializableExtra("userObject1");
        requests= (String)intent.getSerializableExtra("Requests1");

        loadRequestList(requests);

        System.out.println("I have these requests and my name is "+user.getUsername()+"\n"+requests);
        //  i = new Intent(RequestActivity.this, serverListener.class);
        //  this.startService(i);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                if (operation.equals("Request List")) {
                    requests=user.getMessage();
                    if(requests.equals("none")){
                        Toast.makeText(RequestsActivity.this, "No Friend Requests", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Request List Updated", Toast.LENGTH_LONG).show();
                    }
                    loadRequestList(requests);
                }
            }
        };

        backButton = (Button) findViewById(R.id.go_back2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RequestsActivity.this, MainActivity.class);
                intent.putExtra("userObject0", user);
                startActivity(intent);
                RequestsActivity.this.finish();
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

    protected void deleteRequest(String str){
        user.setMessage(str);
        user.setOperation("Answer Request");
        user = serverConnection.sendToServer(user);
    }
    protected void onClick(final String val){

        builder.setTitle("Add "+val+" as a friend?");
        builder.setItems(choices,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                if(which==0){
                    //Accept
                    System.out.println("Result: "+choices[0]);
                    deleteRequest("Accept,"+val);
                }
                if(which==1){
                    //Reject
                    System.out.println("Result: "+choices[1]);
                    deleteRequest("Reject,"+val);
                }
            }
        });builder.show();
    }
    protected void loadRequestList(String str){

        String[] str1= str.split(",");

        listView = (ListView)findViewById(R.id.requestList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.request_list_item,R.id.req_text1,str1);
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
