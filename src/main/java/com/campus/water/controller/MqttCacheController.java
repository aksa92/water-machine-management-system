package com.campus.water.controller;

import com.campus.water.service.MqttRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mqtt/cache")
@RequiredArgsConstructor
@CrossOrigin
public class MqttCacheController {

    private final MqttRedisCacheService cacheService;

    @GetMapping("/stats")
    public MqttRedisCacheService.CacheStats getCacheStats() {
        return cacheService.getStats();
    }

    @GetMapping("/status")
    public String getStatus() {
        return "ONLINE";
    }

    @GetMapping("/queue-size")
    public int getQueueSize() {
        return cacheService.getStats().getCurrentQueueSize();
    }

    @PostMapping("/clear")
    public String clearQueue() {
        cacheService.clearQueue();
        return "队列已清空";
    }

    @GetMapping("/peek")
    public List<Object> peekMessages(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        return cacheService.peekMessages(start, end);
    }
}
