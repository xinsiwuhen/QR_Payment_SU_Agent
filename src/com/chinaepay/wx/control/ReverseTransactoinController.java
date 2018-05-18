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

import org.apache.http.impl.client.CloseableHttpClient;
import org.xml.sax.SAXException;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.dao.TransOrderDAO;
import com.chinaepay.wx.dao.TransOrderReverseOrderDAO;
import com.chinaepay.wx.entity.PaymentTransactionEntity;
import com.chinaepay.wx.entity.RefundTransactionEntity;
import com.chinaepay.wx.entity.ReverseTransactionEntity;
import com.chinaepay.wx.entity.TransactionEntity;

/**
 * @author xinwuhen
 *	本类完成订单的撤销操作。
 */
public class ReverseTransactoinController extends TransactionController {
private static ReverseTransactoinController reverseTransController = null;
	
	private static final String STR_REVERSE_ORDER_URL = "https://api.mch.weixin.qq.com/secapi/pay/reverse";
	
	/**
	 * 获取本类的唯一实例。
	 * @return
	 */
	public static ReverseTransactoinController getInstance() {
		if (reverseTransController == null) {
			reverseTransController = new ReverseTransactoinController();
		}
		
		return reverseTransController;
	}
	
	/**
	 * 撤销单处理接口。
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 [微信定义的错误码]
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 */
	@Override
//	public HashMap<String, String> startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
	public String startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
//		// 校验订单请求参数
//		boolean blnValOrderArgs = blnValdOrderArgs(hmTransactionOrderCont);
//		if (!blnValOrderArgs) {
//			return orgnizeResponseInfo(ReverseTransactionEntity.SUCCESS, ReverseTransactionEntity.PARAM_ERROR, new String[] {hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(ReverseTransactionEntity.OUT_TRADE_NO), "", "", "", ""});
//		}
//		
//		// 校验代理商是否存在并有效
//		boolean blnValidAgnt = validateAgent(hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID));
//		
//		if (!blnValidAgnt) {
//			return orgnizeResponseInfo(ReverseTransactionEntity.SUCCESS, ReverseTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(ReverseTransactionEntity.OUT_TRADE_NO), 
//					hmTransactionOrderCont.get(ReverseTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(ReverseTransactionEntity.TOTAL_FEE),
//					CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
//		}
		
