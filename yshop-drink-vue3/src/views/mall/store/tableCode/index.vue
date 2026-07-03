<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="套装名称" prop="setName">
        <el-input
          v-model="queryParams.setName"
          placeholder="请输入套装名称"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="套装编号" prop="setCode">
        <el-input
          v-model="queryParams.setCode"
          placeholder="请输入套装编号"
          clearable
          class="!w-280px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="openCreate">
          <Icon icon="ep:plus" class="mr-5px" /> 生成一套10个码
        </el-button>
        <el-button type="success" @click="openBind">
          <Icon icon="ep:connection" class="mr-5px" /> 绑定套装
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="套装名称" align="center" prop="setName" min-width="150" />
      <el-table-column label="套装编号" align="center" prop="setCode" min-width="190" />
      <el-table-column label="桌号" align="center" prop="tableNo" width="90">
        <template #default="scope">{{ scope.row.tableNo }}号桌</template>
      </el-table-column>
      <el-table-column label="桌码编号" align="center" prop="code" min-width="210" />
      <el-table-column label="小程序码" align="center" width="130">
        <template #default="scope">
          <el-image
            v-if="scope.row.qrcodeImage"
            :src="scope.row.qrcodeImage"
            :preview-src-list="[scope.row.qrcodeImage]"
            style="width: 86px; height: 86px"
          />
        </template>
      </el-table-column>
      <el-table-column label="绑定状态" align="center" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.merchantUserId" type="success">已绑定</el-tag>
          <el-tag v-else type="info">未绑定</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商家账号ID" align="center" prop="merchantUserId" width="120" />
      <el-table-column label="门店ID" align="center" prop="shopId" width="100" />
      <el-table-column label="操作" align="center" fixed="right" width="110">
        <template #default="scope">
          <el-button link type="primary" @click="downloadCode(scope.row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <el-dialog v-model="createVisible" title="生成桌码套装" width="420px">
    <el-form :model="createForm" label-width="90px">
      <el-form-item label="套装名称">
        <el-input v-model="createForm.setName" placeholder="例如：第一套餐码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="submitCreate">生成</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="bindVisible" title="绑定桌码套装" width="460px">
    <el-form :model="bindForm" label-width="90px">
      <el-form-item label="套装编号">
        <el-input v-model="bindForm.setCode" placeholder="请输入总后台生成的套装编号" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="bindVisible = false">取消</el-button>
      <el-button type="primary" :loading="binding" @click="submitBind">绑定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts" name="StoreTableCode">
import * as TableCodeApi from '@/api/mall/store/tableCode'

const message = useMessage()

const loading = ref(false)
const list = ref<TableCodeApi.TableCodeVO[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 100,
  setName: '',
  setCode: ''
})

const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive({ setName: '' })
const bindVisible = ref(false)
const binding = ref(false)
const bindForm = reactive({ setCode: '' })

const getList = async () => {
  loading.value = true
  try {
    const data = await TableCodeApi.getTableCodePage(queryParams)
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => getList()

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  getList()
}

const openCreate = () => {
  createForm.setName = ''
  createVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.setName.trim()) {
    message.warning('请输入套装名称')
    return
  }
  creating.value = true
  try {
    const setCode = await TableCodeApi.createTableCodeSet({ setName: createForm.setName })
    message.success(`生成成功，套装编号：${setCode}`)
    createVisible.value = false
    await getList()
  } finally {
    creating.value = false
  }
}

const openBind = () => {
  bindForm.setCode = ''
  bindVisible.value = true
}

const submitBind = async () => {
  if (!bindForm.setCode.trim()) {
    message.warning('请输入套装编号')
    return
  }
  binding.value = true
  try {
    await TableCodeApi.bindTableCodeSet({ setCode: bindForm.setCode })
    message.success('绑定成功')
    bindVisible.value = false
    await getList()
  } finally {
    binding.value = false
  }
}

const downloadCode = (row: TableCodeApi.TableCodeVO) => {
  const link = document.createElement('a')
  link.href = row.qrcodeImage
  link.download = `${row.setName}-${row.tableNo}号桌.png`
  link.click()
}

onMounted(() => {
  getList()
})
</script>
