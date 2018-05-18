/**
 * @author xinwuhen
 */
package com.chinaepay.wx.common;

/**
 * @author xinwuhen
 * 本类主要存储固定的配置参数信息，如：微信支付URL、JDBC驱动类名、公众账号ID等
 */
public class CommonInfo {
	/** Socket服务配置参数  **/
	// Socket服务端所打开的监听端口
	public static final int SERVER_SOCKET_PORT = 10086;
	// 为每一个Socket连接所设置的超时时间, 即：服务端在此时间后若读取不到客户端的信息，则会抛出例外java.net.SocketTimeoutException
	public static final int SERVER_SOCKET_TIME_OUT = 5000;
	// 同样，在客户端的Socket对象也需要设置同样的参数，以便确定在一定时间内未收到服务端的返回消息时，抛出同样的例外。
	public static final int CLIENT_SOCKET_TIME_OUT = 15000;
	
	/** Socket报文中用于标识处理何种业务的标识  **/
	// 支付交易
	public static final String PAYMENT_TRANSACTION_BIZ = "PaymentTransaction";
	// 撤销交易
	public static final String REVERSE_TRANSACTION_BIZ = "ReverseTransactoin";
	// 申请退款
	public static final String REFUND_TRANSACTION_BIZ = "RefundTransaction";
	// 查询交易
	public static final String INQUIRY_TRANSACTION_BIZ = "InquiryTransactionOrder";
	// 查询退款
	public static final String INQUIRY_REFUND_BIZ = "InquiryRefundOrder";
	
	// 易付通美国(Harvest)交易相关的基本信息(由腾讯分派)
	// 公众账号ID
	public static final String HARVEST_APP_ID = "wxb91c84b6c4d2e07b";
	// 商户号
	public static final String HARVEST_MCH_ID = "1900014621";
	// 密钥
	public static final String HARVEST_KEY = "024edfffae32c829b012c98a61686f3b";
	
	
	/** 数据库表名 **/
	// 商户信息表
	public static final String TBL_MCH_INFO = "tbl_mch_info";
	// 商户与交易单关联表
	public static final String TBL_MCH_INFO_TRANS_ORDER = "tbl_mch_info_trans_order";
	// 交易单信息表
	public static final String TBL_TRANS_ORDER = "tbl_trans_order";
	// 撤销单信息表
	public static final String TBL_TRANS_ORDER_REVERSE_ORDER = "tbl_trans_order_reverse_order";
	// 交易单与退款单关联表
	public static final String TBL_TRANS_ORDER_REFUND_ORDER = "tbl_trans_order_refund_order";
	// 退款单信息表
	public static final String TBL_REFUND_ORDER = "tbl_refund_order";
	
	/** 交易错误信息提示 **/
	// 未提交任何订单
	public static final String strNonAnyOrder = "0000";
	// 订单类型错误
	public static final String strOrderTypeErr = "0001";
	// 订单参数错误
	public static final String strOrderArgsErr = "0002";
	// 插入初始订单数据到数据库失败
	public static final String strInsertInitalOrderErr = "0003";
	// 微信端后台系统出现错误
	public static final String strWxBackendSystemErr = "0004";
	// 微信端返回应答信息为空
	public static final String strWxResponseInfoIsEmperty = "0005";
	// 解析微信端的返回应答信息错误
	public static final String strAnaylizeWxResponseErr = "0006";
	// 更新订单结果到数据库失败
	public static final String strUpdateOrderResultErr = "0007";
	// 返回商户的订单应答信息为空
	public static final String strResponseInfoIsEmpertyForCommercial = "0008";
}
