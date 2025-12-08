<!-- src/views/area/CampusManagement.vue -->
<template>
  <div class="campus-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>校区管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 区域管理 / 校区管理</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <!-- 新增校区按钮 -->
      <button class="btn-add" @click="handleAddCampus">新增校区</button>
      
      <!-- 校区下拉筛选 -->
      <div class="filter-box">
        <label>选择校区：</label>
        <select v-model="selectedCampus" @change="handleCampusChange" class="campus-select">
          <option value="">全部校区</option>
          <option v-for="campus in campusList" :key="campus.id" :value="campus.id">
            {{ campus.name }}
          </option>
        </select>
      </div>
    </div>

    <!-- 校区表格（新增所属市区列） -->
    <div class="card">
      <table class="campus-table">
        <thead>
          <tr>
            <th>校区</th>
            <th>所属市区</th> <!-- 新增列 -->
            <th>设备数量</th>
            <th>范围</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="campus in filteredCampus" :key="campus.id">
            <td>{{ campus.name }}</td>
            <td>{{ getAreaName(campus.areaId) }}</td> <!-- 显示所属市区名称 -->
            <td>{{ campus.deviceCount }}</td>
            <td>{{ campus.range }}</td>
            <td class="operation-buttons">
              <button 
                class="btn-delete" 
                @click="handleDelete(campus.id)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredCampus.length === 0">
            <td colspan="5" class="no-data">暂无校区数据</td> <!-- 列数调整为5 -->
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

    <!-- 新增/编辑校区弹窗（新增所属市区选择） -->
    <div class="modal-mask" v-if="showModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑校区' : '新增校区' }}</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSave">
            <div class="form-item">
              <label>校区名称：</label>
              <input 
                type="text" 
                v-model="formData.name" 
                placeholder="请输入校区名称"
                required
              >
            </div>
            <div class="form-item">
              <label>所属市区：</label> <!-- 新增表单项 -->
              <select 
                v-model="formData.areaId" 
                class="area-select"
                required
              >
                <option value="">请选择所属市区</option>
                <option v-for="area in areaList" :key="area.id" :value="area.id">
                  {{ area.name }}
                </option>
              </select>
            </div>
            <div class="form-item">
              <label>校区范围：</label>
              <textarea 
                v-model="formData.range" 
                placeholder="请输入校区范围描述"
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
          <p>确定要删除 "{{ deleteCampusName }}" 校区吗？此操作不可撤销。</p>
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

// 市区数据接口（用于关联校区所属市区）
interface Area {
  id: string
  name: string
  deviceCount: number
  range: string
}

// 校区数据接口（新增areaId字段关联市区）
interface Campus {
  id: string
  name: string
  areaId: string // 所属市区ID
  deviceCount: number
  range: string
}

// 模拟市区数据（用于校区关联）
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
  }
])

// 模拟校区数据
const campusList = ref<Campus[]>([
  {
    id: '1',
    name: '主校区',
    areaId: '1', // 关联市区东区
    deviceCount: 89,
    range: '包含教学区、生活区、运动区，覆盖整个主校区范围'
  },
  {
    id: '2',
    name: '分校区',
    areaId: '2', // 关联市区西区
    deviceCount: 45,
    range: '包含新教学楼、实训楼、学生公寓1-5号楼'
  },
  {
    id: '3',
    name: '南校区',
    areaId: '1', // 关联市区东区
    deviceCount: 67,
    range: '包含研究生院、国际交流中心、留学生公寓'
  }
])

// 响应式数据
const selectedCampus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const isEdit = ref(false)
const showDeleteConfirm = ref(false)
const deleteCampusId = ref('')
const deleteCampusName = ref('')

// 表单数据（新增areaId字段）
const formData = ref<Campus>({
  id: '',
  name: '',
  areaId: '',
  deviceCount: 0,
  range: ''
})

// 根据市区ID获取市区名称
const getAreaName = (areaId: string) => {
  const area = areaList.value.find(item => item.id === areaId)
  return area ? area.name : '未指定'
}

// 筛选后的校区列表
const filteredCampus = computed(() => {
  let filtered = [...campusList.value]
  
  // 根据下拉选择筛选校区
  if (selectedCampus.value) {
    filtered = filtered.filter(campus => campus.id === selectedCampus.value)
  }
  
  // 分页处理
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filtered.slice(startIndex, endIndex)
})

// 总页数计算
const totalPages = computed(() => {
  const filteredCount = selectedCampus.value 
    ? campusList.value.filter(campus => campus.id === selectedCampus.value).length
    : campusList.value.length
  return Math.ceil(filteredCount / pageSize.value)
})

// 校区选择变更处理
const handleCampusChange = () => {
  currentPage.value = 1 // 重置到第一页
}

// 新增校区
const handleAddCampus = () => {
  isEdit.value = false
  // 重置表单
  formData.value = {
    id: '',
    name: '',
    areaId: '',
    deviceCount: 0,
    range: ''
  }
  showModal.value = true
}

// 删除校区（先显示确认弹窗）
const handleDelete = (id: string) => {
  const campus = campusList.value.find(item => item.id === id)
  if (campus) {
    deleteCampusId.value = id
    deleteCampusName.value = campus.name
    showDeleteConfirm.value = true
  }
}

// 确认删除
const confirmDelete = () => {
  campusList.value = campusList.value.filter(campus => campus.id !== deleteCampusId.value)
  showDeleteConfirm.value = false
  // 如果删除的是当前选中的校区，清空选择
  if (selectedCampus.value === deleteCampusId.value) {
    selectedCampus.value = ''
  }
}

// 保存校区信息
const handleSave = () => {
  if (isEdit.value) {
    // 编辑模式
    const index = campusList.value.findIndex(item => item.id === formData.value.id)
    if (index !== -1) {
      campusList.value[index] = { ...formData.value }
    }
  } else {
    // 新增模式
    const newCampus: Campus = {
      ...formData.value,
      id: Date.now().toString() // 生成唯一ID
    }
    campusList.value.unshift(newCampus)
  }
  showModal.value = false
}
</script>

<style scoped>
/* 基础样式 - 与市区管理页面完全一致 */
.campus-page {
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

.campus-select, .area-select { /* 新增area-select样式 */
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
  font-size: 14px;
}

/* 表格样式 */
.campus-table {
  width: 100%;
  border-collapse: collapse;
}

.campus-table th,
.campus-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.campus-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.campus-table tbody tr:hover {
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
.form-item textarea,
.form-item select { /* 新增select样式 */
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
  
  .campus-select, .area-select {
    width: 100%;
  }
}
</style>