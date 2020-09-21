package com.authine.cloudpivot.web.api.controller;

import com.authine.cloudpivot.engine.api.model.runtime.BizObjectModel;
import com.authine.cloudpivot.web.api.controller.base.BaseController;
import com.authine.cloudpivot.web.api.utils.DingUtils;
import com.authine.cloudpivot.web.api.view.ResponseResult;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiBlackboardCategoryListRequest;
import com.dingtalk.api.request.OapiBlackboardListidsRequest;
import com.dingtalk.api.response.*;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/dingding")
@Api(value = "钉钉相关接口", tags = "钉钉相关接口")
@Slf4j
public class DingDingController extends BaseController {

    @Autowired
    DingUtils dingUtils;

    @ApiOperation(value = "根据临时code获取userId", notes = "根据临时code获取userId", httpMethod = "GET")
    @RequestMapping("/userIdByCode")
    public ResponseResult<Object> userIdByCode(@ApiParam("企业corpId") String corpId, String code) {
        String userId = dingUtils.getUserIdByCode(corpId, code);
        log.info("userId:{}", userId);
        return this.getErrResponseResult(userId, 200L, "成功");
    }

    @GetMapping("/token")
    public ResponseResult<Object> token(String corpId) {

        String accessToken = dingUtils.getAccessToken(corpId);

        return this.getErrResponseResult(accessToken, 200L, "成功");
    }

    @GetMapping("/blackboardDetailsById")
    public ResponseResult<Object> blackboardDetails(String corpId, String userId, String blackboardId) throws ApiException {
       /* String accessToken = dingUtils.getAccessToken(corpId);
        Object details = dingUtils.getBlackboardDetails(accessToken, userId, blackboardId);
        return this.getErrResponseResult(details, 200L, "成功");*/

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/category/list");
        OapiBlackboardCategoryListRequest req = new OapiBlackboardCategoryListRequest();
        req.setOperationUserid(userId);
        OapiBlackboardCategoryListResponse rsp = client.execute(req, dingUtils.getAccessToken(corpId));
        return this.getErrResponseResult(rsp, 200L, "成功");
    }

    /**
     * 获取可看公告列表
     *
     * @param corpId       企业corpid
     * @param page         页数
     * @param categoryName 分类名
     * @return
     * @throws ApiException
     * @throws IOException
     */
    @GetMapping("/blackBoardDetailsVisible")
    public ResponseResult<Object> blackBoardDetailsVisible(String corpId, String userId, String page, String categoryName) throws ApiException, IOException {
        String accessToken = dingUtils.getAccessToken(corpId);
        //获取公告管理员id
        Map<String, String> configuration = dingUtils.getDingConfiguration(corpId);
        String operationId = configuration.get("operationId");
        //获取userId
//        String userId = dingUtils.getUserIdByCode(corpId, code);
        //String userId = "2507640341938140";
        OapiUserGetResponse userInfo = dingUtils.getUserInfo(userId, corpId);
        List<String> departmentNames = new ArrayList<>();
        if (userInfo.getDepartment() == null || userInfo.getDepartment().isEmpty()) {
            departmentNames = null;
        } else {
            for (Long deptId : userInfo.getDepartment()) {
                OapiDepartmentGetResponse departmentDetails = dingUtils.getDepartmentDetails(Long.toString(deptId), corpId);
                departmentNames.add(departmentDetails.getName());
            }
        }

        //获取分类列表
        List<OapiBlackboardCategoryListResponse.BlackboardCategoryVo> blackboardCategoryList = dingUtils.categoryList(accessToken, operationId);
        if (blackboardCategoryList == null || blackboardCategoryList.isEmpty()) {
            return this.getOkResponseResult("", "未获取到分类列表");
        }
        //初始化分类id
        String categoryId = "";
        for (OapiBlackboardCategoryListResponse.BlackboardCategoryVo blackboardCategoryVo : blackboardCategoryList) {
            if (blackboardCategoryVo.getName().equals(categoryName)) {
                categoryId = blackboardCategoryVo.getId();
            }
        }
        if (categoryId.equals("")) {
            return this.getOkResponseResult("", "未查到分类列表内匹配的该分类");
        }
        long pageLong = Long.parseLong(page);
        //用户可看到公告
        Map<String, Object> resultList = new ConcurrentHashMap<>();
        //如果可看公告小于10条 则继续页数加1向下查询
        queryNextPagePlacard(accessToken, operationId, pageLong, categoryId, resultList, userInfo, departmentNames);
        if (resultList.containsKey("blackboardData")) {
            List<Map<String, Object>> blackboardData = (List<Map<String, Object>>) resultList.get("blackboardData");
            if (blackboardData.size() < 10) {
                resultList.put("isOver", true);
            } else {
                resultList.put("isOver", false);
            }
        } else {
            return this.getErrResponseResult(null, 404L, "暂无数据");
        }
        return this.getErrResponseResult(resultList, 200L, "获取公告数据成功");
    }

