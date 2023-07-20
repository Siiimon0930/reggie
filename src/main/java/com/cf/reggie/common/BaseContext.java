package com.cf.reggie.common;

/*
* TODO：使用ThreadLocal来存储id，使得自动填充handler可获取到id
* */

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * decription: 设置id
     * @param id
     * @return void
     */
    public static void setId(Long id){
        threadLocal.set(id);
    }

    /**
     * decription: 获取id
     *
     * @return java.lang.Long
     */
    public static Long getId(){
        return threadLocal.get();
    }
}
