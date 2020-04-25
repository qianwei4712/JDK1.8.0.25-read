package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author shiva   2020/3/15 18:46
 * @since   1.2
 */
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 8683452581122892189L;

    //默认初始化容量
    private static final int DEFAULT_CAPACITY = 10;
    //默认空list，当构造方法传入长度为0时返回
    private static final Object[] EMPTY_ELEMENTDATA = {};
    //默认空list，无参构造器时返回的默认列表
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    //数组最大容量，实际值为2^31-1-8，超出会爆OutOfMemoryError。数组除了存放数据外，还有一个length属性，减8为了存放数组长度
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    //底层基于 Object[] 实现，可以存储所有类型。
    transient Object[] elementData;
    //列表长度,并不是Object[]的长度，而是实际列表元素的个数
    private int size;


    /**
     * 指定链表长度的构造方法
     * 当指定长度为0时，返回 static final 默认空链表 EMPTY_ELEMENTDATA
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        }
    }

    //无参构造方法，默认返回空列表
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    //集合参数构造器，顺序初始化
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            if (elementData.getClass() != Object[].class)
                // c.toArray() 返回的数组类型不是 Object[]，则利用 Arrays.copyOf()来构造一个大小为 size 的 Object[] 数组
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            //集合参数长度为0，设置为默认列表
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }


    /*
     * **********************************************************************
     * public方法
     * **********************************************************************
     */


    //添加一个元素
    public boolean add(E e) {
        //确定数组长度，
        ensureCapacityInternal(size + 1);
        //列表长度加1，并在数组末尾复制
        elementData[size++] = e;
        return true;
    }

    //在指定位置添加元素
    public void add(int index, E element) {
        // 判断指定位置是否超出列表位置范围
        rangeCheckForAdd(index);
        ensureCapacityInternal(size + 1);
        //进行数组复制，从指定位置往后移一位
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }

    // 顺序批量添加
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    // 从指定位置开始批量添加
    public boolean addAll(int index, Collection<? extends E> c) {
        // 判断指定位置是否超出列表位置范围
        rangeCheckForAdd(index);
        // 将需添加集合转为数组
        Object[] a = c.toArray();
        // 需要添加的个数
        int numNew = a.length;
        //确定数组长度，长度如果添加 numNew,是否需要扩容
        ensureCapacityInternal(size + numNew);
        //获得开始移动的下标，使用 native 方法进行赋值移动
        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                    numMoved);
        // 将需要添加的数组拷贝到列表数组
        System.arraycopy(a, 0, elementData, index, numNew);
        // 数组长度更改
        size += numNew;
        return numNew != 0;
    }

    // 根据数组下标删除
    public E remove(int index) {
        // 判断下标是否超出
        rangeCheck(index);
        modCount++;
        E oldValue = elementData(index);
        //将删除位置后的子数组往前移动一位
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        elementData[--size] = null;
        return oldValue;
    }

    // 根据元素对象删除
    public boolean remove(Object o) {
        //如果对象为null,用 == 判断，否则用 equals
        //只删除第一个符合条件的元素
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    // 根据数组下标范围删除
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        // 将删除范围之后的子数组前移，到删除范围开始位置
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                numMoved);
        int newSize = size - (toIndex-fromIndex);
        // 重复存在的元素置空
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    // 根据集合批量删除, 移除 list 中和 c 中共有的元素
    public boolean removeAll(Collection<?> c) {
        //判空
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    // 只保留 list 和 集合 c 中公有的元素：和 removeAll() 功能相反
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    //1.8开始加入的新方法，根据条件删除
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);

        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    // list转数组
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // 清空列表
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            elementData[i] = null;
        size = 0;
    }

    // 遍历查询第一个相同元素的位置
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    // 包含方法其实是调用 indexOf
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    // 最后一个相同元素，从后往前遍历
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    // 根据下标获得
    public E get(int index) {
        rangeCheck(index);
        return elementData(index);
    }

    // 替换指定下标元素，并返回旧元素
    public E set(int index, E element) {
        rangeCheck(index);
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    //带比较器的排序方法
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        //记录原本更改次数
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        //如果在排序过程中，其他线程对list进行了增删操作，那么 modCount 会发生变化， 然后丢出异常
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    // 批量操作元素
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            // 实际使用中，通过重写 apply(Object o) 方法，进行对元素的操作
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }


    // 截取list
    public List<E> subList(int fromIndex, int toIndex) {
        //判断下标是否符合
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    //迭代器
    public Iterator<E> iterator() {
        return new Itr();
    }


    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }


    //遍历数组，拿到每一个并进行操作，并不修改原数组
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }


    // 若数组长度大于元素个数，重新创建一个大小为元素个数的数组，节省空间
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0)
                    ? EMPTY_ELEMENTDATA
                    : Arrays.copyOf(elementData, size);
        }
    }

    // 返回列表长度
    public int size() {
        return size;
    }

    // 判断列表是否为空
    public boolean isEmpty() {
        return size == 0;
    }

    // 克隆方法
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    // 主动扩容
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) ? 0 : DEFAULT_CAPACITY;
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /*
     * **********************************************************************
     * private方法
     * **********************************************************************
     */


    // 扩容数组，判断是否为默认空数组
    private void ensureCapacityInternal(int minCapacity) {
        //如果当前数组为默认空数组，扩容数组长度，最小为10
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    // 修改计数，判断是否需要增加数组长度
    private void ensureExplicitCapacity(int minCapacity) {
        // 操作计数，此参数继承自 AbstractList，用来记录列表的增删操作次数
        // 实际作用为，在线程不安全的对象中，进行简单的线程安全判断。
        modCount++;
        // 如果需求长度大于当前数组长度，进行扩容
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    // 实际扩容方法，
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        // 运算符 >> 是带符号右移. 如 oldCapacity = 10,则 newCapacity = 10 + (10 >> 1) = 10 + 5 = 15
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            // 若 newCapacity 依旧小于 minCapacity，直接赋值
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            // 若超出最大长度，直接赋值为最大值
            newCapacity = hugeCapacity(minCapacity);
        // 重新创建指定长度的数组，并将原数组复制过去
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }


    // 判断指定位置是否超出列表位置范围
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    // 编写异常信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    // 判断需要删除的下标是否超出长度
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    //返回指定位置元素
    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    // 快速删除指定位置的元素，删除之后，指定位置之后的子数组前移
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                    numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }

    // 批量删除，true-保留并集，false-保留差集
    private boolean batchRemove(Collection<?> c, boolean complement) {
        // 重新创建一个数组保存原数组
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                // 如果 c 包含原数组元素并且模式为保留并集，将该元素赋值到新数组
                // 如果 c 不含原数组元素并且模式为差集，将该元素赋值到新数组
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            //这个if，官方的注释是为了保持和AbstractCollection的兼容性
            //假如上面c.contains抛出了异常，导致for循环终止，那么必然会导致r != size
            //所以0-w之间是需要保留的数据，同时从w索引开始将剩下没有循环的数据(也就是从r开始的)拷贝回来，也保留
            if (r != size) {
                System.arraycopy(elementData, r,
                        elementData, w,
                        size - r);
                w += size - r;
            }
            //for循环完毕，检测了所有的元素，0-w之间保存了需要留下的数据，w开始以及后面的数据全部清空
            if (w != size) {
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }






























    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        int expectedModCount = modCount;
        s.defaultWriteObject();
        s.writeInt(size);
        for (int i=0; i<size; i++) {
            s.writeObject(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }


    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;
        s.defaultReadObject();
        s.readInt(); // ignored
        if (size > 0) {
            ensureCapacityInternal(size);
            Object[] a = elementData;
            for (int i=0; i<size; i++) {
                a[i] = s.readObject();
            }
        }
    }


    /*
     * **********************************************************************
     * 内部类List开始
     * **********************************************************************
     */
    private class Itr implements Iterator<E> {
        // 标记字段，达到指针效果
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        // 记录变化值，简单的线程安全判断
        int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();
            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        // 简单的线程安全判断，并不真正有效
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    /*
     * **********************************************************************
     * 内部类List结束
     * **********************************************************************
     */


    /*
     * **********************************************************************
     * 内部类ListItr开始
     * **********************************************************************
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
    /*
     * **********************************************************************
     * 内部类ListItr结束
     * **********************************************************************
     */

    /*
     * **********************************************************************
     * 内部类SubList开始
     * **********************************************************************
     */
    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }
        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }
        public int size() {
            checkForComodification();
            return this.size;
        }
        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }
        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }
        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                    parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }
        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize==0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }
        public Iterator<E> iterator() {
            return listIterator();
        }
        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != ArrayList.SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= ArrayList.SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = ArrayList.SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        ArrayList.SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }
        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+this.size;
        }
        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }
        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayList.ArrayListSpliterator<E>(ArrayList.this, offset,
                    offset + this.size, this.modCount);
        }
    }
    /*
     * **********************************************************************
     * 内部类SubList结束
     * **********************************************************************
     */




    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        private final ArrayList<E> list;
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayList.ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // divide range in half unless too small
                    new ArrayList.ArrayListSpliterator<E>(list, lo, index = mid,
                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}

