package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
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
    protected static LocalBroadcastManager broadcaster = null;

    public IBinder onBind(Intent intent) {
        return null;
    }
    final Handler handler = new Handler();;

    @Override
    public void onCreate() {
        Log.d("Logs: ", "Creating");
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Starting up service", Toast.LENGTH_SHORT).show();

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
            while (!done) {
                System.out.println("Waiting...");
                try {
                    UserObject user = (UserObject)IN.readObject();
                    user.setUsername(savedUser.getUsername());
                    user.setPassword(savedUser.getPassword());
                    Thread t = new Thread(new performOperation(user));
                    t.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    done = true;
                }
            }
        }
    };

    class performOperation implements Runnable {
        UserObject user = new UserObject();
        performOperation(UserObject user) {
            this.user = user;
        }

        public void run() {
            String operation = this.user.getOperation();
            String message = this.user.getMessage();
            int status = this.user.getStatus();
            Log.d("Logs: ", ": Operation is ... " + operation);
            Log.d("Logs: ", ": Message is ... " + message);
            Log.d("Logs: ", ": Status is ... " + status);
            if (status == 1)
                sendResult(operation, message);
        }
    }

    final static public String serverResult = "com.cs656chatapp.client.serverListener.REQUEST_PROCESSED";
    final static public String serverOperation = "com.cs656chatapp.client.serverListener.serverOperation";
    final static public String serverMessage = "com.cs656chatapp.client.serverListener.serverMessage";
    public void sendResult(String operation, String message) {
        Intent intent = new Intent(serverResult);
        if (operation != null && message != null) {
            intent.putExtra(serverOperation, operation);
            intent.putExtra(serverMessage, message);
        }
        broadcaster.sendBroadcast(intent);
    }

    public void onDestroy() {
        try {
            Toast.makeText(this, "Closing down service", Toast.LENGTH_SHORT).show();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}