/*
 * Main Server for the CS656 Chat App
 * Jeremy Hochheiser for CS*656 Spring 2017 Guiling Wang
*/

package com.cs656chatapp.server;

import com.cs656chatapp.common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;



public class appServer {
	public static void main(String[] args) { //throws ClassNotFoundException, SQLException {
		try	{  @SuppressWarnings("resource")
			ServerSocket myServerSocket = new ServerSocket(2597);
			System.out.println("CS656ChatApp server now online at port 2597");
			for (;;) {
            Socket incoming = myServerSocket.accept();
				new ThreadClientHandler(incoming).start();
			}
		}
		catch (IOException e) {
			// When error occur, print the exception message.
			System.out.println(e);
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
			System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + user.getUsername() + ") is attempting to log in.");
			boolean found = false;
			while(rs.next() && found != true)
			{
				if (user.getUsername().equals(rs.getString("username")) && user.getPassword().equals(rs.getString("password")))
					found = true;
			}
			if (found)
			{
				user.setStatus(1);
				System.out.println(user.getUsername() + " has successfully logged in.");
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
				System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + user.getUsername() + ") failed to log in with wrong username or password.");
			}
			// Close the connection.
			rs.close();
			objectInputStream.close();
			objectOutputStream.close();
			incoming.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}