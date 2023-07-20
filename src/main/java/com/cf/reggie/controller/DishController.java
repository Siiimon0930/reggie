package com.cf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cf.reggie.common.R;
import com.cf.reggie.dto.DishDto;
import com.cf.reggie.entity.Category;
import com.cf.reggie.entity.Dish;
import com.cf.reggie.entity.DishFlavor;
import com.cf.reggie.service.CategoryService;
import com.cf.reggie.service.DishFlavorService;
import com.cf.reggie.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * decription:  新增菜品
     * @param dishDto
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * decription: 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return com.cf.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Dish> pageDish = new Page<>(page, pageSize);
        // 最终返回Page<DishDto>
        Page<DishDto> pageDishDto = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 条件构造器
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        // 排序构造器
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 查询
        dishService.page(pageDish, queryWrapper);

        // 拷贝
        BeanUtils.copyProperties(pageDish, pageDishDto, "records");
        List<DishDto> recordsDto = new ArrayList<>();
        List<Dish> recordsDish = pageDish.getRecords();
        recordsDto = recordsDish.stream().map(item -> {
            // 先把dish中的属性拷贝到dishDto
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 查询分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            // 设置dishDto分类名称属性
            if(category != null)
                dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        pageDishDto.setRecords(recordsDto);

        return R.success(pageDishDto);
    }

    /**
     * decription: 根据id获取dish和dishFlavor
     * @param id
     * @return com.cf.reggie.common.R<com.cf.reggie.dto.DishDto>
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * decription:  修改菜品
     * @param dishDto
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * decription: 修改菜品销售状态，批量修改 或 单个修改
     * @param status
     * @param ids
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam("ids") List<Long> ids){
        // 设置一个dish对象接收status
        Dish dish = new Dish();
        dish.setStatus(status);
        // 设置条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        dishService.update(dish, queryWrapper);
        return R.success("修改成功");
    }

    /**
     * decription: 批量删除 或 单个删除
     * @param ids
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @DeleteMapping()
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        dishService.deleteWithFlavor(ids);
        return R.success("删除菜品成功");
    }


    /**
     * decription: 按条件查询菜品
     * @param dish
     * @return com.cf.reggie.common.R<java.util.List<com.cf.reggie.entity.Dish>>
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 菜系
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 销售状态
        queryWrapper.eq(Dish::getStatus, 1);
        // 排序
        queryWrapper.orderByAsc(Dish::getSort);
        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> listDto = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            Long dishId = item.getId();

            // 查询dishFlavor
            LambdaQueryWrapper<DishFlavor> queryWrapper0 = new LambdaQueryWrapper<>();
            queryWrapper0.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavors = dishFlavorService.list(queryWrapper0);

            // 拷贝属性到dishDto
            BeanUtils.copyProperties(item, dishDto);
            dishDto.setFlavors(flavors);

            return dishDto;
        }).collect(Collectors.toList());



        return R.success(listDto);
    }

}
