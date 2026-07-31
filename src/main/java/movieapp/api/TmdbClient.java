package movieapp.api;

import io.github.cdimascio.dotenv.Dotenv;

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

public class TmdbClient {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("TMDB_API_KEY");
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // sends a get request (using title) to tmdb and gets back a response as JSON
    // the response is a wrapper object with an array of movies as 'result'
    public String searchMovies(String title) throws IOException, InterruptedException {

        // encode the title
        // construct the url using api key + encoded title
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&query=" + encodedTitle;

        // build the request as an HttpRequest object to be sent
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        // send the request and save the response as a HttpResponse object
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // return the response's body (json)
        return response.body();
    } // end of searchMovie

    // parses the search results and returns a list of tmdbMovieResult objects
    public List<TmdbMovieResult> parseSearchResults(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TmdbSearchResponse response = objectMapper.readValue(json, TmdbSearchResponse.class);

        return response.getResults();
    } // end of parseSeearchResults()

    // sends a get request using tmdb id to get movie details
    // returns response body as JSON object
    public String getMovieDetails(int tmdbId) throws IOException, InterruptedException {

        // construct the url
        String url = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + API_KEY;

        // build the request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        // send the request and save the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // return the response's body (JSON)
        return response.body();
    } // end of getMovieDetails()

    // parse the movie details request's json and return a TmdbMovieDetails object
    public TmdbMovieDetails parseMovieDetails(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, TmdbMovieDetails.class);
    } // end of parseMovieDetails()



} // end of class
