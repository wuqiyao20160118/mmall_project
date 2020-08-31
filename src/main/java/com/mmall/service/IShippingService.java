package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

public interface IShippingService {

    // 增加收货地址接口
    ServerResponse add(Integer userId, Shipping shipping);
    // 删除收货地址接口
    ServerResponse<String> del(Integer userId, Integer shippingId);
    // 更新收货地址接口
    ServerResponse update(Integer userId, Shipping shipping);
    // 查找收获地址接口
    ServerResponse<Shipping> select(Integer userId, Integer shippingId);
    // 展示所有收货地址接口，提供分页功能
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
