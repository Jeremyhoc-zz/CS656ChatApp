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
			userIn = (UserObject)IN.readObject();
			String clientUsername = userIn.getUsername();
			System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + clientUsername + ") is attempting to log in.");
			dbconn = new dbConnection();
			userIn = checkCredentials(userIn);
			int found = userIn.getStatus();
			if (found==1)
			{
				System.out.println(clientUsername);
				System.out.println(incoming);
				clients.put(clientUsername, incoming);
				OUT.writeObject(userIn);
				OUT.flush();
				System.out.println(clientUsername + " has successfully logged in.");
				while(!done) {
					//Maintain connection for requests and processing
					System.out.println("Waiting for a command from " + clientUsername);
					userIn = (UserObject) IN.readObject();
					String operation = userIn.getOperation();
					System.out.printf("Request coming in from %s for: %s", clientUsername, userIn.getOperation());

					UserObject userOut = new UserObject();;
					userOut.setUserID(userIn.getUserID());
					userOut.setClientName(userIn.getName());
					userOut.setUsername(userIn.getUsername());
					userOut.setPassword(userIn.getPassword());
					
					if (operation.equals("Set Message")) {
						userIn.setMessage("Message 1: Example of userIn not working");
						OUT.writeObject(userIn);
						OUT.flush();
						TimeUnit.SECONDS.sleep(1);
						userIn.setMessage("This is Message 2");
						TimeUnit.SECONDS.sleep(1);
						userOut = setExample(userIn);
						OUT.writeObject(userOut);
						OUT.flush();						
					}
					else if (operation.equals("Send Text")) {
						userOut = sendMessage(userIn);
					} 
					else if (operation.equals("Send Pic")) {
						userOut = sendPicture(userIn);
					} 
					else if (operation.equals("Send Voice")) {
						userOut = sendVoice(userIn);
					} 
					else if (operation.equals("Friend Request")) {
						userOut = friendRequestHandler(userIn);
					}
					else if (operation.equals("Delete Friend")) {
						userOut = deleteFriend(userIn);
					}
					else if (operation.equals("Log Out")) {
						logOut(userIn);
						done = true;
						continue;
					} else {
						System.out.printf("Empty object came from %s", clientUsername);
					}
					
					OUT.writeObject(userOut);
					OUT.flush();
					System.out.println("Response sent out.");
				}
				System.out.printf("%s has logged out", clientUsername);
			} else if(found == 9){
				System.out.println("User already exists so status = " + userIn.getStatus());
				clients.put(clientUsername, incoming);
				OUT.writeObject(userIn);
				OUT.flush();
			} else {
				System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + clientUsername + ") failed to log in with wrong username or password.");
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
			if(createAccount(user)) {
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
					user.setClientName(rs.getString("name"));
					user.setUsername(rs.getString("username"));
					user.setUserID(rs.getInt("user_id"));
					user.setStatus(1);
					found = true;
					user = logIn(user);
				}
			}
			return user;
	}
	
	public boolean createAccount(UserObject user) throws ClassNotFoundException, IOException, SQLException {
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
	
	public UserObject logIn(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		clients.put(user.getUsername(), incoming);
		user = loadBuddyList(user);
		
		notifyLoggedIn(user);
		user.setStatus(1);
		return user;
	}
	
	public void notifyLoggedIn(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		rs = dbconn.executeSQL("select username from Users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+");");
		while(rs.next()) {
			String friendsName = rs.getString("username");
			Socket friend = findSocket(friendsName);
			OutputStream os = friend.getOutputStream();
	        ObjectOutputStream toFriendSocket = new ObjectOutputStream(os);
	        UserObject notifyFriend = new UserObject();
	        notifyFriend.setOperation("Friend Logged On");
	        notifyFriend.setMessage(user.getUsername());
	        notifyFriend.setStatus(1);
	        toFriendSocket.writeObject(notifyFriend);
	        toFriendSocket.flush();
		}
	}
	
	public UserObject loadBuddyList(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Grab user ID's friends in DB that are online, return to client for loading
		rs = dbconn.executeSQL("select user_id, name, username from users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+") OR user_id IN (select user_id from friends where friend_id="+user.getUserID()+");");
		String friends="";
		while(rs.next())
		{
			friends += rs.getString("username") + ",";
		}
		user.setMessage(friends);
		return user;
	}
	
	public UserObject setExample(UserObject user) {
		//Test method!
		user.setMessage("This is Message 3");
		user.setStatus(1);
		return user;
	}
	
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

	public UserObject friendRequestHandler(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		String clientUsername = user.getUsername();
		String message = user.getMessage();
		String[] msgSplit = message.split(",");
		String operation = msgSplit[0];
		if (operation.equals("Send Friend Request")) {
			String strangerName = msgSplit[1];
			Socket stranger = findSocket(strangerName);
			OutputStream os = stranger.getOutputStream();
	        ObjectOutputStream toStrangerSocket = new ObjectOutputStream(os);
	        UserObject msgToStranger = new UserObject();
	        msgToStranger.setOperation("New Friend Request");
	        msgToStranger.setMessage(clientUsername);
	        msgToStranger.setStatus(1);
	        toStrangerSocket.writeObject(msgToStranger);
	        toStrangerSocket.flush();
	        //TODO: Add potential friend request to the database friendRequest table
		} else if (operation.equals("Respond to Friend Request")) {
			String strangerName = msgSplit[1];
			String result = msgSplit[2];
			Socket stranger = findSocket(strangerName);
			OutputStream os = stranger.getOutputStream();
	        ObjectOutputStream toStrangerSocket = new ObjectOutputStream(os);
	        UserObject msgToStranger = new UserObject();
	        msgToStranger.setOperation("Response to Friend Request");
	        msgToStranger.setMessage(clientUsername + "," + result);
	        msgToStranger.setStatus(1);
	        toStrangerSocket.writeObject(msgToStranger);
	        toStrangerSocket.flush();
	        //TODO: Update friend request results in database friendRequest table!
		}
		user.setStatus(1);
		return user;
	}
	
	public UserObject deleteFriend(UserObject user) {
		//Disable an old friendship in the database friendshipHistory table
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}
	
	public UserObject logOut(UserObject user) throws IOException, SQLException {
		//Broadcast to user's friends this client is logging off
		clients.remove(user.getUsername());
		notifyLoggedOut(user);
		user.setMessage("Log out successful.");
		user.setStatus(1);
		return user;
	}
	
	public void notifyLoggedOut(UserObject user) throws IOException, SQLException {
		rs = dbconn.executeSQL("select username from Users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+");");
		while(rs.next()) {
			String friendsName = rs.getString("username");
			Socket friend = findSocket(friendsName);
			OutputStream os = friend.getOutputStream();
	        ObjectOutputStream toFriendSocket = new ObjectOutputStream(os);
	        UserObject notifyFriend = new UserObject();
	        notifyFriend.setOperation("Friend Logged Off");
	        notifyFriend.setMessage(user.getUsername());
	        notifyFriend.setStatus(1);
	        toFriendSocket.writeObject(notifyFriend);
	        toFriendSocket.flush();
		}
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
	UserObject userIn = null;
	ObjectInputStream IN = null;
	ObjectOutputStream OUT = null;
}