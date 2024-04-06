package BloomFilter.decompose;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An in-memory sink that uses a {@link ByteArrayOutputStream} to store
 * the incoming bytes.
 */
public class ByteSink {

    /**
     * The actual storage stream
     */
    protected ByteArrayOutputStream stream = new ByteArrayOutputStream();

    /**
     * Wrapper over the byte stream
     */
    protected DataOutputStream dataStream = new DataOutputStream(stream);

    /**
     * Get the byte-array of bytes currently stored
     *
     * @return
     */
    public byte[] getByteArray() {
        return stream.toByteArray();
    }

    /**
     * Store the given bytes in this sink.
     *
     * @param bytes
     * @return
     */
    public ByteSink putBytes(byte[] bytes) {
        try {
            stream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Unable to store bytes inside the sink", e);
        }

        return this;
    }
}