package com.hb.batch.service;

import com.hb.facade.entity.AgentDO;

/**
 * ========== 代理商service接口 ==========
 *
 * @author Mr.huang
 * @version IAgentService.java, v1.0
 * @date 2019年06月16日 12时24分
 */
public interface IAgentService {

    /**
     * ########## 查找代理商 ##########
     *
     * @param agentDO 代理商信息
     * @return AgentDO
     */
    AgentDO findAgent(AgentDO agentDO);

    /**
     * 根据邀请人手机号查询代理人员
     *
     * @param inviterMobile 邀请人手机号
     * @return 代理人信息
     */
    AgentDO getAgentByInviterMobile(String inviterMobile);
}
