package org.curljni;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Arena {

    final private int bbSize;
    final private ByteBuffer bb;
    final protected long ptr;
    final private ByteOrder BO_JVM;
    final private ByteOrder BO_JNI;
    final private long NULL;
    private final static byte TERM = 0;

    static {
        Native.loadLib();
    }

    private Arena(final ByteBuffer bb,
                  final int bbSize,
                  final long ptr,
                  final ByteOrder BO_JVM,
                  final ByteOrder BO_JNI,
                  final long NULL) {
        this.bb = bb;
        this.bbSize = bbSize;
        this.ptr = ptr;
        this.BO_JVM = BO_JVM;
        this.BO_JNI = BO_JNI;
        this.NULL = NULL;
    }

    private static native int _init_byte_buffer(final ByteBuffer bb);
    public static Arena create() {
        return create(Const.ARENA_MIN_SIZE);
    }
    public static Arena create(final int size) {
        if (size < Const.ARENA_MIN_SIZE) {
            throw Err.error("Arena size %s is less than %s", size, Const.ARENA_MIN_SIZE);
        }
        final ByteBuffer bb = ByteBuffer.allocateDirect(size);
        final ByteOrder BO_JVM = bb.order();
        final ByteOrder BO_JNI = ByteOrder.nativeOrder();

        final int code = _init_byte_buffer(bb);
        if (code != 0) {
            throw Err.error("failed to init arena, size: %s, code: %s", size, code);
        }
        bb.order(BO_JNI);
        final long NULL = bb.getLong();
        final long ptr = bb.getLong();
        bb.rewind();
        return new Arena(bb, size, ptr, BO_JVM, BO_JNI, NULL);
    }

    public long ptr() {
        return ptr;
    }

    public String getCString() {
        int pos = bb.position();
        if (bb.get(pos) == 0) {
            return null;
        }
        int len = 0;
        for (int i = pos; i < bb.limit(); i++) {
            if (bb.get(pos + len) == 0) {
                break;
            }
            len++;
        }
        ByteBuffer slice = bb.slice(pos, len);
        return StandardCharsets.UTF_8.decode(slice).toString();
    }

    public Arena rewind() {
        bb.rewind();
        return this;
    }

    public Arena orderJVM() {
        bb.order(BO_JVM);
        return this;
    }

    public Arena orderJNI() {
        bb.order(BO_JNI);
        return this;
    }

    public int getInt() {
        return bb.getInt();
    }

    public long getLong() {
        return bb.getLong();
    }

    public long getLong(int index) {
        return bb.getLong(index);
    }

    public Arena clear() {
        bb.clear();
        return this;
    }

    public Arena putInt(final int i) {
        bb.putInt(i);
        return this;
    }

    public long charPtr(String s) {
        int pos = bb.position();
        if (s == null) {
            bb.putLong(NULL);
        } else {
            byte[] buf = s.getBytes(StandardCharsets.UTF_8);
            bb.put(buf);
            bb.put((byte) 0);
        }
        return ptr + pos;
    }

    public long bufPtr(byte[] buf) {
        int pos = bb.position();
        if (buf == null) {
            bb.putLong(NULL);
        } else {
            bb.put(buf);
            bb.put((byte) 0);
        }
        return ptr + pos;
    }

    public Arena putInt(final int index, final int i) {
        bb.putInt(index, i);
        return this;
    }

    public Arena putLong(final long l) {
        bb.putLong(l);
        return this;
    }

    public Arena putLong(final int index, final long l) {
        bb.putLong(index, l);
        return this;
    }

    public int position() {
        return bb.position();
    }

    public Arena skip(final int len) {
        final int pos = bb.position();
        bb.position(pos + len);
        return this;
    }

    public Arena get(final byte[] ba) {
        bb.get(ba);
        return this;
    }

    @SuppressWarnings("unused")
    public Arena debug(final int len) {
        final byte[] ba = new byte[len];
        bb.get(0, ba);
        System.out.println(Arrays.toString(ba));
        return this;
    }

    public static void main(final String... args) {
        final Arena a = Arena.create();
        a.debug(64);
        System.out.println(a.BO_JVM);
        System.out.println(a.BO_JNI);
        System.out.println(a.bbSize);
        System.out.println(a.ptr);
        System.out.println(a.NULL);

    }

}