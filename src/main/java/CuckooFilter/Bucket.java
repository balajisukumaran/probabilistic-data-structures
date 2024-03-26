package CuckooFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Bucket {
    private static final int DEFAULT_BUCKET_SIZE = 4;
    private final List<Byte> bucket;
    private final ReentrantLock lock = new ReentrantLock();
    private final int capacity = DEFAULT_BUCKET_SIZE;

    public Bucket() {
        this.bucket = new ArrayList<>(capacity);
    }

    public boolean insert(byte[] fingerprint) {
        lock.lock();
        try {
            if (bucket.size() >= capacity) {
                return false;
            }
            for (byte b : fingerprint) {
                bucket.add(b);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public boolean delete(byte[] fingerprint) {
        lock.lock();
        try {
            return bucket.remove((Byte) fingerprint[0]); // Simplified for single byte fingerprint
        } finally {
            lock.unlock();
        }
    }

    public int getFingerprintIndex(byte[] fingerprint) {
        return bucket.indexOf(fingerprint[0]); // Simplified for single byte fingerprint
    }

    public byte[] swap(byte[] fingerprint) {
        lock.lock();
        try {
            Random rand = new Random();
            int index = rand.nextInt(bucket.size());
            byte temp = bucket.get(index);
            bucket.set(index, fingerprint[0]); // Simplified for single byte fingerprint
            return new byte[]{temp};
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(byte[] fingerprint) {
        return getFingerprintIndex(fingerprint) > -1;
    }
}