package com.hb.batch.impl;

import com.hb.batch.mapper.UserMapper;
import com.hb.batch.service.IUserService;
import com.hb.facade.entity.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ========== 用户service实现 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.api.sys.impl.UserServiceImpl.java, v1.0
 * @date 2019年06月03日 12时00分
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDO findUser(UserDO userDO) {
        return userMapper.findUser(userDO);
    }

}
