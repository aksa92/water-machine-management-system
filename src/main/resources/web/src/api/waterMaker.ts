// src/api/waterMaker.ts
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'

// 设备基本信息接口
interface MachineInfo {
  deviceId: string
  model: string
  area: string
  location: string
  installDate: string
  status: 'online' | 'offline' | 'warning' | 'error'
  lastOnlineTime: string
}

// 实时数据接口
interface RealtimeData {
  tapWaterTds: number       // 自来水TDS
  pureWaterTds: number      // 纯净水TDS
  temperature: number
  pressure: number
  flow1: number
  flow2: number
  updateTime: string
}

// 滤芯状态接口
interface FilterStatus {
  id: string
  name: string
  usage: number
  remainingDays: number
}

// 历史数据记录接口
interface HistoryRecord {
  date: string                          // 日期
  tapWaterTdsAvg: number                // 自来水TDS平均值
  pureWaterTdsAvg: number               // 纯净水TDS平均值
  mineralWaterTdsAvg: number            // 矿化水TDS平均值
}

// 维护记录接口
interface MaintenanceRecord {
  orderNo: string
  maintenanceType: string
  maintainer: string
  maintenanceTime: string
  status: 'completed' | 'processing' | 'pending'
}

// API服务类
export class WaterMakerApi {
  // 获取设备基本信息
  static async getDeviceById(deviceId: string): Promise<ResultVO<MachineInfo>> {
    return await request<ResultVO<MachineInfo>>(
      `/api/web/device/${deviceId}`,
      { method: 'GET' }
    )
  }

  // 获取实时数据
  static async getRealtimeData(deviceId: string): Promise<ResultVO<RealtimeData>> {
    return await request<ResultVO<RealtimeData>>(
      `/api/web/device/${deviceId}/realtime`,
      { method: 'GET' }
    )
  }

  // 获取滤芯状态
  static async getFilterStatus(deviceId: string): Promise<ResultVO<FilterStatus[]>> {
    return await request<ResultVO<FilterStatus[]>>(
      `/api/web/device/${deviceId}/filter-status`,
      { method: 'GET' }
    )
  }

  // 获取历史数据
  static async getHistoryData(deviceId: string, date: string): Promise<ResultVO<HistoryRecord[]>> {
    return await request<ResultVO<HistoryRecord[]>>(
      `/api/web/device/${deviceId}/history?date=${date}`,
      { method: 'GET' }
    )
  }

  // 获取维护记录
  static async getMaintenanceRecords(deviceId: string): Promise<ResultVO<MaintenanceRecord[]>> {
    return await request<ResultVO<MaintenanceRecord[]>>(
      `/api/web/device/${deviceId}/maintenance-records`,
      { method: 'GET' }
    )
  }

  // 刷新设备数据
  static async refreshDeviceData(deviceId: string): Promise<ResultVO<any>> {
    return await request<ResultVO<any>>(
      `/api/web/device/${deviceId}/refresh`,
      { method: 'POST' }
    )
  }
}
