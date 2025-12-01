<template>
  <div 
    class="sidebar-item" 
    :class="{ 
      active: item.id === activeItem, 
      'has-children': item.children 
    }"
    @click="handleClick"
  >
    <div class="item-main">
      <span class="item-icon">{{ item.icon }}</span>
      <span class="item-text">{{ item.name }}</span>
      <span v-if="item.children" class="dropdown-arrow" :class="{ rotated: isExpanded }">
        ▼
      </span>
    </div>
    <div v-if="item.children && isExpanded" class="dropdown-menu">
      <div 
        v-for="(child, index) in item.children" 
        :key="index"
        class="dropdown-item"
        :class="{ active: isChildActive(child.route) }"
        @click.stop="handleChildClick(child.route)"
      >
        {{ child.name }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

interface MenuChild {
  name: string
  route: string
}

interface MenuItem {
  id: number
  name: string
  icon: string
  active?: boolean
  children?: MenuChild[]
  route?: string
}

interface Props {
  item: MenuItem
  activeItem: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  itemClick: [id: number]
}>()

const route = useRoute()
const router = useRouter()
const isExpanded = ref(false)

const isChildActive = computed(() => (childRoute: string) => {
  return route.path === childRoute
})

const handleClick = () => {
  if (props.item.children) {
    isExpanded.value = !isExpanded.value
  } else {
    emit('itemClick', props.item.id)
    if (props.item.route) {
      router.push(props.item.route)
    }
  }
}

const handleChildClick = (childRoute: string) => {
  router.push(childRoute)
}
</script>

<style scoped>
.sidebar-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.sidebar-item:hover {
  background: #f8f9fa;
}

.sidebar-item.active {
  background: #e3f2fd;
  color: #1976d2;
  border-left-color: #1976d2;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.item-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 10px;
  transition: transform 0.3s ease;
  opacity: 0.6;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  margin-top: 8px;
  margin-left: 32px;
  animation: slideDown 0.3s ease;
}

.dropdown-item {
  padding: 8px 12px;
  font-size: 13px;
  color: #666;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: #f0f0f0;
  color: #333;
}

.dropdown-item.active {
  background: #e3f2fd;
  color: #1976d2;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>