/**
 * @author xinwuhen
 */
package com.chinaepay.wx.control;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.control.CommunicateController.ParsingWXResponseXML;
import com.chinaepay.wx.dao.MchInfoDAO;
import com.chinaepay.wx.dao.MchInfoTransOrderDAO;
import com.chinaepay.wx.dao.TransOrderDAO;
import com.chinaepay.wx.entity.InquiryTransactionEntity;
import com.chinaepay.wx.entity.PaymentTransactionEntity;
import com.chinaepay.wx.entity.RefundTransactionEntity;

/**
 * @author xinwuhen
 *	支付类型的订单处理类。
 */
public class PaymentTransactionController extends TransactionController {
	private static PaymentTransactionController paymentOrderCntrl = null;
	
	private static final String STR_PAYMENT_ORDER_URL = "https://api.mch.weixin.qq.com/pay/micropay";
	
	/**
	 * 获取本类的唯一实例。
	 * @return
	 */
	public static PaymentTransactionController getInstance() {
		if (paymentOrderCntrl == null) {
			paymentOrderCntrl = new PaymentTransactionController();
		}
		
		return paymentOrderCntrl;
	}
	
	
	/**
	 * 此方法是与商户的对接接口，方法内除了处理所有的易付通与微信后台的业务逻辑外，还要返回附合商户与易付通之间约定的报文。其中，返回给商户的报文格式，如下：
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 [微信定义的错误码]
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 */
//	public HashMap<String, String> startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
	public String startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
		// 支付订单参数校验
//		boolean blnValOrderArgs = blnValdOrderArgs(hmTransactionOrderCont);
//		if (!blnValOrderArgs) {
//			return orgnizeResponseInfo(PaymentTransactionEntity.SUCCESS, PaymentTransactionEntity.PARAM_ERROR, new String[] {hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), "", "", "", ""});
//		}
//		
//		// 校验代理商是否存在并有效
//		boolean blnValidAgnt = validateAgent(hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID));
//		
//		if (!blnValidAgnt) {
//			return orgnizeResponseInfo(PaymentTransactionEntity.SUCCESS, PaymentTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), 
//					hmTransactionOrderCont.get(PaymentTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(PaymentTransactionEntity.TOTAL_FEE),
//					CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
//		}
		
		// 关联代理商及终端商户
		refferAgentAndSubMerchant(hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(PaymentTransactionEntity.SUB_MCH_ID));
		
		// 初始订单信息持久化到DB
		hmTransactionOrderCont.put(PaymentTransactionEntity.TRANS_TIME, CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"));
		boolean blnPersistOrderInfo = insertOrderInfoToTbl(hmTransactionOrderCont);
		hmTransactionOrderCont.remove(PaymentTransactionEntity.TRANS_TIME);
//		if (!blnPersistOrderInfo) {
//			return orgnizeResponseInfo(PaymentTransactionEntity.SUCCESS, PaymentTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(PaymentTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(PaymentTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
//		}
		
		// 与微信后台进行交易对接，获取微信的实时应答报文, 并依据微信端应答结果进行不同的业务处理
		String strWXResponseResult = this.sendReqAndGetResp(STR_PAYMENT_ORDER_URL, hmTransactionOrderCont, CommonTool.getDefaultHttpClient());
		System.out.println("++++++++++++++strWXResponseResult = " + strWXResponseResult);
		// 校验交易结果，据此确定是否需要调用【查询订单的API】或【撤销订单的API】，并返回最后一次的交易应答信息(来自于微信后台)
		// 解析XML并保存在Map中
		HashMap<String, String> hmWXRespResult = null;
		try {
			hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strWXResponseResult);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
//			return orgnizeResponseInfo(PaymentTransactionEntity.SUCCESS, PaymentTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(PaymentTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(PaymentTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
		}
		
		
		String strReturnCode = hmWXRespResult.get(PaymentTransactionEntity.RETURN_CODE);
		String strResultCode = hmWXRespResult.get(PaymentTransactionEntity.RESULT_CODE);
		String strErrCode = hmWXRespResult.get(PaymentTransactionEntity.ERR_CODE);
		
//		String strSysCommRst = null;
//		String strRespCommcialRst = null;
//		String[] strRespCommcialInfo = null;
		
