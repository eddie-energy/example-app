<script setup lang="ts">

import {onMounted, ref} from "vue"
import { removePermissionByID } from '@/api'
import ModalDialog from "@/components/modals/ModalDialog.vue";
import CustomButton from "@/components/CustomButton.vue";

const deletePermissionModal = ref<HTMLDialogElement>()
const permissionName = ref('')

const showModal = () => {
  console.log("Delete Permission Component Show Modal")
  deletePermissionModal.value?.showModal()
}

const handleDeletePermission = async () => {
  await removePermissionByID(id)
  deletePermissionModal.value?.close()
}

const { id, name } = defineProps<{
  id: number
  name: string
}>()

const emit = defineEmits(['close'])

defineExpose({ showModal })

onMounted(() => {
  permissionName.value = name
})

</script>

<template>
  <ModalDialog
      title="Delete Permission"
      ref="deletePermissionModal"
      @close="$emit('close')"
  >
    <p class="bottom-margin">Are you sure you want to delete {{name}}?</p>

    <div class="action-buttons">
      <CustomButton button-style="error-secondary" @click.stop="deletePermissionModal?.close()">
        Cancel
      </CustomButton>
      <CustomButton @click.stop="handleDeletePermission" class="hide-on-load">
        Delete
      </CustomButton>
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

</style>