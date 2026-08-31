package movieapp.service;

import movieapp.exception.InvalidImageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ImageValidator {

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};

    // reads the file's real bytes and returns the correct extension for its
    // ACTUAL format -- never trusts getContentType() or the original filename
    public String detectExtension(MultipartFile file) throws IOException, InvalidImageException {
        byte[] bytes = file.getBytes();

        if (matchesSignature(bytes, JPEG_SIGNATURE))
            return "jpg";

        if (matchesSignature(bytes, PNG_SIGNATURE))
            return "png";

        throw new InvalidImageException("File is not a valid JPEG or PNG image");
    } // end of detectExtension()

    private boolean matchesSignature(byte[] fileBytes, byte[] signature) {
        if (fileBytes.length < signature.length)
            return false;

        for (int i = 0; i < signature.length; i++) {
            if (fileBytes[i] != signature[i])
                return false;
        }

        return true;
    } // end of matchesSignature()

} // end of class