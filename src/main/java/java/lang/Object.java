package java.lang;

/**
 * 类 {@code Object} 是类层次结构的根。
 * 每个类都有{@code Object}作为超类。
 * 所有对象，包括数组，实现了这个类的方法。
 * @see     java.lang.Class
 * @since   JDK1.0
 */
public class Object {

    private static native void registerNatives();
    static {
        registerNatives();
    }

    /**
     * 返回此Object的运行时类。
     * 返回的类对象是被表示类的static synchronized方法锁定的对象。
     * <p><b>实际返回结果类型是 {@code Class<? extends |X|>}，其中 {@code |X|} 是静态类型上被调用的 Class</b>
     * 举个栗子，在此代码中不需要转换：
     * <p>
     * {@code Number n = 0;                             }<br>
     * {@code Class<? extends Number> c = n.getClass(); }
     * </p>
     *
     * @return 表示类对象的运行时类的Class对象。
     */
    public final native Class<?> getClass();

    /**
     * 返回对象的哈希码值.
     * 支持这种方法是为了散列表，如HashMap提供的那样 。
     * <p> hashCode 一般有以下约定：
     * <ul>
     * <li>只要在执行Java应用程序时多次在同一个对象上调用该方法， hashCode方法必须始终返回相同的整数，前提是修改了对象中equals比较中的信息。
     *     该整数不需要从一个应用程序的执行到相同应用程序的另一个执行保持一致。
     * <li>如果根据equals(Object)方法两个对象相等，则在两个对象中的每个对象上调用hashCode方法必须产生相同的整数结果。
     * <li>不要求如果两个对象根据equals(java.lang.Object)方法不相等，那么在两个对象中的每个对象上调用hashCode方法必须产生不同的整数结果。
     *     但是，程序员应该意识到，为不等对象生成不同的整数结果可能会提高哈希表的性能。
     * </ul>
     * <p>
     * 尽可能多的合理实用，由类别Object定义的hashCode方法确实为不同对象返回不同的整数。
     * （这通常通过将对象的内部地址转换为整数来实现，但Java的编程语言不需要此实现技术。）
     * @return  此对象的哈希码值.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.lang.System#identityHashCode
     */
    public native int hashCode();

    /**
     * 指示一些其他对象是否等于此。
     * <p>equals方法在非空对象引用上实现等价关系：
     * <ul>
     * <li>自反性 ：对于任何非空的参考值x ， x.equals(x)应该返回true 。
     * <li>对称性 ：对于任何非空引用值x和y ， x.equals(y)应该返回true当且仅当y.equals(x)回报true 。
     * <li>传递性 ：对于任何非空引用值x ， y和z ，如果x.equals(y)回报true个y.equals(z)回报true ，然后x.equals(z)应该返回true 。
     * <li>一致性 ：对于任何非空引用值x和y ，多次调用x.equals(y)始终返回true或始终返回false ，没有设置中使用的信息equals比较上的对象被修改。
     * <li>对于任何非空的参考值x ， x.equals(null)应该返回false 。
     * </ul>
     * <p>该equals类方法Object实现对象上差别可能性最大的相等关系;
     * 也就是说，对于任何非空的参考值x和y ，当且仅当x和y引用相同的对象（ x == y具有值true ）时，该方法返回true 。
     * <p>请注意，无论何时覆盖该方法，通常需要覆盖hashCode方法，以便维护hashCode方法的通用合同，该方法规定相等的对象必须具有相等的哈希码。
     *
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     * @see     #hashCode()
     * @see     java.util.HashMap
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * 创建并返回此对象的副本。 “复制”的精确含义可能取决于对象的类。 一般的意图是，对于任何对象x ，表达式：
     * <blockquote><pre>x.clone() != x</pre></blockquote>
     * 将是真实的，而且表达：
     * <blockquote><pre>x.clone().getClass() == x.getClass()</pre></blockquote>
     * 将是true ，但这些都不是绝对的要求。 通常情况是：
     * <blockquote><pre>x.clone().equals(x)</pre></blockquote>
     * 将是true ，这不是一个绝对的要求。
     * <p>按照惯例，返回的对象应该通过调用super.clone获得。
     * 如果一个类和它的所有超类（除了Object ）遵守这个惯例，那将是x.clone().getClass() == x.getClass()的情况。
     * <p>按照惯例，此方法返回的对象应该与此对象（正被克隆）无关。
     * 为了实现这一独立性，可能需要修改super.clone返回的对象的一个或多个字段。
     * 通常，这意味着复制构成被克隆的对象的内部“深层结构”的任何可变对象，并通过引用该副本替换对这些对象的引用。
     * 如果一个类仅包含原始字段或对不可变对象的引用，则通常情况下， super.clone返回的对象中的字段通常不需要修改。
     * <p>clone的方法Object执行特定的克隆操作。
     * 首先，如果此对象的类不实现接口Cloneable ，则抛出CloneNotSupportedException 。
     * 请注意，所有数组都被认为是实现接口Cloneable ，并且数组类型T[]的clone方法的返回类型是T[] ，其中T是任何引用或原始类型。
     * 否则，该方法将创建该对象的类的新实例，并将其所有字段初始化为完全符合该对象的相应字段的内容，就像通过赋值一样。 这些字段的内容本身不被克隆。
     * 因此，该方法执行该对象的“浅拷贝”，而不是“深度拷贝”操作。
     * <p>Object类本身并不实现接口Cloneable ，因此在类别为Object的对象上调用clone方法将导致运行时抛出异常。
     *
     * @return 这个实例的一个克隆。
     * @throws  CloneNotSupportedException  如果对象的类不支持Cloneable接口。 覆盖clone方法的子类也可以抛出此异常以指示实例无法克隆。
     * @see java.lang.Cloneable
     */
    protected native Object clone() throws CloneNotSupportedException;

