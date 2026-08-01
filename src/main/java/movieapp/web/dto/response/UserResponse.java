package movieapp.web.dto.response;

import movieapp.model.User;

import java.time.LocalDateTime;

public class UserResponse {

    private final int id;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

} // end of class