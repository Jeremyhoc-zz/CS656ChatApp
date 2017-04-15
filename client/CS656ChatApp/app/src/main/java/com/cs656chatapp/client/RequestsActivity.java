package com.cs656chatapp.client;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

import java.util.ArrayList;

/**
 * Created by shereen on 4/12/2017.
 */

public class RequestsActivity extends Fragment {

    UserObject user;
    String requests_list, buddy_list,sent_list;
    ListView listView;
    AlertDialog.Builder builder;
    Context context;
    Intent intent;
    View rootView;
    ArrayAdapter<String> adapter,adapter1;
    protected ArrayList<String> requests = MainActivity.requests;

    private BroadcastReceiver receiver;
    CharSequence choices[] = new CharSequence[]{"Accept","Reject"};

    public RequestsActivity(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        context = getActivity().getApplicationContext();
        rootView = inflater.inflate(R.layout.fragment_requests, container, false);
        listView = (ListView)rootView.findViewById(R.id.requestList);

        requests_list = getArguments().getString("Requests");
        buddy_list = getArguments().getString("Buddies");
        sent_list = getArguments().getString("Sent");
        user =  (UserObject) intent.getSerializableExtra("userObject");

        //String bd= MainActivity.buddy_list;

        System.out.println("FRIENDSHIP I am "+user.getUsername());
        System.out.println("FRIENDSHIP Buddy List recieved: " + buddy_list);
        System.out.println("FRIENDSHIP Requests received: " + requests_list);
        System.out.println("FRIENDSHIP Sent List received: " + sent_list);

        loadList();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String operation = intent.getStringExtra(serverListener.serverOperation);
                String message = intent.getStringExtra(serverListener.serverMessage);
                user.setMessage(message);
                /*if (operation.equals("Take Request List")) {
                    requests = user.getMessage();
                    if(requests.equals("none")){
                        Toast.makeText(getActivity(), "No Friend Requests", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "Request List Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadList();
                }*/
            }
        };

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

    protected void loadList(){

        if(!requests.equals("none")) {
            adapter = new ArrayAdapter<String>(context, R.layout.request_list_item, R.id.req_text1, requests);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String itemValue = (String) listView.getItemAtPosition(position);
                    Toast.makeText(getActivity(), "Clicked item "+itemValue, Toast.LENGTH_SHORT).show();


                }
            });
        }
        else {
            String empty = "\t \t";
            adapter1 = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,empty.split(" "));
            listView.setAdapter(adapter1);
        }
    }

    //UNUSED
    protected void getRequests(){
        user.setOperation("Get Request List");
        user = serverConnection.sendToServer(user);
    }

    protected void deleteRequest(String str){
        user.setMessage(str);
        user.setOperation("Answer Request");
        user = serverConnection.sendToServer(user);
    }
}
