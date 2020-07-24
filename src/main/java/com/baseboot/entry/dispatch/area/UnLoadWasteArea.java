package com.baseboot.entry.dispatch.area;

import com.baseboot.entry.global.AbstractEventPublisher;
import com.baseboot.entry.global.EventType;
import com.baseboot.entry.global.Listener;
import com.baseboot.enums.AreaTypeEnum;
import lombok.Data;

import java.util.Arrays;

@Data
public class UnLoadWasteArea extends AbstractEventPublisher implements TaskArea {

    private Integer unloadAreaId;

    private QueuePoint queuePoint;

    private UnloadPoint[] unloadPoints;

    private UnLoadWasteAreaStateEnum status = UnLoadWasteAreaStateEnum.ON;//卸土区状态

    private AreaTypeEnum unloadWasteType = AreaTypeEnum.UNLOAD_WASTE_AREA;

    @Override
    public Integer getTaskAreaId() {
        return unloadAreaId;
    }

    @Override
    public AreaTypeEnum getAreaType() {
        return unloadWasteType;
    }

    /**
     * 卸土区状态改变
     * */
    @Override
    public void eventPublisher(EventType eventType, Object value) {

    }

    @Override
    public void updateCache() {

    }


    public enum UnLoadWasteAreaStateEnum {

        ON,//开
        OFF//关
    }

    @Override
    public String toString(){
        return unloadWasteType.getDesc()+":areaId="+unloadAreaId+",queuePoint=["+(null==queuePoint?"":queuePoint.toString())+"],unloadPoints="+ (null==unloadPoints?"":Arrays.toString(unloadPoints));
    }
}
