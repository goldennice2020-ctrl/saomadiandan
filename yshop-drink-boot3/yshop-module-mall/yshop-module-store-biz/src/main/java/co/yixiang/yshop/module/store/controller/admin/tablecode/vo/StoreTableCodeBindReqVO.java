package co.yixiang.yshop.module.store.controller.admin.tablecode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 绑定桌码套装 Request VO")
@Data
public class StoreTableCodeBindReqVO {

    @Schema(description = "套装编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "套装编号不能为空")
    private String setCode;

}
