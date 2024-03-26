package BloomFilter;

import java.nio.charset.Charset;
import BloomFilter.core.BitArray;
import BloomFilter.core.JavaBitSetArray;
import BloomFilter.decompose.ByteSink;
import BloomFilter.decompose.Decomposable;
import BloomFilter.decompose.Decomposer;
import BloomFilter.decompose.DefaultDecomposer;

/**
 * An abstract implementation for the bloom filter.
 * The default composer is a simple {@link Object#toString()} decomposer which
 * then converts this {@link String} into raw bytes.
 * One may override the decomposer to be used, the hash function to be used
 * as well as the implementation of the {@link BitArray} that needs to be
 * used.
 * @param <T> the type of objects to be stored in the filter
 */
public class BloomFilter<T> {
	
	/**
	 * The decomposer to use when there is none specified at construction
	 */
	protected static final DefaultDecomposer<Object> DEFAULT_COMPOSER = new DefaultDecomposer();
	
	/**
	 * The default hasher to use if one is not specified
	 */
	protected static final HashFunction DEFAULT_HASHER = new HashFunction();
	
	/**
	 * Constant
	 */
	public static final double LOG_2 = Math.log(2);
	
	/**
	 * Constant
	 */
	public static final double LOG_2_SQUARE = LOG_2 * LOG_2;
	
	/**
	 * The default {@link Charset} is the platform encoding charset
	 */
	protected transient Charset currentCharset = Charset.defaultCharset();
	
	/**
	 * The {@link BitArray} instance that holds the entire data
	 */
	protected final BitArray bitArray;
	
	/**
	 * Number of hash functions needed
	 */
	protected final int kOrNumberOfHashFunctions;
	
	/**
	 * Holds the custom decomposer that should be used for this bloom filter
	 * 
	 */
	protected final Decomposer<T> customDecomposer;
	
	/**
	 * The hashing method to be used for hashing
	 */
	protected final HashFunction hasher;
	
	/**
	 * Number of bits required for the bloom filter
	 */
	protected final int numBitsRequired;

	/**
	 * Create a new bloom filter.
	 * 
	 * @param expectedInsertions
	 *            the number of max expected insertions
	 * 
	 * @param falsePositiveProbability
	 *            the max false positive probability rate that the bloom filter
	 *            can give
	 */
	public BloomFilter(int expectedInsertions, double falsePositiveProbability) {
		this(expectedInsertions, falsePositiveProbability, null, null);
	}

	/**
	 * Create a new bloom filter.
	 * 
	 * @param expectedInsertions
	 *            the number of max expected insertions
	 * 
	 * @param falsePositiveProbability
	 *            the max false positive probability rate that the bloom filter
	 *            can give
	 * 
	 * @param decomposer
	 *            a {@link Decomposer} that helps decompose the given object
	 * 
	 * @param hasher
	 *            the hash function to use. If <code>null</code> is specified
	 *            the {@link BloomFilter#DEFAULT_HASHER} will be used as
	 *            the hashing function
	 */
	protected BloomFilter(int expectedInsertions, double falsePositiveProbability, Decomposer<T> decomposer, HashFunction hasher) {
		this.numBitsRequired = optimalBitSizeOrM(expectedInsertions, falsePositiveProbability);
		this.kOrNumberOfHashFunctions = optimalNumberofHashFunctionsOrK(expectedInsertions, numBitsRequired);
		this.bitArray = createBitArray(numBitsRequired);
		
		this.customDecomposer = decomposer;
		
		if(hasher != null) {
			this.hasher = hasher;
		} else {
			this.hasher = DEFAULT_HASHER;
		}
	}

	/**
	 * Compute the optimal size <code>m</code> of the bloom filter in bits.
	 * 
	 * @param n
	 *            the number of expected insertions, or <code>n</code>
	 * 
	 * @param p
	 *            the maximum false positive rate expected, or <code>p</code>
	 * 
	 * @return the optimal size in bits for the filter, or <code>m</code>
	 */
	public static int optimalBitSizeOrM(final double n, final double p) {
		return (int) (-n * Math.log(p) / (LOG_2_SQUARE));
        // return (int) Math.ceil(-1 * n * Math.log(p) / LOG_2_SQUARE);
	}
	
	/**
	 * Compute the optimal number of hash functions, <code>k</code>
	 * 
	 * @param n
	 *            the number of expected insertions or <code>n</code>
	 * 
	 * @param m
	 *            the number of bits in the filter
	 * 
	 * @return the optimal number of hash functions to be used also known as
	 *         <code>k</code>
	 */
	public static int optimalNumberofHashFunctionsOrK(final long n, final long m) {
		return Math.max(1, (int) Math.round(m / n * Math.log(2)));
		// return Math.max(1, (int) Math.round(m / n * LOG_2));
	}

