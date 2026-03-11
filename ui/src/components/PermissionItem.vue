<script setup lang="ts">
import { getPermissionByID } from '@/api'
import type { PermissionDetailsType } from '@/types'
import { onMounted, ref } from 'vue'
import PermissionIcon from '@/assets/icons/PermissionIcon.svg'
import ChevronRightIcon from '@/assets/icons/ChevronRightIcon.svg'
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import { getDateFromUTC } from '@/helpers'

const router = useRouter()

const { id } = defineProps<{
  id: number
}>()

const permission = ref<PermissionDetailsType>()

onMounted(async () => {
  permission.value = await getPermissionByID(id)
})

const moveToPermissionDetailsType = () => {
  router.push(`/graph/${id}`)
}
</script>

<template>
  <li v-if="permission" class="permission-item" @click="moveToPermissionDetailsType">
    <div class="start">
      <PermissionIcon />
      <h2 class="title">{{ permission.permissionType }}</h2>
    </div>
    <time>{{ getDateFromUTC(permission.createdAt).toLocaleString() }}</time>
    <StatusTag
      minimal-on-mobile
      :status-type="permission.status !== 'Withdrawn' ? 'healthy' : 'unhealthy'"
    >
      {{ permission.status }}
    </StatusTag>
    <ChevronRightIcon class="chevron last" />
  </li>
</template>

<style scoped>
.permission-item {
  display: grid;
  grid-template-columns: 2fr repeat(2, minmax(0, 1fr)) auto;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--eddie-primary);
  border-radius: var(--border-radius);
  cursor: pointer;
}

.title {
  width: 20vw;
  text-overflow: ellipsis;
  overflow: hidden;
}

.start {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}
</style>
