<template>
  <div class="flex flex-col gap-20px">
    <el-radio-group v-model="radioGroup">
      <el-radio-button value="property">属性列表</el-radio-button>
      <el-radio-button value="service">服务列表</el-radio-button>
    </el-radio-group>
    <div class="flex flex-col">
      <div class="pb-10px">
        标准物模型
      </div>
      <property v-if="radioGroup==='property'" :key="radioGroup + typeId" :show-edite="!productId" :type-id="typeId" />
      <service v-if="radioGroup==='service'" :key="radioGroup + typeId" :type-id="typeId" />
    </div>

    <div v-if="productId" class="flex flex-col">
      <div class="pb-10px">
        自定义物模型
      </div>
      <property v-if="radioGroup==='property'" :key="radioGroup + typeId+productId"
                show-edite="true"
                :type-id="typeId"
                :product-id="productId" />
      <service v-if="radioGroup==='service'" :key="radioGroup + typeId+productId"
               :type-id="typeId"
               :product-id="productId"
               :show-edite="true"
      />
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import Property from '@/views/tslModel/widget/property.vue'
import Service from '@/views/tslModel/widget/service.vue'

const props = defineProps(['typeId', 'productId'])
const radioGroup = ref('property')
</script>

<style scoped lang="scss">

</style>
