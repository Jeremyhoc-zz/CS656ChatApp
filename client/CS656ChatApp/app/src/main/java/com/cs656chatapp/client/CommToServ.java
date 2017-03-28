package com.cs656chatapp.client;

import com.cs656chatapp.common.UserObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Jeremy on 3/23/2017.
 * Communicate to the server, send necessary information, and receive it back.
 */

public class CommToServ {

    private static Socket mySocket = null;
    private static ObjectInputStream IN = null;
    private static ObjectOutputStream OUT = null;

    public CommToServ() {
    }

    public static void connect() {
        try {
            mySocket = new Socket("192.168.1.156", 2597); //98.109.17.60 //10.0.2.2
            OUT = new ObjectOutputStream(mySocket.getOutputStream());
            IN = new ObjectInputStream(mySocket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public static UserObject talkToServer(UserObject user) {
        try {
            OUT.writeObject(user);
            OUT.flush();
            user = (UserObject) IN.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    };

    public static void close() {
        try {
            IN.close();
            OUT.close();
            mySocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}