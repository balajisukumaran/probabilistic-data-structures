package BloomFilter.core;

import java.io.Closeable;

public interface BitArray extends Closeable {

    /**
     * Get the bit at index
     *
     * @param index the index of the bit in the array
     * @return <code>true<code> if the but is set, <code>false</code> otherwise
     */
    public boolean getBit(int index);

    /**
     * Set the bit at index
     *
     * @param index the index of the bit in the array
     * @return <code>true</code> if the bit was updated, <code>false</code>
     * otherwise.
     */
    public boolean setBit(int index);

    /**
     * The space used by this {@link BitArray} in number of bytes.
     *
     * @return the number of bytes being used
     */
    public int bitSize();
}