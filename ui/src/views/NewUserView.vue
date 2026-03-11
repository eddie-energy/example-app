<script setup lang="ts">
import { keycloak } from '@/keycloak'
import CustomButton from '@/components/CustomButton.vue'
import { onMounted, ref, useTemplateRef } from 'vue'
import { skipped } from '@/stores/skipped'

import { useRouter } from 'vue-router'
import { fetchPermissions } from '@/stores/permissions'

const router = useRouter()

const historicDataneedID = import.meta.env.VITE_EDDIE_HISTORIC_DATANEED_ID
const realtimeDataneedID = import.meta.env.VITE_EDDIE_REALTIME_DATANEED_ID

const userId = ref<string | undefined>()
const historicButton = useTemplateRef('historic-eddie-button')
const realtimeButton = useTemplateRef('realtime-eddie-button')

const handleEddieButtonSuccess = async () => {
  await fetchPermissions()
  router.push('/')
}

onMounted(async () => {
  try {
    userId.value = (await keycloak.loadUserProfile()).id
  } catch (error) {
    console.error('Error loading Keycloak profile', error)
  }

  historicButton.value.addEventListener('eddie-request-accepted', handleEddieButtonSuccess)
  realtimeButton.value.addEventListener('eddie-request-accepted', handleEddieButtonSuccess)
})

const handleSkip = async () => {
  skipped.value = true
  await router.push('/')
}
</script>

<template>
  <main class="main">
    <h1 class="heading-2">Select your Data!</h1>
    <p class="text-normal">
      Please select your historical or real time data to display your releases.
    </p>
    <p class="text-normal">
      You will not be able to see anything on the dashboard without connecting to EDDIE!
    </p>
    <div class="buttons">
      <div class="button-group">
        <h2 class="heading-3">Real Time Data</h2>
        <eddie-connect-button
          v-if="userId"
          :connection-id="userId"
          :data-need-id="realtimeDataneedID"
          remember-permission-administrator="true"
          ref="realtime-eddie-button"
        />
        <p class="error" v-else>There was an Error loading the EDDIE button</p>
      </div>
      <div class="button-group">
        <h2 class="heading-3">Historic Validated Data</h2>
        <eddie-connect-button
          v-if="userId"
          :connection-id="userId"
          :data-need-id="historicDataneedID"
          remember-permission-administrator="true"
          ref="historic-eddie-button"
        />
        <p class="error" v-else>There was an Error loading the EDDIE button</p>
      </div>
    </div>
    <CustomButton @click="handleSkip" class="button">Skip this step</CustomButton>
  </main>
</template>

<style scoped>
.main {
  width: fit-content;
  margin: auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);

  > * {
    display: block;
  }
}

.buttons {
  margin-top: var(--spacing-xxl);
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.button {
  text-align: center;
  margin-top: var(--spacing-md);
}

.error {
  color: var(--eddie-red-medium);
  font-weight: 600;
}
</style>
