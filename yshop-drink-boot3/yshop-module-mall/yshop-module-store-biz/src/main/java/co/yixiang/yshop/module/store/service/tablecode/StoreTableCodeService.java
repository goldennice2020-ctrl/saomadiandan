package co.yixiang.yshop.module.store.service.tablecode;

import co.yixiang.yshop.framework.common.pojo.PageResult;
import co.yixiang.yshop.module.store.controller.admin.tablecode.vo.*;

public interface StoreTableCodeService {

    String createSet(StoreTableCodeCreateReqVO reqVO);

    void bindSet(StoreTableCodeBindReqVO reqVO);

    PageResult<StoreTableCodeRespVO> getPage(StoreTableCodePageReqVO reqVO);

    StoreTableCodeResolveRespVO resolve(String code);

}
