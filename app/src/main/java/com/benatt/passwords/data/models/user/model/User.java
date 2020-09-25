package com.benatt.passwords.data.models.user.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author bernard
 */
@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String username;
    private String password;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
