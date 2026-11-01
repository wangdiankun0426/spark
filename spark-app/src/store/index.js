import { createStore } from 'vuex'
import user from './modules/user'
import theme from './modules/theme'

export default createStore({
  modules: {
    user,
    theme
  }
})
