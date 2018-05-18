/**
 * @author xinwuhen
 */
package com.chinaepay.wx.control;

import java.util.HashMap;

/**
 * @author xinwuhen
 *
 */
public abstract class InquiryController extends CommunicateController {
	/**
	 * 查询交易单的入口。
	 * @param hmInquiryOrderCont
	 * @return
	 */
//	public abstract HashMap<String, String> startInquiryOrder(HashMap<String, String> hmInquiryOrderCont);
	public abstract String startInquiryOrder(HashMap<String, String> hmInquiryOrderCont);
	
}
