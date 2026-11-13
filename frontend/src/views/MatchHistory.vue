<template>
  <div class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-16 selection:bg-cyan-500 selection:text-white">
    <!-- 顶部导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20 cursor-pointer" @click="goHome">
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white">毒瘤榜<span class="text-cyan-400">.GG</span></span>
            <span class="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-cyan-950/80 text-cyan-400 font-bold border border-cyan-800/50">MATCH HISTORY</span>
          </div>
        </div>

        <div class="flex items-center space-x-4">
          <div class="text-xs text-slate-400 bg-[#121721] px-3 py-1.5 rounded-lg border border-slate-800">
            玩家 ID: <span class="text-white font-mono">{{ accountId }}</span>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-3">
          <span class="w-2 h-8 bg-cyan-500 rounded-full"></span>
          全量对局记录
        </h2>
        <div class="text-sm text-slate-400">
          共入库 <span class="text-cyan-400 font-bold">{{ matches.length }}</span> 场比赛
        </div>
      </div>

      <div class="bg-[#121721] border border-slate-800/80 rounded-2xl overflow-hidden shadow-2xl shadow-black/60">
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm font-mono">
            <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 uppercase text-[10px] tracking-widest">
              <tr>
                <th class="py-4 px-6">对局 ID</th>
                <th class="py-4 px-6">英雄 / 位置</th>
                <th class="py-4 px-6 text-center">结果</th>
                <th class="py-4 px-6">K / D / A</th>
                <th class="py-4 px-6">对线表现</th>
                <th class="py-4 px-6">GPM / XPM</th>
                <th class="py-4 px-6">参战 / 伤害</th>
                <th class="py-4 px-6">开始时间</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/40">
              <tr 
                v-for="m in matches" 
                :key="m.matchId" 
                class="hover:bg-[#151c2a] transition-all cursor-pointer group"
                @click="openMatchDetail(m.matchId)"
              >
                <td class="py-4 px-6 text-slate-500 group-hover:text-cyan-400 transition-colors">
                  {{ m.matchId }}
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <div class="relative">
                      <img :src="getHeroAvatar(m.heroId)" class="w-10 h-7 rounded border border-slate-700 object-cover shadow-sm" />
                      <span v-if="m.isMvp" class="absolute -top-2 -right-2 bg-amber-500 text-black text-[8px] font-black px-1 rounded shadow-sm">MVP</span>
                    </div>
                    <div>
                      <div class="font-bold text-white group-hover:text-cyan-300 transition-colors">{{ getHeroName(m.heroId) }}</div>
                      <div class="text-[10px] text-slate-500">{{ getPosName(m.laneRole) }}</div>
                    </div>
                  </div>
                </td>
                <td class="py-4 px-6 text-center">
                  <span 
                    :class="m.win ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20' : 'text-rose-400 bg-rose-500/10 border-rose-500/20'" 
                    class="px-3 py-1 rounded-full text-xs font-black border"
                  >
                    {{ m.win ? 'VICTORY' : 'DEFEAT' }}
                  </span>
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center gap-2">
                    <span class="text-white font-bold">{{ m.kills }} / {{ m.deaths }} / {{ m.assists }}</span>
                    <span class="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400">{{ m.kda }} KDA</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <span :class="m.laneResult === 1 ? 'text-emerald-400' : m.laneResult === 3 ? 'text-rose-400' : 'text-amber-400'" class="text-xs font-bold">
                    {{ m.laneResult === 1 ? '优势 (WIN)' : m.laneResult === 3 ? '劣势 (LOSS)' : '均势 (EVEN)' }}
                  </span>
                  <div class="text-[10px] text-slate-500 mt-1">效率: {{ (m.laneEfficiency * 100).toFixed(1) }}%</div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-amber-400 font-bold">{{ m.goldPerMin }} <span class="text-[10px] text-slate-500 font-normal">GPM</span></div>
                  <div class="text-slate-300">{{ m.xpPerMin }} <span class="text-[10px] text-slate-500 font-normal">XPM</span></div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-white">{{ m.heroDamage?.toLocaleString() || '-' }} <span class="text-[10px] text-slate-500 font-normal">DMG</span></div>
                  <div class="text-slate-400 text-[10px]">{{ m.towerDamage?.toLocaleString() || '-' }} TOWER</div>
                </td>
                <td class="py-4 px-6 text-slate-500 text-[11px]">
                  {{ formatDate(m.startTime) }}
                </td>
              </tr>
              <tr v-if="loading">
                <td colspan="8" class="py-20 text-center">
                  <div class="flex flex-col items-center gap-4">
                    <div class="w-10 h-10 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
                    <span class="text-cyan-400 font-bold tracking-widest text-xs">正在调取数据库存档...</span>
                  </div>
                </td>
              </tr>
              <tr v-if="!loading && matches.length === 0">
                <td colspan="8" class="py-20 text-center text-slate-500">
                  数据库暂无已解析的历史战绩
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import heroConstants from '../assets/dota2_heroes_constants.json';
import { fetchAllMatches } from '../api/player';

const props = defineProps<{
  accountId: string;
}>();

const router = useRouter();
const matches = ref<any[]>([]);
const loading = ref(true);

const heroMap = heroConstants as Record<string, any>;
const getHeroName = (id: number | string) => heroMap[String(id)]?.localized_name || `英雄 #${id}`;
const getHeroAvatar = (id: number | string) => {
  const item = heroMap[String(id)];
  return item?.img ? `https://cdn.cloudflare.steamstatic.com${item.img}` : '';
};

const getPosName = (laneRole: number) => {
  const map: any = { 1: 'POS 1 Carry', 2: 'POS 2 Mid', 3: 'POS 3 Offlane', 4: 'POS 4 Support', 5: 'POS 5 Support' };
  return map[laneRole] || 'Unknown';
};

const formatDate = (timestamp: number) => {
  if (!timestamp) return '-';
  const date = new Date(timestamp * 1000);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

// 点击某场比赛新开标签页跳转至单场详细数据面板，并携带当前玩家 ID 以便高亮本视角
const openMatchDetail = (matchId: number) => {
  const url = router.resolve({
    name: 'MatchDetail',
    params: { matchId },
    query: { accountId: props.accountId }
  }).href;
  window.open(url, '_blank');
};

const goHome = () => {
  router.push(`/player/${props.accountId}`);
};

const loadMatches = async () => {
  loading.value = true;
  try {
    const res = await fetchAllMatches(props.accountId);
    if (res?.data) {
      matches.value = res.data;
    }
  } catch (e) {
    console.error('加载历史战绩失败', e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadMatches);
</script>