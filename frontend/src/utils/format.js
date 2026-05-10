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

export function formatDateTime(value) {
  if (!value) {
    return '--'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'medium'
  }).format(date)
}

export function formatTransactionType(type) {
  const typeMap = {
    WITHDRAW: '取款',
    DEPOSIT: '存款',
    TRANSFER: '转账',
    1: '取款',
    2: '存款',
    3: '转账'
  }

  return typeMap[type] || type || '--'
}

export function formatTransactionStatus(status) {
  const statusMap = {
    SUCCESS: '成功',
    FAILED: '失败',
    PENDING: '待处理',
    CANCELLED: '已撤销',
    0: '待处理',
    1: '成功',
    2: '失败',
    3: '已撤销'
  }

  return statusMap[status] || status || '--'
}

