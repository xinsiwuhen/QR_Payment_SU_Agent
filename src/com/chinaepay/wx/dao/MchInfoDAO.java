/**
 * @author xinwuhen
 */
package com.chinaepay.wx.dao;

/**
 * @author xinwuhen
 *
 */
public class MchInfoDAO extends TblDAO {
	private String mch_id = "";
	private String sub_mch_id = "";
	private String appid = "";
	private String app_key = "";
	
	/**
	 * @return the mch_id
	 */
	public String getMch_id() {
		return mch_id;
	}
	/**
	 * @param mch_id the mch_id to set
	 */
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	/**
	 * @return the sub_mch_id
	 */
	public String getSub_mch_id() {
		return sub_mch_id;
	}
	/**
	 * @param sub_mch_id the sub_mch_id to set
	 */
	public void setSub_mch_id(String sub_mch_id) {
		this.sub_mch_id = sub_mch_id;
	}
	/**
	 * @return the appid
	 */
	public String getAppid() {
		return appid;
	}
	/**
	 * @param appid the appid to set
	 */
	public void setAppid(String appid) {
		this.appid = appid;
	}
	/**
	 * @return the app_key
	 */
	public String getApp_key() {
		return app_key;
	}
	/**
	 * @param app_key the app_key to set
	 */
	public void setApp_key(String app_key) {
		this.app_key = app_key;
	}
}
