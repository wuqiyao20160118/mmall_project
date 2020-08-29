package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {

    // 加入购物车接口
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    // 购物车更新商品数量接口
    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);
    // 购物车删除商品数量接口
    ServerResponse<CartVo> deleteProduct(Integer userId, String productIds);
    // 购物车查询商品接口
    ServerResponse<CartVo> list(Integer userId);
    // 购物车全选或全反选接口
    ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked);
    // 获取购物车内商品数量接口
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
