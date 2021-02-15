package com.example.androidtrlts.Model;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class File {
    private String id;
    private String uid;
    private String name;
    private String dir;
    private String filename;
    private @ServerTimestamp Date timestamp;
    private @ServerTimestamp Date lastUpdated;

    public File() {
    }

    public File(String id, String uid, String name, String dir, String filename, Date timestamp, Date lastUpdated) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.dir = dir;
        this.filename = filename;
        this.timestamp = timestamp;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
