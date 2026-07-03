<template>
	<layout>
		<uv-navbar
		  :fixed="false"
		  :title="title"
		  left-arrow
		  @leftClick="$onClickLeft"
		/>
		
		<view class="container" v-if="!loading">
			<!-- <view>
				<image :src="shopAd" mode="aspectFill" class="w-100 " style="height: 250rpx;"></image>
			</view> -->
			<view class="notice-bar" v-if="store.notice">
					<uv-notice-bar  :text="store.notice"></uv-notice-bar>
			</view>
		<view class="main">
			<view class="nav">
				<view class="header">
					<view class="left" v-if="orderType == 'takein'">
						<view class="store-name">
							<text>{{ store.name }}</text>
							<text class="small" v-if="tableNo">({{ tableNo }}号桌)</text>
						</view>
					</view>
					<view class="left overflow-hidden" v-else>
						<view class="store-name">
							<view>{{ store.name }}
								<text class="small" v-if="orderType == 'takeout'">(外带)</text>
							</view>
						</view>
					</view>
					<view class="right">
						<view class="dinein" :class="{active: orderType == 'takein'}" @tap="takein">
							<text>堂食</text>
						</view>
						<view class="takeout" :class="{active: orderType == 'takeout'}" @tap="takout">
							<text>外带</text>
						</view>
					</view>
				</view>
			</view>
		
			<!-- #ifdef H5 -->
			<view class="content"
				:style="{height: 'calc(100vh - 300rpx + '+(store.notice ? '0rpx':'60rpx')+')'}">
				<!-- #endif -->
				<!-- #ifndef H5 -->
				<view class="content" :style="{height: 'calc(100vh - 350rpx + '+(store.notice ? '0rpx':'60rpx')+')'}">
					<!-- #endif -->
					<scroll-view class="menus" :scroll-into-view="menuScrollIntoView" scroll-with-animation scroll-y>
						<view class="wrapper">
							<view class="menu" :id="`menu-${item.id}`" :class="{'current': item.id === currentCateId}"
								v-for="(item, index) in goods" :key="index" @tap="handleMenuTap(item.id)">
								<text>{{ item.name }}</text>
								<view class="dot" v-show="menuCartNum(item.id)">{{ menuCartNum(item.id) }}</view>
							</view>
						</view>
					</scroll-view>
					<!-- goods list begin -->
					<scroll-view class="goods" scroll-with-animation scroll-y :scroll-top="cateScrollTop"
						@scroll="handleGoodsScroll">
						<view class="wrapper">
							<view class="list">
								<!-- category begin -->
								<view class="category" v-for="(item, index) in goods" :key="index"
									:id="`cate-${item.id}`">
									<view class="title">
										<text>{{ item.name }}</text>
										<image mode="aspectFill" :src="item.icon" class="icon"></image>
									</view>
									<view class="items">
										<!-- 商品 begin -->
										<view class="good" v-for="(good, key) in item.goodsList" :key="key"
											:class="{'background-grey': good.stock <= 0}">
											<image mode="aspectFill" :src="good.image" class="image"
												@tap="showGoodDetailModal(item, good)"></image>
											<view class="right">
												<view class="name">{{ good.storeName }}</view>
												<view class="tips" v-if="good.storeInfo">{{ good.storeInfo }}</view>
												<view class="price_and_action">
													<view class="price">
														<text class="price-symbol">￥</text>{{ good.price }}<text class="price-unit"> /份</text>
													</view>
													<view class="btn-group" v-if="good.stock > 0">
														<button v-if="hasSpecs(good)" type="primary" class="btn property_btn"
															hover-class="none" size="mini"
															@tap="showGoodDetailModal(item, good)">
															选规格
														</button>
														<template v-else>
															<button v-if="goodCartNum(good.id)" class="btn reduce_btn"
																hover-class="none" size="mini"
																@tap="handleQuickReduce(good)">
																<text class="control-symbol">-</text>
															</button>
															<view v-if="goodCartNum(good.id)" class="quantity">
																{{ goodCartNum(good.id) }}
															</view>
															<button class="btn add_btn" hover-class="none" size="mini"
																@tap="handleQuickAdd(item, good)">
																<text class="control-symbol">+</text>
															</button>
														</template>
													</view>
											

													<view v-if="good.stock == 0">已售罄</view>
												</view>

											</view>
										</view>
										<!-- 商品 end -->
									</view>
								</view>
								<!-- category end -->
								<view class="bottom-spacer"></view>
							</view>
						</view>
					</scroll-view>
					<!-- goods list end -->
				</view>
				<!-- content end -->
				<!-- 购物车栏 begin -->
				<view class="cart-box" v-if="hasAddedToCart && cart.length > 0 && isCartShow">
					<view class="mark">
						<image src="/static/images/menu/cart.png" class="cart-img" @tap="openCartPopup"></image>
						<view class="tag">{{ getCartGoodsNumber }}</view>
					</view>
					<view class="price" @tap="openCartShow">￥{{ getCartGoodsPrice }}</view>
					<button type="primary" class="pay-btn" @tap="toPay" :disabled="disabledPay">
						{{ disabledPay ? `差${spread}元起送` : '去结算' }}
					</button>
				</view>
				<!-- 购物车栏 end -->
			</view>
			<!-- 商品详情模态框 begin -->
			<modal :show="goodDetailModalVisible" class="good-detail-modal" color="#5A5B5C" width="90%" custom
				padding="0rpx" radius="12rpx">
				<view class="cover">
					<view class="btn-group">
						<image src="/static/images/menu/close.png" @tap="closeGoodDetailModal"></image>
					</view>
				</view>
				<scroll-view class="detail" scroll-y>
					<view v-if="good.image" class="image">
						<image :src="good.image"></image>
					</view>

					<view class="wrapper">
						<view class="basic">
							<view class="name">{{ good.storeName }}</view>
							<view class="tips flex justify-between">{{ good.storeInfo }} <text style="color: red;">可获积分:10</text></view>
						</view>
						<view class="properties">
							<view class="property" v-for="(item, index) in good.productAttr" :key="index">
								<view class="title">
									<text class="name">{{ item.attrName }}</text>
								</view>
								<view class="values">
									<view class="value" v-for="(value, key) in item.attrValueArr" :key="key"
										:class="{'default': value == newValue[index]}"
										@tap="changePropertyDefault(index, key,false)">
										{{ value }}
									</view>
								</view>
							</view>
						</view>
					</view>
				</scroll-view>
				<view class="action">
					<view class="left">
						<view class="price">￥{{ good.price }}</view>
						<view class="props">
							{{ good.valueStr }}
						</view>
					</view>
					<view class="btn-group">
						<text style="margin-right: 20rpx;">库存：{{good.stock}} </text>
						<button type="default" plain class="btn" size="mini" hover-class="none"
							@tap="handlePropertyReduce">
							<view class="iconfont iconsami-select"></view>
						</button>
						<view class="number">{{ good.number }}</view>
						<button type="primary" class="btn" size="min" hover-class="none" @tap="handlePropertyAdd">
							<view class="iconfont iconadd-select"></view>
						</button>
					</view>
				</view>
				<view class="add-to-cart-btn" @tap="handleAddToCartInModal">
					<view>加入购物车</view>
				</view>
			</modal>
			<!-- 商品详情模态框 end -->
			<!-- 购物车详情popup -->
			<uv-popup ref="popup" mode="bottom" class="cart-popup">
				<template #default>
				<view  class="cart-popup">
					 <view class="top">
					  <text @tap="handleCartClear">清空</text>
					 </view>
					 <scroll-view class="cart-list" scroll-y>
					  <view class="wrapper">
					   <view class="item" v-for="(item, index) in cart" :key="index">
						<view class="left">
						 <view class="name">{{ item.name }}</view>
						 <view class="props">{{ item.valueStr }}</view>
						</view>
						<view class="center">
						 <text>￥{{ item.price }}</text>
						</view>
						<view class="right">
						 <button type="default" plain size="mini" class="btn" hover-class="none"
						  @tap="handleCartItemReduce(index)">
						  <view class="iconfont iconsami-select"></view>
						 </button>
						 <view class="number">{{ item.number }}</view>
						 <button type="primary" class="btn" size="min" hover-class="none"
						  @tap="handleCartItemAdd(index)">
						  <view class="iconfont iconadd-select"></view>
						 </button>
						</view>
					   </view>
							
					  </view>
					 </scroll-view>
				 </view>
				</template>
			</uv-popup>
			   <!-- 购物车详情popup -->
			<uv-toast ref="uToast"></uv-toast>
		</view>
		<!--轻提示-->
		<view class="loading" v-else>
			<uv-loading-icon  color="#DA5650" size=40 mode="circle" ></uv-loading-icon>
			<view class="loading-text" v-if="loadingMessage">{{ loadingMessage }}</view>
		<!-- 	<uv-toast ref="uToast"></uv-toast> -->
		</view>
	</layout>
