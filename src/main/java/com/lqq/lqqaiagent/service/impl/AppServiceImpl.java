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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lqq.lqqaiagent.constant.AppConstant.APP_LIST_CACHE_KEY;
import static com.lqq.lqqaiagent.constant.AppConstant.LIST_CACHE_EXPIRE_TIME;

/**
* @author LQQ
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2025-10-14 22:53:12
*/
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String APP_CACHE_KEY = "app:";
    private static final long CACHE_EXPIRE_TIME = 30; // 缓存过期时间（分钟）

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

    /**
     * 根据 ID 获取应用（带缓存）
     * 
     * 缓存策略：
     * - 缓存 Key：app:{id}
     * - 过期时间：30 分钟
     * - 缓存穿透防护：缓存空对象 5 分钟
     * 
     * @param id 应用 ID
     * @return 应用信息（脱敏后）
     */
    @Override
    public AppVO getAppByIdWithCache(Long id) {
        if (id == null || id <= 0) {
            return null;
        }

        String cacheKey = APP_CACHE_KEY + id;

        // 1. 查询缓存
        AppVO cachedAppVO = (AppVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAppVO != null) {
            log.info("缓存命中，appId={}", id);
            return cachedAppVO;
        }

        // 2. 缓存未命中，查询数据库
        App app = this.getById(id);
        if (app == null) {
            log.warn("应用不存在，appId={}", id);
            // 防止缓存穿透：缓存空对象，过期时间设置较短
            redisTemplate.opsForValue().set(cacheKey, new AppVO(), 5, TimeUnit.MINUTES);
            return null;
        }

        // 3. 转换为 VO（脱敏）
        AppVO appVO = getAppVO(app);

        // 4. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, appVO, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("数据已缓存，appId={}", id);

        return appVO;
    }

    /**
     * 新增应用时清除列表缓存
     */
    @Override
    public boolean save(App entity) {
        boolean result = super.save(entity);
        if (result) {
            // 清除列表缓存，因为新增了应用
            clearListCache();
        }
        return result;
    }

    /**
     * 更新应用时删除缓存
     */
    @Override
    public boolean updateById(App entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getId() != null) {
            // 删除应用详情缓存
            String cacheKey = APP_CACHE_KEY + entity.getId();
            redisTemplate.delete(cacheKey);
            log.info("应用详情缓存已删除，appId={}", entity.getId());
            
            // 清除列表缓存，因为应用信息可能影响列表排序
            clearListCache();
        }
        return result;
    }

    /**
     * 获取热门应用列表（带缓存）
     * 
     * 缓存策略：
     * - 缓存 Key：app:list:hot
     * - 过期时间：10 分钟
     * - 查询规则：按优先级和创建时间降序，取前 10 条
     *
     * @return 热门应用列表（脱敏后）
     */
    @Override
    public List<AppVO> getHotAppListWithCache() {
        // 1. 从 Redis 查询缓存
        List<AppVO> cachedList = (List<AppVO>) redisTemplate.opsForValue().get(APP_LIST_CACHE_KEY);
        if (cachedList != null) {
            log.info("热门应用列表缓存命中");
            return cachedList;
        }

        // 2. 缓存未命中，查询数据库
        log.info("热门应用列表缓存未命中，查询数据库");
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", 0)
                .orderByDesc("priority", "create_time")
                .last("LIMIT 10");
        List<App> appList = this.list(queryWrapper);

        // 3. 转换为 VO 列表（脱敏）
        List<AppVO> appVOList = getAppVOList(appList);

        // 4. 写入缓存（10 分钟过期）
        redisTemplate.opsForValue().set(APP_LIST_CACHE_KEY, appVOList, LIST_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        log.info("热门应用列表已缓存，数量={}", appVOList.size());

        return appVOList;
    }

    /**
     * 清除应用列表缓存
     * 在应用增删改时调用，保证数据一致性
     */
    private void clearListCache() {
        redisTemplate.delete(APP_LIST_CACHE_KEY);
        log.info("应用列表缓存已清除");
    }

    /**
     * 删除应用时删除缓存
     */
    @Override
    public boolean removeById(Long id) {
        boolean result = super.removeById(id);
        if (result) {
            // 删除应用详情缓存
            String cacheKey = APP_CACHE_KEY + id;
            redisTemplate.delete(cacheKey);
            log.info("应用详情缓存已删除，appId={}", id);
            
            // 清除列表缓存，因为删除了应用
            clearListCache();
        }
        return result;
    }
}
