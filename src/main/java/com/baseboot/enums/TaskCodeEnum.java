package com.baseboot.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 当前上报任务编号
 * */
public enum  TaskCodeEnum{

    TASKLAUNCHMOTOR("1","启动发动机"),
    TASKSELFCHECK("2","VAK自检"),
    TASKSILENCE("3","静默（人工模式）"),
    TASKSTATICTEST("4","VAK静态测试"),
    TASKSTANDBY("5","VAK待机"),
    TASKDRIVING("6","VAK轨迹跟随"),
    TASKUNLOADMINE("7","VAK卸矿"),
    TASKUNLOADSOIL("8","VAK排土"),
    TASKREMOTECONTROL("9","VAK远程操控"),
    TASKDATASAVE("10","VAK数据上传"),
    TASKCLOSEMOTOR("11","关闭发动机"),
    TASKEMERGENCYPARKBYLINE("12","直线急停"),
    TASKEMERGENCYPARKBYTRAJECTORY("13","原路径急停"),
    TASKNORMALPARKBYTRAJECTORY("14","原路径安全停车"),
    TASKFORCELAUNCH("15","强制启动"),
    TASKCLEAREMERGENCYPARK("16","清除紧急停车");

    private String value;

    private String desc;

    TaskCodeEnum(String value, String desc){
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

    public static TaskCodeEnum getEnum(String value){
        for (TaskCodeEnum taskCodeEnum : TaskCodeEnum.values()) {
            if(taskCodeEnum.getValue().equals(value)){
                return taskCodeEnum;
            }
        }
        return null;
    }
}
