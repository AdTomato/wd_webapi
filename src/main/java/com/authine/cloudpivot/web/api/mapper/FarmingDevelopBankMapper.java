package com.authine.cloudpivot.web.api.mapper;

import com.authine.cloudpivot.web.api.entity.Attachment;
import com.authine.cloudpivot.web.api.entity.CreditDailyAllBank;
import com.authine.cloudpivot.web.api.entity.CreditMonitor;
import com.authine.cloudpivot.web.api.entity.SaveMonitor;
import org.mapstruct.Mapper;

import java.util.Date;
import java.util.List;

/**
 * @ClassName FarmingDevelopBankMapper
 * @author: lfh
 * @Date:2020/7/1 15:41
 * @Description:
 **/
@Mapper
public interface FarmingDevelopBankMapper {
    void saveSaveMonitor(List<SaveMonitor> saveMonitors);

    void saveCreditMonitor(List<CreditMonitor> creditMonitors);

    void saveCreditDailyAllBank(List<CreditDailyAllBank> creditDailyAllBanks);

    SaveMonitor findTodayDate(Date createdTime);

    void deleteTodaySaveDate(Date createdTime);

    CreditMonitor findTodayCreditDate(Date createdTime);

    void deleteTodayCreditDate(Date createdTime);

    CreditDailyAllBank findCreditDailyAllBankDate(Date createdTime);

    void deleteCreditDailyAllBankDate(Date createdTime);

    Attachment findAttachment(String refId, String name);
}
