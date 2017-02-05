package com.cs656chatapp.client;

import com.cs656chatapp.common.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginActivity extends Activity {

    private static final long serialVersionUID = 1L;

    Button nextPage;
    EditText Eusername, Epassword;
    String username, password;
    Thread tSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Eusername = (EditText) findViewById(R.id.editTextUsername);
        Epassword = (EditText) findViewById(R.id.editTextPassword);

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
            try {
                Socket mySocket = new Socket("192.168.1.156", 2597); //98.109.17.60 //10.0.2.2
                String up = username + " " + password;
                System.out.println(up);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(mySocket.getOutputStream());
                UserObject user = new UserObject();
                user.setUsername(username);
                user.setPassword(password);
                objectOutputStream.writeObject(user);
                objectOutputStream.flush();
                ObjectInputStream objectInputStream = new ObjectInputStream(mySocket.getInputStream());
                user = (UserObject) objectInputStream.readObject();
                Log.d("Jet", "Get something");
                if (user.getStatus() == 1) {
                    //startActivity
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    Log.d("Jet", "no match");
                }
                objectInputStream.close();
                objectOutputStream.close();
                mySocket.close();
            } catch (Exception e) {
                Log.d("Jet", e.getMessage());
            }
        }
    };
}
