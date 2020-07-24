package com.baseboot.enums;

import com.baseboot.entry.dispatch.area.LoadArea;
import com.baseboot.entry.dispatch.area.TaskArea;
import com.baseboot.entry.dispatch.area.UnLoadMineralArea;
import com.baseboot.entry.dispatch.area.UnLoadWasteArea;
import com.baseboot.entry.global.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 区域类型枚举
 */
public enum AreaTypeEnum implements IEnum<String> {

    ROAD("0", "路", null),
    JUNCTION("1", "连接点", null),
    UNLOAD_MINERAL_AREA("2", "卸矿区", UnLoadMineralArea.class),
    UNLOAD_WASTE_AREA("3", "卸土区", UnLoadWasteArea.class),
    PARKING_LOT("4", "停车场", null),
    PETORL_STATION("5", "加油区", null),
    WATER_STATION("6", "加水区", null),
    PASSABLE_AREA("7", "可通行区域", null),
    IMPASSABLE_AREA("8", "不可通行区域", null),
    LOAD_AREA("9", "装载区", LoadArea.class);

    private String value;

    private String desc;

    private Class<? extends TaskArea> taskAreaClass;

    private AreaTypeEnum(String value, String desc, Class<? extends TaskArea> taskAreaClass) {
        this.value = value;
        this.desc = desc;
        this.taskAreaClass = taskAreaClass;
    }

    ;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public Class<? extends TaskArea> getTaskAreaClass() {
        return taskAreaClass;
    }
}
