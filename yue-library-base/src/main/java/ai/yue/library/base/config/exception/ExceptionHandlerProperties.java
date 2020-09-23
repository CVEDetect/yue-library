package ai.yue.library.base.config.exception;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 全局统一异常处理自动配置属性
 * 
 * @author	ylyue
 * @since	2018年11月6日
 */
@Data
@ConfigurationProperties("yue.exception-handler")
public class ExceptionHandlerProperties {
	
	/**
	 * 是否启用 <code style="color:red">全局统一异常处理</code> 自动配置
	 * <p>
	 * 默认：true
	 */
	private boolean enabled = true;
	
}
