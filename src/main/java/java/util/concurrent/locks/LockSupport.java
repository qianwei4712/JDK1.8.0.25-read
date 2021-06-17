package java.util.concurrent.locks;
import sun.misc.Unsafe;

/**
 * 用于创建锁和其他同步类的基本线程阻塞原语。
 *
 * <p>此类与使用它的每个线程关联一个许可（在 {@link java.util.concurrent.Semaphore Semaphore} 类的意义上）。
 * 如果许可可用，调用 {@code park} 将立即返回，并在此过程中使用它；否则它<em>可能<em>阻塞。
 * 如果尚未许可可用，则调用 {@code unpark} 可使许可可用。 （尽管与信号量不同，许可不会累积。最多只有一个。）
 *
 * <p>方法 {@code park} 和 {@code unpark} 提供了阻塞和解除阻塞线程的有效方法，
 * 这些线程不会遇到导致已弃用的方法 {@code Thread.suspend} 和 {@code Thread.resume} 无法用于的问题此类目的：
 * 在一个调用 {@code park} 的线程和另一个试图 {@code unpark} 的线程之间进行竞争，由于许可，它将保持活跃度。
 *
 * 此外，如果调用者的线程被中断，{@code park} 将返回，并且支持超时版本。
 * {@code park} 方法也可能在任何其他时间返回，“无缘无故”，因此通常必须在返回时重新检查条件的循环中调用。
 *
 * 从这个意义上说，{@code park} 是“忙等待”的优化，不会浪费太多时间旋转，但必须与 {@code unpark} 配对才能有效。
 *
 * <p>{@code park} 的三种形式都支持 {@code blocker} 对象参数。
 * 该对象在线程被阻塞时被记录，以允许监控和诊断工具识别线程被阻塞的原因。 （此类工具可以使用方法 {@link #getBlocker(Thread)} 访问阻止程序。）
 * 强烈鼓励使用这些表格而不是没有此参数的原始表格。在锁实现中作为 {@code blocker} 提供的正常参数是 {@code this}。
 *
 * <p>这些方法旨在用作创建更高级别同步实用程序的工具，它们本身对大多数并发控制应用程序没有用处。
 * {@code park} 方法仅用于以下形式的构造:
 *  <pre> {@code
 * while (!canProceed()) { ... LockSupport.park(this); }}</pre>
 *
 * 其中 {@code canProceed} 或调用 {@code park} 之前的任何其他操作都不需要锁定或阻止。
 * 由于每个线程只关联一个许可证，因此对 {@code park} 的任何中间使用都可能干扰其预期效果。
 *
 * <p><b>Sample Usage.</b> Here is a sketch of a first-in-first-out
 * non-reentrant lock class:
 *  <pre> {@code
 * class FIFOMutex {
 *   private final AtomicBoolean locked = new AtomicBoolean(false);
 *   private final Queue<Thread> waiters
 *     = new ConcurrentLinkedQueue<Thread>();
 *
 *   public void lock() {
 *     boolean wasInterrupted = false;
 *     Thread current = Thread.currentThread();
 *     waiters.add(current);
 *
 *     // Block while not first in queue or cannot acquire lock
 *     while (waiters.peek() != current ||
 *            !locked.compareAndSet(false, true)) {
 *       LockSupport.park(this);
 *       if (Thread.interrupted()) // ignore interrupts while waiting
 *         wasInterrupted = true;
 *     }
 *
 *     waiters.remove();
 *     if (wasInterrupted)          // reassert interrupt status on exit
 *       current.interrupt();
 *   }
 *
 *   public void unlock() {
 *     locked.set(false);
 *     LockSupport.unpark(waiters.peek());
 *   }
 * }}</pre>
 */
public class LockSupport {
    private LockSupport() {} // 私有构造函数，无法实例化。

    private static void setBlocker(Thread t, Object arg) {
        // 即使不稳定，在这里也不需要写屏障。
        UNSAFE.putObject(t, parkBlockerOffset, arg);
    }

    /**
     * 除非许可，否则出于线程调度目的禁用当前线程。
     * <p>如果运行此线程可用，则它被消耗并且调用立即返回;
     * 否则，当前线程将因线程调度目的而被禁用并处于休眠状态，直到发生以下三种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程在返回时的中断状态。
     */
    public static void park() {
        UNSAFE.park(false, 0L);
    }

    /**
     * 除非许可，否则出于线程调度目的禁用当前线程。
     * <p>如果运行此线程可用，则它被消耗并且调用立即返回;
     * 否则，当前线程将因线程调度目的而被禁用并处于休眠状态，直到发生以下三种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程在返回时的中断状态。
     *
     * @param blocker 负责此线程停放的同步对象
     */
    public static void park(Object blocker) {
        // 获取当前线程
        Thread t = Thread.currentThread();
        // 设置Blocker
        setBlocker(t, blocker);
        // 获取许可
        UNSAFE.park(false, 0L);
        // 重新可运行后再此设置Blocker
        setBlocker(t, null);
    }

