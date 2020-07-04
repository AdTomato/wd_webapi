package com.authine.cloudpivot.web.api.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.authine.cloudpivot.engine.api.model.organization.DepartmentModel;
import com.authine.cloudpivot.engine.api.model.organization.UserModel;
import com.authine.cloudpivot.web.api.constants.Constants;
import com.authine.cloudpivot.web.api.entity.CreditDailyAllBank;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.service.FarmingDevelopBankService;
import com.authine.cloudpivot.web.api.utils.SystemDataSetUtils;
import com.esotericsoftware.minlog.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName CreditDailyAllBankListener
 * @author: lfh
 * @Date:2020/7/2 10:16
 * @Description:
 **/
public class CreditDailyAllBankListener extends AnalysisEventListener<CreditDailyAllBank> {
    private UserModel user;
    private DepartmentModel department;
    private FarmingDevelopBankService farmingDevelopBankService;
    private static final int BATCH_COUNT = 100;
    List<CreditDailyAllBank> list = new ArrayList<>();
    int sort = 1;
    public CreditDailyAllBankListener(UserModel user, DepartmentModel department, FarmingDevelopBankService farmingDevelopBankService) {
        this.user = user;
        this.department = department;
        this.farmingDevelopBankService = farmingDevelopBankService;
    }

    @Override
    public void invoke(CreditDailyAllBank creditDailyAllBank, AnalysisContext analysisContext) {
        SystemDataSetUtils.dataSet(user, department, user.getName(), Constants.COMPLETED_STATUS, creditDailyAllBank);
        creditDailyAllBank.setSort_key(sort++);
        list.add(creditDailyAllBank);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

        try {
            CreditDailyAllBank creditDailyAllBankDate = farmingDevelopBankService.findCreditDailyAllBankDate(list.get(0).getCreatedTime());
            if (null !=creditDailyAllBankDate){
                //本日已经插入数据，删除本日数据
                farmingDevelopBankService.deleteCreditDailyAllBankDate(list.get(0).getCreatedTime());
            }
            farmingDevelopBankService.saveCreditDailyAllBank(list);
        }catch (Exception e ){
            e.printStackTrace();
            Log.info("插入失败");
        }
    }
}
