package org.curljni;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class WriteStream implements IResource {

    private final long ptr;
    private final OutputStream out;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    private WriteStream(long ptr, OutputStream out, boolean isClosed) {
        this.ptr = ptr;
        this.out = out;
        this.isClosed = isClosed;
    }

    public static WriteStream create(OutputStream out) {
        long ptr = _allocate(out);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate write stream");
        }
        return new WriteStream(ptr, out, false);
    }

    private static native long _allocate(OutputStream out);
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
            out.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (code != 0) {
            throw Err.error("failed to close write stream, code: %s", code);
        }
    }
}
