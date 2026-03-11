<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
const { buttonStyle = 'primary' } = defineProps<{
  buttonStyle?: 'primary' | 'secondary' | 'error' | 'error-secondary'
}>()
</script>

<template>
  <button class="button" :class="[buttonStyle]">
    <slot />
  </button>
</template>

<style scoped>
.button {
  --button-block-padding: var(--spacing-sm);
  --button-color: var(--light);
  --button-bg-color: var(--eddie-primary);

  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--button-block-padding) var(--spacing-lg);
  transition:
    background-color 0.3s ease-in-out,
    color 0.3s ease-in-out;
  cursor: pointer;
  color: var(--button-color);
  background-color: var(--button-bg-color);
  border: 1px solid var(--button-bg-color);
  border-radius: 2rem;
  font-weight: 600;
  width: 100%;
  height: fit-content;
  font-family: 'Poppins';
  font-size: 1rem;
  line-height: 1.5;
  text-wrap: nowrap;

  &:hover {
    color: var(--button-bg-color);
    background-color: var(--button-color);
  }

  &:disabled {
    color: var(--eddie-grey-medium);
    border-color: var(--eddie-grey-medium);
    background-color: var(--eddie-secondary);
  }
}

.secondary {
  --button-bg-color: var(--light);
  --button-color: var(--eddie-primary);
  border-color: var(--button-color);
}

.error {
  --button-bg-color: var(--eddie-red-medium);
  --button-color: var(--light);
}

.error-secondary {
  --button-bg-color: var(--light);
  --button-color: var(--eddie-red-medium);
  border-color: var(--button-color);
}
</style>
