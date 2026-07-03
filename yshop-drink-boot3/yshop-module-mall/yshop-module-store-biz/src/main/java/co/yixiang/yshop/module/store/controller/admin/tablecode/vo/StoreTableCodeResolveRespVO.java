package co.yixiang.yshop.module.store.controller.admin.tablecode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户端 - 桌码解析 Response VO")
@Data
public class StoreTableCodeResolveRespVO {

    private String code;

    private String setCode;

    private Integer tableNo;

    private Long merchantUserId;

    private Long shopId;

}
