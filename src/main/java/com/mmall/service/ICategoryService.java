package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {

    // 增加目录接口
    ServerResponse addCategory(String categoryName, Integer parentId);
    // 更新品类名称接口
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
    // 平级分类查询接口
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);
    // 递归查询接口
    ServerResponse selectCategoryAndChildrenById(Integer categoryId);
}
