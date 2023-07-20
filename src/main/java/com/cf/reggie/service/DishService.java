package com.cf.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cf.reggie.dto.DishDto;
import com.cf.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    // 存入菜品需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    // 根据id获得dish和dishFlavor
    public DishDto getByIdWithFlavor(Long id);

    // 修改菜品需要操作两张表：dish、dish_flavor
    void updateWithFlavor(DishDto dishDto);

    // 删除菜品同时删除flavor，批量删除 或 单个删除
    void deleteWithFlavor(List<Long> ids);
}
