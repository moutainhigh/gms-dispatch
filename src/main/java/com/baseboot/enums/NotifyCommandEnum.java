package com.baseboot.enums;

public enum NotifyCommandEnum {

    NONE_COMMAND,
    EXCAVATOR_INOTSIGN_COMMAND,//进车
    EXCAVATOR_OUTSIGN_COMMAND,//出车
    EXCAVATOR_BEGIN_LOAD_COMMAND,//开装
    UNLOAD_AREA_STATE_NO,//卸载区打开通知
    VEHICLE_AUTOLOAD_COMMAND,//装载指令
    VEHICLE_UNLOAD_START_COMMAND,//卸载指令
    VEHICLE_UNLOAD_END_COMMAND;//卸载完成
}