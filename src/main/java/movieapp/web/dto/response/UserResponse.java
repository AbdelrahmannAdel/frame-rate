package movieapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import movieapp.model.User;

import java.time.LocalDateTime;

public class UserResponse {

    private final Integer id;
    private final String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String email;

    private final LocalDateTime createdAt;

    private final String avatarUrl;

    public UserResponse(User user, boolean includeEmail) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = includeEmail ? user.getEmail() : null;
        this.createdAt = user.getCreatedAt();
        this.avatarUrl = user.getAvatarPath() != null ? "/" + user.getAvatarPath() : null;
    }

    public Integer getId() {
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

} // end of class