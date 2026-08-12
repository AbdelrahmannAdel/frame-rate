package movieapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import movieapp.model.User;

import java.time.LocalDateTime;

public class UserResponse {

    private final int id;
    private final String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String email;

    private final LocalDateTime createdAt;

    public UserResponse(User user, boolean includeEmail) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = includeEmail ? user.getEmail() : null;
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