    /**
     * 返回对象的字符串表示形式。
     * 一般来说， toString方法返回一个“textually代表”这个对象的字符串。
     * 结果应该是一个简明扼要的表达，容易让人阅读。
     * 建议所有子类都重写此方法。
     * <p>该toString类方法Object返回一个由其中的对象是一个实例，该符号字符`的类的名称的字符串@ ”和对象的哈希码的无符号的十六进制表示。
     * 换句话说，这个方法返回一个等于下列值的字符串：
     * <blockquote> <pre>
     *      getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     * @return  对象的字符串表示形式。
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * 唤醒正在等待对象监视器的单个线程。
     * 如果任何线程正在等待这个对象，其中一个被选择被唤醒。
     * 选择是任意的，并且由实施器判断发生。
     * 线程通过调用wait方法之一等待对象的监视器。
     *
     * <p>唤醒的线程将无法继续，直到当前线程放弃此对象上的锁定为止。
     * 唤醒的线程将以通常的方式与任何其他线程竞争，这些线程可能正在积极地竞争在该对象上进行同步;
     * 例如，唤醒的线程在下一个锁定该对象的线程中没有可靠的权限或缺点。
     *
     * <p>该方法只能由作为该对象的监视器的所有者的线程调用。 线程以三种方式之一成为对象监视器的所有者：
     * <ul>
     * <li>通过执行该对象的同步实例方法。
     * <li>通过执行在对象上synchronized synchronized语句的正文。
     * <li>对于类型为Class,的对象，通过执行该类的同步静态方法。
     * </ul>
     * <p>一次只能有一个线程可以拥有一个对象的显示器。
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的监视器的所有者
     */
    public final native void notify();

    /**
     * 唤醒正在等待对象监视器的所有线程。 线程通过调用wait方法之一等待对象的监视器。
     * <p>唤醒的线程将无法继续，直到当前线程释放该对象上的锁。
     * 唤醒的线程将以通常的方式与任何其他线程竞争，这些线程可能正在积极地竞争在该对象上进行同步;
     * 例如，唤醒的线程在下一个锁定该对象的线程中不会有可靠的特权或缺点。
     * <p>该方法只能由作为该对象的监视器的所有者的线程调用。
     * 有关线程可以成为监视器所有者的方法的说明，请参阅notify方法。
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的监视器的所有者
     */
    public final native void notifyAll();

