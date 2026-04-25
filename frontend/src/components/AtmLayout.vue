<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'ATM Terminal'
  },
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="atm-shell">
    <div class="atm-layout">
      <section class="atm-display">
        <span class="atm-eyebrow">{{ eyebrow }}</span>
        <h1 class="atm-title">{{ title }}</h1>
        <p class="atm-subtitle">{{ subtitle }}</p>
        <div class="atm-display-slot">
          <slot name="display" />
        </div>
      </section>

      <section class="atm-panel">
        <slot />
      </section>
    </div>
  </div>
</template>

