package ConcurrentSkipList;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class SkipList {
    private final Node head;
    private final Node tail;
    private static final float PROBABILITY = 0.5f;
    private final int maxLevel;

    public SkipList(int maxElements) {
        maxLevel = (int) (Math.log(maxElements) / Math.log(1 / PROBABILITY));
        head = new Node(Integer.MIN_VALUE, "", maxLevel);
        tail = new Node(Integer.MAX_VALUE, "", maxLevel);
        for (int i = 0; i <= maxLevel; i++) {
            head.setNext(i, tail);
        }
    }

    public int find(int key, List<Node> predecessors, List<Node> successors) {
        int found = -1;
        Node prev = head;

        for (int level = maxLevel; level >= 0; level--) {
            Node curr = prev.getNext(level);

            while (key > curr.getKey()) {
                prev = curr;
                curr = prev.getNext(level);
            }

            if (found == -1 && key == curr.getKey()) {
                found = level;
            }

            predecessors.set(level, prev);
            successors.set(level, curr);
        }

        return found;
    }

    public int getRandomLevel() {
        int level = 0;
        while (ThreadLocalRandom.current().nextFloat() < 0.5) {
            level++;
        }
        return Math.min(level, maxLevel);
    }

    public boolean add(int key, String value) {
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

                Node newNode = new Node(key, value, topLevel);
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

    public String search(int key) {
        List<Node> preds = new ArrayList<>(maxLevel + 1);
        List<Node> succs = new ArrayList<>(maxLevel + 1);

        for (int i = 0; i <= maxLevel; i++) {
            preds.add(null);
            succs.add(null);
        }

        int found = find(key, preds, succs);

        if (found == -1) {
            return "";
        }

        Node curr = head;
        for (int level = maxLevel; level >= 0; level--) {
            while (curr.getNext(level) != null && key > curr.getNext(level).getKey()) {
                curr = curr.getNext(level);
            }
        }

        curr = curr.getNext(0);

        if (curr != null && curr.getKey() == key && succs.get(found).isFullyLinked() && !succs.get(found).isMarked()) {
            return curr.getValue();
        } else {
            return "";
        }
    }

    public boolean remove(int key) {
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

    public Map<Integer, String> range(int startKey, int endKey) {
        Map<Integer, String> rangeOutput = new HashMap<>();

        if (startKey > endKey) {
            return rangeOutput;
        }

        Node curr = head;

        // Traverse down the levels of the skip list to get close to the startKey
        for (int level = maxLevel; level >= 0; level--) {
            while (curr.getNext(level) != null && startKey > curr.getNext(level).getKey()) {
                curr = curr.getNext(level);
            }
        }

        // Traverse at the bottom level to collect all nodes within the range [startKey, endKey]
        curr = curr.getNext(0); // Move to the first node that might be in the range.
        while (curr != null && endKey >= curr.getKey()) {
            if (curr.getKey() >= startKey && curr.getKey() <= endKey) {
                rangeOutput.put(curr.getKey(), curr.getValue());
            }
            curr = curr.getNext(0); // Move to the next node at the bottom level
        }

        return rangeOutput;
    }

    public void display() {
        for (int i = 0; i <= maxLevel; i++) {
            Node temp = head;
            int count = 0;

            if (!(temp.getKey() == Integer.MIN_VALUE && temp.getNext(i).getKey() == Integer.MAX_VALUE)) {
                System.out.printf("Level %d: ", i);
                while (temp != null) {
                    System.out.printf("%d -> ", temp.getKey());
                    temp = temp.getNext(i);
                    count++;
                }
                System.out.println();
            }
            if (count == 3) break;
        }
        System.out.println("--------------------------------------------------------\n");
    }
}
