import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class ConvertMultipleFiles {
    private static final String NAME = "kafka";
    private static final String EXTENSION = "txt";
    private static final String OUTPUT_DIR = "toutput/";
    private static final String OUTPUT_FILE = OUTPUT_DIR + NAME + ".o" + EXTENSION;
    private static final int NUMBER_OF_DIRS = 30; // Tested up to 3000
    private FileToBytesExporter mToBytesExporter;

    @Before
    public void setup() throws IOException {
        String filename = NAME.concat(".").concat(EXTENSION);
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists())
            outputDir.mkdir();
        System.out.println("Generating directories...");
        mToBytesExporter = new FileToBytesExporter(filename, generateDirs());
        System.out.println("Reading sources...");
        Measure.START();
        mToBytesExporter.readSource(true);
        System.out.println(Measure.STOP());
    }

    @Test
    public void generateFile() throws IOException {
        File destination = new File(OUTPUT_FILE);
        System.out.println("Writing file...");
        Measure.START();
        mToBytesExporter.writeObject(destination);
        System.out.println(Measure.STOP());
    }

    @After
    public void readFile() throws IOException {
        File source = new File(OUTPUT_FILE);
        System.out.println("Reading bytes file...");
        Measure.START();
        mToBytesExporter.readObject(source);
        System.out.println(Measure.STOP());
    }

    private String[] generateDirs() {
        File dirs = new File("dirs");
        if (!dirs.exists())
            dirs.mkdir();
        else
            return getAllDirs();
        File input = new File(NAME + "." + EXTENSION);
        String[] createdDirs = new String[NUMBER_OF_DIRS];
        Measure.START();
        for (int i = 0; i < NUMBER_OF_DIRS; ++i) {
            String childDir = "d" + i;
            File newDir = new File(dirs, childDir);
            newDir.mkdir();
            File destinationFile = new File(newDir.toString() + "\\" + input.toString());
            try {
                Files.copy(input.toPath(), destinationFile.toPath());
                createdDirs[i] = newDir.toString();
                if (i % 500 == 0) {
                    File subDir = new File(newDir, childDir);
                    subDir.mkdir();
                    File newFile = new File(subDir, input.toString());
                    Files.copy(input.toPath(), newFile.toPath());
                }
            } catch (FileAlreadyExistsException ignored) {

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        String measureTime = Measure.STOP();
        System.out.println(measureTime);
        return createdDirs;
    }

    private String[] getAllDirs() {
        String[] dirs = new String[NUMBER_OF_DIRS];
        File rDir = new File("dirs");
        if (!rDir.exists())
            generateDirs();
        for (int i = 0; i < NUMBER_OF_DIRS; ++i) {
            String childDir = "d" + i;
            File currentDir = new File(rDir, childDir);
            dirs[i] = currentDir.toString();
        }
        return dirs;
    }
}
