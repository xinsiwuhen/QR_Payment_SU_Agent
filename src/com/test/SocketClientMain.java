/**
 * @author xinwuhen
 */
package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * @author xinwuhen
 *
 */
public class SocketClientMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Socket socket = null;
		OutputStream os = null;
		PrintWriter pw = null;
		for (int i = 0; i < 1; i++) {
			try {
				socket = new Socket("127.0.0.1", 10086);
				socket.setSoTimeout(3000);
				
				System.out.println("socket's hashcode [Client] = " + socket.hashCode());
				System.out.println("socket's address [Client] = " + socket.getInetAddress() + ", Local Port = " + socket.getLocalPort());
				os = socket.getOutputStream();
				pw = new PrintWriter(os);
				pw.write("0012a");
				pw.flush();
				socket.shutdownOutput();
				
				System.out.println("*****");
				InputStream is = socket.getInputStream();
				System.out.println("is = " + is);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				System.out.println("br = " + br);
				int iByteData = -1;
				StringBuffer sb = new StringBuffer();
				while ((iByteData = br.read()) != -1) {
					sb.append((char) iByteData);
				}
				System.out.println("sb = " + sb);
				
//				String strSvr = null;
//				while ((strSvr = br.readLine()) != null ) {
//					System.out.println("strSvr = " + strSvr);
//				}
				
//				is.close();
//				isr.close();
//				br.close();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (pw != null) {
					pw.close();
				}
				
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
