package java.util;

import java.io.InvalidObjectException;

/**
 * 此类实现 Set接口，并由哈希表（实际上是 HashMap实例）支持。
 * 它不保证集合的迭代顺序，特别是，它不能保证顺序会随着时间的推移保持恒定。此类允许null元素。<p>
 *
 * 此类为基本操作提供恒定的时间性能（添加，删除，包含和大小），假设哈希函数将元素正确分散在各个存储桶中。
 * 对此集合进行迭代需要的时间与 HashSet实例的大小（元素数）之和加上 HashMap实例的“容量”（数量之和）成比例个桶。
 * 因此，如果迭代性能很重要，则不要将初始容量设置得过高（或负载因数过低），这一点非常重要。<p>
 *
 * <strong>请注意，此实现未同步。</ strong>
 * 如果多个线程同时访问 hashset，并且线程中的至少一个修改了哈希集，则必须外部同步。
 * 这通常是通过对某些自然封装了该对象的对象进行同步来完成的。<p>
 *
 * 如果不存在这样的对象，则应使用{@link Collections＃synchronizedSet Collections.synchronizedSet} 方法来“包装”该集合。
 * 最好在创建时执行此操作，以防止意外异步访问集合：
 * <pre> * Set s = Collections.synchronizedSet（new HashSet（...））; </ pre><p>
 *
 *此类的iterator方法返回的迭代器为<i>fail-fast</ i>：
 * 如果在创建迭代器后的任何时间修改了集合，则除通过迭代器自己的remove法外，迭代器将引发{@link ConcurrentModificationException}。<p>
 *
 * 请注意，不能保证迭代器的快速失败行为，因为通常来说，在存在不同步的并发修改的情况下，不可能做出任何硬性保证。
 * 快速失败的迭代器尽最大努力抛出<tt> ConcurrentModificationException </ tt>。
 * 因此，编写依赖于此异常的程序的正确性是错误的：<i>迭代器的快速失败行为仅应用于检测错误。</ i>
 *
 * @param <E> 此集合所维护的元素类型
 * @since   1.2
 */
public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    static final long serialVersionUID = -5024744406713321676L;

    private transient HashMap<E,Object> map;

    // 在 HashMap中与key对应的虚拟值，所有 HashSet公用一个
    private static final Object PRESENT = new Object();

    /**
     * 构造一个空的 HashSet，默认HashMap长度16，扩容系数0.75
     */
    public HashSet() {
        map = new HashMap<>();
    }

    /**
     * 构造一个新集合，其中包含指定集合中的元素。 HashMap是使用默认加载因子（0.75）和足以容纳指定集合中的元素的初始容量创建的。
     * @param c 指定添加的集合
     * @throws NullPointerException 如果指定集合为空
     */
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
        addAll(c);
    }

    /**
     * 带默认长度和扩容系数的构造器
     * @param      initialCapacity  hashmap的初始容量
     * @param      loadFactor        hashmap的扩容系数
     * @throws     IllegalArgumentException 如果扩容系数和初始长度小于0
     */
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 指定初始容量的构造器，默认扩容系数0.75
     * @param      initialCapacity   指定初始容量
     * @throws     IllegalArgumentException 如果初始长度小于0
     */
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * 构造一个新的空链接哈希集。 （此包private构造函数仅由LinkedHashSet使用。）支持 HashMap实例是具有指定的初始容量和指定的负载因子的LinkedHashMap。
     * @param      initialCapacity    hashmap的初始容量
     * @param      loadFactor         hashmap的扩容系数
     * @param      dummy             被忽略（将此构造函数与其他int，float构造函数区分开）
     * @throws     IllegalArgumentException 如果扩容系数和初始长度小于0
     */
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 返回此集合中元素的迭代器。直接使用 hashmap的keyset迭代器
     * 元素不按特定顺序返回。
     * @return set迭代器
     * @see ConcurrentModificationException
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * @return 此集合中元素的数量
     */
    public int size() {
        return map.size();
    }

    /**
     * @return 如果此集合不包含任何元素，则返回 true
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 如果此集合包含指定的元素，则返回 true。
     * 更正式地讲，当且仅当此集合包含元素 e，使得（o == null？e == null：o时，才返回 true）。
     * @param o 需要进行判定的元素
     * @return 如果此集合包含该元素，则返回 true
     */
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 如果指定的元素尚不存在，则将其添加到该集合中。
     * 因为 hashmap 添加已有的 key，会返回原value,所以不为null。
     * @param e 需要添加的元素
     * @return 如果原 set不含有该元素，则返回true
     */
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    /**
     * 从该集合中删除指定的元素（如果存在）。
     * 因为 hashmap 删除某个 key，会返回原value。如果不存在则会返回null。
     * @param o 要从此集中移除的对象（如果存在）
     * @return 如果此集合包含该元素，则返回 true
     */
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }

    /**
     * 清空
     */
    public void clear() {
        map.clear();
    }

    /**
     * @return 返回此HashSet的浅拷贝
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 将此 HashSet实例的状态保存到流中（即对其进行序列化）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();
        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());
        // Write out size
        s.writeInt(map.size());
        // Write out all elements in the proper order.
        for (E e : map.keySet())
            s.writeObject(e);
    }

    /**
     * 从流中重构 HashSet实例（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();
        // Read capacity and verify non-negative.
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Illegal capacity: " + capacity);
        }
        // Read load factor and verify positive and non NaN.
        float loadFactor = s.readFloat();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        }
        // Read size and verify non-negative.
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Illegal size: " + size);
        }
        // Set the capacity according to the size and load factor ensuring that
        // the HashMap is at least 25% full but clamping to maximum capacity.
        capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f), HashMap.MAXIMUM_CAPACITY);
        // Create backing HashMap
        map = (((HashSet<?>)this) instanceof LinkedHashSet ?
               new LinkedHashMap<E,Object>(capacity, loadFactor) :
               new HashMap<E,Object>(capacity, loadFactor));
        // Read in all elements in the proper order.
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            map.put(e, PRESENT);
        }
    }

    /**
     * 在此元素上创建一个可分割迭代器<em> <a href="Spliterator.html#binding"></a> </ em>，实现了快速失败机制<em>fail-fast</ em> {@link Spliterator}。
     * @return a {@code Spliterator} over the elements in this set
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return new HashMap.KeySpliterator<E,Object>(map, 0, -1, 0, 0);
    }
}
