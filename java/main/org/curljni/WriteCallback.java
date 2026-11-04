package org.curljni;

public class WriteCallback implements IResource {

    private final long ptr;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    @FunctionalInterface
    public interface IHandler {
        void handle(byte[] buf, int off, int len);
    }

    private WriteCallback(long ptr, boolean isClosed) {
        this.ptr = ptr;
        this.isClosed = isClosed;
    }

    @SuppressWarnings("unused")
    public static WriteCallback create(IHandler handler) {
        long ptr = _allocate(handler);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate WriteCallback");
        }
        return new WriteCallback(ptr, false);
    }

    public static WriteCallback maybeCreate(IHandler handler) {
        if (handler == null) {
            return null;
        } else {
            return create(handler);
        }
    }

    private static native long _allocate(IHandler handler);
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
