package com.xw.gateway.filter.pre;

import com.xw.constants.Protocol;
import com.xw.gateway.context.AttributeKey;
import com.xw.gateway.context.Context;
import com.xw.gateway.context.GatewayContext;
import com.xw.gateway.context.GatewayRequest;
import com.xw.gateway.filter.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 超时的前置过滤器
 */
@Filter(
		id = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID,
		name = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_NAME,
		value = ProcessorFilterType.PRE,
		order = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ORDER
)
public class TimeoutPreFilter extends AbstractEntryProcessorFilter<TimeoutPreFilter.Config> {

	public TimeoutPreFilter() {
		super(Config.class);
	}

	/**
	 * 超时的过滤器核心方法实现
	 * @param ctx
	 * @param args
	 * @throws Throwable
	 */
	@Override
	public void filter(Context ctx, Object... args) {
		try {
			GatewayContext gatewayContext = (GatewayContext)ctx;
			String protocol = gatewayContext.getProtocol();
			Config config = (Config) args[0];
			switch (protocol) {
				case Protocol.HTTP:
					GatewayRequest gatewayRequest = gatewayContext.getRequest();
					gatewayRequest.setRequestTimeout(config.getTimeout());
					break;
				case Protocol.DUBBO:
					// DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker)gatewayContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
					// dubboServiceInvoker.setTimeout(config.getTimeout());
					break;
				default:
					break;
			}			
		} finally {
			//	非常重要的，一定要记得：驱动我们的过滤器链表
			fireNext(ctx, args);
		}
	}
	
	@Getter
	@Setter
	public static class Config extends FilterConfig {

		/**
		 * 超时时间
		 */
		private Integer timeout;

	}
	
}
