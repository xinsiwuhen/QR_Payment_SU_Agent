/**
 * @author xinwuhen
 */
package com.chinaepay.wx.dao;

/**
 * @author xinwuhen
 *
 */
public class TransOrderReverseOrderDAO extends TblDAO {
	private String out_trade_no = "";
	private String transaction_id = "";
	private String return_code = "";
	private String result_code = "";
	private String err_code = "";
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
	 * @return the return_code
	 */
	public String getReturn_code() {
		return return_code;
	}
	/**
	 * @param return_code the return_code to set
	 */
	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}
	/**
	 * @return the result_code
	 */
	public String getResult_code() {
		return result_code;
	}
	/**
	 * @param result_code the result_code to set
	 */
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	/**
	 * @return the err_code
	 */
	public String getErr_code() {
		return err_code;
	}
	/**
	 * @param err_code the err_code to set
	 */
	public void setErr_code(String err_code) {
		this.err_code = err_code;
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
