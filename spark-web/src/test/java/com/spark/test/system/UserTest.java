package com.spark.test.system;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.vo.UserVO;
import com.spark.common.bean.base.ResultData;
import com.spark.manage.sys.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/18 21:10
 */
@SpringBootTest
public class UserTest {
    @Autowired
    private IUserService userService;

    /**
     * 测试创建用户
     */
    @Test
    public void testCreateUser() {
        try {
            SessionHolder.setCurrentUserId(101L);
            SessionHolder.setCurrentTenantId(103L);
            UserVO userVO = new UserVO();
            userVO.setLoginName("wangxiaoer");
            userVO.setName("王小二");
            ResultData<Long> result = userService.createUser(userVO);
        } catch (Exception e) {
        } finally {
            SessionHolder.clearLocalSession();
        }
    }

}
