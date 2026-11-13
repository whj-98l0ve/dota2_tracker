import { createRouter, createWebHistory } from 'vue-router';
import PlayerDashboard from '../views/PlayerDashboard.vue';
import MatchHistory from '../views/MatchHistory.vue';
import MatchDetail from '../views/MatchDetail.vue';

const routes = [
  {
    path: '/',
    redirect: '/player/199666669' // Default to a demo ID or a landing page
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