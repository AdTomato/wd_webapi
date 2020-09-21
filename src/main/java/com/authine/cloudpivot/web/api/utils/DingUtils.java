package com.authine.cloudpivot.web.api.utils;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 钉钉工具类
 */
@Component
public class DingUtils {

    @Autowired
    RedisUtils redisUtils;

    private static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
    private static final String CUSTOM_TOKEN_URL = "https://oapi.dingtalk.com/service/get_corp_token";
    private static final String GET_USER_INFO = "https://oapi.dingtalk.com/user/getuserinfo";

    private static final Long TOKEN_EXPIRE = 7100L;

    /**
     * 根据配置文件名获取钉钉的appkey，appsecret
     *
     * @param corpId 配置文件名
     * @return 钉钉appkey，appsecret
     */
    public Map<String, String> getDingConfiguration(String corpId) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = DingUtils.class.getResourceAsStream("/dingding.properties");
        Map<String, String> result = new ConcurrentHashMap<>();
        properties.load(inputStream);
        String appkey = properties.getProperty(corpId + "_appkey");
        String appsecret = properties.getProperty(corpId + "_appsecret");
        String operationId = properties.getProperty(corpId + "_blackboard_user_id");
        result.put("appkey", appkey);
        result.put("appsecret", appsecret);
        result.put("operationId", operationId);
        return result;
    }

    /**
     * 获取钉钉的accesstoken
     *
     * @param corpId
     * @return 钉钉accessToken
     */
    public String getAccessToken(String corpId) {
        if (redisUtils.hasKey(corpId + "_token")) {
            return (String) redisUtils.get(corpId + "_token");
        }
        try {

            Map<String, String> dingConfig = getDingConfiguration(corpId);

//            DefaultDingTalkClient client = new DefaultDingTalkClient(TOKEN_URL);
//            OapiGettokenRequest request = new OapiGettokenRequest();
//            request.setAppkey(dingConfig.get("appkey"));
//            request.setAppsecret(dingConfig.get("appsecret"));
//            request.setHttpMethod("GET");
//            OapiGettokenResponse response = client.execute(request);

            DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/service/get_corp_token");
            OapiServiceGetCorpTokenRequest req = new OapiServiceGetCorpTokenRequest();
            req.setAuthCorpid(corpId);
            OapiServiceGetCorpTokenResponse response = client.execute(req,dingConfig.get("appkey"),dingConfig.get("appsecret"), "suiteTicket");

            if (response.getErrcode() == 0) {
                String accessToken = response.getAccessToken();
                redisUtils.set(corpId + "_token", accessToken, TOKEN_EXPIRE);
                return accessToken;
            } else {
                throw new RuntimeException(JSON.toJSONString(response));
            }
        } catch (IOException e) {
            throw new RuntimeException("dingConfig查找失败");
        } catch (ApiException e) {
            throw new RuntimeException("token查询失败");
        }
    }

    /**
     * 获取用户id
     *
     * @param corpId 配置文件名
     * @param code   免登授权码
     * @return userId
     */
    public String getUserIdByCode(String corpId, String code) {
        String accessToken = getAccessToken(corpId);
        DingTalkClient client = new DefaultDingTalkClient(GET_USER_INFO);
        OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
        request.setCode(code);
        request.setHttpMethod("GET");
        OapiUserGetuserinfoResponse response = null;
        try {
            response = client.execute(request, accessToken);
            if (response.isSuccess()) {
                return response.getUserid();
            } else {
                throw new RuntimeException(JSON.toJSONString(response));
            }
        } catch (ApiException e) {
            throw new RuntimeException("token查询失败");
        }
    }

    public OapiBlackboardGetResponse.OapiBlackboardVo getBlackboardDetails(String accessToken, String operationId, String placardId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/get");
        OapiBlackboardGetRequest req = new OapiBlackboardGetRequest();
        req.setBlackboardId(placardId);
        req.setOperationUserid(operationId);
        OapiBlackboardGetResponse rsp = client.execute(req, accessToken);
        return rsp.getResult();
    }

    /**
     * 获取用户详情
     *
     * @return
     * @throws ApiException
     */
    public OapiUserGetResponse getUserInfo(String userId, String corpId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userId);
        request.setHttpMethod("GET");
        OapiUserGetResponse response = client.execute(request, getAccessToken(corpId));
        return response;
    }

    /**
     * 获取部门详情
     */
    public OapiDepartmentGetResponse getDepartmentDetails(String deptId, String corpId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/department/get");
        OapiDepartmentGetRequest request = new OapiDepartmentGetRequest();
        request.setId(deptId);
        request.setHttpMethod("GET");
        OapiDepartmentGetResponse response = client.execute(request, getAccessToken(corpId));
        return response;
    }

    /**
     * 获取用户可看公告
     */
    public OapiBlackboardListtoptenResponse userVisibleBlackboard(String accessToken, String userId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/listtopten");
        OapiBlackboardListtoptenRequest request = new OapiBlackboardListtoptenRequest();
        request.setUserid(userId);
        OapiBlackboardListtoptenResponse response = client.execute(request, accessToken);
        return response;
    }

    /**
     * 获取分类列表
     *
     * @return
     */
    public List<OapiBlackboardCategoryListResponse.BlackboardCategoryVo> categoryList(String accessToken, String operationId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/category/list");
        OapiBlackboardCategoryListRequest req = new OapiBlackboardCategoryListRequest();
        req.setOperationUserid(operationId);
        OapiBlackboardCategoryListResponse rsp = client.execute(req, accessToken);
        return rsp.getResult();
    }

    /**
     * 根据分类id获取公告id列表
     */
    public List<String> placardListByCategoryId(String accessToken, String operationId, Long page, String categoryId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/blackboard/listids");
        OapiBlackboardListidsRequest req = new OapiBlackboardListidsRequest();
        OapiBlackboardListidsRequest.OapiBlackboardQueryVo obj1 = new OapiBlackboardListidsRequest.OapiBlackboardQueryVo();
        obj1.setOperationUserid(operationId);
        obj1.setPageSize(10L);
        LocalDateTime nowDay = LocalDateTime.now();
        nowDay.plusSeconds(-10);
        Date endTime = Date.from(nowDay.atZone(ZoneId.systemDefault()).toInstant());
        nowDay.plusSeconds(10);
        LocalDateTime halfYearDay = nowDay.plusMonths(-5);

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
