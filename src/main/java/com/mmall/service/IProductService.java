package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    // 新增或者更新产品接口
    ServerResponse saveOrUpdateProduct(Product product);
    // 更新产品上下架状态接口
    ServerResponse<String> setSaleStatus(Integer productId, Integer status);
    // 管理商品详情接口
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    // 后台获取商品列表接口，采用分页
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    // 商品搜索接口，采用分页
    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    // 前台商品详情接口
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    // 前台获取商品列表接口，采用分页
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy);
}
