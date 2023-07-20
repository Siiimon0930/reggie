package com.cf.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cf.reggie.common.CustomException;
import com.cf.reggie.dto.SetmealDto;
import com.cf.reggie.entity.Dish;
import com.cf.reggie.entity.DishFlavor;
import com.cf.reggie.entity.Setmeal;
import com.cf.reggie.entity.SetmealDish;
import com.cf.reggie.mapper.DishMapper;
import com.cf.reggie.mapper.SetmealMapper;
import com.cf.reggie.service.DishService;
import com.cf.reggie.service.SetmealDishService;
import com.cf.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * decription: 新增套餐
     * @param setmealDto
     * @return void
     */
    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // setmeal表插入
        this.save(setmealDto);
        // setmeal_dish 对象设置 setmealId 属性
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        // setmeal_dish表插入
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * decription: 删除套餐 批量 或 单个
     * @param ids
     * @return void
     */
    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        // 获得这批套餐 在售 的数量
        LambdaQueryWrapper<Setmeal> queryWrapper0 = new LambdaQueryWrapper<>();
        queryWrapper0.in(Setmeal::getId, ids);
        queryWrapper0.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper0);

        if (count > 0){
            // 如果有 在售 套餐，抛出异常
            throw new CustomException("套餐正在售卖，不可删除");
        }

        // 删除菜品
        this.removeByIds(ids);
        // 删除setmealDish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);
    }

    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDto setmealDto) {
        // 先更新setmeal
        this.updateById(setmealDto);
        // 在setmeal_dish中删除旧dish
        Long setmealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
        setmealDishService.remove(queryWrapper);
        // 再插入新dish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
