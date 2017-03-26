/*
 * Database Connectivity and Commands for the CS656 Chat App
 * @author Jeremy Hochheiser for CS*656 Spring 2017 Guiling Wang
 */

package com.cs656chatapp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class dbConnection {

	final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	final String DB_URL = "jdbc:mysql://localhost/cs656chatapp?useSSL=false";
	public Connection conn = null;
	public Statement stmt = null;
	public PreparedStatement prepstmt = null;

	public dbConnection() throws ClassNotFoundException, SQLException
	{
		connectToDatabase("Jeremy", "development");
	}

	public boolean connectToDatabase(String username, String password) throws SQLException, ClassNotFoundException
	{
	      //STEP 2: Register JDBC driver
	      Class.forName(JDBC_DRIVER);

	      //STEP 3: Open a connection
	      System.out.println("Connecting to database...");
	      conn = DriverManager.getConnection(DB_URL,username,password);
         System.out.println("Connected to database");
	      return true;
	}

	public ResultSet executeSQL(String sql)
	{
		ResultSet result = null;
	    try
	    {
			stmt = conn.createStatement();
		    result = stmt.executeQuery(sql);
		}
	    catch (SQLException e)
	    {
			System.out.println(e);
		}

	    return result;
	}

   public boolean executeUpdate(String sql)
	{
	    try
	    {
			stmt = conn.createStatement();
		   stmt.executeUpdate(sql);
         return true;
		} catch (SQLException e) {
			System.out.println(e);
         return false;
		}
	}

	public PreparedStatement prepareStatement(String sql)
	{
		try
		{
			prepstmt = conn.prepareStatement(sql);
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		return prepstmt;
   }


}