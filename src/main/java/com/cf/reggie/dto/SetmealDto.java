package com.cf.reggie.dto;

import com.cf.reggie.entity.Setmeal;
import com.cf.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
