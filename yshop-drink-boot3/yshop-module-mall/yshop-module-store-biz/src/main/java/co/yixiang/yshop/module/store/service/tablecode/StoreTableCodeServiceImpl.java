package co.yixiang.yshop.module.store.service.tablecode;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.yixiang.yshop.framework.common.pojo.PageResult;
import co.yixiang.yshop.framework.security.core.util.SecurityFrameworkUtils;
import co.yixiang.yshop.module.store.controller.admin.tablecode.vo.*;
import co.yixiang.yshop.module.store.dal.dataobject.tablecode.StoreTableCodeDO;
import co.yixiang.yshop.module.store.dal.mysql.tablecode.StoreTableCodeMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static co.yixiang.yshop.framework.common.exception.util.ServiceExceptionUtil.exception;
import static co.yixiang.yshop.module.store.enums.ErrorCodeConstants.TABLE_CODE_WECHAT_CONFIG_ERROR;

@Service
public class StoreTableCodeServiceImpl implements StoreTableCodeService {

    private static final int TABLE_COUNT = 10;

    @Resource
    private StoreTableCodeMapper tableCodeMapper;

    @Value("${wx.miniapp.appid:}")
    private String miniAppId;

    @Value("${wx.miniapp.secret:}")
    private String miniAppSecret;

    @Value("${wx.miniapp.env-version:release}")
    private String miniAppEnvVersion;

    @Override
    @Transactional
    public String createSet(StoreTableCodeCreateReqVO reqVO) {
        String setCode = "TC" + IdUtil.getSnowflakeNextIdStr();
        for (int tableNo = 1; tableNo <= TABLE_COUNT; tableNo++) {
            String code = setCode + "-" + tableNo;
            tableCodeMapper.insert(StoreTableCodeDO.builder()
                    .setCode(setCode)
                    .setName(reqVO.getSetName())
                    .tableNo(tableNo)
                    .code(code)
                    .qrcodeImage(createMiniProgramCode(code))
                    .build());
        }
        return setCode;
    }

    @Override
    @Transactional
    public void bindSet(StoreTableCodeBindReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long shopId = SecurityFrameworkUtils.getLoginUser().getShopId();
        List<StoreTableCodeDO> codes = tableCodeMapper.selectListBySetCode(reqVO.getSetCode());
        if (codes.isEmpty()) {
            throw new IllegalArgumentException("桌码套装不存在");
        }
        boolean hasBound = codes.stream().anyMatch(item -> item.getMerchantUserId() != null && item.getMerchantUserId() > 0);
        if (hasBound) {
            throw new IllegalArgumentException("该桌码套装已绑定商家");
        }
        for (StoreTableCodeDO code : codes) {
            code.setMerchantUserId(userId);
            code.setShopId(shopId == null ? 0L : shopId);
            code.setBindTime(LocalDateTime.now());
            tableCodeMapper.updateById(code);
        }
    }

    @Override
    public PageResult<StoreTableCodeRespVO> getPage(StoreTableCodePageReqVO reqVO) {
        List<StoreTableCodeDO> rows = tableCodeMapper.selectPageLike(reqVO.getSetName(), reqVO.getSetCode());
        Long shopId = SecurityFrameworkUtils.getLoginUser().getShopId();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (shopId != null && shopId > 0) {
            rows = rows.stream()
                    .filter(item -> item.getMerchantUserId() == null || item.getMerchantUserId() == 0 || userId.equals(item.getMerchantUserId()))
                    .toList();
        }
        List<StoreTableCodeRespVO> list = rows.stream().map(this::convert).toList();
        return new PageResult<>(list, (long) list.size());
    }

    @Override
    public StoreTableCodeResolveRespVO resolve(String code) {
        StoreTableCodeDO tableCode = tableCodeMapper.selectByCode(code);
        if (tableCode == null) {
            throw new IllegalArgumentException("桌码不存在");
        }
        StoreTableCodeResolveRespVO respVO = new StoreTableCodeResolveRespVO();
        respVO.setCode(tableCode.getCode());
        respVO.setSetCode(tableCode.getSetCode());
        respVO.setTableNo(tableCode.getTableNo());
        respVO.setMerchantUserId(tableCode.getMerchantUserId());
        respVO.setShopId(tableCode.getShopId());
        return respVO;
    }

    private StoreTableCodeRespVO convert(StoreTableCodeDO item) {
        StoreTableCodeRespVO respVO = new StoreTableCodeRespVO();
        respVO.setId(item.getId());
        respVO.setSetCode(item.getSetCode());
        respVO.setSetName(item.getSetName());
        respVO.setTableNo(item.getTableNo());
        respVO.setCode(item.getCode());
        respVO.setMerchantUserId(item.getMerchantUserId());
        respVO.setShopId(item.getShopId());
        respVO.setBindTime(item.getBindTime());
        respVO.setQrcodeImage(item.getQrcodeImage());
        return respVO;
    }

    private String createMiniProgramCode(String code) {
        if (StrUtil.isBlank(miniAppId) || StrUtil.isBlank(miniAppSecret)) {
            throw exception(TABLE_CODE_WECHAT_CONFIG_ERROR, "请先配置 wx.miniapp.appid 和 wx.miniapp.secret");
        }
        String accessToken = getAccessToken();
        JSONObject body = JSONUtil.createObj()
                .set("scene", code)
                .set("page", "pages/menu/menu")
                .set("check_path", false)
                .set("env_version", miniAppEnvVersion)
                .set("width", 430);
        try (HttpResponse response = HttpUtil.createPost("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken)
                .body(body.toString(), "application/json")
                .execute()) {
            String contentType = response.header("Content-Type");
            byte[] bytes = response.bodyBytes();
            if (contentType != null && contentType.contains("application/json")) {
                throw exception(TABLE_CODE_WECHAT_CONFIG_ERROR, parseWechatError(new String(bytes)));
            }
            return "data:image/png;base64," + Base64.encode(bytes);
        }
    }

    private String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                + miniAppId + "&secret=" + miniAppSecret;
        String result = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(result);
        String token = json.getStr("access_token");
        if (StrUtil.isBlank(token)) {
            throw exception(TABLE_CODE_WECHAT_CONFIG_ERROR, parseWechatError(result));
        }
        return token;
    }

    private String parseWechatError(String result) {
        JSONObject json = JSONUtil.parseObj(result);
        Integer errcode = json.getInt("errcode");
        String errmsg = json.getStr("errmsg", result);
        if (Integer.valueOf(40164).equals(errcode)) {
            return "当前服务器出口 IP 未加入微信小程序后台 IP 白名单，请到微信公众平台添加白名单后再生成。微信原始错误：" + errmsg;
        }
        if (Integer.valueOf(40013).equals(errcode) || Integer.valueOf(40125).equals(errcode)) {
            return "wx.miniapp.appid 或 wx.miniapp.secret 配置不正确。微信原始错误：" + errmsg;
        }
        return errmsg;
    }
}
