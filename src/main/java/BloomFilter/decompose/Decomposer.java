package BloomFilter.decompose;

/**
 * Contract for implementations that wish to help in decomposing
 * an object to a byte-array so that various hashes can be computed
 * over the same.
 *
 * @param <T> the type of object over which this decomposer works
 */
public interface Decomposer<T> {

    /**
     * Decompose the object into the given {@link ByteSink}
     *
     * @param object the object to be decomposed
     * @param sink   the sink to which the object is decomposed
     */
    public void decompose(T object, ByteSink sink);
}