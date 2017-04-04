package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import android.app.Service;
import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;

/**
 * Created by Jeremy on 3/23/2017.
 * Communicate to the server, send necessary information, and receive it back.
 */

public class serverListener extends Service {

    protected static Socket socket = null;
    protected static ObjectInputStream IN = null;
    protected static ObjectOutputStream OUT = null;
    protected static UserObject user = null;
    //protected static ServerSocket serverSocket = null;
    //protected static Handler mHandler = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("Creating");
        super.onCreate();
        //mHandler = new Handler();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        System.out.println("onStart");
        Toast.makeText(this, "Starting up service", Toast.LENGTH_LONG).show();

        try {
            socket = serverConnection.getSocket();
            IN = serverConnection.getInputStream();
            OUT = serverConnection.getOutputStream();
            user = serverConnection.getUserObject();
            /*Log.d("Logs: ", "serverListener - socket is: " + socket);
            Log.d("Logs: ", "serverListener - socket created. Creating serverSocket with port = " + socket.getPort() + " and address = " + socket.getInetAddress());
            serverSocket = new ServerSocket(socket.getPort(), 0, socket.getInetAddress());
            Log.d("Logs: ", "serverListener - Created serverSocket: " + serverSocket);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(listenToServer).start(); // Runnable listen to server
    }

    private static Runnable listenToServer = new Runnable() {
        public void run() {
            System.out.println("running listenToServer");
            boolean done = false;
            //socket = serverSocket.accept();
            //System.out.println("serverSocket accepted");
            while (!done) {
                try {
                    System.out.println("Waiting...");
                    UserObject user = (UserObject)IN.readObject();
                    System.out.println("Incoming message");
                    String message = user.getMessage();
                    System.out.println("Operation is ... " + message);
                    //Toast.makeText(this, operation, Toast.LENGTH_SHORT).show();
                    //done = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

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