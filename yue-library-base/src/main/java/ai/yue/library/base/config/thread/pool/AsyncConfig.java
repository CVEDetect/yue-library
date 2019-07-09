package ai.yue.library.base.config.thread.pool;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

/**
 * <b>异步线程池</b>
 * <p>
 * 共用父线程上下文环境，异步执行任务时不丢失token
 * @author 	 孙金川
 * @version 创建时间：2017年10月13日
 */
@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(AsyncProperties.class)
@ConditionalOnProperty(prefix = "yue.thread-pool.async", name = "enabled", havingValue = "true")
public class AsyncConfig implements AsyncConfigurer {
	
	@Autowired
	AsyncProperties asyncProperties;
	
	/**
	 * 自定义异常处理类
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncUncaughtExceptionHandler() {
			@Override
			public void handleUncaughtException(Throwable arg0, Method arg1, Object... arg2) {
				log.error("=========================={}=======================", arg0.getMessage(), arg0);
				log.error("exception method: {}", arg1.getName());
				for (Object param : arg2) {
		        	log.error("Parameter value - {}", param);
		        }
			}
		};
	}
	
	/**
	 * 异步线程池<br>
	 * 实现AsyncConfigurer接口并重写getAsyncExecutor方法，返回一个ThreadPoolTaskExecutor，这样我们就获得了一个基本线程池TaskExecutor。
	 */
	@Override
	public Executor getAsyncExecutor() {
		ContextAwareAsyncExecutor executor = new ContextAwareAsyncExecutor();
		executor.setThreadNamePrefix(asyncProperties.getThreadNamePrefix());// 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
		executor.setCorePoolSize(asyncProperties.getCorePoolSize());// 核心线程数10：线程池创建时候初始化的线程数
		executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());// 最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
		executor.setQueueCapacity(asyncProperties.getQueueCapacity());// 缓冲队列200：用来缓冲执行任务的队列
		executor.setKeepAliveSeconds(asyncProperties.getKeepAliveSeconds());// 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
		executor.setWaitForTasksToCompleteOnShutdown(asyncProperties.getWaitForTasksToCompleteOnShutdown());// 用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean。
		executor.setAwaitTerminationSeconds(asyncProperties.getAwaitTerminationSeconds());// 该方法用来设置线程池中任务的等待时间，如果超过这个时间还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
		executor.setRejectedExecutionHandler(asyncProperties.getRejectedExecutionHandlerPolicy().getRejectedExecutionHandler());// 线程池拒绝策略：CallerRunsPolicy，当线程池的所有线程都已经被占用时，若原始线程未关闭将由原始线程来执行任务，否则该任务将被丢弃。
		executor.initialize();
		return executor;
	}
	
}