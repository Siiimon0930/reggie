package com.cf.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cf.reggie.dto.DishDto;
import com.cf.reggie.entity.Dish;
import com.cf.reggie.entity.DishFlavor;
import com.cf.reggie.mapper.DishMapper;
import com.cf.reggie.service.DishFlavorService;
import com.cf.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * decription: 新增菜品，同时新增口味
     * @param dishDto
     * @return void
     */
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 1.存入dish，因为DishDto继承自Dish，所以可以直接用
        this.save(dishDto);
        // 2.获得dishFlavors，并设置dishID
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        // 3.存入flavors
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        // 查询dish
        Dish dish = this.getById(id);

        // 查询dishFlavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        // 拷贝属性到dishDto
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 首先修改dish表
        this.updateById(dishDto);

        // 修改dishFlavor的方式是先删除之前的flavor，而后插入新的flavor
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(queryWrapper);

        // 插入新的flavor
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * decription: 删除，批量或单个
     * @param ids
     * @return void
     */
    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        // 删除菜品
        this.removeByIds(ids);
        // 删除flavors
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);
    }
}
