export function formatCurrency(value) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(Number(value || 0))
}

export function maskCardNo(cardNo) {
  if (!cardNo) {
    return '--'
  }

  return `${cardNo.slice(0, 4)} **** **** ${cardNo.slice(-4)}`
}