</template>

<script setup>
import {
  ref,
  computed,
  nextTick
} from 'vue'
import { useMainStore } from '@/store/store'
import { storeToRefs } from 'pinia'
import { onLoad,onShow ,onPullDownRefresh,onHide} from '@dcloudio/uni-app'
import { formatDateTime } from '@/utils/util'
import {
  shopGet,
  menuGoods,
  resolveTableCode
} from '@/api/goods'
import {
  menuAds,
  shopGetList
} from '@/api/market'
const main = useMainStore()
const { orderType,address, store,isLogin } = storeToRefs(main)
const title = ref('点餐')
const text = ref('滚动通知')

const goods = ref([])
const ads = ref([])
const loading = ref(true) 
const loadingMessage = ref('')
const currentCateId = ref(0)
const cateScrollTop = ref(0)
const menuScrollIntoView = ref('')
const cart = ref([])
const goodDetailModalVisible = ref(false)
const good= ref({})
const category = ref({})
const cartPopupVisible = ref(false)
const sizeCalcState = ref(false)
const newValue = ref([])
const shopAd = ref('')
const hasAddedToCart = ref(false)
const isCartShow = ref(true)
const popup = ref()
const tableNo = ref(uni.getStorageSync('tableNo') || '')
const tableCode = ref(uni.getStorageSync('tableCode') || '')
const DEFAULT_SHOP_ID = 2

