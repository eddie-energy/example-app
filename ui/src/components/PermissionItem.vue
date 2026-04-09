<script setup lang="ts">
import { getPermissionByID, removePermissionByID } from '@/api'
import type { PermissionDetailsType } from '@/types'
import { onMounted, ref } from 'vue'
import PermissionIcon from '@/assets/icons/PermissionIcon.svg'
import ChevronRightIcon from '@/assets/icons/ChevronRightIcon.svg'
import TrashIcon from '@/assets/icons/TrashIcon.svg'
import PenIcon from '@/assets/icons/PenIcon.svg'
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import { getDateFromUTC } from '@/helpers'
import { fetchPermissions } from "@/stores/permissions.ts";
import RenamePermissionModal from "@/components/modals/RenamePermissionModal.vue";

const router = useRouter()
const permissionModalRef = ref<HTMLDialogElement>()

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

const removePermission = async () => {
  await removePermissionByID(id)
  await fetchPermissions()
}

const showRenamePermissionModal = async () => {
  permissionModalRef.value?.showModal()
  await fetchPermissions()
}
</script>

<template>
  <li v-if="permission" class="permission-item" @click="moveToPermissionDetailsType">
    <div class="start">
      <PermissionIcon />
      <h2 class="title">{{ permission.name }}</h2>
      <PenIcon class="pen" @click.stop="showRenamePermissionModal" />
    </div>
    <time>{{ getDateFromUTC(permission.createdAt).toLocaleString() }}</time>
    <StatusTag
      minimal-on-mobile
      :status-type="permission.status !== 'Withdrawn' ? 'healthy' : 'unhealthy'"
    >
      {{ permission.status }}
    </StatusTag>
    <TrashIcon class="trash" @click.stop="removePermission"/>
    <ChevronRightIcon class="chevron last" />
    <Teleport to="body">
      <RenamePermissionModal :id="id" ref="permissionModalRef"/>
    </Teleport>

  </li>
</template>

<style scoped>
.permission-item {
  display: grid;
  grid-template-columns: 2fr repeat(2, minmax(0, 1fr)) auto auto;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--eddie-primary);
  border-radius: var(--border-radius);
  cursor: pointer;
}

.title {
  max-width: 20vw;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.start {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.trash, .pen {
  width: 1.5rem;
  height: 1.5rem;
}
</style>
