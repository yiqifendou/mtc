package com.haiziwang.platform.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.haiziwang.platform.mtc.core.Task;
import com.haiziwang.platform.mtc.core.TaskAction;
import com.haiziwang.platform.mtc.media.Media;

@SuppressWarnings("all")
public class JunitTest {
	
	/**
	 * 测试单线程和多线程计算1到4294967295的和平均耗时(10次)
	 */
	@Test
	public void testSum(){
		System.out.println("***计算1到4294967295的和平均耗时（10次）***");
		
		Long s0 = System.currentTimeMillis();
		for(int i=0;i<10;i++){
			TaskAction<Long> ta = new TaskAction<Long>();
			//1-1073741823
			//1073741824-2147483646
			//2147483647-3221225469
			//3221225470-4294967295
			
			Task<Long> task0 = new Task<Long>() {
				@Override
				public Long excute() {
					long sum = 0;
					for (long j = 1L; j <=1073741823 ; j++) {
						sum += j;
					}
					return sum;
				}
			};
			
			Task<Long> task1 = new Task<Long>() {
				@Override
				public Long excute() {
					long sum = 0;
					for (long j = 1073741824L; j <= 2147483646L; j++) {
						sum += j;
					}
					return sum;
				}
			};
			
			Task<Long> task2 = new Task<Long>() {
				@Override
				public Long excute() {
					long sum = 0;
					for (long j =2147483647L; j <= 3221225469L; j++) {
						sum += j;
					}
					return sum;
				}
			};
			
			Task<Long> task3 = new Task<Long>() {
				@Override
				public Long excute() {
					long sum = 0;
					for (long j = 3221225470L; j <= 4294967295L; j++) {
						sum += j;
					}
					return sum;
				}
			};
			
			// 汇总多线程计算的结果
			List<Long> resultList = ta.addTask(task0).addTask(task1).addTask(task2).addTask(task3).doTasks();
			Long total = 0L; 
			for(Long ti:resultList){
				total+=ti; 
			} 
			System.out.println(total);
		}
		long s1 = System.currentTimeMillis();
		System.out.println("多线程平均耗时:"+((s1-s0)/10)+"毫秒");
		
		
		
		long s2 = System.currentTimeMillis();
		for(int i=0;i<10;i++){
			long total = 0;
			for(long j=1;j<=4294967295L;j++){
				total+=j;
			}
			System.out.println(total);
		}
		long s3 = System.currentTimeMillis();
		System.out.println("单线程平均耗时："+((s3-s2)/10)+"毫秒");
	}
	
	
	/**
	 * 模拟任务子线程发生异常时的情形
	 */
	@Test
	public void testException(){
		
		TaskAction<Integer> ta = new TaskAction<Integer>();
		
		Task<Integer> task0 = new Task<Integer>() {
			@Override
			public Integer excute() {
				// by zero
				int i = 1/0;
				return 1;
			}
		};
		
		Task<Integer> task1 = new Task<Integer>() {
			@Override
			public Integer excute() {
				String a = null;
				//nullPoint
				a.substring(0);
				return 2;
			}
		};
		
		Task<Integer> task2 = new Task<Integer>() {
			@Override
			public Integer excute() {
				return 3;
			}
		};
		
		Integer total = ta.addTask(task0).addTask(task1).addTask(task2).doTasksForSingleResult();
		System.out.println("计算结果为："+total);
	}
	
	/**
	 * 小插曲
	 * @param args
	 */
	public static void main(String[] args) {
		new JunitTest().testException();
	}
	
	
	/**
	 * 多线程下载
	 */
	@Test
	public void testDownloadResource(){
		Media media = new Media();
		media.downloadByMultiThread("http://b.zol-img.com.cn/desk/bizhi/image/7/1024x768/1449566818591.jpg", "D:/2.jpg");
		System.out.println("资源大小："+media.getResourceLength()+" 已读取："+media.getReadyLen());
	}
	
	
	/**
	 * 测试1万个线程并发运算（1到一亿的和）
	 */
	@Test
	public void testConcurrent(){
		
		Long s0 = System.currentTimeMillis();
		
		//计算1到1亿的累计和,构造函数传false代表每个任务都单独启一个线程去计算
		TaskAction<Long> ta = new TaskAction<Long>(false);
		
		for(int i=1;i<=10000;i++){
			final int start = (i-1)*10000+1;
			final int end = i*10000;;
			Task<Long> task = new Task<Long>() {
				@Override
				public Long excute() throws Exception {
					Long st = 0L;
					for(int j=start;j<=end;j++){
						st += j;
					}
					return st;
				}
			};
			ta.add(task);
		}
		
		long result0 = ta.doTasksForSingleResult();
		Long s1 = System.currentTimeMillis();
		System.out.println(result0+":1万个线程并发计算结果,耗时："+(s1-s0));
		
		
		Long s2 = System.currentTimeMillis();
		long result1 = 0L;
		for(int i=1;i<=100000000;i++){
			result1+=i;
		}
		Long s3 = System.currentTimeMillis();
		System.out.println(result1+":单线程计算结果,耗时："+(s3-s2));
		
		Assert.assertTrue(result0==result1);
	}
	
	/**
	 * 多线程查数据库，略
	 */
	@Test
	public void testConcurrentQuery(){
		
	}
	
	
}
