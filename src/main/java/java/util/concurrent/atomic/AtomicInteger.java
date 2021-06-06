package java.util.concurrent.atomic;
import java.util.function.IntUnaryOperator;
import java.util.function.IntBinaryOperator;
import sun.misc.Unsafe;

/**
 *
 * 可以自动更新的 int 值。
 * 有关原子变量属性的描述，请参阅原子包规范。
 * AtomicInteger 用于诸如原子递增计数器之类的应用程序中，并且不能用作 Integer 的替代品。
 * 但是，此类确实扩展了 {@code Number} 以允许处理基于数字的类的工具和实用程序进行统一访问。
 * @since 1.5
*/
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // 设置使用 Unsafe.compareAndSwapInt 进行更新
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    /**
     * 带参构造器，使用给定的初始值创建一个新的 AtomicInteger。
     * @param initialValue 初始值
     */
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    /**
     * 无参，使用初始值0，创建一个新的 AtomicInteger。
     */
    public AtomicInteger() {
    }

    /**
     * @return 获得当前值
     */
    public final int get() {
        return value;
    }

    /**
     * 设置为给定值。
     * @param newValue 新值
     */
    public final void set(int newValue) {
        value = newValue;
    }

    /**
     * 最终设置为给定值。
     * @param newValue 新值
     */
    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this, valueOffset, newValue);
    }

    /**
     * 原子地设置为给定值并返回旧值。
     * @param newValue 新值
     * @return 之前的值
     */
    public final int getAndSet(int newValue) {
        return unsafe.getAndSetInt(this, valueOffset, newValue);
    }

    /**
     * 如果当前值 {@code ==} 是预期值，则原子地将值设置为给定的更新值。
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * 如果当前值 {@code ==} 是预期值，则原子地将值设置为给定的更新值。
     * <p>可能会错误地失败并且不提供排序保证，因此很少是 compareAndSet 的合适替代方案。
     * @param expect 期望值
     * @param update 新值
     * @return {@code true} 如果成功
     */
    public final boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    /**
     * 以原子方式将当前值递增 1。
     * @return 之前的值
     */
    public final int getAndIncrement() {
        return unsafe.getAndAddInt(this, valueOffset, 1);
    }

    /**
     * 以原子方式将当前值递减 1。
     * @return 之前的值
     */
    public final int getAndDecrement() {
        return unsafe.getAndAddInt(this, valueOffset, -1);
    }

    /**
     * 以原子方式将给定值添加到当前值。
     * @param delta 要添加的值
     * @return 之前的值
     */
    public final int getAndAdd(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }

    /**
     * 以原子方式将当前值递增 1。
     * @return 更新后的值
     */
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }

    /**
     * 以原子方式将当前值递减 1。
     * @return 更新后的值
     */
    public final int decrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, -1) - 1;
    }

    /**
     * 以原子方式将给定值与当前值相加.
     * @param delta 要添加的值
     * @return 更新后的值
     */
    public final int addAndGet(int delta) {
        return unsafe.getAndAddInt(this, valueOffset, delta) + delta;
    }

    /**
     * 使用给定函数的返回结果，原子地更新当前值，返回原值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * @param updateFunction 无副作用的功能
     * @return 之前的值
     */
    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    /**
     * 使用给定函数的返回结果，原子地更新当前值，返回更新后的值。
     * 该函数应该是无副作用的，因为当尝试更新由于线程之间的争用而失败时，它可能会被重新应用。
     * @param updateFunction 无副作用的功能
     * @return 更新后的值
     */
    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
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
    public final int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsInt(prev, x);
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
    public final int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = get();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    /**
     * @return 返回当前值的字符串表示形式。
     */
    public String toString() {
        return Integer.toString(get());
    }

    /**
     * 将此 AtomicInteger 的值作为 int 返回。
     */
    public int intValue() {
        return get();
    }

    /**
     * 在扩展原始转换后，将此 AtomicInteger 的值作为 long 返回。
     */
    public long longValue() {
        return (long)get();
    }

    /**
     * 在扩展原始转换后，将此 AtomicInteger 的值作为 float 返回。
     */
    public float floatValue() {
        return (float)get();
    }

    /**
     * 在扩展原始转换后，将此 AtomicInteger 的值作为 double 返回。
     */
    public double doubleValue() {
        return (double)get();
    }

}
