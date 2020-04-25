package java.util;

/**
 * 栈类，从 JDK1.0 开始存在，已经不被推荐使用
 * 推荐使用 ArrayDeque 实现栈。
 * @author shiva   2020/3/31 21:16
 */
public
class Stack<E> extends Vector<E> {
    //无参构造器
    public Stack() {
    }
    //将元素压入栈，并返回，调用 Vector的添加方法
    public E push(E item) {
        addElement(item);
        return item;
    }

    //弹出栈顶的元素
    public synchronized E pop() {
        E       obj;
        int     len = size();
        obj = peek();
        removeElementAt(len - 1);
        return obj;
    }

    //只返回栈顶端的元素，不弹出该元素（空栈会抛出异常）
    public synchronized E peek() {
        int  len = size();
        if (len == 0)
            throw new EmptyStackException();
        return elementAt(len - 1);
    }

    //判断是否为空栈
    public boolean empty() {
        return size() == 0;
    }

    //返回最靠近顶端的目标元素到顶端的距离
    public synchronized int search(Object o) {
        int i = lastIndexOf(o);
        if (i >= 0) {
            return size() - i;
        }
        return -1;
    }
    //序列化
    private static final long serialVersionUID = 1224463164541339165L;
}
