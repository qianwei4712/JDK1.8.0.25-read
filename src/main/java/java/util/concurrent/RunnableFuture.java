package java.util.concurrent;

/**
 * A {@link Future} that is {@link Runnable}.
 * {@code run} 方法的成功执行会导致 {@code Future} 的完成并允许访问其结果。
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> 此 Future 的 {@code get} 方法返回的结果类型
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * 将此 Future 设置为其计算结果，除非它已被取消。
     */
    void run();
}
