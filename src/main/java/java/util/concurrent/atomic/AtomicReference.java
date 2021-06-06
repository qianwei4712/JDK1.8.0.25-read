package java.util.concurrent.atomic;
import java.util.function.UnaryOperator;
import java.util.function.BinaryOperator;
import sun.misc.Unsafe;

/**
 * 可以自动更新的对象引用。有关原子变量属性的描述，请参阅原子包规范。
 * @since 1.5
 * @param <V> 此引用所引用的对象类型
 */
public class AtomicReference<V> implements java.io.Serializable {
    private static final long serialVersionUID = -1848883965231344442L;

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicReference.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile V value;

    /**
     * 使用给定的初始值创建一个新的 AtomicReference。
     * @param initialValue 初始值
     */
    public AtomicReference(V initialValue) {
        value = initialValue;
    }

    /**
     * 创建一个具有空初始值的新 AtomicReference。
     */
    public AtomicReference() {
    }

    /**
     * 获取当前值。
     * @return 当前值
     */
    public final V get() {
        return value;
    }

    /**
     * 设置为给定值。
     * @param newValue 新值
     */
    public final void set(V newValue) {
        value = newValue;
    }

    /**
     * 最终设置为给定值。
     * @param newValue 新值
     */
    public final void lazySet(V newValue) {
        unsafe.putOrderedObject(this, valueOffset, newValue);
    }

    /**
     * 如果当前值 {@code ==} 是预期值，则原子地将值设置为给定的更新值。
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} 如果成功。假返回表示实际值不等于预期值。
     */
    public final boolean compareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 是预期值，则原子地将值设置为给定的更新值。
     * <p>可能会错误地失败并且不提供排序保证，因此很少是 {@code compareAndSet} 的合适替代品。
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} 如果成功
     */
    public final boolean weakCompareAndSet(V expect, V update) {
        return unsafe.compareAndSwapObject(this, valueOffset, expect, update);
    }

    /**
     * 原子地设置为给定值并返回旧值。
     * @param newValue 新值
     * @return 之前的值
     */
    @SuppressWarnings("unchecked")
    public final V getAndSet(V newValue) {
        return (V)unsafe.getAndSetObject(this, valueOffset, newValue);
    }

    /**
     * 使用给定函数的应用结果原子地更新当前值，返回前一个值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * @param updateFunction 无副作用的功能
     * @return 之前的值
     */
    public final V getAndUpdate(UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get();
            next = updateFunction.apply(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 使用给定函数的应用结果原子地更新当前值，返回更新后的值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * @param updateFunction 无副作用的功能
     * @return 更新后的值
     */
    public final V updateAndGet(UnaryOperator<V> updateFunction) {
        V prev, next;
        do {
            prev = get();
            next = updateFunction.apply(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * 使用将给定函数应用于当前值和给定值的结果原子地更新当前值，返回前一个值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * 该函数以当前值作为第一个参数，给定的更新作为第二个参数。
     * @param x 更新值
     * @param accumulatorFunction 两个参数的无副作用函数
     * @return 之前的值
     */
    public final V getAndAccumulate(V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get();
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 将给定函数应用于当前和给定值的结果原子地更新当前值，返回更新后的值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * 该函数以当前值作为第一个参数，给定的更新作为第二个参数。
     * @param x 更新值
     * @param accumulatorFunction 两个参数的无副作用函数
     * @return 更新后的值
     */
    public final V accumulateAndGet(V x,
                                    BinaryOperator<V> accumulatorFunction) {
        V prev, next;
        do {
            prev = get();
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * @return 当前值的字符串表示
     */
    public String toString() {
        return String.valueOf(get());
    }

}
