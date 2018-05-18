/**
 * @author xinwuhen
 */
package com.chinaepay.wx.dao;

/**
 * @author xinwuhen
 *
 */
public class TransOrderRefundOrderDAO extends TblDAO {
	private String out_trade_no = "";
	private String out_refund_no = "";
	
	/**
	 * @return the out_trade_no
	 */
	public String getOut_trade_no() {
		return out_trade_no;
	}
	/**
	 * @param out_trade_no the out_trade_no to set
	 */
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	/**
	 * @return the out_refund_no
	 */
	public String getOut_refund_no() {
		return out_refund_no;
	}
	/**
	 * @param out_refund_no the out_refund_no to set
	 */
	public void setOut_refund_no(String out_refund_no) {
		this.out_refund_no = out_refund_no;
	}
}
