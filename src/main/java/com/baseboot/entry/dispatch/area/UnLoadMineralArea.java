package com.baseboot.entry.dispatch.area;

import com.baseboot.entry.global.AbstractEventPublisher;
import com.baseboot.entry.global.EventType;
import com.baseboot.entry.global.Listener;
import com.baseboot.enums.AreaTypeEnum;
import lombok.Data;

import java.util.Arrays;
import java.util.zip.ZipEntry;

@Data
public class UnLoadMineralArea extends AbstractEventPublisher implements TaskArea {

    private Integer unloadAreaId;

    private QueuePoint queuePoint;

    private UnloadPoint[] unloadPoints;

    private UnLoadMineralAreaStateEnum status = UnLoadMineralAreaStateEnum.ON;//卸矿区状态

    private AreaTypeEnum unloadMineralType = AreaTypeEnum.UNLOAD_MINERAL_AREA;

    @Override
    public Integer getTaskAreaId() {
        return unloadAreaId;
    }

    @Override
    public AreaTypeEnum getAreaType() {
        return unloadMineralType;
    }


    /**
     * 卸矿区状态改变
     * */
    @Override
    public void eventPublisher(EventType eventType, Object value) {

    }

    @Override
    public void updateCache() {

    }


    public enum UnLoadMineralAreaStateEnum {

        ON,//开
        OFF//关
    }

    @Override
    public String toString(){
        return unloadMineralType.getDesc()+":areaId="+unloadAreaId+",queuePoint=["+(null==queuePoint?"":queuePoint.toString())+"],unloadPoints="+ (null==unloadPoints?"":Arrays.toString(unloadPoints));
    }
}
