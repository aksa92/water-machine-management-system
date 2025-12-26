package com.campus.water.service;

import com.campus.water.entity.vo.TerminalLocationVO;
import java.util.List;

/**
 * 终端机位置服务接口（返回整合device表的VO数据）
 */
public interface WaterTerminalLocationService {
    /**
     * 获取所有终端机位置（整合device表的安装位置、状态）
     */
    List<com.campus.water.entity.vo.TerminalLocationVO> getAllTerminalLocations();

    /**
     * 获取可用的终端机位置（仅展示正常运行的设备）
     */
    List<com.campus.water.entity.vo.TerminalLocationVO> getAvailableTerminalLocations();
}