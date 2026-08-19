package movieapp.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import movieapp.api.dto.TmdbMovieDetails;
import movieapp.api.dto.TmdbMovieResult;
import movieapp.api.dto.TmdbSearchResponse;

import java.util.List;

@Component
public class TmdbClient {

    private final String apiKey;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TmdbClient(@Value("${TMDB_API_KEY}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String searchMovies(String title) throws IOException, InterruptedException {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + encodedTitle;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    } // end of searchMovies()

    public List<TmdbMovieResult> parseSearchResults(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TmdbSearchResponse response = objectMapper.readValue(json, TmdbSearchResponse.class);

        return response.getResults();
    } // end of parseSearchResults()

    public String getMovieDetails(int tmdbId) throws IOException, InterruptedException {
        String url = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    } // end of getMovieDetails()

    public TmdbMovieDetails parseMovieDetails(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, TmdbMovieDetails.class);
    } // end of parseMovieDetails()

} // end of class