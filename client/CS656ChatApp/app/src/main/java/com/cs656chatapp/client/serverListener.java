package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Jeremy on 3/23/2017.
 * Communicate to the server, send necessary information, and receive it back.
 */

public class serverListener extends Service {

    protected static Socket socket = null;
    protected static ObjectInputStream IN = null;
    protected static ObjectOutputStream OUT = null;
    protected static UserObject savedUser = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("Logs: ", "Creating");
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Starting up service", Toast.LENGTH_LONG).show();

        try {
            socket = serverConnection.getSocket();
            IN = serverConnection.getInputStream();
            OUT = serverConnection.getOutputStream();
            savedUser = serverConnection.getUserObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(listenToServer).start(); // Runnable listen to server
    }

    private Runnable listenToServer = new Runnable() {

        public void run() {
            Log.d("Logs: ", "running listenToServer");
            boolean done = false;
            int i = 0;
            while (!done) {
                System.out.println(i++ + ": Waiting...");
                try {
                    UserObject user = (UserObject)IN.readObject();
                    user.setUsername(savedUser.getUsername());
                    savedUser.setPassword(savedUser.getPassword());
                    Log.d("Logs: ", "message in listenToServer = " + user.getMessage());
                    //Thread t = new Thread(new performOperation(user, i));
                    //t.start();
/*                    System.out.println("Incoming message");
                    String message = user.getMessage();
                    System.out.println("Operation is ... " + message);*/
                    //Toast.makeText(this, operation, Toast.LENGTH_SHORT).show();
                    //done = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    done = true;
                }
            }
        }
    };

    class performOperation implements Runnable {
        UserObject user;
        int i;
        performOperation(UserObject user, int i) {
            this.user = user;
            this.i = i;
            Log.d("Logs: ", i++ + "AFTER: Message from this.user = " + this.user.getMessage() + "\nAnd from user = " + user.getMessage());
        }

        public void run() {
            String message = user.getMessage();
            Log.d("Logs: ", i++ + ": Message is ... " + message);
        }
    }

    public void onDestroy() {
        try {
            Toast.makeText(this, "Closing down service", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*    private static Handler h = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0){
                updateUI();
            }else{
                showErrorDialog();
            }
        }
    };*/
}