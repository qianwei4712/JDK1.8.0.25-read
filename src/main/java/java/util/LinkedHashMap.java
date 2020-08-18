package java.util;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.io.IOException;

/**
 * <p>哈希表和链表实现的Map接口，具有可预测的迭代次序。
 *这种实现不同于HashMap，它维持于所有条目的运行双向链表。
 *此链接列表定义迭代排序，通常是将 key-value 插入到 map（插入顺序 ）中的顺序 。
 *请注意，如果将键重新插入到地图中，则插入顺序不受影响。 （A键k被重新插入到地图m如果当m.containsKey(k)将返回true之前立即调用m.put(k, v)被调用。）
 *
 * <p>这个实现可以让客户从指定的，未排序的 HashMap创建和原始地图顺序相同的 map 副本，并且不像 TreeMap 增加消耗：
 * <pre>
 *     void foo(Map m) {
 *         Map copy = new LinkedHashMap(m);
 *         ...
 *     }
 * </pre>
 * 如果模块在输入上进行映射，复制它，然后返回其顺序由该副本决定的结果，则此技术特别有用。 （客户一般都喜欢以相同的顺序返回事情。）
 *
 * <p>提供了一种特殊的constructor来创建一个链接的哈希映射，其迭代顺序是最后访问的条目的顺序，从最近最近访问到最近的（ 访问顺序 ）。
 * <strong>这种 map 非常适合建立LRU缓存。</strong>
 * replace方法只会导致条目的访问，如果该值被替换。
 * putAll方法按照指定地图的条目集迭代器提供的键值映射的顺序，为指定地图中的每个映射生成一个条目访问。
 * 没有其他方法生成条目访问。 特别地，对于集合视图的操作不会影响背景映射的迭代顺序。
 *
 * <p>removeEldestEntry(Map.Entry)方法可能会被覆盖，以便在将新的映射添加到地图时，自动执行删除过时映射的策略。
 *
 * <p>此类提供了所有可选的 Map操作，并允许空元素。
 * 像 HashMap，它提供了基本操作（add，contains和remove）稳定的性能，假定散列函数散桶中适当的元件。
 * 表现性能可能略低于HashMap的水平 ，这是由于维护链表的额外费用，除了一个例外：
 * LinkedHashMap的收集视图的迭代需要与地图大小成比例的时间，无论其容量如何。
 * HashMap的迭代可能更昂贵，需要与其容量成比例的时间。
 *
 * <p>链接的哈希映射有两个参数影响其性能： 初始容量和负载因子 。它们的定义正如 HashMap 。
 * 但是请注意，该惩罚为初始容量选择非常高的值是该类比HashMap不太严重的，因为迭代次数对于这个类是由容量不受影响。
 *
 * <p><strong>请注意，此实现不同步。</strong>
 * 如果多个线程同时访问链接的散列映射，并且至少一个线程在结构上修改映射，则必须在外部进行同步。
 * 这通常通过在自然地封装地图的一些对象上同步来实现。
 * 如果没有这样的对象存在，应该使用Collections.synchronizedMap方法“包装”地图。
 * 这最好在创建时完成，以防止意外的不同步访问地图：
 * <pre> Map m = Collections.synchronizedMap(new LinkedHashMap(...)); </pre>
 *
 * 结构修改是添加或删除一个或多个映射的任何操作，或者在访问有序链接的散列图的情况下，影响迭代顺序。
 * 在插入有序的链接散列图中，仅改变与已经包含在地图中的键相关联的值不是结构修改。
 * <strong>在访问有序的链接散列图中，仅使用get查询地图是一种结构修改。</strong>
 *
 * <p>通过所有这些类的集合视图方法返回的集合 iterator方法返回的迭代器是快速失败的 ：
 * 如果map是在任何时间从结构上修改创建迭代器之后，以任何方式，除了通过迭代器自己remove方法，迭代器会抛出一个ConcurrentModificationException 。
 * 因此，面对并发修改，迭代器将快速而干净地失败，而不是在未来未确定的时间冒着任意的非确定性行为。
 *
 * <p>请注意，迭代器的故障快速行为无法保证，因为一般来说，在不同步并发修改的情况下，无法做出任何硬性保证。
 * 失败快速迭代器尽力投入ConcurrentModificationException 。
 * 因此，编写依赖于此异常的程序的正确性将是错误的：迭代器的故障快速行为应仅用于检测错误。
 *
 * @param <K> 该 map 维护的键的类型
 * @param <V> 映射值的类型
 * @since   1.4
 */
