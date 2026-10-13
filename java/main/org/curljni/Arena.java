package org.curljni;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Arena {

    final int bbLen;
    final private ByteBuffer bb;
    final private long ptr;
    final private ByteOrder BO_JVM;
    final private ByteOrder BO_JNI;
    final private long NULL;
    private final static byte TERM = 0;

    static {
        Native.loadLib();
    }

    private Arena(final int bbLen,
                  final ByteBuffer bb,
                  final long ptr,
                  final ByteOrder BO_JVM,
                  final ByteOrder BO_JNI,
                  final long NULL) {
        this.bbLen = bbLen;
        this.bb = bb;
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
        byte lead;

        // detect byte order for JVM
        bb.putLong(1);
        lead = bb.get(0);
        final ByteOrder BO_JVM = (lead == 1) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        final int code = _init_byte_buffer(bb);
        if (code != 0) {
            throw Err.error("failed to init arena, size: %s, code: %s", size, code);
        }
        bb.rewind();

        // byte order for JNI
        lead = bb.get();
        final ByteOrder BO_JNI = (lead == 1) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        // other fields
        bb.order(BO_JNI);
        final long NULL = bb.getLong();
        final long ptr = bb.getLong();

        return new Arena(size, bb, ptr, BO_JVM, BO_JNI, NULL);
    }

    public long ptr() {
        return ptr;
    }

    public Arena putNULL() {
        bb.putLong(NULL);
        return this;
    }

    public Arena putNULL(final int index) {
        bb.putLong(index, NULL);
        return this;
    }

    public int putCString(final String s) {
        final byte[] buf = s.getBytes(StandardCharsets.UTF_8);
        bb.put(buf);
        bb.put(TERM);
        return buf.length + 1;
    }

    public int putCString(final int index, final String s) {
        final byte[] buf = s.getBytes(StandardCharsets.UTF_8);
        bb.put(index, buf);
        bb.put(index + buf.length, TERM);
        return buf.length + 1;
    }

    public String readCString() {
        int start = bb.position();
        if (bb.get(start) == 0) {
            return null;
        }
        int len = bb.limit();
        int end = start;
        for (int i = start; i < len; i++) {
            if (bb.get(end) == 0) {
                break;
            }
            end++;
        }
        ByteBuffer slice = bb.slice(start, end);
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

    public Arena putInt(final int i) {
        bb.putInt(i);
        return this;
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
        System.out.println(a.bbLen);
        System.out.println(a.ptr);
        System.out.println(a.NULL);

    }

}