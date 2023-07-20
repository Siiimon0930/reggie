package com.cf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cf.reggie.common.R;
import com.cf.reggie.entity.Category;
import com.cf.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * decription: 分页查询
     * @param page
     * @param pageSize
     * @return com.cf.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        Page pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 按sort字段升序排列
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * decription: 新增分类
     * @param category
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("保存成功");
    }

    /**
     * decription: 删除分类
     * @param ids
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * decription: 更新分类
     * @param category
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("更新成功");
    }

    /**
     * decription: 返回分类列表
     * @param category
     * @return com.cf.reggie.common.R<java.util.List<com.cf.reggie.entity.Category>>
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 排序：先按序位后按更新时间
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);
        // 查询
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }

}
