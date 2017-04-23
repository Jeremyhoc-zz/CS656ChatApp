package com.cs656chatapp.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.cs656chatapp.common.UserObject;

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

    @Override
    public void onCreate() {
        Log.d("Logs: ", "Creating");
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Successfully Logged In.", Toast.LENGTH_SHORT).show();

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
                    UserObject user = (UserObject) IN.readObject();
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
            Log.d("Logs: ", ": Incoming operation is ... " + operation);
            Log.d("Logs: ", ": Incoming message is ... " + message);
            Log.d("Logs: ", ": Incoming status is ... " + status);
            if (status == 1)
                sendResult(operation, message, this.user);
        }
    }

    final static public String serverResult = "com.cs656chatapp.client.serverListener.REQUEST_PROCESSED";
    final static public String serverOperation = "com.cs656chatapp.client.serverListener.serverOperation";
    final static public String serverMessage = "com.cs656chatapp.client.serverListener.serverMessage";
    final static public String serverEncodedImage = "com.cs656chatapp.client.serverListener.serverEncodedImage";
    final static public String serverEncodedImages = "com.cs656chatapp.client.serverListener.serverEncodedImages";
    final static public String serverEncodedVoice = "com.cs656chatapp.client.serverListener.serverEncodedVoice";
    final static public String serverEncodedVoices = "com.cs656chatapp.client.serverListener.serverEncodedVoices";

    public void sendResult(String operation, String message, UserObject user) {
        Intent intent = new Intent(serverResult);
        intent.putExtra(serverOperation, operation);
        intent.putExtra(serverMessage, message);
        if (operation.contains("Chat History:")) {
            String[] encodedImages = user.getEncodedImages();
            intent.putExtra(serverEncodedImages, encodedImages);
        } else if (operation.contains("Receive Pic:")) {
            String encodedImage = user.getEncodedImage();
            intent.putExtra(serverEncodedImage, encodedImage);
        } else if(operation.contains("Receive Voice:")){
            String encodedVoice = user.getEncodedVoice();
            System.out.println("From listener:\n"+encodedVoice);
            intent.putExtra(serverEncodedVoice, encodedVoice);
        }
        broadcaster.sendBroadcast(intent);
    }

    public void onDestroy() {
        try {
            Toast.makeText(this, "Successfully Logged Out.", Toast.LENGTH_SHORT).show();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}