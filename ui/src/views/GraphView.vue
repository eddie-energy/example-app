<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getPermissionByID } from '@/api'
import PermissionDetails from '@/components/PermissionDetails.vue'
import type { AggregationOption, GranularityOption, PermissionDetailsType } from '@/types'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'
import CustomSelect from '@/components/CustomSelect.vue'
import BarChart from '@/components/BarChart.vue'
import PermissionStats from '@/components/PermissionStats.vue'
import { beautifyGranularityName, getDateFromUTC } from '@/helpers'

const route = useRoute()

const permission = ref<PermissionDetailsType>()
const granularity = ref<GranularityOption>()
const realtimeAggregate = ref<AggregationOption>('30')
const intervalID = ref()
const date = ref()
const UnitType = {
  WATT: 'W',
  KWH: 'KWH',
  KW: 'KW',
  KILOWATT_HOUR: 'KWH',
}

const fetchPermission = async () => {
  if (route.params.id && Number.isFinite(Number(route.params.id))) {
    permission.value = await getPermissionByID(Number(route.params.id))
    if (permission.value?.permissionType === 'REAL_TIME_DATA') {
      granularity.value = undefined
    }
  } else {
    permission.value = undefined
  }
}

onMounted(async () => {
  await fetchPermission()
  const temporalResolution = permission.value?.timeSeriesList?.temporalResolution
  granularity.value =
    temporalResolution && granularityEnum[temporalResolution] < granularityEnum.P1D
      ? 'P1D'
      : temporalResolution
  if (permission.value?.permissionType === 'REAL_TIME_DATA') {
    intervalID.value = setInterval(fetchPermission, parseInt(realtimeAggregate.value) * 1000)
  }
  const { start, end } = startEndDate.value
  if (!isNaN(start) && !isNaN(end)) {
    const diffInMs = end * 1000 - start * 1000
    const oneWeekMs = 7 * 24 * 60 * 60 * 1000
    if (diffInMs > oneWeekMs) {
      const endDate = new Date(end * 1000)
      const startDate = new Date(endDate.getTime() - oneWeekMs)
      startDate.setHours(0, 0, 0, 0)
      endDate.setHours(23, 59, 59, 999)
      date.value = [startDate, endDate]
    }
  }
})

watch([realtimeAggregate], () => {
  clearInterval(intervalID.value)
  intervalID.value = setInterval(fetchPermission, parseInt(realtimeAggregate.value) * 1000)
})

const granularityEnum = {
  PT5M: 5,
  PT10M: 10,
  PT15M: 15,
  PT30M: 30,
  PT1H: 60,
  P1D: 1440,
  P1M: 43200,
  P1Y: 518400,
}

const realTimeGranularityOptions = [
  {
    label: 'realtime',
    value: '1',
  },
  {
    label: '15 seconds',
    value: '15',
  },
  {
    label: '30 seconds',
    value: '30',
  },
  {
    label: '1 minute',
    value: '60',
  },
]

const startEndDate = computed(() => {
  const timeSeries = permission.value?.timeSeriesList?.timeSeries
  if (!timeSeries || timeSeries.length === 0) {
    return {
      start: NaN,
      end: NaN,
    }
  }
  const endTimestamp = timeSeries.reduce(
    (a, b) => (a.timestamp > b.timestamp ? a : b),
    timeSeries[0],
  ).timestamp
  if (timeSeries.filter((ts) => ts.timestamp === endTimestamp).length === 1) {
    // Subtract 1 minute (60 seconds) from end timestamp
    return {
      start: timeSeries.reduce((a, b) => (a.timestamp < b.timestamp ? a : b), timeSeries[0])
        .timestamp,
      end: endTimestamp - 60,
    }
  }
  return {
    start: timeSeries.reduce((a, b) => (a.timestamp < b.timestamp ? a : b), timeSeries[0])
      .timestamp,
    end: endTimestamp,
  }
})

