package com.cf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cf.reggie.common.BaseContext;
import com.cf.reggie.common.R;
import com.cf.reggie.entity.ShoppingCart;
import com.cf.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * decription: 新增购物车菜品或套餐
     * @param shoppingCart
     * @return com.cf.reggie.common.R<com.cf.reggie.entity.ShoppingCart>
     */
    @PostMapping("/add")
    public R<ShoppingCart> add (@RequestBody ShoppingCart shoppingCart){
        log.info(String.valueOf(shoppingCart));
        // 获取当前用户id
        Long userId = BaseContext.getId();
        // 填补userId
        shoppingCart.setUserId(userId);
        // 填补creat_time
        shoppingCart.setCreateTime(LocalDateTime.now());

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        if(shoppingCart.getDishId() != null){
            // 如果是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else {
            // 如果是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 查询当前数据库是否已有此条记录
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null){
            // 已有则数量number加1
            cartServiceOne.setNumber(cartServiceOne.getNumber() + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 无则插入新数据
            shoppingCartService.save(shoppingCart);
        }
        // 再次查询用于返回给前端
        cartServiceOne = shoppingCartService.getOne(queryWrapper);

        return R.success(cartServiceOne);
    }

    /**
     * decription: 减少购物车菜品或套餐
     * @param shoppingCart
     * @return com.cf.reggie.common.R<com.cf.reggie.entity.ShoppingCart>
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        // 获取用户id
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if(shoppingCart.getDishId() != null){
            // 如果是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else {
            // 如果是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 查询当前此条记录
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if(cartServiceOne.getNumber() > 1){
            // 如果数量大于1则number-1
            cartServiceOne.setNumber(cartServiceOne.getNumber() - 1);
            shoppingCartService.updateById(cartServiceOne);
            return R.success(cartServiceOne);
        }else {
            // 如果数量等于1则删除
            shoppingCartService.removeById(cartServiceOne);
            return R.success(new ShoppingCart());
        }

    }

    /**
     * decription: 返回用户购物车列表
     *
     * @return com.cf.reggie.common.R<java.util.List<com.cf.reggie.entity.ShoppingCart>>
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        // 获取用户id
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        // 获取用户id
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        // 删除全部
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
