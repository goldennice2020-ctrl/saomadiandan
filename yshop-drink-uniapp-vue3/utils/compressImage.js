const TARGET_IMAGE_SIZE = 500 * 1024
const QUALITY_LEVELS = [80, 65, 50, 35]

export const compressImageTo500KB = async (filePath) => {
  if (!filePath) return filePath

  const originalSize = await getFileSize(filePath)
  if (originalSize > 0 && originalSize <= TARGET_IMAGE_SIZE) return filePath
  if (typeof uni.compressImage !== 'function') return filePath

  let bestPath = filePath
  let bestSize = originalSize || Number.MAX_SAFE_INTEGER

  for (const quality of QUALITY_LEVELS) {
    const compressedPath = await compressImage(filePath, quality).catch(() => '')
    if (!compressedPath) continue

    const compressedSize = await getFileSize(compressedPath)
    if (compressedSize > 0 && compressedSize < bestSize) {
      bestPath = compressedPath
      bestSize = compressedSize
    }
    if (compressedSize > 0 && compressedSize <= TARGET_IMAGE_SIZE) return compressedPath
  }

  return bestPath
}

const getFileSize = (filePath) => {
  return new Promise((resolve) => {
    if (typeof uni.getFileInfo !== 'function') {
      resolve(0)
      return
    }

    uni.getFileInfo({
      filePath,
      success: (res) => resolve(res.size || 0),
      fail: () => resolve(0)
    })
  })
}

const compressImage = (src, quality) => {
  return new Promise((resolve, reject) => {
    uni.compressImage({
      src,
      quality,
      success: (res) => resolve(res.tempFilePath),
      fail: reject
    })
  })
}
