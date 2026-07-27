package com.spark.web.exception;

import com.spark.bean.base.BaseException;
import com.spark.bean.base.ResultData;
import com.spark.enums.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/22 13:45
 * 全局异常处理切面
 *
 * 本步骤需要使用@RestControllerAdvice注解，它是一个组合注解，由@ControllerAdvice、@ResponseBody组成，
 * 而@ControllerAdvice继承了@Component，因此@RestControllerAdvice本质上是个Component，用于定义@ExceptionHandler，
 * @InitBinder和@ModelAttribute方法，适用于所有使用@RequestMapping方法。还要用到@ExceptionHandler注解，
 * 可以认为它是一个异常拦截器，它采用“就近原则”，存在多个满足条件的异常处理器时会选择最接近的一个来使用。
 * 它本质上就是使用Spring AOP定义的一个切面，在系统抛出异常后执行。
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 默认的运行期异常
     * @param e 运行时异常
     * @return 错误信息
     */
    @ExceptionHandler({RuntimeException.class})
    public ResultData<Void> handleRuntimeException(RuntimeException e) {
        logger.error("globalExceptionHandlerAdvice handleRuntimeException is ", e);
        ResultData<Void> result = new ResultData<>();
        result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
        return result;
    }

    /**
     * 自定义异常
     * @param e 自定义异常
     * @return 错误信息
     */
    @ExceptionHandler({BaseException.class})
    public ResultData<Void> handleBaseException(BaseException e) {
        logger.error("globalExceptionHandlerAdvice handleBaseException is ", e);
        ResultData<Void> result = new ResultData<>();
        result.setCode(e.getCode());
        result.setMessage(e.getMessage());
        return result;
    }
}