		// 交易失败
		if (strReturnCode == null || strResultCode == null
				|| !strReturnCode.equals(PaymentTransactionEntity.SUCCESS) || !strResultCode.equals(PaymentTransactionEntity.SUCCESS)) {
			// 判断通信及系统状态
//			strSysCommRst = (strReturnCode == null || "".equals(strReturnCode)) ? PaymentTransactionEntity.SYSTEMERROR : strReturnCode;
			
			// 处理支付业务出现失败
//			strRespCommcialRst = (strErrCode == null || "".equals(strErrCode)) ? PaymentTransactionEntity.SYSTEMERROR : strErrCode;
			
			// 更新交易结果到数据库(交易失败)
			HashMap<String, String> newClonedMap = CommonTool.getCloneMap(hmWXRespResult);
			if (strErrCode != null && !"".equals(strErrCode)) {
				newClonedMap.put(PaymentTransactionEntity.TRADE_STATE, strErrCode);
			} else {
				newClonedMap.put(PaymentTransactionEntity.TRADE_STATE, PaymentTransactionEntity.SYSTEMERROR);
			}
			newClonedMap.put(PaymentTransactionEntity.OUT_TRADE_NO, hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO));
			boolean blnUpdateOrderRst = updateOrderInfoToTbl(newClonedMap);
//			if (!blnUpdateOrderRst) {
//				strRespCommcialRst = PaymentTransactionEntity.SYSTEMERROR;
//			}
			
			// 用户密码支付中状态时，启动另一线程查询3次
			if (strErrCode != null && strErrCode.equals(PaymentTransactionEntity.USERPAYING)) {
				// 每5秒查询一次，持续查询1分半钟，确认用户是否支付成功
				ValidatePaymentResultThread vprt = new ValidatePaymentResultThread(hmTransactionOrderCont, 18, 10 * 1000);	
				new Thread(vprt).start();
			}
			
		} else { // 交易成功
			// 设置返回给商户端的应答信息
//			strSysCommRst = PaymentTransactionEntity.SUCCESS;
//			strRespCommcialRst = PaymentTransactionEntity.SUCCESS;

			// 更新交易结果到数据库(交易成功)
			HashMap<String, String> newClonedMap = CommonTool.getCloneMap(hmWXRespResult);
			newClonedMap.put(PaymentTransactionEntity.OUT_TRADE_NO, hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO));
			newClonedMap.put(PaymentTransactionEntity.TRADE_STATE, PaymentTransactionEntity.SUCCESS);
			boolean blnUpdateOrderRst = updateOrderInfoToTbl(newClonedMap);
//			if (!blnUpdateOrderRst) {
//				strRespCommcialRst = PaymentTransactionEntity.SYSTEMERROR;
//			}
		}
		
//		Map<String, String> mapRespCommInfo = getRespCommcialInfo(hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), CommonInfo.TBL_TRANS_ORDER);
//		strRespCommcialInfo = new String[] {hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID), mapRespCommInfo.get(PaymentTransactionEntity.OUT_TRADE_NO), 
//								mapRespCommInfo.get(PaymentTransactionEntity.FEE_TYPE), mapRespCommInfo.get(PaymentTransactionEntity.TOTAL_FEE),
//								mapRespCommInfo.get(PaymentTransactionEntity.TIME_END), mapRespCommInfo.get(PaymentTransactionEntity.TRANSACTION_ID)};
		
		// 返回本次交易的处理结果到商户端，由商户端根据交易结果作后续的查询操作
