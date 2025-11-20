package com.lqq.lqqaiagent.model.dto.app;

import com.lqq.lqqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppAdminQueryRequest extends PageRequest implements Serializable {
    private Long id;
    private String appName;
    private String cover;
    private String initPrompt;
    private String codeGenType;
    private String deployKey;
    private Integer priority;
    private Long userId;
    private Integer isDelete;
}
