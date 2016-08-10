package com.haiziwang.platform.mtc.exception;

public class TaskException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TaskException(){
		super("进行多线程计算时,任务子线程发生异常...");
	}
	
}
