package co.yixiang.yshop.module.store.controller.app.tablecode;

import co.yixiang.yshop.framework.common.pojo.CommonResult;
import co.yixiang.yshop.module.store.controller.admin.tablecode.vo.StoreTableCodeResolveRespVO;
import co.yixiang.yshop.module.store.service.tablecode.StoreTableCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static co.yixiang.yshop.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 桌码")
@RestController
@RequestMapping("/store/table-code")
@Validated
public class AppStoreTableCodeController {

    @Resource
    private StoreTableCodeService tableCodeService;

    @GetMapping("/resolve")
    @Operation(summary = "扫码解析桌码")
    @Parameter(name = "code", description = "桌码编号", required = true)
    public CommonResult<StoreTableCodeResolveRespVO> resolve(@RequestParam("code") String code) {
        return success(tableCodeService.resolve(code));
    }
}
