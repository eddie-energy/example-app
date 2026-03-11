import { getPermissions } from '@/api'
import type { Permission } from '@/types'
import { ref } from 'vue'

export const permissions = ref<Permission[]>([])

export const fetchPermissions = async () => {
  permissions.value = await getPermissions()
}
