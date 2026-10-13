package org.curljni;

import java.io.File;
import java.nio.file.Path;

public class FILE implements IResource {

    private final long ptr;
    private final String path;
    private final String mode;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    private FILE(final long ptr, final String path, final String mode, final boolean isClosed) {
        this.ptr = ptr;
        this.path = path;
        this.mode = mode;
        this.isClosed = isClosed;
    }

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public String toString() {
        return String.format("<FILE %s %s>", mode, path);
    }

    public static FILE open(final String path, final String mode) {
        final long fd = _fopen(path, mode);
        if (fd < 0) {
            throw Err.error("fopen failed, code: %s, mode: %s, path: %s", fd, mode, path);
        }
        return new FILE(fd, path, mode, false);
    }

    @SuppressWarnings("unused")
    public static FILE open(final File file, final String mode) {
        return open(file.getAbsolutePath(), mode);
    }

    @SuppressWarnings("unused")
    public static FILE open(final Path path, final String mode) {
        return open(path.toAbsolutePath().toString(), mode);
    }

    private native static long _fopen(final String path, final String mode);

    private native static long _fclose(final long ptr);

    @Override
    public void close() {
        if (isClosed) return;
        final long code = _fclose(ptr);
        isClosed = true;
        if (code != 0) {
            throw Err.error("fclose failed, code: %s, mode: %s, path: %s", code, mode, path);
        }
    }

    public static void main(final String... args) {
        try (final FILE f = open("pom.txt", "rb")) {
            System.out.println(f);
        }
    }
}
