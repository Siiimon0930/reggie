package com.cf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cf.reggie.common.R;
import com.cf.reggie.dto.DishDto;
import com.cf.reggie.dto.SetmealDto;
import com.cf.reggie.entity.Category;
import com.cf.reggie.entity.Dish;
import com.cf.reggie.entity.Setmeal;
import com.cf.reggie.entity.SetmealDish;
import com.cf.reggie.service.CategoryService;
import com.cf.reggie.service.SetmealDishService;
import com.cf.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * decription: 添加套餐
     * @param setmealDto
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) // 添加套餐时删除全部套餐缓存
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("添加套餐成功");
    }

    /**
     * decription: 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return com.cf.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> pageSetmeal = new Page<>(page, pageSize);
        // 最终返回Page<DishDto>
        Page<SetmealDto> pageSetmealDto = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 条件构造器
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        // 排序构造器
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 查询
        setmealService.page(pageSetmeal, queryWrapper);

        // 拷贝
        BeanUtils.copyProperties(pageSetmeal, pageSetmealDto, "records");
        List<SetmealDto> recordsDto = new ArrayList<>();
        List<Setmeal> recordsSetmeal = pageSetmeal.getRecords();
        recordsDto = recordsSetmeal.stream().map(item -> {
            // 先把dish中的属性拷贝到dishDto
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            // 查询分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            // 设置dishDto分类名称属性
            if(category != null)
                setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        pageSetmealDto.setRecords(recordsDto);

        return R.success(pageSetmealDto);
    }

    /**
     * decription: 批量删除 或 单个删除
     * @param ids
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @DeleteMapping()
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        setmealService.deleteWithFlavor(ids);
        return R.success("删除菜品成功");
    }

    /**
     * decription: 修改套餐销售状态，批量修改 或 单个修改
     * @param status
     * @param ids
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> updateStatus(@PathVariable int status, @RequestParam("ids") List<Long> ids){
        // 设置一个dish对象接收status
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        // 设置条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        setmealService.update(setmeal, queryWrapper);
        return R.success("修改成功");
    }

    /**
     * decription: 根据id查询套餐
     * @param id
     * @return com.cf.reggie.common.R<com.cf.reggie.dto.SetmealDto>
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        // 查询套餐
        SetmealDto setmealDto = new SetmealDto();
        Setmeal setmeal = setmealService.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 查询套餐关联的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);
    }

    /**
     * decription: 修改套餐
     * @param setmealDto
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithSetmealDish(setmealDto);
        return R.success("修改套餐成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId")
    public R<List<Setmeal>> list (Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus, setmeal.getStatus());
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

}
