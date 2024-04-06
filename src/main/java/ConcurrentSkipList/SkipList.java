package ConcurrentSkipList;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a concurrent skip list. A skip list is a probabilistic data structure that
 * allows for average time complexity of O(log n) for search, insertion, and deletion operations.
 * This implementation is thread-safe, using fine-grained locking to manage concurrent access.
 * The structure dynamically adjusts its height and node levels to maintain efficient operations
 * as elements are added or removed.
 */
public class SkipList {

    /**
     * Head node
     */
    private final Node head;

    /**
     * Tail node
     */
    private final Node tail;
    private static final float PROBABILITY = 0.5f;
    private final int maxLevel;

    /**
     * Initializes the skip list with a specified maximum number of elements, which determines
     * the maximum level of the list for efficient operation based on the logarithm of the maximum
     * elements. It prepares the list with initial head and tail nodes spanning all levels to
     * facilitate the insertion and deletion processes.
     *
     * @param maxElements the maximum number of elements the skip list is expected to hold
     */
    public SkipList(int maxElements) {
        maxLevel = (int) (Math.log(maxElements) / Math.log(1 / PROBABILITY));
        head = new Node("", maxLevel);
        tail = new Node("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", maxLevel);
        for (int i = 0; i <= maxLevel; i++) {
            head.setNext(i, tail);
        }
    }

    /**
     * Searches for the position of a key within the skip list, updating the provided lists of
     * predecessors and successors at each level. This method is primarily used by add, remove,
     * and search operations to locate a key while keeping track of its potential neighbors for
     * efficient modification of the list structure.
     *
     * @param key          the key to search for in the skip list
     * @param predecessors a list to be populated with the preceding nodes at each level for the key
     * @param successors   a list to be populated with the succeeding nodes at each level for the key
     * @return the highest level at which the key was found, or -1 if not found
     */
    public int find(String key, List<Node> predecessors, List<Node> successors) {
        int found = -1;
        Node prev = head;

        for (int level = maxLevel; level >= 0; level--) {
            Node curr = prev.getNext(level);

            while (key.compareTo(curr.value) > 0) {
                prev = curr;
                curr = prev.getNext(level);
            }

            if (found == -1 && key.equals(curr.value)) {
                found = level;
            }

            predecessors.set(level, prev);
            successors.set(level, curr);
        }

        return found;
    }

    /**
     * Generates a random level for a new node being inserted into the skip list. The level is
     * determined probabilistically to maintain the balance and efficiency of the skip list's
     * layered structure. This method uses a geometric distribution with a predefined probability
     * to ensure the logarithmic complexity of operations.
     *
     * @return a randomly determined level for a new node
     */
    public int getRandomLevel() {
        int level = 0;
        while (ThreadLocalRandom.current().nextFloat() < 0.5) {
            level++;
        }
        return Math.min(level, maxLevel);
    }

    /**
     * Attempts to add a key to the skip list. If the key already exists, the method fails and
     * returns false. Otherwise, it inserts the key at a probabilistically determined level,
     * maintaining the skip list's structure and ensuring efficient future operations.
     *
     * @param key the key to add to the skip list
     * @return true if the key was successfully added, false if the key already exists
     */
    public boolean add(String key) {
        int topLevel = getRandomLevel();

        List<Node> preds = new ArrayList<>(Collections.nCopies(maxLevel + 1, null));
        List<Node> succs = new ArrayList<>(Collections.nCopies(maxLevel + 1, null));

        while (true) {
            int found = find(key, preds, succs);
            if (found != -1) {
                Node nodeFound = succs.get(found);
                if (!nodeFound.isMarked()) {
                    while (!nodeFound.isFullyLinked()) {
                        // Busy-wait
                    }
                    return false;
                }
                continue;
            }

            HashMap<Node, ReentrantLock> lockedNodes = new HashMap<>();
            boolean valid = true;

            try {
                Node pred;
                Node succ;
                for (int level = 0; valid && (level <= topLevel); level++) {
                    pred = preds.get(level);
                    succ = succs.get(level);
                    if (!lockedNodes.containsKey(pred)) {
                        pred.lock();
                        lockedNodes.put(pred, pred.getNodeLock());
                    }
                    valid = !pred.isMarked() && pred.getNext(level) == succ;
                }

                if (!valid) {
                    lockedNodes.values().forEach(ReentrantLock::unlock);
                    continue;
                }

                Node newNode = new Node(key, topLevel);
                for (int level = 0; level <= topLevel; level++) {
                    newNode.setNext(level, succs.get(level));
                }
                for (int level = 0; level <= topLevel; level++) {
                    preds.get(level).setNext(level, newNode);
                }
                newNode.setFullyLinked(true);
                lockedNodes.values().forEach(ReentrantLock::unlock);
                return true;
            } catch (Exception e) {
                // Log the exception
                e.printStackTrace();
                lockedNodes.values().forEach(ReentrantLock::unlock);
            }
        }
    }

