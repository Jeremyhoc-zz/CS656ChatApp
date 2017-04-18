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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
			System.out.println("People online:");
			for (Map.Entry<String, ObjectOutputStream> entry: streams.entrySet()){
				System.out.println(entry.getKey());
				//System.out.println(entry);
			}
			boolean sendToClient=true;
			boolean done = false;
			IN = new ObjectInputStream(incoming.getInputStream());
			OUT = new ObjectOutputStream(incoming.getOutputStream());
			userIn = (UserObject)IN.readObject();
			String clientUsername = userIn.getUsername();
			System.out.println(incoming.getLocalAddress().getHostAddress() + "(" + clientUsername + ") is attempting to log in.");
			dbconn = new dbConnection();
			userIn = checkCredentials(userIn);			
			int found = userIn.getStatus();
			//check if user already logged in
			System.out.println("Checking if user already logged in...");
			if(found==1 && streams.containsKey(clientUsername)){
			    System.out.println("Already logged in ");
			    userIn.setStatus(2);
			    OUT.writeUnshared(userIn);
				OUT.flush();
				found=-1;
			}
			if (found == 1)
			{
				System.out.println(clientUsername);
				System.out.println(incoming);
				streams.put(clientUsername, OUT);
				OUT.writeUnshared(userIn);
				OUT.flush();
				System.out.println(clientUsername + " has successfully logged in.");
				while(!done) {
					//Maintain connection for requests and processing
					System.out.println("Waiting for a command from " + clientUsername);
				    System.out.println("---------------------------------------");

					userIn = (UserObject) IN.readObject();
			
				    int userID = userIn.getUserID();
				    String username = userIn.getUsername();
				    String name = userIn.getName();
				    String password = userIn.getPassword();
				    int status = userIn.getStatus();
				    String operation = userIn.getOperation();
				    String message = userIn.getMessage();
					System.out.printf("Request coming in from %s for: %s\n", clientUsername, operation);

					UserObject userOut = new UserObject();
					userOut.setUserID(userID);
					userOut.setClientName(name);
					userOut.setUsername(username);
					userOut.setPassword(password);
					userOut.setOperation(operation);
					userOut.setMessage(message);
					sendToClient=true;

					
					if (operation.equals("Text")) {
						userOut.setOperation("Text");
						userOut = setExample(userOut, "UserOut 1");
					}
					else if (operation.equals("Get Buddy List")) {
						userOut = loadBuddyList(userOut);
					}
					else if (operation.equals("Retrieve Messages")) {
						retrieveChatHistory(userOut);
					}
					else if (operation.contains("Send Text:")) {
						String friendName = operation.split(":")[1];
						sendMessage(userIn, friendName, message);
					} 
					else if (operation.equals("Send Pic")) {
						userOut = sendPicture(userOut);
					} 
					else if (operation.equals("Send Voice")) {
						userOut = sendVoice(userOut);
					} 
					else if (operation.equals("Friend Request")) {
						userOut = friendRequestHandler(userOut/*, operation, message, username*/);
					}
					else if (operation.equals("Delete Request")) {
						deleteRequest(userOut);
						sendToClient=false;
					}
					else if (operation.equals("Get Request List")) {
						userOut = getRequests(userOut);
					}
					else if (operation.equals("Get Sent List")) {
						userOut = getSentRequests(userOut);
					}
					else if (operation.equals("Delete Friend")) {
						   deleteFriend(userOut);
						   sendToClient=false;
					}
					else if (operation.equals("Log Out")) {
						logOut(userOut, username);
						done = true;
						streams.remove(username);
						continue;
					} else {
						System.out.printf("Empty object came from %s", clientUsername);
					}
					//System.out.println("Send back? "+sendToClient);
					if(sendToClient){
					//OUT.writeObject(userOut);
		            OUT.writeUnshared(userOut);
					OUT.flush();
					//OUT.reset();
					System.out.println("Response sent out.");
					//printUser(userOut);
					}
				}
				System.out.printf("%s has logged out\n", clientUsername);
			} else if(found == 9){					//Username already exists
				System.out.println("User already exists so status = " + userIn.getStatus());
				OUT.writeUnshared(userIn);
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
	
/*	public void tester(){
		System.out.println("Initial test:");
		UserObject use = new UserObject();
		for (Map.Entry<String, Socket> entry: clients.entrySet()){
			String name = entry.getKey();
			use.setUsername(name);
			use = setInfo(use);
			Socket stranger = findSocket(name);
			OutputStream os = stranger.getOutputStream();
	        ObjectOutputStream toStrangerSocket = new ObjectOutputStream(os);
	        user.setUsername(strangerName);
	        msgToStranger = setInfo(msgToStranger);
	        msgToStranger.setOperation("New Friend Request");
	        msgToStranger.setMessage(username);
	        msgToStranger.setStatus(1);
	        toStrangerSocket.writeUnshared(msgToStranger);
	      //  toStrangerSocket.reset();
	        toStrangerSocket.flush();
		}
	}*/
	
	public void retrieveChatHistory(UserObject user) throws SQLException, IOException {
		String friendName=user.getMessage();
		rs = dbconn.executeSQL("select user_id from users where username=\"" + friendName + "\";");
		int friendID = -1;
		if (rs.next()) friendID = rs.getInt("user_id");
		String retrieveChatHistorySQL = "select from_uid, to_uid, message_type, content, sent_dt "
				+ "from messages "
				+ "where from_uid = " + user.getUserID() + " or from_uid = " + friendID
				+ " and to_uid = " + user.getUserID() + " or to_uid = " + friendID + " "
				+ "order by sent_dt ASC limit 16;";
		System.out.println(retrieveChatHistorySQL);
		rs = dbconn.executeSQL(retrieveChatHistorySQL);
		String msg = "";
		while (rs.next()) {
			msg += Integer.toString(rs.getInt("from_uid")) + ",,,";
			msg += rs.getString("message_type") + ",,,";
			msg += rs.getString("content") + ",,,";
		}
		ObjectOutputStream client = streams.get(user.getUsername());
		UserObject msgToClient = new UserObject();
		msgToClient.setOperation("Chat History:" + friendName);
		msgToClient.setMessage(msg);
		msgToClient.setStatus(1);
		client.writeUnshared(msgToClient);
		client.flush();
	}
	
	
	public void sendIt(UserObject user) throws SQLException, IOException {
		String friendName=user.getMessage();
		if(streams.containsKey(friendName)) {
			ObjectOutputStream toStrangerSocket = streams.get(friendName);
			UserObject msgToFriend = new UserObject();
			msgToFriend.setOperation("New Chat!");
			msgToFriend.setMessage(user.getUsername());
			msgToFriend.setStatus(1);
			toStrangerSocket.writeUnshared(msgToFriend);
			toStrangerSocket.flush();

		}
	}
	
/*	public void printUser(UserObject u){
		System.out.println("UserId= "+u.getUserID()+" Username= "+u.getUsername());
		System.out.println("Clientname= "+u.getName()+" Password= "+u.getPassword());
		System.out.println("Operation= "+u.getOperation()+" Message= "+u.getMessage()+" hmm");
		Socket stranger = findSocket(u.getUsername());
		System.out.println("Socket= "+stranger);
		System.out.println();
		Socket another = clients.get(u.getUsername());
		System.out.println("Again socket= "+another);
	}*/
	
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
		boolean ret;
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
		String tempHolder;
		user = loadBuddyList(user);
		tempHolder=user.getMessage();
		user = getRequests(user);
		tempHolder += "-" + user.getMessage();
		user=getSentRequests(user);
		user.setMessage(user.getMessage()+"-"+tempHolder);
		user.setOperation("Login Info Attached");
		notifyLoggedIn(user);
		user.setStatus(1);
		return user;
	}
	
	public void notifyLoggedIn(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		rs = dbconn.executeSQL("select username from Users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+");");
		while(rs.next()) {
			String friendsName = rs.getString("username");
		  	if(streams.containsKey(friendsName)){
		  		ObjectOutputStream toFriendSocket = streams.get(friendsName);
		        UserObject notifyFriend = new UserObject();
		        notifyFriend.setOperation("Friend Logged On");
		        notifyFriend.setMessage(user.getUsername());
		        notifyFriend.setStatus(1);
		        toFriendSocket.writeUnshared(notifyFriend);
		        toFriendSocket.flush();
			}
		}
	}
	
	public UserObject getRequests(UserObject user) throws SQLException{
		rs = dbconn.executeSQL("select username from users where user_id IN "
				+ "(select sender_id from friendRequests where receiver_id= " + user.getUserID() + ");");
		String senderNames="";
		while(rs.next())
		{
			senderNames += rs.getString("username") + ",";
		}
		if(senderNames.isEmpty()) senderNames="none";
		user.setOperation("Take Request List");
		user.setMessage(senderNames);
		user.setStatus(1);
		return user;
	}
	
	public UserObject loadBuddyList(UserObject user) throws ClassNotFoundException, IOException, SQLException {
		//Grab user ID's friends in DB, return to client for loading
		rs = dbconn.executeSQL("select user_id, name, username from users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+") OR user_id IN (select user_id from friends where friend_id="+user.getUserID()+");");
		String friends="";
		while(rs.next())
		{
			friends += rs.getString("username") + ",";
		}
		user.setMessage(friends);
		if (friends.isEmpty()) user.setMessage("No friends");
		user.setOperation("Take Buddy List");
		user.setStatus(1);
		return user;
	}
	
	public UserObject setExample(UserObject user, String message) {
		//Test method!
		user.setMessage(message);
		user.setStatus(1);
		return user;
	}
	
	public UserObject sendMessage(UserObject user, String to, String message) throws ClassNotFoundException, IOException, SQLException {
		//Add message to DB, Grab OutputStream of friend's username in variable streams, and send to that receiver.
	  	if(streams.containsKey(to)){
	  		String from = user.getUsername();
			int fromID = user.getUserID();
			rs = dbconn.executeSQL("select user_ID from users where username=\"" + to + "\";");
			int toID = -1;
			if (rs.next()) toID = rs.getInt("user_id");

			//Add new entry in messages table
			boolean ret=dbconn.executeUpdate("insert into messages (from_uid, to_uid, message_type, content) values ("+fromID+","+toID+","+"\"text\",\""+message+"\");");
			if(!ret) System.out.println("Something wrong adding friend");
			else System.out.println("Message sent successfully.");
			
			//Send message out to the friend			
	        UserObject sendMsg = new UserObject();
	        sendMsg.setOperation("Receive Text:" + from);
	        sendMsg.setMessage(message);
	        sendMsg.setStatus(1);
	        ObjectOutputStream sendTo = streams.get(to);
	        sendTo.writeUnshared(sendMsg);
	        sendTo.flush();
	  	}        
        
        user.setStatus(1);
        return user;
	}

	public UserObject sendPicture(UserObject user) {
		//Add picture to DB, Grab OutputStream of friend's username in variable streams, and send to that receiver.
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}

	public UserObject sendVoice(UserObject user) {
		//Add voice to DB, Grab OutputStream of friend's username in variable streams, and send to that receiver.
		String message = user.getMessage();
		user.setStatus(1);
		return user;
	}
	
	public UserObject getSentRequests(UserObject user) throws /*ClassNotFoundException, IOException, */SQLException{
		rs = dbconn.executeSQL("select username from users where user_id IN "
				+ "(select receiver_id from friendRequests where sender_id="+user.getUserID()+");");
		String receiverNames="";
		while(rs.next())
		{
			receiverNames+= rs.getString("username") + ",";
			
		}
		if(receiverNames.isEmpty()) receiverNames="nobody";
		user.setOperation("Take List");
		user.setMessage(receiverNames);
		user.setStatus(1);
		return user;
	}
	
