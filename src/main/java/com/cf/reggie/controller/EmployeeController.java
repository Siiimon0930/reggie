package com.cf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cf.reggie.common.R;
import com.cf.reggie.entity.Employee;
import com.cf.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * decription: 员工登录
     * @param request
     * @param employee
     * @return com.cf.reggie.common.R<com.cf.reggie.entity.Employee>
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // 1.将页面提交的密码password进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper); // 这里使用getOne是因为数据库已设计了username唯一

        // 3.判读是否查询结果emp是否为空，空则返回错误信息
        if (emp == null) {
            return R.error("用户名不存在");
        }

        // 4.比对密码，密码错误返回错误信息
        if(!emp.getPassword().equals(password)){
            // 这里emp.password是从数据库里取出来的加密后的，不可与employee.password计较
            return R.error("密码错误");
        }

        // 5.判断用户是否被禁用，禁用则返回错误信息
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        // 6.登录成功，在Session中保存用户id，并返回登录成功信息
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * decription: 员工登出
     * @param request
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * decription: 新增员工
     * @param request
     * @param employee
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping()
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        // 1.设置初始密码，123456 md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 2.save入数据库
        employeeService.save(employee);
        return R.success("保存成功");
    }

    /**
     * decription: 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return com.cf.reggie.common.R<com.baomidou.mybatisplus.extension.plugins.pagination.Page>
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page:{}, pageSize:{}, name:{}", page, pageSize, name);
        // 构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 按更新时间降序排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * decription: 更新员工
     * @param request
     * @param employee
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info("update: {}", employee);
        // 执行更新
        employeeService.updateById(employee);

        return R.success("操作成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getByid(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee != null)
            return R.success(employee);
        return R.error("没有查到对应信息");
    }
}
