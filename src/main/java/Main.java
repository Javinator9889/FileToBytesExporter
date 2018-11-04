import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String FILE = "kafka";
    private static final String EXTENSION = "txt";
    public static final String FILENAME = FILE + "." + EXTENSION;
    private static final int DIRS_NUMBER = 10000;

    public static void main(String[] args) {
//        Test.createFiles(true);
        String[] dirs = Test.getAllDirs();
        FileToBytesExporter exporter = new FileToBytesExporter();
        exporter.setFilename(FILENAME);
        exporter.setPaths(dirs);
        try {
            exporter.readSource(true);
            exporter.getReadData();
            System.out.println("Read all files!");
            File dest = new File(FILE + ".o" + EXTENSION);
            exporter.writeObject(dest);
            System.out.println("Wrote file! " + dest.toString());
//            System.out.println(data);
            TimeUnit.SECONDS.sleep(5);
            exporter.readObject(dest);
            System.out.println(exporter.getFileSeparator());
//        TimeUnit.SECONDS.sleep(5);
            System.out.println(exporter.getReadData());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        /*FileToBytesExporter exporter = new FileToBytesExporter();
        exporter.setFilename(FILENAME);
        exporter.readSource();
        exporter.writeObject(new File(FILENAME + ".o" + EXTENSION));
        System.out.println("waiting");
        TimeUnit.SECONDS.sleep(5);
        File dest = new File(FILENAME + ".o" + EXTENSION);
        exporter.readObject(dest);
        System.out.println(exporter.getFileSeparator());
//        TimeUnit.SECONDS.sleep(5);
        System.out.println(exporter.getReadData());*/
//        Test.createFiles(true);
    }

    private static final class Test {
        private static void createFiles(boolean debug) {
            File dirs = new File("dirs");
            if (!dirs.exists())
                dirs.mkdir();
            File sql = new File(FILENAME);
            for (int i = 0; i < DIRS_NUMBER; ++i) {
                String childDir = "d" + i;
                File newDir = new File(dirs, childDir);
                newDir.mkdir();
                if (debug)
                    System.out.println("Created folder " + newDir.toString());
                File destinationDir = new File(newDir.toString() + "\\" + sql.toString());
                try {
                    Files.copy(sql.toPath(), destinationDir.toPath());
                    if (debug)
                        System.out.println("Copied! " + destinationDir.toString());
                } catch (IOException e) {
                    if (debug)
                        System.err.println("Error while copying - trace: " + e.getMessage());
                }
            }
        }

        private static String[] getAllDirs() {
            String[] dirs = new String[DIRS_NUMBER];
            File rDir = new File("dirs");
            if (!rDir.exists())
                createFiles(false);
            for (int i = 0; i < DIRS_NUMBER; ++i) {
                String childDir = "d" + i;
                File currentDir = new File(rDir, childDir);
                dirs[i] = currentDir.toString();
            }
            return dirs;
        }
    }
}
