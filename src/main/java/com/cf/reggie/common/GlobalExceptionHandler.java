package com.cf.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/*
* 全局异常处理
* */
// 表示这是一个全局异常处理器类，annotations 参数定义了控制器类型的注解
@ControllerAdvice(annotations = {RestController.class, Controller.class})
// 表示该类中的方法返回值将被直接写入响应体中，而不是被视图解析器解析为一个视图。
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * decription: 异常处理方法
     * @param e
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    // @ExceptionHandler 注解用于指定处理特定异常类型的方法。
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());

        if(e.getMessage().contains("Duplicate entry")){
            String[] split = e.getMessage().split(" ");
            String msg = "用户名"+ split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * decription: 自定义异常处理方法
     * @param e
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    // @ExceptionHandler 注解用于指定处理特定异常类型的方法。
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException e){
        log.error(e.getMessage());
        return R.error(e.getMessage());
    }
}
