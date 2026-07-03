package co.yixiang.yshop.module.store.controller.admin.tablecode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 创建桌码套装 Request VO")
@Data
public class StoreTableCodeCreateReqVO {

    @Schema(description = "套装名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "A店第一套餐码")
    @NotBlank(message = "套装名称不能为空")
    private String setName;

}
