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

    public void mkdir(String path) throws NotADirectoryException {
        if (path.startsWith("/root")) {
            Node<FileObject> temp = currentNode;
            String[] directories = path.split("/");
            if (directories.length > 1) {
                for (int i = 0; i < directories.length - 1; i++) {
                    cd(directories[i]);
                }
                mkdir(directories[directories.length - 1]);
            }
            currentNode = temp;
        }
        currentNode.addChild(new FileObject(path, FileObject.DIRECTORY));
    }

    public List<String> ls() {
        List<String> listing = new ArrayList<>();
        for (Node<FileObject> child : currentNode.getChildren()) {
            listing.add(child.getItem().getFileName());
        }
        return listing;
    }

    public List<String> ls(String path) throws NotADirectoryException {
        Node<FileObject> temp = currentNode;
        cd(path);
        List<String> listing = ls();
        currentNode = temp;
        return listing;
    }

    public void cd(String path) throws NotADirectoryException {
        if (path.startsWith("/root")) {
            String[] directories = path.split("/");
            if (directories.length == 1) {
                currentNode = root;
            }
            for (int i = 1; i < directories.length; i++) {
                cd(directories[i]);
            }
        }
        for (Node<FileObject> child : currentNode.getChildren()) {
            if (child.getItem().getFileName().equals(path)) {
                if (child.getItem().isFile()) {
                    throw new NotADirectoryException();
                }
                currentNode = child;
                return;
            }
        }
    }

    public void rmdir(String path) throws NotADirectoryException {
        for (int i = 0; i < currentNode.getChildren().size(); i++) {
            Node<FileObject> child = currentNode.getChildren().get(i);
            if (child.getItem().getFileName().equals(path)) {
                if (!child.getItem().isFile()) {
                    currentNode.removeChild(i);
                    return;
                } else {
                    throw new NotADirectoryException();
                }
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

class NotADirectoryException extends Exception {

}
