/**
 * @author xinwuhen
 */
package com.chinaepay.wx.intf;

import java.util.HashMap;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author xinwuhen
 *
 */
public interface CommunicateIntf {
	/**
	 * 校验交易参数。
	 * @param transOrderInfo
	 * @return
	 */
	public boolean blnValdOrderArgs(HashMap<String, String> hmOrderCont);
	
	/**
	 * 向微信后台发送业务请求报文并获取报文内容。
	 * @param transOrderInfo
	 * @return String 返回的报文内容。
	 */
	public String sendReqAndGetResp(String strURL, HashMap<String, String> hmOrderCont, CloseableHttpClient httpclient);
	
}
