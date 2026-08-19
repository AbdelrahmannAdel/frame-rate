package movieapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MovieAppApplication {

    public static void main(String[] args) {

        // load env variables to system properties
        Dotenv.configure()
                .systemProperties()
                .load();

        SpringApplication.run(MovieAppApplication.class, args);
    }

} // end of class