package com.xw.gateway.filter;

import com.xw.gateway.context.Context;

/**
 * 抽象的链表形式的过滤器
 * @param <T>
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context> {

	//	链表中的下一个过滤器元素
	protected AbstractLinkedProcessorFilter<T> next = null;
	
	@Override
	public void fireNext(Context ctx, Object... args) {
		if (next != null) {
			if(!next.check(ctx)) {
				next.fireNext(ctx, args);
			} else {
				next.filter(ctx, args);
			}
		}
		//	没有下一个节点了，已经到了链表的最后一个节点
	}

	@Override
	public void prepareForFilter(Context ctx, Object... args) {
		//	子类调用：这里就是真正执行下一个节点(元素)的操作
		filter(ctx, args);
	}

	public void setNext(AbstractLinkedProcessorFilter<T> next) {
		this.next = next;
	}
	
	public AbstractLinkedProcessorFilter<T> getNext() {
		return next;
	}
	
}
