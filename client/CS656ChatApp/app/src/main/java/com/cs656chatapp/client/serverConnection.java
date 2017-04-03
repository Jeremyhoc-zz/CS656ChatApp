package com.cs656chatapp.client;

import android.util.Log;

import com.cs656chatapp.common.UserObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Jeremy on 3/23/2017.
 * Communicate to the server, send necessary information, and receive it back.
 */

public class serverConnection {

    protected static Socket mySocket = null;
    protected static ObjectInputStream IN = null;
    protected static ObjectOutputStream OUT = null;
    protected static UserObject userStored = new UserObject();

    public serverConnection() {
    }

    public static UserObject connect(UserObject user) {
        try {
            mySocket = new Socket("192.168.1.156", 2597); //98.109.17.60 //10.0.2.2
            OUT = new ObjectOutputStream(mySocket.getOutputStream());
            IN = new ObjectInputStream(mySocket.getInputStream());
            userStored.setUsername(user.getUsername());
            userStored.setPassword(user.getPassword());
            OUT.writeObject(user);
            OUT.flush();
            user = (UserObject) IN.readObject();
            //user.setStatus(userObj.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static UserObject sendToServer(UserObject user) {
        try {
            user.setUsername(userStored.getUsername());
            user.setPassword(userStored.getPassword());
            OUT.writeObject(user);
            OUT.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void close() {
        try {
            mySocket.close();
            IN.close();
            OUT.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket() {
        return mySocket;
    }

    public static ObjectInputStream getInputStream() {
        return IN;
    }

    public static ObjectOutputStream getOutputStream() {
        return OUT;
    }

    public static UserObject getUserObject() {
        return userStored;
    }
}