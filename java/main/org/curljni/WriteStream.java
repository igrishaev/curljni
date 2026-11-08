package org.curljni;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class WriteStream implements IPeer {

    private long ptr;
    private WritableByteChannel channel;
    private boolean isClosed;
    private ByteBuffer bb;
    private int bbSize;

    static {
        Native.loadLib();
    }

    private WriteStream() {}

    @SuppressWarnings("unused")
    void callback(int size) throws IOException {
        bb.clear();
        bb.limit(size);
        channel.write(bb);
    }

    public static WriteStream create(OutputStream out) {
        return create(Channels.newChannel(out));
    }

    public static WriteStream create(WritableByteChannel channel) {
        int bbSize = 1024 * 16; // TODO
        ByteBuffer bb = ByteBuffer.allocateDirect(bbSize);
        WriteStream ws = new WriteStream();
        long ptr = ws._allocate(bb, bbSize);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate a write stream");
        }
        ws.bb = bb;
        ws.bbSize = bbSize;
        ws.isClosed = false;
        ws.ptr = ptr;
        ws.channel = channel;
        return ws;
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
        final long code = _free(ptr);
        isClosed = true;
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (code != 0) {
            throw Err.error("failed to close write stream, code: %s", code);
        }
    }
}
