package java.lang;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;

/**
 * Thread 是指程序当中的执行线程。Java 虚拟机允许一个应用拥有多个线程同时运行。
 * <p>
 * 每一条线程都有优先级。拥有更高优先级的线程在执行时优先于低优先级的线程。<br/>
 * 每一条线程都可能被标注为守护线程。<br/>
 * 当线程中执行的代码创建了一个新的线程对象，那么这个新的线程对象的优先级和创建它的线程的优先级相同。
 * 并且，当创建它的线程是一个守护线程时，新线程才是守护线程。
 * <p>
 * 当 Java 虚拟机启动时，通常已经存在了一个非守护线程（这个线程通常会调用某指定类的名为main的方法）。
 * Java 虚拟机将继续执行，直到发生以下任一情况，发生这两种情况，Java 虚拟机将结束：
 * <ul>
 * <li>Runtime类的exit方法被调用，并且安全管理器已经允许进行退出操作。
 * <li>所有非守护线程都已经消亡，消亡原因要么是从run方法返回了，要么是抛出异常了。
 * </ul>
 * <p>
 * 有两种方式去创建一个新的线程。第一种是继承 Thread 类，这个子类需要重写 run 方法。
 * 子类的示例可以被分配资源并启动。<br/>
 * 另一种方式方式是实现 Runnable 接口，也需要实现 run 方法。这个类的实例可以作为参数用于创建 Thread，然后启动。
 * <p>
 * 每个线程都有一个name用于标识区别。可以有多个线程名称相同。
 * 如果在创建线程时未指定名称，则会为其生成一个新名称。
 * <p>
 * 除非另有说明，否则将 null参数传递给 null 中的构造函数或方法将导致抛出 NullPointerException 。
 * @since   JDK1.0
 */
public class Thread implements Runnable {
    /*确保registerNatives是<clinit>要做的第一件事。 */
    private static native void registerNatives();
    static {
        //注册本地方法，用于调用操作系统方法
        registerNatives();
    }

    /**
     * 线程名
     */
    private volatile char  name[];
    /**
     * 线程优先级，int类型，范围为1-10，默认为5
     */
    private int            priority;
    private Thread         threadQ;
    private long           eetop;

    /* Whether or not to single_step this thread. */
    private boolean     single_step;

    /**
     * 该线程是否是守护程序线程。
     */
    private boolean     daemon = false;

    /* JVM state */
    private boolean     stillborn = false;

    /* 将要运行的对象. */
    private Runnable target;

    /* 该线程的线程组组 */
    private ThreadGroup group;

    /* The context ClassLoader for this thread */
    private ClassLoader contextClassLoader;

    /* The inherited AccessControlContext of this thread */
    private AccessControlContext inheritedAccessControlContext;

    /* 用于自动编号匿名线程，static 序号 */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    /* ThreadLocal values pertaining to this thread. This map is maintained by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is maintained by the InheritableThreadLocal class.
     */
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

    /*
     * The requested stack size for this thread, or 0 if the creator did
     * not specify a stack size.  It is up to the VM to do whatever it
     * likes with this number; some VMs will ignore it.
     */
    private long stackSize;

    /*
     * JVM-private state that persists after native thread termination.
     */
    private long nativeParkEventPointer;

    /*
     * Thread ID
     */
    private long tid;

    /* For generating thread ID */
    private static long threadSeqNumber;

    /**
     * Java线程状态工具，默认0-尚未启动
     */
    private volatile int threadStatus = 0;

    private static synchronized long nextThreadID() {
        return ++threadSeqNumber;
    }

    /**
     * The argument supplied to the current call to
     * java.util.concurrent.locks.LockSupport.park.
     * Set by (private) java.util.concurrent.locks.LockSupport.setBlocker
     * Accessed using java.util.concurrent.locks.LockSupport.getBlocker
     */
    volatile Object parkBlocker;

    /* The object in which this thread is blocked in an interruptible I/O
     * operation, if any.  The blocker's interrupt method should be invoked
     * after setting this thread's interrupt status.
     */
    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();

    /* Set the blocker field; invoked via sun.misc.SharedSecrets from java.nio code
     */
    void blockedOn(Interruptible b) {
        synchronized (blockerLock) {
            blocker = b;
        }
    }

    /**
     * 线程可以具有的最低优先级。
     */
    public final static int MIN_PRIORITY = 1;
   /**
     * 分配给线程的默认优先级
     */
    public final static int NORM_PRIORITY = 5;
    /**
     * 线程可以具有的最高优先级。
     */
    public final static int MAX_PRIORITY = 10;


