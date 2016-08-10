package com.haiziwang.platform.mtc.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.haiziwang.platform.mtc.exception.TaskException;
/**
 * 很多成员属性不定义成private，并提供get/set方法，是因为不希望其他包可以访问到这些成员属性
 * @author yiqifendou
 *
 * @param <R>
 */
public class TaskAction<R> extends ArrayList<Task<R>>{
	private static final long serialVersionUID = 1L;
	
	//是否使用线程池来进行计算
	private boolean userThreadPool = true;
	
	//同步汇总线程是否进入结果集等待状态
	boolean waitResult = false;
	
	//任务子线程执行的时候是否发生异常
	boolean happenException = false;
	
	Lock lock = new ReentrantLock();
	//Condition效率比obj.wait()、obj.notify()高
	Condition complementCondition = lock.newCondition();
	Condition waitResultCondition = lock.newCondition();
	
	//最终汇总的结果集
	Vector<R> result = new Vector<R>();
	
	//子线程任务已完成数量，线程安全
	SafelyInteger cc = new SafelyInteger();
	
	
	public TaskAction(){}
	
	public TaskAction(boolean userThreadPool){
		this.userThreadPool = userThreadPool;
	}
	
	/**
	 * 为了可以addTask(t0).addTask(t1).addTask(t2)...
	 * @param task
	 * @return
	 */
	public TaskAction<R> addTask(Task<R> task){
		super.add(task);
		return this;
	}
	
	public List<R> doTasks(){
		ExecutorService service = null;
		for(int i=0;i<this.size();i++){
			Task<R> task = get(i);
			task.ta = this;
			
			//建议使用线程池的方式执行任务，因为防止创建了过多线程，从而影响效率和内存，线程池内部线程数建议是cpu内核数量,不超过内核2倍
			if(userThreadPool){
				//使用线程池的方式执行任务,Runtime.getRuntime().availableProcessors()为cpu核数，充分利用多个cpu啦
				service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				service.execute(task);
			}else{
				//使用独立线程执行任务
				new Thread(task).start();
			}
			
		}
		
		/**
		 * 标号001,
		 * 模拟同步汇总线程比较倒霉，一直未得到cpu时间片，还未进入结果集等待状态，而所有任务子线程已经执行完毕了。
		 * (标号001和标号002的地方请放开一块，另一块注释掉，分别模拟两种不同情况)
		 */
		/*try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
		
		try {
			lock.lock();
			waitResult = true;
			waitResultCondition.signal();
			complementCondition.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			//ignore
		}finally{
			lock.unlock();
			//如果使用的是线程池的方式，请记得最后要shutdown
			if(userThreadPool && null!=service){
				service.shutdown();
			}
		}
		
		//子线程是否发生异常
		if(happenException){
			//其实可以new Vector()来收集每个任务线程的异常堆栈信息，最后由汇总线程打出来，具体你们可以自己去实现
			throw new TaskException();
		}
		return result;
	}
	
	
	@SuppressWarnings("all")
	public R doTasksForSingleResult(){
		List<R> rList = doTasks();
		//基本不会出现null的情况
		R result = rList.get(0);
		if(result instanceof Integer){
			Integer total = 0;
			for(R r:rList){
				total+= (Integer)r;
			}
			result = (R) total;
		}else if(result instanceof Long){
			Long total = 0L;
			for(R r:rList){
				total+= (Long)r;
			}
			result = (R) total;
		}else if(result instanceof Collection){
			Collection c = (Collection)result;
			for(R r:rList){
				c.addAll((Collection)r);
			}
			result = (R)c;
		}else{
			throw new RuntimeException("不知道如何进行汇总计算");
		}
		return result;
	}
	
	
}

