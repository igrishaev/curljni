package org.curljni;

import java.nio.LongBuffer;

public class CURLFOLLOW {

    public static final long OFF;
    public static final long ALL;
    public static final long OBEYCODE;
    public static final long FIRSTONLY;

    native private static long[] _get_values();

    static {
        Native.loadLib();
        final long[] options = _get_values();
        final LongBuffer bb = LongBuffer.wrap(options);

        OFF = bb.get();
        ALL = bb.get();
        OBEYCODE = bb.get();
        FIRSTONLY = bb.get();
    }
}
