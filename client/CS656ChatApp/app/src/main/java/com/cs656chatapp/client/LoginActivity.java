package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;



public class LoginActivity extends Activity {

    Button nextPage, newButton;
    EditText Eusername, Epassword;
    String username, password;
    Thread tSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Eusername = (EditText) findViewById(R.id.editTextUsername);
        Epassword = (EditText) findViewById(R.id.editTextPassword);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        newButton = (Button) findViewById(R.id.button_new);
        newButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, NewUserActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });


        nextPage = (Button) findViewById(R.id.buttonNextPage);
        nextPage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                username = Eusername.getText().toString();
                password = Epassword.getText().toString();
                if (!username.equals("") && !password.equals("")) {
                    System.out.println("Starting up!");
                    new Thread(send).start();
                }
            }
        });
    }

    private Runnable send = new Runnable() {
        @Override
        public void run() {
            String up = username + " " + password;
            System.out.println(up);
            UserObject user = new UserObject();
            user.setUsername(username);
            user.setPassword(password);
            user = serverConnection.connect(user);
            Log.d("Jet", "Get something");
            if (user.getStatus() == 1) {
                user.setStatus(0);
                //startActivity
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
               intent.putExtra("userObject0", user);
                startActivity(intent);
                LoginActivity.this.finish();
            } else {
                Log.d("Jet", "no match");
            }
        }
    };
}
