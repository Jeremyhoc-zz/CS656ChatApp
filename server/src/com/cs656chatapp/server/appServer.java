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
import java.util.concurrent.*;

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
			//int found = 0;
			//if (findSocket(user.getUsername()) == null) {
				dbconn = new dbConnection();
				user = checkCredentials(user);
				int found = user.getStatus();
			/*} else {
				found = 1;
			}*/
			if (found==1)
			{
				System.out.println(user.getUsername());
				System.out.println(incoming);
				clients.put(user.getUsername(), incoming);
				OUT.writeObject(user);
				OUT.flush();
				System.out.println(user.getUsername() + " has successfully logged in.");
				while(!done) {
					//Maintain connection for requests and processing
					System.out.println("Waiting for a command from " + user.getUsername());
					user = (UserObject) IN.readObject();
					System.out.println("Request coming in.");
					
					int userID = user.getUserID();
					String username = user.getUsername();
					String name = user.getName();
					int status = user.getStatus();
					String operation = user.getOperation();
					String message = user.getMessage();
					System.out.println(operation);

					if (operation.equals("Set Message")) {
						user.setMessage("First message in!");
						OUT.writeObject(user);
						OUT.flush();
						TimeUnit.SECONDS.sleep(2);
						UserObject user2 = new UserObject();
						user2 = setExample(user2);
						OUT.writeObject(user2);
						OUT.flush();						
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
					else if (operation.equals("Log Out")) {
						user = logOut(user);
						done=true;
					} else {
						System.out.println("Empty object came from " + user.getUsername());
					}
					
					OUT.writeObject(user);
					OUT.flush();
					System.out.println("Response sent out.");
				}
				System.out.println(user.getUsername() + " has logged out.");
			} else if(found==9){
				System.out.println("User already exists so status= "+user.getStatus());
				System.out.println(user.getUsername());
				System.out.println(incoming);
				clients.put(user.getUsername(), incoming);
				OUT.writeObject(user);
				OUT.flush();
				
			}
			else {
				System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + user.getUsername() + ") failed to log in with wrong username or password.");
			}
			// Close the connection.
			rs.close();
			IN.close();
			OUT.close();
			incoming.close();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public UserObject checkCredentials(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Login Process
		if(user.getStatus()==7) {
			//new user creation
			System.out.println("Attempting to create new user");
			if(createUser(user)) {
				System.out.println("New user created successfully");
			} else {
				System.out.println("New user not created! Username already exists!");
				user.setStatus(9);
				return user;
			}
			
		} 
			rs = dbconn.executeSQL("select * from Users;");
			boolean found = false;
			while(rs.next() && !found)
			{
				if (user.getUsername().equals(rs.getString("username")) && user.getPassword().equals(rs.getString("password"))) {
					user.setUsername(rs.getString("username"));
					user.setUserID(rs.getInt("user_id"));
					user.setStatus(1);
					found = true;
					user = logIn(user);
				}
			}
			return user;
	}
	
	public UserObject logIn(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		clients.put(user.getUsername(), incoming);
		user = loadBuddyList(user);
		
		//notifyLoggedIn(user.getName());
		user.setStatus(1);
		return user;
	}
	
	public boolean createUser(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Check if user already exists Process
		rs = dbconn.executeSQL("select username from Users;");
		boolean ret = false;
		while(rs.next())
		{
			if (user.getUsername().equals(rs.getString("username")))
				return false;
		}
		ret=dbconn.executeUpdate("insert into users(username,password,name) values(\""+user.getUsername()+"\",\""+user.getPassword()+"\",\""+user.getName()+"\");");
		if(ret) return true;
		return false;
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
	
	public UserObject setExample(UserObject user) {
		//Test method!
		user.setMessage("Client-Server Conn works.");
		user.setStatus(1);
		return user;
	}
	
	public UserObject loadBuddyList(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Grab user ID's friends in DB that are online, return to client for loading
		rs = dbconn.executeSQL("select user_id, name, username from users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+") OR user_id IN (select user_id from friends where friend_id="+user.getUserID()+");");
		String friends="";
		while(rs.next())
		{
			friends +=rs.getString("username") + ",";
		}
		user.setMessage(friends);
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
		String from = user.getUsername();
		String to = msgSplit[0];
		String message = msgSplit[1];
        UserObject sendMsg = new UserObject();
        sendMsg.setOperation("incoming message");
        sendMsg.setMessage(from + "," + message);
		
		Socket client = findSocket(to);
		
		OutputStream os = client.getOutputStream();
        ObjectOutputStream sendTo = new ObjectOutputStream(os);
        sendTo.writeObject(message);
        sendTo.flush();
        
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
		user.setMessage("Log out initiated.");
		clients.remove(user.getUsername());
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