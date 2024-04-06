package BloomFilter;

import java.util.zip.CRC32;

/**
 * A CRC32 hash function.
 */
public class HashFunction {
    public boolean isSingleValued() {
        return true;
    }

    public long hash(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }

    public long[] hashMultiple(byte[] bytes) {
        return null;
    }

}