package movieapp.web.controller;

import movieapp.auth.JwtService;
import movieapp.exception.InvalidCredentialsException;
import movieapp.model.User;
import movieapp.service.UserService;
import movieapp.web.dto.request.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) throws InvalidCredentialsException {
        User user = userService.login(request.email(), request.password());
        String token = jwtService.generateToken(user.getId());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token));
    }

} // end of class
