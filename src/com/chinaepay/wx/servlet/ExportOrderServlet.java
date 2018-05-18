/**
 * @author xinwuhen
 */
package com.chinaepay.wx.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chinaepay.wx.common.CommonTool;

public class ExportOrderServlet extends InquiryOrderServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("UTF-8");
			String mch_id = CommonTool.urlDecodeUTF8(request.getParameter("mch_id"));
			String sub_mch_id = CommonTool.urlDecodeUTF8(request.getParameter("sub_mch_id"));
			String orderType = CommonTool.urlDecodeUTF8(request.getParameter("orderType"));
			String orderStat = CommonTool.urlDecodeUTF8(request.getParameter("orderStat"));
			String transStartTime = CommonTool.urlDecodeUTF8(request.getParameter("transStartTime"));
			String transEndTime = CommonTool.urlDecodeUTF8(request.getParameter("transEndTime"));
			String exportPath = CommonTool.urlDecodeUTF8(request.getParameter("exportPath"));
			String exportFile = CommonTool.urlDecodeUTF8(request.getParameter("exportFile"));
		
			List<Map<String, String>> listInquiryRst = super.getOrderInquiryResult(mch_id, sub_mch_id, orderType, orderStat, transStartTime, transEndTime);
			
			// 生成导出文件
			this.generateExportFile(listInquiryRst, exportPath, exportFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doPosst(HttpServletRequest request, HttpServletResponse response) {
		this.doGet(request, response);
	}
	
	/**
	 * 根据数据库查询结果，生成供导出的文件，压缩后进行存储。
	 * @param listInquiryRst
	 * @param exportPath	压缩文件所存储的路径
	 * @param exportFileName	压缩文件名(*.zip)
	 * @return
	 * @throws Exception 
	 */
	private boolean generateExportFile(List<Map<String, String>> listInquiryRst, String exportPath, String exportFileName) throws SQLException {
		if (listInquiryRst == null) {
			throw new SQLException("无任何查询结果...");
		}
		
		if (exportPath == null || exportPath.equals("") || exportFileName == null || exportFileName.equals("")) {
			System.out.println("文件路径或文件名为空...");
			return false;
		}
		
        // 判断.txt文件是否包含"."字符
        if (!exportFileName.contains(".")) {
        	System.out.println("压缩文件名缺失'.'字符!");
			return false;
        }
        
        String SYSTEM_PATH_CHARACTOR = System.getProperty("file.separator");
        // 获取WebApp根目录，并依据不同的操作系统（Windows/Linux/Unix）格式化
        String strOSWebAppPath = CommonTool.getWebAppAbsolutPath(this.getClass(), SYSTEM_PATH_CHARACTOR);
        
        // 判断文件存储的目录是否存在
 		File zipDir = new File(strOSWebAppPath.concat(SYSTEM_PATH_CHARACTOR).concat(exportPath));  
		if (!zipDir.exists() || !zipDir.isDirectory()) {  
		     zipDir.mkdirs();
		}
        
		// 获取.txt与.zip文件存放的共同目录
		String strTxtAndZipFilePath = strOSWebAppPath.concat(SYSTEM_PATH_CHARACTOR).concat(exportPath);
		
        // 将数据写入.txt文件
		File txtFile = null;
		try {
			// 文件名(*.txt格式中字符“.”前边的部分)
	    	String strPrefixFileName = exportFileName.split("\\.")[0];
	    	String strTxtFileFullName = strTxtAndZipFilePath.concat(SYSTEM_PATH_CHARACTOR).concat(strPrefixFileName).concat(".txt");
			txtFile = getTxtFile(strTxtFileFullName, listInquiryRst);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
         
		// 生成.zip文件
		if (txtFile == null) {
			System.out.println("生成文本文件.txt为空...");
			return false;
		}
		
		String strZipFileFullName = strTxtAndZipFilePath.concat(SYSTEM_PATH_CHARACTOR).concat(exportFileName);
		File zipFile = new File(strZipFileFullName);
		boolean blnCastRst = castTxtToZipFile(txtFile, zipFile);
		
		// 删除旧的.txt与.zip文件
		this.deleteOldDirAndFiles(zipDir, txtFile, zipFile);
    	
		return blnCastRst;
	}
	
	
	
	/**
	 * 将查询结果数据写入到.txt文件。
	 * @param strOSWebAppPath
	 * @param strFileSeparator
	 * @param exportPath
	 * @param exportFile
	 * @param listInquiryRst
	 * @return
	 * @throws IOException 
	 */
	private File getTxtFile(String strTxtFileFullName, List<Map<String, String>> listInquiryRst) throws IOException {
		
    	File txtFile = new File(strTxtFileFullName);
        	
    	// 将数据写入.txt文件
    	FileOutputStream fos = null;
    	// 获取换行符，为了适应客户端绝大多数为Windows的情况，故文件换行符用Windows格式
    	String strEnterForWin = "\r\n";	// Linux换行符：\r   Mac换行符：\n
    	String strFirstLine = "序号|商户号|子商户号|订单类型|订单状态|商户订单号|微信订单号|交易结束时间|金额|标价币种|汇率" + strEnterForWin;
    	try {
    		fos = new FileOutputStream(txtFile);
			fos.write(strFirstLine.getBytes("UTF-8"));
			if (listInquiryRst != null) {
				for (int i = 0; i < listInquiryRst.size(); i++) {
					Map<String, String> mapRow = listInquiryRst.get(i);
		    		StringBuffer sb = new StringBuffer();
		    		sb.append(i + 1);
		    		sb.append("|");
		    		sb.append(mapRow.get("mch_id") == null ? "" : mapRow.get("mch_id"));
		    		sb.append("|");
		    		sb.append(mapRow.get("sub_mch_id") == null ? "" : mapRow.get("sub_mch_id"));
		    		sb.append("|");
		    		sb.append(mapRow.get("orderType") == null ? "" : mapRow.get("orderType"));
		    		sb.append("|");
		    		sb.append(mapRow.get("trade_state") == null ? "" : mapRow.get("trade_state"));
		    		sb.append("|");
		    		sb.append(mapRow.get("out_trade_no") == null ? "" : mapRow.get("out_trade_no"));
		    		sb.append("|");
		    		sb.append(mapRow.get("transaction_id") == null ? "" : mapRow.get("transaction_id"));
		    		sb.append("|");
		    		sb.append(mapRow.get("trans_time") == null ? "" : mapRow.get("trans_time"));
		    		sb.append("|");
		    		sb.append(mapRow.get("total_fee") == null ? "" : mapRow.get("total_fee"));
		    		sb.append("|");
		    		sb.append(mapRow.get("fee_type") == null ? "" : mapRow.get("fee_type"));
		    		sb.append("|");
		    		sb.append(mapRow.get("rate") == null ? "" : mapRow.get("rate"));
		    		// 写完一行后添加换行符
		    		sb.append(strEnterForWin);
		    		
					fos.write(sb.toString().getBytes("UTF-8"));
		    	}
			}
			
			// 文件内容写完后，添加文件结束标识符EOF
			fos.write("EOF".getBytes("UTF-8"));
			
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	
    	return txtFile;
	}
	
	/**
	 * 将.txt文件压缩成为.zip文件。
	 * @param txtFile
	 * @param strTxtAndZipFilePath
	 * @param exportFileName
	 * @return
	 * @throws IOException
	 */
	private boolean castTxtToZipFile(File txtFile, File zipFile) {
		if (txtFile == null || zipFile == null) {
			System.out.println("castTxtToZipFile方法输入参数为空");
			return false;
		}
		
		// 写出.txt文件到.zip文件
		int count = 0;
		byte data[] = new byte[1024];
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
        try {
        	// 定义输出文件的相关变量
        	fos = new FileOutputStream(zipFile);
//        	cos = new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32());  
        	zos = new ZipOutputStream(fos);  
        	zos.putNextEntry(new ZipEntry(txtFile.getName()));
        	bis = new BufferedInputStream(new FileInputStream(txtFile));
        	int bufferLen = data.length;
			while ((count = bis.read(data, 0, bufferLen)) != -1) {  
			    zos.write(data, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        
        return true;
	}
	
	/**
	 * 删除旧的.txt与.zip文件。
	 * @param zipDir
	 * @param txtFile
	 * @param zipFile
	 */
	private void deleteOldDirAndFiles(File zipDir, File txtFile, File zipFile) {
		if (zipDir == null || txtFile == null || zipFile == null) {
			return;
		}
		
		if (zipDir.exists() && zipDir.isDirectory()) {
			File[] lstFileAndDirect = zipDir.listFiles();
			if (lstFileAndDirect != null) {
				for (File file : lstFileAndDirect) {
					System.out.println(file);
					if (!(file.getName()).equals(txtFile.getName()) && !(file.getName()).equals(zipFile.getName())) {
						file.delete();
					}
				}
			}
		}
	}
}
