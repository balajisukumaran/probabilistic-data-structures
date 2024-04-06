package CuckooFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a bucket in a Cuckoo Filter. A bucket is used to store a limited number of fingerprints,
 * with mechanisms to handle collisions through cuckoo hashing. This class provides methods to add,
 * delete, and check for fingerprints within the bucket, alongside a method to swap fingerprints,
 * facilitating the cuckoo hashing process.
 */
public class Bucket {
    private static final int DEFAULT_BUCKET_SIZE = 4;
    private final List<String> bucket;
    private final ReentrantLock lock = new ReentrantLock();
    private final int capacity = DEFAULT_BUCKET_SIZE;

    public Bucket() {
        this.bucket = new ArrayList<>(capacity);
    }

    /**
     * Attempts to insert a given fingerprint into the bucket. If the bucket has reached its
     * capacity, the insert operation will fail, and the method will return false.
     *
     * @param fingerprint the fingerprint to insert into the bucket
     * @return true if the insertion is successful, false if the bucket is full
     */
    public boolean insert(String fingerprint) {
        lock.lock();
        try {
            if (bucket.size() >= capacity) {
                return false;
            }
            bucket.add(fingerprint);
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * Attempts to delete a specified fingerprint from the bucket. If the fingerprint is found and
     * removed successfully, the method returns true; otherwise, it returns false.
     *
     * @param fingerprint the fingerprint to delete from the bucket
     * @return true if the fingerprint was found and deleted, false otherwise
     */
    public boolean delete(String fingerprint) {
        lock.lock();
        try {
            return bucket.remove(fingerprint);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the index of a specified fingerprint within the bucket. If the fingerprint is not
     * present in the bucket, the method returns -1.
     *
     * @param fingerprint the fingerprint to find within the bucket
     * @return the index of the fingerprint if found, -1 if not found
     */
    public int getFingerprintIndex(String fingerprint) {
        return bucket.indexOf(fingerprint);
    }

    /**
     * Swaps a given fingerprint with another fingerprint already in the bucket. The method is
     * designed to support the cuckoo hashing process by moving fingerprints to resolve collisions.
     *
     * @param fingerprint the fingerprint to swap into the bucket
     * @return the fingerprint that was replaced in the swap process
     */
    public String swap(String fingerprint) {
        lock.lock();
        try {
            Random rand = new Random();
            int index = rand.nextInt(bucket.size());
            String temp = bucket.get(index);
            bucket.set(index, fingerprint);
            return temp;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if a specified fingerprint is present within the bucket. This method is useful for
     * verifying the existence of an element without modifying the bucket's content.
     *
     * @param fingerprint the fingerprint to check for
     * @return true if the fingerprint is present, false otherwise
     */
    public boolean contains(String fingerprint) {
        return getFingerprintIndex(fingerprint) > -1;
    }
}