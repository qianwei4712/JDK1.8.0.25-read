package java.util.concurrent.atomic;

/**
 * {@code AtomicStampedReference} 维护一个对象引用以及一个可以自动更新的整数“标记”。
 * <p>实现说明：此实现通过创建表示“装箱”[引用，整数] 对的内部对象来维护标记引用。
 * @since 1.5
 * @param <V> 此引用所引用的对象类型
 */
public class AtomicStampedReference<V> {

    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    private volatile Pair<V> pair;

    /**
     * 使用给定的初始值创建一个新的 {@code AtomicStampedReference}。
     * @param initialRef 初始参考
     * @param initialStamp 最初的版本
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    /**
     * @return 参考的当前值
     */
    public V getReference() {
        return pair.reference;
    }

    /**
     * @return 当前值的版本号
     */
    public int getStamp() {
        return pair.stamp;
    }

    /**
     * 返回引用和版本的当前值。
     * 典型用法是 {@code int[1] holder; ref = v.get(holder); }.
     * @param stampHolder 大小至少为一个的数组。返回时，{@code stampholder[0]} 将保存版本的值。
     * @return 参考的当前值
     */
    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    /**
     * 如果当前引用是对预期引用的 {@code ==} 并且当前标记等于预期标记，则原子地将引用和标记的值设置为给定的更新值。
     * <p>可能会错误地失败并且不提供排序保证，因此很少是 {@code compareAndSet} 的合适替代品。
     * @param expectedReference 更新之前的原始值
     * @param newReference 将要更新的新值
     * @param expectedStamp 期待更新的标志版本
     * @param newStamp 将要更新的标志版本
     * @return {@code true} 如果成功
     */
    public boolean weakCompareAndSet(V   expectedReference,
                                     V   newReference,
                                     int expectedStamp,
                                     int newStamp) {
        return compareAndSet(expectedReference, newReference,
                             expectedStamp, newStamp);
    }

    /**
     * 如果当前引用是对预期引用的 {@code ==} 并且当前标记等于预期标记，则原子地将引用和标记的值设置为给定的更新值。
     * @param expectedReference 更新之前的原始值
     * @param newReference 将要更新的新值
     * @param expectedStamp 期待更新的标志版本
     * @param newStamp 将要更新的标志版本
     * @return {@code true} 如果成功
     */
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            expectedStamp == current.stamp &&
            ((newReference == current.reference &&
              newStamp == current.stamp) ||
             casPair(current, Pair.of(newReference, newStamp)));
    }

    /**
     * 无条件地设置引用和标记的值。
     * @param newReference 将要更新的新值
     * @param newStamp 新的版本值
     */
    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    /**
     * 如果当前引用是 {@code ==} 到预期引用，则原子地将标记的值设置为给定的更新值。
     * 此操作的任何给定调用都可能会错误地失败（返回 {@code false}），但是当当前值保持预期值并且没有其他线程也尝试设置该值时，重复调用最终会成功。
     * @param expectedReference 参考的期望值
     * @param newStamp 新的版本值
     * @return {@code true} 如果成功
     */
    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newStamp == current.stamp ||
             casPair(current, Pair.of(expectedReference, newStamp)));
    }

    // 不安全的机制

    private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
    private static final long pairOffset =
        objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
    }

    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // 将异常转换为相应的错误
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }
}
