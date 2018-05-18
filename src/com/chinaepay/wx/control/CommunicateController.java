/**
 * @author xinwuhen
 */
package com.chinaepay.wx.control;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.chinaepay.wx.common.CommonInfo;
import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.dao.TblDAO;
import com.chinaepay.wx.entity.CommunicateEntity;
import com.chinaepay.wx.entity.PaymentTransactionEntity;
import com.chinaepay.wx.entity.TransactionEntity;
import com.chinaepay.wx.intf.CommunicateIntf;

/**
 * @author xinwuhen
 *
 */
public abstract class CommunicateController implements CommunicateIntf {
	
	public boolean blnValdOrderArgs(HashMap<String, String> hmOrderCont) {
		if (hmOrderCont == null || hmOrderCont.size() == 0) {
			System.out.println("错误码：" + CommonInfo.strNonAnyOrder + ", 未提交任何订单!");
			return false;
		}
		
		String[] strKeys = hmOrderCont.keySet().toArray(new String[0]);
		for (String strkey : strKeys) {
			String strValue = hmOrderCont.get(strkey);
			if (strkey == null || strkey.equals("") || strValue == null || strValue.equals("")) {
				System.out.println("错误码：" + CommonInfo.strOrderArgsErr);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 校验代理商是否存在，并且有效。
	 * @param strAgentId
	 * @return
	 */
	public boolean validateAgent(String strAgentId) {
		boolean blnValRst = false; 
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(true);
			String strInqirySql = "select * from tbl_agent_info where agent_id='" + strAgentId + "';";
			preStat = conn.prepareStatement(strInqirySql);
			rs = preStat.executeQuery();
			if (rs.next()) {
				String strAvalible = rs.getString("avalible");
				if (strAvalible != null && strAvalible.toLowerCase().equals("y")) {
					blnValRst = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
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
			
			if (conn != null) {
				MysqlConnectionPool.getInstance().releaseConnection(conn);
			}
		}
		
		return blnValRst;
	}
	
	/**
	 * 将代理商ID与终端商户ID进行关联。
	 * @param strAgentId
	 * @param strSubMerchId
	 */
	public void refferAgentAndSubMerchant(String strAgentId, String strSubMerchId) {
		Connection conn = null;
		PreparedStatement preStat = null;
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(false);
			String strInOrUpSql = "replace into tbl_agent_info_mch_info set agent_id='" + strAgentId + "', sub_mch_id='" + strSubMerchId + "';";
			preStat = conn.prepareStatement(strInOrUpSql);
			preStat.execute();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
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
	}
	
	/**
	 * 获取返回终端商户的应答信息。
	 * @param strOuttradNo
	 * @return
	 */
	public Map<String, String> getRespCommcialInfo(String strOuttradNo, String strTblName) {
		Map<String, String> mapRespCommInfo = new HashMap<String, String>();
		
		Connection conn = null;
		PreparedStatement preStat = null;
		ResultSet rs = null;
		
		try {
			conn = MysqlConnectionPool.getInstance().getConnection(true);
			String strInqirySql = "select * from " + strTblName + " where out_trade_no = '" + strOuttradNo + "';";
			System.out.println("strInqirySql = " + strInqirySql);
			preStat = conn.prepareStatement(strInqirySql);
			rs = preStat.executeQuery();
			if (rs.next()) {
				String strFeeType = strTblName.equals(CommonInfo.TBL_REFUND_ORDER) ? rs.getString("refund_fee_type") : rs.getString("fee_type");
				System.out.println("--->strFeeType = " + strFeeType);
				String strTotalFee = rs.getString("total_fee");
				String strTimeEnd = CommonTool.getFormatDate(new Date(), "yyyyMMddHHmmss");
				String strTransId = rs.getString("transaction_id");
				
				mapRespCommInfo.put(TransactionEntity.OUT_TRADE_NO, strOuttradNo);
				mapRespCommInfo.put(TransactionEntity.FEE_TYPE, strFeeType == null ? "" : strFeeType);
				mapRespCommInfo.put(TransactionEntity.TOTAL_FEE, strTotalFee == null ? "0" : strTotalFee);
				mapRespCommInfo.put(TransactionEntity.TIME_END, strTimeEnd == null ? "" : strTimeEnd);
				mapRespCommInfo.put(TransactionEntity.TRANSACTION_ID, strTransId == null ? "" : strTransId);
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
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
		
		return mapRespCommInfo;
	}

	public String sendReqAndGetResp(String strURL, HashMap<String, String> hmOrderCont, CloseableHttpClient httpclient) {
		/*
		// 发送XML文件数据到微信服务端
		URL url = null;
		HttpURLConnection httpURLConn = null;
		String strRespResult = "";
		try {
			url = new URL(strURL);
			httpURLConn = (HttpURLConnection) url.openConnection();
			httpURLConn.setDoOutput(true);
			httpURLConn.setRequestMethod("POST");
			OutputStream os = httpURLConn.getOutputStream();
			System.out.println("sending request XML to Wechat: " + formatToRequestXML(hmOrderCont));
			os.write(formatToRequestXML(hmOrderCont).getBytes("UTF-8"));
			os.flush();
			
			// 从微信服务端获取交易结果
			InputStream is = httpURLConn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				strRespResult = strRespResult.concat(strLine);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		*/
		String jsonStr = null;
		if (httpclient != null) {
			CloseableHttpResponse response = null;
			try {
				HttpPost httpost = new HttpPost(strURL); // 设置响应头信息
				httpost.addHeader("Connection", "keep-alive");
//				httpost.addHeader("Accept", "*/*");
//				httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//				httpost.addHeader("Host", "api.mch.weixin.qq.com");
//				httpost.addHeader("X-Requested-With", "XMLHttpRequest");
//				httpost.addHeader("Cache-Control", "max-age=0");
//				httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
				
//				// 克隆一个新的MAP容器及内容
//				Map<String, String> mapOrderContClone = (Map<String, String>) hmOrderCont.clone();
//				// 删除生成签名时无效的字段
//				mapOrderContClone.remove(CommunicateEntity.AGENT_ID);
				
				String strPaymentOrderSign = CommonTool.getEntitySign(hmOrderCont);
				hmOrderCont.put(CommunicateEntity.SIGN, strPaymentOrderSign);
				
				String strXMLRequest = formatToRequestXML(hmOrderCont);
				System.out.println("strXMLRequest = " + strXMLRequest);
				httpost.setEntity(new StringEntity(strXMLRequest, "UTF-8"));
				response = httpclient.execute(httpost);
				HttpEntity entity = response.getEntity();
				jsonStr = EntityUtils.toString(entity, "UTF-8");
				EntityUtils.consume(entity);
			} catch(IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (httpclient != null) {
					try {
						httpclient.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return jsonStr;
	}
	
	/**
	 * 生成返回给商户端的最终报文。
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 FAIL
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 * @param strBizProcResult
	 * @param strRespResult
	 * @return
	 */
	public HashMap<String, String> orgnizeResponseInfo(String strSysCommResult, String strBizProcResult, String[] strRespResult) {
		HashMap<String, String> hmReturnResult = new HashMap<String, String>();
		hmReturnResult.put(CommunicateEntity.SYSTEM_COMM_RESULT_KEY, strSysCommResult == null ? "" : strSysCommResult);
		hmReturnResult.put(CommunicateEntity.BUSINESS_PROC_RESULT_KEY, strBizProcResult == null ? "" : strBizProcResult);
		hmReturnResult.put(CommunicateEntity.BUSINESS_RESPONSE_RESULT, strRespResult == null ? "" : generateLastRespResult(strRespResult[0], strRespResult[1], 
							strRespResult[2], strRespResult[3], strRespResult[4], strRespResult[5]));
		return hmReturnResult;
	}
	
	/**
	 * 依据参数组装返回给商户端的报文。
	 * HashMap的格式：
	 * [key]: BUSINESS_PROC_RESULT 		[value]: SUCCESS 或 FAIL
	 * [key]: BUSINESS_RESPONSE_RESULT 	[value]: out_trade_no=1217752501201407033233368018&fee_type=USD&total_fee=888&time_end=20141030133525&transaction_id=013467007045764
	 * @param strProcResult
	 * @param strOutTradeNo
	 * @param strFeeType
	 * @param strTotalFee
	 * @param strTimeEnd
	 * @return
	 */
	private String generateLastRespResult(String strAgentId, String strOutTradeNo, String strFeeType, String strTotalFee, String strTimeEnd, String strTransactionId) {
		String strTemp = "";
		strTemp = strTemp.concat(TransactionEntity.AGENT_ID + "=" + (strAgentId != null ? strAgentId : ""));
		strTemp = strTemp.concat("&" + TransactionEntity.OUT_TRADE_NO + "=" + (strOutTradeNo != null ? strOutTradeNo : ""));
		strTemp = strTemp.concat("&" + TransactionEntity.FEE_TYPE + "=" + (strFeeType != null ? strFeeType : ""));
		strTemp = strTemp.concat("&" + TransactionEntity.TOTAL_FEE + "=" + (strTotalFee != null ? strTotalFee : ""));
		strTemp = strTemp.concat("&" + TransactionEntity.TIME_END + "=" + (strTimeEnd != null ? strTimeEnd : ""));
		strTemp = strTemp.concat("&" + TransactionEntity.TRANSACTION_ID + "=" + (strTransactionId != null ? strTransactionId : ""));
		return strTemp;
	}
	
	/**
	 * 在发送微信后台进行处理前，将订单实体格式化请求文件为XML格式。
	 * @param orderEntity
	 * @return
	 */
	private String formatToRequestXML(Map<String, String> hmOrderCont) {
		String strXML = "";
		
		String[] strKeys = hmOrderCont.keySet().toArray(new String[0]);
		if (strKeys.length > 0) {
			strXML = strXML.concat("<xml>");
			
			StringBuffer sb = new StringBuffer();
			for (String strKey : strKeys) {
				if (strKey != null && !"".equals(strKey) && !strKey.equals(CommunicateEntity.APP_KEY) && !strKey.equals(CommunicateEntity.AGENT_ID)) {
					String strValue = hmOrderCont.get(strKey);
					if (strValue != null && !"".equals(strValue)) {
						sb.setLength(0);
						sb.append("<").append(strKey).append(">").append(strValue).append("</").append(strKey).append(">");
						strXML = strXML.concat(sb.toString());
					}
				}
			}
			
			strXML = strXML.concat("</xml>");
		}
		
		return strXML;
	}
	
	/**
	 * 生成与数据库表交互的DAO类，并将MAP内的信息灌入此类的对象实例中。
	 * @param mapInfo
	 * @param daoClazz
	 * @return
	 */
	public TblDAO loadMapInfoToDAO(Map<String, String> mapInfo, Class<?> daoClazz) {
		if (mapInfo == null || mapInfo.size() == 0 || daoClazz == null) {
			return null;
		}
		
		TblDAO tblDAO = null;
		try {
			tblDAO = (TblDAO) daoClazz.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		if (tblDAO == null) {
			return null;
		}
		
		Field[] fields = daoClazz.getDeclaredFields();
		if (fields != null && fields.length != 0) {
			for (Field fld : fields) {
				String strFieldName = fld.getName();
				// 取得set方法的名字
				try {
					String strValue = mapInfo.get(strFieldName);
					if (strValue != null && !"".equals(strValue)) {
						String strSetMethodName = "set" + strFieldName.substring(0, 1).toUpperCase() + strFieldName.substring(1);
						Method daoMethod = daoClazz.getDeclaredMethod(strSetMethodName, fld.getType());
						Class clsType = fld.getType();
//						System.out.println("clsType = " + clsType);
						if (clsType == int.class) {	// int type.
							daoMethod.invoke(tblDAO, Integer.parseInt(strValue));
						} else if (clsType == Integer.class) {	// Integer type.
							daoMethod.invoke(tblDAO, Integer.valueOf(strValue));
						} else {	// Default is String type.
							daoMethod.invoke(tblDAO, strValue);
						}
					}
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// output the setting result.
//		Field[] flds = tblDAO.getClass().getDeclaredFields();
//		if (flds != null) {
//			for (Field field : flds) {
//				String strFieldName = field.getName();
//				String strGetMethodName = "get" + strFieldName.substring(0, 1).toUpperCase() + strFieldName.substring(1);
//				Method method = null;
//				try {
//					method = tblDAO.getClass().getDeclaredMethod(strGetMethodName);
//				} catch (NoSuchMethodException | SecurityException e1) {
//					e1.printStackTrace();
//				}
//				if (method != null) {
//					try {
//						Object strValue = (Object) method.invoke(tblDAO);
//						System.out.println(field.getName() + " 's value =  " + (strValue == null ? "" : strValue.toString()));
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					} catch (IllegalArgumentException e) {
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		
		return tblDAO;
	}
	
	/**
	 * 更新订单结果信息到数据库。
	 * @param mapOrderInfo
	 * @return
	 */
	public abstract boolean updateOrderInfoToTbl(HashMap<String, String> mapOrderInfo);
	
	
	/**
	 * 获取更新表的简单SQL语句，不包含where条件。
	 * @param tblDao
	 * @param strTbl
	 * @return
	 */
	public String getSimpleUpdateSqlFromDAO(TblDAO tblDao, String strTbl) {
		if (tblDao == null || strTbl == null || strTbl.equals("")) {
			return null;
		}

		Class clazz = tblDao.getClass();
		Field[] flds = clazz.getDeclaredFields();
		if (flds == null || flds.length == 0) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		String strPrefix = "update " + strTbl + " set ";
		sb.append(strPrefix);
		for (int i = 0; i < flds.length; i++) {
			Field fld = flds[i];
			if (fld != null) {
				String strFldName = fld.getName();
				if (strFldName != null && !"".equals(strFldName)) {
					String strGetMethodName = "get" + strFldName.substring(0, 1).toUpperCase()
							+ strFldName.substring(1);
					Method getMethod = null;
					try {
						getMethod = clazz.getDeclaredMethod(strGetMethodName);
						Object objValue = getMethod.invoke(tblDao);
						Class fldType = fld.getType();
						if (objValue != null) {
							if (fldType == int.class) { // int type
								if ((int) objValue != 0) {
									sb.append(strFldName + "=");
									sb.append((int) objValue);
									sb.append(",");
								}
							} else if (fldType == Integer.class) { // Integer type
								if (!((Integer) objValue).toString().equals("0")) {
									sb.append(strFldName + "=");
									sb.append(((Integer) objValue).toString());
									sb.append(",");
								}
							} else { // default is String type
								if (!objValue.equals("")) {
									sb.append(strFldName + "=");
									sb.append("\'" + (String) objValue + "\'");
									sb.append(",");
								}
							}
						}
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException 
							| IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}

		String strRst = null;
		String strTmp = sb.toString();
		int iStrLen = strTmp.length();
		if (strTmp.substring(iStrLen - 1).equals(",")) {
			strRst = strTmp.substring(0, iStrLen - 1);
		}
		
		// 如果SQL没有任何可以更新的字段，将StringBuffer内容清空
		System.out.println("strRst = " + strRst);
		System.out.println("strPrefix = " + strPrefix);
		if (strRst != null && strRst.equals(strPrefix)) {
			strRst = "";
		}

		return strRst;
	}
	
	/**
	 * 获取更新表的简单SQL语句，不包含where条件。但标识了需要更新哪些业务字段(参照lstFields参数).
	 * @param tblDao
	 * @param strTbl
	 * @return
	 */
	/*
	public String getSimpleUpdateSqlFromDAO(TblDAO tblDao, String strTbl, List<String> lstFields) {
		if (tblDao == null || strTbl == null || strTbl.equals("") || lstFields == null || lstFields.size() == 0) {
			return null;
		}
		
		Class clazz = tblDao.getClass();
		Field[] flds = clazz.getDeclaredFields();
		if (flds == null || flds.length == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("update " + strTbl + " set ");
		for (int i = 0; i < flds.length; i++) {
			Field fld = flds[i];
			if (fld != null) {
				int ind = -1;
				if ((ind = lstFields.indexOf(fld.getName())) != -1) {
					String strLstFld = lstFields.get(ind);
					if (strLstFld != null && !"".equals(strLstFld)) {
						
						String strGetMethodName = "get" + strLstFld.substring(0, 1).toUpperCase() + strLstFld.substring(1);
						Method getMethod = null;
						try {
							getMethod = clazz.getDeclaredMethod(strGetMethodName);
							Object objValue = getMethod.invoke(tblDao);
							Class fldType = fld.getType();
							if (objValue != null) {
								if (fldType == int.class) {	// int type
									if ((int) objValue != 0) {
										sb.append(strLstFld + "=");
										sb.append((int) objValue);
										sb.append(",");
									}
								} else if (fldType == Integer.class) {	// Integer type
									if (!((Integer) objValue).toString().equals("0")) {
										sb.append(strLstFld + "=");
										sb.append(((Integer) objValue).toString());
										sb.append(",");
									}
								} else {	// default is String type
									if (!objValue.equals("")) {
										sb.append(strLstFld + "=");
										sb.append("\'" + (String) objValue + "\'");
										sb.append(",");
									}
								}
							}
						} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		String strRst = null;
		String strTmp = sb.toString();
		int iStrLen = strTmp.length();
		if (strTmp.substring(iStrLen - 1).equals(",")) {
			strRst = strTmp.substring(0, iStrLen - 1);
		}
		
		return strRst;
	}
	*/
	
	/**
	 * 获取查询表信息的简单SQL语句，不包含where条件。
	 * @param tblDao
	 * @param strTbl
	 * @return
	 */
	public String getSimpleInquirySqlFromDAO(TblDAO tblDao, String strTbl) {
		if (tblDao == null || strTbl == null || strTbl.equals("")) {
			return null;
		}
		
		Class clazz = tblDao.getClass();
		Field[] flds = clazz.getDeclaredFields();
		if (flds == null || flds.length == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		String strFieldName = null;
		Field fld = null;
		int iFldsLen = flds.length;
		for (int i = 0; i < iFldsLen; i++) {
			fld = flds[i];
			if (fld != null) {
				strFieldName = fld.getName();
				sb.append(strFieldName);
				if (i != iFldsLen - 1) {
					sb.append(",");
				}
			}
		}
		sb.append(" from " + strTbl/* + ";"*/);
		
		return sb.toString();
	}
	
	/**
	 * 此为一个内部类，用于解析微信端后台返回的XML应答内容。
	 * @author xinwuhen
	 *
	 */
	public static class ParsingWXResponseXML {
		private HashMap<String, String> hmWXRespResult = new HashMap<String, String>();

		/**
		 * 解析XML并保存在Map中。
		 * @param strWxResponseResult
		 * @return
		 * @throws ParserConfigurationException 
		 * @throws IOException 
		 * @throws SAXException 
		 */
		public HashMap<String, String> getMapBaseWXRespResult(String strWxResponseResult) throws ParserConfigurationException, IOException, SAXException {
			if (strWxResponseResult == null || "".equals(strWxResponseResult)) {
				return null;
			}
			
			DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
			Document document = docBuilder.parse(new InputSource(new StringReader(strWxResponseResult)));
			
			// 解析XML格式的字符串，并将字符串的以【名-值】对的形式添加到MAP中。
			if (document != null) {
				appendElementNameAndValue(document);
			}
			
			return hmWXRespResult;
		}
		
		/**
		 * 取得元素节点的节点名跟节点值。
		 * @param node
		 * @return
		 */
		private void appendElementNameAndValue(Node node) {
			if (node != null) {  // 判断节点是否为空
				
				if (node.hasChildNodes()) {	// 本元素节点下还有子节点
					NodeList nodeList = node.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node childNode = nodeList.item(i);
						appendElementNameAndValue(childNode);
					}
				} else {	// 本元素节点下已经没有子节点
//					if (node.getNodeType() == node.CDATA_SECTION_NODE) {
						Node nodeParent = null;
						if ((nodeParent = node.getParentNode()) != null) {
							hmWXRespResult.put(nodeParent.getNodeName(), node.getNodeValue());
						}
//					}
				}
			}
		}
	}
}
