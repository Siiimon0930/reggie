package com.cf.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cf.reggie.dto.SetmealDto;
import com.cf.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 新增套餐，操作两张表：setmeal、setmeal_dish
    void saveWithSetmealDish(SetmealDto setmealDto);

    // 删除套餐，批量 或 单个
    void deleteWithFlavor(List<Long> ids);

    // 更新套餐，操作两张表：setmeal、setmeal_dish
    void updateWithSetmealDish(SetmealDto setmealDto);
}
