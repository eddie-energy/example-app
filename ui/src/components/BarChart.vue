<script setup lang="ts">
import { getDateFromUTC } from '@/helpers'
import type { GranularityOption } from '@/types'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  type ChartData,
} from 'chart.js'
import { onMounted, useTemplateRef } from 'vue'
import { Bar } from 'vue-chartjs'
import zoomPlugin from 'chartjs-plugin-zoom'
import CustomButton from './CustomButton.vue'
import ZoomInIcon from '@/assets/icons/ZoomInIcon.svg'
import ZoomOutIcon from '@/assets/icons/ZoomOutIcon.svg'
import HomeIcon from '@/assets/icons/HomeIcon.svg'

const { timeSeries, unit, granularity, permissionType } = defineProps<{
  timeSeries: Record<string, number>
  unit: string
  granularity?: GranularityOption
  permissionType: 'REAL_TIME_DATA' | 'VALIDATED_HISTORICAL_DATA'
}>()

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, zoomPlugin)

import type { Chart as ChartJSInstance } from 'chart.js'
import type { ComponentPublicInstance } from 'vue'

const chartRef = useTemplateRef<ComponentPublicInstance<{ chart: ChartJSInstance }>>('chart')

const getGraphData = (): ChartData<'bar'> => {
  const temp: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        label: `Energy Consumption in ${unit ?? 'WATT'}`,
        data: [],
        backgroundColor: ['rgba(1, 122, 160, 1)'],
      },
    ],
  }

  Object.keys(timeSeries).forEach((key) => {
    let label = ''
    const date = getDateFromUTC(Number(key))
    if (permissionType === 'REAL_TIME_DATA') {
      label = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    } else if (granularity === 'PT1H') {
      label = `${date.toLocaleDateString()} ${date.toLocaleString([], { hour: '2-digit', minute: '2-digit' })}`
    } else if (granularity === 'P1D') {
      label = date.toLocaleDateString()
    } else if (granularity === 'P1M') {
      label = date.toLocaleString([], { month: 'short', year: 'numeric' })
    } else if (granularity === 'P1Y') {
      label = date.getFullYear().toString()
    } else {
      label = date.toLocaleString()
    }
    temp.labels?.push(label)
    temp.datasets[0].data.push(timeSeries[key])
  })
  return temp
}

onMounted(() => {
  //Imperfect solution for super large screens
  if (window.innerWidth >= 2565) {
    ChartJS.defaults.font.size = 24
  }
})
const resetZoom = () => {
  chartRef.value?.chart?.resetZoom?.()
}
const zoomIn = () => {
  chartRef.value?.chart?.zoom?.({ x: 1.25, y: 0 }, 'active')
}
const zoomOut = () => {
  chartRef.value?.chart?.zoom?.({ x: 0.75, y: 0 }, 'active')
}
</script>

<template>
  <div>
    <div class="zoom-controls">
      <CustomButton
        @click="resetZoom"
        class="zoom"
        aria-label="Reset Zoom"
        button-style="secondary"
      >
        <HomeIcon />
      </CustomButton>
      <CustomButton
        @click="zoomOut"
        class="zoom"
        aria-label="Zoom out of Graph"
        button-style="secondary"
      >
        <ZoomOutIcon />
      </CustomButton>
      <CustomButton
        @click="zoomIn"
        class="zoom"
        aria-label="Zoom into Graph"
        button-style="secondary"
      >
        <ZoomInIcon />
      </CustomButton>
    </div>
    <Bar
      ref="chart"
      :data="getGraphData()"
      :options="{
        responsive: true,
        indexAxis: 'x',
        plugins: {
          legend: {
            position: 'bottom',
          },
          zoom: {
            zoom: {
              wheel: {
                enabled: true,
              },
              mode: 'x',
            },
            pan: {
              enabled: true,
              mode: 'x',
            },
          },
        },
        scales: {
          x: {
            position: 'top',
            ticks: {
              autoSkip: true,
              maxTicksLimit: 12,
            },
          },
        },
      }"
    />
  </div>
</template>

<style scoped>
.zoom {
  width: fit-content;
  padding: var(--spacing-sm);
  border-radius: 0.5rem;
}
.zoom-controls {
  align-items: center;
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}
</style>
