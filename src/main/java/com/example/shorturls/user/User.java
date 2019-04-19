package com.example.shorturls.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class User {

    @Id
    private String cookie;

    private Long createdTimestamp = Instant.now().toEpochMilli();

    public User() {
    }

    public User(String cookie) {
        this.cookie = cookie;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
