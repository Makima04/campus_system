/**
 * 日期格式化工具函数
 * @param {Date|string|number} date 日期对象、时间戳或日期字符串
 * @param {string} fmt 格式化模板，例如 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, fmt = 'YYYY-MM-DD') {
  if (!date) return ''
  
  // 如果是时间戳（数字），转换为日期对象
  if (typeof date === 'number') {
    date = new Date(date)
  } else if (typeof date === 'string') {
    // 尝试转换日期字符串为日期对象
    date = new Date(date.replace(/-/g, '/')) // 兼容Safari
  }
  
  if (!(date instanceof Date) || isNaN(date.getTime())) {
    return ''
  }
  
  const o = {
    'M+': date.getMonth() + 1, // 月份
    'D+': date.getDate(), // 日
    'H+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  }
  
  if (/(Y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  
  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1, 
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }
  
  return fmt
}

/**
 * 获取相对时间描述，例如"几分钟前"、"几小时前"、"几天前"
 * @param {Date|string|number} date 日期对象、时间戳或日期字符串
 * @returns {string} 相对时间描述
 */
export function getRelativeTime(date) {
  if (!date) return ''
  
  // 转换为时间戳
  let timestamp
  if (typeof date === 'number') {
    timestamp = date
  } else if (typeof date === 'string') {
    timestamp = new Date(date.replace(/-/g, '/')).getTime()
  } else if (date instanceof Date) {
    timestamp = date.getTime()
  } else {
    return ''
  }
  
  const currentTime = new Date().getTime()
  const timeDiff = currentTime - timestamp
  
  if (timeDiff < 0) {
    return formatDate(date, 'YYYY-MM-DD HH:mm')
  }
  
  const minute = 1000 * 60
  const hour = minute * 60
  const day = hour * 24
  const week = day * 7
  const month = day * 30
  const year = day * 365
  
  if (timeDiff < minute) {
    return '刚刚'
  } else if (timeDiff < hour) {
    return Math.floor(timeDiff / minute) + '分钟前'
  } else if (timeDiff < day) {
    return Math.floor(timeDiff / hour) + '小时前'
  } else if (timeDiff < week) {
    return Math.floor(timeDiff / day) + '天前'
  } else if (timeDiff < month) {
    return Math.floor(timeDiff / week) + '周前'
  } else if (timeDiff < year) {
    return Math.floor(timeDiff / month) + '个月前'
  } else {
    return Math.floor(timeDiff / year) + '年前'
  }
}

/**
 * 获取两个日期之间的天数
 * @param {Date|string|number} startDate 开始日期
 * @param {Date|string|number} endDate 结束日期
 * @returns {number} 天数
 */
export function getDaysBetween(startDate, endDate) {
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  // 将时间部分设为0，只比较日期部分
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  
  // 计算毫秒差，然后转换为天数
  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays
}

/**
 * 判断是否同一天
 * @param {Date|string|number} date1 日期1
 * @param {Date|string|number} date2 日期2
 * @returns {boolean} 是否是同一天
 */
export function isSameDay(date1, date2) {
  const d1 = new Date(date1)
  const d2 = new Date(date2)
  
  return d1.getFullYear() === d2.getFullYear() &&
         d1.getMonth() === d2.getMonth() &&
         d1.getDate() === d2.getDate()
} 