    /**
     * Searches for a key in the skip list. The method traverses the levels of the list from top
     * to bottom, narrowing down the search range at each step to efficiently locate the key, if present.
     *
     * @param key the key to search for in the skip list
     * @return true if the key is found, false otherwise
     */
    public boolean search(String key) {
        List<Node> preds = new ArrayList<>(maxLevel + 1);
        List<Node> succs = new ArrayList<>(maxLevel + 1);

        for (int i = 0; i <= maxLevel; i++) {
            preds.add(null);
            succs.add(null);
        }

        int found = find(key, preds, succs);

        if (found == -1) {
            return false;
        }

        Node curr = head;
        for (int level = maxLevel; level >= 0; level--) {
            while (curr.getNext(level) != null && key.compareTo(curr.getNext(level).value) > 0) {
                curr = curr.getNext(level);
            }
        }

        curr = curr.getNext(0);

        if (curr != null && curr.value.equals(key) && succs.get(found).isFullyLinked() && !succs.get(found).isMarked()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes a key from the skip list. The method first locates the key using the find method
     * to get its predecessors and successors, then adjusts the pointers to exclude the key from
     * the list. If the key is not found, the method returns false, indicating failure to remove.
     *
     * @param key the key to remove from the skip list
     * @return true if the key was successfully removed, false if the key was not found
     */
    public boolean remove(String key) {
        Node victim = null;
        boolean isMarked = false;
        int topLevel = -1;
        List<Node> preds = new ArrayList<>(maxLevel + 1);
        List<Node> succs = new ArrayList<>(maxLevel + 1);

        for (int i = 0; i <= maxLevel; i++) {
            preds.add(null);
            succs.add(null);
        }

        while (true) {
            int found = find(key, preds, succs);
            if (found != -1) {
                victim = succs.get(found);
            }

            if (isMarked || (found != -1 && victim.isFullyLinked() && victim.getTopLevel() == found && !victim.isMarked())) {
                if (!isMarked) {
                    topLevel = victim.getTopLevel();
                    victim.lock();
                    if (victim.isMarked()) {
                        victim.unlock();
                        return false;
                    }
                    victim.setMarked(true);
                    isMarked = true;
                }

                HashMap<Node, Boolean> lockedNodes = new HashMap<>();
                try {
                    Node pred;
                    boolean valid = true;

                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds.get(level);
                        if (!lockedNodes.containsKey(pred)) {
                            pred.lock();
                            lockedNodes.put(pred, true);
                        }
                        valid = !pred.isMarked() && pred.getNext(level) == victim;
                    }

                    if (!valid) {
                        for (Node lockedNode : lockedNodes.keySet()) {
                            lockedNode.unlock();
                        }
                        continue;
                    }

                    for (int level = topLevel; level >= 0; level--) {
                        preds.get(level).setNext(level, victim.getNext(level));
                    }
                    victim.unlock();

                    for (Node lockedNode : lockedNodes.keySet()) {
                        lockedNode.unlock();
                    }
                    return true;
                } catch (Exception e) {
                    for (Node lockedNode : lockedNodes.keySet()) {
                        lockedNode.unlock();
                    }

                    throw new RuntimeException(e);
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Get all the nodes within range
     *
     * @param startKey start key
     * @param endKey   end key
     * @return lists of value of elements
     */
    public Map<String, String> range(String startKey, String endKey) {
        Map<String, String> rangeOutput = new HashMap<>();

        if (startKey.compareTo(endKey) > 0) {
            return rangeOutput;
        }

        Node curr = head;

        // Traverse down the levels of the skip list to get close to the startKey
        for (int level = maxLevel; level >= 0; level--) {
            while (curr.getNext(level) != null && startKey.compareTo(curr.getNext(level).value) > 0) {
                curr = curr.getNext(level);
            }
        }

        // Traverse at the bottom level to collect all nodes within the range [startKey, endKey]
        curr = curr.getNext(0); // Move to the first node that might be in the range.
        while (curr != null && endKey.compareTo(curr.value) >= 0) {
            if (curr.value.compareTo(startKey) >= 0 && curr.value.compareTo(endKey) <= 0) {
                rangeOutput.put(curr.value, curr.value);
            }
            curr = curr.getNext(0); // Move to the next node at the bottom level
        }

        return rangeOutput;
    }

}
