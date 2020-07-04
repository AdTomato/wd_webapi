package com.authine.cloudpivot.web.api.service.impl;

import com.authine.cloudpivot.web.api.entity.Attachment;
import com.authine.cloudpivot.web.api.entity.CreditDailyAllBank;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.entity.SaveMonitor;
import com.authine.cloudpivot.web.api.mapper.FarmingDevelopBankMapper;
import com.authine.cloudpivot.web.api.service.FarmingDevelopBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName FarmingDevelopBankImpl
 * @author: lfh
 * @Date:2020/7/1 15:41
 * @Description:
 **/
@Service
public class FarmingDevelopBankImpl  implements FarmingDevelopBankService {
    @Resource
    private FarmingDevelopBankMapper farmingDevelopBankMapper;
    @Override
    public void saveSaveMonitor(List<SaveMonitor> saveMonitors) {
        farmingDevelopBankMapper.saveSaveMonitor(saveMonitors);
    }

    @Override
    public void saveCreditMonitor(List<CreditMonitor> creditMonitors) {
        farmingDevelopBankMapper.saveCreditMonitor(creditMonitors);
    }

    @Override
    public void saveCreditDailyAllBank(List<CreditDailyAllBank> creditDailyAllBanks) {
        farmingDevelopBankMapper.saveCreditDailyAllBank(creditDailyAllBanks);
    }

    @Override
    public SaveMonitor findTodayDate(Date createdTime) {
        return farmingDevelopBankMapper.findTodayDate(createdTime);
    }

    @Override
    public void deleteTodaySaveDate(Date createdTime) {
        farmingDevelopBankMapper.deleteTodaySaveDate(createdTime);
    }

    @Override
    public CreditMonitor findTodayCreditDate(Date createdTime) {
        return farmingDevelopBankMapper.findTodayCreditDate(createdTime);
    }

    @Override
    public void deleteTodayCreditDate(Date createdTime) {
        farmingDevelopBankMapper.deleteTodayCreditDate(createdTime);
    }

    @Override
    public CreditDailyAllBank findCreditDailyAllBankDate(Date createdTime) {
        return farmingDevelopBankMapper.findCreditDailyAllBankDate(createdTime);
    }

    @Override
    public void deleteCreditDailyAllBankDate(Date createdTime) {
        farmingDevelopBankMapper.deleteCreditDailyAllBankDate(createdTime);
    }

    @Override
    public Attachment findAttachment(String refId, String name) {
        return farmingDevelopBankMapper.findAttachment(refId,name);
    }
}
