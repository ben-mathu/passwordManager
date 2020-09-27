package com.benatt.passwords.data.models.passwords.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author bernard
 */
@Entity(tableName = "passwords")
public class Password {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String cipher = "";
    private String accountName = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
