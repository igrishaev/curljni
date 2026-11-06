package org.curljni;

import java.nio.ByteBuffer;

public class WriteCallback implements IResource {

    private long ptr;
    private boolean isClosed;
    private final ByteBuffer bb;
    private final int bbSize;

    static {
        Native.loadLib();
    }

    @FunctionalInterface
    public interface IHandler {
        void handle(ByteBuffer bb);
    }

    private WriteCallback(IHandler handler) {
        this.hashCode()
        this.ptr = ptr;
        this.isClosed = isClosed;
    }

    private void callback(long size) {

    }

    @SuppressWarnings("unused")
    public static WriteCallback create(IHandler handler) {
        WriteCallback wc = new WriteCallback();
        long ptr = wc._allocate();
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate WriteCallback");
        }
        return wc;
    }

    public static WriteCallback maybeCreate(IHandler handler) {
        if (handler == null) {
            return null;
        } else {
            return create(handler);
        }
    }

    private native long _allocate(ByteBuffer bb, int bbSize );
    private static native long _free(long ptr);

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        if (isClosed) return;
        long code = _free(ptr);
        isClosed = true;
        if (code != 0) {
            throw Err.error("failed to close WriteCallback, code: %s", code);
        }
    }
}
