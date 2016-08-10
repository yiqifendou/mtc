package com.haiziwang.platform.mtc.core;

/**
 * 功能和AtomicInteger原子性操作差不多
 * @author yiqifendou
 *
 */
public final class SafelyInteger {
	
	/**
	 * 只允许同包下创建该对象
	 */
	SafelyInteger() {
	}

	SafelyInteger(int initValue) {
		this.value = initValue;
	}

	private int value = 0;

	public final synchronized int incrementAndGet() {
		return ++value;
	}

	public final synchronized int decrementAndGet() {
		return --value;
	}
	
	public final int get(){
		return value;
	}
}
