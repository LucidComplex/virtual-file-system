import org.junit.Test;

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
    public void changeDirectory() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
    }

    @Test
    public void changeDirAbsolute() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.cd("newFolder");
        assertEquals("/root/newFolder", fs.pwd());
        fs.mkdir("anotherFolder");
        fs.cd("/root/newFolder/anotherFolder");
        assertEquals("/root/newFolder/anotherFolder", fs.pwd());
    }

    @Test
    public void relativeCd() throws NotADirectoryException, PathNotFoundException, FileExistsException {
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
    public void mkdir() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        List<String> listing = fs.ls();
        assertTrue(listing.contains("newFolder"));
    }

    @Test
    public void mkdirAbsolute() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        List<String> listing = fs.ls("/root/newFolder");
        assertTrue(listing.contains("another"));
    }

    @Test
    public void relativeMkdir() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("newFolder/another");
        List<String> dir = fs.ls("newFolder");
        assertTrue(dir.contains("another"));
    }

    @Test(expected = PathNotFoundException.class)
    public void mkdirNonExisting() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder/another");
    }

    @Test
    public void rmdir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.rmdir("newFolder");
        List<String> dir = fs.ls();
        assertFalse(dir.contains("newFolder"));
    }

    @Test
    public void absoluteRmdir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.mkdir("/root/newFolder/another");
        fs.rmdir("/root/newFolder/another");
        List<String> dir = fs.ls("/root/newFolder");
        assertFalse(dir.contains("another"));
    }

    @Test
    public void relativeRmDir() throws NotADirectoryException, IllegalOperationException, PathNotFoundException, FileExistsException {
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
    public void absoluteEdit() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.edit("/root/test", "Hello World.");
        assertEquals("Hello World.", fs.cat("test"));
        fs.mkdir("new");
        fs.edit("/root/new/test", "Testing");
        assertEquals(1, fs.ls("/root/new").size());
        assertEquals("Testing", fs.cat("/root/new/test"));
    }

    @Test
    public void relativeEdit() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("newFolder");
        fs.edit("newFolder/test", "Hello World.");
        assertEquals("Hello World.", fs.cat("newFolder/test"));
    }

    @Test
    public void rm() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("newFile");
        fs.rm("newFile");
        assertEquals(0, fs.ls().size());
    }

    @Test
    public void absoluteRm() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("another");
        fs.touch("another/newFile");
        fs.rm("another/newFile");
        assertEquals(0, fs.ls("/root/another").size());
    }

    @Test
    public void move() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.touch("testFile");
        fs.mkdir("folder");
        fs.mv("testFile", "folder");
        assertEquals(1, fs.ls().size());
        List<String> listing = fs.ls("folder");
        assertTrue(listing.contains("testFile"));
    }

    @Test
    public void absoluteMove() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.touch("testFile");
        fs.mkdir("folder");
        fs.mv("/root/testFile", "/root/folder");
        assertEquals(1, fs.ls().size());
        List<String> listing = fs.ls("folder");
        assertTrue(listing.contains("testFile"));
    }

    @Test
    public void rename() throws NotADirectoryException, PathNotFoundException, IllegalOperationException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.touch("testFile");
        fs.mkdir("folder");
        fs.rn("/root/testFile", "testingFile");
        List<String> listing = fs.ls();
        assertFalse(listing.contains("testFile"));
        assertTrue(listing.contains("testingFile"));
    }

    @Test
    public void copy() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("test");
        fs.cp("test", "test2");
        assertTrue(fs.ls().contains("test2") && fs.ls().contains("test"));
    }

    @Test
    public void absoluteCopy() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.touch("test");
        fs.mkdir("folder");
        fs.cp("/root/test", "/root/folder/test2");
        assertTrue(fs.ls("folder").contains("test2") && fs.ls().contains("test"));
    }

    @Test
    public void lsWithStar() throws NotADirectoryException, PathNotFoundException {
        FileSystem fs = new FileSystem();
        fs.touch("test.doc");
        fs.touch("wow.doc");
        fs.touch("amazing.txt");
        List<String> listing = fs.ls("/root/*.doc");
        assertEquals(2, listing.size());
        assertTrue(listing.contains("test.doc") && listing.contains("wow.doc"));
    }

    @Test
    public void cdWithDot() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("folder");
        fs.cd("/root/folder");
        fs.cd("..");
        assertEquals("/root", fs.pwd());

        fs.mkdir("folder/another");
        fs.cd("/root/folder/another");
        fs.cd("../../");
        assertEquals("/root", fs.pwd());
    }

    @Test
    public void mkdirWithDot() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("folder");
        fs.cd("folder");
        fs.mkdir("../another");
        List<String> listing = fs.ls("/root");
        assertTrue(listing.contains("another"));
    }

    @Test
    public void whereis() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.touch("outside");
        fs.mkdir("folder");
        fs.touch("folder/inside");
        fs.mkdir("another");
        fs.touch("another/inside");
        List<String> paths = fs.whereis("inside");
        assertEquals(2, paths.size());
        assertTrue(paths.contains("/root/folder/inside"));
    }

    @Test(expected = FileExistsException.class)
    public void mkdirMultiple() throws NotADirectoryException, PathNotFoundException, FileExistsException {
        FileSystem fs = new FileSystem();
        fs.mkdir("papa");
        fs.mkdir("papa");
    }
}