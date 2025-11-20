package com.lqq.lqqaiagent.admin.controller;

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
import com.lqq.lqqaiagent.model.dto.app.AppAdminQueryRequest;
import com.lqq.lqqaiagent.model.dto.app.AppAdminUpdateRequest;
import com.lqq.lqqaiagent.model.entity.App;
import com.lqq.lqqaiagent.model.vo.AppVO;
import com.lqq.lqqaiagent.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 后台应用管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/app")
public class AdminAppController {

    @Resource
    private AppService appService;

    /**
     * 管理员根据 id 删除任意应用（逻辑删除）
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        long id = deleteRequest.getId();
        App exist = appService.getById(id);
        ThrowUtils.throwIf(exist == null, ErrorCode.NOT_FOUND_ERROR);
        log.info("管理员删除应用，appId：{}", id);
        App update = new App();
        update.setId(id);
        update.setIsDelete(AppConstant.DELETED);
        update.setUpdateTime(new Date());
        boolean result = appService.updateById(update);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用删除成功，appId：{}", id);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 id 更新任意应用（名称、封面、优先级）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateByAdmin(@RequestBody AppAdminUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        App exist = appService.getById(request.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.NOT_FOUND_ERROR);
        App update = new App();
        update.setId(request.getId());
        update.setAppName(request.getAppName());
        update.setCover(request.getCover());
        update.setPriority(request.getPriority());
        update.setEditTime(new Date());
        boolean result = appService.updateById(update);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表（支持除时间外任何字段查询，数量不限）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppsByAdmin(@RequestBody AppAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        int pageNum = Math.max(1, request.getPageNum());
        int pageSize = Math.max(1, request.getPageSize());
        QueryWrapper<App> qw = appService.getAdminQueryWrapper(request);
        Page<App> page = appService.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(appService.getAppVOList(page.getRecords()));
        return ResultUtils.success(voPage);
    }

    /**
     * 管理员根据 id 查看应用详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getByIdAdmin(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        AppVO appVO = appService.getAppByIdWithCache(id);
        ThrowUtils.throwIf(appVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appVO);
    }
}
