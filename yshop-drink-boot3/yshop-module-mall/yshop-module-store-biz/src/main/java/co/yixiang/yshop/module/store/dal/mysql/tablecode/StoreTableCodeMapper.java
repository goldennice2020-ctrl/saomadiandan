package co.yixiang.yshop.module.store.dal.mysql.tablecode;

import co.yixiang.yshop.framework.mybatis.core.mapper.BaseMapperX;
import co.yixiang.yshop.framework.mybatis.core.query.LambdaQueryWrapperX;
import co.yixiang.yshop.module.store.dal.dataobject.tablecode.StoreTableCodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreTableCodeMapper extends BaseMapperX<StoreTableCodeDO> {

    default List<StoreTableCodeDO> selectListBySetCode(String setCode) {
        return selectList(new LambdaQueryWrapperX<StoreTableCodeDO>()
                .eq(StoreTableCodeDO::getSetCode, setCode)
                .orderByAsc(StoreTableCodeDO::getTableNo));
    }

    default StoreTableCodeDO selectByCode(String code) {
        return selectOne(StoreTableCodeDO::getCode, code);
    }

    default List<StoreTableCodeDO> selectPageLike(String setName, String setCode) {
        return selectList(new LambdaQueryWrapperX<StoreTableCodeDO>()
                .likeIfPresent(StoreTableCodeDO::getSetName, setName)
                .likeIfPresent(StoreTableCodeDO::getSetCode, setCode)
                .orderByDesc(StoreTableCodeDO::getId));
    }
}
