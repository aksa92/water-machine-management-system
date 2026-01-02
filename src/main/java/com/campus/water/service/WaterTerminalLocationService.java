package com.campus.water.service;

import com.campus.water.entity.vo.TerminalLocationVO;
import java.util.List;

/**
 * 终端位置服务接口（截图原命名）
 */
public interface WaterTerminalLocationService {
    /**
     * 获取所有终端位置
     */
    List<TerminalLocationVO> getAllTerminalLocations();

    /**
     * 获取可用终端位置
     */
    List<TerminalLocationVO> getAvailableTerminalLocations();
}