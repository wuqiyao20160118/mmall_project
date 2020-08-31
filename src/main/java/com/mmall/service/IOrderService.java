package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.math.BigDecimal;
import java.util.Map;

public interface IOrderService {

    // 创建订单接口
    ServerResponse createOrder(Integer userId, Integer shippingId);
    // 取消订单接口
    ServerResponse<String> cancel(Integer userId, Long orderNo);
    // 获取购物车已选中的商品详情接口
    ServerResponse getOrderCartProduct(Integer userId);
    // 查看订单详情接口
    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long OrderNo);
    // 个人中心查看订单接口
    ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);

    // 支付接口
    ServerResponse pay(Long orderNo, Integer userId, String path);
    // 验证回调正确性接口
    ServerResponse check(Long orderNo, BigDecimal totalAmount, String sellerId);
    // 回调接口
    ServerResponse aliCallback(Map<String, String> params);
    // 查询订单状态接口
    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);


    // 后台管理系统
    // 查看订单列表接口
    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);
    // 查看订单详情接口
    ServerResponse<OrderVo> manageDetail(Long orderNo);
    // 搜索订单接口，支持分页，目前为精确匹配
    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);
    // 订单发货接口
    ServerResponse<String> manageSendGoods(Long orderNo);
}
