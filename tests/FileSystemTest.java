import org.junit.Test;

import java.util.List;

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
    public void currentWorkingDir() {
        FileSystem fs = new FileSystem();
        String pwd = fs.pwd();
        assertEquals("/root", pwd);
    }

    @Test
    public void mkdir() {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        List<String> listing = fs.ls();
        assertTrue(listing.contains("newFolder"));
    }

    @Test
    public void changeDirectory() {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
    }

    @Test
    public void changeDirAbsolute() {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
        fs.mkdir("anotherFolder");
        fs.cd("/root/newFolder/anotherFolder");
        assertEquals("/root/newFolder/anotherFolder", fs.pwd());
    }
}