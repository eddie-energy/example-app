<script setup lang="ts">
import type { PermissionDetailsType } from '@/types'
import ChevronRightIcon from '@/assets/icons/ChevronRightIcon.svg'
import { ref } from 'vue'
import { beautifyGranularityName } from '@/helpers'

const { permission } = defineProps<{
  permission: PermissionDetailsType
}>()

const isOpen = ref(false)
</script>

<template>
  <div class="permission-details">
    <header class="permission-dropdown" @click="isOpen = !isOpen">
      <h2 class="heading-4">Info for Permission {{ permission.name }}</h2>
      <ChevronRightIcon class="chevron" :class="{ rotate: isOpen }" />
    </header>
    <Transition>
      <dl class="permission-info" v-if="isOpen">
        <div>
          <dt>Permission ID</dt>
          <dd>{{ permission.eddiePermissionId }}</dd>
        </div>
        <div>
          <dt>Type</dt>
          <dd>{{ permission.permissionType }}</dd>
        </div>
        <div>
          <dt>Minimum Granularity</dt>
          <dd>
            {{
              permission.permissionType === 'REAL_TIME_DATA'
                ? 'realtime'
                : beautifyGranularityName(permission.timeSeriesList?.temporalResolution)
            }}
          </dd>
        </div>
        <div>
          <dt>Status</dt>
          <dd>{{ permission.status }}</dd>
        </div>
        <div>
          <dt>Connector</dt>
          <dd>{{ permission.eddieConnectorIdentifier }}</dd>
        </div>
        <div>
          <dt>Connector Country</dt>
          <dd>{{ permission.eddieConnectorCountry }}</dd>
        </div>
        <div>
          <dt>Meter Resource ID</dt>
          <dd>{{ permission.meterResourceId }}</dd>
        </div>
        <div>
          <dt>Data Need ID</dt>
          <dd>{{ permission.eddieDataNeedId }}</dd>
        </div>
      </dl>
    </Transition>
  </div>
</template>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}

.permission-details {
  margin-bottom: var(--spacing-xxl);
}

.permission-dropdown {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
  width: fit-content;
}
.chevron {
  transform: rotate(90deg);
  transition: transform 0.3s ease-in-out;
  width: 1rem;
  margin: 0.5rem;
  &.rotate {
    transform: rotate(-90deg);
  }
}
.permission-info {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--spacing-sm) var(--spacing-md);
  margin-top: var(--spacing-sm);
  > div {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-sm);
    border: 1px solid var(--eddie-primary);
    border-radius: var(--border-radius);
    padding: var(--spacing-xs) var(--spacing-sm);
    justify-content: space-between;
    > dt {
      font-weight: 600;
      &::after {
        content: ' :';
      }
    }
  }
}

@media screen and (min-width: 1024px) {
  .permission-info {
    grid-template-columns: 1fr 1fr;

    > div {
      flex-direction: row;
      gap: var(--spacing-md);
    }
  }
}
</style>
