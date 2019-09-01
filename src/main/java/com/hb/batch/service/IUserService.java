package com.hb.batch.service;


import com.hb.facade.entity.UserDO;

/**
 * ========== 用户service接口 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.api.sys.IUserService.java, v1.0
 * @date 2019年06月03日 12时00分
 */
public interface IUserService {

    /**
     * ########## 查询用户 ##########
     *
     * @param userDO 用户信息
     * @return 用户信息
     */
    UserDO findUser(UserDO userDO);

}
