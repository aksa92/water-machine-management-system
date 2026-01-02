package main.java.com.campus.water.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 制水机-供水机映射工具类（替代数据库字段关联）
 */
public class DeviceMappingUtil {
    // 单例模式，避免重复初始化
    private static final DeviceMappingUtil INSTANCE = new DeviceMappingUtil();
    private final Map<String, String> makerToSupplyMap;

    private DeviceMappingUtil() {
        makerToSupplyMap = new HashMap<>();
        // 配置制水机→供水机的关联关系（根据实际业务调整）
        makerToSupplyMap.put("WM001", "WS001");
        makerToSupplyMap.put("WM002", "WS002");
        makerToSupplyMap.put("WM003", "WS003");
        makerToSupplyMap.put("WM004", "WS004");
    }

    /**
     * 根据制水机ID获取对应的供水机ID
     * @param makerDeviceId 制水机ID（WM开头）
     * @return 供水机ID（WS开头），无关联则返回null
     */
    public static String getSupplyDeviceId(String makerDeviceId) {
        if (makerDeviceId == null) {
            return null;
        }
        return INSTANCE.makerToSupplyMap.get(makerDeviceId);
    }

    // 可选：动态添加/删除映射关系（便于扩展）
    public static void addMapping(String makerDeviceId, String supplyDeviceId) {
        INSTANCE.makerToSupplyMap.put(makerDeviceId, supplyDeviceId);
    }

    public static void removeMapping(String makerDeviceId) {
        INSTANCE.makerToSupplyMap.remove(makerDeviceId);
    }
}