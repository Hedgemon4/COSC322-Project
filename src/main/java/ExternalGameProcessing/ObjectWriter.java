package ExternalGameProcessing;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A class that writes objects to a file. When the file reaches a certain size, it closes the file and opens a new one. This is so that the files can be pushed to GitHub
 */
public class ObjectWriter implements Closeable {
    private ObjectOutputStream out;
    private int objectsWritten = 0;
    private final int maxObjects;
    private final String filename;
    private final String fileExtension;
    private int filesWritten = 0;

    public ObjectWriter(String filename, int maxObjects) throws IOException {
        String[] parts = filename.split("\\.");
        this.filename = parts[0];
        this.fileExtension = parts[1];
        this.maxObjects = maxObjects;
        out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(this.filename + filesWritten + "." + fileExtension))));
    }

    public void writeDataPair(Object[] o) throws IOException {
        out.writeObject(o);
        objectsWritten++;
        if (objectsWritten >= maxObjects) {
            out.close();
            filesWritten++;
            objectsWritten = 0;
            out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(filename + filesWritten + "." + fileExtension))));
        }
    }

    public void close() throws IOException {
        out.close();
    }
}