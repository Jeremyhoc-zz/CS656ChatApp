package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Jeremy on 3/23/2017.
 * Communicate to the server, send necessary information, and receive it back.
 */

public class serverConnection {

    protected static Socket mySocket = null;
    protected static ObjectInputStream IN = null;
    protected static ObjectOutputStream OUT = null;
    protected static UserObject savedUser = new UserObject();

    public serverConnection() {
    }

    public static UserObject connect(UserObject user) {
        try {
            mySocket = new Socket("98.109.17.60", 2597); //10.0.2.2
            OUT = new ObjectOutputStream(mySocket.getOutputStream());
            IN = new ObjectInputStream(mySocket.getInputStream());
            OUT.writeUnshared(user);
            OUT.flush();
            user = (UserObject) IN.readObject();
            if (user.getStatus() == 1) {
                savedUser.setUserID(user.getUserID());
                savedUser.setClientName(user.getName());
                savedUser.setUsername(user.getUsername());
                savedUser.setPassword(user.getPassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static UserObject sendToServer(UserObject user) {
        try {
            user.setUserID(savedUser.getUserID());
            user.setClientName(savedUser.getName());
            user.setUsername(savedUser.getUsername());
            user.setPassword(savedUser.getPassword());
            System.out.println("Sent OUT:\nID: "+user.getUserID()+"\nUsername: "+user.getUsername()+
                                "\nOperation: "+user.getOperation()+"\nMessage: "+user.getMessage());
            //OUT.writeObject(user);
            OUT.writeUnshared(user);
           // OUT.reset();
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
        return savedUser;
    }
}