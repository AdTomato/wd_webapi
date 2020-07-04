package com.authine.cloudpivot.web.api.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName Save_monitor
 * @author: lfh
 * @Date:2020/7/1 14:57
 * @Description: 存款监测表
 **/
@Data
public class SaveMonitor  extends BaseEntity{
    //日期
    @ExcelProperty(value = "日期",index = 0)
    private Date save_money_date;
    //机构
    @ExcelProperty(value = "机构",index = 1)
    private String agency;
    //类型
    @ExcelProperty(value = "类型",index = 2)
    private String type;
    //本日余额
    @ExcelProperty(value = "本日余额",index = 3)
    private BigDecimal today_balance;
    //比前日
    @ExcelProperty(value = "比前日",index = 4)
    private BigDecimal before_yesterday;
    //比月初
    @ExcelProperty(value = "比月初",index = 5)
    private BigDecimal month_begin;
    //比年初
    @ExcelProperty(value = "比年初",index = 6)
    private BigDecimal year_begin;
    private Integer sort_key;
}
