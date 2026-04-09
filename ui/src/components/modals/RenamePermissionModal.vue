<script setup lang="ts">

import { ref } from "vue"
import { renamePermissionByID } from '@/api'
import ModalDialog from "@/components/modals/ModalDialog.vue";

const renamePermissionModal = ref<HTMLDialogElement>()
const permissionName = ref('')

const showModal = () => {
  renamePermissionModal.value?.showModal()
}

const handleRenamePermission = async () => {
  await renamePermissionByID(id, permissionName.value)
  renamePermissionModal.value?.close()
}

const { id } = defineProps<{
  id: number
}>()

defineExpose({ showModal })

</script>

<template>
  <ModalDialog
      title="Rename Permission"
      ref="renamePermissionModal"
  >
    <form class="permission-form bottom-margin">
      <label for="code">Enter new name:</label>
      <input
          type="text"
          id="code"
          placeholder="PermissionName"
          required
          class="name-input text-normal"
          v-model="permissionName"
      />
    </form>
    <div class="action-buttons">
      <Button button-style="error-secondary" @click.stop="renamePermissionModal?.close()">
        Cancel
      </Button>
      <Button @click.stop="handleRenamePermission" class="hide-on-load">Rename</Button>
    </div>
  </ModalDialog>
</template>

<style scoped>

.bottom-margin {
  margin-bottom: var(--spacing-xxl);
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.name-input {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--eddie-grey-medium);
  border-radius: var(--border-radius);
}

.permission-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

</style>