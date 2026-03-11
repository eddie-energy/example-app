export const beautifyGranularityName = (name?: string) => {
  switch (name) {
    case 'PT5M':
      return '5 Minutes'
    case 'PT10M':
      return '10 Minutes'
    case 'PT15M':
      return '15 Minutes'
    case 'PT30M':
      return '30 Minutes'
    case 'PT1H':
      return 'Hourly'
    case 'P1D':
      return 'Daily'
    case 'P1M':
      return 'Monthly'
    case 'P1Y':
      return 'Yearly'
    default:
      return ''
  }
}

export const getDateFromUTC = (time: number) => {
  return new Date(time * 1000)
}
