export type Permission = {
  id: number
  name: string
  type: string
  status: string
  createdAt: number
}

export type PermissionDetailsType = {
  id: number
  userId: string
  name: string
  status: string
  createdAt: number
  eddiePermissionId: string
  eddieDataNeedId: string
  eddieConnectorIdentifier: string
  eddieConnectorCountry: string
  meterResourceId: string
  permissionType: 'REAL_TIME_DATA' | 'VALIDATED_HISTORICAL_DATA'
  timeSeriesList: TimeSeriesList | null
  [key: string]: any
}

export type TimeSeriesList = {
  id: number
  temporalResolution: GranularityOption
  timeSeries: {
    value: number
    timestamp: number
  }[]
  unit: 'WATT' | 'KWH' | 'KILOWATT_HOUR'
}

export type GranularityOption =
  | 'PT5M'
  | 'PT10M'
  | 'PT15M'
  | 'PT30M'
  | 'PT1H'
  | 'P1D'
  | 'P1M'
  | 'P1Y'

export type AggregationOption = '1' | '15' | '30' | '60'
