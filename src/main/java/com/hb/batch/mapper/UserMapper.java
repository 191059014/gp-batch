package com.hb.batch.mapper;

import com.hb.facade.entity.UserDO;
import org.apache.ibatis.annotations.Param;

/**
 * ========== 用户mapper ==========
 *
 * @author Mr.huang
 * @version com.hb.web.mapper.UserMapper.java, v1.0
 * @date 2019年06月03日 20时15分
 */
public interface UserMapper {

    /**
     * ########## 查询用户 ##########
     *
     * @param userDO 用户信息
     * @return 用户信息
     */
    UserDO findUser(@Param("userDO") UserDO userDO);

}
