package com.benatt.passwordsmanager.data.models.passwords.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author bernard
 */
@Entity(tableName = "passwords")
public class Password implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String cipher = "";
    private String accountName = "";

    public Password() {}

    protected Password(Parcel in) {
        id = in.readInt();
        cipher = in.readString();
        accountName = in.readString();
    }

    public static final Creator<Password> CREATOR = new Creator<Password>() {
        @Override
        public Password createFromParcel(Parcel in) {
            return new Password(in);
        }

        @Override
        public Password[] newArray(int size) {
            return new Password[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(cipher);
        dest.writeString(accountName);
    }

    public boolean isNotEmpty() {
        return !this.cipher.isEmpty() || this.id != 0 || !this.accountName.isEmpty();
    }

    @Override
    public String toString() {
        return "Password{" +
                "id=" + id +
                ", cipher='" + cipher + '\'' +
                ", accountName='" + accountName + '\'' +
                '}';
    }
}
