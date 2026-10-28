package org.curljni;

import java.nio.LongBuffer;

public class FN {

    public final static long fwrite;
    public final static long write_callback_stream;

    native private static long[] _get_values();

    static {
        Native.loadLib();
        final long[] values = _get_values();
        final LongBuffer bb = LongBuffer.wrap(values);

        fwrite                = bb.get();
        write_callback_stream = bb.get();
    }

    public static void main(String... args) {
        System.out.println(fwrite);
        System.out.println(write_callback_stream);
    }


}
