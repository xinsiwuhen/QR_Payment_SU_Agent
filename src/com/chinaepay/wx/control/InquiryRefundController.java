/**
 * @author xinwuhen
 */
package com.chinaepay.wx.control;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.entity.InquiryRefundEntity;
import com.chinaepay.wx.entity.PaymentTransactionEntity;

/**
 * @author xinwuhen
 *
 */
public class InquiryRefundController extends InquiryController {
	private static InquiryRefundController inquiryRefundContrl = null;
	
	private static final String INQUIRY_REFUND_ORDER_URL = "https://api.mch.weixin.qq.com/pay/refundquery";
	
	/**
	 * 获取本类的唯一实例。
	 * @return
	 */
	public static InquiryRefundController getInstance() {
		if (inquiryRefundContrl == null) {
			inquiryRefundContrl = new InquiryRefundController();
		}
		return inquiryRefundContrl;
	}

	@Override
	/**
	 * 返回给商户的报文格式，如下：
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 [微信定义的错误码]
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 */
//	public HashMap<String, String> startInquiryOrder(HashMap<String, String> hmInquiryOrderCont) {
	public String startInquiryOrder(HashMap<String, String> hmInquiryOrderCont) {
//		// 校验查询类订单的参数
//		boolean blnValOrderArgs = blnValdOrderArgs(hmInquiryOrderCont);
//		if (!blnValOrderArgs) {
//			return orgnizeResponseInfo(InquiryRefundEntity.SUCCESS, InquiryRefundEntity.PARAM_ERROR, new String[] {hmInquiryOrderCont.get(InquiryRefundEntity.AGENT_ID), hmInquiryOrderCont.get(InquiryRefundEntity.OUT_TRADE_NO), "", "", "", ""});
//		}
//		
//		// 校验代理商是否存在并有效
//		boolean blnValidAgnt = validateAgent(hmInquiryOrderCont.get(InquiryRefundEntity.AGENT_ID));
//		
//		if (!blnValidAgnt) {
//			return orgnizeResponseInfo(PaymentTransactionEntity.SUCCESS, InquiryRefundEntity.SYSTEMERROR, new String[] {hmInquiryOrderCont.get(InquiryRefundEntity.AGENT_ID), hmInquiryOrderCont.get(InquiryRefundEntity.OUT_TRADE_NO), 
//					hmInquiryOrderCont.get(InquiryRefundEntity.FEE_TYPE), hmInquiryOrderCont.get(InquiryRefundEntity.TOTAL_FEE),
//					CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
//		}
		
		// 与微信后台进行交易对接，获取微信的实时应答报文, 并依据微信端应答结果进行不同的业务处理
		String strWXResponseResult = this.sendReqAndGetResp(INQUIRY_REFUND_ORDER_URL, hmInquiryOrderCont, CommonTool.getDefaultHttpClient());
//		System.out.println(">>>>strWXResponseResult" + strWXResponseResult);
		// 解析XML并保存在Map中
		HashMap<String, String> hmWXRespResult = null;
		try {
			hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strWXResponseResult);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
//			return orgnizeResponseInfo(InquiryRefundEntity.SUCCESS, InquiryRefundEntity.SYSTEMERROR, new String[] {hmInquiryOrderCont.get(InquiryRefundEntity.AGENT_ID), hmInquiryOrderCont.get(InquiryRefundEntity.OUT_TRADE_NO), 
//												hmInquiryOrderCont.get(InquiryRefundEntity.FEE_TYPE), hmInquiryOrderCont.get(InquiryRefundEntity.TOTAL_FEE),
//												CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), hmInquiryOrderCont.get(InquiryRefundEntity.TRANSACTION_ID)});
		}
		
//		// 校验微信端返回的应答信息
//		String strInquiryReturnCode = hmWXRespResult.get(InquiryRefundEntity.RETURN_CODE);
//		String strInquiryResultCode = hmWXRespResult.get(InquiryRefundEntity.RESULT_CODE);
//		String strInquiryErrCode = hmWXRespResult.get(InquiryRefundEntity.ERR_CODE);
//		String strRefundResult = getValBaseExtKey(hmWXRespResult, InquiryRefundEntity.REFUND_STATUS);
//		
//		String strSysCommRst = null;
//		String strProcResult = null;
//		String[] strRespResult = null;
//		if ((strInquiryReturnCode != null && strInquiryResultCode != null && strRefundResult != null)
//				&&
//			(strInquiryReturnCode.equals(InquiryRefundEntity.SUCCESS) && strInquiryResultCode.equals(InquiryRefundEntity.SUCCESS) && strRefundResult.equals(InquiryRefundEntity.SUCCESS))) {
//			// 更新返回给商户的应答报文
//			strSysCommRst = InquiryRefundEntity.SUCCESS;
//			strProcResult = strRefundResult;
//		} else {
//			// 判断通信及系统状态(包含：通信链路是否正常以及订单是否存在等)
//			if (strInquiryReturnCode != null && strInquiryReturnCode.equals(InquiryRefundEntity.SUCCESS) && strInquiryResultCode != null && strInquiryResultCode.equals(InquiryRefundEntity.SUCCESS)) {
//				strSysCommRst = InquiryRefundEntity.SUCCESS;
//			} else {
//				if (strInquiryErrCode != null && !"".equals(strInquiryErrCode)) {
//					strSysCommRst = strInquiryErrCode;
//				} else {
//					strSysCommRst = InquiryRefundEntity.SYSTEMERROR;
//				}
//			}
//			
//			// 判断订单交易状态
//			if (strRefundResult != null && !"".equals(strRefundResult)) {
//				strProcResult = strRefundResult;
//			}  else {
//				strProcResult = InquiryRefundEntity.SYSTEMERROR;
//			}
//		}
		