    /**
     * 导致当前线程等待，直到另一个线程调用此对象的notify()方法或notifyAll()方法，或指定的时间已过。
     * <p>当前线程必须持有本身对象监视器。
     * <p>此方法使当前线程（称为T）将自身置入等待 set 集合中，然后放弃该对象的所有同步声明。
     *之后线程 T 无法成为线程调度的目标，并且休眠，直到发生四件事情之一：
     * <ul>
     * <li>一些其他线程调用该对象的notify方法，并且线程T恰好被任意选择为被唤醒的线程。
     * <li>某些其他线程调用此对象的notifyAll方法。
     * <li>一些其他线程interrupts线程T。
     * <li>指定的实时数量已经过去，或多或少。 然而，如果timeout为零，则不考虑实时，线程等待直到通知。
     * </ul>
     * 然后从该对象的等待set集合中删除线程T ，并重新启用线程调度。
     * 然后它以通常的方式与其他线程竞争在对象上进行同步的权限;
     * 一旦获得了对象的控制，其对对象的所有同步声明就恢复到现状，也就是在调用wait方法之后的情况。
     * 线程T然后从调用wait方法返回。 因此，从返回wait方法，对象和线程的同步状态T正是因为它是当wait被调用的方法。
     *
     * <p>线程也可以唤醒，而不会被通知，中断或超时，即所谓的虚假唤醒 。
     * 虽然这在实践中很少会发生，但应用程序必须通过测试应该使线程被唤醒的条件来防范，并且如果条件不满足则继续等待。
     * 换句话说，等待应该总是出现在循环中，就像这样：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;condition does not hold&gt;)
     *             obj.wait(timeout);
     *         ... // Perform action appropriate to condition
     *     }
     * </pre>
     * <p>如果当前线程interrupted任何线程之前或在等待时，那么InterruptedException被抛出。
     * 如上所述，在该对象的锁定状态已恢复之前，不会抛出此异常。
     * <p>请注意， wait方法，因为它将当前线程放入该对象的等待集，仅解锁此对象;
     * 当前线程可以同步的任何其他对象在线程等待时保持锁定。
     * <p>该方法只能由作为该对象的监视器的所有者的线程调用。
     * 有关线程可以成为监视器所有者的方法的说明，请参阅notify方法。
     *
     * @param      timeout   等待的最长时间（以毫秒为单位）。
     * @throws  IllegalArgumentException      如果timeout值为负。
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的监视器的所有者
     * @throws  InterruptedException 如果任何线程在当前线程等待通知之前或当前线程中断当前线程。 当抛出此异常时，当前线程的中断状态将被清除。
     */
    public final native void wait(long timeout) throws InterruptedException;

