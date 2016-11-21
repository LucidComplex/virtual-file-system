import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by tan on 11/22/16.
 */
public class FileObjectTest {

    @Test
    public void isFileTest() {
        FileObject file = new FileObject("test_file");
        assertTrue(file.isFile());
    }

    @Test
    public void isDirectoryTest() {
        FileObject file = new FileObject("test_dir", FileObject.DIRECTORY);
        assertFalse(file.isFile());
    }

    @Test
    public void getCreationTimeTest() {
        long timeBefore = System.currentTimeMillis();
        FileObject file = new FileObject("Test.file");
        assertTrue(timeBefore <= file.getCreationTime());
    }

    @Test
    public void appendContent() {
        FileObject file = new FileObject("test");
        file.appendContent("Hello ");
        file.appendContent("World.");
        String contents = file.getContent();
        assertEquals("Hello World.", contents);
    }
}