package movieapp.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    // stores the file, returns the relative path/key it was stored under
    String store(MultipartFile file, String filename) throws IOException;

    void delete(String path) throws IOException;

} // end of interface