<!-- src/views/area/AreaManagement.vue -->
<template>
  <div class="area-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>片区管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 区域管理 / 片区管理</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <!-- 新增片区按钮 -->
      <button class="btn-add" @click="handleAddArea">新增片区</button>
      
      <!-- 片区下拉筛选 -->
      <div class="filter-box">
        <label>选择片区：</label>
        <select v-model="selectedArea" @change="handleAreaChange" class="area-select">
          <option value="">全部片区</option>
          <!-- 核心修改：移除下拉选项中的设备数量展示，仅保留片区名称 -->
          <option v-for="area in areaList" :key="area.id" :value="area.id">
            {{ area.name }}
          </option>
        </select>
      </div>
    </div>

    <!-- 片区表格 -->
    <div class="card">
      <table class="area-table">
        <thead>
          <tr>
            <th>片区</th>
            <th>设备数量</th>
            <th>范围</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="area in filteredAreas" :key="area.id">
            <td>{{ area.name }}</td>
            <td>{{ area.deviceCount }}</td>
            <td>{{ area.range }}</td>
            <td class="operation-buttons">
              <button 
                class="btn-delete" 
                @click="handleDelete(area.id)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredAreas.length === 0">
            <td colspan="4" class="no-data">暂无片区数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1"
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
      </span>
      <button 
        class="page-btn" 
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 新增/编辑片区弹窗 -->
    <div class="modal-mask" v-if="showModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑片区' : '新增片区' }}</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSave">
            <div class="form-item">
              <label>片区名称：</label>
              <input 
                type="text" 
                v-model="formData.name" 
                placeholder="请输入片区名称"
                required
              >
            </div>
            <div class="form-item">
              <label>片区范围：</label>
              <textarea 
                v-model="formData.range" 
                placeholder="请输入片区范围描述"
                rows="3"
                required
              ></textarea>
            </div>
            <div class="form-item">
              <label>设备数量：</label>
              <input 
                type="number" 
                v-model="formData.deviceCount" 
                placeholder="请输入设备数量"
                min="0"
                required
              >
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="showModal = false">取消</button>
              <button type="submit" class="btn-submit">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-mask" v-if="showDeleteConfirm">
      <div class="modal-container confirm-modal">
        <div class="modal-header">
          <h3>确认删除</h3>
          <button class="close-btn" @click="showDeleteConfirm = false">×</button>
        </div>
        <div class="modal-body">
          <p>确定要删除 "{{ deleteAreaName }}" 片区吗？此操作不可撤销。</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
            <button type="button" class="btn-delete-confirm" @click="confirmDelete">确认删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 片区数据接口
interface Area {
  id: string
  name: string
  deviceCount: number
  range: string
}

// 模拟片区数据
const areaList = ref<Area[]>([
  {
    id: '1',
    name: '市区东区',
    deviceCount: 28,
    range: '东至东风路，西至解放路，南至人民路，北至建设路'
  },
  {
    id: '2',
    name: '市区西区',
    deviceCount: 19,
    range: '东至解放路，西至滨河路，南至青年路，北至黄河路'
  },
  {
    id: '3',
    name: '校区南区',
    deviceCount: 45,
    range: '大学校园内南部区域，包含教学楼、图书馆、学生宿舍1-8号楼'
  },
  {
    id: '4',
    name: '校区北区',
    deviceCount: 32,
    range: '大学校园内北部区域，包含实验楼、体育馆、学生宿舍9-16号楼'
  }
])

// 响应式数据
const selectedArea = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const isEdit = ref(false)
const showDeleteConfirm = ref(false)
const deleteAreaId = ref('')
const deleteAreaName = ref('')

// 表单数据
const formData = ref<Area>({
  id: '',
  name: '',
  deviceCount: 0,
  range: ''
})

// 筛选后的片区列表
const filteredAreas = computed(() => {
  let filtered = [...areaList.value]
  
  // 根据下拉选择筛选片区
  if (selectedArea.value) {
    filtered = filtered.filter(area => area.id === selectedArea.value)
  }
  
  // 分页处理
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filtered.slice(startIndex, endIndex)
})

// 总页数计算
const totalPages = computed(() => {
  const filteredCount = selectedArea.value 
    ? areaList.value.filter(area => area.id === selectedArea.value).length
    : areaList.value.length
  return Math.ceil(filteredCount / pageSize.value)
})

// 片区选择变更处理
const handleAreaChange = () => {
  currentPage.value = 1 // 重置到第一页
}

// 新增片区
const handleAddArea = () => {
  isEdit.value = false
  // 重置表单
  formData.value = {
    id: '',
    name: '',
    deviceCount: 0,
    range: ''
  }
  showModal.value = true
}

// 删除片区（先显示确认弹窗）
const handleDelete = (id: string) => {
  const area = areaList.value.find(item => item.id === id)
  if (area) {
    deleteAreaId.value = id
    deleteAreaName.value = area.name
    showDeleteConfirm.value = true
  }
}

// 确认删除
const confirmDelete = () => {
  areaList.value = areaList.value.filter(area => area.id !== deleteAreaId.value)
  showDeleteConfirm.value = false
  // 如果删除的是当前选中的片区，清空选择
  if (selectedArea.value === deleteAreaId.value) {
    selectedArea.value = ''
  }
}

// 保存片区信息
const handleSave = () => {
  if (isEdit.value) {
    // 编辑模式
    const index = areaList.value.findIndex(item => item.id === formData.value.id)
    if (index !== -1) {
      areaList.value[index] = { ...formData.value }
    }
  } else {
    // 新增模式
    const newArea: Area = {
      ...formData.value,
      id: Date.now().toString() // 生成唯一ID
    }
    areaList.value.unshift(newArea)
  }
  showModal.value = false
}
</script>

<style scoped>
/* 基础样式 - 与人员管理页面保持一致 */
.area-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.btn-add {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-add:hover {
  background: #359e75;
}

.filter-box {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.area-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
  font-size: 14px;
}

/* 表格样式 */
.area-table {
  width: 100%;
  border-collapse: collapse;
}

.area-table th,
.area-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.area-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.area-table tbody tr:hover {
  background-color: #f8f9fa;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.btn-delete {
  background-color: #ffebe6;
  color: #cf1322;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
}

.btn-delete:hover {
  opacity: 0.9;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.page-btn {
  padding: 4px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.confirm-modal {
  width: 400px;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 16px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-delete-confirm {
  background: #cf1322;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-box {
    width: 100%;
  }
  
  .area-select {
    width: 100%;
  }
}
</style>