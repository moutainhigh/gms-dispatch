package com.baseboot.entry.map;

import com.baseboot.enums.AreaTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

import java.io.Serializable;

/**
 * 半静态层数据
 * */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SemiStatic implements Serializable {

    private Integer id;

    private String name;

    private AreaTypeEnum areaType;

    private Integer taskType;

    private Float speed;

    @JsonProperty(value = "queue_point")
    private IdPoint queuePoint;

    @JsonProperty(value = "task_spots")
    private TaskSpot[] taskSpots;

    @JsonProperty(value = "taskType")
    public Integer getTaskType(){
        return this.taskType;
    }

    @JsonProperty(value = "task_type")
    public void setTaskType(Integer taskType){
         this.taskType=taskType;
    }

    @Data
    public static class TaskSpot implements Serializable{

        private Integer id;

        IdPoint[] points;
    }
}
