package java.util.concurrent;

/**
 * 执行提交的 {@link Runnable} 任务的对象。
 * 该接口提供了一种将任务提交与每个任务将如何运行的机制分离的方法，包括线程使用、调度等的细节。
 * {@code Executor} 通常用于代替显式创建线程。
 * 例如，不是为一组任务中的每一个调用 {@code new Thread(new(RunnableTask())).start()}，您可以使用:
 *
 * <pre>
 * Executor executor = <em>anExecutor</em>;
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * </pre>
 *
 * 但是，{@code Executor} 接口并不严格要求执行是异步的。
 * 在最简单的情况下，执行者可以立即在调用者的线程中运行提交的任务:
 *
 *  <pre> {@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }}</pre>
 *
 * 更典型的是，任务在调用者线程之外的某个线程中执行。
 * 下面的执行程序为每个任务生成一个新线程。
 *
 *  <pre> {@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }}</pre>
 *
 * 许多 {@code Executor} 实现对任务的调度方式和时间施加了某种限制。
 * 下面的 executor 将任务的提交序列化到第二个 executor，说明了一个复合 executor。
 *
 *  <pre> {@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(final Runnable r) {
 *     tasks.offer(new Runnable() {
 *       public void run() {
 *         try {
 *           r.run();
 *         } finally {
 *           scheduleNext();
 *         }
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }}</pre>
 *
 * 此包中提供的{@code Executor} 实现实现了{@link ExecutorService}，这是一个更广泛的接口。
 * {@link ThreadPoolExecutor} 类提供了一个可扩展的线程池实现。
 * {@link Executors} 类为这些 Executor 提供了方便的工厂方法。
 *
 * <p>内存一致性影响：在将 {@code Runnable} 对象提交给 {@code Executor} 之前线程中的操作 happen-before执行开始，也许在另一个线程中。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Executor {

    /**
     * 在将来的某个时间执行给定的命令。
     *该命令可以在新线程、池线程或调用线程中执行，具体取决于 {@code Executor} 实现。
     *
     * @param command 可运行的任务
     * @throws RejectedExecutionException 如果这个任务不能被接受执行
     * @throws NullPointerException 如果命令为空
     */
    void execute(Runnable command);
}
