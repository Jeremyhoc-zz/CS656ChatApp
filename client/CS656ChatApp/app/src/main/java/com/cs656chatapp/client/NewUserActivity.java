package com.cs656chatapp.client;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;

import com.cs656chatapp.common.UserObject;

public class NewUserActivity extends AppCompatActivity {

    Button register, goBack;
    EditText Ename, Eusername, Epassword, Econfirm_password;
    String name, username, password, confirm_password;
    boolean passwordSwitch = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Ename = (EditText) findViewById(R.id.setName);
        Eusername = (EditText) findViewById(R.id.setUserName);
        Epassword = (EditText) findViewById(R.id.setPassword);
        Econfirm_password = (EditText) findViewById(R.id.setConfirmPassword);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        register = (Button) findViewById(R.id.buttonReg);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                name = Ename.getText().toString();
                username = Eusername.getText().toString();
                password = Epassword.getText().toString();
                confirm_password = Econfirm_password.getText().toString();
                if (!password.equals(confirm_password)) {
                    System.out.println("Passwords don't match!");
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence text = "Passwords do not match.\nPlease try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    passwordSwitch = false;
                } else {
                    passwordSwitch = true;
                }
                if (!username.equals("") && !password.equals("") && !name.equals("") && !confirm_password.equals("") && passwordSwitch) {
                    System.out.println("Sending this to server!");
                    runThread();
                }

            }
        });

        goBack = (Button) findViewById(R.id.buttonGoBack);
        goBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(NewUserActivity.this, LoginActivity.class);
                startActivity(intent);
                NewUserActivity.this.finish();
            }
        });

    }

    private void runThread() {
        runOnUiThread(new Thread(new Runnable() {
            // private Runnable send = runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UserObject user = new UserObject();
                user.setClientName(name);
                user.setUsername(username);
                user.setPassword(password);
                user.setStatus(7);
                user = serverConnection.connect(user);
                if (user.getStatus() == 1) {
                    // user.setStatus(0);
                    //startActivity
                    Log.d("Jet", "Status= " + user.getStatus());
                    Intent intent = new Intent();
                    intent.setClass(NewUserActivity.this, MainActivity.class);
                    user.setMessage("New User");
                    intent.putExtra("userObject0", user);
                    startActivity(intent);
                    NewUserActivity.this.finish();
                } else if (user.getStatus() == 9) {
                    Log.d("Jet", "Status= " + user.getStatus());
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_LONG;
                    CharSequence text = "Username already exists, please try another one!";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    Log.d("Jet", "no match");
                }
            }
        }));

    }
}


