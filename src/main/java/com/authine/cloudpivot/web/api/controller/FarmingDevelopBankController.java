package com.authine.cloudpivot.web.api.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.authine.cloudpivot.engine.api.model.organization.DepartmentModel;
import com.authine.cloudpivot.engine.api.model.organization.UserModel;
import com.authine.cloudpivot.web.api.controller.base.BaseController;
import com.authine.cloudpivot.web.api.entity.*;
import com.authine.cloudpivot.web.api.listener.CreditDailyAllBankListener;
import com.authine.cloudpivot.web.api.listener.CreditMonitorListener;
import com.authine.cloudpivot.web.api.listener.SaveMonitorListener;
import com.authine.cloudpivot.web.api.service.FarmingDevelopBankService;
import com.authine.cloudpivot.web.api.utils.ExcelUtils;
import com.authine.cloudpivot.web.api.utils.UserUtils;
import com.authine.cloudpivot.web.api.view.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Name;
import java.io.File;
import java.util.List;

/**
 * @ClassName FarmingDevelopBankController
 * @author: lfh
 * @Date:2020/7/1 15:18
 * @Description:
 **/
@RestController
@RequestMapping("/controller/farmingDevelopBank")
@Slf4j
public class FarmingDevelopBankController extends BaseController {

    @Autowired
    private FarmingDevelopBankService farmingDevelopBankService;

    @RequestMapping("/uploadSaveOrCredit")
    public ResponseResult<Object> uploadSaveOrCredit(@RequestBody List<ExcelData> excelData) {
        if (excelData == null || excelData.isEmpty()) {
            return this.getErrResponseResult("error", 100001L, "未上传文件");
        }
//        log.info("上传的文件名为：" + excelData.get(0).getFileName() + " ---" + excelData.get(1).getFileName());
        //fileName = "//sftp//sftpuser//upload" + fileName;//"存贷款表(分市).xlsx";
        UserModel user = this.getOrganizationFacade().getUser(UserUtils.getUserId(getUserId()));
        DepartmentModel department = this.getOrganizationFacade().getDepartment(user.getDepartmentId());
        ExcelReader excelReader = null;
        for (ExcelData excel : excelData) {
            String fileName = excel.getFileName();
            String refId = excel.getRefId();
            if (fileName == null || StringUtils.isBlank(fileName) || refId == null || StringUtils.isBlank(refId)) {
                return this.getErrResponseResult("error", 100001L, "上传的文件名为空");
            }
            if (!ExcelUtils.changeIsExcel(fileName)) {
                return this.getErrResponseResult("error", 100003L, "上传的文件格式错误");
            }
            fileName = new StrBuilder()
                    .append(File.separator)
                    .append("sftp").append(File.separator)
                    .append("sftpuser").append(File.separator)
                    .append("upload").append(File.separator)
                    .append(refId).append(fileName).toString();
            //fileName = "D://"+fileName;
            log.info("文件的名称为：{}", fileName);
            try {
                excelReader = EasyExcel.read(fileName).build();
                ReadSheet readSheet1 =
                        EasyExcel.readSheet("存款监测表").head(SaveMonitor.class).registerReadListener(new SaveMonitorListener(user, department, farmingDevelopBankService)).build();
                ReadSheet readSheet2 =
                        EasyExcel.readSheet("贷款监测表").head(CreditMonitor.class).registerReadListener(new CreditMonitorListener(user, department, farmingDevelopBankService)).build();
                ReadSheet readSheet3 =
                        EasyExcel.readSheet("贷款日报_全行").head(CreditDailyAllBank.class).registerReadListener(new CreditDailyAllBankListener(user, department, farmingDevelopBankService)).build();
                // 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
                excelReader.read(readSheet1, readSheet2, readSheet3);
            } catch (Exception e) {
                e.printStackTrace();
                return this.getErrResponseResult("error", 100000L, "数据存储失败");
            } finally {
                if (excelReader != null) {
                    // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
                    excelReader.finish();
                }
            }
        }
        return this.getOkResponseResult("success", "提交成功");
    }
}
