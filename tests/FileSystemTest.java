import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by tan on 11/22/16.
 */
public class FileSystemTest {

    @Test
    public void currentWorkingDir() {
        FileSystem fs = new FileSystem();
        String pwd = fs.pwd();
        assertEquals("/root", pwd);
    }

    @Test
    public void changeDirectory() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
    }

    @Test
    public void changeDirAbsolute() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
        fs.mkdir("anotherFolder");
        fs.cd("/root/newFolder/anotherFolder");
        assertEquals("/root/newFolder/anotherFolder", fs.pwd());
    }

    @Test
    public void relativeCd() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("newFolder/another");
        fs.cd("newFolder/another");
        assertEquals("/root/newFolder/another", fs.pwd());
    }

    @Test(expected = NotADirectoryException.class)
    public void changeDirectoryIntoFile() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("File");
        fs.cd("File");
    }

    @Test
    public void mkdir() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        List<String> listing = fs.ls();
        assertTrue(listing.contains("newFolder"));
    }

    @Test
    public void mkdirAbsolute() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        List<String> listing = fs.ls("/root/newFolder");
        assertTrue(listing.contains("another"));
    }

    @Test
    public void relativeMkdir() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("newFolder/another");
        List<String> dir = fs.ls("newFolder");
        assertTrue(dir.contains("another"));
    }

    @Test(expected = PathNotFoundException.class)
    public void mkdirNonExisting() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder/another");
    }

    @Test
    public void rmdir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.rmdir("newFolder");
        List<String> dir = fs.ls();
        assertFalse(dir.contains("newFolder"));
    }

    @Test
    public void absoluteRmdir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        fs.rmdir("/root/newFolder/another");
        List<String> dir = fs.ls("/root/newFolder");
        assertFalse(dir.contains("another"));
    }

    @Test
    public void relativeRmDir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("newFolder/another");
        fs.rmdir("newFolder/another");
        List<String> dir = fs.ls("newFolder");
        assertFalse(dir.contains("another"));
    }

    @Test
    public void touch() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("file");
        List<String> list = fs.ls();
        assertTrue(list.contains("file"));
    }

    @Test
    public void edit() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.edit("test", "Hello World.");
        assertEquals("Hello World.", fs.cat("test"));
    }

    @Test
    public void absoluteEdit() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.edit("/root/test", "Hello World.");
        assertEquals("Hello World.", fs.cat("test"));
    }

    @Test
    public void rm() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("newFile");
        fs.rm("newFile");
        assertEquals(0, fs.ls().size());
    }
}