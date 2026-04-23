<script setup lang="ts">
import { getPermissionByID } from '@/api'
import type { PermissionDetailsType } from '@/types'
import { onMounted, ref } from 'vue'
import PermissionIcon from '@/assets/icons/PermissionIcon.svg'
import ContextMenuIcon from '@/assets/icons/ContextMenuIcon.svg'
import { useRouter } from 'vue-router'
import StatusTag from './StatusTag.vue'
import { getDateFromUTC } from '@/helpers'
import { fetchPermissions } from "@/stores/permissions.ts";
import { onClickOutside } from '@vueuse/core'
import RenamePermissionModal from "@/components/modals/RenamePermissionModal.vue";
import DeletePermissionModal from "@/components/modals/DeletePermissionModal.vue";

const router = useRouter()
const renamePermissionModal = ref<HTMLDialogElement>()
const deletePermissionModal = ref<HTMLDialogElement>()

const { id } = defineProps<{
  id: number
}>()

const permission = ref<PermissionDetailsType>()
const openContextMenu = ref<boolean>(false)
const menuRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  permission.value = await getPermissionByID(id)
})

const moveToPermissionDetailsType = () => {
  router.push(`/graph/${id}`)
}

const showRenamePermissionModal = async () => {
  renamePermissionModal.value?.showModal()
}

const showDeletePermissionModal = async () => {
  deletePermissionModal.value?.showModal()
}

const handleModalClose = async () => {
  console.log("handleModalClose")
  openContextMenu.value = false
  await fetchPermissions()
}

onClickOutside(menuRef, () => {
  //openContextMenu.value = false
})
</script>

<template>
  <li v-if="permission" class="permission-item" @click="moveToPermissionDetailsType">
    <div class="start">
      <PermissionIcon />
      <h2 class="title">{{ permission.name }}</h2>
    </div>
    <time>{{ getDateFromUTC(permission.createdAt).toLocaleString() }}</time>
    <StatusTag
      minimal-on-mobile
      :status-type="permission.status !== 'Withdrawn' ? 'healthy' : 'unhealthy'"
    >
      {{ permission.status }}
    </StatusTag>
    <div class="menu-wrapper" ref="menuRef">
      <ContextMenuIcon @click.stop="openContextMenu = !openContextMenu" class="trigger dots" />

      <div v-if="openContextMenu" class="context-menu">
        <button @click.stop="showRenamePermissionModal">Rename</button>
        <Teleport to="body">
          <RenamePermissionModal :id="id" :name="permission.name" ref="renamePermissionModal" @close="handleModalClose"/>
        </Teleport>
        <button @click.stop="showDeletePermissionModal">Delete</button>
        <Teleport to="body">
          <DeletePermissionModal :id="id" :name="permission.name" ref="deletePermissionModal" @close="handleModalClose"/>
        </Teleport>
      </div>
    </div>

  </li>
</template>

<style scoped>
.permission-item {
  display: grid;
  grid-template-columns: 1.5fr repeat(2, minmax(0, 1fr)) auto;
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

.dots {
  width: 1.5rem;
  height: 1.5rem;
  transition: background-color 0.3s ease;
  &:hover {
    background-color: var(--eddie-turquoise-light);
  }
}

.menu-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.trigger {
  background: transparent;
  border: none;
  cursor: pointer;
}

.context-menu {
  position: absolute;
  top: 100%;
  right: 0;

  display: flex;
  flex-direction: column;

  background: white;
  border: 1px solid var(--eddie-grey-medium);
  border-radius: 6px;

  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  padding: 0.25rem;

  z-index: 100;
}

.context-menu button {
  background: none;
  border: none;
  padding: 0.5rem;
  margin-top: 0.5rem;
  text-align: left;
  cursor: pointer;
}

.context-menu button:hover {
  background: var(--eddie-turquoise-light);
  color: white;
}

</style>
