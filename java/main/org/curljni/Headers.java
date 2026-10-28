package org.curljni;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Headers implements IResource {

    private final long ptr;
    private final String[] headers;
    private boolean isClosed;

    private Headers(long ptr, String[] headers, boolean isClosed) {
        this.ptr = ptr;
        this.headers = headers;
        this.isClosed = isClosed;
    }

    private static String[] convertHeaders(Map<String, String> headersMap) {
        int i = 0;
        int size = headersMap.size();
        String header, value, line;
        final String[] headersArr = new String[size];
        for (Map.Entry<String, String> e: headersMap.entrySet()) {
            header = e.getKey();
            value = e.getValue();
            if (header == null) {
                line = null;
            } else {
                line = header + ":" + ((value == null) ? "" : value);
            }
            headersArr[i] = line;
            i++;
        }
        return headersArr;
    }

    public static Headers create(final String[] headers) {
        long ptr = _allocate(headers, headers.length);
        return new Headers(ptr, headers, false);
    }

    public static Headers create(final Map<String, String> headersMap) {
        return create(convertHeaders(headersMap));
    }

    static {
        Native.loadLib();
    }

    @Override
    public String toString() {
        return String.format("<Headers, ptr: %s, values: %s>", ptr, Arrays.toString(headers));
    }

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    private static native long _allocate(String[] headers, int size);
    private static native long _free(long ptr);

    @Override
    public void close() {
        if (isClosed) return;
        final long code = _free(ptr);
        isClosed = true;
        if (code != 0) {
            throw Err.error("failed to close headers, code: %s", code);
        }
    }

    public static void main(final String... args) {
        final Map<String, String> hmap = new HashMap<>();
        hmap.put("foo", "bar");
        hmap.put("aaa", "bbb");
        hmap.put(null, "test");
        hmap.put("olo", null);
        try (final Headers h = create(hmap)) {
            System.out.println(h);
        }
    }
}
