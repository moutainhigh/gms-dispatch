package com.baseboot.entry.dispatch.monitor;

import com.baseboot.entry.map.Point;

/**
 * 系统中所有具有GPS位置的对象
 * */
public interface Location {
    
    Point getCurLocation();
}
