package co.yixiang.yshop.module.store.dal.dataobject.tablecode;

import co.yixiang.yshop.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 门店桌码 DO
 */
@TableName("yshop_store_table_code")
@KeySequence("yshop_store_table_code_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTableCodeDO extends BaseDO {

    @TableId
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
