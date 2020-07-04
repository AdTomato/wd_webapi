package com.authine.cloudpivot.web.api.service;

import com.authine.cloudpivot.web.api.entity.Attachment;
import com.authine.cloudpivot.web.api.entity.CreditDailyAllBank;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.entity.SaveMonitor;

import java.util.Date;
import java.util.List;

/**
 * @ClassName FarmingDevelopBankService
 * @author: lfh
 * @Date:2020/7/1 15:41
 * @Description:
 **/
public interface FarmingDevelopBankService {
    void saveSaveMonitor(List<SaveMonitor> saveMonitors);

    void saveCreditMonitor(List<CreditMonitor> creditMonitors);

    void saveCreditDailyAllBank(List<CreditDailyAllBank> creditDailyAllBanks);

    SaveMonitor findTodayDate(Date createdTime);

    void deleteTodaySaveDate(Date createdTime);

    CreditMonitor findTodayCreditDate(Date createdTime);

    void deleteTodayCreditDate(Date createdTime);

    CreditDailyAllBank findCreditDailyAllBankDate(Date createdTime);

    void deleteCreditDailyAllBankDate(Date createdTime);
    //查询附件表是否有数据
    Attachment findAttachment(String refId, String name);
}
