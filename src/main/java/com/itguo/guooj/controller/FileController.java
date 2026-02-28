package com.itguo.guooj.controller;

import cn.hutool.core.io.FileUtil;
import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.common.ResultUtils;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    /**
     * 文件上传
     *
     * @param file
     * @param biz
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "biz", required = false) String biz,
                                           HttpServletRequest request) {
        // 校验用户登录
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 校验文件
        validFile(file, biz);
        
        // 文件目录
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 文件后缀
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        // 生成随机文件名
        String fileName = RandomStringUtils.randomAlphanumeric(8) + "." + suffix;
        String filePath = uploadPath + File.separator + fileName;

        try {
            // 上传文件
            file.transferTo(new File(filePath));
            // 返回可访问的URL（这里简化处理，实际应该返回可访问的URL）
            return ResultUtils.success("/uploads/" + fileName);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param biz
     */
    private void validFile(MultipartFile multipartFile, String biz) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        
        final long ONE_M = 1024 * 1024L;
        
        if ("user_avatar".equals(biz)) {
            if (fileSize > 2 * ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
            }
            if (!Arrays.asList("jpeg", "jpg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
