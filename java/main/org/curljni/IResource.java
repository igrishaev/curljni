package org.curljni;

public interface IResource extends AutoCloseable {
    long ptr();
    boolean isClosed();
}
