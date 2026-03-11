<script setup lang="ts">
import { beautifyGranularityName } from '@/helpers'
import type { GranularityOption } from '@/types'
import { computed } from 'vue'

const { timeSeries, unit, granularity, type } = defineProps<{
  timeSeries: Record<string, number>
  unit?: string
  granularity?: GranularityOption
  type: 'REAL_TIME_DATA' | 'VALIDATED_HISTORICAL_DATA'
}>()

function calculateEnergyStatsFromKW(dataRecord: Record<string, number>) {
  const entries = Object.entries(dataRecord)
    .map(([timestampStr, powerKW]) => ({
      timestamp: parseInt(timestampStr) * 1000,
      power: unit === 'W' ? powerKW / 1000 : powerKW,
    }))
    .sort((a, b) => a.timestamp - b.timestamp)

  if (entries.length < 2) {
    return {
      totalEnergyKWh: 0,
      averagePowerKW: 0,
    }
  }

  let totalEnergyKWh = 0
  let totalTimeHours = 0

  for (let i = 1; i < entries.length; i++) {
    const { timestamp: t1, power: p1 } = entries[i - 1]
    const { timestamp: t2, power: p2 } = entries[i]

    const deltaTHours = (t2 - t1) / (1000 * 3600)
    const avgPower = (p1 + p2) / 2

    totalEnergyKWh += avgPower * deltaTHours
    totalTimeHours += deltaTHours
  }

  const averagePowerKW = totalEnergyKWh / totalTimeHours

  return {
    totalEnergyKWh,
    averagePowerKW,
  }
}

const stats = computed(() => {
  const granName = beautifyGranularityName(granularity) ?? ''
  const values = Object.values(timeSeries)
  if (!values.length) return undefined
  if (type === 'VALIDATED_HISTORICAL_DATA') {
    return [
      {
        name: 'Total',
        value: values.reduce((a, b) => a + b, 0),
      },
      {
        name: `${granName} Average`,
        value: (() => {
          return values.reduce((a, b) => a + b, 0) / values.length
        })(),
      },
      {
        name: `${granName} Max`,
        value: (() => {
          return Math.max(...values)
        })(),
      },
      {
        name: `${granName} Min`,
        value: (() => {
          return Math.min(...values)
        })(),
      },
    ]
  } else {
    const stats = calculateEnergyStatsFromKW(timeSeries)
    return [
      {
        name: 'Total',
        value: stats.totalEnergyKWh,
        unit: 'KWH',
      },
      {
        name: `${granName} Average`,
        value: stats.averagePowerKW,
      },
      {
        name: `${granName} Max`,
        value: (() => {
          return Math.max(...values)
        })(),
      },
      {
        name: `${granName} Min`,
        value: (() => {
          return Math.min(...values)
        })(),
      },
    ]
  }
})
</script>

<template>
  <div class="stats">
    <div class="stat-item" v-for="stat in stats" :key="stat.name">
      <h3 class="stat-title">{{ stat.name }}</h3>
      <p>
        {{
          type === 'REAL_TIME_DATA'
            ? stat.value.toFixed(4)
            : Math.round((stat.value + Number.EPSILON) * 100) / 100
        }}
      </p>
      <p>
        {{ stat.unit ? stat.unit : unit }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.stats {
  margin-top: var(--spacing-xlg);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xlg);
}
.stat-item {
  background-color: var(--light);
  color: var(--eddie-primary);
  padding: var(--spacing-xlg);
  border-radius: var(--spacing-xlg);
  font-size: 1.5rem;
  text-transform: capitalize;
}
.stat-title {
  margin-bottom: var(--spacing-xxl);
  line-height: 2rem;
  font-weight: 600;
}

@media screen and (min-width: 1024px) {
  .stats {
    grid-template-columns: 1fr 1fr 1fr 1fr;
  }
}
</style>
