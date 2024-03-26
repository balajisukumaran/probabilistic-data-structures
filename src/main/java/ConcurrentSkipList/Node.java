package ConcurrentSkipList;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

public class Node {
    private final KeyValuePair keyValuePair;
    private final List<Node> next = new ArrayList<>();
    private final ReentrantLock nodeLock = new ReentrantLock();
    private final AtomicBoolean marked = new AtomicBoolean(false);
    private final AtomicBoolean fullyLinked = new AtomicBoolean(false);
    private final int topLevel;

    public Node(int key, String value, int level) {
        this.keyValuePair = new KeyValuePair(key, value);
        for (int i = 0; i <= level; i++) {
            next.add(null);
        }
        this.topLevel = level;
    }

    public void lock() {
        nodeLock.lock();
    }

    public ReentrantLock getNodeLock() {
        return nodeLock;
    }

    public void unlock() {
        nodeLock.unlock();
    }

    public boolean isMarked() {
        return marked.get();
    }

    public void setMarked(boolean value) {
        marked.set(value);
    }

    public boolean isFullyLinked() {
        return fullyLinked.get();
    }

    public void setFullyLinked(boolean value) {
        fullyLinked.set(value);
    }

    public int getTopLevel() {
        return topLevel;
    }

    public int getKey() {
        return keyValuePair.getKey();
    }

    public String getValue() {
        return keyValuePair.getValue();
    }

    public Node getNext(int level) {
        if (level >= 0 && level < next.size()) {
            return next.get(level);
        }
        return null;
    }

    public void setNext(int level, Node node) {
        next.set(level, node);
    }
}

