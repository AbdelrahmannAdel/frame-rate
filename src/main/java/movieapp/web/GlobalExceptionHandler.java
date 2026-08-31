package movieapp.web;

import movieapp.exception.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({
            DuplicateUsernameException.class,
            DuplicateEmailException.class,
            DuplicateMovieException.class,
            DuplicateReviewException.class,
            DuplicateWatchlistException.class,
            DuplicateFollowException.class
    })

    public ResponseEntity<String> handleConflict(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler({SelfFollowException.class, InvalidRatingException.class, InvalidImageException.class})
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler({UnauthorizedActionException.class, NotMutualFollowException.class})
    public ResponseEntity<String> handleForbidden(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNumberFormat(NumberFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid number format: " + e.getMessage());
    }

    // MUST be registered before the general IOException handler below --
    // MaxUploadSizeExceededException appears to wrap an IOException as its
    // cause (Spring MVC falls back to walking the cause chain when no exact
    // type match exists), so without this specific handler it was being
    // caught by handleIOFailure() instead and misreported as a 502 (an
    // "upstream service failed" status meant for TMDB call failures) rather
    // than the correct 413 (client sent a payload that's too large)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File exceeds the maximum allowed size");
    }

    @ExceptionHandler({IOException.class, InterruptedException.class})
    public ResponseEntity<String> handleIOFailure(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("I/O failure: " + e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccess(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: " + e.getMessage());
    }

} // end of class