/*	public UserObject friendReqsHandler(UserObject user) throws ClassNotFoundException, IOException, SQLException{
		// Check if username exists
		rs = dbconn.executeSQL("select username,user_ID from users where username=\""+user.getMessage()+"\";");
		String friendUserName="";
		int friendID=-1;
		while(rs.next())
		{
			friendUserName = rs.getString("username");
			friendID=rs.getInt("user_id");
		}
		if(friendUserName.equals("")){
			user.setOperation("User Does Not Exist");
			user.setMessage("User "+user.getMessage()+" does not exist");
			user.setStatus(1);
			return user;
		}
		
		//Add new entry in friendRequests table
		boolean ret=dbconn.executeUpdate("insert into friendRequests values("+user.getUserID()+","+friendID+");");
		if(!ret) System.out.println("Something wrong adding friend");
		else System.out.println("Waiting for friend to accept");
		
		// Get sent list 
		user = getSentRequests(user);
		//function to check if friend is online
		return user;
	}*/

	public boolean verifyStrangerExists(String strangerName, int clientID) throws SQLException {
		// Check if username exists
		rs = dbconn.executeSQL("select username,user_id from users where username=\"" + strangerName + "\";");
		if (rs.next()) {
			int friendID = rs.getInt("user_id");
			dbconn.executeUpdate("insert into friendRequests values(" + clientID + "," + friendID + ");");
			return true;
		} else {
			return false;
		}
	}
	
	public UserObject friendRequestHandler(UserObject user/*, String operation, String message, String clientUsername*/) throws ClassNotFoundException, IOException, SQLException {
	
		String[] msgSplit = user.getMessage().split(",");
		String operation = msgSplit[0];
		String strangerName = msgSplit[1];
		String username = user.getUsername();
		int clientID = user.getUserID();
		if (operation.equals("Send Friend Request")) {
			if (verifyStrangerExists(strangerName, clientID)) {
				user.setOperation("Friend Request Sent");
				user.setMessage("Friend Request sent to " + strangerName + ".");
				if(streams.containsKey(strangerName)){
			        ObjectOutputStream toStranger = streams.get(strangerName);
			        UserObject msgToStranger = new UserObject();
			        msgToStranger.setOperation("New Friend Request");
			        msgToStranger.setMessage(username);
			        msgToStranger.setStatus(1);
			        toStranger.writeUnshared(msgToStranger);
			        toStranger.flush();  
				}
			} else {
				user.setOperation("User Does Not Exist");
				user.setMessage("User " + strangerName + " does not exist");
			}
		} else if (operation.equals("Respond to Friend Request")) {
			String result = msgSplit[2];
			rs = dbconn.executeSQL("select user_id from users where username=\""+ strangerName +"\";");
			int person2ID = -1;
			if (rs.next()) person2ID = rs.getInt("user_id");
			if (result.equals("Accept")) {
				dbconn.executeUpdate("insert into friends values(" + clientID + ", " + person2ID + ");");
			}				
			dbconn.executeUpdate("delete from friendRequests where receiver_id = " + clientID + " AND sender_id  =" + person2ID + ";");
			user.setOperation("Response received");
			user.setMessage("Response documented for "+strangerName);
			if(streams.containsKey(strangerName)){
				ObjectOutputStream toStrangerSocket = streams.get(strangerName);
				UserObject msgToStranger = new UserObject();
				msgToStranger.setOperation("Response to Friend Request");
	        	msgToStranger.setMessage(user.getUsername() + "," + result);
	        	msgToStranger.setStatus(1);
	        	toStrangerSocket.writeUnshared(msgToStranger);
	        	toStrangerSocket.flush();
			}
		}
		user.setStatus(1);
		return user;
	}
	
		