const fallbackShop = {
	id: DEFAULT_SHOP_ID,
	name: '意向餐饮店',
	image: 'https://apidc.yixiang.co/admin-api/infra/file/4/get/44723283500f656cdd5906e13f2997cb292d17f516065ff4b3b38f9810bce4cb.jpg',
	notice: '新店开张',
	status: 1,
	distance: 10,
	far: 999,
	min_price: 10,
	deliveryPrice: 20,
	startTime: 1692406800000,
	endTime: 1692457200000
}

const fallbackGoods = [{
	id: 1,
	name: '推荐',
	icon: '/static/images/index002.png',
	goodsList: [{
		id: 1,
		storeName: '招牌饮品',
		storeInfo: '默认商品，可在后台配置真实菜单',
		image: '/static/images/index002.png',
		price: '9.90',
		stock: 999,
		productAttr: [{
			attrName: '规格',
			attrValueArr: ['标准']
		}],
		productValue: {
			'标准': {
				price: '9.90',
				stock: 999,
				image: ''
			}
		}
	}]
}]

const withTimeout = (promise, timeout = 5000) => {
	return Promise.race([
		promise,
		new Promise((_, reject) => {
			setTimeout(() => reject(new Error('请求超时')), timeout)
		})
	])
}

const showFallbackMenu = (message = '') => {
	uni.hideLoading()
	main.SET_STORE({
		...fallbackShop,
		...store.value
	})
	goods.value = fallbackGoods
	refreshCart()
	loadingMessage.value = message
	loading.value = false
	uni.stopPullDownRefresh()
	nextTick(() => measureMenuLayout())
}

const measureMenuLayout = () => {}

const goodCartNum = computed(() => { //计算单个饮品添加到购物车的数量
	return (id) => cart.value.reduce((acc, cur) => {
		if (cur.id === id) {
			return acc += cur.number
		}
		return acc
	}, 0)
})
const menuCartNum = computed(() =>{
	return (id) => cart.value.reduce((acc, cur) => {
		if (cur.cate_id === id) {
			return acc += cur.number
		}
		return acc
	}, 0)
})
const getCartGoodsNumber = computed(() => { //计算购物车总数
	return cart.value.reduce((acc, cur) => acc + cur.number, 0)
})
const getCartGoodsPrice = computed(() =>{ //计算购物车总价
	let price = cart.value.reduce((acc, cur) => acc + cur.number * cur.price, 0);
	return parseFloat(price).toFixed(2);
})
const hasSpecs = (good) => {
	if (!Array.isArray(good.productAttr) || good.productAttr.length === 0) return false
	if (good.productAttr.length === 1) {
		const values = good.productAttr[0].attrValueArr || []
		return !(values.length === 1 && values[0] === '默认')
	}
	return true
}
const vibrateOnAdd = () => {
	// #ifdef APP-PLUS
	if (typeof plus !== 'undefined' && plus.device && plus.device.vibrate) {
		plus.device.vibrate(50)
		return
	}
	// #endif
	// #ifdef MP-WEIXIN
	if (typeof wx !== 'undefined' && wx.vibrateShort) {
		wx.vibrateShort({ type: 'heavy' })
		return
	}
	// #endif
	// #ifdef H5
	if (typeof navigator !== 'undefined' && navigator.vibrate) {
		navigator.vibrate(50)
		return
	}
	// #endif
	if (uni.vibrateShort) uni.vibrateShort({ type: 'heavy' })
}
const disabledPay = computed(() => { //是否达到起送价
	return false
})
const spread = computed(() => { //差多少元起送
	if (orderType.value != 'takeout') return
	return parseFloat((store.value.min_price - getCartGoodsPrice).toFixed(2))
})

// 监听自定义事件
uni.$on('refreshMenu', () => {
	// 在这里执行onLoad逻辑
	console.log('refreshMenu1:',store.value.id)
	init()
})

onPullDownRefresh(() => {
	init()
})
onLoad((options) => {
	hasAddedToCart.value = false
	init(options);
	refreshCart()
})
onHide(() => {
	// 重新进入要重新计算页面高度，否则有问题
	sizeCalcState.value = false;
})
onShow(() => {
	uni.hideLoading()
	//init();
	refreshCart()
	shopAd.value = uni.getStorageSync('shopAd')
})

