package co.uk.michallet.chatapp.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Accessible threadpool using the Singleton pattern
 */
public class AppThreadPool {
    private static ExecutorService _pool;

    public synchronized static ExecutorService getInstance() {
        if (_pool == null) {
            // ForkJoinPool.common is not designed to be used for long blocking IO operations
            // therefore a separate ThreadPool is created to schedule these on.
            _pool = Executors.newFixedThreadPool(32);
        }
        return _pool;
    }

    private AppThreadPool() {
    }
}
