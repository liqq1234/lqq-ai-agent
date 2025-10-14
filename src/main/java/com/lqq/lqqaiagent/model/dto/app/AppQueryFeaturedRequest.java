package com.lqq.lqqaiagent.model.dto.app;

import com.lqq.lqqaiagent.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppQueryFeaturedRequest extends PageRequest implements Serializable {
    /** 名称模糊查询 */
    private String appName;
}
