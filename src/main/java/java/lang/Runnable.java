package java.lang;

/**
 * <p>Runnable接口应由任何类实现，其实例将由线程执行。 该类必须定义一个无参数的方法，称为run 。
 * @since   JDK1.0
 */
@FunctionalInterface
public interface Runnable {
    /**
     * 当实现接口的对象Runnable被用来创建一个线程，启动线程使对象的run在独立执行的线程中调用的方法。
     * <p>方法run的一般合同是它可以采取任何行动。
     */
    public abstract void run();
}
