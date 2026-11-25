/**
 * 状态字典：颜色语义与设计系统一致（ok/run/warn/bad/gray）。
 * 状态不能只靠颜色表达，必须同时显示中文标签（需求 10.2）。
 */
export const DICT = {
  goods_status: {
    待审核: ['待审核', 'warn'],
    在售: ['在售', 'ok'],
    已下架: ['已下架', 'gray'],
    已售出: ['已售出', 'run'],
    审核驳回: ['审核驳回', 'bad'],
    冻结: ['冻结', 'bad']
  },
  order_status: {
    待卖家确认: ['待卖家确认', 'warn'],
    待面交: ['待面交', 'run'],
    已完成: ['已完成', 'ok'],
    已取消: ['已取消', 'gray'],
    纠纷中: ['纠纷中', 'bad']
  },
  condition_level: {
    全新: ['全新', 'ok'],
    九成新: ['九成新', 'ok'],
    七成新: ['七成新', 'run'],
    五成新: ['五成新', 'warn'],
    有瑕疵: ['有瑕疵', 'bad']
  },
  trade_type: { 面交: ['面交', 'ok'], 自提: ['自提', 'run'], 可送: ['可送', 'warn'] },
  pay_type: { 现金: ['现金', 'ok'], 转账: ['转账', 'run'], 其他: ['其他', 'gray'] },
  report_reason: {
    违禁品: ['违禁品', 'bad'], 虚假描述: ['虚假描述', 'warn'], 诈骗: ['诈骗', 'bad'],
    骚扰: ['骚扰', 'warn'], 校外人员: ['校外人员', 'bad']
  },
  report_status: {
    待受理: ['待受理', 'warn'], 处理中: ['处理中', 'run'], 已处理: ['已处理', 'ok'],
    已驳回: ['已驳回', 'gray'], 申诉中: ['申诉中', 'bad']
  },
  review_status: { 待审核: ['待审核', 'warn'], 已展示: ['已展示', 'ok'], 已隐藏: ['已隐藏', 'gray'] },
  credit_reason: {
    交易好评: ['交易好评', 'ok'], 中评: ['中评', 'run'], 差评: ['差评', 'bad'],
    放鸽子: ['放鸽子', 'bad'], 违规下架: ['违规下架', 'bad'], 人工调整: ['人工调整', 'warn']
  },
  price_grade: { A: ['A 定价合理', 'ok'], B: ['B 略偏高', 'warn'], C: ['C 偏离市场', 'bad'] },
  appeal_status: { 无: ['无', 'gray'], 申诉中: ['申诉中', 'warn'], 申诉通过: ['申诉通过', 'ok'], 申诉驳回: ['申诉驳回', 'bad'] },
  rule_status: { 启用: ['启用', 'ok'], 停用: ['停用', 'gray'], 草稿: ['草稿', 'warn'] },
  notice_status: { 已发布: ['已发布', 'ok'], 草稿: ['草稿', 'warn'], 已下线: ['已下线', 'gray'] },
  enable_status: { 1: ['启用', 'ok'], 0: ['停用', 'gray'] }
}

export const label = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][0] : code ?? '-')
export const tone = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][1] : 'gray')
