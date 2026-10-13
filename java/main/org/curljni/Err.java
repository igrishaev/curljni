package org.curljni;

public class Err {
    public static RuntimeException error(final String template, final Object... args) {
        return new RuntimeException(String.format(template, args));
    }
}
