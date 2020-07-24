package com.baseboot.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 车辆上报模式形式
 */
public enum ModeStateEnum {

    SILENT("1", "静默"),
    SELF_INSPECTION("2", "自检"),
    MANUAL_MODE("3", "手动模式"),
    SELF_MODE("4", "自动模式"),
    REMOTE_MODE("5", "远程模式"),
    ERROR("6", "异常");

    private String value;

    private String desc;

    ModeStateEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }

    public static ModeStateEnum getEnum(String value) {
        for (ModeStateEnum modeState : ModeStateEnum.values()) {
            if (modeState.getValue().equals(value)) {
                return modeState;
            }
        }
        return null;
    }
}