    public void queryNextPagePlacard(String accessToken, String operationId, Long page, String categoryId, Map<String, Object> resultList, OapiUserGetResponse userInfo, List<String> departmentName) throws ApiException {
        //根据分类id获取公告id列表
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> placardList = dingUtils.placardListByCategoryId(accessToken, operationId, page, categoryId);
        if (placardList == null || placardList.isEmpty()) {
//            return this.getOkResponseResult(resultList, "该页无公告内容");
            return;
        }
        BizObjectModel b = new BizObjectModel();
//        this.getBizObjectFacade().re
        List<OapiBlackboardGetResponse.OapiBlackboardVo> allBlackboardList = new ArrayList<>();
        //根据公告id获取公告详情
        for (String placard : placardList) {
            OapiBlackboardGetResponse.OapiBlackboardVo blackboardDetails = dingUtils.getBlackboardDetails(accessToken, operationId, placard);
            allBlackboardList.add(blackboardDetails);
        }
        // 判断公告权限是否可见
        for (OapiBlackboardGetResponse.OapiBlackboardVo blackboardDetails : allBlackboardList) {
            if (blackboardDetails.getDepnameList() == null && blackboardDetails.getUsernameList() == null) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", blackboardDetails.getId());
                resultMap.put("title", blackboardDetails.getTitle());
                resultMap.put("gmt_create", blackboardDetails.getGmtCreate());
                list.add(resultMap);
            } else {
                if (blackboardDetails.getDepnameList() != null) {
                    //公告可看员是选择的部门
                    if (blackboardDetails.getUsernameList() == null) {
                        for (String s : blackboardDetails.getDepnameList()) {
                            if (departmentName.contains(s)) {
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("id", blackboardDetails.getId());
                                resultMap.put("title", blackboardDetails.getTitle());
                                resultMap.put("gmt_create", blackboardDetails.getGmtCreate());
                                list.add(resultMap);
                            }
                        }
                    } else {
                        //包括部门和人员
                        for (String s : blackboardDetails.getDepnameList()) {
                            if (blackboardDetails.getUsernameList().contains(userInfo.getName()) || departmentName.contains(s)) {
                                Map<String, Object> resultMap = new HashMap<>();
                                resultMap.put("id", blackboardDetails.getId());
                                resultMap.put("title", blackboardDetails.getTitle());
                                resultMap.put("gmt_create", blackboardDetails.getGmtCreate());
                                list.add(resultMap);
                            }
                        }
                    }
                } else {
                    //值选择了人
                    if (blackboardDetails.getUsernameList().contains(userInfo.getName())) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("id", blackboardDetails.getId());
                        resultMap.put("title", blackboardDetails.getTitle());
                        resultMap.put("gmt_create", blackboardDetails.getGmtCreate());
                        list.add(resultMap);
                    }
                }
            }

        }

        // 将数据进行存储
        if (resultList.containsKey("blackboardData")) {
            List<Map<String, Object>> blackboardData = (List<Map<String, Object>>) resultList.get("blackboardData");
            blackboardData.addAll(list);
        } else {
            resultList.put("blackboardData", list);
        }

        int dataSize = ((List<Map<String, Object>>) resultList.get("blackboardData")).size();
        if (dataSize < 10) {
            // 不足10条
            queryNextPagePlacard(accessToken, operationId, page + 1, categoryId, resultList, userInfo, departmentName);
        } else {
            resultList.put("page", page);
            return;
        }
    }

    /**
     * 根据分类id获取公告id列表
     */
    @PostMapping("/placardListByCategoryId")
    public List<String> placardListByCategoryId(String accessToken, String operationId, Long page, String categoryId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/listids");
        OapiBlackboardListidsRequest req = new OapiBlackboardListidsRequest();
        OapiBlackboardListidsRequest.OapiBlackboardQueryVo obj1 = new OapiBlackboardListidsRequest.OapiBlackboardQueryVo();
        obj1.setOperationUserid(operationId);
        obj1.setPageSize(10L);
        LocalDateTime nowDay = LocalDateTime.now();
        Date endTime = Date.from(nowDay.atZone(ZoneId.systemDefault()).toInstant());
        LocalDateTime halfYearDay = nowDay.plusMonths(-6);
        Date startTime = Date.from(halfYearDay.atZone(ZoneId.systemDefault()).toInstant());
        obj1.setStartTime(startTime);
        obj1.setEndTime(endTime);
        obj1.setPage(page);
        obj1.setCategoryId(categoryId);
        req.setQueryRequest(obj1);
        OapiBlackboardListidsResponse rsp = client.execute(req, accessToken);
        return rsp.getResult();
    }

}
