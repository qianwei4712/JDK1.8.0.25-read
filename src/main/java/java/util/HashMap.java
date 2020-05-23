package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * Map 接口的基于哈希表的实现。允许 null值和 null键。
 * HashMap与 Hashtable大致等效，除了HashMap不同步并且允许为null
 * 此类不保证 Map的顺序；特别是，它不能保证顺序会随时间保持不变。<p>
 *
 * HashMap的实例具有两个影响其性能的参数：初始容量和扩容系数。
 * capacity是哈希表中存储桶的数量，初始 Capacity是创建哈希表时的容量。
 * 扩容系数是在自动增加其哈希表容量之前允许哈希表获得的满度的度量。
 * 当哈希表中条目的数量超过负载因子与当前容量的乘积时，哈希表将被刷新（即，内部数据结构被重建），以便哈希表的存储桶数约为2倍。<p>
 *
 * 默认扩容系数（0.75）是在时间和空间成本之间提供了一个很好的折中方案。
 * 较高的值会减少空间开销，但会增加查找成本。
 * HashMap类的大多数操作中都得到了体现，包括 get和 put。
 * 设置其初始容量时，应考虑 map中的预期键值对数量及其扩容系数，以最大程度地减少重新哈希操作的次数。
 * 如果初始容量大于最大条目数除以扩容系数，则不会进行任何哈希操作。<p>
 *
 * 如果需要存储大量键值对，创建时初始容量设置大点要比扩容来的更有效率。<p>
 *
 * 请注意，此实现未同步。
 * 如果多个线程同时访问一个哈希映射，并且线程中的至少一个在结构上修改了该映射，则它必须在外部进行同步。<p>
 *
 * @since   1.2
 */
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /**
     * 默认的初始容量 2^4 = 16 - 必须为2的幂。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    /**
     * 最大容量 2^30；如果构造方法指定了更大值，应该使用 2^30。容量必须是2的幂并且小于等于 2^30
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;
    /**
     * 在构造函数中未指定时扩容系数的话，默认扩容系数
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * 添加键值对时，当map已有键值对数量超过此计数阈值，将链表转化为树。至少应该为8
     */
    static final int TREEIFY_THRESHOLD = 8;
    /**
     * 在调整大小操作期间，键值对数量少于此阈值时，将树转回链表。 应小于 TREEIFY_THRESHOLD，至少应该为6
     */
    static final int UNTREEIFY_THRESHOLD = 6;
    /**
     * 数据转化为树时，整个 table的最小容量。至少为 4倍 TREEIFY_THRESHOLD
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * 基础 hash桶的节点，大多数情况下只需要用这个类
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        //节点hash值
        final int hash;
        final K key;
        V value;
        //下一个节点引用
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /* ---------------- 静态公共方法 -------------- */

    /**
     * 计算 key键的 hashcode值，key 为 null, hashcode 为 0。^ 异或运算
     *
     * 官方注解大致意思：
     * 哈希桶长度使用2的幂，因此哈希表肯定会发生冲突。所以使用了右移16位再异或。
     * 在hash算法的速度、有效性、扩展质量上进行了权衡，减少系统损失。
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 如果对象实现了 Comparable 接口，返回该对象的Class，否则返回null
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * 则返回k.compareTo（x），否则返回0。
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * 比参数容量大的下一个 2的幂值。最大长度为 2^30，最小为1
     * 使用或运算和右移运算，计算返回结果容量为 2的幂
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /* ---------------- 字段 -------------- */

    /**
     * 该表在首次使用时初始化，并根据需要调整大小。长度始终是2的幂；特定情况下允许为0
     */
    transient Node<K,V>[] table;
    /**
     * 转化为set集合用于迭代
     */
    transient Set<Map.Entry<K,V>> entrySet;
    /**
     * 当前 map包含的键值对数量
     */
    transient int size;
    /**
     * 修改次数，线程不安全类的 fast-fail 机制
     */
    transient int modCount;
    /**
     * 阈值，超过这个值就需要进行扩容（容量 * 扩容系数）。
     * 如果哈希表数组没有被分配，此字段为默认容量 DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR = 12
     */
    int threshold;
    /**
     * 哈希表的扩容系数
     */
    final float loadFactor;

    /* ---------------- 公共方法 -------------- */

        /* ---------------- 构造方法 -------------- */
    /**
     * 无参构造，使用默认扩容系数 0.75，默认初始容量16；
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // 其他所有字段均默认
    }
    /**
     * 指定初始容量的构造器，默认扩容系数 0.75
     * @param  initialCapacity 初始化数组长度
     * @throws IllegalArgumentException 如果初始容量为负
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
    /**
     * 指定初始容量和扩容系数的构造器
     * @param  initialCapacity 初始化数组长度
     * @param  loadFactor      扩容系数
     * @throws IllegalArgumentException 如果初始容量或者扩容系数为负
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        //最大容量 2^30
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        //扩容系数小于等于零，或者不是个有效数字
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        //下一个要调整到的容量值
        // 比当前容量大的下一个 2的幂，最大 2^30。
        this.threshold = tableSizeFor(initialCapacity);
    }
    /**
     * 构造一个新的 hashmap，并添加参数map的键值对，使用默认长度和扩容系数
     * @param   m 传入原有 map参数
     * @throws  NullPointerException 指定 map为null时抛出异常
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        //默认扩容系数 0.75
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
        /* ---------------- 构造方法结束 -------------- */

    /**
     * 批量添加，用于 使用Map作为参数的构造器 和 Map.putAll 方法实现
     * @param m 指定 map
     * @param evict 最初构造此 map时为false，否则为true（中继到 afterNodeInsertion方法）
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // pre-size
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * @return 返回map的键值对数size
     */
    public int size() {
        return size;
    }

    /**
     * 如果当前map不包含任何键值对，则返回true；
     * 实际判断使用map的键值对数size
     * @return 如果当前map不包含任何键值对，则返回true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 返回指定键所映射到的值；如果此映射不包含键的映射关系，则返回 null
     */
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 实现 Map.get以及相关方法
     * @param hash key键的 hash值
     * @param key key键
     * @return 返回节点, 如果为空，返回null
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        //先判断 hashmap是否初始化，该hash桶位置是否为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            // 遍历之前，需要检查第一个节点是否就是需要查找的节点
            if (first.hash == hash &&
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            //第一个节点不是，并且还有后续节点，开始完后检查
            if ((e = first.next) != null) {
                //如果是红黑树，使用树遍历方法
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    //不是红黑树，链表遍历
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    /**
     * 判断 map是否包含指定key
     * @param   key   需要测试的key
     * @return 如果 map 包含这个key就返回true
     * key.
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 添加键值对，若该 key已存在，则替换 value值
     * @param key 指定需要添加的键
     * @param value 指定 key对应的 value 值
     * @return 指定 key之前对应的 value值；如果之前不存在该键，则返回 null
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * 实现添加方法
     * @param hash key计算所得 hash 值
     * @param key key 键对象
     * @param value key键对应的 value 值
     * @param onlyIfAbsent 如果为true，不要更改现有值
     * @param evict 如果为false，则 hashmap 表处于构造阶段
     * @return 之前的 value值；不存在的话返回 null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        //定义一个节点数组，稍后用来保存 hashmap的节点数组
        Node<K,V>[] tab;
        //需要插入位置的节点
        Node<K,V> p;
        //n:哈希表长度； i:需要插入的位置索引
        int n, i;
        //如果哈希表长度为0，或者还没有初始化，那么需要初始化哈希表
        if ((tab = table) == null || (n = tab.length) == 0)
            //获得 n表长
            n = (tab = resize()).length;
        //如果需要插入的桶内没有其他节点，那么直接插入非树形节点
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {//桶内存在其他节点
            //保存原节点和它的key
            Node<K,V> e; K k;
            // 1.当前桶内的第一个节点 p的哈希值，和传入对象的哈希值相同
            // 2.当前桶内的第一个节点 p的 key值，和传入 key 相等（hash值相等 key不一定相等）
            // 都符合表示需要用新 value替换 p的 value
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                //获取原节点，后面再判断是否要进行替换
                e = p;
            else if (p instanceof TreeNode)
                //树形节点，添加一个节点
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                //链表，在最后添加节点；进行循环移动指针
                for (int binCount = 0; ; ++binCount) {
                    //如果p是最后一个
                    if ((e = p.next) == null) {
                        //直接链在链表最后
                        p.next = newNode(hash, key, value, null);
                        //判断桶内节点数，如果达到 8 个，那需要替换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            //转化为树
                            treeifyBin(tab, hash);
                        break;
                    }
                    //e不是最后一个，但是当前e的key相同，是否需要进行替换后面判断
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    //否则继续循环
                    p = e;
                }
            }
            // 现有键映射，说明该 key已经存在
            if (e != null) {
                //拿到原有值
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                //回调操作，在 HashMap 中无效
                afterNodeAccess(e);
                return oldValue;
            }
        }
        //操作次数+1
        ++modCount;
        //如果达到阈值，则进行扩容
        if (++size > threshold)
            resize();
        //回调操作，在 HashMap 中无效
        afterNodeInsertion(evict);
        //新增键值对，返回null
        return null;
    }

    /**
     * 初始化或增加哈希表大小。
     * @return 返回哈希表
     */
    final Node<K,V>[] resize() {
        //获得旧哈希表
        Node<K,V>[] oldTab = table;
        //获得旧表长度
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //获得旧的扩容阈值
        int oldThr = threshold;
        //定义新表长度和扩容阈值
        int newCap, newThr = 0;
        //旧表长度大于0
        if (oldCap > 0) {
            //如果原长度大于等于最大长度
            //无参构造原长度默认为0，指定长度的构造器，表长度肯定是2的幂，并且最大长度为2^30，所以不可能大于最大值
            if (oldCap >= MAXIMUM_CAPACITY) {
                //扩容系数确定为最大integer.max 2^31 - 1
                threshold = Integer.MAX_VALUE;
                //并返回旧哈希表；实际上并未真正进行扩容
                return oldTab;
            }
            //如果原长度小于 2^30，执行以下判断
            //如果原长度*2的结果 小于 2^30，并且原长度大于等于 16（其实说明 hashmap 已经经过了初始化，并且还未达到最大长度）
            //那么新扩容阈值也是 旧扩容阈值*2（并且新长度 = 原长度 * 2）
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // 双倍旧扩容阈值
        }
        //旧表长度等于0 并且 旧扩容阈值大于 0
        //在构造方法中，如果没有指定 initialCapacity初始长度，则threshold默认为0
        //因此，这个if的判断实际是：该 map为空，且在创建时指定了table长度
        else if (oldThr > 0)
            //新表长度就是旧扩容阈值
            newCap = oldThr;
        //旧表长度等于 0，旧扩容阈值也等于 0（说明 hashmap 还没有进行初始化）
        //使用默认值
        else {
            //新表长度使用默认值16
            newCap = DEFAULT_INITIAL_CAPACITY;
            //新扩容阈值为默认值 16*0.75 = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        //如果新阈值等于0（说明进入了 oldThr > 0条件判断，该 map为空，且在创建时指定了table长度）
        if (newThr == 0) {
            //计算新的阈值
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ? (int)ft : Integer.MAX_VALUE);
        }
        //将新的阈值赋值给 map.threshold
        threshold = newThr;
        //创建新表长度的node数组
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        //判断旧哈希表是否为空
        //实际判断是否为初始化
        if (oldTab != null) {
            //遍历旧表
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                //拿到旧哈希表节点
                if ((e = oldTab[j]) != null) {
                    //清空旧哈希表对应位置引用
                    oldTab[j] = null;
                    //对该节点进行判断
                    if (e.next == null)
                        //如果桶内只有一个节点，直接放入新哈希表
                        newTab[e.hash & (newCap - 1)] = e;
                    //如果不止一个节点，并判断该节点是否树形节点
                    else if (e instanceof TreeNode)
                        //调用 TreeNode方法，进行拆分
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        //不是树形节点，为链式结构
                        //将一个链表拆分成2个链表；lo开头低索引，hi高索引；head为第一个节点，tail为最后一个节点
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            //获得下一个节点
                            next = e.next;
                            //这个判断是为了选择出扩容后在同一个桶的节点
                            if ((e.hash & oldCap) == 0) {
                                //判断是否是第一个节点，并链到该链表末尾
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            } else {
                                //判断是否是第一个节点，并链到该链表末尾
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        //低位链表存在，移动到新哈希表
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        //高位链表，移动到新哈希表
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * 将链表转化为红黑树
     */
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        //如果当前哈希表为空，或者表长小于 64
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            //那么进行扩容
            resize();
        //再次判定指定索引位置不为空，并获得第一个节点引用
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                //链表节点转化为树形节点
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    //根节点
                    hd = p;
                else {
                    //先连成双向链表
                    p.prev = tl;
                    tl.next = p;
                }
                //当前指针
                tl = p;
            } while ((e = e.next) != null);
            //把转化后的双向链表转化为红黑树
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

    /**
     * 批量添加
     * @param m 指定map,需要被添加的参数
     * @throws NullPointerException 指定map为空
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * 如果存在，则从此映射中删除指定键的映射。
     * @param  key 要从地图中删除其映射的键
     * @return 与 key关联的先前值；如果没有 key的映射，则为 null。 （返回 null还可能表明该地图先前将 null与 key关联。）
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
    }

    /**
     * 实现 Map.remove 以及相关方法
     * @param hash key的 hash值
     * @param key 需要移除的key
     * @param value 需要被匹配的方法，1.8新增方法
     * @param matchValue 如果为true，则仅在值相等时删除,1.8新增
     * @param movable 如果为false，则在删除时不要移动其他节点
     * @return 与 key关联的先前值；如果没有 key的映射，则为 null
     */
    final Node<K,V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
        // tab：用于指向table； p：桶内第一个节点； n：数组长度； index：hash所映射的数组下标
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        // hashmap已经初始化，并且key的hash值位置桶内不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            //node:当前节点，e:下一个节点，k:当前节点的key，v:当前节点value值
            Node<K,V> node = null, e; K k; V v;
            //第一个节点就是目标节点
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            //存在下一个节点
            else if ((e = p.next) != null) {
                //节点为树形节点
                if (p instanceof TreeNode)
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {//当前为链表
                    do {
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    //红黑树移除节点
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                else if (node == p)
                    tab[index] = node.next;
                else
                    p.next = node.next;
                ++modCount; //操作次数+1
                --size; //map键值对-1
                afterNodeRemoval(node); //回调，在1.8 hashmap中无效
                return node;
            }
        }
        return null;
    }

    /**
     * 清空map
     */
    public void clear() {
        Node<K,V>[] tab;
        //操作次数+1，即使清空了也意味着这个map不是初始状态
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            //这里有个问题，就是为啥只清空哈希表，不将链表和红黑树之间的连接断开
            //在 LinkedList中明明就进行了操作
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * 判断 hashmap 是否包含指定值
     * @param value 需要被测试的值
     * @return 如果存在则返回 true
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        Set<K> ks;
        return (ks = keySet) == null ? (keySet = new KeySet()) : ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs;
        return (vs = values) == null ? (values = new Values()) : vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
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
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // JDK8 Map扩展方法的替代

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
        }
        else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // 克隆和序列化

    /**
     * 复制一个hashmap，只是浅拷贝
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    // 序列化HashSet时也使用这些方法
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);
            @SuppressWarnings({"rawtypes","unchecked"})
                Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // 迭代

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
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

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap支持


    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // 创建一个非树形的节点
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // For conversion from TreeNodes to plain nodes
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 供 treeifyBin使用，链表节点转化为树形节点
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * 将 hashmap 重置为初始默认状态。由clone和readObject调用。
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // LinkedHashMap用的回调操作
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // 仅从writeObject调用，以确保兼容的顺序。
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * 返回包含此节点的树的根。
         */
        final TreeNode<K,V> root() {
            for (TreeNode<K,V> r = this, p;;) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                if (root != first) {
                    Node<K,V> rn;
                    tab[index] = root;
                    TreeNode<K,V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * 使用给定的哈希和键将节点从根p开始索引。 kc用来保存key的类型
         */
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * 查找树中指定节点
         */
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * 用这个方法来比较两个对象，返回值要么大于0，要么小于0，不会为0
         * 也就是说这一步一定能确定要插入的节点要么是树的左节点，要么是右节点，不然就无继续满足二叉树结构了
         * 先比较两个对象的类名，类名是字符串对象，就按字符串的比较规则
         * 如果两个对象是同一个类型，那么调用本地方法为两个对象生成hashCode值，再进行比较，hashCode相等的话返回-1
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * 转为树
         * @return 树的根(为啥官方注释有返回。。。明明么得啊)
         */
        final void treeify(Node<K,V>[] tab) {
            // 定义树的根节
            TreeNode<K,V> root = null;
            //遍历链表
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                //从双向链表获取下一个节点
                next = (TreeNode<K,V>)x.next;
                //左右节点都为空
                x.left = x.right = null;
                //如果尚未定义根节点，则确定为根节点
                if (root == null) {
                    x.parent = null;
                    x.red = false;//根节点为黑色
                    root = x;
                } else {// 如果已经存在根节点了
                    // 取得当前链表节点的 key和 hash值
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;// 定义key所属的Class
                    //从根节点开始遍历，没有设置循环边界，只能内部跳出
                    for (TreeNode<K,V> p = root;;) {
                        // dir 标识方向（左右），-1表示左侧；1表示右侧；
                        // ph标识当前遍历树节点的hash值
                        int dir, ph;
                        // 当前遍历树节点的key
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            //遍历节点的哈希值比插入节点大，放在p的左侧
                            dir = -1;
                        else if (ph < h)
                            //遍历节点的哈希值比插入节点小，放在p的右侧
                            dir = 1;
                        //如果hash值相等，需要进行其他判断
                        //如果当前链表节点的key实现了comparable接口，并且当前树节点和链表节点是相同Class的实例，那么通过comparable的方式再比较两者
                        //如果还是相等，最后再通过tieBreakOrder（该方法不会返回 0）比较一次
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                        // 保存当前树节点
                        TreeNode<K,V> xp = p;

                        //如果dir小于等于0 ： 当前链表节点一定放置在当前树节点的左侧。
                        //如果dir大于0 ： 当前链表节点一定放置在当前树节点的右侧。
                        //如果还没有遍历到叶子节点，继续遍历
                        //挂载之后，还需要重新把树进行平衡。平衡之后，就可以针对下一个链表节点进行处理了。
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            //自平衡操作
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 把所有的链表节点都遍历完之后，最终构造出来的树可能经历多个平衡操作，根节点目前到底是链表的哪一个节点是不确定的
            // 因为我们要基于树来做查找，所以就应该把 tab[N] 得到的对象一定根是节点对象，而目前只是链表的第一个节点对象，所以要做相应的处理。
            //把红黑树的根节点设为  其所在的数组槽 的第一个元素
            //首先明确：TreeNode既是一个红黑树结构，也是一个双链表结构
            //这个方法里做的事情，就是保证树的根节点一定也要成为链表的首节点
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;
            for (Node<K,V> q = this; q != null; q = q.next) {
                Node<K,V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * 红黑树添加节点
         */
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent != null) ? root() : this;
            for (TreeNode<K,V> p = root;;) {
                int dir, ph; K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                          (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * 删除给定节点，该节点必须在此调用之前存在。
         * 这个实现比典型的红黑树更加混乱，因为后续节点不能进行交换。
         * 如果当前树的节点似乎太少，则转换回普通链表。
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab, boolean movable) {
            int n;
            //如果为哈希表为空，则直接返回
            if (tab == null || (n = tab.length) == 0)
                return;
            //获得索引值
            int index = (n - 1) & hash;
            //first：哈希桶内第一个节点，root：红黑树根节点
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            //方法的调用是由需要移除的节点调用
            //succ用来存储下一个节点
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            //如果当前节点的父节点为空，则说明当前为根节点
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            //如果以下符合一个，树退回链表：
            // 1.根节点为空，2.根的右孩子为空，3.根的左孩子为空，4.根的左孩子的左孩子为空
            if (root == null || root.right == null ||
                (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * 拆分树，如果太小则进行退化回链表
         * @param map 当前 hashmap
         * @param tab 新哈希表
         * @param index 需要被拆分的树的在原哈希表的索引
         * @param bit 原哈希表容量
         */
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            //调用时是使用桶内第一个节点调用，所以this为树的根节点
            TreeNode<K,V> b = this;
            //lo开头低索引，hi高索引；head为第一个节点，tail为最后一个节点
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            //lc低位树个数，
            int lc = 0, hc = 0;
            //定义变量，b为根节点，e:当前节点，next：下一节点
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                //拿到下一个节点，并将引用消除
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                //这个判断是为了选择出扩容后在同一个桶的节点（其实就是区分单数倍和双数倍）
                if ((e.hash & bit) == 0) {
                    //先练成双向链表
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    //链表数量+1
                    ++lc;
                }
                else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }
            if (loHead != null) {//判空
                //如果双向链表数量小于等于6（退化为链表的阈值）
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    //先确定树根节点
                    tab[index] = loHead;
                    //再转化为树
                    if (hiHead != null) // (其他已经被树化了)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // 红黑树方法，全部改编自CLR

        //左旋
        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root, TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }
        //右旋
        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        /**
         * 红黑树插入节点后，重新平衡算法
         * @param root 当前根节点
         * @param x 新插入的节点
         * @return 返回重新平衡后的根节点
         */
        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root, TreeNode<K,V> x) {
            x.red = true;//新插入的节点为红色
            // 循环没有设置边界，只能从内部跳出；先定义变量
            // XP:当前节点的父节点； XPP: 父节点的父节点，爷爷节点。。 ;
            // XPPL: 左叔叔节点 ; XPPR: 右叔叔节点
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {//第一层
                // 如果父节点为空、说明当前节点就是根节点
                // 那么把当前节点标为黑色，返回当前节点
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                // 父节点不为空
                // 如果父节点为黑色，或者爷爷节点为空
                // 说明当前节点只在第二层，不需要平衡
                else if (!xp.red || (xpp = xp.parent) == null)//第二层
                    return root;

                //第三层判断
                //如果父节点是爷爷节点的左孩子
                if (xp == (xppl = xpp.left)) {
                    //获得右叔叔节点，右叔叔不为空且右叔叔是红色
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;// 右叔叔置为黑色
                        xp.red = false;// 父节点置为黑色
                        xpp.red = true;// 爷爷节点置为红色
                        x = xpp; // 将爷爷节点作为新节点继续循环
                    }
                    else {//进入这个else说明右叔叔不存在，或者为黑色
                        //如果当前节点是父节点的右孩子
                        if (x == xp.right) {
                            //左旋
                            root = rotateLeft(root, x = xp);
                            // 获取爷爷节点
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        // 如果父节点不为空
                        if (xp != null) {
                            // 父节点 置为黑色
                            xp.red = false;
                            // 爷爷节点不为空
                            if (xpp != null) {
                                // 爷爷节点置为 红色
                                xpp.red = true;
                                //爷爷节点右旋
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                //如果父节点是爷爷节点的右孩子
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
