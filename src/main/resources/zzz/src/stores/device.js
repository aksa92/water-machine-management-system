import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDeviceStore = defineStore('device', () => {
  const waterSuppliers = ref([
    {
      id: 'A106',
      name: '供水机 #A106',
      location: 'A区教学楼 1楼走廊',
      status: '正常运行',
      waterLevel: 66,
      storage: 360,
      floatValves: {
        high: { status: '开启', threshold: 90 },
        low: { status: '关闭', threshold: 10 }
      },
      leakDetection: {
        status: '无漏水',
        lastChecked: '2024-12-26 10:30'
      }
    },
    // ... 更多设备数据
  ])

  const getWaterSupplierById = (id) => {
    return waterSuppliers.value.find(device => device.id === id || device.name.includes(id))
  }

  return {
    waterSuppliers,
    getWaterSupplierById
  }
})
