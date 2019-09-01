package com.hb.batch.mapper;

import com.hb.facade.entity.AgentDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AgentMapper {

    AgentDO findAgent(@Param("agentDO") AgentDO agentDO);

}