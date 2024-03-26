package BloomFilter.decompose;

import java.nio.charset.Charset;

/**
 * The default implementation of {@link Decomposer} that decomposes the object
 * by converting it to a {@link String} object using the
 * {@link Object#toString()} method.
 * To convert the {@link String} thus obtained into bytes, the default platform
 * {@link Charset} encoding is used.
 */
public class DefaultDecomposer<Object> {
	
	/**
	 * The default platform encoding
	 */
	private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

	/**
	 * Decompose the object
	 */
	public void decompose(Object object, ByteSink sink) {
		if(object == null) {
			return;
		}
		
		if(object instanceof String) {
			sink.putBytes(((String) object).getBytes(DEFAULT_CHARSET));
			return;
		}
		
		sink.putBytes(object.toString().getBytes(DEFAULT_CHARSET));
	}

}