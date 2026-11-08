package org.curljni;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TryLock implements AutoCloseable {

    private final Lock lock;

    private TryLock(Lock lock) {
        this.lock = lock;
    }

    public static TryLock create() {
        return new TryLock(new ReentrantLock());
    }

    public TryLock lock() {
        lock.lock();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