	/**
	 * Create a new {@link BitArray} instance for the given number of bits.
	 * 
	 * @param numBits
	 *            the number of required bits in the underlying array
	 * 
	 * @return the {@link BitArray} implementation to be used
	 */
	protected BitArray createBitArray(int numBits) {
		return new JavaBitSetArray(numBits);
	}

	/**
	 * Add the given byte array to the bloom filter
	 * 
	 * @param bytes
	 *            the byte array to be added to the bloom filter, cannot be null
	 * 
	 * @return <code>true</code> if the value was added to the bloom filter,
	 *         <code>false</code> otherwise
	 * 
	 * @throws IllegalArgumentException
	 *             if the byte array is <code>null</code>
	 */
	public final boolean add(byte[] bytes) {
		long hash64 = getLongHash64(bytes);
		
		// apply the less hashing technique
		int hash1 = (int) hash64;
		int hash2 = (int) (hash64 >>> 32);
		
		boolean bitsChanged = false;
		for (int i = 1; i <= this.kOrNumberOfHashFunctions; i++) {
			int nextHash = hash1 + i * hash2;
			if (nextHash < 0) {
				nextHash = ~nextHash;
			}
			bitsChanged |= this.bitArray.setBit(nextHash % this.bitArray.bitSize());
		}
		
		return bitsChanged;
	}
	
	/**
	 * Check if the given byte array item exists in the bloom filter
	 * 
	 * @param bytes
	 *            the byte array to be tested for existence in the bloom filter,
	 *            cannot be null
	 * 
	 * @return <code>true</code> if the value exists in the bloom filter,
	 *         <code>false</code> otherwise
	 * 
	 * @throws IllegalArgumentException
	 *             if the byte array is <code>null</code>
	 */
	public final boolean contains(byte[] bytes) {
		long hash64 = getLongHash64(bytes);
		
		int hash1 = (int) hash64;
		int hash2 = (int) (hash64 >>> 32);
		for (int i = 1; i <= this.kOrNumberOfHashFunctions; i++) {
			int nextHash = hash1 + i * hash2;
			if (nextHash < 0) {
				nextHash = ~nextHash;
			}
			if (!this.bitArray.getBit(nextHash % this.bitArray.bitSize())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compute one 64-bit hash from the given byte-array using the specified
	 * {@link HashFunction}.
	 * 
	 * @param bytes
	 *            the byte-array to use for hash computation
	 * 
	 * @return the 64-bit hash
	 */
	protected long getLongHash64(byte[] bytes) {
		if(bytes == null) {
			throw new IllegalArgumentException("Bytes to add to bloom filter cannot be null");
		}
		
		if(this.hasher.isSingleValued()) {
			return this.hasher.hash(bytes);
		}
		
		return this.hasher.hashMultiple(bytes)[0];
	}
	
	/**
	 * Given the value object, decompose it into a byte-array so that hashing
	 * can be done over the returned bytes. If a custom {@link Decomposer} has
	 * been specified, it will be used, otherwise the {@link DefaultDecomposer}
	 * will be used.
	 * 
	 * @param value
	 *            the value to be decomposed
	 * 
	 * @return the decomposed byte array
	 */
	protected byte[] decomposedValue(T value) {
		ByteSink sink = new ByteSink();
		
		if(value instanceof Decomposable) {
			((Decomposable) value).decompose(sink);
			
			return sink.getByteArray();
		}
		
		if(this.customDecomposer != null) {
			this.customDecomposer.decompose(value, sink);
			return sink.getByteArray();
		}
		
		DEFAULT_COMPOSER.decompose(value, sink);
		return sink.getByteArray();
	}

	/**
	 * Add the given value to the bloom filter.
	 * 
	 * @param value
	 *            the value to be added
	 * 
	 * @return <code>true</code> if the value was added to the bloom filter,
	 *         <code>false</code> otherwise
	 */
	public boolean add(T value) {
		if(value == null) {
			return false;
		}
		
		return add(decomposedValue(value));
	}
	
	/**
	 * Check if the given value exists in the bloom filter. Note that this
	 * method may return <code>true</code>, indicating a false positive - but
	 * this is the property of the bloom filter and is not a bug.
	 * 
	 * @return <code>false</code> if the value is definitely (100% surety) not
	 *         contained in the bloom filter, <code>true</code> otherwise.
	 */
	public boolean contains(T value) {
		if(value == null) {
			return false;
		}
		
		return contains(value.toString().getBytes(this.currentCharset));
	}
}