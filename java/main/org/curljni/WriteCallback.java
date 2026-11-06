package org.curljni;

import java.nio.ByteBuffer;

public class WriteCallback implements IResource {

    private long ptr;
    private boolean isClosed;
    private ByteBuffer bb;
    private int bbSize;
    private IHandler handler;

    static {
        Native.loadLib();
    }

    @FunctionalInterface
    public interface IHandler {
        void handle(ByteBuffer bb);
    }

    private WriteCallback() {}

    private void callback(int size) {
        bb.clear();
        bb.limit(size);
        handler.handle(bb);
    }

    @SuppressWarnings("unused")
    public static WriteCallback create(IHandler handler) {
        int bbSize = 1024 * 16;
        ByteBuffer bb = ByteBuffer.allocateDirect(bbSize);

        WriteCallback wc = new WriteCallback();

        long ptr = wc._allocate(bb, bbSize);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate WriteCallback");
        }

        wc.ptr = ptr;
        wc.bb = bb;
        wc.bbSize = bbSize;
        wc.handler = handler;
        wc.isClosed = false;
        return wc;
    }

    public static WriteCallback maybeCreate(IHandler handler) {
        if (handler == null) {
            return null;
        } else {
            return create(handler);
        }
    }

    private native long _allocate(ByteBuffer bb, int bbSize);
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
