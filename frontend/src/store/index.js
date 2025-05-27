import { createStore } from 'vuex'

// 创建一个新的 store 实例
const store = createStore({
  state() {
    return {
      user: {
        userInfo: null,
        token: localStorage.getItem('token') || '',
        role: localStorage.getItem('role') || ''
      }
    }
  },
  mutations: {
    // 设置用户信息
    SET_USER_INFO(state, userInfo) {
      state.user.userInfo = userInfo
    },
    // 设置token
    SET_TOKEN(state, token) {
      state.user.token = token
      localStorage.setItem('token', token)
    },
    // 设置用户角色
    SET_ROLE(state, role) {
      state.user.role = role
      localStorage.setItem('role', role)
    },
    // 清除用户信息(登出时调用)
    CLEAR_USER_INFO(state) {
      state.user.userInfo = null
      state.user.token = ''
      state.user.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
    }
  },
  actions: {
    // 登录
    login({ commit }, { token, userInfo, role }) {
      commit('SET_TOKEN', token)
      commit('SET_USER_INFO', userInfo)
      commit('SET_ROLE', role)
    },
    // 登出
    logout({ commit }) {
      commit('CLEAR_USER_INFO')
    },
    // 更新用户信息
    updateUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
    }
  },
  getters: {
    // 获取用户信息
    userInfo: state => state.user.userInfo,
    // 获取token
    token: state => state.user.token,
    // 获取用户角色
    role: state => state.user.role,
    // 判断用户是否已登录
    isAuthenticated: state => !!state.user.token
  }
})

export default store 