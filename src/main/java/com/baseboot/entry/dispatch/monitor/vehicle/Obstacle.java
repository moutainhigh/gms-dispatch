package com.baseboot.entry.dispatch.monitor.vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Obstacle implements Serializable {

    /**
     * 部件编码
     * */
    @JsonIgnore
    private Integer devCode;

    /**
     * 故障编码
     * */
    @JsonIgnore
    private Integer diagCode;

    private Float x;

    private Float y;

    private Float z;

    private Double length;
    private Double width;
    private Double height;
    private Integer angle;
}
