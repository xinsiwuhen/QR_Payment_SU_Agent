<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%><%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!--style type="text/css">
	.table-a table{border:1px solid #2894FF}
</style-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta charset="UTF-8"/>
<base href="<%=basePath%>">
<title></title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="Transaction report for Wechat international version ...">
</head>

<body>
	<!-- form class="idealforms" style="background-color: #C4E1FF; "-->
			<div id="divInqRst"  class="table-a" align="center">
				<table id="inquiedResultTbl" style="font-family:'微软雅黑';  font-size: 15px;" width="100%" border="0" cellspacing="1" cellpadding="0">
					<!--tr bgcolor="#C4E1FF" align="center">
						<td>序号</td>
						<td>商户号</td>
						<td>子商户号</td>
						<td>订单类型</td>
						<td>订单状态</td>
						<td>商户订单号</td>
						<td>微信订单号</td>
						<td>交易结束时间</td>
						<td>总金额</td>
						<td>标价币种</td>
						<td>汇率</td>
						<td>操作</td>
					</tr-->
				</table>
			</div>
		</table>
	<!--/form-->
</body>

<script type="text/javascript">
	sendInquiryRequest();
</script>
</html>
