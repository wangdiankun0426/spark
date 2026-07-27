package com.spark.web.controller.system;

import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.system.vo.LoginVO;
import com.spark.bean.system.vo.UserVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.manage.system.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:51
 */
@RequestMapping("system/user")
@RestController
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private IUserService userService;

    /**
     * 创建用户
     * @param userVO 用户参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createUser(UserVO userVO) {
        return userService.createUser(userVO);
    }

    /**
     * 分页查询用户列表
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<UserResult>> pageUserList(UserQuery query) {
        return userService.pageUserList(query);
    }

    /**
     * 修改用户
     * @param userVO 修改用户参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateUser(UserVO userVO) {
        return userService.updateUser(userVO);
    }

    /**
     * 删除用户
     * @param userVO 删除用户参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteUser(UserVO userVO) {
        return userService.deleteUser(userVO);
    }

    /**
     * 查询用户详情
     * @param userVO 查询参数
     * @return 查询结果
     */
    @GetMapping("detail")
    public ResultData<UserResult> queryUserDetail(UserVO userVO) {
        return userService.queryUserDetail(userVO);
    }

    /**
     * 强制退出
     * @param loginVO 退出的参数
     * @return 退出结果
     */
    @PostMapping("forceLogout")
    public ResultData<Void> forceLogout(LoginVO loginVO) {
        return userService.forceLogout(loginVO);
    }

    /**
     * 修改密码
     * @param userVO 修改密码参数
     * @return 修改结果
     */
    @PostMapping("updatePassword")
    public ResultData<Void> updatePassword(UserVO userVO) {
        return userService.updatePassword(userVO);
    }

    /**
     * 上传用户头像
     * @param file  文件
     * @return 上传结果
     */
    @PostMapping("upload/avatar")
    public ResultData<Void> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    /**
     * 用户头像
     *
     * @param response 响应
     * @param userVO   查询参数
     */
    @GetMapping("avatar")
    public void downloadAvatar(HttpServletResponse response, UserVO userVO) {
        ResultData<String> result = userService.queryUserAvatarPath(userVO);
        if (result.getCode() != ResultData.OK) {
            return;
        }
        try {
            File file = new File(result.getData());
            String filename = file.getName();
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.reset();
            response.setCharacterEncoding("UTF-8");
            // Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            // attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
        } catch (IOException ex) {
            logger.error("downloadAvatar error", ex);
        }
    }

}
