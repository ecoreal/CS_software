import { createApp } from 'vue'
import { createPinia } from 'pinia';
import PortalVue from "portal-vue";
import App from './App.vue'
import router from './router'
import '@vue-flow/core/dist/style.css'
import '@fortawesome/fontawesome-free/css/all.min.css';


const app = createApp(App);
const pinia = createPinia();

app.use(PortalVue);
app.use(pinia);
app.use(createPinia());
app.use(router);
app.mount('#app');

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

app.use(ElementPlus)
