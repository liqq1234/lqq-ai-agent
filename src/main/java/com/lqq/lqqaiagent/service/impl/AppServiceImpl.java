package com.lqq.lqqaiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.mapper.AppMapper;
import com.lqq.lqqaiagent.model.dto.app.AppAdminQueryRequest;
import com.lqq.lqqaiagent.model.entity.App;
import com.lqq.lqqaiagent.model.vo.AppVO;
import com.lqq.lqqaiagent.service.AppService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author LQQ
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-10-14 22:53:12
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

    @Override
    public QueryWrapper<App> getAdminQueryWrapper(AppAdminQueryRequest request) {
        QueryWrapper<App> qw = new QueryWrapper<>();
        if (request == null) {
            return qw;
        }
        Long id = request.getId();
        String appName = request.getAppName();
        String cover = request.getCover();
        String initPrompt = request.getInitPrompt();
        String codeGenType = request.getCodeGenType();
        String deployKey = request.getDeployKey();
        Integer priority = request.getPriority();
        Long userId = request.getUserId();
        Integer isDelete = request.getIsDelete();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        if (id != null) {
            qw.eq("id", id);
        }
        if (StringUtils.isNotBlank(appName)) {
            qw.like("appname", appName);
        }
        if (StringUtils.isNotBlank(cover)) {
            qw.like("cover", cover);
        }
        if (StringUtils.isNotBlank(initPrompt)) {
            qw.like("initprompt", initPrompt);
        }
        if (StringUtils.isNotBlank(codeGenType)) {
            qw.eq("codegentype", codeGenType);
        }
        if (StringUtils.isNotBlank(deployKey)) {
            qw.eq("deploykey", deployKey);
        }
        if (priority != null) {
            qw.eq("priority", priority);
        }
        if (userId != null) {
            qw.eq("userid", userId);
        }
        if (isDelete != null) {
            qw.eq("isdelete", isDelete);
        }
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            qw.orderBy(true, isAsc, sortField);
        }
        return qw;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) return null;
        AppVO vo = new AppVO();
        BeanUtil.copyProperties(app, vo);
        return vo;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(this::getAppVO).collect(Collectors.toList());
    }
}
