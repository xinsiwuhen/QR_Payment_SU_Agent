package com.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.mysql.jdbc.Connection;

public class TestMain {

	public static void main(String[] args) {
		
		Date date = new Date();//获取当前时间    
//		Calendar calendar = Calendar.getInstance();    
//		calendar.setTime(date);    
//		calendar.add(Calendar.YEAR, -1);//当前时间减去一年，即一年前的时间    
//		calendar.add(Calendar.MONTH, -1);//当前时间前去一个月，即一个月前的时间    
//		Date newDate = calendar.getTime();//获取一年前的时间，或者一个月前的时间
		String strFtDate = CommonTool.getPreOrSuffFormatDate(date, "YYYY-MM-dd HH:mm", -1, -1, 0);
		System.out.println(strFtDate);
		
		/*ServerSocket serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(10086);
			serverSocket.setSoTimeout(10000);
			
			while (true) {
				System.out.println("$$$[Start]");
				socket = serverSocket.accept();
				System.out.println("Start one processing thread.");
				new Thread(new MySocketProcesserForServer(socket, 0)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		/*
		long lngStartTime = System.currentTimeMillis();
		System.out.println("lngStartTime = " + lngStartTime);
		
		for (int i = 0; i < 1000; i++) {
			new Thread(new GetConnFromMysqlPool(i+1)).start();
		}
		long lngEndTime = System.currentTimeMillis();
		System.out.println("lngEndTime = " + lngEndTime);
		System.out.println("lngEndTime - lngStartTime:" + (lngEndTime - lngStartTime) + " ms");
		
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		
		int intCurrSize = 20;
		float MYSQL_CONN_INCREMENTAL_RATE = 0.20f;
		System.out.println((int) (intCurrSize * (1 + MYSQL_CONN_INCREMENTAL_RATE)));
		
		
		System.out.println("*****************************");
		sortStr();
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("a");
		sb.append("b");
		System.out.println(sb);
		sb.setLength(0);
		System.out.println("sb = " + sb);
		System.out.println("ddd:" + sb.toString());
		
		*/
		/**
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		int i = 2; 
		switch(i) {
			case 1: System.out.println("nx");
			default: System.out.println("default");
			case 2: System.out.println("x87xc87");
			case 3: System.out.println("ffff88");
		}
		
		
		Father chld = new Father();
		chld.doEvent();
		chld = new Child();
		chld.doEvent();
		
		System.out.println(new TestMain().getRandomString(32));
		
		System.out.println(CommonTool.getOutTradeNo(new Date(), 18));
		
		try {
			System.out.println(CommonTool.getSpbill_Create_Ip());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		**/
		
	}
	
	private static void sortStr() {
		String[] strs = {"mnxow", "mmabc", "adm", "dmi", "acm" };
		Arrays.sort(strs);
		for (String str : strs) {
			System.out.println(str);
		}
	}
	
	
	private String getRandomString(int intLength) {
		char[] ch = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
								'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
								'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
								'u', 'v', 'w', 'x', 'y', 'z'};
		char[] chNew = new char[intLength];
		int iChLengh = ch.length;
		for (int i = 0; i < chNew.length; i++) {
			int index = (int) (iChLengh *  Math.random());
			chNew[i] = ch[index];
		}
		
		return String.valueOf(chNew).toUpperCase();
	}
	
	
	private static class GetConnFromMysqlPool implements Runnable {
		private int i = 0;
		public GetConnFromMysqlPool(int i) {
			this.i = i;
		}
		
		
		@Override
		public void run() {
			MysqlConnectionPool mcp = MysqlConnectionPool.getInstance();
			System.out.println("mcp.hashcode = " + mcp.hashCode() + ", " + this.i);
//			Connection conn = mcp.getConnection(false);
			
//			new ReleaseConnForMysqlPool(conn).run();
		}
	}
	
	
	private static class ReleaseConnForMysqlPool implements Runnable {
		private Connection conn = null;
		public ReleaseConnForMysqlPool(Connection conn) {
			this.conn = conn;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MysqlConnectionPool mcp = MysqlConnectionPool.getInstance();
			mcp.releaseConnection(conn);
		}
	}
	
	
	private static class MySocketProcesserForServer implements Runnable {
		
		private Socket socket = null;
		private int intCount = 0;
		private InputStream is = null;
		private BufferedReader br = null;
		
		public MySocketProcesserForServer(Socket socket, int intCount) {
			this.socket = socket;
			this.intCount = intCount;
		}

		@Override
		public void run() {
			if (socket != null) {
				System.out.println("socket's hashcode [Server] = " + socket.hashCode());
				System.out.println("socket's address [Server] = " + socket.getInetAddress() + ", Remote Port = " + socket.getPort());
				System.out.println("socket's address [Server] = " + socket.getInetAddress() + ", Local Port = " + socket.getLocalPort());

				try {
					socket.setSoTimeout(5000);
					// Get inputstream data from client.
					is = socket.getInputStream();
					System.out.println("is = " + is);
					br = new BufferedReader(new InputStreamReader(is));
					System.out.println("br = " + br);
//					String strCont = null;
//					while ((strCont = br.readLine()) != null) {
//						System.out.println("strCont = " + strCont);
//					}
//					System.out.println("After while, strCont = " + strCont);
//					socket.shutdownInput();
					
					
					
					int iByteData = -1;
					StringBuffer sb = new StringBuffer();
					while ((iByteData = br.read()) != -1) {
						sb.append((char) iByteData);
					}
					System.out.println("sb = " + sb);
					
					// Write out data to client.
					OutputStream os = socket.getOutputStream();
					System.out.println("os = " + os);
					PrintWriter pw = new PrintWriter(os);
					System.out.println("pw = " + pw);
					
					pw.write("I'm Server");
					pw.flush();
					socket.shutdownOutput();
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				/*
				finally {
					if (socket != null) {
						System.out.println("socket is null");
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					if (is != null) {
						System.out.println("is is null");
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					if (br != null) {
						System.out.println("br is null");
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				*/
			}
		}
	}
}
