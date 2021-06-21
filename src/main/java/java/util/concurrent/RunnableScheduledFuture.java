package java.util.concurrent;

/**
 * A {@link ScheduledFuture} that is {@link Runnable}.
 * {@code run} 方法的成功执行会导致 {@code Future} 的完成并允许访问其结果。
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> 此 Future 的 {@code get} 方法返回的结果类型
 */
public interface RunnableScheduledFuture<V> extends RunnableFuture<V>, ScheduledFuture<V> {

    /**
     * 如果此任务是周期性的，则返回 {@code true}。周期性任务可能会根据某个计划重新运行。
     * 一个非周期性任务只能运行一次。
     *
     * @return {@code true} 如果此任务是周期性的
     */
    boolean isPeriodic();
}
