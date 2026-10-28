package org.curljni;

public class WriteCallback implements IResource {

    private final long ptr;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    private WriteCallback(long ptr, boolean isClosed) {
        this.ptr = ptr;
        this.isClosed = isClosed;
    }

    @SuppressWarnings("unused")
    public static WriteCallback create(IWriteHandler handler) {
        long ptr = _allocate(handler);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate WriteCallback");
        }
        return new WriteCallback(ptr, false);
    }

    private static native long _allocate(IWriteHandler handler);
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
