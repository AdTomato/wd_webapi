package com.authine.cloudpivot.web.api.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.metadata.CellData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName CreditMonitor
 * @author: lfh
 * @Date:2020/7/1 15:44
 * @Description:
 **/
@Data
public class CreditMonitor extends BaseEntity {
    //日期
    @ExcelProperty(value = "日期",index = 0)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date credit_date;
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
