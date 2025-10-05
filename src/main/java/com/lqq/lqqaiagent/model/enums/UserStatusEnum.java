package com.lqq.lqqaiagent.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ENABLED("enabled", "启用"),
    DISABLED("disabled", "禁用");

    private final String text;

    private final String value;

    UserStatusEnum(String code, String description) {
        this.text = code;
        this.value = description;
    }
    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserStatusEnum anEnum : UserStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
