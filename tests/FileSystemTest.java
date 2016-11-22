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
    public void mkdir() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        List<String> listing = fs.ls();
        assertTrue(listing.contains("newFolder"));
    }

    @Test
    public void changeDirectory() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
    }

    @Test
    public void changeDirAbsolute() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
        fs.mkdir("anotherFolder");
        fs.cd("/root/newFolder/anotherFolder");
        assertEquals("/root/newFolder/anotherFolder", fs.pwd());
    }

    @Test
    public void mkdirAbsolute() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        List<String> listing = fs.ls("/root/newFolder");
        assertTrue(listing.contains("another"));
    }

    @Test(expected = NotADirectoryException.class)
    public void changeDirectoryIntoFile() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.touch("File");
        fs.cd("File");
    }

    @Test
    public void rmdir() throws NotADirectoryException, IllegalOperationException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.rmdir("newFolder");
        List<String> dir = fs.ls();
        assertFalse(dir.contains("newFolder"));
    }

    @Test
    public void absoluteRmdir() throws NotADirectoryException, IllegalOperationException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        fs.rmdir("/root/newFolder/another");
        List<String> dir = fs.ls("/root/newFolder");
        assertFalse(dir.contains("another"));
    }

    @Test
    public void relativeMkdir() throws NotADirectoryException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("newFolder/another");
        List<String> dir = fs.ls("newFolder");
        assertTrue(dir.contains("another"));
    }
}