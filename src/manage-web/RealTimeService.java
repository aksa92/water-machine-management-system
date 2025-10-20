package com.campus.water.web;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RealTimeService {

    private ConcurrentHashMap<String, Object> realTimeData = new ConcurrentHashMap<>();
    private List<Object> alerts = new ArrayList<>();

    public void updateRealTimeData(Object sensorData) {
        System.out.println("更新实时数据");
    }

    public void addAlert(Object alertData) {
        alerts.add(alertData);
        System.out.println("添加告警");
    }

    public Object getRealTimeData(String deviceId) {
        return realTimeData.get(deviceId);
    }

    public List<Object> getAllRealTimeData() {
        return new ArrayList<>(realTimeData.values());
    }
}