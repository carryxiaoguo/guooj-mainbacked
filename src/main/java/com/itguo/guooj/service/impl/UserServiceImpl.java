package com.itguo.guooj.service.impl;

import static com.itguo.guooj.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.constant.CommonConstant;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.mapper.UserMapper;
import com.itguo.guooj.model.dto.user.UserQueryRequest;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.entity.UserQuestionAccept;
import com.itguo.guooj.model.enums.UserRoleEnum;
import com.itguo.guooj.model.vo.LoginUserVO;
import com.itguo.guooj.model.vo.SystemStatsVO;
import com.itguo.guooj.model.vo.UserActivityVO;
import com.itguo.guooj.model.vo.UserStatsVO;
import com.itguo.guooj.model.vo.UserVO;
import com.itguo.guooj.service.UserQuestionAcceptService;
import com.itguo.guooj.service.UserService;
import com.itguo.guooj.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * 
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "xiaoguo";

    @Resource
    private UserQuestionAcceptService userQuestionAcceptService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private com.itguo.guooj.mapper.QuestionMapper questionMapper;

    @Resource
    private com.itguo.guooj.mapper.QuestionSubmitMapper questionSubmitMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String userName) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //保证现成安全,防止并发注册
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            //用户名不能重复
            queryWrapper.eq("userName", userAccount);
            long countUserName = this.baseMapper.selectCount(queryWrapper);
            if (countUserName > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(userName);
            // 设置默认头像
            user.setUserAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + userAccount);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO userStats = new UserStatsVO();
        
        // 查询用户通过的题目数量
        QueryWrapper<UserQuestionAccept> acceptWrapper = new QueryWrapper<>();
        acceptWrapper.eq("userId", userId);
        int solvedCount = (int) userQuestionAcceptService.count(acceptWrapper);
        
        // 查询用户总提交数和通过数
        int submitCount = userMapper.countUserSubmits(userId);
        int acceptCount = userMapper.countUserAccepts(userId);
        
        // 计算通过率
        double acceptRate = submitCount > 0 ? (double) acceptCount / submitCount * 100 : 0.0;
        
        userStats.setSolvedCount(solvedCount);
        userStats.setSubmitCount(submitCount);
        userStats.setAcceptCount(acceptCount);
        userStats.setAcceptRate(Math.round(acceptRate * 10.0) / 10.0); // 保留一位小数
        
        return userStats;
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 验证当前密码
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!encryptOldPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前密码错误");
        }
        
        // 2. 更新密码
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(encryptNewPassword);
        
        return this.updateById(user);
    }

    @Override
    public List<UserActivityVO> getUserActivities(Long userId) {
        List<UserActivityVO> activities = new ArrayList<>();
        
        // 查询最近的提交记录作为活动记录
        List<UserActivityVO> submitActivities = userMapper.getUserRecentSubmits(userId, 10);
        activities.addAll(submitActivities);
        
        // 可以添加其他类型的活动记录，比如登录记录等
        // 这里暂时只返回提交记录
        
        return activities;
    }

    @Override
    public SystemStatsVO getSystemStats() {
        SystemStatsVO stats = new SystemStatsVO();
        
        // 统计题目总数
        long questionCount = questionMapper.selectCount(null);
        stats.setQuestionCount(questionCount);
        
        // 统计用户总数
        long userCount = this.count();
        stats.setUserCount(userCount);
        
        // 统计提交总数
        long submitCount = questionSubmitMapper.selectCount(null);
        stats.setSubmitCount(submitCount);
        
        return stats;
    }
}
