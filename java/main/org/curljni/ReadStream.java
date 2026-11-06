package org.curljni;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class ReadStream implements IResource {

    private long ptr;
    private boolean isClosed;
    private ByteBuffer bb;
    private int bbSize;
    private ReadableByteChannel channel;

    static {
        Native.loadLib();
    }

    private ReadStream() {}

    @SuppressWarnings("unused")
    private int callback(int limit) throws IOException {
        bb.clear();
        bb.limit(limit);
        return channel.read(bb);
    }

    public static ReadStream create(InputStream in) {
        return create(Channels.newChannel(in));
    }

    @SuppressWarnings("unused")
    public static ReadStream create(ReadableByteChannel channel) {
        int bbSize = 1024 * 16; // TODO
        ByteBuffer bb = ByteBuffer.allocateDirect(bbSize);
        ReadStream rs = new ReadStream();
        long ptr = rs._allocate(bb, bbSize);
        if (ptr == Native.NULL) {
            throw Err.error("failed to allocate read stream");
        }
        rs.ptr = ptr;
        rs.isClosed = false;
        rs.bb = bb;
        rs.bbSize = bbSize;
        rs.channel = channel;
        return rs;
    }

    public static ReadStream maybeCreate(InputStream in) {
        if (in == null) {
            return null;
        } else {
            return create(in);
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
        final long code = _free(ptr);
        isClosed = true;
        try {
            channel.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (code != 0) {
            throw Err.error("failed to close read stream, code: %s", code);
        }
    }
}
