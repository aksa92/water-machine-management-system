<template>
  <aside class="app-sidebar">
    <nav class="sidebar-nav">
      <SidebarItem 
        v-for="item in menuItems" 
        :key="item.id"
        :item="item"
        :active-item="activeItem"
        @item-click="handleItemClick"
      />
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarItem from './SidebarItem.vue'

interface MenuItem {
  id: number
  name: string
  icon: string
  active?: boolean
  children?: { name: string; route: string }[]
  route?: string
}

const route = useRoute()
const router = useRouter()

const activeItem = computed(() => {
  const currentPath = route.path
  const item = menuItems.find(item => 
    item.route === currentPath || 
    item.children?.some(child => child.route === currentPath)
  )
  return item?.id || 1
})

const menuItems: MenuItem[] = [
  { 
    id: 1, 
    name: '仪表盘', 
    icon: '📊', 
    route: '/home'
  },
  { 
    id: 2, 
    name: '设备监控', 
    icon: '🖥️', 
    route: '/home/equipment',
    children: [
      { name: '制水机', route: '/home/equipment/water-maker' },
      { name: '供水机', route: '/home/equipment/water-supplier' }
    ]
  },
  { 
    id: 3, 
    name: '工单管理', 
    icon: '📋', 
    route: '/home/work-order',
    children: [
      { name: '待抢单', route: '/home/work-order/pending' },
      { name: '超时未抢', route: '/home/work-order/timeout' },
      { name: '处理中', route: '/home/work-order/processing' },
      { name: '待审核', route: '/home/work-order/review' },
      { name: '已结单', route: '/home/work-order/completed' }
    ]
  },
  { 
    id: 4, 
    name: '人员管理', 
    icon: '👥', 
    route: '/home/personnel',
    children: [
      { name: '管理员', route: '/home/personnel/admin' },
      { name: '维修人员', route: '/home/personnel/maintenance' },
      { name: '用户', route: '/home/personnel/user' }
    ]
  },
  { 
    id: 5, 
    name: '片区', 
    icon: '🗺️', 
    route: '/home/area',
    children: [
      { name: '市区', route: '/home/area/urban' },
      { name: '校区', route: '/home/area/campus' }
    ]
  },
  { 
    id: 6, 
    name: '个人信息', 
    icon: '👤',
    route: '/home/profile'
  }
]

const handleItemClick = (itemId: number) => {
  const item = menuItems.find(m => m.id === itemId)
  if (item?.route) {
    router.push(item.route)
  }
}
</script>

<style scoped>
.app-sidebar {
  grid-area: sidebar;
  background: white;
  border-right: 1px solid #e1e8ed;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
}

.sidebar-nav {
  padding: 20px 0;
}
</style>