package com.lqq.lqqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lqq.lqqaiagent.model.dto.app.AppAdminQueryRequest;
import com.lqq.lqqaiagent.model.entity.App;
import com.lqq.lqqaiagent.model.vo.AppVO;

import java.util.List;

/**
* @author LQQ
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2025-10-14 22:53:12
*/
public interface AppService extends IService<App> {

    /**
     * 构建管理员端应用查询条件（除时间字段外）
     */
    QueryWrapper<App> getAdminQueryWrapper(AppAdminQueryRequest request);

    /**
     * 脱敏/封装 AppVO
     */
    AppVO getAppVO(App app);

    /**
     * 批量封装 AppVO 列表
     */
    List<AppVO> getAppVOList(List<App> list);
}