/*	public UserObject answerRequest (UserObject user) throwsClassNotFoundException, IOException, SQLException {
		String[] answer = user.getMessage().split(",");	
		int id=-1;
		//Get request sender ID
		rs = dbconn.executeSQL("select user_id from users where username=\""+ answer[1] +"\";");
		while(rs.next())
		{
			id = rs.getInt("user_id");
		}
		
		if(answer[0].equals("Accept")){
			// Add to Friends table
			boolean ret=dbconn.executeUpdate("insert into friends values("+user.getUserID()+", "+id+");");
			if(ret)	System.out.println("Friend Added Successfully");
			else System.out.println("Something wrong adding Friend");
		}
		// Delete from friendRequests table
		boolean ret=dbconn.executeUpdate("delete from friendRequests where receiver_id="+user.getUserID()+" AND sender_id="+id+";");
		if(ret)	System.out.println("Request Deleted Successfully");
		else System.out.println("Something wrong deleting Request");
			
		//Get remaining requests
		rs = dbconn.executeSQL("select username from users where user_id IN "
				+ "(select sender_id from friendRequests where receiver_id="+user.getUserID()+");");
		String senderNames="";
		while(rs.next())
		{
			senderNames+= rs.getString("username") + ",";;	
		}
		if(senderNames.isEmpty()) senderNames="none";
		
		user.setMessage(senderNames);
		user.setOperation("Take Request List");
		user.setStatus(1);
		return user;
	}*/
	
	public void deleteRequest(UserObject user) throws SQLException, IOException {
		rs = dbconn.executeSQL("select user_id from users where username=\""+user.getMessage()+"\";");
		int id=-1;
		while(rs.next())
		{
			id = rs.getInt("user_id");		
		}
	    dbconn.executeUpdate("delete from friendRequests where sender_id="+user.getUserID()+" and receiver_id="+id+";");
	    // Update them if online that they're request has been deleted 
	  	if(streams.containsKey(user.getMessage())){
	  		ObjectOutputStream toStrangerSocket = streams.get(user.getMessage());
	  		UserObject msgToStranger = new UserObject();
	  		msgToStranger.setOperation("Remove from Request List");
	        msgToStranger.setMessage(user.getUsername());
	        msgToStranger.setStatus(1);
	        toStrangerSocket.writeUnshared(msgToStranger);
	        toStrangerSocket.flush();
	  	}
	  
	}
	
	public void deleteFriend(UserObject user)  throws ClassNotFoundException, IOException,SQLException {
		//Disable an old friendship in the database friendshipHistory table
		//1-Get friend's ID from database
		rs = dbconn.executeSQL("select user_id from users where username=\""+user.getMessage()+"\";");
		int friendID=-1;
		while(rs.next())
		{
			friendID= rs.getInt("user_id");	
		}
		//2-Delete from friends table
		dbconn.executeUpdate("delete from friends where user_id="+user.getUserID()+" and friend_id="+friendID+" OR user_id="+friendID
				+" and friend_id="+user.getUserID()+";");
		//3-Inform friend if online
		if(streams.containsKey(user.getMessage())){
			ObjectOutputStream toStrangerSocket = streams.get(user.getMessage());
			UserObject msgToStranger = new UserObject();
			msgToStranger.setOperation("Remove from Buddy List");
        	msgToStranger.setMessage(user.getUsername());
        	msgToStranger.setStatus(1);
        	toStrangerSocket.writeUnshared(msgToStranger);
        	toStrangerSocket.flush();
		}
	}
	
	public UserObject logOut(UserObject user, String username) throws IOException, SQLException {
		//Broadcast to user's friends this client is logging off
		streams.remove(username);
		notifyLoggedOut(user, username);
		user.setMessage("Log out successful.");
		user.setStatus(1);
		return user;
	}
	
	public void notifyLoggedOut(UserObject user, String username) throws IOException, SQLException {
		rs = dbconn.executeSQL("select username from Users where user_id IN (select friend_id from friends where user_id="+user.getUserID()+");");
		while(rs.next()) {
			String friendsName = rs.getString("username");
			if(streams.containsKey(friendsName)){
		        ObjectOutputStream toFriendSocket = streams.get(friendsName);
		        UserObject notifyFriend = new UserObject();
		        notifyFriend.setOperation("Friend Logged Off");
		        notifyFriend.setMessage(username);
		        notifyFriend.setStatus(1);
		        toFriendSocket.writeUnshared(notifyFriend);
		        toFriendSocket.flush();
			}
		}
	}
	
/*	public Socket findStream(String username) {
		//Find a stream in the streams list to send message to
		for (Iterator<String> iter = streams.keySet().iterator(); iter.hasNext(); ) {
		    String key = iter.next();
		    if (key.equalsIgnoreCase(username)) {
		    	stream = streams.get(key);
		    	return stream;
		    }
		}
		return stream;
	}*/
	
	private static Map<String, ObjectOutputStream> streams = new HashMap<String, ObjectOutputStream>();
	dbConnection dbconn = null;
	ResultSet rs = null;
	UserObject userIn = null;
	ObjectInputStream IN = null;
	ObjectOutputStream OUT = null;
}