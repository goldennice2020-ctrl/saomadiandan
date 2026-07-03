import request from '@/config/axios'

export interface TableCodeVO {
  id: number
  setCode: string
  setName: string
  tableNo: number
  code: string
  merchantUserId?: number
  shopId?: number
  bindTime?: Date
  qrcodeImage: string
}

export const getTableCodePage = async (params) => {
  return await request.get({ url: '/store/table-code/page', params })
}

export const createTableCodeSet = async (data: { setName: string }) => {
  return await request.post({ url: '/store/table-code/create-set', data })
}

export const bindTableCodeSet = async (data: { setCode: string }) => {
  return await request.post({ url: '/store/table-code/bind', data })
}
