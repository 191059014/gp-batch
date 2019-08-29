package com.hb.batch.mapper;

import com.hb.facade.entity.StockListDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface StockListMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(StockListDO record);

    int insertSelective(StockListDO record);

    StockListDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(StockListDO record);

    int updateByPrimaryKey(StockListDO record);

    List<StockListDO> getStockListByStockCodeSet(Set<String> stockCodeSet);

    List<StockListDO> findPageList(@Param("queryText") String queryText, @Param("startRow") Integer startRow, @Param("pageSize") Integer pageSize);

    List<StockListDO> getAllStockList();

}