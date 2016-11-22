import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by tan on 11/12/16.
 */
public class VirtualFileSystem {
}

class FileObject {
    public static final int DIRECTORY = 0;
    public static final int FILE = 1;
    private String fileName;
    private boolean isFile;
    private long creationTime;
    private String content;

    public FileObject(String fileName, int FILE_TYPE) {
        this.fileName = fileName;
        this.creationTime = System.currentTimeMillis();
        content = new String();
        switch (FILE_TYPE) {
            case FILE:
                isFile = true;
                break;
            case DIRECTORY:
                isFile = false;
                break;
        }
    }

    public FileObject(String fileName) {
        this(fileName, FILE);
    }

    public boolean isFile() {
        return isFile;
    }

    public String getFileName() {
        return fileName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getContent() {
        return content;
    }

    public void appendContent(String text) {
        content += text;
    }
}

class FileSystem {
    private Node<FileObject> root;
    private Node<FileObject> currentNode;

    public FileSystem() {
        root = new Node<>(new FileObject("root", FileObject.DIRECTORY));
        currentNode = root;
    }

    public void touch(String fileName) {
        root.addChild(new FileObject(fileName));
    }

    public FileObject get(String fileName) {
        List<Node<FileObject>> children = root.getChildren();
        for (Node<FileObject> child : children) {
            if (child.getItem().getFileName().equals(fileName)) {
                return child.getItem();
            }
        }
        return null;
    }

    public String pwd() {
        Stack<String> dir = new Stack<>();
        Node<FileObject> node = currentNode;
        while (node != null) {
            dir.push(node.getItem().getFileName());
            node = node.getParent();
        }
        StringBuilder builder = new StringBuilder();
        while (!dir.isEmpty()) {
            builder.append("/");
            builder.append(dir.pop());
        }
        return builder.toString();
    }

    public void mkdir(String folderName) {
        currentNode.addChild(new FileObject(folderName, FileObject.DIRECTORY));
    }

    public List<String> ls() {
        List<String> listing = new ArrayList<>();
        for (Node<FileObject> child : currentNode.getChildren()) {
            listing.add(child.getItem().getFileName());
        }
        return listing;
    }

    public List<String> ls(String path) {
        Node<FileObject> temp = currentNode;
        cd(path);
        List<String> listing = ls();
        currentNode = temp;
        return listing;
    }

    public void cd(String path) {
        if (path.startsWith("/root")) {
            String[] directories = path.split("/");
            if (directories.length == 1) {
                currentNode = root;
            }
            for (String dir : directories) {
                cd(dir);
            }
        }
        for (Node<FileObject> child : currentNode.getChildren()) {
            if (child.getItem().getFileName().equals(path)) {
                currentNode = child;
                return;
            }
        }
    }
}

class Node<T> {
    private T item;
    private Node<T> parent;
    private List<Node<T>> children;

    public Node() {
        children = new LinkedList<>();
    }

    public Node(T item) {
        this();
        this.item = item;
    }

    public Node(T item, Node<T> parent) {
        this(item);
        this.parent = parent;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public Node<T> getParent() {
        return parent;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public void addChild(T item) {
        Node<T> temp = new Node<>(item, this);
        children.add(temp);
    }

    public void removeChild(int i) {
        children.remove(i);
    }

    public boolean search(T item) {
        if (item.equals(this.item)) {
            return true;
        }
        for (Node<T> node : children) {
            return node.search(item);
        }
        return false;
    }

    public Node<T> getChild(int i) {
        return children.get(i);
    }

    public Node<T> getChild(T item) {
        for (Node<T> child : children) {
            if (child.getItem().equals(item)) {
                return child;
            }
        }
        return null;
    }

    public T getItem() {
        return item;
    }
}
