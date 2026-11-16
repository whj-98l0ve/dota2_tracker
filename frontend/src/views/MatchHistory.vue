<template>
  <div class="text-slate-100 min-h-screen font-sans antialiased pb-16">
    <!-- 顶部极光毛玻璃导航栏（移动端紧凑适配） -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div
            class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 cursor-pointer shrink-0"
            @click="goHome"
          >
            <div class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-lg sm:text-xl font-black tracking-wide text-white cursor-pointer truncate" @click="goHome">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden sm:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-white/[0.06] text-cyan-200 font-bold border border-white/15 backdrop-blur-md">
              MATCH HISTORY
            </span>
          </div>
        </div>

        <div class="flex items-center space-x-2 sm:space-x-3 shrink-0">
          <button
            @click="goHome"
            class="px-2.5 sm:px-3.5 py-1 sm:py-1.5 bg-white/[0.05] hover:bg-white/[0.12] text-slate-200 border border-white/15 hover:border-cyan-400/35 rounded-full text-[11px] sm:text-xs font-medium backdrop-blur-md transition-all"
          >
            ← 玩家档案
          </button>
          <div class="text-[11px] sm:text-xs text-slate-300 aurora-subcard px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-full">
            ID: <span class="text-cyan-300 font-mono font-bold">{{ accountId }}</span>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 mt-5 sm:mt-8">
      <div class="flex items-center justify-between mb-4 sm:mb-6">
        <h2 class="text-xl sm:text-2xl font-black text-white tracking-tight flex items-center gap-2.5 sm:gap-3">
          <span class="w-2 h-6 sm:h-8 bg-gradient-to-b from-cyan-300 to-emerald-400 rounded-full shadow-[0_0_12px_rgba(34,211,238,0.5)]"></span>
          全量对局记录
        </h2>
        <div class="text-xs font-mono text-slate-300 aurora-subcard px-3.5 sm:px-4 py-1.5 rounded-full">
          共入库 <span class="text-cyan-300 font-bold">{{ matches.length }}</span> 场
        </div>
      </div>

      <!-- 移动端专属：极光毛玻璃战绩卡片列表 (md:hidden) -->
      <div class="md:hidden space-y-3 font-mono">
        <div v-if="loading" class="aurora-glass rounded-2xl py-16 text-center">
          <div class="flex flex-col items-center gap-3">
            <div class="w-9 h-9 border-4 border-cyan-400/20 border-t-cyan-300 rounded-full animate-spin"></div>
            <span class="text-cyan-300 font-bold tracking-widest text-xs">正在调取数据库存档...</span>
          </div>
        </div>

        <div v-else-if="matches.length === 0" class="aurora-glass rounded-2xl py-16 text-center text-slate-500 text-xs">
          数据库暂无已解析的历史战绩
        </div>

        <div
          v-for="m in matches"
          :key="'m-card-' + m.matchId"
          @click="openMatchDetail(m.matchId)"
          class="aurora-glass rounded-2xl p-4 space-y-3 active:scale-[0.99] transition-all cursor-pointer"
        >
          <!-- 第一行：英雄信息 + 胜负徽章 -->
          <div class="flex items-center justify-between gap-2">
            <div class="flex items-center gap-3 min-w-0">
              <div class="relative shrink-0">
                <img :src="getHeroAvatar(m.heroId)" class="w-12 h-8 rounded-lg border border-white/15 object-cover shadow-md" />
                <span v-if="m.isMvp" class="absolute -top-1.5 -right-1.5 aurora-btn-gold text-[8px] font-black px-1.5 py-0.2 rounded shadow-sm">MVP</span>
              </div>
              <div class="min-w-0">
                <div class="font-bold text-white text-sm truncate">{{ getHeroName(m.heroId) }}</div>
                <div class="text-[10px] text-slate-400 flex items-center gap-2">
                  <span>{{ getPosName(m.laneRole) }}</span>
                  <span>·</span>
                  <span>#{{ m.matchId }}</span>
                </div>
              </div>
            </div>

            <span
              :class="m.win ? 'text-emerald-300 bg-emerald-500/15 border-emerald-400/35' : 'text-rose-300 bg-rose-500/15 border-rose-400/35'"
              class="px-2.5 py-1 rounded-full text-[11px] font-black border shrink-0"
            >
              {{ m.win ? 'VICTORY' : 'DEFEAT' }}
            </span>
          </div>

          <!-- 第二行：核心四维指标网格 -->
          <div class="grid grid-cols-3 gap-2 aurora-subcard rounded-xl p-2.5 text-xs">
            <div>
              <div class="text-[10px] text-slate-400">K / D / A</div>
              <div class="text-white font-bold mt-0.5">{{ m.kills }}/{{ m.deaths }}/{{ m.assists }}</div>
              <div class="text-[10px] text-cyan-300">{{ m.kda }} KDA</div>
            </div>
            <div class="text-center border-x border-white/[0.06] px-1">
              <div class="text-[10px] text-slate-400">对线表现</div>
              <div :class="m.laneResult === 1 ? 'text-emerald-300' : m.laneResult === 3 ? 'text-rose-400' : 'text-amber-300'" class="font-bold mt-0.5">
                {{ m.laneResult === 1 ? '线优' : m.laneResult === 3 ? '线劣' : '均势' }}
              </div>
              <div class="text-[10px] text-slate-400">效率 {{ (m.laneEfficiency * 100).toFixed(0) }}%</div>
            </div>
            <div class="text-right">
              <div class="text-[10px] text-slate-400">GPM / XPM</div>
              <div class="text-amber-300 font-bold mt-0.5">{{ m.goldPerMin }}/{{ m.xpPerMin }}</div>
              <div class="text-[10px] text-slate-300">伤 {{ m.heroDamage ? (m.heroDamage / 1000).toFixed(1) + 'k' : '-' }}</div>
            </div>
          </div>

          <!-- 第三行：时间与查看详情提示 -->
          <div class="flex items-center justify-between text-[11px] text-slate-400 pt-0.5">
            <span>🕒 {{ formatDate(m.startTime) }}</span>
            <span class="text-cyan-300 font-semibold">查看遥测详情 →</span>
          </div>
        </div>
      </div>

      <!-- 桌面端：8列完整专业宽表 (hidden md:block) -->
      <div class="aurora-glass rounded-2xl overflow-hidden hidden md:block">
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm font-mono">
            <thead class="bg-[#060a15]/75 text-slate-400 border-b border-white/10 uppercase text-[10px] tracking-widest">
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
            <tbody class="divide-y divide-white/[0.06]">
              <tr 
                v-for="m in matches" 
                :key="m.matchId" 
                class="hover:bg-cyan-400/[0.07] transition-all cursor-pointer group"
                @click="openMatchDetail(m.matchId)"
              >
                <td class="py-4 px-6 text-slate-400 group-hover:text-cyan-300 transition-colors">
                  {{ m.matchId }}
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center gap-3">
                    <div class="relative">
                      <img :src="getHeroAvatar(m.heroId)" class="w-11 h-7 rounded-md border border-white/15 object-cover shadow-md group-hover:border-cyan-300/50 transition-colors" />
                      <span v-if="m.isMvp" class="absolute -top-2 -right-2 aurora-btn-gold text-[8px] font-black px-1.5 py-0.2 rounded shadow-sm">MVP</span>
                    </div>
                    <div>
                      <div class="font-bold text-white group-hover:text-cyan-200 transition-colors">{{ getHeroName(m.heroId) }}</div>
                      <div class="text-[10px] text-slate-400">{{ getPosName(m.laneRole) }}</div>
                    </div>
                  </div>
                </td>
                <td class="py-4 px-6 text-center">
                  <span 
                    :class="m.win ? 'text-emerald-300 bg-emerald-500/15 border-emerald-400/35 shadow-[0_0_12px_rgba(16,185,129,0.15)]' : 'text-rose-300 bg-rose-500/15 border-rose-400/35 shadow-[0_0_12px_rgba(244,63,94,0.15)]'" 
                    class="px-3 py-1 rounded-full text-xs font-black border"
                  >
                    {{ m.win ? 'VICTORY' : 'DEFEAT' }}
                  </span>
                </td>
                <td class="py-4 px-6">
                  <div class="flex items-center gap-2">
                    <span class="text-white font-bold">{{ m.kills }} / {{ m.deaths }} / {{ m.assists }}</span>
                    <span class="text-[10px] px-2 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-cyan-200">{{ m.kda }} KDA</span>
                  </div>
                </td>
                <td class="py-4 px-6">
                  <span :class="m.laneResult === 1 ? 'text-emerald-300' : m.laneResult === 3 ? 'text-rose-400' : 'text-amber-300'" class="text-xs font-bold">
                    {{ m.laneResult === 1 ? '优势 (WIN)' : m.laneResult === 3 ? '劣势 (LOSS)' : '均势 (EVEN)' }}
                  </span>
                  <div class="text-[10px] text-slate-400 mt-1">效率: {{ (m.laneEfficiency * 100).toFixed(1) }}%</div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-amber-300 font-bold">{{ m.goldPerMin }} <span class="text-[10px] text-slate-500 font-normal">GPM</span></div>
                  <div class="text-slate-300">{{ m.xpPerMin }} <span class="text-[10px] text-slate-500 font-normal">XPM</span></div>
                </td>
                <td class="py-4 px-6">
                  <div class="text-white">{{ m.heroDamage?.toLocaleString() || '-' }} <span class="text-[10px] text-slate-500 font-normal">DMG</span></div>
                  <div class="text-slate-400 text-[10px]">{{ m.towerDamage?.toLocaleString() || '-' }} TOWER</div>
                </td>
                <td class="py-4 px-6 text-slate-400 text-[11px]">
                  {{ formatDate(m.startTime) }}
                </td>
              </tr>
              <tr v-if="loading">
                <td colspan="8" class="py-20 text-center">
                  <div class="flex flex-col items-center gap-4">
                    <div class="w-10 h-10 border-4 border-cyan-400/20 border-t-cyan-300 rounded-full animate-spin"></div>
                    <span class="text-cyan-300 font-bold tracking-widest text-xs">正在调取数据库存档...</span>
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
