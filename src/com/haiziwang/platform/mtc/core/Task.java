package com.haiziwang.platform.mtc.core;

import com.haiziwang.platform.mtc.exception.ExceptionUtil;

/**
 * 很多成员属性不定义成private，并提供get/set方法，是因为不希望其他包可以访问到这些成员属性
 * @author yiqifendou
 *
 * @param <R>
 */
public abstract class Task<R> implements Runnable{
	TaskAction<R> ta;
	public abstract R excute() throws Exception;
	
	@Override
	public void run() {
		try {
			ta.result.add(excute());
		} catch (Exception e) {
			//任务子线程发生了异常
			ta.happenException = true;
			//打印异常栈信息
			ExceptionUtil.printStackTrace(e);
		}
		if(ta.cc.incrementAndGet()==ta.size()){
			//所有任务子线程已完成
			/**
			 * 标号002,
			 * 标号002,  模拟任务子线程比较耗时，同步汇总线程早已进入等待结果集状态。
			 * (标号001和标号002的地方请放开一块，另一块注释掉，分别模拟两种不同情况)
			 */
			/*try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			try {
				ta.lock.lock();
				if(!ta.waitResult){
					//同步汇总线程未进入等待结果的状态,等待同步汇总线程进入结果集等待状态
					ta.waitResultCondition.await();
				}
				ta.complementCondition.signal();
			} catch (InterruptedException e) {
				e.printStackTrace();
				//ignore
			}finally{
				ta.lock.unlock();
			}
		}
	}
}
