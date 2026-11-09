<template>
  <div class="bg-bgDark text-slate-200 min-h-screen font-sans bg-mesh antialiased pb-16">
    <!-- 顶部导航 -->
    <header class="border-b border-cardBorder bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-tr from-blue-600 to-emerald-400 p-[1.5px] flex items-center justify-center">
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-neonGreen font-black">
              N
            </div>
          </div>
          <span class="text-xl font-black text-white">NEXUS<span class="text-neonBlue">.GG</span></span>
        </div>

        <!-- 录入与搜索输入框 -->
        <div class="flex items-center space-x-3">
          <div class="relative">
            <input 
              v-model="inputAccountId" 
              type="text" 
              placeholder="输入 Steam32 ID (例如 290810906) 回车录入并解析..." 
              class="w-80 bg-[#141d30] border border-cardBorder text-sm rounded-lg px-3 py-1.5 pl-8 focus:outline-none focus:border-neonBlue text-slate-300"
              @keydown.enter="handleStartSync"
            />
            <span class="absolute left-2.5 top-2 text-slate-500 text-xs">🔍</span>
          </div>
          <button 
            @click="handleStartSync" 
            :disabled="syncing"
            class="px-3.5 py-1.5 bg-neonBlue/20 text-neonBlue border border-neonBlue/40 rounded-lg text-xs font-semibold hover:bg-neonBlue/30 transition-all disabled:opacity-50"
          >
            {{ syncing ? '正在录入解析...' : '录入/刷新' }}
          </button>
        </div>
      </div>
    </header>

    <!-- 解析状态监控浮窗条 -->
    <div v-if="taskProgress.total > 0 && taskProgress.pending > 0" class="max-w-7xl mx-auto px-4 mt-3">
      <div class="bg-amber-500/10 border border-amber-500/30 text-amber-300 p-3 rounded-xl flex items-center justify-between text-xs">
        <div class="flex items-center gap-2">
          <span class="w-2.5 h-2.5 rounded-full bg-amber-400 animate-ping"></span>
          <span>OpenDota 比赛解析队列正在进行中：已提交 <strong>{{ taskProgress.total }}</strong> 局，已解析完成 <strong>{{ taskProgress.success }}</strong> 局，剩余 <strong>{{ taskProgress.pending }}</strong> 局（2分钟轮询中...）</span>
        </div>
        <button @click="loadData" class="underline text-neonGreen ml-2">手动刷新看板</button>
      </div>
    </div>

    <!-- 玩家基本信息 Banner -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6">
      <div class="rounded-2xl bg-[#121929] border border-cardBorder p-6 flex flex-col md:flex-row justify-between items-center gap-6 shadow-2xl">
        <div class="flex items-center gap-5">
          <img :src="summary?.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=120'" class="w-20 h-20 rounded-2xl border-2 border-indigo-500/40 object-cover" />
          <div>
            <h1 class="text-2xl font-black text-white">{{ summary?.personaname || '未录入玩家' }}</h1>
            <div class="text-xs text-slate-400 mt-1 flex gap-3">
              <span>Steam32 ID: <strong class="text-neonGold">{{ currentAccountId || '--' }}</strong></span>
              <span>天梯段位: Rank {{ summary?.rankTier || '0' }}</span>
              <span>数据周期: 近 7 天</span>
            </div>
          </div>
        </div>

        <div class="flex gap-4">
          <div class="bg-[#0e1627] p-3 rounded-xl border border-cardBorder text-center min-w-[100px]">
            <span class="text-xs text-slate-400 block">7天场次</span>
            <span class="text-xl font-bold font-mono text-neonGold">{{ summary?.totalMatches || 0 }}</span>
          </div>
          <div class="bg-[#0e1627] p-3 rounded-xl border border-cardBorder text-center min-w-[100px]">
            <span class="text-xs text-slate-400 block">胜率</span>
            <span class="text-xl font-bold font-mono text-neonGreen">{{ summary?.winRate || 0 }}%</span>
          </div>
          <div class="bg-[#0e1627] p-3 rounded-xl border border-cardBorder text-center min-w-[100px]">
            <span class="text-xs text-slate-400 block">线优率</span>
            <span class="text-xl font-bold font-mono text-neonBlue">{{ summary?.laneWinRate || 0 }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- KPI 卡片流 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-cardDark border border-cardBorder p-5 rounded-2xl">
        <span class="text-xs text-slate-400">场均 KDA 与战斗数据</span>
        <h3 class="text-2xl font-black text-white font-mono mt-1">{{ summary?.avgKda || 0 }} <span class="text-xs text-slate-500">KDA</span></h3>
        <p class="text-xs text-slate-400 mt-2 font-mono">
          <span class="text-neonGreen">{{ summary?.avgKills || 0 }}</span> / 
          <span class="text-neonRed">{{ summary?.avgDeaths || 0 }}</span> / 
          <span class="text-neonBlue">{{ summary?.avgAssists || 0 }}</span>
        </p>
      </div>

      <div class="bg-cardDark border border-cardBorder p-5 rounded-2xl">
        <span class="text-xs text-slate-400">分均经济 / 经验 (GPM / XPM)</span>
        <h3 class="text-2xl font-black text-neonGold font-mono mt-1">{{ summary?.avgGpm || 0 }} <span class="text-xs text-slate-400">/ {{ summary?.avgXpm || 0 }}</span></h3>
        <p class="text-xs text-slate-400 mt-2">打钱效率超越同段位平均线</p>
      </div>

      <div class="bg-cardDark border border-cardBorder p-5 rounded-2xl">
        <span class="text-xs text-slate-400">阵营胜率分布</span>
        <div class="flex justify-between text-xs mt-2 font-mono">
          <span class="text-neonGreen">天辉: {{ summary?.radiantMatches ? ((summary.radiantWins / summary.radiantMatches)*100).toFixed(1) : 0 }}%</span>
          <span class="text-neonRed">夜宴: {{ summary?.direMatches ? ((summary.direWins / summary.direMatches)*100).toFixed(1) : 0 }}%</span>
        </div>
        <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden mt-1.5 flex">
          <div class="bg-neonGreen h-full" :style="{ width: (summary?.radiantMatches ? (summary.radiantWins / summary.radiantMatches)*100 : 50) + '%' }"></div>
          <div class="bg-neonRed h-full" :style="{ width: (summary?.direMatches ? (summary.direWins / summary.direMatches)*100 : 50) + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 选项卡与数据表格 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8">
      <div class="bg-cardDark border border-cardBorder rounded-2xl overflow-hidden shadow-xl">
        <div class="border-b border-cardBorder bg-[#0e1627] px-4 flex gap-2">
          <button @click="currentTab = 'heroes'" :class="['px-4 py-3.5 text-xs sm:text-sm font-semibold transition-all', currentTab === 'heroes' ? 'tab-active' : 'text-slate-400 hover:text-white']">
            🛡️ 玩家英雄池总榜
          </button>
          <button @click="currentTab = 'teammates'" :class="['px-4 py-3.5 text-xs sm:text-sm font-semibold transition-all', currentTab === 'teammates' ? 'tab-active' : 'text-slate-400 hover:text-white']">
            🔥 队友红黑榜 (合拍 vs 避雷)
          </button>
          <button @click="currentTab = 'matches'" :class="['px-4 py-3.5 text-xs sm:text-sm font-semibold transition-all', currentTab === 'matches' ? 'tab-active' : 'text-slate-400 hover:text-white']">
            📜 7天战报流 (Match Stream)
          </button>
        </div>

        <!-- Tab 1: 英雄池 -->
        <div v-show="currentTab === 'heroes'" class="p-5 overflow-x-auto">
          <table class="w-full text-left text-xs sm:text-sm font-mono">
            <thead class="bg-[#0e1627] text-slate-400 text-[11px] border-b border-cardBorder">
              <tr>
                <th class="py-3 px-3">英雄</th>
                <th class="py-3 px-3">场次 (胜/负)</th>
                <th class="py-3 px-3">胜率</th>
                <th class="py-3 px-3">KDA (K/D/A)</th>
                <th class="py-3 px-3">线优率 / 线劣率</th>
                <th class="py-3 px-3">GPM / XPM</th>
                <th class="py-3 px-3">评分</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-cardBorder">
              <tr v-for="h in heroes" :key="h.heroId" class="hover:bg-[#141e33] transition-colors">
                <td class="py-3 px-3 font-bold text-white">{{ getHeroName(h.heroId) }}</td>
                <td class="py-3 px-3">{{ h.matches }} 场 ({{ h.wins }}胜 / {{ h.losses }}负)</td>
                <td class="py-3 px-3 font-bold" :class="h.winRate >= 55 ? 'text-neonGreen' : 'text-slate-300'">{{ h.winRate }}%</td>
                <td class="py-3 px-3">{{ h.kda }} ({{ h.avgKills }}/{{ h.avgDeaths }}/{{ h.avgAssists }})</td>
                <td class="py-3 px-3"><span class="text-neonGreen">{{ h.laneWinRate }}% 优</span> / <span class="text-neonRed">{{ h.laneLossRate }}% 劣</span></td>
                <td class="py-3 px-3 text-neonGold">{{ h.avgGpm }} / {{ h.avgXpm }}</td>
                <td class="py-3 px-3"><span class="px-2 py-0.5 rounded text-xs bg-blue-500/20 text-neonBlue">{{ h.perfScore }}</span></td>
              </tr>
              <tr v-if="heroes.length === 0">
                <td colspan="7" class="text-center py-6 text-slate-500">暂无英雄池数据，请在上方录入玩家 ID</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Tab 2: 队友红黑榜 -->
        <div v-show="currentTab === 'teammates'" class="p-5 grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 红榜 -->
          <div class="bg-[#121e2a] border border-emerald-500/20 p-4 rounded-xl">
            <h4 class="font-bold text-neonGreen text-sm mb-3">🌟 通天红榜 (共同胜率 ≥ 60%)</h4>
            <div class="space-y-2.5">
              <div v-for="t in redList" :key="t.teammateId" class="flex justify-between items-center p-2.5 bg-[#0b131a] rounded-lg text-xs font-mono">
                <div>
                  <span class="text-white font-bold">{{ t.teammateName }}</span>
                  <span class="text-slate-500 text-[10px] block">ID: {{ t.teammateId }}</span>
                </div>
                <div class="text-right">
                  <span class="text-neonGreen font-bold">{{ t.winRate }}% 胜率</span>
                  <span class="text-slate-400 text-[10px] block">同队 {{ t.matchesTogether }} 场 ({{ t.winsTogether }}胜)</span>
                </div>
              </div>
              <div v-if="redList.length === 0" class="text-slate-500 text-xs py-4 text-center">暂无满足条件的红榜搭档</div>
            </div>
          </div>

          <!-- 黑榜 -->
          <div class="bg-[#241317] border border-red-500/20 p-4 rounded-xl">
            <h4 class="font-bold text-neonRed text-sm mb-3">💀 避雷黑榜 (共同胜率 &lt; 50%)</h4>
            <div class="space-y-2.5">
              <div v-for="t in blackList" :key="t.teammateId" class="flex justify-between items-center p-2.5 bg-[#170a0d] rounded-lg text-xs font-mono">
                <div>
                  <span class="text-white font-bold">{{ t.teammateName }}</span>
                  <span class="text-slate-500 text-[10px] block">ID: {{ t.teammateId }}</span>
                </div>
                <div class="text-right">
                  <span class="text-neonRed font-bold">{{ t.winRate }}% 胜率</span>
                  <span class="text-slate-400 text-[10px] block">同队 {{ t.matchesTogether }} 场 ({{ t.winsTogether }}胜)</span>
                </div>
              </div>
              <div v-if="blackList.length === 0" class="text-slate-500 text-xs py-4 text-center">暂无满足条件的黑榜搭档</div>
            </div>
          </div>
        </div>

        <!-- Tab 3: 比赛流水 -->
        <div v-show="currentTab === 'matches'" class="p-5 space-y-3">
          <div v-for="m in recentMatches" :key="m.matchId" :class="['bg-[#0e1627] border rounded-xl p-3.5 flex justify-between items-center text-xs font-mono', m.win ? 'border-emerald-500/30' : 'border-red-500/30']">
            <div class="flex items-center gap-3">
              <span :class="['px-2 py-0.5 rounded font-bold text-[11px]', m.win ? 'bg-emerald-500/20 text-neonGreen' : 'bg-red-500/20 text-neonRed']">
                {{ m.win ? 'VICTORY' : 'DEFEAT' }}
              </span>
              <span class="font-bold text-white text-sm">{{ getHeroName(m.heroId) }}</span>
              <span class="text-slate-400">Match: {{ m.matchId }}</span>
            </div>
            <div class="flex gap-6 text-slate-300">
              <span>对线: <strong :class="m.laneResult === 1 ? 'text-neonGreen' : m.laneResult === 3 ? 'text-neonRed' : 'text-amber-400'">{{ m.laneResult === 1 ? '线优' : m.laneResult === 3 ? '线劣' : '均势' }}</strong></span>
              <span>KDA: <strong class="text-white">{{ m.kills }}/{{ m.deaths }}/{{ m.assists }}</strong> ({{ m.kda }})</span>
              <span>GPM: <strong class="text-neonGold">{{ m.goldPerMin }}</strong></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { 
  syncPlayerMatches, fetchTaskStatus, fetchDashboardData,
  type PlayerSummary, type PlayerHeroStats, type RecentMatch, type TeammateSynergy 
} from '../api/player';

const inputAccountId = ref('290810906');
const currentAccountId = ref('');
const syncing = ref(false);
const currentTab = ref<'heroes' | 'teammates' | 'matches'>('heroes');

const summary = ref<PlayerSummary | null>(null);
const heroes = ref<PlayerHeroStats[]>([]);
const recentMatches = ref<RecentMatch[]>([]);
const teammates = ref<TeammateSynergy[]>([]);
const taskProgress = ref({ total: 0, pending: 0, success: 0 });

let pollTimer: any = null;

// 英雄映射字典
const heroMap: Record<number, string> = {
  1: '敌法师', 2: '斧王', 6: '卓尔游侠', 11: '影魔', 27: '暗影萨满',
  39: '痛苦女王', 41: '虚空假面', 49: '龙骑士', 63: '编织者', 69: '末日使者',
  74: '祈求者', 87: '干扰者', 101: '天怒法师'
};
const getHeroName = (id: number) => heroMap[id] || `英雄 #${id}`;

const redList = computed(() => teammates.value.filter(t => t.winRate >= 60));
const blackList = computed(() => teammates.value.filter(t => t.winRate < 50));

// 1. 发起录入与同步
const handleStartSync = async () => {
  const accountId = inputAccountId.value.trim();
  if (!accountId) return;

  currentAccountId.value = accountId;
  syncing.value = true;
  try {
    await syncPlayerMatches(accountId);
    await loadData();
    startTaskPolling(accountId);
  } catch (err: any) {
    alert('触发同步失败: ' + err.message);
  } finally {
    syncing.value = false;
  }
};

// 2. 加载看板数据
const loadData = async () => {
  if (!currentAccountId.value) return;
  try {
    const res = await fetchDashboardData(currentAccountId.value);
    summary.value = res.data.summary;
    heroes.value = res.data.topHeroes || [];
    recentMatches.value = res.data.recentMatches || [];
    teammates.value = res.data.teammates || [];
  } catch (e) {
    console.error('加载看板失败', e);
  }
};

// 3. 轮询 OpenDota 解析进度
const startTaskPolling = (accountId: string) => {
  if (pollTimer) clearInterval(pollTimer);
  pollTimer = setInterval(async () => {
    try {
      const res = await fetchTaskStatus(accountId);
      taskProgress.value = res.data;
      if (res.data.pending === 0 && res.data.total > 0) {
        clearInterval(pollTimer);
        await loadData();
      }
    } catch (e) {
      console.error('查询任务状态出错', e);
    }
  }, 10000); // 每 10 秒刷新一次任务进度
};

onMounted(() => {
  if (inputAccountId.value) {
    currentAccountId.value = inputAccountId.value;
    loadData();
  }
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>