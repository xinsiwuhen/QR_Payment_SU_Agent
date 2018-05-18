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
import com.chinaepay.wx.dao.MchInfoDAO;
import com.chinaepay.wx.dao.RefundOrderDAO;
import com.chinaepay.wx.dao.TransOrderDAO;
import com.chinaepay.wx.dao.TransOrderRefundOrderDAO;
import com.chinaepay.wx.entity.PaymentTransactionEntity;
import com.chinaepay.wx.entity.RefundTransactionEntity;
import com.chinaepay.wx.entity.TransactionEntity;

/**
 * @author xinwuhen
 *	本类完成订单的退款操作。
 */
public class RefundTransactionController extends TransactionController {
	private static RefundTransactionController refundOrderCntrl = null;
	
	private static final String STR_REFUND_ORDER_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	
	/**
	 * 获取本类的唯一实例。
	 * @return
	 */
	public static RefundTransactionController getInstance() {
		if (refundOrderCntrl == null) {
			refundOrderCntrl = new RefundTransactionController();
		}
		
		return refundOrderCntrl;
	}

	@Override
	/**
	 * 此方法是与商户的对接接口，方法内除了处理所有的易付通与微信后台的业务逻辑外，还要返回附合商户与易付通之间约定的报文。其中，返回给商户的报文格式，如下：
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 [微信定义的错误码]
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 */
//	public HashMap<String, String> startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
	public String startTransactionOrder(HashMap<String, String> hmTransactionOrderCont) {
//		// 支付订单参数校验
//		boolean blnValOrderArgs = blnValdOrderArgs(hmTransactionOrderCont);
//		if (!blnValOrderArgs) {
//			return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.PARAM_ERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), RefundTransactionEntity.OUT_TRADE_NO, "", "", "", ""});
//		}
//		
//		// 校验代理商是否存在并有效
//		boolean blnValidAgnt = validateAgent(hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID));
//		
//		if (!blnValidAgnt) {
//			return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), 
//					hmTransactionOrderCont.get(RefundTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(RefundTransactionEntity.TOTAL_FEE),
//					CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
//		}		
		
		// 初始订单信息持久化到DB
		hmTransactionOrderCont.put(RefundTransactionEntity.TRANS_TIME, CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"));
		boolean blnPersistOrderInfo = insertOrderInfoToTbl(hmTransactionOrderCont);
		System.out.println("blnPersistOrderInfo = " + blnPersistOrderInfo);
		hmTransactionOrderCont.remove(RefundTransactionEntity.TRANS_TIME);
//		if (!blnPersistOrderInfo) {
//			return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(RefundTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(RefundTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), hmTransactionOrderCont.get(RefundTransactionEntity.TRANSACTION_ID)});
//		}
		
		// 与微信后台进行交易对接，获取微信的实时应答报文, 并依据微信端应答结果进行不同的业务处理
		CloseableHttpClient httpclient = CommonTool.getCertHttpClient(TransactionEntity.SSL_CERT_PASSWORD);
		String strWXResponseResult = this.sendReqAndGetResp(STR_REFUND_ORDER_URL, hmTransactionOrderCont, httpclient);
//		System.out.println("strWXResponseResult = " + strWXResponseResult);
		// 解析XML并保存在Map中
		HashMap<String, String> hmWXRespResult = null;
		try {
			hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strWXResponseResult);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
//			return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), 
//												hmTransactionOrderCont.get(RefundTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(RefundTransactionEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
		}
		
		String strReturnCode = hmWXRespResult.get(RefundTransactionEntity.RETURN_CODE);
		String strResultCode = hmWXRespResult.get(RefundTransactionEntity.RESULT_CODE);
		String strErrCode = hmWXRespResult.get(RefundTransactionEntity.ERR_CODE);
		
//		String strSysCommRst = null;
//		String strRespCommcialRst = null;
//		String[] strRespCommcialInfo = null;
		
		
		HashMap<String, String> newClonedMap = CommonTool.getCloneMap(hmWXRespResult);
		// 退款申请失败
		if (strReturnCode == null || strResultCode == null
				|| !strReturnCode.equals(RefundTransactionEntity.SUCCESS) || !strResultCode.equals(RefundTransactionEntity.SUCCESS)) {
//			// 判断通信及系统状态
//			strSysCommRst = (strReturnCode == null || "".equals(strReturnCode)) ? RefundTransactionEntity.SYSTEMERROR : strReturnCode;
//			
//			// 处理支付业务出现失败
//			strRespCommcialRst = (strErrCode == null || "".equals(strErrCode)) ? RefundTransactionEntity.SYSTEMERROR : strErrCode;
		} else { // 退款申请成功
//			// 设置返回给商户端的应答信息
//			strSysCommRst = RefundTransactionEntity.SUCCESS;
//			strRespCommcialRst = RefundTransactionEntity.SUCCESS;
			
			// 更新交易单状态为【转入退款】
			newClonedMap.put(PaymentTransactionEntity.TRADE_STATE, PaymentTransactionEntity.REFUND);
		}
		
		boolean blnUpdateOrderRst = updateOrderInfoToTbl(newClonedMap);
		
//		Map<String, String> mapRespCommInfo = getRespCommcialInfo(hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), CommonInfo.TBL_REFUND_ORDER);
//		strRespCommcialInfo = new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), mapRespCommInfo.get(RefundTransactionEntity.OUT_TRADE_NO), 
//								mapRespCommInfo.get(RefundTransactionEntity.FEE_TYPE), mapRespCommInfo.get(RefundTransactionEntity.TOTAL_FEE),
//								mapRespCommInfo.get(RefundTransactionEntity.TIME_END), mapRespCommInfo.get(RefundTransactionEntity.TRANSACTION_ID)};
//		
//		// 返回本次交易的处理结果到商户端，由商户端根据交易结果作后续的查询操作
//		return orgnizeResponseInfo(strSysCommRst, strRespCommcialRst, strRespCommcialInfo);
		
		return strWXResponseResult;
	}
	
