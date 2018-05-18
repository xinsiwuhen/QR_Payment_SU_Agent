/**
 * @author xinwuhen
 */
package com.chinaepay.wx.entity;

import java.util.HashMap;

/**
 * @author xinwuhen
 *	本类用于存储支付类订单的交易数据。
 */
public class PaymentTransactionEntity extends TransactionEntity {
	private HashMap<String, String> hmPaymentTransCont = null;
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> getHashInstance() {
		if (hmPaymentTransCont == null) {
			hmPaymentTransCont = new HashMap<String, String>();
		}
		return hmPaymentTransCont;
	}
}
