/*
 * Serializable Object for the CS656 Chat App
 * @author Jeremy Hochheiser for CS*656 Spring 2017 Guiling Wang
 */

package com.cs656chatapp.common;

import java.io.Serializable;

public class UserObject implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    int userID;
    String username;
    String name;
    String password;
    int status;
    String operation;
    String message;

    public UserObject() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) { //This will probably never be used.
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation= operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
