package com.cf.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * decription: 插入操作，自动更新
     * @param metaObject
     * @return void
     */
    @Override
    public void insertFill(MetaObject metaObject) {
      log.info("公共字段处理insert...");
      log.info("元数据：{}",metaObject);
      metaObject.setValue("createTime", LocalDateTime.now());
      metaObject.setValue("updateTime", LocalDateTime.now());
      metaObject.setValue("createUser", BaseContext.getId());
      metaObject.setValue("updateUser", BaseContext.getId());
    }

    /**
     * decription: 更新操作，自动更新
     * @param metaObject
     * @return void
     */
    @Override
    public void updateFill(MetaObject metaObject) {
      log.info("公共字段处理update...");
      log.info("元数据：{}",metaObject);
      metaObject.setValue("updateTime", LocalDateTime.now());
      metaObject.setValue("updateUser", BaseContext.getId());
    }
}
