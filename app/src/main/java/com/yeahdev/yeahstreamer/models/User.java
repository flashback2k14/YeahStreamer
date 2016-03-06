package com.yeahdev.yeahstreamer.models;


public class User {
    private String username;
    private String emailAddress;
    private String profileImage;
    private String provider;

    public User() {}

    public User(String username, String emailAddress, String profileImage, String provider) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.profileImage = profileImage;
        this.provider = provider;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
