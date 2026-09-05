package com.spark.test.system;

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
        UserVO userVO = new UserVO();
        userVO.setLoginName("wangxiaoer");
        userVO.setName("王小二");
        userVO.setDeptId(602L);
        ResultData<Void> result = userService.createUser(userVO);
    }
}