const openCartShow = () =>{
	openCartPopup()
}
const in_array = (search, array) => {
	for (var i in array) {
		if (array[i] == search) {
			return true;
		}
	}
	return false;
}
const uToast = ref()
const getQueryValue = (options, keys) => {
	for (const key of keys) {
		if (options && options[key]) return options[key]
	}
	if (options && options.q) {
		const q = decodeURIComponent(options.q)
		const query = q.includes('?') ? q.split('?')[1] : q
		const params = query.split(/[&?#]/).reduce((acc, item) => {
			const [key, value] = item.split('=')
			if (key && value) acc[key] = value
			return acc
		}, {})
		for (const key of keys) {
			if (params[key]) return params[key]
		}
	}
	if (options && options.scene) {
		const scene = decodeURIComponent(options.scene)
		if (/^\d+$/.test(scene)) return scene
		const params = scene.split(/[&?]/).reduce((acc, item) => {
			const [key, value] = item.split('=')
			if (key && value) acc[key] = value
			return acc
		}, {})
		for (const key of keys) {
			if (params[key]) return params[key]
		}
		if (keys.some(key => ['code', 'tableCode', 'table_code'].includes(key))) return scene
	}
	return ''
}
const getCurrentPageOptions = () => {
	const pages = getCurrentPages()
	const current = pages[pages.length - 1]
	return current?.options || {}
}
const getCurrentShopId = (options = {}) => {
	const id = getQueryValue(options, ['shopId', 'shop_id', 'storeId', 'store_id', 'id'])
	if (id && !Number.isNaN(Number(id))) return Number(id)
	return DEFAULT_SHOP_ID
}
const resolveScanTableCode = async(options = {}) => {
	const code = getQueryValue(options, ['code', 'tableCode', 'table_code'])
	if (!code) return null
	try {
		const data = await withTimeout(resolveTableCode({ code }), 5000)
		if (data && data.tableNo) {
			tableCode.value = data.code
			tableNo.value = data.tableNo
			uni.setStorageSync('tableCode', data.code)
			uni.setStorageSync('tableNo', data.tableNo)
			uni.setStorageSync('tableShopId', data.shopId || 0)
			main.SET_ORDER_TYPE('takein')
		}
		return data
	} catch (error) {
		console.log('resolve table code failed:', error)
		return null
	}
}
const  init = async(options = {}) => { //页面初始化
	uni.hideLoading()
	loadingMessage.value = '';
	showFallbackMenu()
	const pageOptions = Object.keys(options).length ? options : getCurrentPageOptions()
	const tableInfo = await resolveScanTableCode(pageOptions)
	const shopId = tableInfo && tableInfo.shopId ? Number(tableInfo.shopId) : getCurrentShopId(pageOptions)
	try {
		await getShopList(shopId)
	} catch (error) {
		console.log('init menu failed:', error)
		showFallbackMenu()
	}
}
const getShopList = async(shopId) => {
	try {
		let shop = null
		if (shopId > 0) {
			shop = await withTimeout(shopGet({
				shop_id: shopId
			}));
		} else {
			const list = await withTimeout(shopGetList({
				lat: 0,
				lng: 0,
				kw: '',
				shop_id: 0
			}));
			shop = list && list.length ? list[0] : null
		}
		if (shop) {
			//广告图
			getAds(shop.id);
	
			shop.notice = shop.status == 1 ? shop.notice : '店铺营业时间为:' + formatDateTime(shop.startTime,'hh:mm')+' - '+formatDateTime(shop.endTime,'hh:mm') +
			'，不在营业时间内无法下单';
			// 设置店铺信息
			main.SET_STORE(shop);
			let mygoods = await withTimeout(menuGoods({
				shopId: shop.id
			}));
			if (mygoods && mygoods.length) {
				goods.value = mygoods;
				refreshCart();
			} else {
				goods.value = fallbackGoods;
			}
			console.log('goods:',mygoods)
			console.log('goods:',goods.value)
			uni.hideLoading()
			loading.value = false;
			nextTick(() => measureMenuLayout())
			uni.stopPullDownRefresh();
		} else {
			showFallbackMenu()
		}
	} catch (error) {
		console.log('load shop failed:', error)
		uni.hideLoading()
		showFallbackMenu()
	}
}
const refreshCart = () =>{
	if (goods.value && goods.value.length > 0) {
		let newGoods = goods.value;
		cart.value = [];
		let newCart = uni.getStorageSync('cart') || [];
		let tmpCart = [];
		if (newCart) {
			for (let i in newCart) {
				for (let ii in newGoods) {
					for (let iii in newGoods[ii].goodsList) {
						if (newCart[i].id == newGoods[ii].goodsList[iii].id) {
							tmpCart.push(newCart[i]);
						}
					}
				}
			}
			cart.value = tmpCart;
			cartPopupVisible.value = false;
		}
	}
}
const  getAds = async(shop_id) =>{
	try {
		let data = await withTimeout(menuAds({
			shop_id: shop_id ? shop_id : 0
		}));
		if (data) {
			ads.value = data;
		}
	} catch (error) {
		console.log('load ads failed:', error)
	}
}
const takout = (force = false) => {
	if (orderType.value == 'takeout' && force == false) return
	main.SET_ORDER_TYPE('takeout');

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} 

}
const takein = (force = false) => {
	if (orderType.value == 'takein' && force == false) return
	main.SET_ORDER_TYPE('takein');

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} 

}
const handleMenuTap = (id) => { //点击菜单项事件
	if (!sizeCalcState.value) {
		calcSize()
	}

	currentCateId.value = id
	nextTick(() => cateScrollTop.value = goods.value.find(item => item.id == id).top)
}
const handleGoodsScroll = ({ detail }) => { //商品列表滚动事件
	if (!sizeCalcState.value) {
		calcSize()
	}
	console.log('scrollTop:',detail)
	const {
		scrollTop
	} = detail
	let tabs = goods.value.filter(item => item.top <= scrollTop).reverse()
	if (tabs.length > 0) {
		currentCateId.value = tabs[0].id
	}
}
const calcSize = () => {
	let h = 10
	let view = uni.createSelectorQuery().select('#ads')
	if (view) {
		view.fields({
			size: true
		}, data => {
			if (data) {
				h += Math.floor(data.height)
			}
		}).exec()
	}
	goods.value.forEach(item => {
		let view = uni.createSelectorQuery().select(`#cate-${item.id}`)
		view.fields({
			size: true
		}, data => {
			console.log('h3:',h)
			item.top = h
			h += data.height
			item.bottom = h
		}).exec()
	})
	sizeCalcState.value = true
}
const handleAddToCart = (cate, newGood, num) =>{ //添加到购物车
	hasAddedToCart.value = true
	const valueStr = newGood.valueStr || good.value.valueStr || ''
	const index = cart.value.findIndex(item => {
		if (newGood) {
			return (item.id === newGood.id) && (item.valueStr === valueStr)
		} else {
			return item.id === newGood.id
		}
	})
	if (index > -1) {
		cart.value[index].number += num
	} else {
		cart.value.push({
			id: newGood.id,
			cate_id: cate.id,
			name: newGood.storeName,
			price: newGood.price,
			number: num,
			image: newGood.image,
			valueStr
		})
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const handleQuickAdd = (cate, newGood) => {
	if (newGood.stock <= 0) return
	handleAddToCart(cate, {
		...newGood,
		valueStr: ''
	}, 1)
	vibrateOnAdd()
}
const handleQuickReduce = (newGood) => {
	const index = cart.value.findIndex(item => item.id === newGood.id && !item.valueStr)
	if (index === -1) return
	if (cart.value[index].number === 1) {
		cart.value.splice(index, 1)
	} else {
		cart.value[index].number -= 1
	}
	if (!cart.value.length) {
		hasAddedToCart.value = false
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const handleReduceFromCart = (item, good) => {
	const index = cart.value.findIndex(item => item.id === good.id)
	cart.value[index].number -= 1
	if (cart.value[index].number <= 0) {
		cart.value.splice(index, 1)
	}
	if (!cart.value.length) {
		hasAddedToCart.value = false
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const showGoodDetailModal = (item, newGood) => {
	isCartShow.value = true
	good.value = JSON.parse(JSON.stringify({
		...newGood,
		number: 1
	}))
	category.value = JSON.parse(JSON.stringify(item))
	goodDetailModalVisible.value = true;
	console.log('goodDetailModalVisible:',goodDetailModalVisible.value)
	changePropertyDefault(0, 0,true);
}
const closeGoodDetailModal = () => { //关闭饮品详情模态框
	goodDetailModalVisible.value = false
    category.value = {}
	good.value = {}
}
const changePropertyDefault = (index, key, isDefault) => { //改变默认属性值
	let valueStr = ''
	console.log('good:',good.value)
	if(isDefault){
		newValue.value = []
		for(let i = 0;i < good.value.productAttr.length;i++){
			newValue.value[i] = good.value.productAttr[i].attrValueArr[0]
		}

		//valueStr = newValue.value.join(',')

	}else{
		newValue.value[index] = good.value.productAttr[index].attrValueArr[key]
		//valueStr = newValue.value.join(',')
	}
	
	valueStr = newValue.value.join(',')
	let productValue = good.value.productValue[valueStr]
	if(!productValue) {
		let skukey = JSON.parse(JSON.stringify(newValue.value))
		skukey.sort((a, b) => a.localeCompare(b))
		//console.log('skukey:',skukey)
		valueStr = skukey.join(',')
		productValue = good.value.productValue[valueStr]
	}

	
	//let productValue = good.value.productValue[valueStr]
	good.value.number = 1;
	good.value.price = parseFloat(productValue.price).toFixed(2);
	good.value.stock = productValue.stock;
	good.value.image = productValue.image ? productValue.image : good.value.image;
	good.value.valueStr = valueStr

}
const handlePropertyAdd = () => {
	good.value.number += 1
}
const handlePropertyReduce = () => {
	if (good.value.number === 1) return
	good.value.number -= 1
}
const handleAddToCartInModal = () => {
	if (good.value.stock <= 0) {
		uToast.value.show({message:'商品库存不足',type: 'error'});
		return;
	}
	handleAddToCart(category.value, good.value, good.value.number)
	closeGoodDetailModal()
}
const openCartPopup = () => { //打开/关闭购物车列表popup
	popup.value.open()
}
const handleCartClear = () => { //清空购物车
	uni.showModal({
		title: '提示',
		content: '确定清空购物车么',
		success: ({
			confirm
		}) => {
			if (confirm) {
				popup.value.close()
				cart.value = []
				hasAddedToCart.value = false
				uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
			}
		}
	})
}
const handleCartItemAdd = (index) => {
	cart.value[index].number += 1
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const handleCartItemReduce = (index) => {
	if (cart.value[index].number === 1) {
		cart.value.splice(index, 1)
	} else {
		cart.value[index].number -= 1
	}
	if (!cart.value.length) {
		cartPopupVisible.value = false
		hasAddedToCart.value = false
	}
	uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))
}
const toPay = () => {

	if (!isLogin.value) {
		uni.navigateTo({
			url: '/pages/components/pages/login/login'
		})
		return
	} else {
		if (store.value.status == 0) {
			uToast.value.show({message:'不在店铺营业时间内',type: 'error'});
			return;
		}
		uni.setStorageSync('cart', JSON.parse(JSON.stringify(cart.value)))

		uni.navigateTo({
			url: '/pages/components/pages/pay/pay'
		})
	}
}

</script>

<style lang="scss" scoped>
// 点餐页局部 token（与 uni.scss 全局变量配合）
$menu-nav-height: 140rpx;
$menu-sidebar-width: 200rpx;
$menu-radius-sm: 8rpx;
$menu-radius-pill: 38rpx;
$menu-radius-cart: 48rpx;
$menu-good-image-size: 160rpx;
$menu-control-size-sm: 44rpx;
$menu-control-size: 46rpx;
$menu-list-bottom-space: 200rpx;
$menu-cart-height: 96rpx;
$menu-cart-popup-max-height: 60vh;
$menu-cart-list-bottom: 156rpx;
$menu-cart-price-offset: 120rpx;
$menu-sold-out-bg: #e1e4e4;
$menu-secondary-color: #5a5b5c;
$menu-shadow-cart: 0 0 20rpx rgba(0, 0, 0, 0.2);
$menu-dot-size: 34rpx;
$menu-badge-size: 36rpx;
$menu-tag-size: 40rpx;
$menu-font-menu: 26rpx;
$menu-gap-sm: 10rpx;
$menu-gap-base: 20rpx;
$menu-divider-height: 2rpx;

@mixin text-ellipsis {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

@mixin quantity-control($size) {
	.btn,
	.number {
		width: $size;
		height: $size;
		line-height: $size;
		text-align: center;
	}

	.btn {
		padding: 0;
		border-radius: $border-radius-circle;
	}
}

@mixin list-divider {
	&::after {
		content: '';
		position: absolute;
		bottom: 0;
		left: 0;
		width: 100%;
		height: $menu-divider-height;
		background-color: $border-color;
		transform: scaleY(0.6);
	}
}

/* #ifdef H5 */
page {
	height: auto;
	min-height: 100%;
}
/* #endif */

.container {
	--menu-control-size-sm: #{$menu-control-size-sm};
	--menu-control-size: #{$menu-control-size};
	--menu-cart-popup-max-height: #{$menu-cart-popup-max-height};

	overflow: hidden;
	position: relative;
}

.loading {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;

	image {
		width: 260rpx;
		height: 260rpx;
		position: relative;
		margin-top: -200rpx;
		/* #ifdef h5 */
		margin-top: 0;
		/* #endif */
	}
}

.main {
	width: 100%;
	height: 100%;
		position: relative;
	}

	.loading-text {
		margin-top: $spacing-col-lg;
		color: $text-color-assist;
		font-size: $font-size-base;
	}

.nav {
	width: 100%;
	height: $menu-nav-height;
	display: flex;
	flex-direction: column;

	.header {
		width: 100%;
		height: $menu-nav-height;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: $spacing-row-base;
		background-color: $text-color-white;

		.left {
			flex: 1;
			display: flex;
			flex-direction: column;

			.store-name {
				display: flex;
				align-items: center;
				justify-content: flex-start;
				margin-bottom: $menu-gap-sm;
				font-size: $font-size-lg;

				.small {
					font-size: $font-size-sm;
					color: $text-color-assist;
				}

				.iconfont {
					margin-left: $menu-gap-sm;
					line-height: 100%;
				}
			}
		}

		.right {
			display: flex;
			align-items: center;
			padding: 0 $menu-radius-pill;
			font-size: $font-size-sm;
			color: $text-color-assist;
			background-color: $bg-color-grey;
			border-radius: $menu-radius-pill;

			.dinein,
			.takeout {
				position: relative;
				display: flex;
				align-items: center;

				&.active {
					padding: 14rpx $menu-radius-pill;
					color: $text-color-white;
					background-color: $color-primary;
					border-radius: $menu-radius-pill;
				}
			}

			.takeout {
				flex: 1;
				height: 100%;
				margin-left: $spacing-row-base;
				padding: 14rpx 0;
			}

			.dinein.active {
				margin-left: -$menu-radius-pill;
			}

			.takeout.active {
				margin-right: -$menu-radius-pill;
			}
		}
	}
}
	
.content {
	width: 100%;
	height: calc(100vh - 212rpx);
	/* #ifdef H5 */
	height: calc(100vh - 212rpx - 188rpx);
	/* #endif */
	display: flex;

	.menus {
		width: 176rpx;
		height: 100%;
		overflow: hidden;
		background-color: $text-color-white;
		border-right: 1rpx solid $border-color-light;

		.wrapper {
			width: 100%;
			height: 100%;

			.menu {
				position: relative;
				display: flex;
				align-items: center;
				justify-content: flex-start;
				min-height: 112rpx;
				padding: $spacing-row-lg $spacing-row-base;
				font-size: $menu-font-menu;
				color: $text-color-assist;

				&.current {
					font-weight: 700;
					color: $color-primary;
					background-color: $bg-color-primary;

					&::after {
						position: absolute;
						top: 0;
						right: 0;
						width: 3rpx;
						height: 100%;
						content: '';
						background-color: $color-primary;
					}
				}

				.dot {
					position: absolute;
					top: 12rpx;
					right: 12rpx;
					width: 38rpx;
					height: 38rpx;
					font-size: 22rpx;
					line-height: 38rpx;
					text-align: center;
					color: $text-color-white;
					background-color: $color-primary;
					border-radius: $border-radius-circle;
				}

				&:last-child {
					margin-bottom: $menu-list-bottom-space;
				}
			}
		}
	}

	.goods {
		flex: 1;
		height: 100%;
		overflow: hidden;
		background-color: $text-color-white;

		.wrapper {
			width: 100%;
			height: 100%;
			padding: $spacing-row-base 20rpx;

			.ads {
				height: calc(300 / 550 * 510rpx);

				image {
					width: 100%;
					height: 100%;
					border-radius: $menu-radius-sm;
				}
			}

			.list {
				width: 100%;
				font-size: $font-size-base;

				.category {
					width: 100%;

					.title {
						display: flex;
						align-items: center;
						padding: 10rpx 0 $spacing-row-base;
						font-size: 34rpx;
						font-weight: 700;
						color: $text-color-base;

						.icon {
							width: 38rpx;
							height: 38rpx;
							margin-left: $menu-gap-sm;
						}
					}

					&:last-child {
						margin-bottom: $menu-list-bottom-space;
					}

					.items {
						display: flex;
						flex-direction: column;
						gap: 24rpx;

						.good {
							display: flex;
							flex-direction: column;
							overflow: hidden;
							padding: 0;
							background-color: $text-color-white;
							border: 1rpx solid $border-color-light;
							border-radius: 18rpx;
							box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

							.image {
								width: 100%;
								height: 250rpx;
								margin-right: 0;
								border-radius: 18rpx 18rpx 0 0;
							}

							.right {
								width: 100%;
								display: flex;
								flex-direction: column;
								align-items: flex-start;
								justify-content: space-between;
								min-height: 172rpx;
								padding: 20rpx 22rpx 24rpx;
								overflow: hidden;

								.name {
									width: 100%;
									margin-bottom: 12rpx;
									font-size: 36rpx;
									font-weight: 700;
									line-height: 48rpx;
									@include text-ellipsis;
								}

								.tips {
									display: inline-flex;
									width: auto;
									height: 38rpx;
									margin-bottom: 12rpx;
									padding: 0 12rpx;
									font-size: 24rpx;
									line-height: 38rpx;
									color: $text-color-white;
									background-color: rgba($color-primary, 0.86);
									border-radius: 8rpx;
								}

								.price_and_action {
									display: flex;
									align-items: center;
									justify-content: space-between;
									width: 100%;

									.price {
										font-size: 38rpx;
										font-weight: 700;
										line-height: 54rpx;

										.price-symbol {
											font-size: 30rpx;
										}

										.price-unit {
											font-size: 26rpx;
											font-weight: 400;
											color: $text-color-assist;
										}
									}

									.btn-group {
										position: relative;
										display: flex;
										align-items: center;
										justify-content: flex-end;
										min-width: 124rpx;

										.btn {
											box-sizing: border-box;
											height: 53rpx;
											padding: 0 24rpx;
											font-size: 24rpx;
											font-weight: 700;
											line-height: 53rpx;

											&.property_btn {
												border-radius: 29rpx;
											}

											&.add_btn,
											&.reduce_btn {
												display: flex;
												align-items: center;
												justify-content: center;
												width: 52rpx;
												height: 52rpx;
												padding: 0;
												border-radius: 52rpx;
												line-height: 52rpx;
											}

											&.add_btn {
												color: $text-color-white;
												background-color: $color-primary;
											}

											&.reduce_btn {
												color: $color-primary;
												background-color: $text-color-white;
												border: 2rpx solid $color-primary;
											}

											.control-symbol {
												display: block;
												width: 100%;
												height: 100%;
												font-size: 42rpx;
												font-weight: 500;
												line-height: 46rpx;
												text-align: center;
											}
										}

										.quantity {
											width: 56rpx;
											font-size: 32rpx;
											line-height: 52rpx;
											text-align: center;
											color: $text-color-base;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
	
.good-detail-modal {
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100%;

	.cover {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 20rpx;

		.btn-group {
			position: absolute;
			top: 0;
			right: $menu-gap-sm;
			z-index: 210;
			display: flex;
			align-items: center;
			justify-content: space-around;

			image {
				width: $img-size-lg;
				height: $img-size-lg;
			}
		}
	}

	.detail {
		position: relative;
		width: 100%;
		min-height: 1vh;
		max-height: calc(90vh - 320rpx - 80rpx - 120rpx);

		.image {
			display: flex;
			align-items: center;
			justify-content: center;

			image {
				width: 260rpx;
				height: 260rpx;
			}
		}

		.wrapper {
			width: 100%;
			height: 100%;
			overflow: hidden;

			.basic {
				display: flex;
				flex-direction: column;
				padding: 0 $spacing-row-base $spacing-row-lg;

				.name {
					margin-bottom: $menu-gap-sm;
					font-size: $font-size-base;
					color: $text-color-base;
				}

				.tips {
					font-size: $font-size-sm;
					color: $text-color-grey;
				}
			}

			.properties {
				display: flex;
				flex-direction: column;
				width: 100%;
				padding: $menu-gap-sm $spacing-row-lg 0;
				border-top: $menu-divider-height solid $bg-color-grey;

				.property {
					display: flex;
					flex-direction: column;
					width: 100%;
					margin-bottom: $spacing-row-lg;

					.title {
						display: flex;
						align-items: center;
						justify-content: flex-start;
						width: 100%;
						margin-bottom: $spacing-row-base;

						.name {
							margin-right: $spacing-row-base;
							font-size: $menu-font-menu;
							color: $text-color-base;
						}

						.desc {
							flex: 1;
							font-size: $font-size-sm;
							color: $color-primary;
							@include text-ellipsis;
						}
					}

					.values {
						display: flex;
						flex-wrap: wrap;
						width: 100%;

						.value {
							margin-right: $spacing-col-base;
							margin-bottom: $spacing-col-base;
							padding: $spacing-col-base $spacing-row-lg;
							font-size: $menu-font-menu;
							color: $text-color-assist;
							background-color: $bg-color-grey;
							border-radius: $menu-radius-sm;

							&.default {
								color: $text-color-white;
								background-color: $color-primary;
							}
						}
					}
				}
			}
		}
	}

	.action {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 120rpx;
		padding: 0 26rpx;
		background-color: $bg-color-grey;

		.left {
			flex: 1;
			display: flex;
			flex-direction: column;
			justify-content: center;
			margin-right: $spacing-row-base;
			overflow: hidden;

			.price {
				font-size: $font-size-lg;
				color: $text-color-base;
			}

			.props {
				width: 100%;
				font-size: $font-size-sm;
				color: $text-color-assist;
				@include text-ellipsis;
			}
		}

		.btn-group {
			display: flex;
			align-items: center;
			justify-content: space-around;

			.btn {
				font-size: $font-size-base;
			}

			@include quantity-control(var(--menu-control-size-sm));
		}
	}

	.add-to-cart-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 80rpx;
		font-size: $font-size-base;
		color: $text-color-white;
		background-color: $color-primary;
		border-radius: 0 0 $border-radius-lg $border-radius-lg;
	}
}
	
.cart-box {
	position: fixed;
	bottom: 0;
	/* #ifdef H5 */
	bottom: var(--window-bottom);
	/* #endif */
	left: 0;
	right: 0;
	z-index: 9999;
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 118rpx;
	padding: 18rpx 28rpx 18rpx 38rpx;
	background-color: $text-color-white;
	border-radius: 0;
	box-shadow: 0 -4rpx 18rpx rgba(0, 0, 0, 0.08);

	.cart-img {
		position: relative;
		width: 84rpx;
		height: 84rpx;
		margin-top: 0;
	}

	.pay-btn {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 172rpx;
		height: 63rpx;
		padding: 0;
		font-size: 25rpx;
		font-weight: 600;
		color: $text-color-white;
		border-radius: 36rpx;
	}

	.mark {
		position: relative;
		flex: 0 0 94rpx;
		margin-right: 30rpx;
		padding-left: 0;

		.tag {
			position: absolute;
			top: -12rpx;
			right: -4rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 38rpx;
			height: 38rpx;
			padding: 4rpx;
			font-size: 24rpx;
			font-weight: 600;
			color: $text-color-white;
			background-color: $color-primary;
			border-radius: $border-radius-circle;
			border: 4rpx solid $text-color-white;
		}
	}

	.price {
		flex: 1;
		font-size: 40rpx;
		font-weight: 700;
		color: $text-color-base;
	}
}

.cart-popup {
	.top {
		padding: $menu-gap-sm $spacing-row-lg;
		font-size: $font-size-sm;
		text-align: right;
		color: $menu-secondary-color;
		background-color: $bg-color-primary;
	}

	.cart-list {
		width: 100%;
		min-height: 1vh;
		max-height: var(--menu-cart-popup-max-height);
		overflow: hidden;
		background-color: $text-color-white;

		.wrapper {
			display: flex;
			flex-direction: column;
			height: 100%;
			padding: 0 $spacing-row-lg;
			margin-bottom: $menu-cart-list-bottom;

			.item {
				position: relative;
				display: flex;
				align-items: center;
				justify-content: space-between;
				padding: $spacing-row-lg 0;
				@include list-divider;

				.left {
					flex: 1;
					display: flex;
					flex-direction: column;
					margin-right: $spacing-row-lg;
					overflow: hidden;

					.name {
						font-size: $font-size-sm;
						color: $text-color-base;
					}

					.props {
						font-size: $font-size-sm;
						color: $text-color-assist;
						@include text-ellipsis;
					}
				}

				.center {
					margin-right: $menu-cart-price-offset;
					font-size: $font-size-base;
				}

				.right {
					display: flex;
					align-items: center;
					justify-content: space-between;
					@include quantity-control(var(--menu-control-size));

					.btn {
						font-size: $font-size-base;
					}
				}
			}
		}
	}
}

// 页面级工具类
.notice-bar {
	height: 60rpx;
	background-color: $text-color-white;
}

.bottom-spacer {
	height: 110rpx;
}

.background-grey {
	padding: 15rpx !important;
	background-color: $menu-sold-out-bg;
}
</style>
