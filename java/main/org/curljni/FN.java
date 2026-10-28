package org.curljni;

import java.nio.LongBuffer;

public class FN {

    public final static long fwrite;
    public final static long write_callback_stream;
    public final static long write_callback_handler;
    public final static long accum_write_callback;
    public final static long fread;
    public final static long read_callback_file;
    public final static long read_seek_file;
    public final static long read_callback_stream;
    public final static long read_seek_cannot;

    // TODO
    // CURLOPT_UPLOAD
    // CURLOPT_POSTFIELDSIZE

    native private static long[] _get_values();

    static {
        Native.loadLib();
        final long[] values = _get_values();
        final LongBuffer bb = LongBuffer.wrap(values);

        fwrite                 = bb.get();
        write_callback_stream  = bb.get();
        write_callback_handler = bb.get();
        accum_write_callback   = bb.get();
        fread                  = bb.get();
        read_callback_file     = bb.get();
        read_seek_file         = bb.get();
        read_callback_stream   = bb.get();
        read_seek_cannot       = bb.get();
    }

    public static void main(String... args) {
        System.out.println(fwrite);
        System.out.println(write_callback_stream);
    }


}
