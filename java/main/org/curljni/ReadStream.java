package org.curljni;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class ReadStream implements IResource {

    private final long ptr;
    private final InputStream in;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    private ReadStream(long ptr, InputStream in, boolean isClosed) {
        this.ptr = ptr;
        this.in = in;
        this.isClosed = isClosed;
    }

    @SuppressWarnings("unused")
    public static ReadStream create(InputStream in) {
        long ptr = _allocate(in);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate read stream");
        }
        return new ReadStream(ptr, in, false);
    }

    private static native long _allocate(InputStream in);
    private static native long _free(long ptr);

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public void close() {
        if (isClosed) return;
        final long code = _free(ptr);
        isClosed = true;
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (code != 0) {
            throw Err.error("failed to close read stream, code: %s", code);
        }
    }
}
