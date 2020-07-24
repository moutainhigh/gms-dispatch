package com.baseboot.enums;

import com.baseboot.entry.global.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;


public enum VehicleStateEnum implements IEnum<String> {

    INACTIVATE("0","初始状态，未连接"),
    RUNNING("1","下发轨迹，正在运行"),
    STOP("2","不下发轨迹，停止状态");

    private String value;

    private String desc;

    VehicleStateEnum(String value, String desc){
        this.value=value;
        this.desc=desc;
    }

    @JsonValue
    public String getValue(){
        return this.value;
    }

    public String getDesc(){
        return this.desc;
    }
}