//		return orgnizeResponseInfo(strSysCommRst, strRespCommcialRst, strRespCommcialInfo);
		return strWXResponseResult;
	}
	
	/**
	 * 初始化交易单信息到数据库。
	 * @param strTblName
	 * @param hmDataValues
	 * @return
	 */
	public boolean insertOrderInfoToTbl(Map<String, String> mapOrderInfo) {
		if (mapOrderInfo == null || mapOrderInfo.size() == 0) {
			return false;
		}
		
		// 生成MchInfoDAO对象，并更新该对象中的数据
		MchInfoDAO mchInfoDao = (MchInfoDAO) loadMapInfoToDAO(mapOrderInfo, MchInfoDAO.class);
		// 生成MchInfoTransOrderDAO对象，并更新该对象中的数据
		MchInfoTransOrderDAO mchInfoTransOrderDao = (MchInfoTransOrderDAO) loadMapInfoToDAO(mapOrderInfo, MchInfoTransOrderDAO.class);
		// 生成TransOrderDAO对象，并更新该对象中的数据
		TransOrderDAO transOrderDao = (TransOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderDAO.class);
		
		// 插入客户端发起的请求数据到数据库表
		if (mchInfoDao == null || mchInfoTransOrderDao == null || transOrderDao == null) {
			System.out.println("生成DAO对象错误！");
			return false;
		}
		
		
		boolean blnReturenRst = false;
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			
			// 判断数据库内信息是否重复
			// 判断tbl_mch_info表
			String strSimInqSql = getSimpleInquirySqlFromDAO(mchInfoDao, CommonInfo.TBL_MCH_INFO);
			String strMchInfoWhereArgs = " where mch_id='" + mchInfoDao.getMch_id() + "' and sub_mch_id='" + mchInfoDao.getSub_mch_id() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strMchInfoWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strMchInfoWhereArgs));
			rs = preStat.executeQuery();
			if (rs != null && !rs.next()) {	// tbl_mch_info表中没有当前where条件查询到的记录
				// 插入数据到tbl_mch_info表
				String strSqlMchInfoDao = getInsertSqlFromDAO(mchInfoDao, CommonInfo.TBL_MCH_INFO);
				System.out.println("strSqlMchInfoDao = " + strSqlMchInfoDao);
				preStat = conn.prepareStatement(strSqlMchInfoDao);
				int iRowsMchInfo = preStat.executeUpdate();
				System.out.println("iRowsMchInfo = " + iRowsMchInfo);
			}
			
			// 判断tbl_mch_info_trans_order表
			strSimInqSql = getSimpleInquirySqlFromDAO(mchInfoTransOrderDao, CommonInfo.TBL_MCH_INFO_TRANS_ORDER);
			String strMchInfoTransOrderWhereArgs = " where mch_id='" + mchInfoTransOrderDao.getMch_id() + "' and sub_mch_id='" 
													+ mchInfoTransOrderDao.getSub_mch_id() + "' and out_trade_no ='" 
													+ mchInfoTransOrderDao.getOut_trade_no() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strMchInfoTransOrderWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strMchInfoWhereArgs));
			rs = preStat.executeQuery();
			if (rs != null && !rs.next()) {	// tbl_mch_info_trans_order表中没有当前where条件查询到的记录
				// 插入数据到tbl_mch_info_trans_order表
				String strSqlMchInfoTransOrderDao = getInsertSqlFromDAO(mchInfoTransOrderDao, CommonInfo.TBL_MCH_INFO_TRANS_ORDER);
				System.out.println("strSqlMchInfoTransOrderDao = " + strSqlMchInfoTransOrderDao);
				preStat = conn.prepareStatement(strSqlMchInfoTransOrderDao);
				int iRowsMchInfoTransOrder = preStat.executeUpdate();
				System.out.println("iRowsMchInfoTransOrder = " + iRowsMchInfoTransOrder);
			}
			
			// 判断tbl_trans_order表
			strSimInqSql = getSimpleInquirySqlFromDAO(transOrderDao, CommonInfo.TBL_TRANS_ORDER);
			String strTransOrderWhereArgs = " where out_trade_no ='" + transOrderDao.getOut_trade_no() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strTransOrderWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strTransOrderWhereArgs));
			rs = preStat.executeQuery();
			if (rs != null && !rs.next()) {	// tbl_trans_order表中没有当前where条件查询到的记录
				// 插入数据到tbl_trans_order表
				String strSqlTransOrderDao = getInsertSqlFromDAO(transOrderDao, CommonInfo.TBL_TRANS_ORDER);
				System.out.println("strSqlTransorderDao = " + strSqlTransOrderDao);
				preStat = conn.prepareStatement(strSqlTransOrderDao);
				int iRowsTransOrder = preStat.executeUpdate();
				System.out.println("iRowsTransOrder = " + iRowsTransOrder);
			}
			
			// 提交事务数据
			conn.commit();
			blnReturenRst = true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			
			// 执行Rollback操作
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			blnReturenRst = false;
			
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			
			if (preStat != null) {
				try {
					preStat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if (conn != null) {
				MysqlConnectionPool.getInstance().releaseConnection(conn);
			}
		}
		
		return blnReturenRst;
	}
	
	@Override
	public boolean updateOrderInfoToTbl(HashMap<String, String> mapOrderInfo) {
		// 生成TransOrderDAO对象，并更新该对象中的数据
		TransOrderDAO transOrderDao = (TransOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderDAO.class);
				
		// 插入客户端发起的请求数据到数据库表
		if (transOrderDao == null) {
			System.out.println("生成DAO对象错误！");
			return false;
		}
		
		boolean blnUpdateRst = false;
		Connection conn = null;
		PreparedStatement preStat = null;
		try {
			String strSimpleUpdateSql = getSimpleUpdateSqlFromDAO(transOrderDao, CommonInfo.TBL_TRANS_ORDER);
			if (strSimpleUpdateSql != null && !"".equals(strSimpleUpdateSql)) {
				conn = MysqlConnectionPool.getInstance().getConnection(false);
				String strTransOrderWhereArgs = " where out_trade_no='" + transOrderDao.getOut_trade_no() + "'";
				System.out.println("updateSql = " + (strSimpleUpdateSql + strTransOrderWhereArgs));
				preStat = conn.prepareStatement(strSimpleUpdateSql + strTransOrderWhereArgs);
				int iUpdatedRows = preStat.executeUpdate();
				conn.commit();
				blnUpdateRst = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			blnUpdateRst = false;
		} finally {
			if (preStat != null) {
				try {
					preStat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if (conn != null) {
				MysqlConnectionPool.getInstance().releaseConnection(conn);
			}
		}
		
		return blnUpdateRst;
	}
	
	/**
	 * 校验支付是否成功的独立线程。
	 * @author xinwuhen
	 */
	private class ValidatePaymentResultThread implements Runnable {
		private HashMap<String, String> hmTransactionOrderCont = null;
		// 校验的最大次数
		private int iMaxTimes = 0;
		// 每次检验的间隔时间(毫秒)
		private long lMiniScnds = 0;
		
		public ValidatePaymentResultThread(HashMap<String, String> hmTransactionOrderCont, int iMaxTimes, long lMiniScnds) {
			this.hmTransactionOrderCont = hmTransactionOrderCont;
			this.iMaxTimes = iMaxTimes;
			this.lMiniScnds = lMiniScnds;
		}
		
		@Override
		public void run() {
			if (hmTransactionOrderCont != null && hmTransactionOrderCont.size() > 0) {
				StringBuffer sb = new StringBuffer();
				sb.append(InquiryTransactionEntity.AGENT_ID + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.AGENT_ID));
				sb.append("&" + InquiryTransactionEntity.APPID + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.APPID));
				sb.append("&" + InquiryTransactionEntity.MCH_ID + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.MCH_ID));
				sb.append("&" + InquiryTransactionEntity.SUB_MCH_ID + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.SUB_MCH_ID));
				sb.append("&" + InquiryTransactionEntity.OUT_TRADE_NO + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO));	// 测试时修改此参数
				sb.append("&" + InquiryTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
				sb.append("&" + InquiryTransactionEntity.APP_KEY + "=" + hmTransactionOrderCont.get(PaymentTransactionEntity.APP_KEY));
				String strInquiryTransSign = CommonTool.getEntitySign(CommonTool.formatStrToMap(sb.toString()));
				sb.append("&" + InquiryTransactionEntity.SIGN + "=" + strInquiryTransSign);
				
				HashMap<String, String> hmInquiryOrder = CommonTool.formatStrToMap(sb.toString());
				
				InquiryTransactionController inquiryTransCntrl = InquiryTransactionController.getInstance();
				for (int i = 0; i < iMaxTimes; i++) {
					System.out.println("--->i = " + (i+1));

					// 查询订单支付结果
//					HashMap<String, String> hmInquiryRst = inquiryTransCntrl.startInquiryOrder(hmInquiryOrder);
					String strResponRst = inquiryTransCntrl.startInquiryOrder(hmInquiryOrder);
//					if (hmInquiryRst != null && hmInquiryRst.size() > 0) {
//						String strCommuRst = hmInquiryRst.get(InquiryTransactionEntity.SYSTEM_COMM_RESULT_KEY);
//						String strBizRst = hmInquiryRst.get(InquiryTransactionEntity.BUSINESS_PROC_RESULT_KEY);
//						
//						if (strCommuRst != null && strCommuRst.equals(PaymentTransactionEntity.SUCCESS) 
//								&& strBizRst != null && strBizRst.equals(PaymentTransactionEntity.SUCCESS)) {	// 支付成功
//							// 由于在查询交易单时，在查询过程内部已经进行数据库表(tbl_trans_order)的交易状态(trade_state)更新，故此处不再需要更新数据库，直接跳出循环即可
//							break;
//						}
//					}
					// 解析XML并保存在Map中
					HashMap<String, String> hmWXRespResult = null;
					try {
						hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strResponRst);
					} catch (ParserConfigurationException | IOException | SAXException e) {
						e.printStackTrace();
//						return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), 
//															hmTransactionOrderCont.get(RefundTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(RefundTransactionEntity.TOTAL_FEE),
//															CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
					}
					
					String strReturnCode = hmWXRespResult.get(InquiryTransactionEntity.RETURN_CODE);
					String strResultCode = hmWXRespResult.get(InquiryTransactionEntity.RESULT_CODE);
					String strTradeStatus = hmWXRespResult.get(InquiryTransactionEntity.TRADE_STATE);
					if (strReturnCode != null && strResultCode != null && strTradeStatus != null
							&& strReturnCode.equals(InquiryTransactionEntity.SUCCESS) && strResultCode.equals(InquiryTransactionEntity.SUCCESS)
							&& strTradeStatus.equals(InquiryTransactionEntity.SUCCESS)) {
						break;
					}
					
					
					try {
						Thread.sleep(lMiniScnds);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