    /**
     * Initializes a Thread with the current AccessControlContext.
     * @see #init(ThreadGroup,Runnable,String,long,AccessControlContext)
     */
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize) {
        init(g, target, name, stackSize, null);
    }

    /**
     * 初始化线程。
     * @param g 线程组
     * @param target Runnable 对象，它的 run() 方法将会被调用
     * @param name 新创建的线程的名称
     * @param stackSize 新线程的栈的大小，0表示这个参数被忽略
     * @param acc 要继承的AccessControlContext；如果为null，则为 AccessController.getContext（）
     */
    private void init(ThreadGroup g, Runnable target, String name,
                      long stackSize, AccessControlContext acc) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name.toCharArray();

        Thread parent = currentThread();
        SecurityManager security = System.getSecurityManager();
        if (g == null) {
            /* Determine if it's an applet or not */
            /* If there is a security manager, ask the security manager
               what to do. */
            if (security != null) {
                g = security.getThreadGroup();
            }
            /* 如果安全性对此问题没有强烈的看法，请使用父线程组. */
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }

        /* checkAccess regardless of whether or not threadgroup is
           explicitly passed in. */
        g.checkAccess();
        //我们具有所需的权限吗？
        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }

        g.addUnstarted();

        this.group = g;
        //线程的优先级和是否为守护线程，默认继承创建它的父线程
        this.daemon = parent.isDaemon();
        this.priority = parent.getPriority();
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =
                acc != null ? acc : AccessController.getContext();
        this.target = target;
        setPriority(priority);
        if (parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /* Stash the specified stack size in case the VM cares */
        this.stackSize = stackSize;

        /* Set thread ID */
        tid = nextThreadID();
    }

    /**
     * 将CloneNotSupportedException作为线程抛出无法有意义地克隆。 构造一个新的线程。
     * @throws  CloneNotSupportedException always
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * 无参构造，无线程组；
     * 分配一个新的Thread对象。
     * 此构造具有相同的效果Thread (null, null, gname) ，其中gname是新生成的名字。
     * 自动生成的名称格式为"Thread-"+ n ，其中n为整数。
     */
    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的Thread对象。 该构造函数具有与Thread (null, target, gname)相同的效果，其中gname是新生成的名称。
     * 自动生成的名称格式为"Thread-"+ n ，其中n为整数。
     * @param  target 启动此线程时调用其run方法的对象。 如果null ，这个类run方法什么都不做。
     */
    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 创建一个继承给定AccessControlContext的新线程。 这不是公共构造函数。
     */
    Thread(Runnable target, AccessControlContext acc) {
        init(null, target, "Thread-" + nextThreadNum(), 0, acc);
    }

    /**
     * 分配一个新的Thread对象。 此构造具有相同的效果Thread (group, target, gname) ，其中gname是新生成的名字。
     * 自动生成的名称格式为"Thread-"+ n ，其中n为整数。
     * @param  group  线程组。 如果是null并且有一个安全管理员，那么该组由SecurityManager.getThreadGroup()决定 。
     *                如果没有安全管理员或SecurityManager.getThreadGroup()返回null ，该组将设置为当前线程的线程组。
     * @param  target 启动此线程时调用其run方法的对象。 如果null ，这个线程的run方法被调用。
     * @throws  SecurityException 如果当前线程不能在指定的线程组中创建线程
     */
    public Thread(ThreadGroup group, Runnable target) {
        init(group, target, "Thread-" + nextThreadNum(), 0);
    }

    /**
     * 分配一个新的Thread对象。 此构造具有相同的效果Thread (null, null, name) 。
     * @param   name 新线程的名称
     */
    public Thread(String name) {
        init(null, null, name, 0);
    }

    /**
     * 分配一个新的Thread对象。 此构造具有相同的效果Thread (group, null, name) 。
     * @param  group 线程组。 如果是null并且有一个安全管理员，那么该组由SecurityManager.getThreadGroup()决定 。
     *               如果没有安全管理员或SecurityManager.getThreadGroup()返回null ，该组将设置为当前线程的线程组。
     * @param  name 新线程的名称
     * @throws  SecurityException 如果当前线程无法在指定的线程组中创建线程
     */
    public Thread(ThreadGroup group, String name) {
        init(group, null, name, 0);
    }

    /**
     * 分配一个新的Thread对象。 此构造具有相同的效果Thread (null, target, name) 。
     * @param  target 启动此线程时调用其run方法的对象。 如果null ，则调用此线程的run方法。
     * @param  name 新线程的名称
     */
    public Thread(Runnable target, String name) {
        init(null, target, name, 0);
    }

    /**
     * 分配一个新的Thread对象，使其具有target作为其运行对象，具有指定的name作为其名称，属于group引用的线程组。
     * <p>如果有安全管理器，则使用ThreadGroup作为参数调用其checkAccess方法。
     * <p>此外，它的checkPermission方法由RuntimePermission("enableContextClassLoaderOverride")权限调用，
     * 直接或间接地由覆盖getContextClassLoader或setContextClassLoader方法的子类的getContextClassLoader setContextClassLoader调用。
     * <p>新创建的线程的优先级设置为等于创建线程的优先级，即当前正在运行的线程。
     * 可以使用方法setPriority将优先级改变为新值。
     * <p>当且仅当创建它的线程当前被标记为守护线程时，新创建的线程才被初始化为守护线程。
     * 方法setDaemon可以用于改变线程是否是守护进程。
     *
     * @param  group 线程组。 如果是null并且有一个安全管理器，则该组由SecurityManager.getThreadGroup()决定 。
     *               如果没有安全管理员或SecurityManager.getThreadGroup()返回null ，该组将设置为当前线程的线程组。
     * @param  target 启动此线程时调用其run方法的对象。 如果null ，则调用此线程的run方法。
     * @param  name  新线程的名称
     * @throws  SecurityException 如果当前线程不能在指定的线程组中创建线程，或者不能覆盖上下文类加载器方法。
     */
    public Thread(ThreadGroup group, Runnable target, String name) {
        init(group, target, name, 0);
    }

    /**
     * 分配一个新的Thread对象，以便它具有target作为其运行对象，将指定的name正如其名，以及属于该线程组由称作group ，并具有指定的堆栈大小 。
     *
     * <p>这个构造函数与Thread(ThreadGroup,Runnable,String)相同，除了它允许指定线程栈大小的事实之外。
     * 堆栈大小是虚拟机为该线程的堆栈分配的大致的地址空间字节数。 stackSize参数的影响（如果有的话）与平台有关。
     *
     * <p>在某些平台上，指定了一个较高的值stackSize参数可以允许抛出一个前一个线程来实现更大的递归深度StackOverflowError 。
     * 类似地，指定较低的值可能允许更多数量的线程同时存在，而不会抛出OutOfMemoryError （或其他内部错误）。
     * 所述stackSize参数的值和最大递归深度和并发水平之间的关系的细节是依赖于平台的。
     * 在某些平台上，该值stackSize参数可能没有任何效果。
     *
     * <p>虚拟机可以自由地对待stackSize参数作为建议。
     * 如果平台的指定值不合理地低，虚拟机可能会改为使用一些平台特定的最小值; 如果指定的值不合理地高，虚拟机可能会使用一些平台特定的最大值。
     * 同样，虚拟机可以自由地按照合适的方式向上或向下舍入指定的值（或完全忽略它）。
     *
     * <p>对于指定的值为零stackSize参数将使这种构造的行为酷似Thread(ThreadGroup, Runnable, String)构造。
     *
     * <p>由于此构造函数的行为依赖于平台依赖性质，因此在使用时应特别小心。 执行给定计算所需的线程栈大小可能会因JRE实现而异。
     * 鉴于这种变化，可能需要仔细调整堆栈大小参数，并且可能需要对要运行应用程序的每个JRE实现重复调整。
     *
     * <p>实现注意事项：鼓励Java平台实现者的记录其实施的行为stackSize参数。
     *
     * @param  group 线程组。 如果是null并且有一个安全管理器，则该组由SecurityManager.getThreadGroup()决定 。
     *               如果没有安全管理员或SecurityManager.getThreadGroup()返回null ，该组将设置为当前线程的线程组。
     * @param  target 启动此线程时调用其run方法的对象。 如果null ，则调用此线程的run方法。
     * @param  name  新线程的名称
     * @param  stackSize 新线程所需的堆栈大小，或为零表示此参数将被忽略。
     * @throws  SecurityException 如果当前线程无法在指定线程组中创建线程
     * @since 1.4
     */
    public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
        init(group, target, name, stackSize);
    }

    /**
     * 导致线程开始执行，Java 虚拟机会调用线程的 run 方法。
     * <p>然后当前线程和被调用线程将会并发执行
     * <p>一个线程不可以被重新启动，线程只能启动一次。
     * @exception  IllegalThreadStateException  如果线程已经被启动
     * @see        #run()
     * @see        #stop()
     */
    public synchronized void start() {
        /**
         * 这个方法不能作为main启动调用，或者虚拟机内部启动调用
         * 将来可能会向该方法添加新功能，也可能添加到虚拟机。
         * 线程0状态代表刚创建，NEW 状态；也就是说，一条线程只能start一次
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();
        /* 通知组该线程即将开始，以便可以将其添加到组的线程列表中，并且该组的未启动计数可以减少。*/
        group.add(this);
        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* 没做什么。如果start0抛出了Throwable，则它将被向上传递到调用堆栈 */
            }
        }
    }

    /**
     * 如果这个线程是使用单独的 Runnable 对象构造的，则 Runnable 对象的run方法会被调用; 否则，此方法不执行任何操作并返回。
     * <p>Thread 的子类应该重写这个方法
     * @see     #start()
     * @see     #Thread(ThreadGroup, Runnable, String)
     */
    @Override
    public void run() {
        // target 是一个 Runnable 对象参数
        // 如果 target 不为空，那么说明这个 Thread 线程是通过 Runnable 创建的
        // 那么启动的时候需要执行 Runnable 的 run 方法
        if (target != null) {
            target.run();
        }
    }

    /**
     * 系统调用此方法，以使Thread 在实际退出之前有机会进行清理。
     */
    private void exit() {
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* Aggressively null out all reference fields: see bug 4006245 */
        target = null;
        /* Speed the release of some of these resources */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }

    /**
     * 中断此线程。
     * <p>线程可以中断自身，这是允许的。在这种情况下，不用进行安全性验证（{@link #checkAccess() checkAccess} 方法检测）
     *
     * <p>若当前线程由于 wait() 方法阻塞，或者由于join()、sleep()方法，然后线程的中断状态将被清除，并且将收到 {@link InterruptedException}。
     * <p>如果线程由于 IO操作（{@link java.nio.channels.InterruptibleChannel InterruptibleChannel}）阻塞，那么通道 channel 将会关闭，
     * 并且线程的中断状态将被设置，线程将收到一个 {@link java.nio.channels.ClosedByInterruptException} 异常。
     * <p>如果线程由于在 {@link java.nio.channels.Selector} 中而阻塞，那么线程的中断状态将会被设置，它将立即从选择操作中返回。
     *该值可能是一个非零值，就像调用选择器的{@link java.nio.channels.Selector＃wakeupakeup}方法一样。
     *
     * <p>如果上述条件均不成立，则将设置该线程的中断状态。</p>
     * <p>中断未运行的线程不必产生任何作用。
     * @throws  SecurityException 如果当前线程无法修改此线程
     */
    public void interrupt() {
        //如果调用中断的是线程自身，则不需要进行安全性判断
        if (this != Thread.currentThread())
            checkAccess();

        synchronized (blockerLock) {
            Interruptible b = blocker;
            if (b != null) {
                interrupt0();           // 只是设置中断标志
                b.interrupt(this);
                return;
            }
        }
        interrupt0();
    }

    /**
     * 测试当前线程是否已被中断。
     * 通过此方法可以清除线程的中断状态.
     * 换句话说，如果此方法要连续调用两次，则第二个调用将返回false(除非当前线程在第一个调用清除了它的中断状态之后，且在第二个调用对其进行检查之前再次中断)
     * <p>如果中断时，线程并没有存活，那么该方法返回 false
     * @return   如果该线程已被中断，返回true；否则返回 false
     * @see #isInterrupted()
     */
    public static boolean interrupted() {
        //清除线程的中断状态
        return currentThread().isInterrupted(true);
    }

    /**
     * 测试此线程是否已被中断。线程的中断状态不受此方法的影响。
     * <p>如果中断时，线程并没有存活，那么该方法返回 false。意思就是，如果线程还没有 start 启动，或者已经消亡，那么返回依然是 false.
     * @return  如果该线程已被中断，返回true；否则返回 false
     * @see     #interrupted()
     */
    public boolean isInterrupted() {
        //不清除中断状态
        return isInterrupted(false);
    }

    /**
     * 更改此线程的优先级。
     * <p> 首先调用这个线程的checkAccess方法，没有参数。
     * 这可能会导致抛出异常 SecurityException 。
     * <p> 否则，该线程的优先级设置为指定的最小 newPriority 和最大允许的线程的线程组的优先级。
     * @param newPriority 需要设置的此线程的优先级
     * @exception  IllegalArgumentException  如果优先级不在 MIN_PRIORITY到 MAX_PRIORITY 。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @see        #getThreadGroup()
     * @see        ThreadGroup#getMaxPriority()
     */
    public final void setPriority(int newPriority) {
        ThreadGroup g;
        //判定当前运行的线程是否有权修改该线程
        checkAccess();
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        //获取该线程所属的线程组
        if((g = getThreadGroup()) != null) {
            // 判断是否高于此线程组的最高优先级
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            // 调用 native 方法设置优先级
            setPriority0(priority = newPriority);
        }
    }

    /**
     * @return  返回当前线程的优先级
     * @see     #setPriority
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * 更改线程名
     * @param      name  此线程的新名称。
     * @exception  SecurityException  如果当前线程无法修改此线程。
     * @see        #getName
     */
    public final synchronized void setName(String name) {
        checkAccess();
        this.name = name.toCharArray();
        // 如果线程状态不为0（初始状态），说明线程已经启动
        // 那就需要调用 native 方法进行更改。
        if (threadStatus != 0) {
            setNativeName(name);
        }
    }

    /**
     * @return  返回线程的名称
     * @see     #setName(String)
     */
    public final String getName() {
        return new String(name, true);
    }

    /**
     * Returns the thread group to which this thread belongs.
     * This method returns null if this thread has died
     * (been stopped).
     *
     * @return 该线程的线程组。
     */
    public final ThreadGroup getThreadGroup() {
        return group;
    }

    /**
     * Returns an estimate of the number of active threads in the current
     * thread's {@linkplain java.lang.ThreadGroup thread group} and its
     * subgroups. Recursively iterates over all subgroups in the current
     * thread's thread group.
     *
     * <p> The value returned is only an estimate because the number of
     * threads may change dynamically while this method traverses internal
     * data structures, and might be affected by the presence of certain
     * system threads. This method is intended primarily for debugging
     * and monitoring purposes.
     *
     * @return  an estimate of the number of active threads in the current
     *          thread's thread group and in any other thread group that
     *          has the current thread's thread group as an ancestor
     */
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * Copies into the specified array every active thread in the current
     * thread's thread group and its subgroups. This method simply
     * invokes the {@link java.lang.ThreadGroup#enumerate(Thread[])}
     * method of the current thread's thread group.
     *
     * <p> An application might use the {@linkplain #activeCount activeCount}
     * method to get an estimate of how big the array should be, however
     * <i>if the array is too short to hold all the threads, the extra threads
     * are silently ignored.</i>  If it is critical to obtain every active
     * thread in the current thread's thread group and its subgroups, the
     * invoker should verify that the returned int value is strictly less
     * than the length of {@code tarray}.
     *
     * <p> Due to the inherent race condition in this method, it is recommended
     * that the method only be used for debugging and monitoring purposes.
     *
     * @param  tarray
     *         an array into which to put the list of threads
     * @return  the number of threads put into the array
     * @throws  SecurityException
     *          if {@link java.lang.ThreadGroup#checkAccess} determines that
     *          the current thread cannot access its thread group
     */
    public static int enumerate(Thread tarray[]) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }



    /**
     * 等待最多为millis毫秒，直至这个线程死亡。 0的超时意味着永远等待。
     * <p> 此实现使用this.wait调用的循环，条件为this.isAlive 。
     * 当线程终止时，调用this.notifyAll方法。
     * 建议应用程序不使用wait ， notify ，或notifyAll 的 Thread 实例上。
     *
     * @param  millis 等待时间（以毫秒为单位）
     * @throws  IllegalArgumentException 如果{@code millis}的值为负
     * @throws  InterruptedException 如果有任何线程中断了当前线程，抛出此异常时，线程的中断状态将被清除。
     */
    public final synchronized void join(long millis) throws InterruptedException {
        //记录进入方法的时间
        long base = System.currentTimeMillis();
        long now = 0;
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (millis == 0) {
            //如果线程未死亡，则循环调用 wait
            while (isAlive()) {
                wait(0);
            }
        } else {
            while (isAlive()) {
                //第一次进入，now 为0，等待 millis 毫秒
                //第二次进入，now 为已经等待时间，delay小于等于0时跳出
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

    /**
     * 等待最多millis毫秒加上 nanos 纳秒。
     * <p> 此实现使用this.wait调用的循环，条件为this.isAlive 。
     * 当线程终止时，调用this.notifyAll方法。
     * 建议应用程序不使用wait ， notify ，或notifyAll 在 Thread 的实例上。
     * @param  millis 等待时间（以毫秒为单位）
     * @param  nanos 等待额外的纳秒，取值范围{@code 0-999999}
     * @throws  IllegalArgumentException 如果值 millis是负数，或的值 nanos不在范围 0-999999
     * @throws  InterruptedException 如果任何线程已中断当前线程。 当抛出此异常时，当前线程的中断状态将被清除。
     */
    public final synchronized void join(long millis, int nanos) throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("nanosecond timeout value out of range");
        }
        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }
        join(millis);
    }

    /**
     * 等待这个线程死亡.
     * <p>调用此方法的行为方式与调用 join(0) 完全相同
     * @throws  InterruptedException 如果任何线程中断当前线程。 当抛出此异常时，当前线程的中断状态将被清除。
     */
    public final void join() throws InterruptedException {
        join(0);
    }

    /**
     * Prints a stack trace of the current thread to the standard error stream.
     * This method is used only for debugging.
     *
     * @see     Throwable#printStackTrace()
     */
    public static void dumpStack() {
        new Exception("Stack trace").printStackTrace();
    }

    /**
     * 将此线程标记为{@linkplain #isDaemon 守护线程}或用户线程。
     * 当仅运行的线程都是守护程序线程时，Java虚拟机将退出。
     * <p> 必须在线程启动之前调用此方法
     * @param  on 如果{@code true}，则将该线程标记为守护线程
     * @throws  IllegalThreadStateException 如果此线程是 {@linkplain #isAlive alive}
     * @throws  SecurityException 如果 {@link #checkAccess} 确定当前线程无法修改此线程
     */
    public final void setDaemon(boolean on) {
        checkAccess();
        if (isAlive()) {
            throw new IllegalThreadStateException();
        }
        daemon = on;
    }

    /**
     * @return 如果是守护线程则返回 true, 否则就是 false
     * @see     #setDaemon(boolean)
     */
    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * 确定当前正在运行的线程是否有权限修改此线程。
     * <p>如果有一个安全管理器，它的checkAccess方法被调用这个线程作为它的参数。 这可能会导致 SecurityException 异常。
     *
     * @exception  SecurityException  如果当前线程不允许访问此线程。
     * @see        SecurityManager#checkAccess(Thread)
     */
    public final void checkAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkAccess(this);
        }
    }

    /**
     * Returns a string representation of this thread, including the
     * thread's name, priority, and thread group.
     * @return  a string representation of this thread.
     */
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "Thread[" + getName() + "," + getPriority() + "," +
                           group.getName() + "]";
        } else {
            return "Thread[" + getName() + "," + getPriority() + "," +
                            "" + "]";
        }
    }

    /**
     * Returns the context ClassLoader for this Thread. The context
     * ClassLoader is provided by the creator of the thread for use
     * by code running in this thread when loading classes and resources.
     * If not {@linkplain #setContextClassLoader set}, the default is the
     * ClassLoader context of the parent Thread. The context ClassLoader of the
     * primordial thread is typically set to the class loader used to load the
     * application.
     *
     * <p>If a security manager is present, and the invoker's class loader is not
     * {@code null} and is not the same as or an ancestor of the context class
     * loader, then this method invokes the security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission) checkPermission}
     * method with a {@link RuntimePermission RuntimePermission}{@code
     * ("getClassLoader")} permission to verify that retrieval of the context
     * class loader is permitted.
     *
     * @return  the context ClassLoader for this Thread, or {@code null}
     *          indicating the system class loader (or, failing that, the
     *          bootstrap class loader)
     * @throws  SecurityException
     *          if the current thread cannot get the context ClassLoader
     * @since 1.2
     */
    @CallerSensitive
    public ClassLoader getContextClassLoader() {
        if (contextClassLoader == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(contextClassLoader,
                                                   Reflection.getCallerClass());
        }
        return contextClassLoader;
    }

    /**
     * Sets the context ClassLoader for this Thread. The context
     * ClassLoader can be set when a thread is created, and allows
     * the creator of the thread to provide the appropriate class loader,
     * through {@code getContextClassLoader}, to code running in the thread
     * when loading classes and resources.
     *
     * <p>If a security manager is present, its {@link
     * SecurityManager#checkPermission(java.security.Permission) checkPermission}
     * method is invoked with a {@link RuntimePermission RuntimePermission}{@code
     * ("setContextClassLoader")} permission to see if setting the context
     * ClassLoader is permitted.
     *
     * @param  cl
     *         the context ClassLoader for this Thread, or null  indicating the
     *         system class loader (or, failing that, the bootstrap class loader)
     * @throws  SecurityException
     *          if the current thread cannot set the context ClassLoader
     * @since 1.2
     */
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader = cl;
    }

    private static final StackTraceElement[] EMPTY_STACK_TRACE
        = new StackTraceElement[0];

    /**
     * 返回表示此线程的堆栈转储的堆栈跟踪元素数组。
     * 该方法将返回一个零长度的数组，如果该线程尚未启动，已启动但尚未被计划运行，或已终止。
     * 如果返回的数组非零长度，则数组的第一个元素表示堆栈的顶部，这是序列中最近的方法调用。
     * 数组的最后一个元素表示堆栈的底部，这是序列中最近最少的方法调用。
     * <p>如果有一个安全管理器，并且这个线程不是当前的线程，
     * 那么安全管理器的checkPermission方法被调用一个RuntimePermission("getStackTrace")权限来查看是否可以获取堆栈跟踪。
     * <p>在某些情况下，某些虚拟机可能从堆栈跟踪中省略一个或多个堆栈帧。
     * 在极端情况下，允许没有关于该线程的堆栈跟踪信息的虚拟机从该方法返回零长度数组。
     *
     * @return 一个 StackTraceElement的数组，每个代表一个堆栈帧。
     * @throws SecurityException 如果安全管理器存在，并且其 checkPermission方法不允许获取线程的堆栈跟踪。
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     * @since 1.5
     */
    public StackTraceElement[] getStackTrace() {
        if (this != Thread.currentThread()) {
            // check for getStackTrace permission
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(
                    SecurityConstants.GET_STACK_TRACE_PERMISSION);
            }
            // optimization so we do not call into the vm for threads that
            // have not yet started or have terminated
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
            StackTraceElement[][] stackTraceArray = dumpThreads(new Thread[] {this});
            StackTraceElement[] stackTrace = stackTraceArray[0];
            // a thread that was alive during the previous isAlive call may have
            // since terminated, therefore not having a stacktrace.
            if (stackTrace == null) {
                stackTrace = EMPTY_STACK_TRACE;
            }
            return stackTrace;
        } else {
            // Don't need JVM help for current thread
            return (new Exception()).getStackTrace();
        }
    }

    /**
     * 返回所有活动线程的堆栈跟踪图。
     * map 键是线程，每个 map 值是StackTraceElement数组，表示对应的Thread的堆栈转储。 返回的堆栈跟踪格式为getStackTrace方法指定的格式。
     * <p>线程可能正在执行，而此方法被调用。
     * 每个线程的堆栈跟踪仅表示快照，并且可以在不同时间获取每个堆栈跟踪。
     * 如果虚拟机没有关于线程的堆栈跟踪信息，则将在地图值中返回零长度的数组。
     * <p>如果有一个安全管理员，那么安全管理员的checkPermission方法被调用一个RuntimePermission("getStackTrace")权限
     * 以及RuntimePermission("modifyThreadGroup")权限来查看是否可以获取所有线程的堆栈跟踪。
     *
     * @return 一个 Map从 Thread到一个 StackTraceElement的数组， 代表相应线程的堆栈跟踪。
     * @throws SecurityException 如果安全管理器存在，并且其 checkPermission方法不允许获取线程的堆栈跟踪。
     * @see #getStackTrace
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // check for getStackTrace permission
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // Get a snapshot of the list of all threads
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m = new HashMap<>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            StackTraceElement[] stackTrace = traces[i];
            if (stackTrace != null) {
                m.put(threads[i], stackTrace);
            }
            // else terminated so we don't put it in the map
        }
        return m;
    }


    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION =
                    new RuntimePermission("enableContextClassLoaderOverride");

    /** cache of subclass security audit results */
    /* Replace with ConcurrentReferenceHashMap when/if it appears in a future
     * release */
    private static class Caches {
        /** cache of subclass security audit results */
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
            new ConcurrentHashMap<>();

        /** queue for WeakReferences to audited subclasses */
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
            new ReferenceQueue<>();
    }

    /**
     * Verifies that this (possibly subclass) instance can be constructed
     * without violating security constraints: the subclass must not override
     * security-sensitive non-final methods, or else the
     * "enableContextClassLoaderOverride" RuntimePermission is checked.
     */
    private static boolean isCCLOverridden(Class<?> cl) {
        if (cl == Thread.class)
            return false;

        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }

        return result.booleanValue();
    }

    /**
     * Performs reflective checks on given subclass to verify that it doesn't
     * override security-sensitive non-final methods.  Returns true if the
     * subclass overrides any of the methods, false otherwise.
     */
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    for (Class<?> cl = subcl;
                         cl != Thread.class;
                         cl = cl.getSuperclass())
                    {
                        try {
                            cl.getDeclaredMethod("getContextClassLoader", new Class<?>[0]);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {
                        }
                        try {
                            Class<?>[] params = {ClassLoader.class};
                            cl.getDeclaredMethod("setContextClassLoader", params);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException ex) {
                        }
                    }
                    return Boolean.FALSE;
                }
            }
        );
        return result.booleanValue();
    }


    /**
     * 返回此线程的 ID标识符.
     * 线程ID 是一个私有的 long 类型数字，线程创建的时候生成。
     * 线程ID是唯一的，并且在其生命周期内保持不变.
     * 当线程终止时，该线程ID可以重用.
     * @return 返回线程 ID
     * @since 1.5
     */
    public long getId() {
        return tid;
    }

    /**
     * 线程状态枚举
     * @since   1.5
     */
    public enum State {
        /**
         * 尚未启动的线程的线程状态
         */
        NEW,
        /**
         * 可运行线程的线程状态。处于可运行状态的线程正在Java虚拟机中执行，但可能正在等待来自操作系统的其他资源，例如处理器。
         */
        RUNNABLE,
        /**
         * 线程的线程状态被阻塞，等待监视器锁定。
         * 处于阻塞状态的线程正在等待监视器锁定输入同步块/方法或调用 {@link Object＃wait（）Object.wait}后重新输入同步块/方法。
         */
        BLOCKED,
        /**
         * 等待线程的线程状态。
         * 由于调用以下方法之一，线程处于等待状态:
         * <ul>
         *   <li>{@link Object#wait() Object.wait} with no timeout</li>
         *   <li>{@link #join() Thread.join} with no timeout</li>
         *   <li>{@link LockSupport#park() LockSupport.park}</li>
         * </ul>
         * <p>处于等待状态的线程正在等待另一个线程执行特定操作。
         *
         * 例如，在某个对象上调用了 Object.wait（）的线程正在等待另一个线程调用 Object.notify（）或 Object。
         * 该对象上的notifyAll（）。名为 Thread.join（）的线程正在等待指定的线程终止。
         */
        WAITING,
        /**
         * 具有指定等待时间的等待线程的线程状态。
         * 由于以指定的正等待时间调用以下方法之一，因此线程处于定时等待状态:
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
         *   <li>{@link #join(long) Thread.join} with timeout</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         * </ul>
         */
        TIMED_WAITING,
        /**
         * 终止线程的线程状态。
         * 线程已完成执行。
         */
        TERMINATED;
    }

    /**
     * 返回此线程的状态。 此方法设计用于监视系统状态，不用于同步控制。
     * @return 返回此线程的状态
     * @since 1.5
     */
    public State getState() {
        return sun.misc.VM.toThreadState(threadStatus);
    }

    // Added in JSR-166

    /**
     * 当Thread由于未捕获的异常而突然终止时，处理程序的接口被调用。
     * <p>当一个线程要终止由于未捕获到异常的Java虚拟机将使用查询线程其UncaughtExceptionHandler Thread.getUncaughtExceptionHandler() ，
     * 将调用处理程序的uncaughtException方法，将线程和异常作为参数。
     *
     * 如果一个线程一直没有其UncaughtExceptionHandler明确设置，
     * 那么它ThreadGroup对象充当其UncaughtExceptionHandler。
     *
     * 如果ThreadGroup对象没有处理异常的特殊要求，则可以将调用转发到default uncaught exception handler 。
     *
     * @see #setDefaultUncaughtExceptionHandler
     * @see #setUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        /**
         * Method invoked when the given thread terminates due to the
         * given uncaught exception.
         * <p>Any exception thrown by this method will be ignored by the
         * Java Virtual Machine.
         * @param t the thread
         * @param e the exception
         */
        void uncaughtException(Thread t, Throwable e);
    }

    // null unless explicitly set
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    // null unless explicitly set
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    /**
     * 设置当线程由于未捕获的异常突然终止而调用的默认处理程序，并且没有为该线程定义其他处理程序。
     * <p>未捕获的异常处理首先由线程控制，然后由线程的ThreadGroup对象控制，最后由默认的未捕获异常处理程序控制。
     * 如果线程没有明确的未捕获异常处理程序集，并且线程的线程组（包括父线程组）没有专门化其uncaughtException方法，
     * 那么默认处理程序的uncaughtException方法将被调用。
     * <p>通过设置默认未捕获的异常处理程序，应用程序可以更改未被捕获的异常处理方式（例如，记录到特定设备或文件），
     * 这些线程将已经接受了系统提供的任何“默认”行为。
     * <p>请注意，默认未捕获的异常处理程序通常不会延迟到线程的ThreadGroup对象，因为这可能会导致无限递归。
     *
     * @param eh 用作默认未捕获异常处理程序的对象。 如果null那么没有默认处理程序。
     * @throws SecurityException 如果安全管理器存在，并且否认 RuntimePermission ("setDefaultUncaughtExceptionHandler")
     *
     * @see #setUncaughtExceptionHandler
     * @see #getUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(
                new RuntimePermission("setDefaultUncaughtExceptionHandler")
                    );
        }
         defaultUncaughtExceptionHandler = eh;
     }

    /**
     * 返回当线程由于未捕获异常突然终止而调用的默认处理程序。 如果返回值为null ，则没有默认值。
     * @since 1.5
     * @see #setDefaultUncaughtExceptionHandler
     * @return 所有线程的默认未捕获的异常处理程序
     */
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
        return defaultUncaughtExceptionHandler;
    }

    /**
     * 返回由于未捕获的异常，此线程突然终止时调用的处理程序。
     * 如果此线程没有明确设置未捕获的异常处理程序，则返回此线程的ThreadGroup对象，除非此线程已终止，否则返回null 。
     * @since 1.5
     * @return 该线程的未捕获的异常处理程序
     */
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler != null ? uncaughtExceptionHandler : group;
    }

    /**
     * 设置当该线程由于未捕获的异常而突然终止时调用的处理程序。
     * <p>线程可以完全控制如何通过明确设置其未捕获的异常处理来响应未捕获的异常。
     * 如果没有设置这样的处理程序，那么线程的ThreadGroup对象将作为其处理程序。
     * @param eh 用作此线程未捕获的异常处理程序的对象。 如果null那么这个线程没有明确的处理程序。
     * @throws  SecurityException  如果当前线程不允许修改此线程
     * @see #setDefaultUncaughtExceptionHandler
     * @see ThreadGroup#uncaughtException
     * @since 1.5
     */
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        checkAccess();
        uncaughtExceptionHandler = eh;
    }

    /**
     * Dispatch an uncaught exception to the handler. This method is intended to be called only by the JVM.
     */
    private void dispatchUncaughtException(Throwable e) {
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    /**
     * Removes from the specified map any keys that have been enqueued on the specified reference queue.
     */
    static void processQueue(ReferenceQueue<Class<?>> queue, ConcurrentMap<? extends WeakReference<Class<?>>, ?> map)
    {
        Reference<? extends Class<?>> ref;
        while((ref = queue.poll()) != null) {
            map.remove(ref);
        }
    }

    /**
     *  Weak key for Class objects.
     **/
    static class WeakClassKey extends WeakReference<Class<?>> {
        /**
         * 引用者身份哈希码的保存值，以在清除引用者后保持一致的哈希码
         */
        private final int hash;
        /**
         * 为给定对象创建一个新的WeakClassKey，并在队列中注册。
         */
        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }
        /**
         * 返回原始参考对象的身份哈希码。
         */
        @Override
        public int hashCode() {
            return hash;
        }
        /**
         * 如果给定的对象是相同的WeakClassKey实例，则返回true；
         * 或者，如果尚未清除此对象的引用，则如果给定的对象是另一个WeakClassKey实例，且具有与此对象相同的非null引用，则返回true。
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&
                       (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }


    /**
     * 使当前正在执行的线程进入休眠状态（暂时停止执行），以毫秒为单位，取决于系统定时器和调度程序的精度和准确性。
     * 并且线程不会丢失监视器锁。
     * @param  millis 睡眠时间（以毫秒为单位）
     * @param  nanos {@code 0-999999} 额外的纳秒睡眠
     * @throws  IllegalArgumentException 如果{@code millis}的值为负，或者{@code nanos}的值不在{@code 0-999999}范围内
     * @throws  InterruptedException 如果有任何线程中断了当前线程。抛出此异常时，将清除当前线程的中断状态。
     */
    public static void sleep(long millis, int nanos) throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                    "nanosecond timeout value out of range");
        }
        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }
        sleep(millis);
    }

    // 以下三个最初未初始化的字段专门由类java.util.concurrent.ThreadLocalRandom管理。
    // 这些字段用于在并发代码中构建高性能PRNG，因此我们不会冒意外的错误共享的风险。
    // 因此，使用@Contended隔离字段。

    /** ThreadLocalRandom的当前种子 */
    @sun.misc.Contended("tlr")
    long threadLocalRandomSeed;

    /** Probe hash value; nonzero if threadLocalRandomSeed initialized */
    @sun.misc.Contended("tlr")
    int threadLocalRandomProbe;

    /** Secondary seed isolated from public ThreadLocalRandom sequence */
    @sun.misc.Contended("tlr")
    int threadLocalRandomSecondarySeed;

    //****************************************************************************************
    //                                   native 方法汇总
    //****************************************************************************************

    /**
     * @return  返回对当前正在执行的线程对象的引用。
     */
    public static native Thread currentThread();
    /**
     * @return  测试此线程是否仍然存在。如果一个线程已经启动并且尚未死亡，则该线程是活动的。
     */
    public final native boolean isAlive();
    /**
     * 使当前正在执行的线程进入休眠状态（暂时停止执行），以毫秒为单位，取决于系统定时器和调度程序的精度和准确性。
     * 并且线程不会丢失监视器锁。
     * @param  millis 睡眠时间（以毫秒为单位）
     * @throws  IllegalArgumentException 如果{@code millis}的值为负
     * @throws  InterruptedException 如果有任何线程中断了当前线程。抛出此异常时，将清除当前线程的中断状态。
     */
    public static native void sleep(long millis) throws InterruptedException;
    /**
     * 向处理器提出建议，当前线程愿意让出CPU给其他线程。处理器也可以忽略此提示。
     * <p>Yield 是一种启发式尝试，旨在提高线程之间的相对进展，否则将过度利用CPU。
     * 应将其使用与详细的性能分析和基准测试结合起来，以确保它实际上具有所需的效果。
     * <p>这是一个很少使用的方法。它可能在调试或者测试的时候，或者设计并发控制程序的时候很有用。
     */
    public static native void yield();
    /**
     * 如果当前线程持有指定锁，则返回true
     * <p>此方法旨在允许程序断言当前线程已持有指定的锁：
     * <pre>
     *     assert Thread.holdsLock(obj);
     * </pre>
     * @param  obj 测试锁所有权的对象
     * @throws NullPointerException 如果obj为 null
     * @return 如果当前线程持有指定锁，则返回true
     * @since 1.4
     */
    public static native boolean holdsLock(Object obj);

    /**
     * 测试某些线程是否已被中断。线程的中断状态不受此方法的影响。
     * ClearInterrupted参数决定线程中断状态是否被重置。true则重置。
     */
    private native boolean isInterrupted(boolean ClearInterrupted);


    private native void start0();//启动线程
    private native void setPriority0(int newPriority);// 设置优先级
    private native void interrupt0(); //中断线程
    private native void setNativeName(String name);// 设置线程名
    private native static StackTraceElement[][] dumpThreads(Thread[] threads);
    private native static Thread[] getThreads();

    private native void stop0(Object o);//停止线程，已过时
    private native void suspend0();//挂起线程，已过时
    private native void resume0();//恢复挂起的线程，已过时

    //****************************************************************************************
    //                                   已过时方法
    //****************************************************************************************

    /**
     * 强制线程停止执行。
     * <p>
     * 如果安装了一个安全管理器，它的checkAccess方法this作为参数。 这可能导致SecurityException被提升（在当前线程中）。
     * <p>
     * 如果此线程与当前线程不同（即当前线程正试图停止除本身线程之外的线程），
     * 则另外还调用安全管理器的checkPermission方法（具有RuntimePermission("stopThread")参数）。
     * 再次，这可能会导致抛出SecurityException （在当前线程中）。
     * <p>
     * 由该线程表示的线程被强制停止，它正在异常进行，并抛出一个新创建的ThreadDeath对象作为例外。
     * <p>
     * 允许停止尚未启动的线程。 如果线程最终启动，它将立即终止。
     * <p>
     * 一个应用程序通常不应该尝试捕获ThreadDeath ，除非它必须做一些非凡的清理操作（请注意，抛出ThreadDeath导致finally语句try语句在线程正式死亡之前执行）。
     * 如果一个catch子句捕获一个ThreadDeath对象，重要的是重新抛出该对象，使线程实际上死亡。
     * <p>
     * 该反应否则捕获的异常不打印出消息，或者如果未捕获的异常是一个实例，否则通知应用程序的顶级错误处理程序ThreadDeath 。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @deprecated 这种方法本质上是不安全的。
     *             使用Thread.stop停止线程可以解锁所有已锁定的监视器（由于未ThreadDeath ThreadDeath异常在堆栈中ThreadDeath的自然结果）。
     *             如果先前受这些监视器保护的任何对象处于不一致的状态，则损坏的对象将变得对其他线程可见，可能导致任意行为。
     *             stop许多用途应该被代替，只需修改一些变量来指示目标线程应该停止运行。
     *             目标线程应该定期检查此变量，如果变量表示要停止运行，则以有序方式从其运行方法返回。
     *             如果目标线程长时间等待（例如，在interrupt变量上），则应该使用interrupt方法来中断等待。
     */
    @Deprecated
    public final void stop() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            checkAccess();
            if (this != Thread.currentThread()) {
                security.checkPermission(SecurityConstants.STOP_THREAD_PERMISSION);
            }
        }
        // 零状态值对应于“ NEW”，它不能更改为
        // 不是新的，因为我们持有锁。
        if (threadStatus != 0) {
            resume(); //唤醒线程（如果已暂停）；否，否则
        }
        // VM可以处理所有线程状态
        stop0(new ThreadDeath());
    }

    /**
     * Throws {@code UnsupportedOperationException}.
     * @param obj ignored
     * @deprecated 该方法最初设计为强制线程停止并抛出一个给定的Throwable作为例外。
     *             它本质上是不安全的（详见stop() ），此外还可用于生成目标线程未准备处理的异常
     */
    @Deprecated
    public final synchronized void stop(Throwable obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 抛出异常 {@link NoSuchMethodError}.
     * @deprecated 这种方法最初是为了销毁这个线程而没有任何清理。
     *             它所持有的任何监视器都将保持锁定。但是，该方法从未实现。
     *             如果要实施，那么suspend()的方式会是僵死的 。
     *             如果目标线程在销毁时保护关键系统资源的锁，则无法再次访问该资源。
     *             如果另一个线程曾尝试锁定此资源，将导致死锁。
     *             这种僵局通常表现为“冻结”过程。
     * @throws NoSuchMethodError always
     */
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError();
    }

    /**
     * 挂起线程
     * <p>
     * 首先，这个线程的checkAccess方法被调用，没有参数。 这可能会导致SecurityException （在当前线程中）。
     * <p>
     * 如果线程活着，它将被暂停，并且不会进一步进行，除非和直到恢复。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @deprecated 这种方法已被弃用，因为它本身就是死锁的。
     *             如果目标线程在挂起时保护关键系统资源的监视器上的锁定，则在目标线程恢复之前，线程不能访问该资源。
     *             如果要恢复目标线程的线程在调用resume之前尝试锁定此监视器， resume导致死锁。
     *             这种僵局通常表现为“冻结”过程。
     */
    @Deprecated
    public final void suspend() {
        checkAccess();
        suspend0();
    }

    /**
     * 恢复挂起的线程。
     * <p>
     * 首先，这个线程的checkAccess方法被调用，没有参数。 这可能会导致SecurityException （在当前线程中）。
     * <p>
     * 如果线程存活但被暂停，则它被恢复并被允许在其执行中取得进展。
     * @exception  SecurityException  如果当前线程不能修改此线程。
     * @deprecated 此方法仅适用于suspend() ，由于它是死锁倾向，因此已被弃用。
     */
    @Deprecated
    public final void resume() {
        checkAccess();
        resume0();
    }

    /**
     * @return 此线程中的堆栈帧数。
     * @exception  IllegalThreadStateException  如果此线程未挂起。
     * @deprecated 此调用的定义取决于suspend() ，它被废弃了。 此外，此呼叫的结果从未明确。
     *             计算此线程中的堆栈帧数。 线程必须暂停。
     */
    @Deprecated
    public native int countStackFrames();


}
