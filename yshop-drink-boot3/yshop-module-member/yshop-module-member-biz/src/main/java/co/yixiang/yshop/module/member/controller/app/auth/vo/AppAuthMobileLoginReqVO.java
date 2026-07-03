package co.yixiang.yshop.module.member.controller.app.auth.vo;

import co.yixiang.yshop.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 手机号直接登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthMobileLoginReqVO {

    @Schema(description = "手机号", required = true, example = "15601691300")
    @NotEmpty(message = "手机号不能为空")
    @Mobile
    private String mobile;

    @Schema(description = "设备来源", example = "h5")
    private String from;

    private String openid;

}
