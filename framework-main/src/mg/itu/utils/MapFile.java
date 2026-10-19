package mg.itu.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.Part;

/** Uploaded file value object; bytes are held in memory for controller convenience. */
public final class MapFile {
    private final String fileName;
    private final String extension;
    private final byte[] bytes;

    public MapFile(String fileName, String extension, byte[] bytes) {
        this.fileName = fileName;
        this.extension = extension;
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
    }

    public static MapFile from(Part part) throws IOException {
        String submitted = Path.of(part.getSubmittedFileName() == null ? "upload" : part.getSubmittedFileName()).getFileName().toString();
        int dot = submitted.lastIndexOf('.');
        return new MapFile(submitted, dot < 0 ? "" : submitted.substring(dot + 1), part.getInputStream().readAllBytes());
    }

    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public byte[] getBytes() { return bytes.clone(); }

    /** Saves under the supplied directory, creating it when needed. */
    public Path saveTo(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path absoluteDirectory = directory.toAbsolutePath().normalize();
        Path destination = absoluteDirectory.resolve(Path.of(fileName).getFileName().toString()).normalize();
        if (!destination.getParent().equals(absoluteDirectory)) {
            throw new IOException("Invalid upload destination");
        }
        return Files.write(destination, bytes);
    }
}
