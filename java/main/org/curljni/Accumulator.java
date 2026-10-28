package org.curljni;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Accumulator implements IResource {

    private final long ptr;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    private Accumulator(long ptr, boolean isClosed) {
        this.ptr = ptr;
        this.isClosed = isClosed;
    }

    @SuppressWarnings("unused")
    public static Accumulator create() {
        return create(Const.ACCUM_SIZE);
    }

    public static Accumulator create(long initSize) {
        long ptr = _allocate(initSize);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate accumulator");
        }
        return new Accumulator(ptr, false);
    }

    @Override
    public String toString() {
        return String.format("<Accumulator %s>", ptr);
    }

    public byte[] getBytes() {
        return _get_bytes(ptr);
    }

    public String getString() {
        return getString(StandardCharsets.UTF_8);
    }

    public String getString(final Charset charset) {
        return new String(getBytes(), charset);
    }

    @SuppressWarnings("unused")
    public InputStream getStream() {
        return new ByteArrayInputStream(getBytes());
    }

    private native static long _allocate(final long initSize);
    private native static long _free(final long ptr);
    private native static byte[] _get_bytes(final long ptr);

    @Override
    public void close() {
        if (isClosed) return;
        final long code = _free(ptr);
        isClosed = true;
        if (code != 0) {
            throw Err.error("failed to close Accumulator, code: %s", code);
        }
    }
}
