package com.cs656chatapp.client;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        user= new UserObject();
        Intent intent = getIntent();
        user = (UserObject)intent.getSerializableExtra("userObject1");
        requests= (String)intent.getSerializableExtra("Requests1");

        if(requests.equals("none")){
            Toast.makeText(RequestsActivity.this, "No Friend Requests", Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent();
            intent1.setClass(RequestsActivity.this, MainActivity.class);
            intent1.putExtra("userObject0", user);
            startActivity(intent1);
            RequestsActivity.this.finish();
        }
        else{
            loadRequestList(requests.split(","));
        }

        System.out.println("I have these requests and my name is "+user.getUsername()+"\n"+requests);


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
    }
    protected void loadRequestList(String[] sample1){

        listView = (ListView)findViewById(R.id.requestList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.request_list_item,R.id.req_text1,sample1);
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
}
