import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Created by tan on 11/12/16.
 */
public class VirtualFileSystem {
}

class FileObject implements Serializable, Comparable<FileObject> {
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int compareTo(FileObject fileObject) {
        if (fileObject.getFileName().equals(this.fileName)) {
            if (fileObject.getCreationTime() == this.creationTime) {
                return 0;
            } else if (fileObject.getCreationTime() < creationTime) {
                return -1;
            }
        }
        return 1;
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
        List<String> listing = null;
        if (path.contains("*")) {
            String[] paths = path.split("/");
            // build a path that cd understands, i.e., path minus the file name
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
            listing = ls();
            listing.removeIf(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    String regex = paths[paths.length - 1]; // *.doc
                    regex = regex.replaceAll("\\.", "\\\\.");
                    regex = regex.replaceAll("\\*", ".*");
                    return !s.matches(regex);
                }
            });
        } else {
            cd(path);
            listing = ls();
        }
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
            for (String dir : dirs) {
                cd(dir);
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

    public void rm(String path) throws NotADirectoryException, PathNotFoundException {
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
                child.remove();
            }
        }
        currentNode = temp;
    }

    public void mv(String before, String after) throws NotADirectoryException, PathNotFoundException {
        Node<FileObject> temp = currentNode;
        String[] paths = before.split("/");
        StringBuilder builder = new StringBuilder();
        if (before.startsWith("/")) {
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
        Node<FileObject> toMove = null;
        for (Node<FileObject> child : children) {
            if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                toMove = child;
            }
        }
        if (toMove != null) {
            cd(after);
            toMove.remove();
            toMove.setParent(currentNode);
            currentNode.addChild(toMove);
        }
        currentNode = temp;
    }

    public void rn(String before, String after) throws NotADirectoryException, PathNotFoundException, IllegalOperationException {
        if (after.contains("/")) {
            throw new IllegalOperationException();
        }
        Node<FileObject> temp = currentNode;
        String[] paths = before.split("/");
        StringBuilder builder = new StringBuilder();
        if (before.startsWith("/")) {
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
                child.getItem().setFileName(after);
            }
        }
        currentNode = temp;
    }

    public void cp(String source, String target) throws NotADirectoryException, PathNotFoundException {
        Node<FileObject> temp = currentNode;
        String[] paths = source.split("/");
        StringBuilder builder = new StringBuilder();
        if (source.startsWith("/")) {
            for (int i = 2; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        } else {
            for (int i = 0; i < paths.length - 1; i++) {
                builder.append(paths[i]);
            }
        }
        cd(builder.toString());
        Node<FileObject> sourceNode = null;
        for (Node<FileObject> child : currentNode.getChildren()) {
            if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                sourceNode = child;
                break;
            }
        }
        if (sourceNode != null) {
            Node<FileObject> copy = (Node<FileObject>) Utilities.clone(sourceNode);
            builder = new StringBuilder();
            paths = target.split("/");
            if (target.startsWith("/")) {
                for (int i = 2; i < paths.length - 1; i++) {
                    builder.append(paths[i]);
                }
            } else {
                for (int i = 0; i < paths.length - 1; i++) {
                    builder.append(paths[i]);
                }
            }
            cd(builder.toString());
            copy.getItem().setFileName(paths[paths.length - 1]);
            currentNode.addChild(copy);
        }
        currentNode = temp;
    }
}

class Node<T extends Comparable<T>> implements Serializable {
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

    public Node(Node<T> sourceNode) {
        item = sourceNode.item;
        parent = sourceNode.parent;
        children = new LinkedList<>();
        for (Node<T> child : sourceNode.children) {
            children.add(new Node<T>(child));
        }
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
        if (item.compareTo(this.item) == 0) {
            return true;
        }
        for (Node<T> node : children) {
            if (node.search(item)) {
                return true;
            }
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

    public void remove() {
        if (parent != null) {
            for (int i = 0; i < parent.children.size(); i++) {
                if (parent.children.get(i).equals(this)) {
                    parent.removeChild(i);
                }
            }
        }
    }


    public void setParent(Node<T> parent) {
        this.parent = parent;
    }

    public void addChild(Node<T> child) {
        children.add(child);
    }
}

class Utilities {
    public static Object clone(Object object) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(object);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            return objectIn.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class NotADirectoryException extends Exception {

}

class IllegalOperationException extends Exception {

}

class PathNotFoundException extends Exception {

}
