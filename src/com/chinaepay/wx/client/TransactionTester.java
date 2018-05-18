/**
 * @author xinwuhen
 */
package com.chinaepay.wx.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.CommonTool.SocketConnectionManager;
import com.chinaepay.wx.control.InquiryRefundController;
import com.chinaepay.wx.entity.InquiryRefundEntity;
import com.chinaepay.wx.entity.InquiryTransactionEntity;
import com.chinaepay.wx.entity.PaymentTransactionEntity;
import com.chinaepay.wx.entity.RefundTransactionEntity;
import com.chinaepay.wx.entity.ReverseTransactionEntity;

/**
 * @author xinwuhen
 *	本类主要用于测试。
 */
public class TransactionTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		System.out.println(TransactionTester.class);
//		System.out.println(new String("Test").getClass());
		
		TransactionTester transTester = new TransactionTester();
		String strBizReq = null;
		String strBizType = null;
		
		/** 测试支付订单 **/
//		strBizType = CommonInfo.PAYMENT_TRANSACTION_BIZ;
//		strBizReq = transTester.getPamentTransRequest();
//		exeSocketTest(strBizType + ":" + strBizReq);
		
		/** 测试查询订单 **/
//		strBizType = CommonInfo.INQUIRY_TRANSACTION_BIZ;
//		strBizReq = transTester.getInquiryTransRequest();
//		exeSocketTest(strBizType + ":" + strBizReq);
		
		/** 测试撤销订单 **/
//		strBizType = CommonInfo.REVERSE_TRANSACTION_BIZ;
//		strBizReq = transTester.getReverseTransRequest();
//		exeSocketTest(strBizType + ":" + strBizReq);
		
		/** 测试退款订单 **/