const aggregateData = () => {
  // temp will store sum + count for accurate averaging
  const temp: { [timestamp: string]: { sum: number; count: number } } = {}

  permission.value?.timeSeriesList?.timeSeries
    ?.sort((a, b) => a.timestamp - b.timestamp)
    ?.forEach(({ timestamp, value }) => {
      let rounded = timestamp

      if (permission.value?.permissionType === 'REAL_TIME_DATA') {
        const agg = parseInt(realtimeAggregate.value) // e.g. 30 for 30s intervals
        rounded = Math.floor(timestamp / agg) * agg
      } else if (permission.value?.permissionType === 'VALIDATED_HISTORICAL_DATA') {
        if (granularity.value === 'P1M') {
          const d = getDateFromUTC(timestamp)
          rounded = Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), 1) / 1000
        } else {
          const mins = granularityEnum[granularity.value as GranularityOption]
          const secs = mins * 60
          rounded = Math.floor(timestamp / secs) * secs
        }

        const dateAtRounded = getDateFromUTC(rounded)
        if (
          dateAtRounded.getHours() === 0 &&
          dateAtRounded.getMinutes() === 0 &&
          dateAtRounded.getSeconds() === 0
        ) {
          rounded -= 60
        }

        if (
          granularity.value !== 'P1Y' &&
          (rounded < startEndDate.value.start || rounded > startEndDate.value.end)
        ) {
          return
        }
      }

      if (!temp[rounded]) {
        temp[rounded] = { sum: 0, count: 0 }
      }

      temp[rounded].sum += value
      temp[rounded].count += 1
    })

  const aggregated: { [timestamp: string]: number } = {}
  for (const [timestamp, { sum, count }] of Object.entries(temp)) {
    aggregated[timestamp] =
      permission.value?.permissionType === 'VALIDATED_HISTORICAL_DATA' ? sum : sum / count
  }

  return aggregated
}

const filterDataBasedOnDate = (temp: { [timestamp: string]: number }) => {
  const [start, end] = date.value
  const startDate = new Date(start)
  startDate.setHours(0, 0, 0, 0)
  const startTs = Math.floor(startDate.getTime() / 1000)
  const endDate = end ? new Date(end) : startDate
  endDate.setHours(23, 59, 59, 999)
  const endTs = Math.floor(endDate.getTime() / 1000)

  if (granularity.value !== 'P1M') {
    return Object.fromEntries(
      Object.entries(temp).filter(([timestamp]) => {
        const ts = Number(timestamp)
        return ts >= startTs && ts <= endTs
      }),
    )
  }

  const startMonth = getDateFromUTC(startTs).getUTCMonth()
  const startYear = getDateFromUTC(startTs).getUTCFullYear()
  const endMonth = getDateFromUTC(endTs).getUTCMonth()
  const endYear = getDateFromUTC(endTs).getUTCFullYear()
  return Object.fromEntries(
    Object.entries(temp).filter(([timestamp]) => {
      const d = getDateFromUTC(Number(timestamp))
      const month = d.getUTCMonth()
      const year = d.getUTCFullYear()
      return (
        (year > startYear || (year === startYear && month >= startMonth)) &&
        (year < endYear || (year === endYear && month <= endMonth))
      )
    }),
  )
}

const aggregateAndFilterTimeSeries = () => {
  if (date.value) {
    return filterDataBasedOnDate(aggregateData())
  }
  return aggregateData()
}

const getGranularityOptions = (minGranularity: GranularityOption) => {
  const granularities = Object.entries(granularityEnum)
    .filter(([, value]) => value >= granularityEnum[minGranularity])
    .map(([key]) => key)

  if (!date.value) {
    return granularities.map((gran) => ({
      label: beautifyGranularityName(gran),
      value: gran,
    }))
  }

  const [start, end] = date.value
  const startDate = new Date(start)
  startDate.setHours(0, 0, 0, 0)
  const endDate = end ? new Date(end) : new Date(startDate)
  endDate.setHours(23, 59, 59)
  const diffInMinutes = Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60))
  return granularities
    .filter((gran) => granularityEnum[gran as GranularityOption] <= diffInMinutes)
    .map((gran) => ({
      label: beautifyGranularityName(gran),
      value: gran,
    }))
}
</script>

