package com.campus.water.util;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import com.campus.water.entity.Device;
import com.campus.water.entity.Device.DeviceType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 校园直饮水设备通用工具类
 * 适配设备数据处理、格式转换、参数校验等通用场景
 */
@Slf4j
public class CommonUtils {

    // ========== 设备字符串处理方法 ==========
    /**
     * 设备ID格式校验（WM/WS开头 + 3位数字）
     */
    public static boolean validateDeviceId(String deviceId) {
        if (deviceId == null || deviceId.length() != 5) {
            return false;
        }
        String prefix = deviceId.substring(0, 2);
        String suffix = deviceId.substring(2);
        if (!("WM".equals(prefix) || "WS".equals(prefix))) {
            return false;
        }
        try {
            Integer.parseInt(suffix);
            return true;
        } catch (NumberFormatException e) {
            log.warn("设备ID后缀非数字：{}", deviceId);
            return false;
        }
    }

    /**
     * 设备ID脱敏处理（如WM001 → WM*01）
     */
    public static String desensitizeDeviceId(String deviceId) {
        if (!validateDeviceId(deviceId)) {
            return deviceId;
        }
        return deviceId.substring(0, 2) + "*" + deviceId.substring(3);
    }

    /**
     * 安装位置字符串格式化（去除多余空格、统一格式）
     */
    public static String formatInstallLocation(String location) {
        if (location == null) {
            return "未配置安装位置";
        }
        // 去除首尾空格、替换全角空格、多个空格合并为一个
        String formatted = location.trim().replace("　", " ").replaceAll("\\s+", " ");
        return formatted.isEmpty() ? "未配置安装位置" : formatted;
    }

    /**
     * 设备备注信息长度限制（最多50字）
     */
    public static String limitRemarkLength(String remark) {
        if (remark == null) {
            return "";
        }
        if (remark.length() <= 50) {
            return remark;
        }
        return remark.substring(0, 50) + "...";
    }

    /**
     * 拼接设备完整名称（类型+ID+名称）
     */
    public static String concatDeviceFullName(Device device) {
        if (device == null) {
            return "";
        }
        String typeName = DeviceType.water_maker.equals(device.getDeviceType()) ? "制水机" : "供水机";
        return String.format("[%s]%s-%s", typeName, device.getDeviceId(), device.getDeviceName());
    }

    /**
     * 判断字符串是否为空白（包含全角空格）
     */
    public static boolean isBlankWithFullWidth(String str) {
        if (str == null) {
            return true;
        }
        return str.replace("　", "").trim().isEmpty();
    }

    /**
     * 字符串非空判断（包含全角空格处理）
     */
    public static boolean isNotBlankWithFullWidth(String str) {
        return !isBlankWithFullWidth(str);
    }

    /**
     * 设备状态字符串转换（枚举转中文）
     */
    public static String convertDeviceStatusToCn(String status) {
        if (status == null) {
            return "未知状态";
        }
        return switch (status) {
            case "online" -> "在线";
            case "offline" -> "离线";
            case "fault" -> "故障";
            default -> "未知状态";
        };
    }

    /**
     * 设备类型字符串转换（枚举转中文）
     */
    public static String convertDeviceTypeToCn(DeviceType type) {
        if (type == null) {
            return "未知类型";
        }
        return DeviceType.water_maker.equals(type) ? "制水机" : "供水机";
    }

    /**
     * 字符串转设备类型枚举
     */
    public static DeviceType convertStrToDeviceType(String typeStr) {
        if (typeStr == null) {
            return null;
        }
        return switch (typeStr.toLowerCase()) {
            case "water_maker", "制水机" -> DeviceType.water_maker;
            case "water_supply", "供水机" -> DeviceType.water_supply;
            default -> null;
        };
    }