    /**
     * 导致当前线程等待，直到另一个线程调用此对象的notify()方法或notifyAll()方法，或其他一些线程中断当前线程，或者等待时间已经过去。
     * <p>这种方法类似于一个参数的wait方法，但它允许对放弃之前等待通知的时间进行更精细的控制。 以纳秒为单位的实时数量由下式给出：
     * <blockquote><pre> 1000000*timeout+nanos </pre></blockquote>
     * <p>在所有其他方面，该方法与一个参数的方法wait(long)相同。
     * 特别是， wait(0, 0)意味着同样的事情wait(0) 。
     * <p>当前的线程必须拥有该对象的显示器。 线程释放此监视器的所有权，并等待直到发生以下两种情况之一：
     * <ul>
     * <li>另一个线程通知等待该对象的监视器的线程通过调用notify方法或notifyAll方法来唤醒。
     * <li>由timeout毫秒加nanos纳秒参数指定的超时时间已过。
     * </ul>
     * <p>然后线程等待，直到它可以重新获得监视器的所有权并恢复执行。
     * <p>像在一个参数版本中，中断和虚假唤醒是可能的，并且该方法应该始终在循环中使用：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;condition does not hold&gt;)
     *             obj.wait(timeout, nanos);
     *         ... // Perform action appropriate to condition
     *     }
     * </pre>
     * 该方法只能由作为该对象的监视器的所有者的线程调用。 有关线程可以成为监视器所有者的方式的说明，请参阅notify方法。
     *
     * @param      timeout   等待的最长时间（以毫秒为单位）。
     * @param      nanos      额外时间，以纳秒为单位，范围为0-999999。
     * @throws  IllegalArgumentException      如果 timeout 值为负或 nanos的值不在0-999999范围内。
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的监视器的所有者
     * @throws  InterruptedException  如果任何线程在当前线程等待通知之前或当前线程中断当前线程。 当抛出此异常时，当前线程的中断状态将被清除。
     */
    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException("nanosecond timeout value out of range");
        }
        if (nanos >= 500000 || (nanos != 0 && timeout == 0)) {
            timeout++;
        }
        wait(timeout);
    }

    /**
     * 使当前线程等待，直到另一个线程调用该对象的notify()方法或notifyAll()方法。
     * 换句话说，这个方法的行为就好像简单地执行调用 wait(0) 。
     * <p>当前线程必须持有对象监视器。
     * 该线程释放此监视器的所有权，并等待另一个线程通知等待该对象监视器的线程，通过调用notify方法或notifyAll方法notifyAll。
     * 然后线程等待，直到它可以重新获得监视器的所有权并恢复执行。
     * <p>像在一个参数版本中，中断和虚假唤醒是可能的，并且该方法应该始终在循环中使用：
     * <pre>
     *     synchronized (obj) {
     *         while (&lt;condition does not hold&gt;)
     *             obj.wait();
     *         ... // Perform action appropriate to condition
     *     }
     * </pre>
     * 该方法只能由作为该对象的监视器的所有者的线程调用。
     * 有关线程可以成为监视器所有者的方式的说明，请参阅notify方法。
     *
     * @throws  IllegalMonitorStateException  如果当前线程不是此对象的监视器的所有者
     * @throws  InterruptedException 如果任何线程在当前线程等待通知之前或当前线程中断当前线程。 当抛出此异常时，当前线程的中断状态将被清除。
     */
    public final void wait() throws InterruptedException {
        wait(0);
    }

    /**
     * 当垃圾收集确定不再有对该对象的引用时，垃圾收集器在对象上调用该对象。 一个子类覆盖了处理系统资源或执行其他清理的finalize方法。
     * <p>finalize的一般合同是，如果Java虚拟机已经确定不再有任何方法可以被任何尚未死亡的线程访问的方法被调用，除非是由于最后确定的其他对象或类的准备工作所采取的行动。
     * finalize方法可以采取任何行动，包括使此对象再次可用于其他线程; 然而， finalize的通常目的是在对象不可撤销地丢弃之前执行清除动作。
     * 例如，表示输入/输出连接的对象的finalize方法可能会在对象被永久丢弃之前执行显式I / O事务来中断连接。
     * <p>所述finalize类的方法Object执行任何特殊操作; 它只是返回正常。 Object的Object可以覆盖此定义。
     * <p>Java编程语言不能保证哪个线程将为任何给定的对象调用finalize方法。
     * 但是，确保调用finalize的线程在调用finalize时不会持有任何用户可见的同步锁。
     * 如果finalize方法抛出未捕获的异常，则会忽略该异常，并终止该对象的定类。
     * <p>在为对象调用finalize方法之后，在Java虚拟机再次确定不再有任何方式可以通过任何尚未被死亡的线程访问此对象的任何方法的情况下，将采取进一步的操作，
     * 包括可能的操作由准备完成的其他对象或类别，此时可以丢弃对象。
     * <p>finalize方法从不被任何给定对象的Java虚拟机调用多次。
     * <p>finalize方法抛出的任何异常都会导致该对象的终止被停止，否则被忽略。
     *
     * @throws Throwable 这个方法提出的异常
     * @see java.lang.ref.WeakReference
     * @see java.lang.ref.PhantomReference
     */
    protected void finalize() throws Throwable { }
}
