/*
 * Main Server for the CS656 Chat App
 * Jeremy Hochheiser for CS*656 Spring 2017 Guiling Wang
*/

package com.CS656ChatApp.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class appServer {
	public static void main(String[] args) { //throws ClassNotFoundException, SQLException {
		try	{
			ServerSocket myServerSocket = new ServerSocket(2597);
			System.out.println("CS656ChatApp server now online at port 2597.");
			for (;;) {
				Socket incoming = myServerSocket.accept();
				//new ThreadClientHandler(incoming).start();
			}
		}
		catch (IOException e) {
			// When error occur, print the exception message.
			e.printStackTrace();
		}
		System.out.println("Server shutting down.");
	}
}

class ThreadClientHandler extends Thread {
	private Socket incoming;
	public ThreadClientHandler (Socket i)
	{
		incoming = i;
	}

	public void run()
	{
		try
		{
			boolean done = false;
			ObjectInputStream objectInputStream = new ObjectInputStream(incoming.getInputStream());
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(incoming.getOutputStream());
			ResultSet rs;
			dbConnection dbconn = new dbConnection();

			//Login Process
			rs = dbconn.executeSQL("select username, password from Users;");
			UserObject user = (UserObject)objectInputStream.readObject();
			System.out.println("A client connected from " + incoming.getLocalAddress().getHostAddress());
			boolean found = false;
			while(rs.next() && found != true)
			{
				if (user.getUsername().equals(rs.getString("username")) && user.getPassword().equals(rs.getString("password")))
					found = true;
			}
			if (found)
			{
				user.setStatus(1);
				objectOutputStream.writeObject(user);
				objectOutputStream.flush();
            System.out.println(user.getUsername() + " has logged in successfully.");
				/*while(!done) {
					//Maintain connection for requests and processing
					user = (UserObject)objectInputStream.readObject();
					objectOutputStream.writeObject(user);
					objectOutputStream.flush();
				}*/
				System.out.println(user.getUsername() + " has logged out.");
			}
			else
			{
				System.out.println(user.getUsername() + " failed to log in with wrong username or password.");
			}
			// Close the connection.
			rs.close();
			objectInputStream.close();
			objectOutputStream.close();
			incoming.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
