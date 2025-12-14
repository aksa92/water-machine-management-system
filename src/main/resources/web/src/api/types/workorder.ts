// src/api/types/workorder.ts
export interface WorkOrder {
  orderId: string
  deviceId: string
  areaId: string
  description: string
  status: 'pending' | 'processing' | 'reviewing' | 'completed' | 'timeout'
  createdTime?: string
  assignedRepairmanId?: string
}
