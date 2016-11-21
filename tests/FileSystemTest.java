import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tan on 11/22/16.
 */
public class FileSystemTest {

    @Test
    public void createFile() {
        FileSystem fs = new FileSystem();
        fs.touch("test_file");
        assertEquals("test_file", fs.get("test_file").getFileName());
    }

    @Test
    public void createDirectory() {
        FileSystem fs = new FileSystem();
    }

}