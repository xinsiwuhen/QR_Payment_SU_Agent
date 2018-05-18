package com.chinaepay.wx.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.control.CommunicateController.ParsingWXResponseXML;
import com.chinaepay.wx.control.RefundTransactionController;
import com.chinaepay.wx.entity.RefundTransactionEntity;

/**
 * 
 * @author xinwuhen
 */
public class RefundOrderServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		PrintWriter pw = null;
		try {
			request.setCharacterEncoding("UTF-8");
			String outTradeNo = CommonTool.urlDecodeUTF8(request.getParameter("out_trade_no"));
			System.out.println("outTradeNo = " + outTradeNo);
			
			if (outTradeNo != null && !"".equals(outTradeNo)) {
				// 查询mch_id/sub_mch_id/appid/key信息
				StringBuffer sb = new StringBuffer();
				sb.append("select tmi.mch_id, tmi.sub_mch_id, tmi.appid, tmi.app_key from ");
				sb.append(CommonInfo.TBL_MCH_INFO);
				sb.append(" as tmi, ");
				sb.append(CommonInfo.TBL_MCH_INFO_TRANS_ORDER);
				sb.append(" as tmito where tmi.mch_id=tmito.mch_id and tmi.sub_mch_id=tmito.sub_mch_id and tmito.out_trade_no='");
				sb.append(outTradeNo);
				sb.append("';");
				System.out.println("sb = " + sb.toString());
				conn = MysqlConnectionPool.getInstance().getConnection(true);
				preStat = conn.prepareStatement(sb.toString());
				rs = preStat.executeQuery();
				if (rs != null && rs.next()) {
					// 获取参数值
					String strMchId = rs.getString(RefundTransactionEntity.MCH_ID);
					String strSubMchId = rs.getString(RefundTransactionEntity.SUB_MCH_ID);
					String strAppId = rs.getString(RefundTransactionEntity.APPID);
					String strKEY = rs.getString(RefundTransactionEntity.APP_KEY);
					
					sb = new StringBuffer();
					sb.append("select total_fee from ");
					sb.append(CommonInfo.TBL_TRANS_ORDER);
					sb.append(" where out_trade_no='");
					sb.append(outTradeNo);
					sb.append("';");
					System.out.println("sb = " + sb.toString());
					preStat = conn.prepareStatement(sb.toString());
					rs = preStat.executeQuery();
					if (rs != null && rs.next()) {
						int iTotalFee = rs.getInt(RefundTransactionEntity.TOTAL_FEE);
						int iRefundFee = iTotalFee;
						
						// 执行退款操作
						StringBuffer sbPrams = new StringBuffer();
						sbPrams.append(RefundTransactionEntity.APPID + "=" + strAppId);
						sbPrams.append("&" + RefundTransactionEntity.MCH_ID + "=" + strMchId);
						sbPrams.append("&" + RefundTransactionEntity.SUB_MCH_ID + "=" + strSubMchId);
						sbPrams.append("&" + RefundTransactionEntity.NONCE_STR + "=" + CommonTool.getRandomString(32));
						sbPrams.append("&" + RefundTransactionEntity.OUT_TRADE_NO + "=" + outTradeNo);
						sbPrams.append("&" + RefundTransactionEntity.OUT_REFUND_NO + "=" + CommonTool.getOutRefundNo(new Date(), 18));
						sbPrams.append("&" + RefundTransactionEntity.TOTAL_FEE + "=" + iTotalFee);
						sbPrams.append("&" + RefundTransactionEntity.REFUND_FEE + "=" + iRefundFee);
						sbPrams.append("&" + RefundTransactionEntity.APP_KEY + "=" + strKEY);
						String strRefundOrderSign = CommonTool.getEntitySign(CommonTool.formatStrToMap(sbPrams.toString()));
						sbPrams.append("&" + RefundTransactionEntity.SIGN + "=" + strRefundOrderSign);
						
						HashMap<String, String> hmTransactionOrderCont = CommonTool.formatStrToMap(sbPrams.toString());
//						HashMap<String, String> hmRefundResp = RefundTransactionController.getInstance().startTransactionOrder(hmTransactionOrderCont);
						String strRefundResp = RefundTransactionController.getInstance().startTransactionOrder(hmTransactionOrderCont);
						
//						if (hmRefundResp != null && hmRefundResp.size() > 0) {
//							
//							String strCommuRst = hmRefundResp.get(RefundTransactionEntity.SYSTEM_COMM_RESULT_KEY);
//							String strBizRst = hmRefundResp.get(RefundTransactionEntity.BUSINESS_PROC_RESULT_KEY);
						if (strRefundResp != null) {	
							String strResp = RefundTransactionEntity.FAIL;
//							if (strCommuRst != null && strCommuRst.equals(RefundTransactionEntity.SUCCESS) 
//									&& strBizRst != null && strBizRst.equals(RefundTransactionEntity.SUCCESS)) {	// 申请退款业务授理成功
//								strResp = RefundTransactionEntity.SUCCESS;
//							}
							
							// 解析XML并保存在Map中
							HashMap<String, String> hmWXRespResult = null;
							try {
								hmWXRespResult = new ParsingWXResponseXML().getMapBaseWXRespResult(strRefundResp);
							} catch (ParserConfigurationException | IOException | SAXException e) {
								e.printStackTrace();
//								return orgnizeResponseInfo(RefundTransactionEntity.SUCCESS, RefundTransactionEntity.SYSTEMERROR, new String[] {hmTransactionOrderCont.get(RefundTransactionEntity.AGENT_ID), hmTransactionOrderCont.get(RefundTransactionEntity.OUT_TRADE_NO), 
//																	hmTransactionOrderCont.get(RefundTransactionEntity.FEE_TYPE), hmTransactionOrderCont.get(RefundTransactionEntity.TOTAL_FEE),
//																	CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss"), ""});
							}
							
							String strReturnCode = hmWXRespResult.get(RefundTransactionEntity.RETURN_CODE);
							String strResultCode = hmWXRespResult.get(RefundTransactionEntity.RESULT_CODE);
							if (strReturnCode != null && strResultCode != null 
									&& strReturnCode.equals(RefundTransactionEntity.SUCCESS) && strResultCode.equals(RefundTransactionEntity.SUCCESS)) {
								strResp = RefundTransactionEntity.SUCCESS;
							}
							
							response.setContentType("text/xml;charset=UTF-8");
						    response.setHeader("Cache-Control","no-cache");
						    StringBuffer sbRsp = new StringBuffer();
						    sbRsp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
						    sbRsp.append("<RefundResult>");
						    sbRsp.append(strResp);
						    sbRsp.append("</RefundResult>");
						    
						    System.out.println("sbRsp = " + sbRsp.toString());
						    
						    pw = response.getWriter();
						    pw.write(sbRsp.toString());
						    pw.flush();
						}
					}
				}
			}
			
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				MysqlConnectionPool.getInstance().releaseConnection(conn);
			}
			
			if (preStat != null) {
				try {
					preStat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		this.doGet(request, response);
	}
}