    /**
     * 为线程调度目的禁用当前线程，直至指定的等待时间，除非许可可用。
     * <p>如果许可可用，则它被消耗并且调用立即返回；
     * 否则，当前线程将因线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>指定的等待时间过去
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告其中哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程的中断状态或返回时经过的时间。
     *
     * @param nanos 等待的最大纳秒数
     */
    public static void parkNanos(long nanos) {
        if (nanos > 0)
            UNSAFE.park(false, nanos);
    }

    /**
     * 为线程调度目的禁用当前线程，直至指定的等待时间，除非许可可用。
     * <p>如果运行此线程可用，则它被消耗并且调用立即返回;
     * 否则，当前线程将因线程调度目的而被禁用并处于休眠状态，直到发生以下四种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>指定的等待时间过去；
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程在返回时的中断状态。
     *
     * @param blocker 负责此线程停放的同步对象
     * @param nanos 等待的最大纳秒数
     */
    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            UNSAFE.park(false, nanos);
            setBlocker(t, null);
        }
    }

    /**
     * 出于线程调度目的禁用当前线程，直到指定的截止日期，除非许可可用。
     *
     * <p>如果许可，则它被消耗并且调用立即返回；
     * 否则，当前线程将因线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>指定的等待时间过去；
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程的中断状态或返回时的当前时间。
     * @param deadline 绝对时间，以从纪元开始的毫秒数，等待
     */
    public static void parkUntil(long deadline) {
        UNSAFE.park(true, deadline);
    }

    /**
     * 出于线程调度目的禁用当前线程，直到指定的截止日期，除非许可可用。
     *
     * <p>如果许可，则它被消耗并且调用立即返回；
     * 否则，当前线程将因线程调度目的而被禁用，并处于休眠状态，直到发生以下四种情况之一：
     * <ul>
     * <li>其他一些线程以当前线程为参数调用 unpark 方法；
     * <li>其他线程 {@linkplain Thread#interrupt interrupts} 中断当前线程;
     * <li>指定的等待时间过去；
     * <li>虚假调用（无缘无故）返回。
     * </ul>
     * <p>此方法不报告哪些导致方法返回。
     * 调用者应该首先重新检查导致线程停放的条件。
     * 例如，调用者还可以确定线程的中断状态或返回时的当前时间。
     * @param blocker 负责此线程停放的同步对象
     * @param deadline 绝对时间，以从纪元开始的毫秒数，等待
     */
    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        UNSAFE.park(true, deadline);
        setBlocker(t, null);
    }

    /**
     * 使给定线程的许可可用（如果它尚不可用）。
     * 如果线程在 {@code park} 上被阻塞，那么它将解除阻塞。
     * 否则，它对 {@code park} 的下一次调用保证不会阻塞。如果给定的线程尚未启动，则无法保证此操作有任何效果。
     *
     * @param thread 线程非null情况下，取消线程停放
     */
    public static void unpark(Thread thread) {
        if (thread != null)
            UNSAFE.unpark(thread);
    }

    /**
     * 返回提供给尚未解除阻塞的 park 方法的最新调用的阻塞程序对象，如果未阻塞，则返回 null。
     * 返回的值只是一个瞬间的快照——线程可能已经在不同的阻塞器对象上解除阻塞或阻塞。
     *
     * @param t the thread
     * @return the blocker
     * @throws NullPointerException 如果参数为空
     */
    public static Object getBlocker(Thread t) {
        if (t == null)
            throw new NullPointerException();
        return UNSAFE.getObjectVolatile(t, parkBlockerOffset);
    }


    /**
     * Returns the pseudo-randomly initialized or updated secondary seed.
     * Copied from ThreadLocalRandom due to package access restrictions.
     */
    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = UNSAFE.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;   // 异或移位
            r ^= r >>> 17;
            r ^= r << 5;
        }
        else if ((r = java.util.concurrent.ThreadLocalRandom.current().nextInt()) == 0)
            r = 1; // 避免零
        UNSAFE.putInt(t, SECONDARY, r);
        return r;
    }

    // Hotspot implementation via intrinsics API
    private static final sun.misc.Unsafe UNSAFE;
    private static final long parkBlockerOffset;
    private static final long SEED;
    private static final long PROBE;
    private static final long SECONDARY;
    static {
        try {
            // 获取Unsafe实例
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            // 线程类类型
            Class<?> tk = Thread.class;
            // 获取 Thread 的 parkBlocker 字段的内存偏移地址
            parkBlockerOffset = UNSAFE.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
            // 获取 Thread 的 threadLocalRandomSeed 字段的内存偏移地址
            SEED = UNSAFE.objectFieldOffset(tk.getDeclaredField("threadLocalRandomSeed"));
            // 获取 Thread 的 threadLocalRandomProbe 字段的内存偏移地址
            PROBE = UNSAFE.objectFieldOffset(tk.getDeclaredField("threadLocalRandomProbe"));
            // 获取 Thread 的 threadLocalRandomSecondarySeed 字段的内存偏移地址
            SECONDARY = UNSAFE.objectFieldOffset(tk.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (Exception ex) { throw new Error(ex); }
    }

}
