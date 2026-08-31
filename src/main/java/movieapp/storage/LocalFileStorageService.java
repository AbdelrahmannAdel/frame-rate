package movieapp.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadDir;

    public LocalFileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir, "avatars");
    }

    @Override
    public String store(MultipartFile file, String filename) throws IOException {
        Files.createDirectories(uploadDir);

        Path destination = uploadDir.resolve(filename);
        file.transferTo(destination);

        return "avatars/" + filename;
    } // end of store()

    @Override
    public void delete(String path) throws IOException {
        Path target = uploadDir.getParent().resolve(path);
        Files.deleteIfExists(target);
    } // end of delete()

} // end of class