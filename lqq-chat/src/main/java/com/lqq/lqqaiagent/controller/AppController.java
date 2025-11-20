package com.lqq.lqqaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqq.lqqaiagent.annotation.AuthCheck;
import com.lqq.lqqaiagent.common.BaseResponse;
import com.lqq.lqqaiagent.common.DeleteRequest;
import com.lqq.lqqaiagent.common.ResultUtils;
import com.lqq.lqqaiagent.constant.AppConstant;
import com.lqq.lqqaiagent.constant.UserConstant;
import com.lqq.lqqaiagent.exception.ErrorCode;
import com.lqq.lqqaiagent.exception.ThrowUtils;
import com.lqq.lqqaiagent.model.dto.app.*;
import com.lqq.lqqaiagent.model.entity.App;
import com.lqq.lqqaiagent.model.entity.User;
import com.lqq.lqqaiagent.model.vo.AppVO;
import com.lqq.lqqaiagent.service.AppService;
import com.lqq.lqqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // 用户创建应用（须填写 initPrompt）
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(request.getInitPrompt()), ErrorCode.PARAMS_ERROR, "initPrompt 必填");
        ThrowUtils.throwIf(StringUtils.isBlank(request.getAppName()), ErrorCode.PARAMS_ERROR, "应用名称必填");
        User loginUser = userService.getLoginUser(httpRequest);
        log.info("用户创建应用，userId：{}，应用名称：{}", loginUser.getId(), request.getAppName());

        App app = new App();
        app.setAppName(request.getAppName());
        app.setCover(request.getCover());
        app.setInitPrompt(request.getInitPrompt());
        app.setCodeGenType(request.getCodeGenType());
        app.setPriority(AppConstant.DEFAULT_PRIORITY);
        app.setUserId(loginUser.getId());
        Date now = new Date();
        app.setCreateTime(now);
        app.setUpdateTime(now);
        app.setEditTime(now);
        app.setIsDelete(AppConstant.NOT_DELETED);
        boolean saved = appService.save(app);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，appId：{}", app.getId());
        return ResultUtils.success(app.getId());
    }

    // 用户根据 id 修改自己的应用（仅名称）
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateMyRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(request.getAppName()), ErrorCode.PARAMS_ERROR, "应用名称必填");
        User loginUser = userService.getLoginUser(httpRequest);
        App exist = appService.getById(request.getId());
        ThrowUtils.throwIf(exist == null || exist.getIsDelete() != null && exist.getIsDelete() == 1, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH);
        App update = new App();
        update.setId(request.getId());
        update.setAppName(request.getAppName());
        update.setEditTime(new Date());
        boolean result = appService.updateById(update);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // 用户根据 id 删除自己的应用（逻辑删除）
    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        long id = deleteRequest.getId();
        App exist = appService.getById(id);
        ThrowUtils.throwIf(exist == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH);
        App update = new App();
        update.setId(id);
        update.setIsDelete(AppConstant.DELETED);
        update.setUpdateTime(new Date());
        boolean result = appService.updateById(update);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // 用户根据 id 查看应用详情（仅能看自己的）
    @GetMapping("/get/my")
    public BaseResponse<AppVO> getMyAppById(@RequestParam("id") long id, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);

        // 使用缓存方法获取应用（已经是 VO，包含脱敏后的数据）
        AppVO appVO = appService.getAppByIdWithCache(id);
        ThrowUtils.throwIf(appVO == null, ErrorCode.NOT_FOUND_ERROR);

        // 权限校验：只能查看自己的应用
        ThrowUtils.throwIf(!appVO.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH);

        return ResultUtils.success(appVO);
    }

    // 用户分页查询自己的应用列表（按名称、每页最多 20 条）
    @PostMapping("/list/page/my")
    public BaseResponse<Page<AppVO>> listMyApps(@RequestBody AppQueryMyRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        int pageNum = Math.max(1, request.getPageNum());
        int pageSize = Math.min(20, Math.max(1, request.getPageSize()));
        User loginUser = userService.getLoginUser(httpRequest);

        QueryWrapper<App> qw = new QueryWrapper<>();
        qw.eq("userid", loginUser.getId());
        qw.eq("isdelete", AppConstant.NOT_DELETED);
        if (StringUtils.isNotBlank(request.getAppName())) {
            qw.like("appname", request.getAppName());
        }
        // 默认按更新时间倒序
        qw.orderByDesc("updatetime");

        Page<App> page = appService.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<AppVO> voList = appService.getAppVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    // 用户分页查询精选应用列表（按名称、每页最多 20 条）
    @PostMapping("/list/page/featured")
    public BaseResponse<Page<AppVO>> listFeaturedApps(@RequestBody AppQueryFeaturedRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        int pageNum = Math.max(1, request.getPageNum());
        int pageSize = Math.min(20, Math.max(1, request.getPageSize()));

        QueryWrapper<App> qw = new QueryWrapper<>();
        qw.eq("isdelete", AppConstant.NOT_DELETED);
        if (StringUtils.isNotBlank(request.getAppName())) {
            qw.like("appname", request.getAppName());
        }
        // 精选：按优先级倒序，其次更新时间倒序
        qw.orderByDesc("priority").orderByDesc("updatetime");

        Page<App> page = appService.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<AppVO> voList = appService.getAppVOList(page.getRecords());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    // 获取热门应用列表（首页展示，带缓存）
    @GetMapping("/list/hot")
    public BaseResponse<List<AppVO>> getHotAppList() {
        // 使用缓存方法获取热门应用列表（已经是 VO 列表，包含脱敏后的数据）
        List<AppVO> hotAppList = appService.getHotAppListWithCache();
        return ResultUtils.success(hotAppList);
    }
}
