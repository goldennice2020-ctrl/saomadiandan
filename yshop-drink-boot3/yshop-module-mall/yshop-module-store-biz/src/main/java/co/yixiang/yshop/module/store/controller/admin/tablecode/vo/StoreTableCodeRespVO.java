package co.yixiang.yshop.module.store.controller.admin.tablecode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 桌码 Response VO")
@Data
public class StoreTableCodeRespVO {

    private Long id;

    private String setCode;

    private String setName;

    private Integer tableNo;

    private String code;

    private Long merchantUserId;

    private Long shopId;

    private LocalDateTime bindTime;

    private String qrcodeImage;

}
