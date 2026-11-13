import { createStore } from 'vuex';
import userModule from './modules/user';
import permissionModule from './modules/permission';

const store = createStore({
  modules: {
    user: userModule,
    permission: permissionModule
  }
});

export default store;