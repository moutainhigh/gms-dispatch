package com.baseboot.entry.map;

import com.baseboot.common.utils.BaseUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Point {

    private double x;

    private double y;

    private double z;

    private double yawAngle;


    public Point() {

    }

    public Point(double x, double y, double z,double yawAngle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yawAngle = yawAngle;
    }

    @JsonProperty("yaw_angle")
    public double getYawAngle(){
        return this.yawAngle;
    }

    @Override
    public String toString(){
        return BaseUtil.format("点[{},{},{},{}]",x,y,z,yawAngle);
    }
}
