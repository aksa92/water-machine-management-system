// src/api/types/workorder.ts
// src/api/types/workorder.ts
export interface WorkOrder {
  orderId: string
  deviceId: string
  areaId: string
  orderType: 'repair' | 'maintenance' | 'inspection'  // 添加此属性
  description: string
  priority: 'low' | 'medium' | 'high' | 'urgent'     // 添加此属性
  status: 'pending' | 'processing' | 'reviewing' | 'completed' | 'timeout'
  assignedRepairmanId?: string
  createdTime?: string
  grabbedTime?: string           // 添加此属性
  deadline?: string             // 添加此属性
  completedTime?: string        // 添加此属性
  dealNote?: string             // 添加此属性
  imgUrl?: string               // 添加此属性
  createdBy: string             // 添加此属性
  updatedTime?: string          // 添加此属性
  alertId?: string              // 添加此属性
}
