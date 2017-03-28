/*
 * Main Server for the CS656 Chat App
 * Jeremy Hochheiser for CS*656 Spring 2017 Guiling Wang
*/

package com.cs656chatapp.server;

import com.cs656chatapp.common.UserObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
			IN = new ObjectInputStream(incoming.getInputStream());
			OUT = new ObjectOutputStream(incoming.getOutputStream());
			user = (UserObject)IN.readObject();
			System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + user.getUsername() + ") is attempting to log in.");

			dbconn = new dbConnection();

			boolean found = logIn(user);
			if (found)
			{
				System.out.println(user.getUsername());
				System.out.println(incoming);
				clients.put(user.getUsername(), incoming);
				user.setStatus(1);
				OUT.writeObject(user);
				OUT.flush();
				System.out.println(user.getUsername() + " has successfully logged in.");
				while(!done) {
					//Maintain connection for requests and processing
					user = (UserObject) IN.readObject();
					System.out.println("Request coming in.");
					
					int userID = user.getUserID();
					String username = user.getUsername();
					String name = user.getName();
					int status = user.getStatus();
					String operation = user.getOperation();
					String message = user.getMessage();
					
					if (operation.equals("Set Message")) {
						user = setMessage(user);
					} 
					else if (operation.equals("Load buddy list")) {
						user = loadBuddyList(user);
					} 
					else if (operation.equals("Send Message")) {
						user = sendMessage(user);
					} 
					else if (operation.equals("Send Picture")) {
						user = sendPicture(user);
					} 
					else if (operation.equals("Send Voice")) {
						user = sendVoice(user); 
					} 
					else if (operation.equals("Send Friend Request")) {
						user = sendFriendRequest(user);
					}
					else if (operation.equals("Delete Friend")) {
						user = deleteFriend(user);
					}
					else if (operation.equals("Logout")) {
						user = logOut(user);
					}
					
					OUT.writeObject(user);
					OUT.flush();
					System.out.println("Response sent out.");
				}
				System.out.println(user.getUsername() + " has logged out.");
			} else {
				System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + user.getUsername() + ") failed to log in with wrong username or password.");
			}
			// Close the connection.
			
			
			
			rs.close();
			IN.close();
			OUT.close();
			incoming.close();
		} catch (EOFException e) {
			System.out.println("Execute function here.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean logIn(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Login Process
		rs = dbconn.executeSQL("select username, password from Users;");
		boolean found = false;
		int id = 0;
		while(rs.next() && found != true)
		{
			if (user.getUsername().equals(rs.getString("username")) && user.getPassword().equals(rs.getString("password"))) {
				user.setUsername(rs.getString("username"));
				user.setUserID(rs.getInt("ID"));
				found = true;
		}
		//Find all friends of Client X with specific ID,
		rs = dbconn.executeSQL("select friends of user");
		String friends = "";
		while (rs.next()) {
			friends += rs.getString("") + ",";
		}
			user.setMessage(friends);
		}
		notifyLoggedIn(user.getName());
		return found;
	}
	
	public void notifyLoggedIn(String myName) throws ClassNotFoundException, IOException, SQLException {
		rs = dbconn.executeSQL("select friends of client X;");
		while(rs.next()) {
			String friendsName = rs.getString("name");
			Socket friend = findSocket(friendsName);
			OutputStream os = friend.getOutputStream();
	        ObjectOutputStream toFriendSocket = new ObjectOutputStream(os);
	        UserObject msgToFriend = new UserObject();
	        msgToFriend.setOperation("Logged In," + myName);
	        toFriendSocket.writeObject(msgToFriend);
	        toFriendSocket.flush();
		}
	}
	
	public UserObject createAccount(UserObject user) {
		//Create a new Account in DB
		user.setStatus(1);
		return user;
	}
	
	public UserObject setMessage(UserObject user) {
		//Test method!
		user.setMessage("Client-Server Conn works.");
		user.setStatus(1);
		return user;
	}
	
	public UserObject loadBuddyList(UserObject user) {
		//Grab user ID's friends in DB that are online, return to client for loading
		user.setStatus(1);
		return user;
	}

	/* Can sendMessage
	 * sendPicture
	 * and sendVoice
	 * be combined into one method?
	 */
	
	public UserObject sendMessage(UserObject user) throws ClassNotFoundException, IOException {
		//Add message to DB, Grab socket of friend's username in variable clients, and send to that receiver.
		String[] msgSplit = user.getMessage().split(",");
		String friendsName = msgSplit[0];
		String message = msgSplit[1];

		Socket client = findSocket(friendsName);
		
		OutputStream os = client.getOutputStream();
        ObjectOutputStream toFriend = new ObjectOutputStream(os);
        toFriend.writeObject(message);
        toFriend.flush();
        
        //addMessageToDB(user);
        user.setStatus(1);
        return user;
	}

	public UserObject sendPicture(UserObject user) {
		//Add picture to DB, Grab socket of friend's username in variable clients, and send to that receiver.
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}

	public UserObject sendVoice(UserObject user) {
		//Add voice to DB, Grab socket of friend's username in variable clients, and send to that receiver.
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}

	public UserObject sendFriendRequest(UserObject user) {
		//Forge a new friendship
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}
	
	public UserObject deleteFriend(UserObject user) {
		//Delete an old friendship
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}
	
	public void notifyLoggedOut(String myName) throws ClassNotFoundException, IOException, SQLException {
		rs = dbconn.executeSQL("select friends of client X;");
		while(rs.next()) {
			String friendsName = rs.getString("name");
			Socket friend = findSocket(friendsName);
			OutputStream os = friend.getOutputStream();
	        ObjectOutputStream toFriendSocket = new ObjectOutputStream(os);
	        UserObject msgToFriend = new UserObject();
	        msgToFriend.setOperation("Logged Out," + myName);
	        toFriendSocket.writeObject(msgToFriend);
	        toFriendSocket.flush();
		}
	}
	
	public UserObject logOut(UserObject user) {
		//Broadcast to user's friends this client is logging off so they can refresh their screens, then log client off.
		//rs = ;
		//notifyLoggedOut();
		user.setStatus(1);
		return user;
	}
	
	public Socket findSocket(String username) {
		//Find a client in the clients list to send message to
		Socket client = null;
		for (Iterator<String> iter = clients.keySet().iterator(); iter.hasNext(); ) {
		    String key = iter.next();
		    if (key.equalsIgnoreCase(username)) {
		    	client = clients.get(key);
		    	System.out.println("InetAddress: " + client.getInetAddress().toString());
		    	System.out.println("Port: " + client.getPort());
		    	return client;
		    }
		}
		return client;
	}
	
	private static Map<String, Socket> clients = new HashMap<String, Socket> ();
	dbConnection dbconn = null;
	ResultSet rs = null;
	UserObject user = null;
	ObjectInputStream IN = null;
	ObjectOutputStream OUT = null;
}