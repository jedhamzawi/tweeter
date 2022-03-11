package edu.byu.cs.tweeter.model.net.request;

public class RegisterRequest {
    private String username;
    private String password;
    private String image;

    /**
     * Allows construction of the object from Json. Private so it won't be called in normal code.
     */
    private RegisterRequest() {}

    public RegisterRequest(String username, String password, String image) {
        this.username = username;
        this.password = password;
        this.image = image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
