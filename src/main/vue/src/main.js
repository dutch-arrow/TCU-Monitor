import App from './MainPage.vue'

import {createApp} from 'vue'
import BootstrapVueNext from 'bootstrap-vue-next'

import Axios from 'axios'
import mitt from 'mitt';

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'

const app = createApp(App);
const emitter = mitt();
app.use(BootstrapVueNext)
app.config.globalProperties.axios = Axios;
app.config.globalProperties.emitter = emitter;
app.mount('#app')