import * as FileApi from '@/api/infra/file'
import CryptoJS from 'crypto-js'
import { UploadRawFile, UploadRequestOptions } from 'element-plus/es/components/upload/src/upload'
import axios from 'axios'

const MAX_COMPRESSED_IMAGE_SIZE = 500 * 1024
const COMPRESSIBLE_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']

export const useUpload = () => {
  // 后端上传地址
  const uploadUrl = import.meta.env.VITE_UPLOAD_URL
  // 是否使用前端直连上传
  const isClientUpload = UPLOAD_TYPE.CLIENT === import.meta.env.VITE_UPLOAD_TYPE
  // 重写ElUpload上传方法
  const httpRequest = async (options: UploadRequestOptions) => {
    const uploadFile = await compressImageBeforeUpload(options.file)

    // 模式一：前端上传
    if (isClientUpload) {
      // 1.1 生成文件名称
      const fileName = await generateFileName(uploadFile)
      // 1.2 获取文件预签名地址
      const presignedInfo = await FileApi.getFilePresignedUrl(fileName)
      // 1.3 上传文件（不能使用 ElUpload 的 ajaxUpload 方法的原因：其使用的是 FormData 上传，Minio 不支持）
      return axios.put(presignedInfo.uploadUrl, uploadFile, {
        headers: {
          'Content-Type': uploadFile.type
        }
      }).then(() => {
        // 1.4. 记录文件信息到后端（异步）
        createFile(presignedInfo, fileName, uploadFile)
        // 通知成功，数据格式保持与后端上传的返回结果一致
        return { data: presignedInfo.url }
      })
    } else {
      // 模式二：后端上传
      // 重写 el-upload httpRequest 文件上传成功会走成功的钩子，失败走失败的钩子
      return new Promise((resolve, reject) => {
        FileApi.updateFile({ file: uploadFile })
          .then((res) => {
            if (res.code === 0 || res.code === 200) {
              resolve(res)
            } else {
              reject(res)
            }
          })
          .catch((res) => {
            reject(res)
          })
      })
    }
  }

  return {
    uploadUrl,
    httpRequest
  }
}

async function compressImageBeforeUpload(file: UploadRawFile) {
  if (!shouldCompressImage(file)) return file

  try {
    const compressedFile = await compressImage(file)
    return compressedFile.size < file.size ? compressedFile : file
  } catch (error) {
    console.warn('图片压缩失败，已使用原图上传', error)
    return file
  }
}

function shouldCompressImage(file: UploadRawFile) {
  return file.size > MAX_COMPRESSED_IMAGE_SIZE && COMPRESSIBLE_IMAGE_TYPES.includes(file.type)
}

async function compressImage(file: UploadRawFile): Promise<UploadRawFile> {
  const image = await loadImage(file)
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')

  if (!context) return file

  let width = image.naturalWidth || image.width
  let height = image.naturalHeight || image.height
  let quality = 0.85
  let blob = await drawImageToBlob(canvas, context, image, width, height, quality)

  while (blob.size > MAX_COMPRESSED_IMAGE_SIZE && quality > 0.45) {
    quality = Math.max(quality - 0.1, 0.45)
    blob = await drawImageToBlob(canvas, context, image, width, height, quality)
  }

  while (blob.size > MAX_COMPRESSED_IMAGE_SIZE && Math.max(width, height) > 900) {
    width = Math.round(width * 0.85)
    height = Math.round(height * 0.85)
    quality = 0.75
    blob = await drawImageToBlob(canvas, context, image, width, height, quality)
  }

  while (blob.size > MAX_COMPRESSED_IMAGE_SIZE && quality > 0.35) {
    quality = Math.max(quality - 0.1, 0.35)
    blob = await drawImageToBlob(canvas, context, image, width, height, quality)
  }

  while (blob.size > MAX_COMPRESSED_IMAGE_SIZE && Math.max(width, height) > 480) {
    width = Math.round(width * 0.85)
    height = Math.round(height * 0.85)
    quality = 0.65
    blob = await drawImageToBlob(canvas, context, image, width, height, quality)
  }

  URL.revokeObjectURL(image.src)

  const compressedName = replaceFileExtension(file.name, 'jpg')
  return Object.assign(new File([blob], compressedName, { type: 'image/jpeg' }), { uid: file.uid })
}

function loadImage(file: File): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image()
    const objectUrl = URL.createObjectURL(file)

    image.onload = () => resolve(image)
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('图片读取失败'))
    }
    image.src = objectUrl
  })
}

function drawImageToBlob(
  canvas: HTMLCanvasElement,
  context: CanvasRenderingContext2D,
  image: HTMLImageElement,
  width: number,
  height: number,
  quality: number
): Promise<Blob> {
  canvas.width = width
  canvas.height = height
  context.fillStyle = '#fff'
  context.fillRect(0, 0, width, height)
  context.drawImage(image, 0, 0, width, height)

  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) {
          resolve(blob)
        } else {
          reject(new Error('图片压缩失败'))
        }
      },
      'image/jpeg',
      quality
    )
  })
}

function replaceFileExtension(fileName: string, extension: string) {
  const index = fileName.lastIndexOf('.')
  return `${index > -1 ? fileName.substring(0, index) : fileName}.${extension}`
}

/**
 * 创建文件信息
 * @param vo 文件预签名信息
 * @param name 文件名称
 * @param file 文件
 */
function createFile(vo: FileApi.FilePresignedUrlRespVO, name: string, file: UploadRawFile) {
  const fileVo = {
    configId: vo.configId,
    url: vo.url,
    path: name,
    name: file.name,
    type: file.type,
    size: file.size
  }
  FileApi.createFile(fileVo)
  return fileVo
}

/**
 * 生成文件名称（使用算法SHA256）
 * @param file 要上传的文件
 */
async function generateFileName(file: UploadRawFile) {
  // 读取文件内容
  const data = await file.arrayBuffer()
  const wordArray = CryptoJS.lib.WordArray.create(data)
  // 计算SHA256
  const sha256 = CryptoJS.SHA256(wordArray).toString()
  // 拼接后缀
  const ext = file.name.substring(file.name.lastIndexOf('.'))
  return `${sha256}${ext}`
}

/**
 * 上传类型
 */
enum UPLOAD_TYPE {
  // 客户端直接上传（只支持S3服务）
  CLIENT = 'client',
  // 客户端发送到后端上传
  SERVER = 'server'
}
