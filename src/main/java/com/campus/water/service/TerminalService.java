// java/com/campus/water/service/TerminalService.java
package main.java.com.campus.water.service;

import main.java.com.campus.water.entity.DeviceTerminalMapping;
import main.java.com.campus.water.entity.WaterTerminalLocation;
import main.java.com.campus.water.entity.vo.TerminalManageVO;

import java.util.List;

public interface TerminalService {
    // 新增终端（同时保存位置信息和基础映射信息）
    TerminalManageVO addTerminal(TerminalManageVO terminalVO);

    // 更新终端（支持更新名称、状态、经纬度等信息）
    TerminalManageVO updateTerminal(TerminalManageVO terminalVO);

    // 删除终端（先校验是否绑定设备，再级联删除两张表的相关数据）
    void deleteTerminal(String terminalId);

    // 按ID查询终端详情（整合两张表的数据）
    TerminalManageVO getTerminalById(String terminalId);

    // 查询终端列表（支持按名称筛选，返回整合后的数据）
    List<TerminalManageVO> getTerminalList(String terminalName);
}