import com.github.javinator9889.exporter.FileToBytesExporter;
import org.junit.Test;

import java.io.File;

public class StaticTest {
    private String filename = "kafka.txt";
    private String parent = "test_path";

    @Test
    public void generateFile() throws Exception {
        Measure.START();
        String fileContents = FileToBytesExporter.readSource(new File(parent, filename));
        System.out.println(Measure.STOP());
        Measure.START();
        FileToBytesExporter.writeObject(fileContents, new File(parent, "kafka.extxt"));
        System.out.println(Measure.STOP());
    }

    @Test
    public void readFile() throws Exception {
        FileToBytesExporter reader = new FileToBytesExporter();
        Measure.START();
        reader.readObject(new File(parent, "kafka.extxt"));
        System.out.println(Measure.STOP());
        System.out.println(reader.getReadData());
    }
}