//		strBizType = CommonInfo.REFUND_TRANSACTION_BIZ;
//		strBizReq = transTester.getRefundTransRequest();
//		exeSocketTest(strBizType + ":" + strBizReq);
		
		
		/** 测试查询退款单 **/
		strBizType = CommonInfo.INQUIRY_REFUND_BIZ;
		strBizReq = transTester.getInquiryRefundRequest();
		exeSocketTest(strBizType + ":" + strBizReq);
		
	}
	
	/**
	 * 通过Socket进行业务通信。
	 * @param strData
	 */
	private static void exeSocketTest(String strData) {
		SocketConnectionManager socketConnMngr = CommonTool.SocketConnectionManager.getInstance();
//		socketConnMngr.openSocket("47.93.125.18", 10086);
		socketConnMngr.openSocket("127.0.0.1", 10086);
		socketConnMngr.writeData(strData);
		String strResp = socketConnMngr.readData();
		System.out.println("strResp = " + strResp);
		socketConnMngr.closeSocket();
	}
	
	/**
	 * 支付交易对应的请求报文，格式：appid=43453&mch_id=dsw342&sub_mch_id=983477232&nonce_str=aiadjsis8732487jsd8l
	 * @return
	 */
	private String getPamentTransRequest() {
		
		String strAuthCode = "135029600960750624"; // 二维码中的用户授权码
		StringBuffer sb = new StringBuffer();
		sb.append(PaymentTransactionEntity.AGENT_ID + "=" + "1r10s84408mdj0tgp6iov2c0k54jbps9");
		sb.append("&" + PaymentTransactionEntity.SUB_MCH_ID + "=" + "12152566");
		sb.append("&" + PaymentTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
		sb.append("&" + PaymentTransactionEntity.BODY + "=" + "Ipad mini  16G  白色"); // Ipad mini  16G  白色
		sb.append("&" + PaymentTransactionEntity.OUT_TRADE_NO + "=" + CommonTool.getOutTradeNo(new Date(), 18));
		sb.append("&" + PaymentTransactionEntity.TOTAL_FEE + "=" + "1");
		sb.append("&" + PaymentTransactionEntity.FEE_TYPE + "=" + "USD");
		try {
			sb.append("&" + PaymentTransactionEntity.SPBILL_CREATE_IP + "=" + CommonTool.getSpbill_Create_Ip());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		sb.append("&" + PaymentTransactionEntity.AUTH_CODE + "=" + strAuthCode);
//		sb.append("&" + PaymentTransactionEntity.APP_KEY + "=" + "024edfffae32c829b012c98a61686f3b");
		
		return sb.toString();
	}
	
	/**
	 * 撤销单请求报文。
	 * @return
	 */
	private String getReverseTransRequest() {
		StringBuffer sb = new StringBuffer();
		sb.append(ReverseTransactionEntity.AGENT_ID + "=" + "1r10s84408mdj0tgp6iov2c0k54jbps9");
		sb.append("&" + ReverseTransactionEntity.SUB_MCH_ID + "=" + "12152566");
		sb.append("&" + ReverseTransactionEntity.OUT_TRADE_NO + "=" + "20180317162341003734102708751406"); // 测试时修改此字段
		sb.append("&" + ReverseTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
//		sb.append("&" + ReverseTransactionEntity.APP_KEY + "=" + "024edfffae32c829b012c98a61686f3b");
		
		return sb.toString();
	}
	
	/**
	 * 退款请求报文。
	 * @return
	 */
	private String getRefundTransRequest() {
		StringBuffer sb = new StringBuffer();
		sb.append(RefundTransactionEntity.AGENT_ID + "=" + "1r10s84408mdj0tgp6iov2c0k54jbps9");
		sb.append("&" + RefundTransactionEntity.SUB_MCH_ID + "=" + "12152566");
		sb.append("&" + RefundTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
		sb.append("&" + RefundTransactionEntity.OUT_TRADE_NO + "=" + "20180317173710924965776167374654"); // 测试时修改此字段
		sb.append("&" + RefundTransactionEntity.OUT_REFUND_NO + "=" + CommonTool.getOutRefundNo(new Date(), 18));	// 同一退款单号时记得修改此字段为固定值
		sb.append("&" + RefundTransactionEntity.TOTAL_FEE + "=" + "1");
		sb.append("&" + RefundTransactionEntity.REFUND_FEE + "=" + "1");
//		sb.append("&" + RefundTransactionEntity.APP_KEY + "=" + "024edfffae32c829b012c98a61686f3b");
		
		return sb.toString();
	}
	
	/**
	 * 生成查询操作所需的参数列表(HashMap类型).
	 * @param hmTransactionOrderCont
	 * @return
	 */
	private String getInquiryTransRequest() {
		StringBuffer sb = new StringBuffer();
		sb.append(InquiryTransactionEntity.AGENT_ID + "=" + "1r10s84408mdj0tgp6iov2c0k54jbps9");
		sb.append("&" + InquiryTransactionEntity.SUB_MCH_ID + "=" + "12152566");
		sb.append("&" + InquiryTransactionEntity.OUT_TRADE_NO + "=" + "20180317173710924965776167374654");	// 测试时修改此参数
		sb.append("&" + InquiryTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
//		sb.append("&" + InquiryTransactionEntity.APP_KEY + "=" + "024edfffae32c829b012c98a61686f3b");
		
		return sb.toString();
	}
	
	/**
	 * 查询退款单对应的请求报文。
	 * @return
	 */
	private String getInquiryRefundRequest() {
		StringBuffer sb = new StringBuffer();
		sb.append(InquiryRefundEntity.AGENT_ID + "=" + "1r10s84408mdj0tgp6iov2c0k54jbps9");
		sb.append("&" + InquiryRefundEntity.SUB_MCH_ID + "=" + "12152566");
		sb.append("&" + InquiryRefundEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
		sb.append("&" + InquiryRefundEntity.OUT_TRADE_NO + "=" + "20180317173710924965776167374654");	// 测试时需修改此参数
//		sb.append("&" + InquiryRefundEntity.APP_KEY + "=" + "024edfffae32c829b012c98a61686f3b");
		
		return sb.toString();
	}
}
