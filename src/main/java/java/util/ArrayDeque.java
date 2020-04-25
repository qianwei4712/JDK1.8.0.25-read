package java.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * @author shiva   2020/4/1 20:46
 * @since   1.6
 */
public class ArrayDeque<E> extends AbstractCollection<E>
                           implements Deque<E>, Cloneable, Serializable
{
    private static final long serialVersionUID = 2340985798034038923L;
    //ArrayDeque最小容量，必须是2的幂
    private static final int MIN_INITIAL_CAPACITY = 8;

    //底层数组实现，容量就是队列长度
    transient Object[] elements;
    //队列头的索引
    transient int head;
    //将被添加的元素的位置索引，队列尾指针
    transient int tail;

    /*
     * **********************************************************************
     * 构造方法
     * **********************************************************************
     */

    //无参构造默认长度 16
    public ArrayDeque() {
        elements = new Object[16];
    }

    //指定容量的构造器
    //因为要求数组长度必须是 2的幂，所以需要对传入的长度进行计算
    public ArrayDeque(int numElements) {
        allocateElements(numElements);
    }

    //批量添加，和指定容量构造的长度指定方式相同
    public ArrayDeque(Collection<? extends E> c) {
        allocateElements(c.size());
        addAll(c);
    }

    /*
     * **********************************************************************
     * public方法
     * **********************************************************************
     */

    //最基础的方法，其他方法都是调用这些方法

    //队列头部添加元素
    public void addFirst(E e) {
        //不允许null
        if (e == null)
            throw new NullPointerException();
        //在头部插入数据，头指针向左移动，每次调用addFirst，head减一
        elements[head = (head - 1) & (elements.length - 1)] = e;
        //如果队列头索引和将被添加的位置索引相同，进行扩容
        if (head == tail)
            doubleCapacity();
    }

    //队列尾部添加元素
    public void addLast(E e) {
        if (e == null)
            throw new NullPointerException();
        //先将元素加入数组
        elements[tail] = e;
        //将tail加1，然后再判断是否和头指针索引相同，若相同则扩容
        if ( (tail = (tail + 1) & (elements.length - 1)) == head)
            doubleCapacity();
    }

    //队列头添加元素
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    //队列尾添加元素
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    //移除队列头元素并返回
    public E pollFirst() {
        int h = head;
        @SuppressWarnings("unchecked")
        E result = (E) elements[h];
        //因为队列不允许添加null，所以若返回空，表示已经是空队列了
        if (result == null)
            return null;
        //移除清空
        elements[h] = null;
        //移动 head 指针索引
        head = (h + 1) & (elements.length - 1);
        return result;
    }

    //移除队列尾元素并返回
    public E pollLast() {
        //因为队列尾指针指向比实际队列尾多一位，所以需要提前减一
        int t = (tail - 1) & (elements.length - 1);
        @SuppressWarnings("unchecked")
        E result = (E) elements[t];
        if (result == null)
            return null;
        elements[t] = null;
        tail = t;
        return result;
    }

    //移除队列头元素
    public E removeFirst() {
        E x = pollFirst();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }

    //移除队列尾元素
    public E removeLast() {
        E x = pollLast();
        if (x == null)
            throw new NoSuchElementException();
        return x;
    }

    //获得队列头元素
    public E getFirst() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[head];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    //获得队列尾元素
    public E getLast() {
        @SuppressWarnings("unchecked")
        E result = (E) elements[(tail - 1) & (elements.length - 1)];
        if (result == null)
            throw new NoSuchElementException();
        return result;
    }

    //获得队列头元素
    @SuppressWarnings("unchecked")
    public E peekFirst() {
        // elements[head] is null if deque empty
        return (E) elements[head];
    }

    //获得队列尾元素
    @SuppressWarnings("unchecked")
    public E peekLast() {
        return (E) elements[(tail - 1) & (elements.length - 1)];
    }


    //删除第一个相同元素，从头遍历
    public boolean removeFirstOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = head;
        Object x;
        //根据null判断，基于不允许添加null
        while ( (x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i + 1) & mask;
        }
        return false;
    }

    //删除第一个相同元素，从尾遍历
    public boolean removeLastOccurrence(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = (tail - 1) & mask;
        Object x;
        //根据null判断，基于不允许添加null
        while ( (x = elements[i]) != null) {
            if (o.equals(x)) {
                delete(i);
                return true;
            }
            i = (i - 1) & mask;
        }
        return false;
    }


    // *** Queue methods ***

    //队列尾添加元素
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    //队列尾添加元素
    public boolean offer(E e) {
        return offerLast(e);
    }

    //移除队列头元素
    public E remove() {
        return removeFirst();
    }

    //移除队列头元素并返回
    public E poll() {
        return pollFirst();
    }

    //获得队列头元素
    public E element() {
        return getFirst();
    }

    //获得队列头元素
    public E peek() {
        return peekFirst();
    }

    // *** Stack methods ***

    //队列头部添加元素
    public void push(E e) {
        addFirst(e);
    }

    //移除队列头元素
    public E pop() {
        return removeFirst();
    }

    // *** Object methods ***

    //克隆对象，浅拷贝
    public ArrayDeque<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
            result.elements = Arrays.copyOf(elements, elements.length);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    // *** Collection Methods ***

    //返回队列长度
    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    //因为队列头索引和下一个元素索引相等时，必定会进行扩容
    //因此只有在刚构造时才会出现 head = tail = 0 的情况。所以是空数组
    public boolean isEmpty() {
        return head == tail;
    }

    //迭代器
    public Iterator<E> iterator() {
        return new DeqIterator();
    }

    //反向迭代器
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    //判断是否包含某个元素
    public boolean contains(Object o) {
        if (o == null)
            return false;
        int mask = elements.length - 1;
        int i = head;
        Object x;
        while ( (x = elements[i]) != null) {
            if (o.equals(x))
                return true;
            i = (i + 1) & mask;
        }
        return false;
    }

    //顺序遍历删除某个元素
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    //清空
    public void clear() {
        int h = head;
        int t = tail;
        if (h != t) { // clear all cells
            head = tail = 0;
            int i = h;
            int mask = elements.length - 1;
            //遍历数组清空
            do {
                elements[i] = null;
                i = (i + 1) & mask;
            } while (i != t);
        }
    }

    //转数组，重新转为顺序数组，返回的是一个新数组。
    public Object[] toArray() {
        return copyElements(new Object[size()]);
    }

    //转到指定数组
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        copyElements(a);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    public Spliterator<E> spliterator() {
        return new DeqSpliterator<E>(this, -1, -1);
    }

    /*
     * **********************************************************************
     * private方法
     * **********************************************************************
     */

    //如果传入参数大于等于8，计算比传入参数大的最小的 2的幂
    //传入1，得到8；传入8，得到16；传入17，得到32
    private void allocateElements(int numElements) {
        //最小长度 8
        int initialCapacity = MIN_INITIAL_CAPACITY;
        //如果传入的指定长度大于等于 8，计算数组大小
        //算法利用或运算和右移运算，计算结果始终为2的n次方。。。
        if (numElements >= initialCapacity) {
            initialCapacity = numElements;
            initialCapacity |= (initialCapacity >>>  1);
            initialCapacity |= (initialCapacity >>>  2);
            initialCapacity |= (initialCapacity >>>  4);
            initialCapacity |= (initialCapacity >>>  8);
            initialCapacity |= (initialCapacity >>> 16);
            initialCapacity++;

            //如果超出 int 长度是 2^31-1，需要缩短长度
            if (initialCapacity < 0)
                //缩小为 2^30
                initialCapacity >>>= 1;
        }
//        这里对方法中的几个右移距举例解释，假设初始值为二进制 1XXX XXXX XXXX ，
//        1. 第一次右移1位后用0补上空位，01XX XXXX XXXX ，然后进行 或运算 得 11XX XXXX XXXX
//        2. 第二次右移2位，0011 XXXX XXXX ，或运算 得 1111 XXXX XXXX
//        3. 第三次右移4位运算得 1111 1111 XXXX
//        4. .........因为 initialCapacity 的类型是 int ，用二进制补码表示数值，最大值是 2^31 - 1 或者 0x7fffffff ，二进制表示为32位，所以最后一次右移16位后可以补满32位
//        5. 然后 initialCapacity++ ，从 1111 1111 1111 进位为 1 0000 0000 0000 变成 2^12 这样2的12次幂。。。
//        6. 如果 initialCapacity++ 前，已经是 int 最大值，那么实际值超出 int 最大值，变为 int 最小值 -2^31  或者 0x80000000 。因为小于0，所以右移一位变成  2^30  或者 0x40000000 。
        elements = new Object[initialCapacity];
    }

    //双倍扩容，仅在数组填充满的时候扩容
    private void doubleCapacity() {
        //断言：数组已经填满了
        assert head == tail;
        //记录队列头索引
        int p = head;
        //记录队列长度
        int n = elements.length;
        //队列头到数组结尾的元素个数，0---队列尾---null---队列头---数组尾
        int r = n - p;
        //左移1位相当于乘以2，双倍长度
        int newCapacity = n << 1;
        //若超度超出int最大值，抛出异常，所以最大长度 2^30
        if (newCapacity < 0)
            throw new IllegalStateException("Sorry, deque too big");
        //新建数组
        Object[] a = new Object[newCapacity];
        //将p开始往右的元素赋值到新数组
        System.arraycopy(elements, p, a, 0, r);
        //将0到p（因为原数组已填满）的元素复制到新数组
        System.arraycopy(elements, 0, a, r, p);
        elements = a;
        //重新定义新head和tail
        head = 0;
        tail = n;
    }



    //写入流
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();
        s.writeInt(size());
        int mask = elements.length - 1;
        for (int i = head; i != tail; i = (i + 1) & mask)
            s.writeObject(elements[i]);
    }

    //从流中读取
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        allocateElements(size);
        head = 0;
        tail = size;
        for (int i = 0; i < size; i++)
            elements[i] = s.readObject();
    }


    //拷贝队列，将队列拷贝到一个指定数组。
    private <T> T[] copyElements(T[] a) {
        if (head < tail) {
            //如果 头索引比尾索引小，可以直接复制
            System.arraycopy(elements, head, a, 0, size());
        } else if (head > tail) {
            //如果 头索引比尾索引大，循环数组需要分段复制
            int headPortionLen = elements.length - head;
            System.arraycopy(elements, head, a, 0, headPortionLen);
            System.arraycopy(elements, 0, a, headPortionLen, tail);
        }
        //如果 头索引和尾索引一样大（都是0），空队列不需要复制了
        return a;
    }

    //判断当前队列是否符合条件
    private void checkInvariants() {
        assert elements[tail] == null;
        assert head == tail ? elements[head] == null :
                (elements[head] != null &&
                        elements[(tail - 1) & (elements.length - 1)] != null);
        assert elements[(head - 1) & (elements.length - 1)] == null;
    }

    //删除指定下标的元素，因为队列不允许留空，所以需要移动数组
    private boolean delete(int i) {
        checkInvariants();
        final Object[] elements = this.elements;
        final int mask = elements.length - 1;
        final int h = head;
        final int t = tail;
        final int front = (i - h) & mask;
        final int back  = (t - i) & mask;

        // Invariant: head <= i < tail mod circularity
        if (front >= ((t - h) & mask))
            throw new ConcurrentModificationException();

        // Optimize for least element motion
        if (front < back) {
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else { // Wrap around
                System.arraycopy(elements, 0, elements, 1, i);
                elements[0] = elements[mask];
                System.arraycopy(elements, h, elements, h + 1, mask - h);
            }
            elements[h] = null;
            head = (h + 1) & mask;
            return false;
        } else {
            if (i < t) { // Copy the null tail as well
                System.arraycopy(elements, i + 1, elements, i, back);
                tail = t - 1;
            } else { // Wrap around
                System.arraycopy(elements, i + 1, elements, i, mask - i);
                elements[mask] = elements[0];
                System.arraycopy(elements, 1, elements, 0, t);
                tail = (t - 1) & mask;
            }
            return true;
        }
    }



    private class DeqIterator implements Iterator<E> {
        private int cursor = head;
        private int fence = tail;
        private int lastRet = -1;

        public boolean hasNext() {
            return cursor != fence;
        }

        public E next() {
            if (cursor == fence)
                throw new NoSuchElementException();
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor];
            // This check doesn't catch all possible comodifications,
            // but does catch the ones that corrupt traversal
            if (tail != fence || result == null)
                throw new ConcurrentModificationException();
            lastRet = cursor;
            cursor = (cursor + 1) & (elements.length - 1);
            return result;
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            if (delete(lastRet)) { // if left-shifted, undo increment in next()
                cursor = (cursor - 1) & (elements.length - 1);
                fence = tail;
            }
            lastRet = -1;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] a = elements;
            int m = a.length - 1, f = fence, i = cursor;
            cursor = f;
            while (i != f) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
            }
        }
    }

    private class DescendingIterator implements Iterator<E> {

        private int cursor = tail;
        private int fence = head;
        private int lastRet = -1;

        public boolean hasNext() {
            return cursor != fence;
        }

        public E next() {
            if (cursor == fence)
                throw new NoSuchElementException();
            cursor = (cursor - 1) & (elements.length - 1);
            @SuppressWarnings("unchecked")
            E result = (E) elements[cursor];
            if (head != fence || result == null)
                throw new ConcurrentModificationException();
            lastRet = cursor;
            return result;
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            if (!delete(lastRet)) {
                cursor = (cursor + 1) & (elements.length - 1);
                fence = head;
            }
            lastRet = -1;
        }
    }

    static final class DeqSpliterator<E> implements Spliterator<E> {
        private final ArrayDeque<E> deq;
        private int fence;  // -1 until first use
        private int index;  // current index, modified on traverse/split

        /** Creates new spliterator covering the given array and range */
        DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) {
            this.deq = deq;
            this.index = origin;
            this.fence = fence;
        }

        private int getFence() { // force initialization
            int t;
            if ((t = fence) < 0) {
                t = fence = deq.tail;
                index = deq.head;
            }
            return t;
        }

        public DeqSpliterator<E> trySplit() {
            int t = getFence(), h = index, n = deq.elements.length;
            if (h != t && ((h + 1) & (n - 1)) != t) {
                if (h > t)
                    t += n;
                int m = ((h + t) >>> 1) & (n - 1);
                return new DeqSpliterator<>(deq, h, index = m);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements;
            int m = a.length - 1, f = getFence(), i = index;
            index = f;
            while (i != f) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                i = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
            }
        }

        public boolean tryAdvance(Consumer<? super E> consumer) {
            if (consumer == null)
                throw new NullPointerException();
            Object[] a = deq.elements;
            int m = a.length - 1, f = getFence(), i = index;
            if (i != fence) {
                @SuppressWarnings("unchecked") E e = (E)a[i];
                index = (i + 1) & m;
                if (e == null)
                    throw new ConcurrentModificationException();
                consumer.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() {
            int n = getFence() - index;
            if (n < 0)
                n += deq.elements.length;
            return (long) n;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED |
                    Spliterator.NONNULL | Spliterator.SUBSIZED;
        }
    }
}
