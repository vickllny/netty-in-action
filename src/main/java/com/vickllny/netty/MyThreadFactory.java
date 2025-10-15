package com.vickllny.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThreadFactory implements ThreadFactory {

    private final String prefix;

    private final AtomicInteger counter = new AtomicInteger();

    public MyThreadFactory(final String prefix) {
        this.prefix = prefix;
    }


    @Override
    public Thread newThread(final Runnable r) {
        final Thread thread = new Thread(r);
        thread.setName(this.prefix + "-" + counter.incrementAndGet());
        return thread;
    }
}
