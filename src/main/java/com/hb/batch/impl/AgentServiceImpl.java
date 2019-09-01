package com.hb.batch.impl;

import com.hb.batch.mapper.AgentMapper;
import com.hb.batch.service.IAgentService;
import com.hb.facade.entity.AgentDO;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ========== 代理商service实现类 ==========
 *
 * @author Mr.huang
 * @version AgentServiceImpl.java, v1.0
 * @date 2019年06月16日 12时25分
 */
@Service
public class AgentServiceImpl implements IAgentService {

    private Logger LOGGER = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Autowired
    private AgentMapper agentMapper;

    @Override
    public AgentDO findAgent(AgentDO agentDO) {
        return agentMapper.findAgent(agentDO);
    }

    @Override
    public AgentDO getAgentByInviterMobile(String inviterMobile) {
        AgentDO query = new AgentDO();
        query.setMobile(inviterMobile);
        AgentDO agent = findAgent(query);
        if (agent == null) {
            agent = new AgentDO();
        }
        return agent;
    }


}
