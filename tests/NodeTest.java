import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tan on 11/14/16.
 */
public class NodeTest {
    @Test
    public void setItem() {
        Node<String> testNode = new Node();
        testNode.setItem("testing");
        assertEquals("testing", testNode.getItem());
    }

    @Test
    public void addChild() {
        Node<String> testNode = new Node();
        testNode.addChild("test");
        assertEquals("test", testNode.getChild(0).getItem());
        assertEquals(testNode, testNode.getChild(0).getParent());
    }

    @Test
    public void remove() {
        Node<String> testNode = new Node();
        testNode.addChild("test");
        testNode.removeChild(0);
        try {
            testNode.getChild(0);
        } catch (Exception e) {
            assert true;
            return;
        }
        fail();
    }

    @Test
    public void search() {
        Node<String> testNode = new Node<>();
        testNode.addChild("test");
        testNode.getChild(0).setItem("123");
        testNode.getChild(0).addChild("here");
        boolean result = testNode.search("here");
        assertTrue(result);
        result = testNode.search("test123");
        assertFalse(result);
    }

    @Test
    public void getChildByItem() {
        Node<String> testNode = new Node<>();
        testNode.addChild("test");
        Node<String> childNode = testNode.getChild("test");

        assertEquals("test", childNode.getItem());
        assertNull(testNode.getChild("wala"));
    }

    @Test
    public void removeSelf() {
        Node<String> testNode = new Node<>();
        testNode.addChild("Another testing");
        testNode.getChild(0).remove();
        assertEquals(0, testNode.getChildren().size());
    }
}