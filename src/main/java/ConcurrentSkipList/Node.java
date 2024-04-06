package ConcurrentSkipList;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a node in a concurrent skip list implementation. Each node holds a value and
 * references to its successors in the skip list. This class is designed to be thread-safe,
 * allowing concurrent modifications through the use of a ReentrantLock. It supports efficient
 * navigation and modification in a skip list data structure, catering to a concurrent environment.
 */
public class Node {

    /**
     * The value stored in this node. It's public and final, indicating that once a node is
     * created, its value cannot be changed, though its position in the skip list can be.
     */
    public final String value;

    /**
     * List of pointers to the next node(s) in the skip list, supporting the layered structure
     * of the skip list and allowing for efficient traversal and insertion operations.
     */
    private final List<Node> next = new ArrayList<>();

    /**
     * A lock to ensure thread-safe operations on this node, crucial for the concurrent
     * behavior of the skip list. It prevents simultaneous modifications that could lead
     * to inconsistent states.
     */
    private final ReentrantLock nodeLock = new ReentrantLock();
    private final AtomicBoolean marked = new AtomicBoolean(false);
    private final AtomicBoolean fullyLinked = new AtomicBoolean(false);
    private final int topLevel;

    public Node(String value, int level) {
        this.value = value;
        for (int i = 0; i <= level; i++) {
            next.add(null);
        }
        this.topLevel = level;
    }

    /**
     * Locks this node to prevent concurrent modifications, ensuring thread-safe operations
     * on its contents and links. This method should be used before performing any operation
     * that modifies the node or its connections.
     */
    public void lock() {
        nodeLock.lock();
    }

    /**
     * Retrieves the ReentrantLock object associated with this node. This allows for custom
     * lock management strategies outside of the node's predefined methods.
     *
     * @return the ReentrantLock for this node
     */
    public ReentrantLock getNodeLock() {
        return nodeLock;
    }

    /**
     * Unlocks this node, allowing other threads to modify it. This method should be called
     * after completing an operation that required exclusive access to the node.
     */
    public void unlock() {
        nodeLock.unlock();
    }

    /**
     * Checks if this node is marked for removal. A marked node is typically not considered
     * part of the skip list for traversal or search operations.
     *
     * @return true if the node is marked for removal, false otherwise
     */
    public boolean isMarked() {
        return marked.get();
    }

    /**
     * Sets the marked status of this node. Marking a node is usually a preliminary step
     * to removing it from the skip list, indicating it should no longer be traversed.
     *
     * @param value true to mark the node for removal, false otherwise
     */
    public void setMarked(boolean value) {
        marked.set(value);
    }

    /**
     * Checks if the node is fully linked into the skip list, meaning all of its forward
     * pointers are set and it is considered part of the list for all operations.
     *
     * @return true if the node is fully integrated into the skip list, false otherwise
     */
    public boolean isFullyLinked() {
        return fullyLinked.get();
    }

    /**
     * Sets the fully linked status of this node, indicating whether it is fully integrated
     * into the skip list structure and can be considered in traversal and search operations.
     *
     * @param value true to mark the node as fully linked, false otherwise
     */
    public void setFullyLinked(boolean value) {
        fullyLinked.set(value);
    }

    /**
     * Retrieves the top level that this node resides on within the skip list. The top level
     * indicates the highest layer of forward pointers originating from this node.
     *
     * @return the top level of this node in the skip list
     */
    public int getTopLevel() {
        return topLevel;
    }

    /**
     * Retrieves the next node at the specified level. This is used to traverse the skip list
     * at different levels for efficient search and insertion operations.
     *
     * @param level the level to retrieve the next node from
     * @return the next node at the specified level, or null if no such node exists
     */
    public Node getNext(int level) {
        if (level >= 0 && level < next.size()) {
            return next.get(level);
        }
        return null;
    }

    /**
     * Sets the next node at the specified level. This is part of the mechanism to build and
     * maintain the layered structure of the skip list.
     *
     * @param level the level to set the next node on
     * @param node  the node to set as next at the specified level
     */
    public void setNext(int level, Node node) {
        next.set(level, node);
    }
}

