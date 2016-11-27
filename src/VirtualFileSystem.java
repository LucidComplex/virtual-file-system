import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
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
        content = "";
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

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}

class FileSystem implements Serializable {
    private Node<FileObject> root;
    private Node<FileObject> currentNode;

    public FileSystem() {
        root = new Node<>(new FileObject("root", FileObject.DIRECTORY));
        currentNode = root;
    }

    public void touch(String path) throws NotADirectoryException, PathNotFoundException, FileExistsException {
        if (path.startsWith("/root")) {
            Node<FileObject> temp = currentNode;
            cd("/root");
            String[] paths = path.split("/");
            if (paths.length > 1) {
                for (int i = 2; i < paths.length - 1; i++) {
                    cd(paths[i]);
                }
                for (Node<FileObject> child : currentNode.getChildren()) {
                    if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                        currentNode = temp;
                        throw new FileExistsException();
                    }
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
            for (Node<FileObject> child : currentNode.getChildren()) {
                if (child.getItem().getFileName().equals(paths[paths.length - 1])) {
                    currentNode = temp;
                    throw new FileExistsException();
                }
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

    private String buildPath(Node<FileObject> node) {
        Stack<String> dir = new Stack<>();
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

    public String pwd() {
        return buildPath(currentNode);
    }

    public void mkdir(String path) throws NotADirectoryException, PathNotFoundException, FileExistsException {
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
            for (Node<FileObject> child : currentNode.getChildren()) {
                if (child.getItem().getFileName().equals(dirs[dirs.length - 1])) {
                    currentNode = temp;
                    throw new FileExistsException();
                }
            }
            currentNode.addChild(new FileObject(dirs[dirs.length - 1], FileObject.DIRECTORY));
            currentNode = temp;
        }
    }

    public List<String> ls() {
        List<String> listing = new ArrayList<>();
        for (Node<FileObject> child : currentNode.getChildren()) {
            listing.add(child.getItem().getFileName() + " - " + (child.getItem().isFile() ? "File" : "Directory") +
                    " - Date Created: " + new Date(child.getItem().getCreationTime()).toString());
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
                    s = s.substring(0, s.indexOf(" - "));
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
        if (path.equals("..")) {
            if (currentNode.getParent() != null) {
                currentNode = currentNode.getParent();
            }
            return;
        }
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
                    currentNode = temp;
                    throw new NotADirectoryException();
                }
            }
        }
        currentNode = temp;
    }

    public void edit(String path, String content) throws NotADirectoryException, PathNotFoundException, FileExistsException {
        try {
            touch(path);
        } catch (FileExistsException e) {
            ;
        }
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

    public void rm(String path) throws NotADirectoryException, PathNotFoundException, NotAFileException {
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
                if (!child.getItem().isFile()) {
                    currentNode = temp;
                    throw new NotAFileException();
                }
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
            copy.getItem().setCreationTime(System.currentTimeMillis());
            copy.setParent(currentNode);
            currentNode.addChild(copy);
        }
        currentNode = temp;
    }

    public List<String> whereis(String fileName) {
        List<String> foundItems = new ArrayList<>();
        Stack<Node<FileObject>> frontier = new Stack<>();
        frontier.push(root);
        Node<FileObject> head = null;
        while (!frontier.isEmpty()) {
            head = frontier.pop();
            for (Node<FileObject> child : head.getChildren()) {
                frontier.push(child);
            }
            if (head.getItem().getFileName().equals(fileName)) {
                foundItems.add(buildPath(head));
            }
        }
        return foundItems;
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

    public static void serialize(FileSystem fileSystem) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(fileSystem);
            FileOutputStream outFile = new FileOutputStream("disk");
            outFile.write(byteOut.toByteArray());
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileSystem deserialize() {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(Files.readAllBytes(new File("disk").toPath()));
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            return (FileSystem) objectIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Console extends JFrame {
    private JLabel workingDirectoryLabel;
    private JTextField commandTextField;
    private JTextArea resultsArea;

    private FileSystem fileSystem;
    Console() {
        super("Console");
        fileSystem = Utilities.deserialize();
        if (fileSystem == null) {
            fileSystem = new FileSystem();
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(600, 400));
        getContentPane().setBackground(Color.BLACK);

        // init scroll pane
        resultsArea = new JTextArea();
        resultsArea.setBackground(Color.BLACK);
        resultsArea.setLineWrap(true);
        resultsArea.setEnabled(false);
        JScrollPane resultsPane = new JScrollPane(resultsArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsPane.setBorder(null);
        getContentPane().add(resultsPane);

        // init text input and current dir
        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        workingDirectoryLabel = new JLabel("/root > ");
        c.weightx = 0.08;
        workingDirectoryLabel.setForeground(Color.WHITE);
        textPanel.add(workingDirectoryLabel, c);
        c.weightx = 1 - c.weightx;
        c.gridx = 1;
        commandTextField = new JTextField();
        commandTextField.setForeground(Color.WHITE);
        commandTextField.setBackground(Color.BLACK);
        commandTextField.setBorder(null);
        textPanel.add(commandTextField, c);
        commandTextField.grabFocus();
        getContentPane().add(textPanel, BorderLayout.SOUTH);

        // set event handlers
        commandTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String command = commandTextField.getText();
                commandTextField.setText("");
                String[] tokens = command.split(" ");
                String exec = tokens[0];
                String args = "";
                for (int i = 1; i < tokens.length; i++) {
                    args += tokens[i] + " ";
                }
                args = args.trim();
                String[] split;
                try {
                    println(fileSystem.pwd() + " > " + command);
                    switch (exec) {
                        case "ls":
                            List<String> listing = fileSystem.ls(args);
                            for (String path : listing) {
                                println(path);
                            }
                            break;
                        case "touch":
                            fileSystem.touch(args);
                            break;
                        case "cd":
                            fileSystem.cd(args);
                            workingDirectoryLabel.setText(fileSystem.pwd());
                            break;
                        case "mkdir":
                            fileSystem.mkdir(args);
                            break;
                        case "rmdir":
                            fileSystem.rmdir(args);
                            break;
                        case "rm":
                            fileSystem.rm(args);
                            break;
                        case "rn":
                            split = args.split(" ");
                            fileSystem.rn(split[0], split[1]);
                            break;
                        case "mv":
                            split = args.split(" ");
                            fileSystem.mv(split[0], split[1]);
                            break;
                        case "cp":
                            split = args.split(" ");
                            fileSystem.cp(split[0], split[1]);
                            break;
                        case "whereis":
                            fileSystem.whereis(args);
                            break;
                        case "show":
                            String out = fileSystem.cat(args);
                            println(out);
                            break;
                        case ">":
                            fileSystem.rm(args);
                            fileSystem.touch(args);
                        case ">>":
                            println(fileSystem.cat(args));
                            workingDirectoryLabel.setText("EDITING " + args + " (CTRL+O to save) >");
                            commandTextField.removeActionListener(this);
                            final String[] text = {""};
                            ActionListener editingListener = new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent actionEvent) {
                                    text[0] += commandTextField.getText() + "\n";
                                    println(commandTextField.getText());
                                    commandTextField.setText("");
                                }
                            };
                            commandTextField.addActionListener(editingListener);
                            ActionListener that = this;
                            String finalArgs = args;
                            String finalArgs1 = args;
                            commandTextField.addKeyListener(new KeyListener() {

                                @Override
                                public void keyTyped(KeyEvent keyEvent) {
                                    return;
                                }

                                @Override
                                public void keyPressed(KeyEvent keyEvent) {
                                    if (keyEvent.getKeyCode() == 'O') {
                                        System.out.println("event");
                                        if (keyEvent.isControlDown()) {
                                            commandTextField.removeActionListener(editingListener);
                                            commandTextField.addActionListener(that);
                                            commandTextField.removeKeyListener(this);
                                            workingDirectoryLabel.setText(fileSystem.pwd());
                                            try {
                                                fileSystem.edit(finalArgs, text[0]);
                                            } catch (NotADirectoryException e) {
                                                println(exec + ": " + finalArgs1 + ": not a directory");
                                            } catch (PathNotFoundException e) {
                                                println(exec + ": " + finalArgs1 + ": no such file or directory");
                                            } catch (FileExistsException e) {
                                                println(exec + ": " + finalArgs1 + ": file exists");
                                            } catch (Exception e) {
                                                println(exec + ": " + finalArgs1 + ": unexpected error occurred");
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void keyReleased(KeyEvent keyEvent) {
                                    return;
                                }
                            });
                            break;
                        default:
                            resultsArea.append("Unrecognized command.");
                    }
                    resultsArea.append("\n");
                } catch (NotADirectoryException e) {
                    println(exec + ": " + args + ": not a directory");
                } catch (PathNotFoundException e) {
                    println(exec + ": " + args + ": no such file or directory");
                } catch (FileExistsException e) {
                    println(exec + ": " + args + ": file exists");
                } catch (Exception e) {
                    println(exec + ": " + args + ": unexpected error occurred");
                    e.printStackTrace();
                }
                try {
                    resultsArea.scrollRectToVisible(resultsArea.modelToView(resultsArea.getDocument().getLength()));
                } catch (BadLocationException e) {
                    throw new RuntimeException();
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Utilities.serialize(fileSystem);
            }
        });
        pack();
        setVisible(true);
    }
    private void println(String message) {
        resultsArea.append(message);
        resultsArea.append("\n");
    }
    public static void main(String[] args) {
        Console console = new Console();
    }
}

class NotADirectoryException extends Exception {

}

class IllegalOperationException extends Exception {

}

class PathNotFoundException extends Exception {

}

class FileExistsException extends Exception {

}

class NotAFileException extends Exception {

}
