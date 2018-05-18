/**
 * @author xinwuhen
 */
package com.chinaepay.wx.dao;

/**
 * @author xinwuhen
 *
 */
public class TransOrderDAO extends TblDAO {
	private String out_trade_no = "";
	private String transaction_id = "";
	private String trade_state = "";
	private String body = "";
	private int total_fee = 0;
	private String fee_type = "";
	private String openid = "";
	private String trade_type = "";
	private String bank_type = "";
	private String cash_fee_type = "";
	private int cash_fee = 0;
	private String trans_time = "";
	private String time_end = "";
	private String rate = "";
	
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
	 * @return the trade_state
	 */
	public String getTrade_state() {
		return trade_state;
	}
	/**
	 * @param trade_state the trade_state to set
	 */
	public void setTrade_state(String trade_state) {
		this.trade_state = trade_state;
	}
	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}
	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
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
	 * @return the fee_type
	 */
	public String getFee_type() {
		return fee_type;
	}
	/**
	 * @param fee_type the fee_type to set
	 */
	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}
	/**
	 * @return the openid
	 */
	public String getOpenid() {
		return openid;
	}
	/**
	 * @param openid the openid to set
	 */
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	/**
	 * @return the trade_type
	 */
	public String getTrade_type() {
		return trade_type;
	}
	/**
	 * @param trade_type the trade_type to set
	 */
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	/**
	 * @return the bank_type
	 */
	public String getBank_type() {
		return bank_type;
	}
	/**
	 * @param bank_type the bank_type to set
	 */
	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
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
	 * @return the trans_time
	 */
	public String getTrans_time() {
		return trans_time;
	}
	/**
	 * @param time_end the time_end to set
	 */
	public void setTrans_time(String trans_time) {
		this.trans_time = trans_time;
	}
	
	/**
	 * @return the time_end
	 */
	public String getTime_end() {
		return time_end;
	}
	/**
	 * @param time_end the time_end to set
	 */
	public void setTime_end(String time_end) {
		this.time_end = time_end;
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
}
