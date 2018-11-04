import com.github.javinator9889.exporter.FileToBytesExporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ConvertSingleFile {
    private static final String NAME = "kafka";
    private static final String EXTENSION = "txt";
    private static final String OUTPUT_DIR = "toutput/";
    private static final String OUTPUT_FILE = OUTPUT_DIR + NAME + "-single.o" + EXTENSION;
    private FileToBytesExporter mToBytesExporter;

    @Before
    public void setup() throws IOException {
        String filename = NAME.concat(".").concat(EXTENSION);
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists())
            outputDir.mkdir();
        mToBytesExporter = new FileToBytesExporter(filename);
        System.out.println("Reading file");
        long startTime = System.nanoTime();
        mToBytesExporter.readSource();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Total execution time: " + elapsedTime + " ns.");
        System.out.println("Total execution time: " + elapsedTime / 1000000 + " ms.");
    }

    @Test
    public void generateFile() throws IOException {
        File destination = new File(OUTPUT_FILE);
        System.out.println("Writing file...");
        mToBytesExporter.writeObject(destination);
    }

    @After
    public void readFile() throws IOException {
        File source = new File(OUTPUT_FILE);
        System.out.println("Reading bytes file...");
        long startTime = System.nanoTime();
        mToBytesExporter.readObject(source);
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Total execution time: " + elapsedTime + " ns.");
        System.out.println("Total execution time: " + elapsedTime / 1000000 + " ms.");
        System.out.println(mToBytesExporter.getFileSeparator());
        System.out.println(mToBytesExporter.getReadData());
    }
}
