package com.cf.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cf.reggie.common.CustomException;
import com.cf.reggie.entity.Category;
import com.cf.reggie.entity.Dish;
import com.cf.reggie.entity.Employee;
import com.cf.reggie.entity.Setmeal;
import com.cf.reggie.mapper.CategoryMapper;
import com.cf.reggie.mapper.EmployeeMapper;
import com.cf.reggie.service.CategoryService;
import com.cf.reggie.service.DishService;
import com.cf.reggie.service.EmployeeService;
import com.cf.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * decription: 删除分类
     * @param id
     * @return void
     */
    @Override
    public void remove(Long id) {
        // 1.查询是否有 菜品 关联当前分类
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(queryWrapper1);

        if(count1 > 0){
            throw new CustomException("当前分类关联了菜品，不可删除");
        }

        // 2.查询是否有 套餐 关联当前分类
        LambdaQueryWrapper<Setmeal> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(queryWrapper2);

        if(count2 > 0){
            throw new CustomException("当前分类关联了菜品，不可删除");
        }

        super.removeById(id);
    }
}
