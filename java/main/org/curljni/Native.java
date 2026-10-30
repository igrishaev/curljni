package org.curljni;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Native {

    public static final long NULL;
    public static final String libPath;

    private static String writeLibToTemp() throws IOException {
        ClassLoader cl = Native.class.getClassLoader();
        String resourcePath = OS.getLibFileName();
        final URL url = cl.getResource(resourcePath);
        if (url == null) {
            throw Err.error("failed to load a resource: %s", resourcePath);
        }
        final String libFileName = OS.getLibFileName();
        final File tmp = File.createTempFile("temp_", libFileName);
        final Path path = tmp.toPath();
        tmp.deleteOnExit();
        try (InputStream in = url.openStream();
             OutputStream out = Files.newOutputStream(tmp.toPath())) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            in.transferTo(out);
        }
        return path.toString();
    }

    static {
        try {
            libPath = writeLibToTemp();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadLib() {
        System.load(libPath);
    }

    native private static long _get_null();
    static {
        loadLib();
        NULL = _get_null();
    }
}
