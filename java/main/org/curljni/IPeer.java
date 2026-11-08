package org.curljni;

public interface IPeer extends AutoCloseable {
    long ptr();
    boolean isClosed();
}
