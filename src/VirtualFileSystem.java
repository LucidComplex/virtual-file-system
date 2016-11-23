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

    public void touch(String path) throws NotADirectoryException, PathNotFoundException {
        if (path.startsWith("/root")) {
            Node<FileObject> temp = currentNode;
            cd("/root");
            String[] paths = path.split("/");
            if (paths.length > 1) {
                for (int i = 2; i < paths.length - 1; i++) {
                    cd(paths[i]);
                }
                currentNode.addChild(new FileObject(paths[paths.length - 1]));
            }
            currentNode = temp;
        } else {
            Node<FileObject> temp = currentNode;
            String[] paths = path.split("/");
            for (int i = 0; i < paths.length - 1; i++) {
                cd(paths[i]);
            }
            currentNode.addChild(new FileObject(paths[paths.length - 1]));
            currentNode = temp;
        }
    }

    public String cat(String path) throws NotADirectoryException, PathNotFoundException {
        Node<FileObject> temp = currentNode;
        String[] paths = path.split("/");
        StringBuilder builder = new StringBuilder();
        if (path.startsWith("/")) {
            for (int i = 2; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        } else {
            for (int i = 0; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        }
        cd(builder.toString());
        List<Node<FileObject>> children = currentNode.getChildren();
        for (Node<FileObject> child : children) {
            if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                currentNode = temp;
                return child.getItem().getContent();
            }
        }
        currentNode = temp;
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

    public void mkdir(String path) throws NotADirectoryException, PathNotFoundException {
        if (path.startsWith("/root")) {
            Node<FileObject> temp = currentNode;
            cd("/root");
            String[] directories = path.split("/");
            for (int i = 2; i < directories.length - 1; i++) {
                cd(directories[i]);
            }
            mkdir(directories[directories.length - 1]);
            currentNode = temp;
        } else {
            Node<FileObject> temp = currentNode;
            String[] dirs = path.split("/");
            for (int i = 0; i < dirs.length - 1; i++) {
                cd(dirs[i]);
            }
            currentNode.addChild(new FileObject(dirs[dirs.length - 1], FileObject.DIRECTORY));
            currentNode = temp;
        }
    }

    public List<String> ls() {
        List<String> listing = new ArrayList<>();
        for (Node<FileObject> child : currentNode.getChildren()) {
            listing.add(child.getItem().getFileName());
        }
        return listing;
    }

    public List<String> ls(String path) throws NotADirectoryException, PathNotFoundException {
        Node<FileObject> temp = currentNode;
        cd(path);
        List<String> listing = ls();
        currentNode = temp;
        return listing;
    }

    public void cd(String path) throws NotADirectoryException, PathNotFoundException {
        if (path.trim().length() == 0) {
            return;
        }
        if (path.startsWith("/root")) {
            String[] directories = path.split("/");
            currentNode = root;
            for (int i = 2; i < directories.length; i++) {
                cd(directories[i]);
            }
            return;
        }
        if (path.contains("/")) {
            String[] dirs = path.split("/");
            for (int i = 0; i < dirs.length; i++) {
                cd(dirs[i]);
            }
            return;
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
        throw new PathNotFoundException();
    }

    public void rmdir(String path) throws NotADirectoryException, IllegalOperationException, PathNotFoundException {
        if (path.startsWith("/root")) {
            Node<FileObject> temp = currentNode;
            String[] directories = path.split("/");
            if (directories.length == 1) {
                throw new IllegalOperationException();
            }
            for (int i = 2; i < directories.length - 1; i++) {
                cd(directories[i]);
            }
            rmdir(directories[directories.length - 1]);
            currentNode = temp;
            return;
        }
        Node<FileObject> temp = currentNode;
        String[] dirs = path.split("/");
        for (int i = 0; i < dirs.length - 1; i++) {
            cd(dirs[i]);
        }
        for (int i = 0; i < currentNode.getChildren().size(); i++) {
            Node<FileObject> child = currentNode.getChildren().get(i);
            if (child.getItem().getFileName().equals(dirs[dirs.length - 1])) {
                if (!child.getItem().isFile()) {
                    currentNode.removeChild(i);
                    currentNode = temp;
                    return;
                } else {
                    throw new NotADirectoryException();
                }
            }
        }
        currentNode = temp;
    }

    public void edit(String path, String content) throws NotADirectoryException, PathNotFoundException {
        touch(path);
        Node<FileObject> temp = currentNode;
        String[] paths = path.split("/");
        StringBuilder builder = new StringBuilder();
        if (path.startsWith("/")) {
            for (int i = 2; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        } else {
            for (int i = 0; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        }
        cd(builder.toString());

        List<Node<FileObject>> children = currentNode.getChildren();
        for (Node<FileObject> child : children) {
            if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                child.getItem().appendContent(content);
                currentNode = temp;
                return;
            }
        }
        currentNode = temp;
    }

    public void rm(String path) {

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

class IllegalOperationException extends Exception {

}

class PathNotFoundException extends Exception {

}
