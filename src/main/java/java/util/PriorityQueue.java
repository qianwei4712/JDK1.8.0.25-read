package java.util;

import java.util.function.Consumer;

/**
 * @author shiva   2020/4/12 20:25
 * @since 1.5
 */
public class PriorityQueue<E> extends AbstractQueue<E>
        implements java.io.Serializable {

    private static final long serialVersionUID = -7720805057305804111L;
    //默认初始容量
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    //数组最大长度
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;


    //底层使用数组实现
    transient Object[] queue; // 非私有以简化嵌套类访问
    //优先队列元素个数
    private int size = 0;
    //优先队列比较器，如果为null,优先队列使用元素的自然顺序。
    private final Comparator<? super E> comparator;
    //修改次数，fast-fail机制，因为没有继承 AbstractList，所以需要自行定义。线程不安全。
    transient int modCount = 0;

    /*
     * **********************************************************************
     * 构造方法
     * **********************************************************************
     */

    //无参构造器，默认数组长度 11；无比较器，元素按照顺序排序
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    //指定长度，没有比较器的构造函数，元素按照顺序排序
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    //创建一个带有比较器的优先队列，其元素根据比较器进行排序。默认长度 11
    public PriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    //指定初始化长度和比较器的构造器，其元素根据比较器进行排序
    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        //如果参数长度是 1，抛出异常。。
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    //根据 Collection集合 创建优先队列
    @SuppressWarnings("unchecked")
    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof SortedSet<?>) {
            //如果原始 collection 类型是 SortedSet的实现类
            //直接赋值 SortedSet 的比较器，再初始化数组
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            //直接初始化有序集合
            initElementsFromCollection(ss);
        }
        else if (c instanceof PriorityQueue<?>) {
            //如果原始 collection 类型是 PriorityQueue，同一类型的初始化就比较简单了
            PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            //初始化同类型队列
            initFromPriorityQueue(pq);
        }
        else {
            //正常的 Collection，没有比较器，默认为一个顺序容器
            this.comparator = null;
            //没有比较器，不需要进行排序，默认顺序容器，并将数组转化为堆
            initFromCollection(c);
        }
    }

    //根据原优先队列创建新队列
    @SuppressWarnings("unchecked")
    public PriorityQueue(PriorityQueue<? extends E> c) {
        //设置比较器
        this.comparator = (Comparator<? super E>) c.comparator();
        //初始化队列
        initFromPriorityQueue(c);
    }

    //根据 SortedSet 初始化优先队列
    @SuppressWarnings("unchecked")
    public PriorityQueue(SortedSet<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        initElementsFromCollection(c);
    }

    /*
     * **********************************************************************
     * public方法
     * **********************************************************************
     */

    //将指定的元素插入此优先队列
    public boolean add(E e) {
        return offer(e);
    }

    //将指定的元素插入此优先队列
    public boolean offer(E e) {
        //不允许插入 null
        if (e == null)
            throw new NullPointerException();
        //队列操作数+1
        modCount++;
        //当前队列元素
        int i = size;
        //如果当前队列元素个数大于等于数组长度
        //数组已经填充满了，进行扩容
        if (i >= queue.length)
            grow(i + 1);
        //队列元素个数+1
        size = i + 1;
        if (i == 0)
            //如果原队列是空队列
            queue[0] = e;
        else
            //根据元素进行筛选
            siftUp(i, e);
        return true;
    }

    //返回队列头，不删除。如果队列为空，返回null
    @SuppressWarnings("unchecked")
    public E peek() {
        return (size == 0) ? null : (E) queue[0];
    }

    //弹出队列头元素
    @SuppressWarnings("unchecked")
    public E poll() {
        //当队列为空时，返回null
        if (size == 0)
            return null;
        //队列元素-1
        int s = --size;
        //操作次数+1
        modCount++;
        //获得队列头元素
        E result = (E) queue[0];
        //获得队列尾元素
        E x = (E) queue[s];
        //将队尾置空，因为队头空了，要往前移动重新筛选
        queue[s] = null;
        //如果弹出后队列长度不是0，那需要重新排序
        if (s != 0)
            //将队尾元素放到队头，然后一层层往下移动
            siftDown(0, x);
        //排序后返回队头元素
        return result;
    }

    //移除指定元素
    public boolean remove(Object o) {
        int i = indexOf(o);
        if (i == -1)
            return false;
        else {
            removeAt(i);
            return true;
        }
    }

    //判断是否包含某个元素
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    //迭代器
    public Iterator<E> iterator() {
        return new Itr();
    }

    //队列转数组，其实就是把底层数组复制一份
    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }

    //将队列复制到指定数组
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(queue, size, a.getClass());
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    //队列元素个数
    public int size() {
        return size;
    }

    //清空优先队列，因为不允许添加null，所以只要置空就好了
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            queue[i] = null;
        size = 0;
    }

    //返回比较器
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /*
     * **********************************************************************
     * private方法
     * **********************************************************************
     */
    //根据另一个PriorityQueue初始化，
    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        //如果类型是 PriorityQueue，字段类型相同，直接赋值就好了
        //PriorityQueue 在JDK1.8 没有子类，但是判断类型可以防止自定义子类，预防出现问题
        if (c.getClass() == PriorityQueue.class) {
            this.queue = c.toArray();
            this.size = c.size();
        } else {
            //c已经是有序数组，进行初始化，并
            initFromCollection(c);
        }
    }

    //直接初始化有序数组， Collection c 已经是有序数组
    private void initElementsFromCollection(Collection<? extends E> c) {
        //先将集合转为数组
        Object[] a = c.toArray();
        //如果 c.toArray 错误地不返回 Object []，则将其复制。
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf(a, a.length, Object[].class);
        //获得数组长度
        int len = a.length;
        //如果数组长度是1，或者设置有比较器
        if (len == 1 || this.comparator != null){
            //循环判断是否存在空元素，不允许添加 null
            for (int i = 0; i < len; i++){
                if (a[i] == null)
                    throw new NullPointerException();
            }
        }
        this.queue = a;
        this.size = a.length;
    }

    //使用给定Collection中的元素初始化队列数组。
    private void initFromCollection(Collection<? extends E> c) {
        //直接初始化有序数组， Collection c 已经是有序数组
        initElementsFromCollection(c);
        //转化为堆
        heapify();
    }

    /**
     * 将项目x插入位置k，通过将x提升到树上直到其大于或等于其父级或成为根，从而保持堆不变。
     * 简化并加快强制和比较。将自然比较和比较器比较分为不同的方法，这些方法在其他方面相同。
     * @param k 需要插入的位置
     * @param x 需要加入的元素
     */
    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    //使用优先队列自带的比较器
    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (E) e) >= 0)
                break;
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    //没有比较器，使用自然比较，存储对象的比较器
    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        //将需要比较的元素转为 Comparable
        Comparable<? super E> key = (Comparable<? super E>) x;
        //如果插入下标大于0
        //如果是根结点那就不需要重新筛选了。只有一个元素
        while (k > 0) {
            //根据堆的特性获得父节点的下标，(i-1)/2
            int parent = (k - 1) >>> 1;
            //获得父节点元素
            Object e = queue[parent];
            //比较器比较，如果符合父节点小于等于插入元素则跳出
            if (key.compareTo((E) e) >= 0)
                break;
            //否则将父节点放置到原插入位置，并继续循环
            queue[k] = e;
            k = parent;
        }
        //循环结束，确定位置
        queue[k] = key;
    }

    @SuppressWarnings("unchecked")
    private void heapify() {
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }

    /**
     * 在位置 k 插入项 x，通过反复将 x 降级到树上小于或等于其子级或为叶子，从而保持堆不变。
     * 简化并加快强制和比较。将自然比较和比较器比较分为不同的方法，这些方法在其他方面相同。
     * @param k 需要填补的位置
     * @param x 需要插入的元素
     */
    private void siftDown(int k, E x) {
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    //使用自然比较筛选
    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        //将需要插入的元素转为 Comparable
        Comparable<? super E> key = (Comparable<? super E>)x;
        //长度除以二，需要循环的次数
        int half = size >>> 1;        // loop while a non-leaf
        while (k < half) {
            //左孩子的下标
            int child = (k << 1) + 1;
            //获得左孩子
            Object c = queue[child];
            //获得右孩子的下标
            int right = child + 1;
            //条件1：右孩子不是空的
            //条件2：左孩子比右孩子大
            if (right < size &&
                    ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
                //将选用对象改为右孩子
                //原则是选择左右孩子中更小的那个进行交换
                c = queue[child = right];
            //如果需要插入的元素已经比孩子小，那就跳出
            if (key.compareTo((E) c) <= 0)
                break;
            //否则就将子节点放置到要插入的位置，继续循环
            queue[k] = c;
            k = child;
        }
        //循环结束，确定位置
        queue[k] = key;
    }

    //使用优先队列的比较器筛选，和上面方法基本一样
    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size &&
                    comparator.compare((E) c, (E) queue[right]) > 0)
                c = queue[child = right];
            if (comparator.compare(x, (E) c) <= 0)
                break;
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    //扩容数组
    private void grow(int minCapacity) {
        //拿到旧长度
        int oldCapacity = queue.length;
        // Double size if small; else grow by 50%
        //原长度x小于64，就扩容为 2x+2
        //原长度x大于等于64，扩容为 1.5倍，扩容一半
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                (oldCapacity + 2) :
                (oldCapacity >> 1));
        // 有溢出意识的代码
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            //获得最大长度，参数 minCapacity = oldCapacity + 1
            newCapacity = hugeCapacity(minCapacity);
        //复制数组
        queue = Arrays.copyOf(queue, newCapacity);
    }

    //最大容量方法；minCapacity = oldCapacity + 1
    private static int hugeCapacity(int minCapacity) {
        //超出integer变为负数，抛出异常
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    //获得指定元素的下标
    private int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++)
                if (o.equals(queue[i]))
                    return i;
        }
        return -1;
    }


    //移除相同元素
    boolean removeEq(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == queue[i]) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }


    private final class Itr implements Iterator<E> {

        private int cursor = 0;
        private int lastRet = -1;
        private ArrayDeque<E> forgetMeNot = null;
        private E lastRetElt = null;

        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                    (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = PriorityQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

    //根据下标移除，然后重新进行筛选上浮
    @SuppressWarnings("unchecked")
    private E removeAt(int i) {
        // assert i >= 0 && i < size;
        modCount++;
        int s = --size;
        if (s == i) // removed last element
            queue[i] = null;
        else {
            E moved = (E) queue[s];
            queue[s] = null;
            siftDown(i, moved);
            if (queue[i] == moved) {
                siftUp(i, moved);
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    //写入流
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();
        // Write out array length, for compatibility with 1.5 version
        s.writeInt(Math.max(2, size + 1));

        // Write out all elements in the "proper order".
        for (int i = 0; i < size; i++)
            s.writeObject(queue[i]);
    }

    //从流中读出
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();
        s.readInt();
        queue = new Object[size];
        for (int i = 0; i < size; i++)
            queue[i] = s.readObject();
        // Elements are guaranteed to be in "proper order", but the
        // spec has never explained what that might be.
        heapify();
    }


    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator<E>(this, 0, -1, 0);
    }

    static final class PriorityQueueSpliterator<E> implements Spliterator<E> {
        private final PriorityQueue<E> pq;
        private int index;            // current index, modified on advance/split
        private int fence;            // -1 until first use
        private int expectedModCount; // initialized when fence set

        /** Creates new spliterator covering the given range */
        PriorityQueueSpliterator(PriorityQueue<E> pq, int origin, int fence,
                                 int expectedModCount) {
            this.pq = pq;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi;
            if ((hi = fence) < 0) {
                expectedModCount = pq.modCount;
                hi = fence = pq.size;
            }
            return hi;
        }

        public PriorityQueueSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                    new PriorityQueueSpliterator<E>(pq, lo, index = mid,
                            expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            PriorityQueue<E> q; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((q = pq) != null && (a = q.queue) != null) {
                if ((hi = fence) < 0) {
                    mc = q.modCount;
                    hi = q.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (E e;; ++i) {
                        if (i < hi) {
                            if ((e = (E) a[i]) == null) // must be CME
                                break;
                            action.accept(e);
                        }
                        else if (q.modCount != mc)
                            break;
                        else
                            return;
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), lo = index;
            if (lo >= 0 && lo < hi) {
                index = lo + 1;
                @SuppressWarnings("unchecked") E e = (E)pq.queue[lo];
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
                if (pq.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }
}
