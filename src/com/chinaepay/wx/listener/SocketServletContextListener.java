/**
 * @author xinwuhen
 */
package com.chinaepay.wx.listener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.control.InquiryRefundController;
import com.chinaepay.wx.control.InquiryTransactionController;
import com.chinaepay.wx.control.PaymentTransactionController;
import com.chinaepay.wx.control.RefundTransactionController;
import com.chinaepay.wx.control.ReverseTransactoinController;
import com.chinaepay.wx.entity.CommunicateEntity;
import com.chinaepay.wx.entity.PaymentTransactionEntity;

/**
 * @author xinwuhen
 *
 */
public class SocketServletContextListener implements ServletContextListener {
	
	
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// 启动处理Socket请求的守护线程
		ServerSocketDeamonThread serverSocktDeamonThread = new ServerSocketDeamonThread();
		new Thread(serverSocktDeamonThread).start();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}
	
	
	/**
	 * 接收Socket客户端连接的守护线程。
	 * @author xinwuhen
	 *
	 */
	private class ServerSocketDeamonThread implements Runnable {
		private ServerSocket srvSocket = null;
		private Socket socket = null;
		
		@Override
		public void run() {
			try {
				srvSocket = new ServerSocket(CommonInfo.SERVER_SOCKET_PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (srvSocket != null) {
				while(true) {
					try {
						socket = srvSocket.accept();
						ServerSocketProcessThread sspt = new ServerSocketProcessThread(socket);
						new Thread(sspt).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					try {
						Thread.currentThread().sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	// while
			}
		}
	}
	
	/**
	 * 处理Socket客户端内容的独立连接线程，针对不同的客户端连接会生成不出的该线程类。
	 * @author xinwuhen
	 *
	 */
	private class ServerSocketProcessThread implements Runnable {
		private Socket socket = null;
		private InputStream in = null;
		private InputStreamReader isr = null;
		private BufferedReader br = null;
		private OutputStream os = null;
		private OutputStreamWriter osw =null;
		private BufferedWriter bw = null;
		
		public ServerSocketProcessThread(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			if (socket == null) {
				return;
			}
			
			try {
				// 设置服务端读取数据的超时时间
				socket.setSoTimeout(CommonInfo.SERVER_SOCKET_TIME_OUT);
				
				in = socket.getInputStream();
				isr = new InputStreamReader(in, "UTF-8");
				br = new BufferedReader(isr);
				int iByteData = -1;
				StringBuffer sb = new StringBuffer();
				while ((iByteData = br.read()) != -1) {
					sb.append((char) iByteData);
				}
//				socket.shutdownInput();		// 关闭Socket的InputStream.
				
				String strSocketReqCont = sb.toString();
				if (!"".equals(strSocketReqCont)) {
					System.out.println("strSocketReqCont = " + strSocketReqCont);
					
					// 处理从Socket客户端获取的内容信息
					String strProcCntrlFlag = getProcCntrlFlag(strSocketReqCont);
					String strWXReqCont = getWXRequestContent(strSocketReqCont) + getHarvestTransInfo();
					
					System.out.println("****strWXReqCont = " + strWXReqCont);
					
					if (strProcCntrlFlag != null && !"".equals(strProcCntrlFlag) && strWXReqCont != null && !"".equals(strWXReqCont)) {
						HashMap<String, String> hmWXReqCont = CommonTool.formatStrToMap(strWXReqCont);
//						HashMap<String, String> hmReturnResult = null;
						String strResponRst = null;
						switch(strProcCntrlFlag) {
							// 支付交易
							case CommonInfo.PAYMENT_TRANSACTION_BIZ:
								PaymentTransactionController payTransCntrl = PaymentTransactionController.getInstance();
//								hmReturnResult = payTransCntrl.startTransactionOrder(hmWXReqCont);
								strResponRst = payTransCntrl.startTransactionOrder(hmWXReqCont);
								break;
								
							// 撤销交易
							case CommonInfo.REVERSE_TRANSACTION_BIZ:
								ReverseTransactoinController reverseTransCntrl = ReverseTransactoinController.getInstance();
//								hmReturnResult = reverseTransCntrl.startTransactionOrder(hmWXReqCont);
								strResponRst = reverseTransCntrl.startTransactionOrder(hmWXReqCont);
								break;
								
							// 申请退款
							case CommonInfo.REFUND_TRANSACTION_BIZ:
								RefundTransactionController refundTransCntrl = RefundTransactionController.getInstance();
//								hmReturnResult = refundTransCntrl.startTransactionOrder(hmWXReqCont);
								strResponRst = refundTransCntrl.startTransactionOrder(hmWXReqCont);
								break;
								
							// 查询交易
							case CommonInfo.INQUIRY_TRANSACTION_BIZ:
								InquiryTransactionController inquiryTransCntrl = InquiryTransactionController.getInstance();
//								hmReturnResult = inquiryTransCntrl.startInquiryOrder(hmWXReqCont);
								strResponRst = inquiryTransCntrl.startInquiryOrder(hmWXReqCont);
								break;
								
							// 查询退款
							case CommonInfo.INQUIRY_REFUND_BIZ:
								InquiryRefundController inquiryRefundCntrl = InquiryRefundController.getInstance();
//								hmReturnResult = inquiryRefundCntrl.startInquiryOrder(hmWXReqCont);
								strResponRst = inquiryRefundCntrl.startInquiryOrder(hmWXReqCont);
								break;
						}
						
						
//						if (hmReturnResult == null || hmReturnResult.size() == 0) {
//							return;
//						}
						
//						// 格式化Socket应答报文
//						String strSocketRespCont = getSocketRespContent(hmReturnResult);
						
						// 向客户端返回内容处理结果
//						if (strSocketRespCont != null) {
						if (strResponRst != null) {
							System.out.println("strResponRst = " + strResponRst);
							os = socket.getOutputStream();
							osw = new OutputStreamWriter(os, "UTF-8");
							bw = new BufferedWriter(osw);
//							bw.write(strSocketRespCont);
							bw.write(strResponRst);
							bw.flush();
							socket.shutdownOutput();	// 关闭Socket的OutputStream，防止Socket的输入流读取时阻塞.
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			/*
			finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (isr != null) {
					try {
						isr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (osw != null) {
					try {
						osw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			*/
		}
	}
	
	/**
	 * 获取Socket请求报文内的服务端处理类所对应的标识。
	 * @param strTotalCotent
	 * @return
	 */
	private String getProcCntrlFlag(String strTotalCotent) {
		if (strTotalCotent == null || strTotalCotent.equals("")) {
			return null;
		}
		
		if (!strTotalCotent.contains(":")) {
			return null;
		}
		
		return strTotalCotent.split(":")[0];
	}
	
	/**
	 * 获取Socket请求报文内的业务内容。
	 * @param strTotalCotent
	 * @return
	 */
	private String getWXRequestContent(String strTotalCotent) {
		if (strTotalCotent == null || strTotalCotent.equals("")) {
			return null;
		}
		
		if (!strTotalCotent.contains(":")) {
			return null;
		}
		
		return strTotalCotent.split(":")[1];
	}
	
	/**
	 * 获取机构模式下易付通美国(Harvest)交易相关的基本信息。
	 * @return
	 */
	private String getHarvestTransInfo() {
		String strHarTransInfo = "";
		strHarTransInfo = strHarTransInfo.concat("&" + PaymentTransactionEntity.APPID + "=" + CommonInfo.HARVEST_APP_ID);
		strHarTransInfo = strHarTransInfo.concat("&" + PaymentTransactionEntity.MCH_ID + "=" + CommonInfo.HARVEST_MCH_ID);
		strHarTransInfo = strHarTransInfo.concat("&" + PaymentTransactionEntity.APP_KEY + "=" + CommonInfo.HARVEST_KEY);
		return strHarTransInfo;
	}
	
	/**
	 * 将Socket服务端处理后生成的Map类型返回内容格式化为字符串类型。
	 * 字符格式:BUSINESS_PROC_RESULT=SUCCESS&out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 * 其中，BUSINESS_PROC_RESULT来自于类CommunicateEntity.
	 * @param hmReturnResult
	 * @return
	 */
	private String getSocketRespContent(HashMap<String, String> hmReturnResult) {
		if (hmReturnResult == null || hmReturnResult.size() == 0) {
			return "";
		}
		
		String strSysCommRst = hmReturnResult.get(CommunicateEntity.SYSTEM_COMM_RESULT_KEY);
		String strProcRst = hmReturnResult.get(CommunicateEntity.BUSINESS_PROC_RESULT_KEY);
		String strProcCont = hmReturnResult.get(CommunicateEntity.BUSINESS_RESPONSE_RESULT);
		if (strSysCommRst == null ||  strProcRst == null || strProcCont == null) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(CommunicateEntity.SYSTEM_COMM_RESULT_KEY + "=" + strSysCommRst);
		sb.append("&" + CommunicateEntity.BUSINESS_PROC_RESULT_KEY + "=" + strProcRst);
		sb.append(":" + strProcCont);
		
		return sb.toString();
	}
}
