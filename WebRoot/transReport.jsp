<%@page import="com.chinaepay.wx.entity.RefundTransactionEntity"%>
<%@page language="java" import="javax.swing.text.Document,java.util.*,com.chinaepay.wx.common.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  	<meta charset="UTF-8"/>
    <base href="<%=basePath%>">
    
    <title>微信支付国际版交易记录...</title>
    
    <script src="js/jquery.1.7.2.min.js"></script>
    <!-- 以下为日历脚本 -->
    <script src="js/mobiscroll_002.js" type="text/javascript"></script>
	<script src="js/mobiscroll_004.js" type="text/javascript"></script>
	<link href="css/mobiscroll_002.css" rel="stylesheet" type="text/css">
	<link href="css/mobiscroll_003.css" rel="stylesheet" type="text/css">
	<link href="css/mobiscroll.css" rel="stylesheet" type="text/css">
	<script src="js/mobiscroll.js" type="text/javascript"></script>
	<script src="js/mobiscroll_003.js" type="text/javascript"></script>
	<script src="js/mobiscroll_005.js" type="text/javascript"></script>
	
	<!-- 以下为下拉框脚本 -->
	<!--script src="js/jquery-1.11.3.min.js"></script-->
	<script src="js/jquery.combo.select.js"></script>
	<link rel="stylesheet" href="css/combo.select.css">
	
	<!-- 以下为表单美化脚本 -->
	<link rel="stylesheet" href="css/jquery.idealforms.css">
	
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Transaction report for Wechat international version ...">
	
	<script type="text/javascript">
		var XMLHttpReq = null;  
        //创建XMLHttpRequest对象         
        function createXMLHttpRequest() {  
            if(window.XMLHttpRequest) { //Mozilla 浏览器  
                XMLHttpReq = new XMLHttpRequest();  
            }  
            else if (window.ActiveXObject) { // IE浏览器  
                try {  
                    XMLHttpReq = new ActiveXObject("Msxml2.XMLHTTP");  
                } catch (e) {  
                    try {  
                        XMLHttpReq = new ActiveXObject("Microsoft.XMLHTTP");  
                    } catch (e) {}  
                }  
            }  
        }
        
        // 发送查询请求  
        function sendInquiryRequest() {
            createXMLHttpRequest();  
            var url = getInquiryUrl("<%=basePath%>order/inqOrderSvlt");  
            XMLHttpReq.open("GET", encodeURI(encodeURI(url)), false);  
            XMLHttpReq.onreadystatechange = processInquiryResponse;//指定响应函数  
            XMLHttpReq.send(null);  // 发送请求  
        }  
        
        // 订单查询URL
        function getInquiryUrl(inquiryServlet) {
        	var mch_id = document.getElementById("mch_id").value;
			var sub_mch_id = document.getElementById("sub_mch_id").value;
			var orderType = document.getElementById("orderType").value;
			var orderStat = document.getElementById("orderStat").value;
			var transStartTime = document.getElementById("transStartTime").value;
			var transEndTime = document.getElementById("transEndTime").value;
			var URL = inquiryServlet + "?mch_id=" + mch_id 
										+ "&sub_mch_id=" + sub_mch_id
										+ "&orderType=" + orderType
										+ "&orderStat=" + orderStat
										+ "&transStartTime=" + transStartTime
										+ "&transEndTime=" + transEndTime;
										
			return URL;
        }
        
        // 处理返回信息函数  
        function processInquiryResponse() { 
            if (XMLHttpReq.readyState == 4) { // 判断对象状态  
                if (XMLHttpReq.status == 200) { // 信息已经成功返回，开始处理信息  
                    DisplayHot();  
                    //setTimeout("sendInquiryRequest()", 1000);  
                } else { //页面不正常  
                    window.alert("您所请求的页面有异常。");  
                }  
            }  
        }
        
        // 显示查询结果
        function DisplayHot() {
        	var inqRstTblEle = document.getElementById("inquiedResultTbl");
        	
        	// 清空原有表格内容
        	if (inqRstTblEle) {
        		inqRstTblEle.parentNode.removeChild(inqRstTblEle);
        	}
        	
         	// 获取服务端的查询结果
        	var altInfo = "无满足查询条件的记录!";
        	var respXML = XMLHttpReq.responseXML;
        	if (respXML == null) {
        		alert(altInfo);
        		return;
        	}
        	
        	var grpRecord = respXML.getElementsByTagName("record");
        	if (grpRecord == null || grpRecord.length == 0) {
       			alert(altInfo);
       			return;
       		}
       		
       		// 将数据显示到表格
        	var tbl = document.createElement("table");
        	tbl.setAttribute("id", "inquiedResultTbl");
        	tbl.setAttribute("style", "font-family:'微软雅黑';  font-size: 15px;");
        	tbl.setAttribute("width", "100%");
        	tbl.setAttribute("border", "0");
        	tbl.setAttribute("cellspacing", "1");
        	tbl.setAttribute("cellpadding", "0");
        	
        	var tblHead = document.createElement("tr");
        	tblHead.setAttribute("bgcolor", "#C4E1FF");
        	tblHead.setAttribute("align", "center");
        	var vHead = "<td>序号</td><td>商户号</td><td>子商户号</td><td>订单类型</td><td>订单状态</td><td>商户订单号</td><td>微信订单号</td><td>交易结束时间</td><td>金额($)</td><td>标价币种</td><td>汇率</td><td>操作</td>";
        	tblHead.innerHTML = vHead;
        	tbl.appendChild(tblHead);
        	
        	for (var i = 0; i < grpRecord.length; i++) {
        		var newRow = document.createElement("tr");
        		newRow.setAttribute("align", "center");
        		if (i % 2 == 0) {
        			newRow.setAttribute("bgcolor", "#F0F0F0");
        		}
        		
       			var firstClm = document.createElement("td");
       			firstClm.innerHTML = i + 1;
       			newRow.appendChild(firstClm)
       			
	        	//var clmNodes = grpRecord[i].children;
	        	var clmNodes = grpRecord[i].childNodes;
	        	var paymentSuccess = false;
	        	var outTradeNo = "";
        		for (var j = 0; j < clmNodes.length; j++) {
	        		var newClm = document.createElement("td");
	        		if (clmNodes[j].childNodes[0]) {
	        			var ndValue = clmNodes[j].firstChild.nodeValue;
		        		newClm.innerHTML = ndValue;
		        		
		        		var nodeName = clmNodes[j].nodeName;
		        		// 订单状态
		        		if (nodeName == "trade_state" && ndValue == "支付成功") {
		        			paymentSuccess = true;
		        			
		        		} else if (nodeName == "out_trade_no") {	// 商户订单号
		        			outTradeNo = ndValue;
		        		}
	        		} else {
	        			newClm.innerHTML = "-";
	        		}
	        		newRow.appendChild(newClm);
        		}
        		// 添加最后一列【操作】
        		var lastTd = document.createElement("td");
	        	if (paymentSuccess && outTradeNo != "") {	// 添加【退款】按钮
	        		var lastInput = document.createElement("input");
	        		lastInput.setAttribute("type", "button");
	        		lastInput.setAttribute("value", "退款");
	        		lastInput.setAttribute("onclick", "refundOrder('" + outTradeNo + "')");
	        		lastTd.appendChild(lastInput);
	        	} else {	// 添加“-”
					lastTd.innerHTML = "-";        	
	        	}
        		newRow.appendChild(lastTd);
	        	
	        	tbl.appendChild(newRow);
        	}
        	
			document.getElementById("divInqRst").appendChild(tbl);
        } 
        
        // 导出查询结果
        function exportInquiryRst() {
        	// 生成文件时所需要日期后缀
        	var date = new Date(); 
  			var year = date.getFullYear();
  			var month = date.getMonth() + 1;
  			var day = date.getDate();
			var hour = date.getHours();
			var minute = date.getMinutes();
			var second = date.getSeconds();
			
			String(month).length < 2 ? (month = "0" + month): month;
    		String(day).length < 2 ? (day = "0" + day): day;
    		String(hour).length < 2 ? (hour = "0" + hour): hour;
    		String(minute).length < 2 ? (minute = "0" + minute): minute;
    		String(second).length < 2 ? (second = "0" + second): second;
    		
    		var yyyyMMddhhmmSS = year + "" + month + "" + day + "" + hour + "" + minute + "" + second;
    		
    		// 生成文件名
    		var fileName = "OrderReport_" + yyyyMMddhhmmSS + ".zip"
    		
    		// 向服务器发送订单查询请求，并生成文件
    		createXMLHttpRequest();  
    		var exportPath = "export";
            var url = getInquiryUrl("<%=basePath%>order/expOrderSvlt") + "&exportPath=" + exportPath + "&exportFile=" + fileName;
            XMLHttpReq.open("GET", encodeURI(encodeURI(url)), false);  
            //XMLHttpReq.onreadystatechange = processInquiryResponse;//指定响应函数  
            XMLHttpReq.send(null);  // 发送请求
    		
    		// 下载文件
        	location.href = "<%=basePath%>/" + exportPath + "/" + fileName;
        }
        
        // 执行退款操作
        function refundOrder(outTradeNo) {
        	var url = "<%=basePath%>order/rfdOrderSvlt?out_trade_no=" + outTradeNo;
        	
        	createXMLHttpRequest();
        	XMLHttpReq.open("GET", encodeURI(encodeURI(url)), false);  
            XMLHttpReq.onreadystatechange = processRefundResponse;//指定响应函数  
            XMLHttpReq.send(null);  // 发送请求
        }
        
        // 处理退款操作对应的应答报文
        function processRefundResponse() {
        	if (XMLHttpReq.readyState == 4) { // 判断对象状态  
                if (XMLHttpReq.status == 200) { // 信息已经成功返回，开始处理信息  
                	var respXML = XMLHttpReq.responseXML;
              	  	var grpRecord = respXML.getElementsByTagName("RefundResult");
              	  	var refundErr = "退款申请失败，请重新操作！";
              	  	if (grpRecord == null || grpRecord.length == 0) {
		       			alert(refundErr);
		       			return;
		       		}
		       		
		       		var vRspVal = grpRecord[0].firstChild.nodeValue;
		       		if (vRspVal == "<%=RefundTransactionEntity.SUCCESS%>") {
		       			alert("退款申请已经授理！");
		       		} else {
		       			alert(refundErr);
		       		}
                } else { //页面不正常  
                    window.alert("您所请求的页面有异常。");  
                }  
            }
            
            // 重新进行页面信息查询
        	sendInquiryRequest();
        }
        
        $(function () {
			var currYear = (new Date()).getFullYear();	
			var opt={};
			opt.date = {preset : 'date'};
			opt.datetime = {preset : 'datetime'};
			opt.time = {preset : 'time'};
			opt.default = {
				theme: 'android-ics light', //皮肤样式
		        display: 'modal', //显示方式 
		        mode: 'scroller', //日期选择模式
				dateFormat: 'yyyy-mm-dd',
				lang: 'zh',
				showNow: true,
				nowText: "今天",
		        startYear: currYear - 10, //开始年份
		        endYear: currYear + 10 //结束年份
			};
			
		  	//$("#appDate").mobiscroll($.extend(opt['date'], opt['default']));
		  	var optDateTime = $.extend(opt['datetime'], opt['default']);
		  	var optTime = $.extend(opt['time'], opt['default']);
		    $("#transStartTime").mobiscroll(optDateTime).datetime(optDateTime);
		    $("#transEndTime").mobiscroll(optDateTime).datetime(optDateTime);
		    //$("#appTime").mobiscroll(optTime).time(optTime);
        });
	</script>
  </head>

  <body>
  	<table style="width: 97%;" align="center">
  		<tr valign="center">
  			<td>
				<!--form class="idealforms" style="background-color: #C4E1FF;" action="order/inqOrderSvlt" method="post"-->
				<!--form class="idealforms" style="background-color: #C4E1FF;" action="./inquiryResult.jsp" method="post"-->
				<form class="idealforms" style="background-color: #C4E1FF;" id="inquiryArgsFrm">
					<table align="center">
						<tr valign="center">
							<td align="right" width="13%"><label>商户号:</label></td>
							<td align="left" width="25%"><input id="mch_id"></input></td>
							<td align="right" width="18%"><label>子商户号:</label></td>
							<td align="left"width="30%"><input id="sub_mch_id"></input></td>
							<td align="right" width="13%"><label>订单类型:</label></td>
							<td align="left"><select id="orderType">
									<option value="NOSELECT">请选择...</option>
									<option value="1">支付单</option>
									<!--option value="2">退款单</option-->
									<!-->option value="3">撤销单</option-->
							</select></td>
						</tr>
						<tr valign="center">
							<td align="right" width="13%"><label>订单状态:</label></td>
							<td align="left" width="25%">
								<select id="orderStat">
									<option value="NOSELECT">请选择...</option>
									<option value="SUCCESS">支付成功</option>
									<option value="REFUND">转入退款</option>
									<option value="NOTPAY">未支付</option>
									<option value="CLOSED">已关闭</option>
									<option value="REVOKED">已撤销</option>
									<option value="USERPAYING">支付中</option>
									<option value="PAYERROR">支付失败</option>
									<option value="SYSTEMERROR">系统错误</option>
								</select></td>
							<td align="right" width="18%"><label>交易时间段:</label></td>
							<td align="left" width="30%"><input value="<%=CommonTool.getPreOrSuffFormatDate(new Date(), "YYYY-MM-dd 00:00", 0, -1, 0)%>" class="" readonly="readonly" name="transStartTime" id="transStartTime" type="text"></td>
							<td align="center" width="13%">～</td>
							<td align="left" ><input value="<%=CommonTool.getFormatDate(new Date(), "YYYY-MM-dd 23:59")%>" class="" readonly="readonly" name="transEndTime" id="transEndTime" type="text">
							</td>
						</tr>
					</table>
					</td>
			</tr>
			<tr valign="center" align="center">
				<td align="center" width="100%">
					<input type="button" value="查询" onclick="sendInquiryRequest()" style="width: 45px; height: 30px;">&nbsp;&nbsp;&nbsp;
					<input type="reset" value="重置" style="width: 45px; height: 30px;">&nbsp;&nbsp;&nbsp;
					<input type="button" value="导出" onclick="exportInquiryRst()" style="width: 45px; height: 30px;">
				</td>
			</tr>
		</form>
		<tr>
			<!--td align="right"><input type="button" value="导出" onclick="alert('导出报表')"></td-->
		</tr>
		<tr valign="center">
			<td>
				<jsp:include page="./inquiryResult.jsp" flush="true"></jsp:include>
			</td>
		</tr>
	</table>
	</body>
</html>
