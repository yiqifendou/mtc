package com.haiziwang.platform.mtc.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.haiziwang.platform.mtc.core.Task;
import com.haiziwang.platform.mtc.core.TaskAction;

public class Media {
	//已下载的字节数
	private Long readyLen = 0L;
	//文件总字节数
	private long resourceLength = 0;
	
	/**
	 * 多线程下载文件
	 */
	public void downloadByMultiThread(String resourceUrl,String targetFile){
		RandomAccessFile raf = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(resourceUrl).openConnection();
			resourceLength = conn.getContentLengthLong();
			raf = new RandomAccessFile(targetFile,"rw");
			raf.setLength(resourceLength);
			TaskAction<Integer> ta = new TaskAction<Integer>();
			//启cpu核数个task
			int cpuCount = Runtime.getRuntime().availableProcessors();
			long skipUnit = resourceLength/cpuCount;
			for(int i=0;i<cpuCount;i++){
				final long startIndex = skipUnit*i;
				final long endIndex = (i==(cpuCount-1))?resourceLength:(i+1)*skipUnit-1;
				Task<Integer> task = new Task<Integer>() {
					@Override
					public Integer excute() throws IOException {
						readAndWrite(resourceUrl, targetFile, startIndex, endIndex);
						return 1;
					}
				};
				ta.addTask(task);
			}
			ta.doTasks();
			System.out.println("多线程下载完毕");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(null!=raf){
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
					//ignore
				}
			}
		}
	}
	
	private void readAndWrite(String resourceUrl,String targetFile,long startIndex,long endIndex) throws IOException{
		URL url = new URL(resourceUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		//设置头部的参数，表示请求服务器资源的某一部分
		conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
		//设置了上面的头信息后，响应码为206代表请求资源成功，而不再是200
		int code = conn.getResponseCode();
		if(code == 206){
			InputStream is = conn.getInputStream();
			int hasRead = 0;
			byte[] buf = new byte[1024];
			//这里要注意新创建一个RandomAccessFile对象，而不能重复使用download方法中创建的
			RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
			//将写文件的指针指向下载的起始点
			raf.seek(startIndex);
			while((hasRead = is.read(buf)) > 0) {
				raf.write(buf, 0, hasRead);
				addReadyLen(hasRead);
			}
			is.close();
			raf.close();
			System.out.println("线程"+Thread.currentThread().getId()+"已经下载完毕");
		}
	}
	
	private long addReadyLen(Integer len){
		synchronized (readyLen) {
			readyLen+=len;
		}
		return readyLen;
	}

	public Long getReadyLen() {
		return readyLen;
	}

	
	public long getResourceLength() {
		return resourceLength;
	}
	
}
