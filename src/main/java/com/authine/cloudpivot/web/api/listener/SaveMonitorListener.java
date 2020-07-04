package com.authine.cloudpivot.web.api.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.authine.cloudpivot.engine.api.model.organization.DepartmentModel;
import com.authine.cloudpivot.engine.api.model.organization.UserModel;
import com.authine.cloudpivot.web.api.constants.Constants;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.entity.SaveMonitor;
import com.authine.cloudpivot.web.api.service.FarmingDevelopBankService;
import com.authine.cloudpivot.web.api.utils.SystemDataSetUtils;
import com.esotericsoftware.minlog.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveMonitorListener
 * @author: lfh
 * @Date:2020/7/1 15:46
 * @Description:
 **/
public class SaveMonitorListener extends AnalysisEventListener<SaveMonitor> {
    private UserModel user;
    private DepartmentModel department;
    private FarmingDevelopBankService farmingDevelopBankService;
    private static final int BATCH_COUNT = 100;
    List<SaveMonitor> list = new ArrayList<SaveMonitor>();
    int sort = 1;
    public SaveMonitorListener(UserModel user, DepartmentModel department, FarmingDevelopBankService farmingDevelopBankService) {
        this.user = user;
        this.department = department;
        this.farmingDevelopBankService = farmingDevelopBankService;
    }

    @Override
    public void invoke(SaveMonitor saveMonitor, AnalysisContext analysisContext) {
        //将云枢系统字段set
        SystemDataSetUtils.dataSet(user, department, user.getName(), Constants.COMPLETED_STATUS, saveMonitor);
        saveMonitor.setSort_key(sort++);
        list.add(saveMonitor);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //插入之前查询本日是否已经导入
        try {
            SaveMonitor todayDate = farmingDevelopBankService.findTodayDate(list.get(0).getCreatedTime());
            if (null !=todayDate){
                //本日已经插入数据，删除本日数据
                farmingDevelopBankService.deleteTodaySaveDate(list.get(0).getCreatedTime());
            }
            Log.info("存储");
            farmingDevelopBankService.saveSaveMonitor(list);
        } catch (Exception e){
            e.printStackTrace();
            Log.info("插入失败");
        }

    }
}