		// 更新订单状态到交易订单表
		boolean blnUpdateOrder = updateOrderInfoToTbl(hmWXRespResult);
//		if (!blnUpdateOrder) {
//			strSysCommRst = InquiryRefundEntity.SUCCESS;
//			strProcResult = InquiryRefundEntity.SYSTEMERROR;
//		}
//		
//		Map<String, String> mapRespCommInfo = getRespCommcialInfo(hmInquiryOrderCont.get(PaymentTransactionEntity.OUT_TRADE_NO), CommonInfo.TBL_REFUND_ORDER);
//		strRespResult = new String[] {hmInquiryOrderCont.get(InquiryRefundEntity.AGENT_ID), mapRespCommInfo.get(InquiryRefundEntity.OUT_TRADE_NO), 
//										mapRespCommInfo.get(InquiryRefundEntity.FEE_TYPE), mapRespCommInfo.get(InquiryRefundEntity.TOTAL_FEE),
//										mapRespCommInfo.get(InquiryRefundEntity.TIME_END), mapRespCommInfo.get(InquiryRefundEntity.TRANSACTION_ID)};
//		
//		return orgnizeResponseInfo(strSysCommRst, strProcResult, strRespResult);
		
		return strWXResponseResult; 
	}

	@Override
	public boolean updateOrderInfoToTbl(HashMap<String, String> mapOrderInfo) {
		boolean blnUpdateRst = false;
		Connection conn = null;
		PreparedStatement preStat = null;
		String strFinalUpSql = "update " + CommonInfo.TBL_REFUND_ORDER 
								+ " set refund_status='" + castNullToBlank(getValBaseExtKey(mapOrderInfo, InquiryRefundEntity.REFUND_STATUS)) 
								+ "', refund_channel='" + castNullToBlank(getValBaseExtKey(mapOrderInfo, InquiryRefundEntity.REFUND_CHANNEL)) 
								+ "', refund_success_time='" + castNullToBlank(getValBaseExtKey(mapOrderInfo, InquiryRefundEntity.REFUND_SUCCESS_TIME)) 
								+ "', refund_recv_accout='" + castNullToBlank(getValBaseExtKey(mapOrderInfo, InquiryRefundEntity.REFUND_RECV_ACCOUT)) 
								+ "', rate='" + castNullToBlank(mapOrderInfo.get(InquiryRefundEntity.RATE)) 
								+ "' where out_refund_no='" + castNullToBlank(getValBaseExtKey(mapOrderInfo, InquiryRefundEntity.OUT_REFUND_NO))
								+ "' and out_trade_no='" + castNullToBlank(mapOrderInfo.get(InquiryRefundEntity.OUT_TRADE_NO)) + "';";
		System.out.println("strFinalUpSql = " + strFinalUpSql);
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			preStat = conn.prepareStatement(strFinalUpSql);
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
	
	/**
	 * 查询退款单时，针对微信返回的带有下标($n，如：refund_success_time_$n)的字段取值时，所进行的特殊处理。
	 * @param map
	 * @param strKey
	 * @return
	 */
	private String getValBaseExtKey(HashMap<String, String> map, String strKey) {
		if (map == null || strKey == null || "".equals(strKey)) {
			return "";
		}
		
		String strVal = "";
		
		// 退款时微信所允许的一次交易所被拆解的最大退款单数, 默认为：50
		final int MAX_EXT_NUM = 50;
		String strFullKey = null;
		for (int i = 0; i < MAX_EXT_NUM; i++) {
			strFullKey = strKey + "_" + i;
			if (map.containsKey(strFullKey)) {
				strVal = map.get(strFullKey);
				break;
			}
		}
		
		return strVal;
	}
	
	/**
	 * 将Null字符串转化为“”。
	 * @param strSrc
	 * @return
	 */
	private String castNullToBlank(String strSrc) {
		return strSrc == null ? "" : strSrc;
	}

}
