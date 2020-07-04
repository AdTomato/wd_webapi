package com.authine.cloudpivot.web.api.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

/**
 * @ClassName CreditDailyAllBank
 * @author: lfh
 * @Date:2020/7/2 9:39
 * @Description: 全行贷款日报实体类
 **/
@Data
public class CreditDailyAllBank extends BaseEntity {

    //类别
    @ExcelProperty(value = "类别",index = 0)
    private String type;
    //饼图项目简称
    @ExcelProperty(value = "饼图项目简称",index = 1)
    private String pie_chart_project_intro;
    //简称
    @ExcelProperty(value = "简称",index = 2)
    private String abbreviation;
    //项目名称
    @ExcelProperty(value = "项目名称",index = 3)
    private String project_name;
    //余额
    @ExcelProperty(value = "余额",index = 4)
    private BigDecimal balance;
    //比上日
    @ExcelProperty(value = "比上日",index = 5)
    private BigDecimal yesterday;
    //比上月
    @ExcelProperty(value = "比上月",index = 6)
    private BigDecimal last_month;
    //比上季
    @ExcelProperty(value = "比上季",index = 7)
    private BigDecimal last_quarter;
    //比年初
    @ExcelProperty(value = "比年初",index = 8)
    private BigDecimal begin_year;
    //比年初增量同比
    @ExcelProperty(value = "比年初增量同比",index = 9)
    private BigDecimal begin_year_increment;

    //年初至本日累放
    @ExcelProperty(value = "年初至本日累放",index = 10)
    private String begin_year_sum_release;

    //累放同比
    @ExcelProperty(value = "累放同比",index = 11)
    private String cumulative_release;

    //日期
    @ExcelProperty(value = "日期",index = 12)
    @DateTimeFormat("yyyy/MM/dd")
    private String date;

    private Integer sort_key;
}
