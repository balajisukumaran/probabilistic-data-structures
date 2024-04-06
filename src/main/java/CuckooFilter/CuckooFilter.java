package CuckooFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements a Cuckoo Filter, a space-efficient probabilistic data structure that is used
 * to test whether an element is a member of a set. False positive matches are possible, but
 * false negatives are not. It provides high insertion and query performances with a low
 * rate of false positives. This class supports operations for adding, deleting, and checking
 * the presence of elements in the filter.
 */
public class CuckooFilter {
    private Bucket[] buckets;
    private int fingerprintSize;
    private long count;
    private int capacity;
    private ReentrantLock lock = new ReentrantLock();
    private static final int DEFAULT_BUCKET_SIZE = 4;
    private static final int MAX_CUCKOO_COUNT = 500;
    private final Random random = new Random();
    private MessageDigest messageDigest;

    public CuckooFilter(int capacity, int fingerprintSize) throws NoSuchAlgorithmException {
        this.capacity = getNextPow2(capacity) / DEFAULT_BUCKET_SIZE;
        this.fingerprintSize = fingerprintSize;
        this.buckets = new Bucket[this.capacity];
        this.count = 0;

        for (int i = 0; i < this.capacity; i++) {
            this.buckets[i] = new Bucket();
        }
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    /**
     * Inserts an item into the cuckoo filter. This operation might trigger a series of
     * rehashing and relocations within the filter's buckets if the initial locations are occupied.
     *
     * @param item the item to insert into the filter
     * @return true if the item was successfully inserted, false if the filter cannot accommodate
     *         the item without exceeding the maximum load factor
     */
    public boolean insert(String item) {
        String fp = getFingerprint(item);
        int i1 = getIndex1(item);
        int i2 = getIndex2(fp, i1);

        lock.lock();
        try {
            if (_insert(fp, i1) || _insert(fp, i2)) {
                return true;
            }

            // Reinsertion, kick out some fingerprints
            int i = random.nextBoolean() ? i1 : i2;
            for (int n = 0; n < MAX_CUCKOO_COUNT; n++) {
                fp = buckets[i].swap(fp);  // swap method should return the fingerprint
                i = getAlternateIndex(fp, i);
                if (_insert(fp, i)) {
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private boolean _insert(String fp, int index) {
        if (buckets[index].insert(fp)) {
            count++;
            return true;
        }
        return false;
    }

    private static int getNextPow2(int n) {
        return Integer.highestOneBit(n) << 1;
    }

    private byte[] getHash(byte[] data) {
        messageDigest.update(data);
        return messageDigest.digest();
    }

    private String getFingerprint(String data) {
        String hash = new String(getHash(data.getBytes(StandardCharsets.UTF_8)));
        // Ensure the fingerprint size does not exceed the hash length
        int size = Math.min(fingerprintSize, hash.length());
        return hash.substring(0, size);
    }

    public static byte[] objectToBytes(Object obj) throws IOException {
        if (!(obj instanceof Serializable)) {
            throw new IllegalArgumentException("The provided object does not implement Serializable");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        }
    }
    private int getIndex1(String data) {
        return Math.abs(data.hashCode()) % capacity;
    }

    private int getIndex2(String fingerprint, int index1) {
        return Math.abs(index1 ^ fingerprint.hashCode()) % capacity;
    }
    private int getIndex1(byte[] data) {
        return Math.abs(fromBytes(getHash(data))) % capacity;
    }

    private int getIndex2(byte[] fingerprint, int index1) {
        return Math.abs(index1 ^ fromBytes(getHash(fingerprint))) % capacity;
    }

    private int getAlternateIndex(String fingerprint, int index) {
        return getIndex2(fingerprint, index);
    }

    /**
     * Attempts to remove an item from the cuckoo filter. If the item is found and removed,
     * the method returns true; otherwise, it returns false.
     *
     * @param item the item to remove from the filter
     * @return true if the item was found and removed, false otherwise
     */
    public boolean delete(String item) {
        String fingerprint = getFingerprint(item);
        int index1 = getIndex1(item);
        int index2 = getAlternateIndex(fingerprint, index1);

        lock.lock();
        try {
            if (buckets[index1].delete(fingerprint) || buckets[index2].delete(fingerprint)) {
                count--;
                return true;
            }
        } finally {
            lock.unlock();
        }

        return false;
    }


    private static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Serialization error", e);
        }
    }

    private static int fromBytes(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < Integer.BYTES; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }

    /**
     * Checks whether an item is present in the cuckoo filter. Note that due to the probabilistic
     * nature of the filter, this method may return true for items that have not been inserted,
     * representing a false positive.
     *
     * @param item the item to check for its presence in the filter
     * @return true if the item is possibly in the filter, false if the item is definitely not in the filter
     */

    public boolean contains(String item) {
        String fingerprint = getFingerprint(item);
        int index1 = getIndex1(item);
        int index2 = getAlternateIndex(fingerprint, index1);

        Bucket bucket1 = this.buckets[index1];
        Bucket bucket2 = this.buckets[index2];

        return bucket1.contains(fingerprint) || bucket2.contains(fingerprint);
    }

    /**
     * Provides a string representation of the cuckoo filter, detailing its structure, contents,
     * and statistics such as current load. This can be useful for debugging or monitoring the
     * state of the filter.
     *
     * @return a string representation of the cuckoo filter
     */
    @Override
    public String toString() {
        return "CuckooFilter{" +
                "capacity=" + capacity +
                ", fingerprintSize=" + fingerprintSize + " byte(s)" +
                '}';
    }
}

