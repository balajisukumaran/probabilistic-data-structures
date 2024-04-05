package CuckooFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Bucket {
    private static final int DEFAULT_BUCKET_SIZE = 4;
    private final List<String> bucket;
    private final ReentrantLock lock = new ReentrantLock();
    private final int capacity = DEFAULT_BUCKET_SIZE;

    public Bucket() {
        this.bucket = new ArrayList<>(capacity);
    }

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

    public boolean delete(String fingerprint) {
        lock.lock();
        try {
            return bucket.remove(fingerprint);
        } finally {
            lock.unlock();
        }
    }

    public int getFingerprintIndex(String fingerprint) {
        return bucket.indexOf(fingerprint);
    }

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

    public boolean contains(String fingerprint) {
        return getFingerprintIndex(fingerprint) > -1;
    }
}