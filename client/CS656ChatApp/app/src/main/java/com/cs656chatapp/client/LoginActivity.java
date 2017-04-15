package com.cs656chatapp.client;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;


public class LoginActivity extends Activity {

    Button nextPage, newButton;
    EditText Eusername, Epassword;
    String username, password;
    Thread tSend;
    UserObject user;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (isMyServiceRunning(serverListener.class)) {
            //User is logged in already, so start mainactivity
            System.out.println("logged in already");
            intent = new Intent();
            intent.setClass(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        } else {
            // else start loginactivity
            System.out.println("not logged in yet.");
            setContentView(R.layout.activity_user);
            Eusername = (EditText) findViewById(R.id.editTextUsername);
            Epassword = (EditText) findViewById(R.id.editTextPassword);

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
                       // new Thread(send).start();
                        runThread();
                    }
                }
            });
        }
    }

    private void runThread() {
        runOnUiThread(new Thread(new Runnable() {
            // private Runnable send = runOnUiThread(new Runnable() {
            @Override
            public void run() {
            String up = username + " " + password;
            System.out.println(up);
            user = new UserObject();
            user.setUsername(username);
            user.setPassword(password);
            user = serverConnection.connect(user);
            Log.d("Jet", "Get something");
            if (user.getStatus() == 1) {
                user.setStatus(0);
                //startActivity
                intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                setExtras();
                intent.putExtra("userObject0", user);
                startActivity(intent);
                LoginActivity.this.finish();
            } else if (user.getStatus() == 2) {
                Toast.makeText(LoginActivity.this, "User already logged in!", Toast.LENGTH_LONG).show();
            } else {
                Log.d("Jet", "no match");
                Toast.makeText(LoginActivity.this, "Error logging in!", Toast.LENGTH_LONG).show();

            }
            }
        }));

    }

    void setExtras() {
        String[] holder = user.getMessage().split("-");
        String buddy_list = holder[1];
        String request_list = holder[2];
        String sent_list = holder[0];
        System.out.println("Buddy List recieved: " + buddy_list);
        intent.putExtra("Buddies0", buddy_list);
        System.out.println("Requests recieved: " + request_list);
        intent.putExtra("Requests0", request_list);
        System.out.println("Sent List recieved: " + sent_list);
        intent.putExtra("Sent0", sent_list);
    }


    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
