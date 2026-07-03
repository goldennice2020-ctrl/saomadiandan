package co.yixiang.yshop.module.store.controller.admin.tablecode.vo;

import co.yixiang.yshop.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 桌码套装分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoreTableCodePageReqVO extends PageParam {

    @Schema(description = "套装名称")
    private String setName;

    @Schema(description = "套装编号")
    private String setCode;

}
