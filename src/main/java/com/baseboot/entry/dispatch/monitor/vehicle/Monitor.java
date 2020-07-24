package com.baseboot.entry.dispatch.monitor.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class Monitor implements Serializable {

    /**
     * 报文产生设备编号,代表了当前报文所属设备
     */

    private Integer msgProdDevCode;

    /**
     * 矿用自卸车编号(1-9999)
     */

    private Integer fromVakCode = 0;


    private Integer year;

    private Integer month;

    private Integer day;

    private Integer hour;

    private Integer minute;

    private Float second;

    /**
     * 锁定设备编号,VAK当前所执行任务的来源设备
     */

    private Integer lockedDeviceCode;

    /**
     * 监控数据类型,1级还是2级
     */

    private Integer monitorDataType;

    /**
     * 车辆模式编号
     */

    private Integer vakMode;

    /**
     * 当前任务编号
     */
    private Integer currentTaskCode;

    /**
     * 轨迹编号
     */
    private Integer trackCode;

    /**
     * 车载请求编号
     */

    private Integer vakRequestCode;

    /**
     * 车辆当前挡位
     */

    private Integer currentGear;

    /**
     * GNSS状态
     */
    private Integer gnssState;

    /**
     * 经度
     */

    private Double longitude;

    /**
     * 纬度
     */

    private Double latitude;

    /**
     * 大地坐标系x坐标 单位米
     */

    private Double xworld;

    /**
     * 大地坐标系y坐标 单位米
     */

    private Double yworld;

    /**
     * 局部坐标系x坐标 单位米
     */

    private Double xLocality;

    /**
     * 局部坐标系y坐标 单位米
     */

    private Double yLocality;

    /**
     * 横摆角  单位度
     */

    private Double yawAngle;

    /**
     * 航向角  单位度
     */

    private Double navAngle;

    /**
     * 前轮转向角  单位度
     */

    private Double wheelAngle;

    /**
     * 车辆速度  单位度
     */

    private Double curSpeed;

    /**
     * 车辆加速度  单位度
     */

    private Double addSpeed;

    /**
     * 故障数量
     */
    private Integer countofObstacle;

    /**
     * 故障结构体数组
     */
    private Obstacle[] vecObstacle = {};

    /**
     * 实际方向盘转角  deg
     */

    private Double realSteerAngle;

    /**
     * 实际方向盘转速  deg/s
     */

    private Double realSteerRotSpeed;

    /**
     * 实际油门开度 %
     */

    private Double realAcceleratorRate;

    /**
     * 液压制动器主缸实际制动压力比例	%
     */

    private Double realHydBrakeRate;

    /**
     * 电磁涡流制动器实际激磁电流比例	%
     */

    private Double realElectricFlowBrakeRate;

    /**
     * 发动机状态
     */

    private Integer realMotorState;

    /**
     * 行车制动状态
     */

    private Integer realForwardBrakeState;

    /**
     * 电缓制动状态
     */

    private Integer realElectricBrakeState;

    /**
     * 停车制动状态
     */

    private Integer realParkingBrakeState;

    /**
     * 装载制动状态
     */

    private Integer realLoadBrakeState;

    /**
     * 发动机转速
     */

    private Integer realMotorRotSpeed;

    /**
     * 货舱状态
     */

    private Integer realHouseLiftRate;

    /**
     * 左转向灯状态
     */

    private Integer realTurnLeftlightState;

    /**
     * 右转向灯状态
     */

    private Integer realTurnRightlightState;

    /**
     * 近光灯状态
     */

    private Integer realNearLightState;

    /**
     * 示廓灯状态
     */

    private Integer realContourLightState;

    /**
     * 刹车灯状态
     */

    private Integer realBrakeLightState;

    /**
     * 紧急信号灯状态
     */

    private Integer realEmergencyLightState;
}
