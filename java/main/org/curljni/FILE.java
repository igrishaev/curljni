package org.curljni;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FILE implements IPeer {

    private final long ptr;
    private final File file;
    private final String mode;
    private boolean isClosed;

    static {
        Native.loadLib();
    }

    private FILE(final long ptr, final File file, final String mode, final boolean isClosed) {
        this.ptr = ptr;
        this.file = file;
        this.mode = mode;
        this.isClosed = isClosed;
    }

    public File file() {
        return file;
    }

    @SuppressWarnings("unused")
    public static FILE openTemp(String prefix, String suffix, String mode) {
        final File f;
        try {
            f = File.createTempFile(prefix, suffix);
            f.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return open(f, mode);
    }

    @Override
    public long ptr() {
        return ptr;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public String toString() {
        return String.format("<FILE %s %s>", mode, file.getAbsolutePath());
    }

    @SuppressWarnings("unused")
    public long fileSize() throws IOException {
        return Files.size(file.toPath());
    }

    public static FILE open(final String path, final String mode) {
        return open(new File(path), mode);
    }

    public static FILE open(File file, String mode) {
        final String path = file.getAbsolutePath();
        final long fd = _fopen(path, mode);
        if (fd < 0) {
            throw Err.error("fopen failed, code: %s, mode: %s, path: %s", fd, mode, path);
        }
        return new FILE(fd, file, mode, false);
    }

    @SuppressWarnings("unused")
    public static FILE open(final Path path, final String mode) {
        return open(path.toFile(), mode);
    }

    private native static long _fopen(final String path, final String mode);

    private native static long _fclose(final long ptr);

    @Override
    public void close() {
        if (isClosed) return;
        final long code = _fclose(ptr);
        isClosed = true;
        if (code != 0) {
            throw Err.error("fclose failed, code: %s, mode: %s, path: %s", code, mode, file.getAbsolutePath());
        }
    }

    public static void main(final String... args) {
        try (final FILE f = open("pom.txt", "rb")) {
            System.out.println(f);
        }
    }
}
