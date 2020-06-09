package java.util.concurrent;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 允许一个或多个线程等待直到在其他线程中执行的一组操作完成的同步辅助。
 *
 * <p>A CountDownLatch用给定的计数初始化。
 * await方法阻塞，直到由于countDown()方法的调用而导致当前计数达到零，之后所有等待线程被释放，并且任何后续的await 调用立即返回。
 * 这是一个一次性的现象 - 计数无法重置。 如果您需要重置计数的版本，请考虑使用CyclicBarrier 。
 *
 * <p>A CountDownLatch是一种通用的同步工具，可用于多种用途。
 * 一个CountDownLatch为一个计数的CountDownLatch用作一个简单的开/关锁存器，或者门：所有线程调用await在门口等待，直到被调用countDown()的线程打开。
 * 一个CountDownLatch初始化N可以用来做一个线程等待，直到N个线程完成某项操作，或某些动作已经完成N次。
 *
 * <p>CountDownLatch一个有用的属性是，它不要求调用countDown线程等待计数到达零之前继续，它只是阻止任何线程通过await ，直到所有线程可以通过。
 *
 * <p>示例用法：这是一组类，其中一组工作线程使用两个倒计时锁存器：
 * 1. 第一个是启动信号，防止任何工作人员进入，直到驾驶员准备好继续前进;
 * 2. 第二个是完成信号，允许司机等到所有的工作人员完成。
 *
 *  <pre> {@code
 * class Driver { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch startSignal = new CountDownLatch(1);
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *     doSomethingElse();            // don't let run yet
 *     startSignal.countDown();      // let all threads proceed
 *     doSomethingElse();
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
 *     this.startSignal = startSignal;
 *     this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *     try {
 *       startSignal.await();
 *       doWork();
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * <p>另一个典型的用法是将问题划分为N个部分，用一个Runnable来描述每个部分，该Runnable执行该部分并在锁存器上倒计时，并将所有Runnables排队到执行器。
 * 当所有子部分完成时，协调线程将能够通过等待。 （当线程必须以这种方式反复倒数时，请改用CyclicBarrier ））
 *
 *  <pre> {@code
 * class Driver2 { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownLatch doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownLatch doneSignal, int i) {
 *     this.doneSignal = doneSignal;
 *     this.i = i;
 *   }
 *   public void run() {
 *     try {
 *       doWork(i);
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * 内存一致性效果：直到计数调用之前达到零，在一个线程操作countDown() happen-before以下由相应的成功返回行动await()在另一个线程。
 *
 * @since 1.5
 * @author Doug Lea
 */
public class CountDownLatch {
    /**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;

    /**
     * 构造一个以给定计数 CountDownLatch CountDownLatch。
     * @param count count -的次数 countDown()必须调用之前线程可以通过 await()
     * @throws IllegalArgumentException 如果 count为负数
     */
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 导致当前线程等到锁存器计数到零，除非线程是interrupted 。<p>
     * 如果当前计数为零，则此方法立即返回。<p>
     * 如果当前计数大于零，则当前线程将被禁用以进行线程调度，并处于休眠状态，直至发生两件事情之一：<p>
     * 1. 由于countDown()方法的调用，计数达到零;<p>
     * 2. 一些其他线程interrupts当前线程。<p>
     *
     * 如果当前线程：<p>
     * 1. 在进入该方法时设置了中断状态;<p>
     * 2. 是interrupted等待<p>
     * 然后InterruptedException被关上，当前线程的中断状态被清除。 <p>
     *
     * @throws InterruptedException 如果当前线程在等待时中断
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 导致当前线程等到锁存器向下计数到零，除非线程为interrupted ，否则指定的等待时间过去。<p>
     * 如果当前计数为零，则此方法将立即返回值为true 。<p>
     * 如果当前计数大于零，则当前线程将被禁用以进行线程调度，并处于休眠状态，直至发生三件事情之一：<p>
     *
     * 1. 由于countDown()方法的调用，计数达到零;<p>
     * 2. 一些其他线程interrupts当前线程;<p>
     * 3. 指定的等待时间过去了。<p>
     *
     * 如果计数达到零，则方法返回值为true 。<p>
     *
     * 如果当前线程：<p>
     * 1. 在进入该方法时设置了中断状态;<p>
     * 2. 是等待interrupted<p>
     *
     * 然后InterruptedException被关上，当前线程的中断状态被清除。<p>
     *
     * 如果指定的等待时间过去，则返回值false 。 如果时间小于或等于零，该方法根本不会等待。 <p>
     *
     *
     * @param timeout 等待的最长时间
     * @param unit timeout参数的时间单位
     * @return true如果计数达到零和 false如果在计数达到零之前经过的等待时间
     * @throws InterruptedException 如果当前线程在等待时中断
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 减少锁存器的计数，如果计数达到零，释放所有等待的线程。
     * 如果当前计数大于零，则它将递减。 如果新计数为零，则所有等待的线程都将被重新启用以进行线程调度。
     * <p>如果当前计数等于零，那么没有任何反应。
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 返回当前计数。该方法通常用于调试和测试。
     * @return 当前计数
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * 返回一个标识此锁存器的字符串及其状态。 括号中的状态包括字符串"Count ="后跟当前计数。
     * @return 识别此锁存器的字符串以及其状态
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
