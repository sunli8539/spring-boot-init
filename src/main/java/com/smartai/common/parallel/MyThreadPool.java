package com.smartai.common.parallel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 *
 * @author swx1160681
 * @since 2022-07-04
 **/
public final class MyThreadPool extends ThreadPoolExecutor {

    private String name;

    /**
     * 最大负载率
     */
    private float maxLoadRate = 0.8f;

    /**
     * 最小负载率
     */
    private float minLoadRate = 0;

    /**
     * 当前负载
     */
    private float loadRate = 0;

    private LoadRateChangeListener loadRateChangeListener;

    private ExecuteLister executeLister;

    /**
     * 构造函数
     *
     * @param name ThreadPoolExecutor name
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param queueSize queueSize
     */
    public MyThreadPool(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        int queueSize) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueSize));
        this.name = name;
    }

    /**
     * 构造函数
     *
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param queueSize queueSize
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueSize) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(queueSize));
        this.name = "PBIThreadPool@" + hashCode();
    }

    /**
     * 构造函数
     *
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param workQueue workQueue
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.name = "PBIThreadPool@" + hashCode();
    }

    /**
     * 构造函数
     *
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param workQueue workQueue
     * @param threadFactory threadFactory
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.name = "PBIThreadPool@" + hashCode();
    }

    /**
     * 构造函数
     *
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param workQueue workQueue
     * @param handler handler
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.name = "PBIThreadPool@" + hashCode();
    }

    /**
     * 构造函数
     *
     * @param corePoolSize corePoolSize
     * @param maximumPoolSize maximumPoolSize
     * @param keepAliveTime keepAliveTime
     * @param unit unit
     * @param workQueue workQueue
     * @param threadFactory threadFactory
     * @param handler handler
     */
    public MyThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.name = "PBIThreadPool@" + hashCode();
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        Float currRate = calcLoadRate();
        // 负载率阈值判断
        int result = currRate.compareTo(minLoadRate);
        if (result < 0) {
            loadRateChangeListener.onLoadRateChange(EVENT.EVENT_BELOW, currRate);
        } else if (result > 0) {
            loadRateChangeListener.onLoadRateChange(EVENT.EVENT_OVERFLOW, currRate);
        }
        if (result != 0) {
            loadRate = currRate;
            // 触发负载变化事件
            if (loadRateChangeListener != null) {
                loadRateChangeListener.onLoadRateChange(EVENT.EVENT_CHANGE, loadRate);
            }
        }
        executeLister.beforeExecute(this, thread, runnable);
        super.beforeExecute(thread, runnable);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        executeLister.afterExecute(this, runnable, throwable);
        super.afterExecute(runnable, throwable);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取负载
     *
     * @return 负载率
     */
    public float getLoadRate() {
        return this.loadRate;
    }

    /**
     * 计算当前负载
     *
     * @return 负载率
     */
    public float calcLoadRate() {
        return ((float) getActiveCount()) / getMaximumPoolSize();
    }

    public float getMaxLoadRate() {
        return maxLoadRate;
    }

    public void setMaxLoadRate(float maxLoadRate) {
        this.maxLoadRate = maxLoadRate;
    }

    public float getMinLoadRate() {
        return minLoadRate;
    }

    public void setMinLoadRate(float minLoadRate) {
        this.minLoadRate = minLoadRate;
    }

    public LoadRateChangeListener getLoadRateChangeListener() {
        return loadRateChangeListener;
    }

    public void setLoadRateChangeListener(LoadRateChangeListener loadRateChangeListener) {
        this.loadRateChangeListener = loadRateChangeListener;
    }

    public ExecuteLister getExecuteLister() {
        return executeLister;
    }

    public void setExecuteLister(ExecuteLister executeLister) {
        this.executeLister = executeLister;
    }

    /**
     * 线程池
     */
    public interface LoadRateChangeListener {

        /**
         * 负载变化监听
         *
         * @param event event
         * @param loadRate 负载
         */
        void onLoadRateChange(EVENT event, float loadRate);
    }

    /**
     * 线程池执行任务时机监听
     */
    public static abstract class ExecuteLister {

        /**
         * 任务执行前出发事件
         *
         * @param executor executor
         * @param thread thread
         * @param runnable runnable
         */
        abstract void beforeExecute(MyThreadPool executor, Thread thread, Runnable runnable);

        /**
         * 任务执行后触发事件
         *
         * @param executor executor
         * @param runnable runnable
         * @param throwable throwable
         */
        abstract void afterExecute(MyThreadPool executor, Runnable runnable, Throwable throwable);
    }

    /**
     * 线程池事件类型
     */
    public enum EVENT {

        /**
         * 超出阈值事件
         */
        EVENT_CHANGE,

        /**
         * 超出阈值事件
         */
        EVENT_OVERFLOW,

        /**
         * 低于阈值事件
         */
        EVENT_BELOW
    }
}
