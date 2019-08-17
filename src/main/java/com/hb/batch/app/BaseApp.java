package com.hb.batch.app;

import com.hb.facade.common.AppResponseCodeEnum;
import com.hb.facade.common.AppResultModel;
import com.hb.remote.tool.AlarmTools;
import com.hb.unic.logger.Logger;
import com.hb.unic.logger.LoggerFactory;
import com.hb.unic.util.helper.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ========== App控制器超类 ==========
 *
 * @author Mr.huang
 * @version com.hb.web.app.base.BaseApp.java, v1.0
 * @date 2019年06月13日 23时42分
 */
public class BaseApp {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApp.class);

    @Autowired
    public HttpServletRequest request;

    @Autowired
    public HttpServletResponse response;

    @Autowired
    public AlarmTools alarmTools;

    /**
     * ########## 统一异常处理 ##########
     *
     * @param exception 异常
     * @return AppResultModel
     */
    @ExceptionHandler
    public AppResultModel exceptionHandler(Exception exception) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("统一异常处理 => {}", LogHelper.getStackTrace(exception));
        }
        alarmTools.alert("APP", "统一异常处理", "系统异常", exception.getMessage());
        return AppResultModel.generateResponseData(AppResponseCodeEnum.FAIL);
    }

}