<template>
  <div v-if="permission">
    <main class="main">
      <h1 class="heading-1 title">Energy Consumption Dashboard</h1>
      <PermissionDetails :permission />
      <template v-if="permission.permissionType === 'VALIDATED_HISTORICAL_DATA'">
        <p class="heading-4 date-info">
          Data from
          <time>{{ getDateFromUTC(startEndDate.start).toLocaleString() }}</time>
          -
          <time>
            {{ getDateFromUTC(startEndDate.end).toLocaleString() }}
          </time>
        </p>
        <div class="day-filters">
          <div class="select-div">
            <p id="granularitySelect">Select Granularity:</p>
            <CustomSelect
              aria-labelledby="granularitySelect"
              :options="
                getGranularityOptions(permission.timeSeriesList?.temporalResolution ?? 'PT1H')
              "
              v-model="granularity"
              placeholder="Select Granularity"
            />
          </div>
          <div class="select-div">
            <p id="date-range">Select Date Range:</p>
            <VueDatePicker
              aria-labelledby="date-range"
              v-model="date"
              range
              :min-date="getDateFromUTC(startEndDate.start)"
              :max-date="getDateFromUTC(startEndDate.end)"
              :enable-time-picker="false"
              :placeholder="`${getDateFromUTC(startEndDate.start).toLocaleDateString()} - ${getDateFromUTC(startEndDate.end).toLocaleDateString()}`"
              class="date-picker"
            />
          </div>
        </div>
      </template>
      <template v-else>
        <p>
          Last 15 minutes of consumption | Chart updates
          {{
            realtimeAggregate === '1'
              ? 'whenever data is received'
              : `every ${realtimeAggregate} seconds`
          }}
        </p>
        <div class="aggregation-select select-div">
          <p id="aggregation-select">Select aggregation of values:</p>
          <CustomSelect
            aria-labelledby="aggregation-select"
            v-model="realtimeAggregate"
            :options="realTimeGranularityOptions"
            placeholder="Select aggregation"
          />
        </div>
      </template>

      <BarChart
        v-if="permission.timeSeriesList"
        :unit="permission.timeSeriesList?.unit ? UnitType[permission?.timeSeriesList.unit] : 'WATT'"
        :time-series="aggregateAndFilterTimeSeries()"
        :granularity
        :permission-type="permission.permissionType"
        class="chart"
      />
    </main>
    <PermissionStats
      :time-series="aggregateAndFilterTimeSeries()"
      :granularity
      :unit="permission.timeSeriesList?.unit ? UnitType[permission?.timeSeriesList.unit] : 'WATT'"
      :type="permission.permissionType"
    />
  </div>
</template>

<style scoped>
.title {
  margin-bottom: var(--spacing-xxl);
}
.date-info {
  color: var(--eddie-primary);
  font-weight: 600;
  margin-bottom: var(--spacing-md);
}
.chart {
  margin-top: var(--spacing-md);
}

.day-filters {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.filter-buttons {
  display: flex;
  gap: var(--spacing-sm);
  > button {
    width: fit-content;
  }
}

.active {
  background-color: var(--eddie-primary);
  color: var(--light);
  cursor: unset;
  pointer-events: none;
}

.aggregation-select {
  margin-top: var(--spacing-md);
}

.selection-filters {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.select-div {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.date-picker {
  --dp-font-family: 'Poppins';
  --dp-text-color: var(--dark);
  --dp-hover-color: unset;
  --dp-hover-text-color: unset;
  --dp-hover-icon-color: unset;
  --dp-primary-color: var(--eddie-primary);
  --dp-primary-disabled-color: var(--eddie-primary-400);
  --dp-primary-text-color: var(--dark);
  --dp-secondary-color: var(--eddie-grey-light);
  --dp-border-color: var(--eddie-grey-medium);
  --dp-menu-border-color: var(--eddie-grey-medium);
  --dp-border-color-hover: unset;
  --dp-border-color-focus: unset;
  --dp-border-radius: var(--border-radius);
  --dp-input-padding: var(--spacing-sm) var(--spacing-md);
  --dp-input-icon-padding: var(--spacing-xxl);
}

@media screen and (min-width: 640px) {
  .aggregation-select {
    max-width: 40vw;
  }
  .day-filters {
    display: grid;
    grid-template-columns: 1fr 1fr;
    max-width: 50%;

    margin-block: var(--spacing-lg);
  }
}

.v-enter-active,
.v-leave-active {
  transition: opacity 0.3s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
