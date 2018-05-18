/**
 * @author xinwuhen
 */
package com.chinaepay.wx.dao;

/**
 * @author xinwuhen
 *
 */
public class RefundOrderDAO extends TblDAO {
	private String out_refund_no = "";
	private String out_trade_no = "";
	private String transaction_id = "";
	private String refund_id = "";
	private int total_fee = 0;
	private int refund_fee = 0;
	private String refund_fee_type = "";
	private String refund_status = "";
	private String refund_channel = "";
	private String refund_success_time = "";
	private String refund_recv_accout = "";
	private int cash_fee = 0;
	private String cash_fee_type = "";
	private int cash_refund_fee = 0;
	private String cash_refund_fee_type = "";
	private String rate = "";
	private String trans_time = "";
	
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
	 * @return the transaction_id
	 */
	public String getTransaction_id() {
		return transaction_id;
	}
	/**
	 * @param transaction_id the transaction_id to set
	 */
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	/**
	 * @return the refund_id
	 */
	public String getRefund_id() {
		return refund_id;
	}
	/**
	 * @param refund_id the refund_id to set
	 */
	public void setRefund_id(String refund_id) {
		this.refund_id = refund_id;
	}
	/**
	 * @return the total_fee
	 */
	public int getTotal_fee() {
		return total_fee;
	}
	/**
	 * @param total_fee the total_fee to set
	 */
	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}
	/**
	 * @return the refund_fee
	 */
	public int getRefund_fee() {
		return refund_fee;
	}
	/**
	 * @param refund_fee the refund_fee to set
	 */
	public void setRefund_fee(int refund_fee) {
		this.refund_fee = refund_fee;
	}
	/**
	 * @return the refund_fee_type
	 */
	public String getRefund_fee_type() {
		return refund_fee_type;
	}
	/**
	 * @param refund_fee_type the refund_fee_type to set
	 */
	public void setRefund_fee_type(String refund_fee_type) {
		this.refund_fee_type = refund_fee_type;
	}
	/**
	 * @return the refund_status
	 */
	public String getRefund_status() {
		return refund_status;
	}
	/**
	 * @param refund_status the refund_status to set
	 */
	public void setRefund_status(String refund_status) {
		this.refund_status = refund_status;
	}
	/**
	 * @return the refund_channel
	 */
	public String getRefund_channel() {
		return refund_channel;
	}
	/**
	 * @param refund_channel the refund_channel to set
	 */
	public void setRefund_channel(String refund_channel) {
		this.refund_channel = refund_channel;
	}
	/**
	 * @return the refund_success_time
	 */
	public String getRefund_success_time() {
		return refund_success_time;
	}
	/**
	 * @param refund_success_time the refund_success_time to set
	 */
	public void setRefund_success_time(String refund_success_time) {
		this.refund_success_time = refund_success_time;
	}
	/**
	 * @return the refund_recv_accout
	 */
	public String getRefund_recv_accout() {
		return refund_recv_accout;
	}
	/**
	 * @param refund_recv_accout the refund_recv_accout to set
	 */
	public void setRefund_recv_accout(String refund_recv_accout) {
		this.refund_recv_accout = refund_recv_accout;
	}
	/**
	 * @return the cash_fee
	 */
	public int getCash_fee() {
		return cash_fee;
	}
	/**
	 * @param cash_fee the cash_fee to set
	 */
	public void setCash_fee(int cash_fee) {
		this.cash_fee = cash_fee;
	}
	/**
	 * @return the cash_fee_type
	 */
	public String getCash_fee_type() {
		return cash_fee_type;
	}
	/**
	 * @param cash_fee_type the cash_fee_type to set
	 */
	public void setCash_fee_type(String cash_fee_type) {
		this.cash_fee_type = cash_fee_type;
	}
	/**
	 * @return the cash_refund_fee
	 */
	public int getCash_refund_fee() {
		return cash_refund_fee;
	}
	/**
	 * @param cash_refund_fee the cash_refund_fee to set
	 */
	public void setCash_refund_fee(int cash_refund_fee) {
		this.cash_refund_fee = cash_refund_fee;
	}
	/**
	 * @return the cash_refund_fee_type
	 */
	public String getCash_refund_fee_type() {
		return cash_refund_fee_type;
	}
	/**
	 * @param cash_refund_fee_type the cash_refund_fee_type to set
	 */
	public void setCash_refund_fee_type(String cash_refund_fee_type) {
		this.cash_refund_fee_type = cash_refund_fee_type;
	}
	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}
	/**
	 * @param rate the rate to set
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}
	/**
	 * @return the trans_time
	 */
	public String getTrans_time() {
		return trans_time;
	}
	/**
	 * @param trans_time the trans_time to set
	 */
	public void setTrans_time(String trans_time) {
		this.trans_time = trans_time;
	}
}
