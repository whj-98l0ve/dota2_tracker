import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import PlayerDashboard from '../views/PlayerDashboard.vue';
import MatchHistory from '../views/MatchHistory.vue';
import MatchDetail from '../views/MatchDetail.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import SpecialThanks from '../views/SpecialThanks.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/special-thanks',
    name: 'SpecialThanks',
    component: SpecialThanks
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard
  },
  {
    path: '/player/:accountId/admin',
    name: 'PlayerAdminDashboard',
    component: AdminDashboard
  },
  {
    path: '/player/:accountId',
    name: 'PlayerDashboard',
    component: PlayerDashboard,
    props: true
  },
  {
    path: '/player/:accountId/history',
    name: 'MatchHistory',
    component: MatchHistory,
    props: true
  },
  {
    path: '/match/:matchId',
    name: 'MatchDetail',
    component: MatchDetail,
    props: true
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;