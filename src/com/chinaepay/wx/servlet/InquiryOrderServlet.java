/**
 * @author xinwuhen
 */
package com.chinaepay.wx.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.entity.InquiryEntity;

/**
 * @author xinwuhen
 *
 */
public class InquiryOrderServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter pw = null;
		try {
			request.setCharacterEncoding("UTF-8");
			String mch_id = CommonTool.urlDecodeUTF8(request.getParameter("mch_id"));
			String sub_mch_id = CommonTool.urlDecodeUTF8(request.getParameter("sub_mch_id"));
			String orderType = CommonTool.urlDecodeUTF8(request.getParameter("orderType"));
			String orderStat = CommonTool.urlDecodeUTF8(request.getParameter("orderStat"));
			String transStartTime = CommonTool.urlDecodeUTF8(request.getParameter("transStartTime"));
			String transEndTime = CommonTool.urlDecodeUTF8(request.getParameter("transEndTime"));
//			System.out.println("mch_id = " + mch_id);
//			System.out.println("sub_mch_id = " + sub_mch_id);
//			System.out.println("orderType = " + orderType);
//			System.out.println("orderStat = " + orderStat);
//			System.out.println("transStartTime = " + transStartTime);
//			System.out.println("transEndTime = " + transEndTime);
			
			response.setContentType("text/xml;charset=UTF-8");
		    response.setHeader("Cache-Control","no-cache");
		    List<Map<String, String>> listInquiryRst = getOrderInquiryResult(mch_id, sub_mch_id, orderType, orderStat, transStartTime, transEndTime);
		    String strRespXML = getResponseXML(listInquiryRst);
		    //System.out.println(strRespXML);
		    pw = response.getWriter();
		    pw.write(strRespXML);
		    pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		this.doGet(request, response);
	}
	
	/**
	 * 将数据库查询到的订单信息封装为应答字符串。
	 * @param listInquiryRst
	 * @return
	 */
	private String getResponseXML(List<Map<String, String>> listInquiryRst) {
		if (listInquiryRst == null || listInquiryRst.size() == 0) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	    sb.append("<InquiryResult>");
	    for (Map<String, String> mapRow : listInquiryRst) {
	    	sb.append("<record>");
	    	
	    	String mpMch_id = mapRow.get("mch_id");
	    	sb.append("<mch_id>" + (mpMch_id == null || "".equals(mpMch_id) ? "-" : mpMch_id) + "</mch_id>");
	    	
	    	String mpSub_mch_id = mapRow.get("sub_mch_id");
	    	sb.append("<sub_mch_id>" + (mpSub_mch_id == null || "".equals(mpSub_mch_id) ? "-" : mpSub_mch_id) + "</sub_mch_id>");
	    	
	    	String strOdTp = mapRow.get("orderType");
	    	String strOdTpShow = "";
	    	switch(strOdTp) {
	    		case "1": strOdTpShow = "支付单"; break;
	    		case "2": strOdTpShow = "退款单"; break;
	    		default: strOdTpShow = "-"; break;
	    	}
	    	sb.append("<orderType>" + strOdTpShow + "</orderType>");
	    	
	    	String strTdSt = mapRow.get("trade_state");
	    	String strTdStRst = "";
	    	switch(strTdSt) {
	    		case "SUCCESS": strTdStRst = "支付成功"; break;
	    		case "REFUND": strTdStRst = "转入退款"; break;
	    		case "NOTPAY": strTdStRst = "未支付"; break;
	    		case "CLOSED": strTdStRst = "已关闭"; break;
	    		case "REVOKED": strTdStRst = "已撤销"; break;
	    		case "USERPAYING": strTdStRst = "支付中"; break;
	    		case "PAYERROR": strTdStRst = "支付失败"; break;
	    		default: strTdStRst = "系统错误"; break;
	    	}
	    	sb.append("<trade_state>" + strTdStRst + "</trade_state>");
	    	
	    	String strOtnShow = mapRow.get("out_trade_no");
	    	sb.append("<out_trade_no>" + (strOtnShow == null || "".equals(strOtnShow) ? "-" : strOtnShow) + "</out_trade_no>");
	    	
	    	String strTiShow = mapRow.get("transaction_id");
	    	sb.append("<transaction_id>" + (strTiShow == null || "".equals(strTiShow) ? "-" : strTiShow) + "</transaction_id>");
	    	
	    	String strTtShow = mapRow.get("trans_time");
	    	if (strTtShow != null && strTtShow.length() == 14) {
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	    		Date date = null;
				try {
					date = sdf.parse(strTtShow);
					strTtShow = CommonTool.getFormatDate(date, "yyyy-MM-dd HH:mm:ss");
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    	}
	    	sb.append("<trans_time>" + (strTtShow == null || "".equals(strTtShow) ? "-" : strTtShow) + "</trans_time>");
	    	
	    	String strTfShow = mapRow.get("total_fee");
	    	sb.append("<total_fee>" + (strTfShow == null || "".equals(strTfShow) ? "0" : CommonTool.formatNumToDoublePoints(Double.parseDouble(strTfShow)/100)) + "</total_fee>");
	    	
	    	String strFtShow = mapRow.get("fee_type");
	    	sb.append("<fee_type>" + (strFtShow == null || "".equals(strFtShow) ? "USD" : strFtShow) + "</fee_type>");
	    	
	    	String strRateShow = mapRow.get("rate");
	    	sb.append("<rate>" + (strRateShow == null || "".equals(strRateShow) ? "-" : strRateShow) + "</rate>");
	    	
	    	sb.append("</record>");
	    }
	    sb.append("</InquiryResult>");
	    
	    return sb.toString();
	}
	
	/**
	 * 依据查询条件查询订单记录。
	 * @param mch_id
	 * @param sub_mch_id
	 * @param orderType
	 * @param orderStat
	 * @param transStartTime
	 * @param transEndTime
	 * @return
	 */
	public List<Map<String, String>> getOrderInquiryResult(String mch_id, String sub_mch_id, String orderType, String orderStat, String transStartTime, String transEndTime) {
		
		List<Map<String, String>> lstInquiryRst = new ArrayList<Map<String, String>>();
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		StringBuffer sb = null;
		if (orderType == null || orderType.equals("") || orderType.equals("NOSELECT") 
				|| (!orderType.equals("1") && !orderType.equals("2") )) {	// 所有订单类型全部查询
			// 支付类订单
			List<Map<String, String>> lstTrans = getOrderInquiryResult(mch_id, sub_mch_id, "1" /*1为支付类订单*/, orderStat, transStartTime, transEndTime);
			
			
			// 退款类订单
			List<Map<String, String>> lstRefund = new ArrayList<Map<String, String>>();
			// ... ...
			// ... ...
			
			for (Map<String, String> mpRow : lstRefund) {
				lstTrans.add(mpRow);
			}
			
			// 合并以上类型的订单查询结果
			
			lstInquiryRst = lstTrans;
		} else if (orderType.equals("1")) {	// 支付类订单
			sb = new StringBuffer();
			sb.append("select tmito.mch_id, tmito.sub_mch_id, tto.trade_state, tto.out_trade_no, tto.transaction_id, tto.trans_time, tto.total_fee, tto.fee_type, tto.rate from tbl_mch_info_trans_order tmito, tbl_trans_order tto where 1=1 ");
			if (mch_id !=null && !mch_id.equals("")) {
				sb.append(" and tmito.mch_id='" + mch_id + "'");
			}
			
			if (sub_mch_id !=null && !sub_mch_id.equals("")) {
				sb.append(" and tmito.sub_mch_id='" + sub_mch_id + "'");
			}
			
			if (orderStat != null && !orderStat.equals("") && !orderStat.equals("NOSELECT")) {
				switch(orderStat) {
					case InquiryEntity.SUCCESS:
					case InquiryEntity.REFUND:
					case InquiryEntity.NOTPAY:
					case InquiryEntity.CLOSED:
					case InquiryEntity.REVOKED:
					case InquiryEntity.USERPAYING:
					case InquiryEntity.PAYERROR:
						sb.append(" and tto.trade_state='" + orderStat + "'");
						break;
				
					case InquiryEntity.SYSTEMERROR: 
					default: 
						sb.append(" and tto.trade_state<>'" + InquiryEntity.SUCCESS + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.REFUND + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.NOTPAY + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.CLOSED + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.REVOKED + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.USERPAYING + "'");
						sb.append(" and tto.trade_state<>'" + InquiryEntity.PAYERROR + "'");
						break;
				}
			}
			
			if (transStartTime !=null && !transStartTime.equals("")) {
				sb.append(" and tto.trans_time >= DATE_FORMAT('" + transStartTime + "', '%Y%m%d%H%i%S')");
			}
			
			if (transEndTime !=null && !transEndTime.equals("")) {
				sb.append(" and tto.trans_time <= DATE_FORMAT('" + transEndTime + "', '%Y%m%d%H%i%S')");
			}
			
			sb.append(" and tmito.out_trade_no = tto.out_trade_no order by tto.trans_time desc;");
			
			// 获取查询结果集
			try {
				conn = MysqlConnectionPool.getInstance().getConnection(true);
				preStat = conn.prepareStatement(sb.toString());
				
				System.out.println(sb.toString());
				
				rs = preStat.executeQuery();
				if (rs != null) {
					while(rs.next()) {
						Map<String, String> mapInquiryRst = new HashMap<String, String>();
						mapInquiryRst.put("mch_id", rs.getString("tmito.mch_id"));
						mapInquiryRst.put("sub_mch_id", rs.getString("tmito.sub_mch_id"));
						mapInquiryRst.put("orderType", "1");	// 订单类型为：支付单
						mapInquiryRst.put("trade_state", rs.getString("tto.trade_state"));
						mapInquiryRst.put("out_trade_no", rs.getString("tto.out_trade_no"));
						mapInquiryRst.put("transaction_id", rs.getString("tto.transaction_id"));
						mapInquiryRst.put("trans_time", rs.getString("tto.trans_time"));
						mapInquiryRst.put("total_fee", rs.getString("tto.total_fee"));
						mapInquiryRst.put("fee_type", rs.getString("tto.fee_type"));
						mapInquiryRst.put("rate", rs.getString("tto.rate"));
						lstInquiryRst.add(mapInquiryRst);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
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
		} else {	// 退款类订单
			// ... ...
			// ... ...
		}
		
		return lstInquiryRst;
	}
}
