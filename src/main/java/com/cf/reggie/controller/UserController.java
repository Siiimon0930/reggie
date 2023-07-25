package com.cf.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cf.reggie.common.R;
import com.cf.reggie.entity.User;
import com.cf.reggie.service.UserService;
import com.cf.reggie.utils.SMSUtils;
import com.cf.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * decription: 发送验证码
     * @param map
     * @param session
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map map, HttpSession session){
        log.info("map: {}", map);
        // 获取电话号码
        String phone = (String) map.get("phone");
        // 生成随机验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code: {}", code);
        // 发送短信
        //SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909", phone, code);
        // 将验证码保存到session中
        //session.setAttribute(phone, code);
        // 将验证码保存在redis中，有效期五分钟
        redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
        return R.success("发送成功");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        // 获得请求中 手机号与验证码
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");
        // 获得session中的验证码
        //String sessionCode = (String) session.getAttribute(phone);
        // 获得redis中的验证码
        String RedisCode = (String) redisTemplate.opsForValue().get(phone);
        // 比对验证码
        if(StringUtils.isNotEmpty(RedisCode) && code.equals(RedisCode)){
            // 比对成功
            // 查询数据库有没有该用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(phone != null, User::getPhone, phone);
            User user = userService.getOne(queryWrapper);

            if(user == null){
                // 没有则新建该用户
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            // 把当前user的id存入session
            session.setAttribute("user", user.getId());
            // 登录成功后删除缓存中的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("验证码错误");
    }

}
