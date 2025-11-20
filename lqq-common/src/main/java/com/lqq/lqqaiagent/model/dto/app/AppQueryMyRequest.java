package com.lqq.lqqaiagent.model.dto.app;

import com.lqq.lqqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppQueryMyRequest extends PageRequest implements Serializable {
    /** 名称模糊查询 */
    private String appName;
}
