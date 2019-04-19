package com.example.shorturls.tracker;

import com.example.shorturls.url.Url;
import com.example.shorturls.user.User;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Tracker {

    public enum Action {CREATED, ACCESSED}

    @Id
    @GeneratedValue
    private Long id;

    private Long timestamp = Instant.now().toEpochMilli();

    private Action action;

    private String nonExistingPath;

    @ManyToOne
    @JoinColumn
    private Url url;

    @ManyToOne
    @JoinColumn
    private User user;


    public Tracker() {
    }

    public Tracker(Url url, User user, Action action) {
        this.url = url;
        this.user = user;
        this.action = action;
    }

    public Tracker(User user, String nonExistingPath, Action action) {
        this.user = user;
        this.nonExistingPath = nonExistingPath;
        this.action = action;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getNonExistingPath() {
        return nonExistingPath;
    }

    public void setNonExistingPath(String nonExistingPath) {
        this.nonExistingPath = nonExistingPath;
    }

    public Url getUrl() {
        return url;
    }

    public void setUrl(Url url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
