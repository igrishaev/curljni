package org.curljni;

@SuppressWarnings("unused")
public class Dummy implements IResource {

    public static final Dummy INSTANCE;
    static {
        INSTANCE = new Dummy();
    }

    @Override
    public long ptr() {
        return Native.NULL;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void close() {
    }
}
