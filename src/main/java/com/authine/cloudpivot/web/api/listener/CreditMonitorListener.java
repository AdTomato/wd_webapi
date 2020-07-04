package com.authine.cloudpivot.web.api.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.authine.cloudpivot.engine.api.model.organization.DepartmentModel;
import com.authine.cloudpivot.engine.api.model.organization.UserModel;
import com.authine.cloudpivot.web.api.constants.Constants;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.entity.SaveMonitor;
import com.authine.cloudpivot.web.api.service.FarmingDevelopBankService;
import com.authine.cloudpivot.web.api.service.impl.FarmingDevelopBankImpl;
import com.authine.cloudpivot.web.api.utils.SystemDataSetUtils;
import com.esotericsoftware.minlog.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CreditMonitorListener
 * @author: lfh
 * @Date:2020/7/1 15:46
 * @Description:
 **/
public class CreditMonitorListener extends AnalysisEventListener<CreditMonitor> {

    private UserModel user;
    private DepartmentModel department;
    private static final int BATCH_COUNT = 100;
    private FarmingDevelopBankService farmingDevelopBankService;
    List<CreditMonitor> list = new ArrayList<CreditMonitor>();
    int sort = 1;
    public CreditMonitorListener(UserModel user, DepartmentModel department, FarmingDevelopBankService farmingDevelopBankService) {
        this.user = user;
        this.department = department;
        this.farmingDevelopBankService = farmingDevelopBankService;
    }

    @Override
    public void invoke(CreditMonitor creditMonitor, AnalysisContext analysisContext) {
        SystemDataSetUtils.dataSet(user, department, user.getName(), Constants.COMPLETED_STATUS, creditMonitor);
        creditMonitor.setSort_key(sort++);
        list.add(creditMonitor);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try {
            CreditMonitor todayCreditDate = farmingDevelopBankService.findTodayCreditDate(list.get(0).getCreatedTime());
            if (null !=todayCreditDate){
                //本日已经插入数据，删除本日数据
                farmingDevelopBankService.deleteTodayCreditDate(list.get(0).getCreatedTime());
            }
            farmingDevelopBankService.saveCreditMonitor(list);
        }catch (Exception e ){
            e.printStackTrace();
            Log.info("插入失败");
        }

    }
}
