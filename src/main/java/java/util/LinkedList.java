package java.util;

import java.util.function.Consumer;

/**
 * @author shiva   2020/3/23 19:47
 * @since 1.2
 */
public class LinkedList<E>
        extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 876323262645176354L;

    //list长度，默认为0
    transient int size = 0;
    //第一个节点
    transient Node<E> first;
    //最后一个节点
    transient Node<E> last;

    /*
     * **********************************************************************
     * 构造方法
     * **********************************************************************
     */

    //无参构造器，所有节点默认为null
    public LinkedList() {
    }

    // 集合参数构造器
    public LinkedList(Collection<? extends E> c) {
        // 调用无参构造器，先创建list
        this();
        // 调用批量添加
        addAll(c);
    }



    /*
     * **********************************************************************
     * public方法
     * **********************************************************************
     */

    //获得第一个节点
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    //获得最后一个节点
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    //获得链表长度
    public int size() {
        return size;
    }

    //添加元素，顺序添加
    public boolean add(E e) {
        //将传入元素标为最后一个节点
        linkLast(e);
        return true;
    }

    //在链表开头添加元素，和add()方法类似
    public void addFirst(E e) {
        linkFirst(e);
    }

    //在链表末尾添加元素，和add()方法相同
    public void addLast(E e) {
        linkLast(e);
    }

    //在指定位置添加元素
    public void add(int index, E element) {
        //判断索引位置是否可用
        checkPositionIndex(index);
        if (index == size)
            //如果添加位置和长度相同，当前最大索引为size-1，则添加在末尾
            linkLast(element);
        else
            //在原索引位置的节点前面插入新节点
            linkBefore(element, node(index));
    }

    //批量添加
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    //从指定位置开始批量添加
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);
        // 将需添加集合转为数组
        Object[] a = c.toArray();
        //获得需添加数组的长度
        int numNew = a.length;
        if (numNew == 0)
            return false;

        //定义添加位置的上一个节点和当前节点引用
        Node<E> pred, succ;
        if (index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }
        //遍历数组，顺序开始插入节点
        for (Object o : a) {
            @SuppressWarnings("unchecked")
            E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null);
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            pred = newNode;
        }
        //处理原节点的引用
        if (succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        modCount++;
        return true;
    }

    //移除链表第一个节点
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    //移除链表最后一个节点
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    // 根据元素对象删除第一个
    public boolean remove(Object o) {
        //如果对象为null,用 == 判断，否则用 equals
        //只删除第一个符合条件的元素
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    //根据索引位置删除
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    // 清空
    public void clear() {
        //官方注释：清理节点之间的引用不是必须的，但是清理引用可以帮助 GC 回收
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }

    //移除头节点，实际上是调用 removeFirst
    public E remove() {
        return removeFirst();
    }

    //移除第一个出现的元素，内部调用了 remove(Object o)
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }


    //移除最后一个出现的元素，倒序遍历
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    //根据索引获得节点的元素值
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    //根据索引更换节点元素值，并返回原值
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    // 判断是否包含指定元素
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    // 根据对象获取第一个索引位置
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }

    // 根据对象获取最后一个索引位置，倒序检查
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }

    //返回第一个节点的值
    public E element() {
        return getFirst();
    }

    //返回第一个节点的值
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    //移除第一个节点，并返回第一个节点的值
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    //添加节点
    public boolean offer(E e) {
        return add(e);
    }

    //在队列顶部添加节点
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    //在队列底部添加节点
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    // 返回第一个节点的元素值，不移除节点
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    // 返回最后一个节点的元素值，不移除节点
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    //返回第一个节点的元素值，并移除节点
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    // 返回最后一个节点的元素值，并移除节点
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    // 往队列顶部添加节点
    public void push(E e) {
        addFirst(e);
    }

    // 从队列顶部弹出节点
    public E pop() {
        return removeFirst();
    }

    // 将链表转为数组
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    //泛型方法，转化并存入指定数组
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        //如果a的长度小于集合的大小的话，通过反射创建一个和集合同样大小的数组
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        //把集合中的所有元素添加到数组中
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        //如果数组的元素的个数大于集合中元素的个数，则把a[size]设置为空
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // 拷贝链表，只进行了浅拷贝
    public Object clone() {
        //获得一个克隆链表，调用 Object clone 方法
        LinkedList<E> clone = superClone();
        //将链表设置为原始状态
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;
        //通过遍历添加方式，进行引用复制
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);
        return clone;
    }

    //双向迭代器
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    //
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }


    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }


    /*
     * **********************************************************************
     * private方法
     * **********************************************************************
     */

    // 将元素标定为最后一个节点
    void linkLast(E e) {
        //原本最后一个节点
        final Node<E> l = last;
        //以E为值，构造新节点
        final Node<E> newNode = new Node<>(l, e, null);
        //将新节点作为最后一个节点
        last = newNode;
        //若原链表为空（最后一个节点为空），将新节点设为第一个节点
        //否则将新节点设置为，原节点的下一个节点
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        //链表长度+1
        size++;
        //操作次数+1
        modCount++;
    }

    // 将元素标定为第一个节点
    private void linkFirst(E e) {
        //原本第一个节点
        final Node<E> f = first;
        //以E为值，构造新节点
        final Node<E> newNode = new Node<>(null, e, f);
        //将新节点作为第一个节点
        first = newNode;
        //若原链表为空（第一个节点为空），将新节点设为最后一个节点
        //否则将新节点设置为，原节点的上一个节点
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        //链表长度+1
        size++;
        //操作次数+1
        modCount++;
    }

    //返回指定索引的节点
    Node<E> node(int index) {
        //判断插入的位置在链表前半段或者是后半段
        //对正数，右移1位相当于除以2，保留整数
        if (index < (size >> 1)) {
            // 前半段顺序遍历
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            // 后半段倒序遍历
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    // 在指定节点前插入新节点
    void linkBefore(E e, Node<E> succ) {
        //获取原节点的上一个
        final Node<E> pred = succ.prev;
        //根据需要插入的元素，创建新节点，
        final Node<E> newNode = new Node<>(pred, e, succ);
        //将新节点设为原节点的前一个
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        //链表长度+1
        size++;
        //操作次数+1
        modCount++;
    }

    // 移除第一个节点，并返回节点元素值
    private E unlinkFirst(Node<E> f) {
        // 保留需要返回的元素值
        final E element = f.item;
        // 获取第二个节点的索引
        final Node<E> next = f.next;
        // 官方注释 帮助GC，以后使用完的对象应该设置为null,帮助 GC尽快回收
        f.item = null;
        f.next = null; // help GC
        // 重新设置链表头
        first = next;
        //判断设置链表尾
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        modCount++;
        return element;
    }

    //移除最后一个节点，并返回节点元素值，和 unlinkFirst 基本相同，不写了。。
    private E unlinkLast(Node<E> l) {
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }


    // 移除指定节点
    E unlink(Node<E> x) {
        // 保存前后节点和当前节点元素值引用
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;
        //前后判空，重新设置 first 和 last 引用
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        //设置为空，帮助 GC尽快回收
        x.item = null;
        size--;
        modCount++;
        return element;
    }

    // 调用 Object clone() 方法
    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }


    //判断索引节点是否可用
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //判断索引节点是否可用
    // 和 isPositionIndex 区别就是，长度超出 1 是否可用
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    // 判断传入索引是否可用，否则抛出异常
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    // 判断链表索引是否在合适范围
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    // 索引超出错误信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    //流操作，写入
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();
        // Write out size
        s.writeInt(size);
        // Write out all elements in the proper order.
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }
    //流操作，读取
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();
        // Read in size
        int size = s.readInt();
        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }


    /*
     * **********************************************************************
     * 内部类Node
     * **********************************************************************
     */
    private static class Node<E> {
        // 当前节点对象
        E item;
        // 下一个节点对象
        Node<E> next;
        // 上一个节点对象
        Node<E> prev;
        // 构造方法为全参构造器
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
    /*
     * **********************************************************************
     * 内部类Node
     * **********************************************************************
     */


    //双向迭代
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    //逆向迭代器
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedList<E> list; // null OK unless traversed
        Node<E> current;      // current node; null until initialized
        int est;              // size estimate; -1 until first needed
        int expectedModCount; // initialized when est set
        int batch;            // batch size for splits

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}