		// 撤销单信息入库
		hmTransactionOrderCont.put(ReverseTransactionEntity.TRANS_TIME, CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"));
		boolean blnPersistOrderInfo = insertOrderInfoToTbl(hmTransactionOrderCont);
		hmTransactionOrderCont.remove(ReverseTransactionEntity.TRANS_TIME);
//		if (!blnPersistOrderInfo) {
//			return orgnizeResponseInfo(ReverseTransactionEntity.SUCCESS, ReverseTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(ReverseTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(ReverseTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(ReverseTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), hmTransactionOrderCont.get(ReverseTransactionEntity.TRANSACTION_ID)});
//		}
		
		// 发送撤销单申请到微信端并获取应答信息
		HashMap<String, String> hmWXRespResult = null;
		String strReturnCode = null;
		String strResultCode = null;
		String strRecall = null;
		String strWXResponseResult = null;
		int iTotalTimes = 3; // 标识当微信端要求重新发起撤单请求时，撤单最多发起的次数
		try {
			for (int i = 0; i < iTotalTimes; i++) {
				CloseableHttpClient httpclient = CommonTool.getCertHttpClient(TransactionEntity.SSL_CERT_PASSWORD);
				strWXResponseResult = this.sendReqAndGetResp(STR_REVERSE_ORDER_URL, hmTransactionOrderCont, httpclient);
//				System.out.println("*strWXResponseResult = " + strWXResponseResult);
				// 解析XML并保存在Map中
				hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strWXResponseResult);
				
				strReturnCode = hmWXRespResult.get(ReverseTransactionEntity.RETURN_CODE);
				strResultCode = hmWXRespResult.get(ReverseTransactionEntity.RESULT_CODE);
				strRecall = hmWXRespResult.get(ReverseTransactionEntity.RECALL);
				
				// 不需要重调微信端的撤单请求
				if (strRecall != null && strRecall.equals("N") || 
						(strReturnCode != null && strReturnCode.equals(ReverseTransactionEntity.SUCCESS) && strResultCode != null && strResultCode.equals(ReverseTransactionEntity.SUCCESS))) {
					break;
				}
				
				// 暂停500毫秒
				try {
					Thread.sleep(1*500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
//			return orgnizeResponseInfo(ReverseTransactionEntity.SUCCESS, ReverseTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(ReverseTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(ReverseTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(ReverseTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), hmTransactionOrderCont.get(ReverseTransactionEntity.TRANSACTION_ID)});
		}
		
		
//		String strSysCommRst = null;
//		String strRespCommcialRst = null;
//		String[] strRespCommcialInfo = null;
		
		HashMap<String, String> respClonedMap = CommonTool.getCloneMap(hmWXRespResult);
		HashMap<String, String> newClonedMap = CommonTool.getAppendMap(respClonedMap, hmTransactionOrderCont);
		
		
		if (strReturnCode != null && strReturnCode.equals(ReverseTransactionEntity.SUCCESS) 
				&& strResultCode != null && strResultCode.equals(ReverseTransactionEntity.SUCCESS)) {	 // 撤单成功
//			strSysCommRst = ReverseTransactionEntity.SUCCESS;
//			strRespCommcialRst = ReverseTransactionEntity.SUCCESS;
			
			// 更新撤单结果到交易订单表(撤单成功)
			newClonedMap.put(PaymentTransactionEntity.TRADE_STATE, ReverseTransactionEntity.REVOKED);
			
		} else {	// 撤单失败
//			strSysCommRst = (strReturnCode == null || "".equals(strReturnCode)) ? ReverseTransactionEntity.FAIL : strReturnCode;
//			strRespCommcialRst = (strResultCode == null || "".equals(strResultCode)) ? ReverseTransactionEntity.SYSTEMERROR : strResultCode;
		}
		
//		Map<String, String> mapRespCommInfo = getRespCommcialInfo(hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), CommonInfo.TBL_TRANS_ORDER);
//		strRespCommcialInfo = new String[] {hmTransactionOrderCont.get(ReverseTransactionEntity.AGENT_ID), mapRespCommInfo.get(ReverseTransactionEntity.OUT_TRADE_NO), 
//								mapRespCommInfo.get(ReverseTransactionEntity.FEE_TYPE), mapRespCommInfo.get(ReverseTransactionEntity.TOTAL_FEE),
//								mapRespCommInfo.get(ReverseTransactionEntity.TIME_END), mapRespCommInfo.get(ReverseTransactionEntity.TRANSACTION_ID)};
		
		if (newClonedMap != null) {
			newClonedMap.put(ReverseTransactionEntity.TIME_END, CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"));
			boolean blnUpdateOrderRst = updateOrderInfoToTbl(newClonedMap);
		}
		
		// 返回本次撤单的处理结果到商户端
//		return orgnizeResponseInfo(strSysCommRst, strRespCommcialRst, strRespCommcialInfo);
		return strWXResponseResult;
	}

	/* (non-Javadoc)
	 * @see com.chinaepay.wx.control.TransactionController#insertOrderInfoToTbl(java.util.Map)
	 */
	@Override
	public boolean insertOrderInfoToTbl(Map<String, String> mapOrderInfo) {
		if (mapOrderInfo == null || mapOrderInfo.size() == 0) {
			return false;
		}
		
		boolean blnReturenRst = false;
		
		// 生成TransOrderDAO对象
		TransOrderDAO transOrderDao = (TransOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderDAO.class);
		if (transOrderDao == null) {
			System.out.println("生成DAO对象错误！");
			return false;
		}
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		
		try {
			// 判断tbl_trans_order表中是否有此商户订单号
			String strSimInqSql = getSimpleInquirySqlFromDAO(transOrderDao, CommonInfo.TBL_TRANS_ORDER);
			String strWhereArgs = " where out_trade_no='" + transOrderDao.getOut_trade_no() + "';";
			
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			preStat = conn.prepareStatement(strSimInqSql + strWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strWhereArgs));
			rs = preStat.executeQuery();
			if (rs != null && rs.next()) {	// tbl_trans_order_reverse_order表中没有当前where条件查询到的记录
				// 生成TransOrderReverseOrderDAO对象，并更新该对象中的数据
				TransOrderReverseOrderDAO tranOrderReveOrderDAO = (TransOrderReverseOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderReverseOrderDAO.class);
				if (tranOrderReveOrderDAO == null) {
					System.out.println("生成DAO对象错误！");
					return false;
				}
				
				
				// 判断数据库内信息是否重复
				// 判断tbl_trans_order_reverse_order表
				strSimInqSql = getSimpleInquirySqlFromDAO(tranOrderReveOrderDAO, CommonInfo.TBL_TRANS_ORDER_REVERSE_ORDER);
				strWhereArgs = " where out_trade_no='" + tranOrderReveOrderDAO.getOut_trade_no() + "';";
				preStat = conn.prepareStatement(strSimInqSql + strWhereArgs);
				System.out.println("strInqSql = " + (strSimInqSql + strWhereArgs));
				rs = preStat.executeQuery();
				if (rs != null && !rs.next()) {	// tbl_trans_order_reverse_order表中没有当前where条件查询到的记录
					// 插入数据到tbl_trans_order_reverse_order表
					String strSqlTransReverseOrderDao = getInsertSqlFromDAO(tranOrderReveOrderDAO, CommonInfo.TBL_TRANS_ORDER_REVERSE_ORDER);
					System.out.println("strSqlTransReverseOrderDao = " + strSqlTransReverseOrderDao);
					preStat = conn.prepareStatement(strSqlTransReverseOrderDao);
					int iRowsTransReverse = preStat.executeUpdate();
					System.out.println("iRowsTransReverse = " + iRowsTransReverse);
				}
				
				// 提交事务数据
				conn.commit();
				blnReturenRst = true;
			}
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
		if (mapOrderInfo == null) {
			return false;
		}
		
		boolean blnUpdateRst = false;
		Connection conn = null;
		PreparedStatement preStat = null;
		
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			
			// 更新tbl_trans_order表内的订单交易状态
			String strTradeStat = mapOrderInfo.get(PaymentTransactionEntity.TRADE_STATE);
			if (strTradeStat != null && !"".equals(strTradeStat)) {
				String strTransOrderSql = "update " + CommonInfo.TBL_TRANS_ORDER 
											+ " set trade_state='" + strTradeStat 
											+ "' where out_trade_no='" + mapOrderInfo.get(ReverseTransactionEntity.OUT_TRADE_NO) + "';";
				
				System.out.println("updateSql = " + strTransOrderSql);
				preStat = conn.prepareStatement(strTransOrderSql);
				int iUpdatedRows = preStat.executeUpdate();
			}
			
			// 更新tbl_trans_order_reverse_order表内的信息
			TransOrderReverseOrderDAO transReverseOrderDao = (TransOrderReverseOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderReverseOrderDAO.class);
			// 插入客户端发起的请求数据到数据库表
			if (transReverseOrderDao == null) {
				System.out.println("生成DAO对象错误！");
				return false;
			}
			
			String strSimpleUpdateSql = getSimpleUpdateSqlFromDAO(transReverseOrderDao, CommonInfo.TBL_TRANS_ORDER_REVERSE_ORDER);
			String strTransOrderWhereArgs = " where out_trade_no='" + transReverseOrderDao.getOut_trade_no() + "'";
			System.out.println("updateSql = " + (strSimpleUpdateSql + strTransOrderWhereArgs));
			preStat = conn.prepareStatement(strSimpleUpdateSql + strTransOrderWhereArgs);
			int iUpdatedRows = preStat.executeUpdate();
			
			conn.commit();
			blnUpdateRst = true;
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

}
