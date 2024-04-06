package BloomFilter.core;

import java.io.IOException;
import java.util.BitSet;

/**
 * A {@link BitArray} implementation that uses the standard Java {@link BitSet}
 * as the underlying implementation.
 **/
public class JavaBitSetArray implements BitArray {

    final BitSet bitSet;

    final int size;

    public JavaBitSetArray(int numBits) {
        this.bitSet = new BitSet(numBits);
        this.size = this.bitSet.size();
    }

    @Override
    public boolean getBit(int index) {
        return this.bitSet.get(index);
    }

    @Override
    public boolean setBit(int index) {
        this.bitSet.set(index);
        return true;
    }

    @Override
    public int bitSize() {
        return this.size;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

}