    // ========== 设备数字处理方法 ==========
    /**
     * 传感器数值校验（非负）
     */
    public static boolean validateSensorValue(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * TDS值保留1位小数（适配传感器数据展示）
     */
    public static BigDecimal formatTdsValue(BigDecimal tdsValue) {
        if (!validateSensorValue(tdsValue)) {
            return BigDecimal.ZERO;
        }
        return tdsValue.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 水压值保留2位小数（适配传感器数据展示）
     */
    public static BigDecimal formatWaterPress(BigDecimal press) {
        if (!validateSensorValue(press)) {
            return BigDecimal.ZERO;
        }
        return press.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 水位值百分比格式化（0-100）
     */
    public static BigDecimal formatWaterLevel(BigDecimal level) {
        if (level == null) {
            return BigDecimal.ZERO;
        }
        if (level.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (level.compareTo(new BigDecimal(100)) > 0) {
            return new BigDecimal(100);
        }
        return level.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 温度值范围校验（0-100℃）
     */
    public static BigDecimal formatTemperature(BigDecimal temp) {
        if (temp == null) {
            return BigDecimal.ZERO;
        }
        if (temp.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (temp.compareTo(new BigDecimal(100)) > 0) {
            return new BigDecimal(100);
        }
        return temp.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 滤芯寿命百分比格式化（0-100）
     */
    public static Integer formatFilterLife(Integer life) {
        if (life == null) {
            return 0;
        }
        if (life < 0) {
            return 0;
        }
        if (life > 100) {
            return 100;
        }
        return life;
    }

    /**
     * 数字相加（Integer，空值处理）
     */
    public static Integer add(Integer num1, Integer num2) {
        if (num1 == null) num1 = 0;
        if (num2 == null) num2 = 0;
        return num1 + num2;
    }

    /**
     * 数字相加（BigDecimal，空值处理）
     */
    public static BigDecimal add(BigDecimal num1, BigDecimal num2) {
        if (num1 == null) num1 = BigDecimal.ZERO;
        if (num2 == null) num2 = BigDecimal.ZERO;
        return num1.add(num2);
    }

    /**
     * 数字相减（BigDecimal，空值处理）
     */
    public static BigDecimal subtract(BigDecimal num1, BigDecimal num2) {
        if (num1 == null) num1 = BigDecimal.ZERO;
        if (num2 == null) num2 = BigDecimal.ZERO;
        return num1.subtract(num2);
    }

    /**
     * 数字比较（BigDecimal，空值视为0）
     */
    public static int compare(BigDecimal num1, BigDecimal num2) {
        if (num1 == null) num1 = BigDecimal.ZERO;
        if (num2 == null) num2 = BigDecimal.ZERO;
        return num1.compareTo(num2);
    }

    /**
     * 获取两个数的最大值（BigDecimal）
     */
    public static BigDecimal max(BigDecimal num1, BigDecimal num2) {
        return compare(num1, num2) >= 0 ? num1 : num2;
    }

    /**
     * 获取两个数的最小值（BigDecimal）
     */
    public static BigDecimal min(BigDecimal num1, BigDecimal num2) {
        return compare(num1, num2) <= 0 ? num1 : num2;
    }

    // ========== 设备集合处理方法 ==========
    /**
     * 设备列表按类型筛选
     */
    public static List<Device> filterDeviceByType(List<Device> deviceList, DeviceType type) {
        List<Device> result = new ArrayList<>();
        if (deviceList == null || deviceList.isEmpty() || type == null) {
            return result;
        }
        for (Device device : deviceList) {
            if (type.equals(device.getDeviceType())) {
                result.add(device);
            }
        }
        return result;
    }

    /**
     * 设备列表转设备ID集合
     */
    public static Set<String> convertDeviceListToIdSet(List<Device> deviceList) {
        Set<String> idSet = new HashSet<>();
        if (deviceList == null || deviceList.isEmpty()) {
            return idSet;
        }
        for (Device device : deviceList) {
            if (validateDeviceId(device.getDeviceId())) {
                idSet.add(device.getDeviceId());
            }
        }
        return idSet;
    }

    /**
     * 设备列表按ID排序
     */
    public static List<Device> sortDeviceById(List<Device> deviceList) {
        List<Device> result = new ArrayList<>();
        if (deviceList == null || deviceList.isEmpty()) {
            return result;
        }
        result.addAll(deviceList);
        result.sort((d1, d2) -> {
            if (!validateDeviceId(d1.getDeviceId())) {
                return 1;
            }
            if (!validateDeviceId(d2.getDeviceId())) {
                return -1;
            }
            return d1.getDeviceId().compareTo(d2.getDeviceId());
        });
        return result;
    }

    /**
     * 批量获取设备名称（设备ID→名称映射）
     */
    public static Map<String, String> getDeviceNameMap(List<Device> deviceList) {
        Map<String, String> nameMap = new HashMap<>();
        if (deviceList == null || deviceList.isEmpty()) {
            return nameMap;
        }
        for (Device device : deviceList) {
            if (validateDeviceId(device.getDeviceId())) {
                nameMap.put(device.getDeviceId(), device.getDeviceName());
            }
        }
        return nameMap;
    }

    /**
     * 列表判空
     */
    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 列表非空判断
     */
    public static <T> boolean isNotEmpty(List<T> list) {
        return !isEmpty(list);
    }

    /**
     * Map判空
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 获取列表第一个元素（空值处理）
     */
    public static <T> T getFirst(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 列表去重（基于equals）
     */
    public static <T> List<T> distinct(List<T> list) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    // ========== 日期时间处理方法 ==========
    /**
     * 设备数据时间格式化（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDeviceDataTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(formatter);
    }

    /**
     * 安装日期格式化（yyyy-MM-dd）
     */
    public static String formatInstallDate(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), TimeZone.getDefault().toZoneId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDateTime.format(formatter);
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 计算两个时间的间隔（分钟）
     */
    public static long calculateMinuteInterval(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * 判断是否为今日（LocalDateTime）
     */
    public static boolean isToday(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate targetDate = time.toLocalDate();
        return today.equals(targetDate);
    }

    /**
     * 获取当日开始时间（00:00:00）
     */
    public static LocalDateTime getTodayStartTime() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * 获取当日结束时间（23:59:59）
     */
    public static LocalDateTime getTodayEndTime() {
        return LocalDate.now().atTime(23, 59, 59);
    }

    // ========== 设备数据随机生成（模拟测试用） ==========
    /**
     * 生成模拟TDS值（50-80）
     */
    public static BigDecimal generateMockTdsValue() {
        Random random = new Random();
        double value = 50 + random.nextDouble() * 30;
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 生成模拟水压值（0.1-0.5）
     */
    public static BigDecimal generateMockWaterPress() {
        Random random = new Random();
        double value = 0.1 + random.nextDouble() * 0.4;
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成模拟水位值（30-100）
     */
    public static BigDecimal generateMockWaterLevel() {
        Random random = new Random();
        double value = 30 + random.nextDouble() * 70;
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 生成模拟温度值（18-25）
     */
    public static BigDecimal generateMockTemperature() {
        Random random = new Random();
        double value = 18 + random.nextDouble() * 7;
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 生成模拟滤芯寿命（0-100）
     */
    public static Integer generateMockFilterLife() {
        Random random = new Random();
        return random.nextInt(101);
    }

    /**
     * 随机生成设备ID
     */
    public static String generateMockDeviceId(DeviceType type) {
        Random random = new Random();
        String prefix = DeviceType.water_maker.equals(type) ? "WM" : "WS";
        int num = random.nextInt(900) + 100; // 100-999
        return prefix + num;
    }

    /**
     * 随机生成设备名称
     */
    public static String generateMockDeviceName(DeviceType type) {
        String[] makerNames = {"教学楼1号制水机", "图书馆制水机", "食堂制水机", "宿舍1号楼制水机", "办公楼制水机"};
        String[] supplyNames = {"教学楼1号供水机", "图书馆供水机", "食堂供水机", "宿舍1号楼供水机", "办公楼供水机"};
        Random random = new Random();
        if (DeviceType.water_maker.equals(type)) {
            return makerNames[random.nextInt(makerNames.length)];
        } else {
            return supplyNames[random.nextInt(supplyNames.length)];
        }
    }

    // ========== 其他通用方法 ==========
    /**
     * 安全休眠（捕获中断异常）
     */
    public static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("线程休眠被中断", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 空值替换
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 生成32位随机UUID（无横线）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 设备告警级别转换（数字→中文）
     */
    public static String convertAlertLevelToCn(Integer level) {
        if (level == null) {
            return "普通";
        }
        return switch (level) {
            case 1 -> "紧急";
            case 2 -> "重要";
            case 3 -> "普通";
            default -> "普通";
        };
    }
}