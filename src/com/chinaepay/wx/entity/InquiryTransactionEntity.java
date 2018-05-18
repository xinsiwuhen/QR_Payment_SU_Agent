/**
 * @author xinwuhen
 */
package com.chinaepay.wx.entity;

import java.util.HashMap;

/**
 * @author xinwuhen
 * 查询类订单业务内容。
 */
public class InquiryTransactionEntity extends InquiryEntity {
	private HashMap<String, String> hmInquiryTransCont = null;
	
	// 交易单对应的数据库表名称
	public static final String TBL_TRANS_ORDER = "TBL_TRANS_ORDER";
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> getHashInstance() {
		if (hmInquiryTransCont == null) {
			hmInquiryTransCont = new HashMap<String, String>();
		}
		return hmInquiryTransCont;
	}
}
