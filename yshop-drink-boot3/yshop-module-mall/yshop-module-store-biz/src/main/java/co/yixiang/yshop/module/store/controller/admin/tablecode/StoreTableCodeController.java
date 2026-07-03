package co.yixiang.yshop.module.store.controller.admin.tablecode;

import co.yixiang.yshop.framework.common.pojo.CommonResult;
import co.yixiang.yshop.framework.common.pojo.PageResult;
import co.yixiang.yshop.module.store.controller.admin.tablecode.vo.*;
import co.yixiang.yshop.module.store.service.tablecode.StoreTableCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static co.yixiang.yshop.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 桌码套装")
@RestController
@RequestMapping("/store/table-code")
@Validated
public class StoreTableCodeController {

    @Resource
    private StoreTableCodeService tableCodeService;

    @PostMapping("/create-set")
    @Operation(summary = "创建一套桌码，固定 10 个")
    public CommonResult<String> createSet(@Valid @RequestBody StoreTableCodeCreateReqVO reqVO) {
        return success(tableCodeService.createSet(reqVO));
    }

    @PostMapping("/bind")
    @Operation(summary = "商家账号绑定桌码套装")
    public CommonResult<Boolean> bind(@Valid @RequestBody StoreTableCodeBindReqVO reqVO) {
        tableCodeService.bindSet(reqVO);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得桌码列表")
    public CommonResult<PageResult<StoreTableCodeRespVO>> getPage(@Valid StoreTableCodePageReqVO reqVO) {
        return success(tableCodeService.getPage(reqVO));
    }

    @GetMapping("/resolve")
    @Operation(summary = "扫码解析桌码")
    @Parameter(name = "code", description = "桌码编号", required = true)
    public CommonResult<StoreTableCodeResolveRespVO> resolve(@RequestParam("code") String code) {
        return success(tableCodeService.resolve(code));
    }
}
