package com.baseboot.entry.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 坐标点及其横摆角
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IdPoint {

    public long id;

    private double x;

    private double y;

    private double z;

    @JsonProperty("yaw_Angle")
    private double yawAngle;
}
