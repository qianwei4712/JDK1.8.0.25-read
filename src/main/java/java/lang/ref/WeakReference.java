package java.lang.ref;


/**
 * 弱参考对象，不会阻止其指定对象的最终确定，最终确定，然后被回收。 弱引用最常用于实现规范化映射。
 * 假设垃圾收集器在某个时间点确定对象是weakly reachable 。
 * 那时，它将原子地清除对该对象的所有弱引用，以及所有弱引用到任何其他弱可触及的对象，通过一连串强软引用可以从该对象到达该对象。
 * 同时，它将声明所有以前弱可触及的对象都是可以确定的。 在同一时间或稍后的时间，它将排列在引用队列中注册的新清除的弱引用。
 *
 * @author   Mark Reinhold
 * @since    1.2
 */
public class WeakReference<T> extends Reference<T> {

    /**
     * 创建引用给定对象的新的弱引用。
     * 新引用未注册到任何队列。
     *
     * @param referent 新的弱引用将引用
     */
    public WeakReference(T referent) {
        super(referent);
    }

    /**
     * 创建引用给定对象并在给定队列中注册的新的弱引用。
     *
     * @param referent 新的弱引用将引用
     * @param q 要注册参考的队列，如果不需要注册， 则为 null
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