public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
{

    /*
     * 实施说明：该类的先前版本在内部结构上有些不同。
     * 由于超类 HashMap现在将树用于其某些节点，因此类 LinkedHashMap.Entry 现在被视为中间节点类，也可以转换为树形式。
     * 此类的名称 LinkedHashMap.Entry在其当前上下文中以多种方式令人困惑，但是无法更改。
     * 否则，即使未将其导出到此程序包之外，一些已知的源代码依赖于符号解析的特殊情况调用 removeEldestEntry的规则抑制了由于用法不明确而引起的编译错误。
     * 因此，我们保留名称以保留未修改的可编译性。
     *
     * 节点类中的更改还需要使用两个字段（head，tail），而不是指向标头节点的指针来维护双向链接的前/后列表。
     * 此类在访问，插入和删除时也曾使用过不同样式的回调方法。
     */

    /**
     * LinkedHashMap 条目继承自 HashMap.Node
     */
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
    private static final long serialVersionUID = 3801124242820219131L;
    /**
     * 双向链表的头（最老的节点）。
     */
    transient LinkedHashMap.Entry<K,V> head;
    /**
     * 双向链表的尾（最新的节点）。
     */
    transient LinkedHashMap.Entry<K,V> tail;
    /**
     * 此链接的哈希映射的迭代排序方法： true-访问顺序， false-插入顺序
     * 访问顺序表示：把访问过的元素放在链表最后，遍历的顺序会随访问而变化
     * 插入顺序表示：直接按照插入的顺序
     */
    final boolean accessOrder;

    // 内部实用程序

    // link at the end of list
    private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
        LinkedHashMap.Entry<K,V> last = tail;
        tail = p;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
    }

    // apply src's links to dst
    private void transferLinks(LinkedHashMap.Entry<K,V> src,
                               LinkedHashMap.Entry<K,V> dst) {
        LinkedHashMap.Entry<K,V> b = dst.before = src.before;
        LinkedHashMap.Entry<K,V> a = dst.after = src.after;
        if (b == null)
            head = dst;
        else
            b.after = dst;
        if (a == null)
            tail = dst;
        else
            a.before = dst;
    }

    // overrides of HashMap hook methods

    void reinitialize() {
        super.reinitialize();
        head = tail = null;
    }

    Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p = new LinkedHashMap.Entry<K,V>(hash, key, value, e);
        linkNodeLast(p);
        return p;
    }

    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
        LinkedHashMap.Entry<K,V> t =
            new LinkedHashMap.Entry<K,V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        TreeNode<K,V> p = new TreeNode<K,V>(hash, key, value, next);
        linkNodeLast(p);
        return p;
    }

    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
        TreeNode<K,V> t = new TreeNode<K,V>(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    // 删除节点回调，解除相连
    void afterNodeRemoval(Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.before = p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a == null)
            tail = b;
        else
            a.before = b;
    }

    /**
     * 可能删除最旧的节点
     * @param evict 如果为false，则 hashmap 表处于构造阶段
     */
    void afterNodeInsertion(boolean evict) {
        LinkedHashMap.Entry<K,V> first;
        // evict参数不需要管，这是在 HashMap 中就确定的
        // 获得第一个节点引用，第一个节点不为null，并且链表断开第一个节点，更改 head 引用
        // 最后才移除节点
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            // 移除第一个节点
            removeNode(hash(key), key, null, false, true);
        }
    }

    // 访问后回调，将节点移到最后面
    void afterNodeAccess(Node<K,V> e) {
        LinkedHashMap.Entry<K,V> last;
        // 如果遍历顺序是访问顺序（accessOrder = true）
        // 并且当前最后一个节点指针并不是该节点
        if (accessOrder && (last = tail) != e) {
            // 获得节点以及前后节点的引用
            LinkedHashMap.Entry<K,V> p = (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            //将下一个节点引用设置为null(意思就是打算放在链表最后)
            p.after = null;

            // 如果原本的上一个节点为空，说明当前节点是第一个节点
            if (b == null) head = a;
            else  b.after = a; //否则将上一个和下一个进行相连

            // 如果原本的下一个节点不为空,将上一个和下一个进行相连
            if (a != null)  a.before = b;
            else last = b;  //否则先暂存最后一个节点索引为 before

            //最后将该节点连到最后
            if (last == null) head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;//快速失败机制标志位 +1
        }
    }

    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }

    /**
     * 指定初始容量和扩容系数的构造方法，默认遍历顺序是插入顺序
     * @param  initialCapacity 初始容量
     * @param  loadFactor      扩容系数
     * @throws IllegalArgumentException 如果初始容量或负载系数为负
     */
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }

    /**
     * 指定初始容量的构造方法，默认遍历顺序是插入顺序，默认扩容系数0.75
     * @param  initialCapacity 初始容量
     * @throws IllegalArgumentException 如果初始容量为负
     */
    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        accessOrder = false;
    }

    /**
     * 默认遍历顺序是插入顺序，默认扩容系数0.75，初始长度 16
     */
    public LinkedHashMap() {
        super();
        accessOrder = false;
    }

    /**
     * @param  m 需要转化的 map
     * @throws NullPointerException 如果指定的map为null
     */
    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        super();
        accessOrder = false;
        putMapEntries(m, false);
    }

    /**
     * 全参构造
     * @param  initialCapacity 初始容量
     * @param  loadFactor      扩容系数
     * @param  accessOrder     排序模式- true用于访问顺序， false用于插入顺序
     * @throws IllegalArgumentException 如果初始容量或负载系数为负
     */
    public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }


    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
            V v = e.value;
            if (v == value || (value != null && value.equals(v)))
                return true;
        }
        return false;
    }

    /**
     * 返回指定键所映射到的值；如果此映射不包含键的映射关系，则返回或{@code null}。
     * <p>返回值 null 不一定表示映射不包含该键的映射；
     * 映射也可能显式地将密钥映射到 null。
     * {@link #containsKey containsKey}操作可用于区分这两种情况。
     */
    public V get(Object key) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) == null)
            return null;
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }

    /**
     * 返回指定 key对应的 value值，如果为不存在指定 key或者为值为 null,返回默认 defaultValue
     * 默认实现不保证此方法的同步或原子性。提供原子性保证的任何实现都必须重写此方法并记录其并发属性。
     * @param key the key whose associated value is to be returned
     * @param defaultValue 参数 key 默认的value值，如果 key 不存在
     * @return 指定 key对应的 value值，如果为不存在指定 key或者为值为 null,返回默认 defaultValue
     */
    public V getOrDefault(Object key, V defaultValue) {
       Node<K,V> e;
       if ((e = getNode(hash(key), key)) == null)
           return defaultValue;
       if (accessOrder)
           afterNodeAccess(e);
       return e.value;
   }

    /**
     * 清空map
     */
    public void clear() {
        super.clear();
        head = tail = null;
    }

    /**
     * 如果此 map应该删除其最老的条目，则返回true 。
     * 这个方法是在 put 和 putAll 执行插入节点后调用的。它提供了每次添加新节点同时删除最老节点的机会。
     * 如果 map 代表一个缓存，这是非常有用的：它允许 map 通过删除陈旧的条目来减少内存消耗。
     * <p>示例使用：此覆盖将允许 map 长达100个条目，然后每次添加新条目时删除最老条目，保持100个条目的稳定状态。
     * <pre>
     *     private static final int MAX_ENTRIES = 100;
     *     protected boolean removeEldestEntry(Map.Entry eldest) {
     *        return size() &gt; MAX_ENTRIES;
     *     }
     * </pre>
     * <p>该方法通常不会以任何方式修改 map，而是允许 map 按其返回值的指示进行修改。
     * 它被允许用于此方法来直接修改地图，但如果这样做的话，它必须返回false（指示地图不应试图任何进一步的修改）。
     * 从该方法中修改地图之后返回true的效果是未指定的。
     * <p>这个实现只返回false （这样，这个地图就像一个正常 map - 最老的元素永远不会被删除）。
     *
     * @param    eldest  地图中最近插入的条目，或者如果这是访问顺序的地图，最近访问的条目。
     *                   如果此方法返回true，这个条目将会被删除 。
     *                   如果在put或putAll调用之前地图为空，导致此调用，则将是刚插入的条目;
     *                   换句话说，如果地图包含单个条目，则最长条目也是最新的条目。
     * @return   如果应从map上删除最旧的条目，则为true。如果应保留，则为false。
     */
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return false;
    }

    /**
     * 返回此地图中包含的键的Set视图。
     * 该集合由地图支持，因此对地图的更改将反映在集合中，反之亦然。
     * 如果映射被修改，而该集合中的迭代正在进行（除了通过迭代器自己的remove操作），迭代的结果是未定义的。
     * 该组支持元件移除，即从映射中相应的映射，经由Iterator.remove，Set.remove，removeAll，retainAll和clear操作。
     * 它不支持add或addAll操作。 其Spliterator通常提供更快的顺序性能，但是比HashMap更差的并行HashMap。
     *
     * @return 该地图中包含的键的集合视图
     */
    public Set<K> keySet() {
        Set<K> ks;
        return (ks = keySet) == null ? (keySet = new LinkedKeySet()) : ks;
    }

    final class LinkedKeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { LinkedHashMap.this.clear(); }
        public final Iterator<K> iterator() {
            return new LinkedKeyIterator();
        }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator()  {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                                            Spliterator.ORDERED |
                                            Spliterator.DISTINCT);
        }
        public final void forEach(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
                action.accept(e.key);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * 返回此地图中包含的值的Collection视图。
     * 集合由地图支持，因此对地图的更改将反映在集合中，反之亦然。
     * 如果在集合中的迭代正在进行中修改映射（除了通过迭代器自己的remove操作），迭代的结果是未定义的。
     * 该collection支持元素移除，即从映射中相应的映射，经由Iterator.remove，Collection.remove，removeAll，retainAll和clear操作。
     * 它不支持add或addAll操作。 它的Spliterator通常提供更快的顺序性能，但是比HashMap更差的并行HashMap 。
     *
     * @return 该地图中包含的值的视图
     */
    public Collection<V> values() {
        Collection<V> vs;
        return (vs = values) == null ? (values = new LinkedValues()) : vs;
    }

    final class LinkedValues extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { LinkedHashMap.this.clear(); }
        public final Iterator<V> iterator() {
            return new LinkedValueIterator();
        }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                                            Spliterator.ORDERED);
        }
        public final void forEach(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
                action.accept(e.value);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * 返回此地图中包含的映射的Set视图。
     * 该集合由地图支持，因此对地图的更改将反映在集合中，反之亦然。
     * 如果在集合中的迭代正在进行时（除了通过迭代器自己的remove操作，或者通过迭代器返回的映射条目上的setValue操作）修改映射，则迭代的结果是未定义的。
     * 该组支持元件移除，即从映射中相应的映射，经由Iterator.remove，Set.remove，removeAll，retainAll和clear操作。
     * 它不支持add或addAll操作。 它的Spliterator通常提供更快的顺序性能，但是比HashMap更差的并行HashMap 。
     *
     * @return 该地图中包含的映射的集合视图
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
    }

    final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { LinkedHashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new LinkedEntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return Spliterators.spliterator(this, Spliterator.SIZED |
                                            Spliterator.ORDERED |
                                            Spliterator.DISTINCT);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            if (action == null)
                throw new NullPointerException();
            int mc = modCount;
            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
                action.accept(e);
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    // Map overrides

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            action.accept(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null)
            throw new NullPointerException();
        int mc = modCount;
        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
            e.value = function.apply(e.key, e.value);
        if (modCount != mc)
            throw new ConcurrentModificationException();
    }

    // Iterators

    abstract class LinkedHashIterator {
        LinkedHashMap.Entry<K,V> next;
        LinkedHashMap.Entry<K,V> current;
        int expectedModCount;

        LinkedHashIterator() {
            next = head;
            expectedModCount = modCount;
            current = null;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final LinkedHashMap.Entry<K,V> nextNode() {
            LinkedHashMap.Entry<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            current = e;
            next = e.after;
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class LinkedKeyIterator extends LinkedHashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().getKey(); }
    }

    final class LinkedValueIterator extends LinkedHashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class LinkedEntryIterator extends LinkedHashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }


}
