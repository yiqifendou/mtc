package com.haiziwang.platform.mtc.exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * 因为不知道是否启动log4j或logback等日志工具，所以用此方式打印日志
 * @author yiqifendou
 *
 */
public class ExceptionUtil {
	public static void printStackTrace(Exception e){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		try {
			e.printStackTrace(ps);
			String stackTraceStr = new String(bos.toByteArray(), "UTF-8");
			System.err.println(stackTraceStr);
		}catch (UnsupportedEncodingException ex) {
			//ignore
		}finally{
			try {
				ps.close();
				bos.close();
			} catch (IOException exx) {
				//ignore
			}
		}
	}
}
