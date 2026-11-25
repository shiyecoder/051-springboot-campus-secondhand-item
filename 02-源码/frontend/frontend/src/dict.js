/**
 * 状态字典：颜色语义与设计系统一致（ok/run/warn/bad/gray），
 * 状态不能只靠颜色表达，必须同时显示中文标签。
 */
export const DICT = {
  order_status: {
    CREATED: ['运单创建', 'warn'],
    DISPATCHED: ['调度配车', 'run'],
    LOADED: ['装货发车', 'run'],
    SHIPPING: ['在途节点', 'run'],
    ARRIVED: ['到达签收', 'warn'],
    SIGNED: ['签收回单', 'ok'],
    FEE_CHECK: ['费用核对', 'warn'],
    DONE: ['已完成', 'ok'],
    EXCEPTION: ['异常', 'bad'],
    CANCELED: ['已取消', 'gray']
  },
  demand_status: {
    SUBMITTED: ['待审核', 'warn'],
    AUDITED: ['已审核', 'run'],
    ORDERED: ['已建单', 'ok'],
    CANCELED: ['已作废', 'gray']
  },
  dispatch_status: {
    TODO: ['待处理', 'bad'],
    PROCESSING: ['处理中', 'run'],
    DONE: ['已完成', 'ok'],
    EXCEPTION: ['异常', 'bad'],
    CANCELED: ['已作废', 'gray']
  },
  vehicle_status: {
    AVAILABLE: ['可用', 'ok'],
    RUNNING: ['运输中', 'run'],
    MAINT: ['维修停用', 'warn'],
    STOP: ['已停用', 'gray']
  },
  driver_status: {
    AVAILABLE: ['可派', 'ok'],
    RUNNING: ['任务中', 'run'],
    LEAVE: ['休假', 'gray']
  },
  exception_status: {
    OPEN: ['待处理', 'warn'],
    PROCESSING: ['处理中', 'run'],
    PENDING_AUDIT: ['待经理审核', 'warn'],
    CLOSED: ['已关闭', 'ok']
  },
  exception_level: {
    LOW: ['低', 'gray'],
    MID: ['中', 'run'],
    HIGH: ['高', 'warn'],
    SEVERE: ['严重', 'bad']
  },
  receipt_status: {
    PENDING: ['待复核', 'run'],
    REJECTED: ['已退回', 'bad'],
    CHECKED: ['已复核', 'ok'],
    ARCHIVED: ['已归档', 'ok']
  },
  paper_status: {
    NONE: ['待寄回', 'gray'],
    MAILED: ['已寄出', 'run'],
    RECEIVED: ['已收到', 'ok'],
    ARCHIVED: ['已归档', 'ok']
  },
  fee_status: {
    PENDING: ['待审核', 'warn'],
    CHECKED: ['已审核', 'ok'],
    REJECTED: ['已驳回', 'bad']
  },
  bill_status: {
    UNPAID: ['未收款', 'bad'],
    PART: ['部分收款', 'warn'],
    PAID: ['已结清', 'ok'],
    CHECKED: ['已对账', 'ok']
  },
  payment_status: {
    REGISTERED: ['已登记', 'run'],
    PENDING: ['待审核', 'warn'],
    CHECKED: ['已对账', 'ok'],
    PAID: ['已付款', 'ok']
  },
  result_level: {
    RECOMMEND: ['推荐', 'ok'],
    BACKUP: ['备选', 'warn'],
    REJECT: ['不推荐', 'bad']
  },
  exception_flag: {
    NORMAL: ['正常', 'ok'],
    DELAY: ['延误', 'bad'],
    DAMAGE: ['货损', 'bad'],
    REJECT: ['拒收', 'bad'],
    OTHER: ['异常', 'bad']
  },
  review_status: {
    NORMAL: ['正常', 'ok'],
    ABNORMAL: ['异常复核', 'bad'],
    HANDLED: ['已处理', 'ok']
  },
  load_status: {
    PENDING: ['待确认', 'warn'],
    CONFIRMED: ['已确认', 'ok'],
    REJECTED: ['已退回', 'bad']
  },
  credit_status: {
    NORMAL: ['正常', 'ok'],
    WARN: ['额度预警', 'warn'],
    DISABLED: ['已停用', 'bad']
  },
  node_type: {
    LOAD: ['已装车', 'run'],
    DEPART: ['已发车', 'run'],
    TRANSIT: ['运输中', 'run'],
    ARRIVE: ['到达卸货', 'warn'],
    UNLOAD: ['卸货中', 'warn'],
    SIGN: ['签收回单', 'ok']
  }
}

export const label = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][0] : code || '-')
export const tone = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][1] : 'gray')
