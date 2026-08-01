package movieapp.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateReviewRequest {

    private int userId;
    private int rating;

    public CreateReviewRequest() {
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public int getRating() {
        return rating;
    }

} // end of class
