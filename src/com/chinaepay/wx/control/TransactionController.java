/**
 * @author xinwuhen
 */
package com.chinaepay.wx.control;

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

import com.chinaepay.wx.common.CommonTool;
import com.chinaepay.wx.common.MysqlConnectionPool;
import com.chinaepay.wx.dao.TblDAO;
import com.chinaepay.wx.entity.TransactionEntity;
import com.chinaepay.wx.intf.TransactionIntf;

/**
 * @author xinwuhen
 *
 */
public abstract class TransactionController extends CommunicateController implements TransactionIntf {
	/**
	 * 开始进行业务交易的入口。
	 * @param hmTransactionOrderCont
	 * @return
	 */
//	public abstract HashMap<String, String> startTransactionOrder(HashMap<String, String> hmTransactionOrderCont);
	public abstract String startTransactionOrder(HashMap<String, String> hmTransactionOrderCont);
	
	/**
	 * 插入DAO内的数据到数据库表。
	 * @param tblDAO
	 * @param strTable
	 * @return
	 */
	public abstract boolean insertOrderInfoToTbl(Map<String, String> mapOrderInfo);
	
	/**
	 * 生成插入数据库表的SQL语句。
	 * @param tblDao
	 * @param strTbl
	 * @return
	 */
	public String getInsertSqlFromDAO(TblDAO tblDao, String strTbl) {
		if (tblDao == null || strTbl == null || strTbl.equals("")) {
			return null;
		}
		
		Class clazz = tblDao.getClass();
		Field[] flds = clazz.getDeclaredFields();
		if (flds == null || flds.length == 0) {
			return null;
		}
		
		StringBuffer sbPrefix = new StringBuffer();
		StringBuffer sbSuffix = new StringBuffer();
		int iFldsLen = flds.length;
		sbPrefix.append("insert into " + strTbl + " (");
		sbSuffix.append(" values(");
		for (int i = 0; i < iFldsLen; i++) {
			Field fld = flds[i];
			if (fld != null) {
				try {
					// 取得字段名字
					String strFldName = fld.getName();
					
					// 取得字段值
					String strGetMethodName = "get" + strFldName.substring(0, 1).toUpperCase() + strFldName.substring(1);
					Method getMethod = clazz.getDeclaredMethod(strGetMethodName);
					Object objFldRst = getMethod.invoke(tblDao);
					
					// 拼接字符串对象前半部分的SQL
					sbPrefix.append(strFldName);
					
					// 拼接字符串对象后半部分的SQL
					Class fldType = fld.getType();
					if (fldType == int.class) {	// int type
						sbSuffix.append(objFldRst == null ? "0" : (int) objFldRst);
					} else if (fldType == Integer.class) {	// Integer type
						sbSuffix.append(objFldRst == null ? "0" : ((Integer) objFldRst).toString());
					} else {	// default is String type
						sbSuffix.append((objFldRst == null || objFldRst.equals("")) ? "\'\'" : "\'" + (String) objFldRst + "\'");
					}
					
					if (i != iFldsLen - 1) {
						sbPrefix.append(",");
						sbSuffix.append(",");
					} else {
						sbPrefix.append(")");
						sbSuffix.append(")");
					}
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		// 添加整个SQL语句的结束符
		sbSuffix.append(";");
		
		return sbPrefix.toString() + sbSuffix.toString();
	}
}
