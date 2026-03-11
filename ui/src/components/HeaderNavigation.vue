<script setup lang="ts">
import { keycloak } from '@/keycloak'
</script>

<template>
  <header class="header">
    <RouterLink to="/">
      <img src="@/assets/EDDIE_logo_width_4C_CMYK.png" class="image" />
    </RouterLink>
    <nav class="nav">
      <RouterLink data-text="Dashboard" to="/"> Dashboard </RouterLink>
      <a @click="keycloak.logout()" data-text="Logout"> Logout </a>
    </nav>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xxl);
}

.image {
  width: 80%;
}

.nav {
  display: flex;
  gap: var(--spacing-md);
  color: var(--eddie-primary);

  a {
    cursor: pointer;
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding-bottom: var(--spacing-xs);
    border-bottom: 1px solid transparent;
    transition:
      border-color 0.3s ease-in-out,
      font-weight 0.3s ease-in-out;
  }

  a:after {
    content: attr(data-text);
    content: attr(data-text) / '';
    height: 0;
    visibility: hidden;
    overflow: hidden;
    user-select: none;
    pointer-events: none;
    font-weight: var(--font-weight-bold);

    @media speech {
      display: none;
    }
  }

  a:hover {
    border-color: var(--eddie-primary);
    font-weight: var(--font-weight-bold);
  }
}

@media screen and (min-width: 1024px) {
  .image {
    display: block;
    width: unset;
    max-height: 62px;
  }
}

@media screen and (min-width: 1920px) {
  .image {
    max-height: unset;
  }
}
</style>