	@Override
	public boolean insertOrderInfoToTbl(Map<String, String> mapOrderInfo) {
		if (mapOrderInfo == null || mapOrderInfo.size() == 0) {
			return false;
		}
		
		// 生成MchInfoDAO对象
		MchInfoDAO mchInfoDao = (MchInfoDAO) loadMapInfoToDAO(mapOrderInfo, MchInfoDAO.class);
		// 生成MchInfoTransOrderDAO对象
		// MchInfoTransOrderDAO mchInfoTransOrderDao = (MchInfoTransOrderDAO) loadMapInfoToDAO(mapOrderInfo, MchInfoTransOrderDAO.class);
		// 生成TransOrderDAO对象
		TransOrderDAO transOrderDao = (TransOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderDAO.class);
		// 插入客户端发起的请求数据到数据库表
		if (mchInfoDao == null || /** mchInfoTransOrderDao == null || **/ transOrderDao == null) {
			System.out.println("生成DAO对象错误！");
			return false;
		}
		
		boolean blnReturenRst = false;
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			
			// 判断数据库内是否存在商户号、子商户号、商户订单号等信息
			// 判断tbl_mch_info表内商户号、子商户号是否存在
			String strSimInqSql = getSimpleInquirySqlFromDAO(mchInfoDao, CommonInfo.TBL_MCH_INFO);
			String strMchInfoWhereArgs = " where mch_id='" + mchInfoDao.getMch_id() + "' and sub_mch_id='" + mchInfoDao.getSub_mch_id() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strMchInfoWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strMchInfoWhereArgs));
			rs = preStat.executeQuery();
			// tbl_mch_info表中无此商户号与子商户号
			if (!rs.next()) {	
				blnReturenRst = false;
				return blnReturenRst;
			}
				
			// 判断tbl_trans_order表内商户订单号是否存在
			strSimInqSql = getSimpleInquirySqlFromDAO(transOrderDao, CommonInfo.TBL_TRANS_ORDER);
			String strTransOrderWhereArgs = " where out_trade_no ='" + transOrderDao.getOut_trade_no() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strTransOrderWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strTransOrderWhereArgs));
			rs = preStat.executeQuery();
			// tbl_trans_order表中无此商户订单号
			if (!rs.next()) {	
				blnReturenRst = false;
				return blnReturenRst;
			}
			
			
			// 判断tbl_trans_order_refund_order表是否存在对应的记录
			TransOrderRefundOrderDAO tranRefundOrderDAO = (TransOrderRefundOrderDAO) loadMapInfoToDAO(mapOrderInfo, TransOrderRefundOrderDAO.class);
			strSimInqSql = getSimpleInquirySqlFromDAO(tranRefundOrderDAO, CommonInfo.TBL_TRANS_ORDER_REFUND_ORDER);
			String strTransRefundOrderWhereArgs = " where out_trade_no ='" + tranRefundOrderDAO.getOut_trade_no() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strTransRefundOrderWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strTransRefundOrderWhereArgs));
			rs = preStat.executeQuery();
			if (!rs.next()) {
				// 插入数据到tbl_trans_order_refund_order表
				String strSqlTransRefundOrderDao = getInsertSqlFromDAO(tranRefundOrderDAO, CommonInfo.TBL_TRANS_ORDER_REFUND_ORDER);
				System.out.println("strSqlTransRefundOrderDao = " + strSqlTransRefundOrderDao);
				preStat = conn.prepareStatement(strSqlTransRefundOrderDao);
				int iRowsTransRefundOrder = preStat.executeUpdate();
				System.out.println("iRowsTransRefundOrder = " + iRowsTransRefundOrder);
			}
			
			// 判断tbl_refund_order表是否存在支付订单对应的退款单记录
			RefundOrderDAO refundOrderDAO = (RefundOrderDAO) loadMapInfoToDAO(mapOrderInfo, RefundOrderDAO.class);
			strSimInqSql = getSimpleInquirySqlFromDAO(refundOrderDAO, CommonInfo.TBL_REFUND_ORDER);
			String strRefundOrderWhereArgs = " where out_trade_no ='" + refundOrderDAO.getOut_trade_no() + "';";
			preStat = conn.prepareStatement(strSimInqSql + strRefundOrderWhereArgs);
			System.out.println("strInqSql = " + (strSimInqSql + strRefundOrderWhereArgs));
			rs = preStat.executeQuery();
			
			if (rs.next()) { // 存在该商户订单号的记录
				String strRefundId = rs.getString("refund_id");
				if (strRefundId != null && !"".equals(strRefundId)) { // 并且微信端还未生成退款单号refund_id（即：退款申请还未被微信端成功接受）
					blnReturenRst = false;
					return blnReturenRst;
				} else {
					// 删除旧的记录
					strSimInqSql = "delete from " + CommonInfo.TBL_REFUND_ORDER + " where out_refund_no='" + refundOrderDAO.getOut_refund_no() + "' and out_trade_no='" + refundOrderDAO.getOut_trade_no() + "';" ;
					preStat = conn.prepareStatement(strSimInqSql);
					System.out.println("strInqSql = " + strSimInqSql);
					int iDelRows = preStat.executeUpdate();
				}
			}
			
			// 插入数据到tbl_refund_order表 
			String strSqlRefundOrderDao = getInsertSqlFromDAO(refundOrderDAO, CommonInfo.TBL_REFUND_ORDER);
			System.out.println("strSqlRefundOrderDao = " + strSqlRefundOrderDao);
			preStat = conn.prepareStatement(strSqlRefundOrderDao);
			int iRowsRefundOrder = preStat.executeUpdate();
			System.out.println("iRowsRefundOrder = " + iRowsRefundOrder);
			
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
											+ "' where out_trade_no='" + mapOrderInfo.get(RefundTransactionEntity.OUT_TRADE_NO) + "';";
				System.out.println("updateSql = " + strTransOrderSql);
				preStat = conn.prepareStatement(strTransOrderSql);
				int iUpdatedRows = preStat.executeUpdate();
			}
						
			// 更新tbl_refund_order表内的订单交易状态
			RefundOrderDAO refundOrderDAO = (RefundOrderDAO) loadMapInfoToDAO(mapOrderInfo, RefundOrderDAO.class);
			// 插入客户端发起的请求数据到数据库表
			if (refundOrderDAO == null) {
				System.out.println("生成DAO对象错误！");
				return false;
			}
			
			String strSimpleUpdateSql = getSimpleUpdateSqlFromDAO(refundOrderDAO, CommonInfo.TBL_REFUND_ORDER);
			if (strSimpleUpdateSql != null && !"".equals(strSimpleUpdateSql)) {
				String strRefundOrderWhereArgs = " where out_refund_no='" + refundOrderDAO.getOut_refund_no() + "' and out_trade_no='" + refundOrderDAO.getOut_trade_no() + "';";
				System.out.println("updateSql = " + (strSimpleUpdateSql + strRefundOrderWhereArgs));
				preStat = conn.prepareStatement(strSimpleUpdateSql + strRefundOrderWhereArgs);
				int iUpdatedRows = preStat.executeUpdate();
			}
			
			// 提交事务
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
