<script setup lang="ts">
import CustomButton from '@/components/CustomButton.vue'
import PermissionItem from '@/components/PermissionItem.vue'
import { keycloak } from '@/keycloak'
import { fetchPermissions, permissions } from '@/stores/permissions'
import { onMounted, ref, useTemplateRef, watch } from 'vue'

const historicDataneedID = import.meta.env.VITE_EDDIE_HISTORIC_DATANEED_ID
const realtimeDataneedID = import.meta.env.VITE_EDDIE_REALTIME_DATANEED_ID

const userId = ref<string | undefined>()
const selectedTab = ref<'active' | 'complete'>('active')
const permissionCategory = ref<'VALIDATED_HISTORICAL_DATA' | 'REAL_TIME_DATA'>(
  'VALIDATED_HISTORICAL_DATA',
)
const filteredPermissions = ref(
  permissions.value
      .filter((perm) => perm.type === permissionCategory.value)
      .sort((a, b) => b.createdAt - a.createdAt)
)
const eddieButton = useTemplateRef('eddie-button')

watch([permissionCategory, permissions, selectedTab], () => {
  filteredPermissions.value = permissions.value
    .filter(
      (perm) =>
        perm.type === permissionCategory.value &&
        (selectedTab.value === 'active'
          ? perm.status !== 'Withdrawn'
          : perm.status === 'Withdrawn'),
    )
    .sort((a, b) => b.createdAt - a.createdAt)
})

onMounted(async () => {
  try {
    userId.value = (await keycloak.loadUserProfile()).id
  } catch (error) {
    console.error('Error loading Keycloak profile', error)
  }
  eddieButton.value.addEventListener('eddie-request-accepted', async () => {
    await fetchPermissions()
  })
})
</script>

<template>
  <main class="dashboard">
    <header class="header">
      <h1 class="heading-1">Data Releases</h1>
      <eddie-connect-button
        :connection-id="userId"
        :data-need-id="
          permissionCategory === 'VALIDATED_HISTORICAL_DATA'
            ? historicDataneedID
            : realtimeDataneedID
        "
        remember-permission-administrator="true"
        ref="eddie-button"
        :class="{ disabled: !userId }"
      />
    </header>
    <div>
      <div class="type-tabs">
        <button
          @click="permissionCategory = 'VALIDATED_HISTORICAL_DATA'"
          class="text-normal"
          :class="{ 'active-category': permissionCategory === 'VALIDATED_HISTORICAL_DATA' }"
        >
          Historic Validated Data
        </button>
        <button
          @click="permissionCategory = 'REAL_TIME_DATA'"
          class="text-normal"
          :class="{ 'active-category': permissionCategory === 'REAL_TIME_DATA' }"
        >
          Real Time Data
        </button>
      </div>
      <div class="list-wrapper">
        <div class="tabs">
          <CustomButton
            button-style="secondary"
            :class="{ active: selectedTab === 'active' }"
            @click="selectedTab = 'active'"
          >
            Active
          </CustomButton>
          <CustomButton
            button-style="secondary"
            :class="{ active: selectedTab === 'complete' }"
            @click="selectedTab = 'complete'"
          >
            {{ permissionCategory === 'VALIDATED_HISTORICAL_DATA' ? 'Revoked' : 'Complete' }}
          </CustomButton>
        </div>
        <TransitionGroup class="list" tag="ul" name="list">
          <PermissionItem
            :id="permission.id"
            v-for="permission in filteredPermissions"
            :key="`${permission.id}-${permission.name}`"
          />
          <h2 v-if="!filteredPermissions.length">No {{ selectedTab }} Permissions</h2>
        </TransitionGroup>
      </div>
    </div>
  </main>
</template>

<style scoped>
.dashboard {
  padding-bottom: var(--spacing-xxl);
}
.header {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: var(--spacing-xlg);
}
.tabs {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xlg);
  flex-direction: column;
}
.list-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xlg);
  border-radius: var(--border-radius);
  margin-bottom: calc(var(--mobile-header-height) / 1.5);
  background-color: var(--light);
  margin-top: var(--spacing-md);
}
.list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  position: relative;
}
.active {
  background-color: var(--eddie-primary);
  color: var(--light);
  pointer-events: none;
}

.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
  width: calc(100% - var(--spacing-xxl));
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.list-leave-active {
  position: absolute;
}

.type-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-column: span 2;
  background: #017aa026;

  border-radius: var(--border-radius) var(--border-radius) 0 0;

  button {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    text-align: left;
    color: var(--eddie-primary);
    cursor: pointer;
    border-radius: var(--border-radius) var(--border-radius) 0 0;
    padding: var(--spacing-lg) var(--spacing-xxl);
    border: 1px solid transparent;
    border-bottom: 1px solid var(--eddie-primary);
    transition:
      background-color 0.3s ease-in-out,
      border-color 0.3s ease-in-out;

    &.active-category {
      border: 1px solid var(--eddie-primary);
      border-bottom-color: transparent;
      background-color: var(--light);
      pointer-events: none;
    }
  }
}

.disabled {
  pointer-events: none;
  opacity: 40%;
}

@media screen and (min-width: 640px) {
  .tabs {
    flex-direction: row;
  }
  .header {
    flex-direction: row;
  }
}

@media screen and (min-width: 1024px) {
  .list-wrapper {
    padding: var(--spacing-lg) var(--spacing-xxl);
    display: grid;
    background-color: var(--light);
    border: 1px solid var(--eddie-primary);
    border-top: none;
    border-radius: 0 0 var(--border-radius) var(--border-radius);
    grid-template-columns: 1fr 6fr;
    grid-template-rows: min-content;
    margin-top: unset;
  }
  .tabs {
    flex-direction: column;
  }
}
</style>
