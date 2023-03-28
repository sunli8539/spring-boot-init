package com.smartai.common.parallel;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ExecutionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionUtil.class);

    // 核心大小
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;

    // 线程池最大
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;

    // 队列长度
    private static final int QUEUE_SIZE = 64;

    // 线程池超时回收（s）
    private static final int KEEP_ALIVE_TIME = 10;

    /**
     * 并行任务超时时间
     */
    public static final int TIMEOUT = 10000;

    /* 线程池  */
    private static MyThreadPool executor;

    /**
     * 获取线程池
     *
     * @return executor
     */
    public static synchronized ExecutorService getExecutor() {
        if (executor == null) {
            executor = new MyThreadPool("CompletableFuturePool", CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS, QUEUE_SIZE);

            executor.setLoadRateChangeListener(((event, loadRate) -> {
                if (event.equals(MyThreadPool.EVENT.EVENT_OVERFLOW)) {
                    // 示例:loadRate event:EVENT_CHANGE,0.04
                    // 超过阈值,在日志云上设置报警
                    LOGGER.warn(" -executor load rate event:{},{}", event, loadRate);
                }
            }));
            executor.setRejectedExecutionHandler(new CallerRunsRejectPolicy());

            executor.setExecuteLister(new MyThreadPool.ExecuteLister() {
                @Override
                public void beforeExecute(MyThreadPool executor, Thread thread, Runnable runnable) {
                    buildExecutionLog(executor);
                }

                @Override
                void afterExecute(MyThreadPool executor, Runnable runnable, Throwable throwable) {

                }
            });
        }
        return executor;
    }

    /**
     * 重写拒绝策略
     */
    private static class CallerRunsRejectPolicy extends ThreadPoolExecutor.CallerRunsPolicy {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            LOGGER.error("Thread Pool is full, reject task to main thread execute");
            super.rejectedExecution(r, e);
        }
    }

    /**
     * 获取线程池的参数
     *
     * @param isDefault true 默认是获取ForkJoinPool.commonPool()的状态，false是自定义的线程池状态
     * @return String
     */
    public static Object getExecutorServiceStatus(boolean isDefault) {
        if (!isDefault) {
            MyThreadPool temp = null;
            try {
                temp = (MyThreadPool) getExecutor();
            } catch (ClassCastException exception) {
                LOGGER.error("ClassCastException:" + exception.getMessage());
                throw new RuntimeException(exception.getMessage());
            }
            return buildExecutionLog(temp);
        } else {
            ForkJoinPool temp = ForkJoinPool.commonPool();
            return temp.toString();
        }
    }

    /**
     * 生成线程池状态
     *
     * @param poolExecutor poolExecutor
     * @return ExecutionLog ExecutionLog
     */
    public static ExecutionLog buildExecutionLog(MyThreadPool poolExecutor) {
        if (ObjectUtils.isEmpty(poolExecutor)) {
            throw new RuntimeException("poolExecutor is empty");
        }
        ExecutionLog executionLog = new ExecutionLog();
        executionLog.setType(ExecutionLog.TYPE_BEGIN);
        executionLog.setExecutorName(poolExecutor.getName());
        executionLog.setCreateTime(new Date());
        executionLog.setActiveThreads(poolExecutor.getActiveCount());
        executionLog.setExternInfo(poolExecutor.toString());
        executionLog.setCompletedTasks(poolExecutor.getCompletedTaskCount());
        executionLog.setPoolSize(poolExecutor.getPoolSize());
        executionLog.setQueuedTasks(poolExecutor.getQueue().size());
        executionLog.setCoreSize(poolExecutor.getCorePoolSize());
        executionLog.setQueueCapatity(poolExecutor.getQueue().size());
        executionLog.setMaxSize(poolExecutor.getMaximumPoolSize());
        executionLog.setPoolSize(poolExecutor.getPoolSize());
        executionLog.setTimeout((int) poolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            String hostname = addr.getHostName();
            executionLog.setIp(ip);
            executionLog.setContainerName(hostname);
        } catch (UnknownHostException exp) {
            LOGGER.error("buildExecutionLog error is {}", exp.getMessage());
        }
        return executionLog;
    }

    /**
     * 包装CompletableFuture.supplyAsync，并使用自定义线程池
     *
     * @param supplier supplier
     * @param <T>      泛型
     * @return CompletableFuture<T>
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return supplier.get();
        }, getExecutor()).handle((result, e) -> {
            if (e != null) {
                LOGGER.error("runAsync run error:{}", e);
                throw new RuntimeException("runAsync run error");
            }
            return result;
        });
    }

    /**
     * 包装CompletableFuture.runAsync，并使用自定义线程池
     *
     * @param runnable runnable
     * @return CompletableFuture<Void>
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            runnable.run();
        }, getExecutor()).handle((result, e) -> {
            if (e != null) {
                LOGGER.error("runAsync run error:{}", e);
                throw new RuntimeException("runAsync run error");
            }
            return result;
        });
    }

    /**
     * 处理并行任务执行异常
     *
     * @param exception ExecutionException
     * @return RuntimeException
     */
    public static RuntimeException dealExecutionException(ExecutionException exception) {
        if (ObjectUtils.isEmpty(exception)) {
            throw new RuntimeException("ExecutionException is empty");
        }
        Throwable cause = exception.getCause();
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        } else {
            LOGGER.error("ExecutionException", exception.getCause());
            return new RuntimeException(exception.getCause());
        }
    }
}
