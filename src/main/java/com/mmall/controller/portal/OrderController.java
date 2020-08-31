package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.createOrder(user.getId(), shippingId);
    }

    @RequestMapping(value = "cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.cancel(user.getId(), orderNo);
    }

    @RequestMapping(value = "get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }

    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(orderNo, user.getId(), path);
    }

    @RequestMapping(value = "alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        // IMPORTANT：验证回调正确性，即验证回调是否为支付宝发送给服务器的，并且需要避免重复通知。
        // 第一步：在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数。
        // 第二步：将剩下参数进行url_decode，然后进行字典排序，组成字符串，得到代签名字字符串
        // 第三步：将签名参数（sign）使用base64解码为字节码串。
        // 第四步：使用RSA/RSA2的验签方法，通过签名字符串、签名参数（经过base64编码）及支付宝公钥验证签名。
        // 第五步：需要严格按照如下描述校验通知数据的正确性。
        // 1、商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号；
        // 2、判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额）；
        // 3、校验通知中的 seller_id（或者seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商户可能有多个 seller_id/seller_email）。
        // 上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
        // 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
        // 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
        //
        // 注意：
        // （1）状态 TRADE_SUCCESS 的通知触发条件是商户签约的产品支持退款功能的前提下，买家付款成功；
        // （2）交易状态 TRADE_FINISHED 的通知触发条件是商户签约的产品不支持退款功能的前提下，买家付款成功；
        // 或者，商户签约的产品支持退款功能的前提下，交易已经成功并且已经超过可退款期限。

        Map requestParams = request.getParameterMap();
        Map<String, String> params = Maps.newHashMap();
        // 支付宝发送的回调request的value为一个数组，故需要迭代器进行遍历
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String keyName = (String)iter.next();
            String[] values = (String[]) requestParams.get(keyName);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(keyName, valueStr);
        }
        logger.info("支付宝回调，sign:{}, trade_status:{}, 参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        // 使用RSA2的验签方法，通过签名字符串、签名参数（经过base64编码）及支付宝公钥验证签名
        // public static boolean rsaCheckV2(Map<String, String> params, String publicKey,
        //            String charset,String signType)
        // 注意：支付宝SDK中rsaCheckV2()只除去了sign参数，没有除去sign_type参数
        params.remove("sign_type");
        try {
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());

            if (!alipayRSACheckV2) {
                return ServerResponse.createByErrorMessage("非法请求，验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常", e);
        }

        // 校验通知数据的正确性
        Long outTradeNo = Long.parseLong(params.get("out_trade_no"));
        BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
        String sellerId = params.get("seller_id");
        if (!iOrderService.check(outTradeNo, totalAmount, sellerId).isSuccess()) {
            return ServerResponse.createByErrorMessage("非法请求，验证不通过");
        }

        // 注意：需要避免重复通知
        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        } else {
            return Const.AlipayCallback.RESPONSE_FAILED;
        }
    }

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if (serverResponse.isSuccess()) {
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
