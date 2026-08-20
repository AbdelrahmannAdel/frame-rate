package movieapp.web.controller;

import movieapp.auth.JwtService;
import movieapp.auth.PasswordHasher;
import movieapp.exception.DuplicateEmailException;
import movieapp.exception.DuplicateUsernameException;
import movieapp.model.User;
import movieapp.service.UserService;
import movieapp.web.dto.request.RegisterUserRequest;
import movieapp.web.dto.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterUserRequest request)
            throws DuplicateUsernameException, DuplicateEmailException {

        String hashedPassword = PasswordHasher.hash(request.password());

        User user = userService.registerUser(request.username(), request.email(), hashedPassword);
        UserResponse userResponse = new UserResponse(user, true);
        String token = jwtService.generateToken(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("user", userResponse, "token", token));
    } // end of register()

} // end of class