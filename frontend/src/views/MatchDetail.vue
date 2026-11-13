<template>
  <div class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-20 selection:bg-cyan-500 selection:text-white">
    <!-- 顶部电竞导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        <div class="flex items-center space-x-3">
          <div
            class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20 cursor-pointer"
            @click="goBackOrHome"
            title="返回玩家主页"
          >
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white cursor-pointer" @click="goBackOrHome">
              毒瘤榜<span class="text-cyan-400">.GG</span>
            </span>
            <span class="text-[10px] uppercase tracking-wider px-2 py-0.5 rounded bg-cyan-950/90 text-cyan-400 font-bold border border-cyan-800/60">
              MATCH TELEMETRY
            </span>
          </div>
        </div>

        <!-- 右侧：快捷查询对局 ID、复制与返回 -->
        <div class="flex items-center gap-3">
          <div class="hidden sm:flex items-center relative">
            <input
              v-model="searchMatchInput"
              type="text"
              placeholder="输入其他比赛 ID 跳转..."
              class="w-48 bg-[#121721] border border-slate-700/70 text-xs rounded-lg px-3 py-1.5 font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              @keydown.enter="jumpToMatch"
            />
          </div>

          <button
            @click="copyMatchId"
            class="flex items-center gap-1.5 text-xs text-slate-300 bg-[#121721] hover:bg-slate-800 px-3 py-1.5 rounded-lg border border-slate-700/80 font-mono transition-colors"
          >
            <span>对局 ID: <strong class="text-cyan-400">{{ matchId }}</strong></span>
            <span class="text-[10px] px-1.5 py-0.2 rounded bg-cyan-500/15 text-cyan-300">{{ copied ? '已复制!' : '复制' }}</span>
          </button>

          <a
            v-if="rawJson?.replay_url"
            :href="rawJson.replay_url"
            target="_blank"
            class="hidden md:inline-flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/20 font-semibold transition-all"
          >
            🎬 下载录像
          </a>

          <button
            v-if="highlightAccountId"
            @click="goBackOrHome"
            class="text-xs px-3 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
          >
            ← 玩家档案
          </button>
        </div>
      </div>
    </header>

    <!-- 加载中状态 -->
    <div v-if="loading" class="flex flex-col items-center justify-center min-h-[70vh] gap-4">
      <div class="w-12 h-12 border-4 border-cyan-500/20 border-t-cyan-400 rounded-full animate-spin"></div>
      <span class="text-cyan-400 font-black tracking-widest text-xs uppercase animate-pulse">
        正在从本地数据库解析高精度比赛 JSON 遥测数据...
      </span>
    </div>

    <!-- 未找到比赛 -->
    <div v-else-if="!matchMeta" class="max-w-3xl mx-auto mt-20 bg-[#121721] border border-slate-800 rounded-2xl p-10 text-center">
      <div class="text-4xl mb-3">⚠️</div>
      <h2 class="text-xl font-black text-white">未查询到该场比赛的本地高精度存档</h2>
      <p class="text-xs text-slate-400 mt-2 font-mono">
        对局编号 {{ matchId }} 尚未入库或仍在等待 OpenDota 解析队列完成。
      </p>
      <button
        @click="goBackOrHome"
        class="mt-6 px-5 py-2 rounded-xl bg-cyan-500 text-black font-black text-xs hover:bg-cyan-400 transition-all"
      >
        返回玩家战绩主页
      </button>
    </div>

    <!-- 主体内容区 -->
    <main v-else class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">
      <!-- 1. 赛事级全局战报大横幅 (Match Overview Hero Card) -->
      <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        <!-- 动态阵营胜利光晕背景 -->
        <div
          :class="
            matchMeta.radiantWin
              ? 'bg-gradient-to-br from-emerald-500/15 via-cyan-500/5 to-transparent'
              : 'bg-gradient-to-br from-rose-500/15 via-purple-500/5 to-transparent'
          "
          class="absolute inset-0 pointer-events-none"
        ></div>

        <!-- 顶部比赛环境元数据条 -->
        <div class="relative z-10 flex flex-wrap items-center justify-between gap-3 pb-5 mb-6 border-b border-slate-800/80 text-xs font-mono">
          <div class="flex flex-wrap items-center gap-2.5">
            <span class="px-2.5 py-1 rounded-lg bg-[#0b0e14] border border-slate-700/80 text-cyan-400 font-bold">
              🎮 {{ getGameModeName(matchMeta.gameMode) }}
            </span>
            <span class="px-2.5 py-1 rounded-lg bg-[#0b0e14] border border-slate-800 text-slate-300">
              🏆 {{ getLobbyTypeName(matchMeta.lobbyType) }}
            </span>
            <span class="px-2.5 py-1 rounded-lg bg-[#0b0e14] border border-slate-800 text-amber-300">
              ⚙️ {{ getPatchName(rawJson?.patch) }}
            </span>
            <span v-if="rawJson?.first_blood_time !== undefined" class="px-2.5 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400">
              🩸 一血时间: {{ formatGameTime(rawJson.first_blood_time) }}
            </span>
            <span v-if="rawJson?.comeback > 2500" class="px-2.5 py-1 rounded-lg bg-purple-500/15 border border-purple-500/40 text-purple-300 font-bold">
              🔥 史诗翻盘局 (逆转 {{ rawJson.comeback.toLocaleString() }} 经济)
            </span>
            <span v-else-if="rawJson?.stomp > 15000" class="px-2.5 py-1 rounded-lg bg-amber-500/15 border border-amber-500/40 text-amber-300 font-bold">
              ⚡ 碾压局 (最高领先 {{ rawJson.stomp.toLocaleString() }} 经济)
            </span>
          </div>

          <div class="text-slate-400">
            开赛时间: <strong class="text-slate-200">{{ formatDate(matchMeta.startTime) }}</strong>
          </div>
        </div>

        <!-- 中央大比分与 MVP / SVP 荣誉殿堂 -->
        <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
          <!-- 左：全场最佳 MVP 卡 -->
          <div
            v-if="mvpPlayer"
            class="lg:col-span-4 bg-[#0b0e14]/90 border border-amber-500/40 rounded-2xl p-4 shadow-lg relative overflow-hidden"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="px-2 py-0.5 rounded bg-gradient-to-r from-amber-400 to-yellow-500 text-black text-[10px] font-black tracking-wider">
                👑 MATCH MVP 全场最佳
              </span>
              <span class="text-xs font-mono text-amber-400 font-black">战力评级 {{ mvpPlayer.impScore }}</span>
            </div>
            <div class="flex items-center gap-3.5">
              <img :src="getHeroAvatar(mvpPlayer.heroId)" class="w-16 h-11 rounded-xl border-2 border-amber-400 object-cover shadow-md" />
              <div class="min-w-0 flex-1">
                <div class="text-sm font-black text-white truncate">{{ mvpPlayer.personaname }}</div>
                <div class="text-xs text-amber-300 font-bold">{{ getHeroName(mvpPlayer.heroId) }} · {{ getPositionMeta(mvpPlayer.laneRole).short }}</div>
                <div class="text-[11px] font-mono text-slate-300 mt-1">
                  <span class="text-emerald-400 font-bold">{{ mvpPlayer.kills }}</span> /
                  <span class="text-rose-400 font-bold">{{ mvpPlayer.deaths }}</span> /
                  <span class="text-cyan-300 font-bold">{{ mvpPlayer.assists }}</span>
                  <span class="text-slate-500 ml-1.5">({{ mvpPlayer.kda }} KDA · 伤害 {{ mvpPlayer.damageShare }}%)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 中：大比分对战牌 -->
          <div class="lg:col-span-4 flex flex-col items-center justify-center py-2">
            <div
              :class="matchMeta.radiantWin ? 'text-emerald-400 border-emerald-500/40 bg-emerald-500/10' : 'text-rose-500 border-rose-500/40 bg-rose-500/10'"
              class="px-4 py-1 rounded-full border text-xs font-black tracking-widest uppercase mb-3 shadow"
            >
              {{ matchMeta.radiantWin ? '✦ 天辉获胜 RADIANT VICTORY' : '✦ 夜魇获胜 DIRE VICTORY' }}
            </div>

            <div class="flex items-center gap-7">
              <div class="text-center">
                <div class="text-5xl font-black font-mono leading-none" :class="matchMeta.radiantWin ? 'text-emerald-400' : 'text-slate-400'">
                  {{ radiantScore }}
                </div>
                <div class="text-[11px] font-black tracking-widest text-emerald-400 mt-2">天辉 RADIANT</div>
              </div>

              <div class="flex flex-col items-center">
                <span class="px-3 py-1 rounded-lg bg-slate-800/90 border border-slate-700 text-xs font-black text-cyan-400 tracking-widest">
                  VS
                </span>
                <span class="text-lg font-mono font-bold text-white mt-2">{{ formatGameTime(matchMeta.duration) }}</span>
                <span class="text-[10px] text-slate-500 font-mono">比赛总时长</span>
              </div>

              <div class="text-center">
                <div class="text-5xl font-black font-mono leading-none" :class="!matchMeta.radiantWin ? 'text-rose-500' : 'text-slate-400'">
                  {{ direScore }}
                </div>
                <div class="text-[11px] font-black tracking-widest text-rose-500 mt-2">夜魇 DIRE</div>
              </div>
            </div>
          </div>

          <!-- 右：败方最佳 SVP 卡 -->
          <div
            v-if="svpPlayer"
            class="lg:col-span-4 bg-[#0b0e14]/90 border border-purple-500/40 rounded-2xl p-4 shadow-lg relative overflow-hidden"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="px-2 py-0.5 rounded bg-gradient-to-r from-purple-500 to-indigo-500 text-white text-[10px] font-black tracking-wider">
                🛡️ MATCH SVP 败方最佳
              </span>
              <span class="text-xs font-mono text-purple-300 font-black">战力评级 {{ svpPlayer.impScore }}</span>
            </div>
            <div class="flex items-center gap-3.5">
              <img :src="getHeroAvatar(svpPlayer.heroId)" class="w-16 h-11 rounded-xl border-2 border-purple-400 object-cover shadow-md" />
              <div class="min-w-0 flex-1">
                <div class="text-sm font-black text-white truncate">{{ svpPlayer.personaname }}</div>
                <div class="text-xs text-purple-300 font-bold">{{ getHeroName(svpPlayer.heroId) }} · {{ getPositionMeta(svpPlayer.laneRole).short }}</div>
                <div class="text-[11px] font-mono text-slate-300 mt-1">
                  <span class="text-emerald-400 font-bold">{{ svpPlayer.kills }}</span> /
                  <span class="text-rose-400 font-bold">{{ svpPlayer.deaths }}</span> /
                  <span class="text-cyan-300 font-bold">{{ svpPlayer.assists }}</span>
                  <span class="text-slate-500 ml-1.5">({{ svpPlayer.kda }} KDA · 伤害 {{ svpPlayer.damageShare }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 团队四维核心指标对抗进度条 (经济 / 英雄伤害 / 建筑伤害 / 治疗) -->
        <div class="relative z-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6 pt-6 border-t border-slate-800/80 font-mono text-xs">
          <div
            v-for="bar in teamComparisonBars"
            :key="bar.label"
            class="bg-[#0b0e14]/80 border border-slate-800/90 rounded-xl p-3"
          >
            <div class="flex justify-between items-center text-[11px] mb-1.5">
              <span class="text-emerald-400 font-bold">{{ bar.rVal.toLocaleString() }}</span>
              <span class="text-slate-400 font-sans font-bold text-[11px]">{{ bar.label }}</span>
              <span class="text-rose-400 font-bold">{{ bar.dVal.toLocaleString() }}</span>
            </div>
            <div class="w-full h-2 bg-rose-500/80 rounded-full overflow-hidden flex">
              <div class="bg-emerald-400 h-full transition-all" :style="{ width: bar.rPct + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- BP 选人与禁用栏 (Draft Picks & Bans) -->
        <div v-if="draftPicksBans.length > 0" class="relative z-10 mt-6 pt-5 border-t border-slate-800/80">
          <div class="text-[11px] font-mono text-slate-400 mb-3 flex items-center justify-between">
            <span class="font-bold text-slate-300">🎯 赛前 BP 征召序列 (Draft Picks & Bans)</span>
            <span>共 {{ draftPicksBans.length }} 轮</span>
          </div>
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <!-- 天辉 BP -->
            <div class="flex flex-wrap items-center gap-1.5 bg-[#0b0e14]/80 p-2.5 rounded-xl border border-emerald-500/20">
              <span class="text-[10px] font-black text-emerald-400 px-1.5">天辉 BP:</span>
              <div
                v-for="pb in draftPicksBans.filter((x) => x.team === 0)"
                :key="pb.order"
                class="relative rounded overflow-hidden border"
                :class="pb.is_pick ? 'border-emerald-400 w-10 h-7' : 'border-slate-700 w-8 h-6 opacity-50 grayscale'"
                :title="`${pb.is_pick ? '选用' : '禁用'}: ${getHeroName(pb.hero_id)} (第 ${pb.order + 1} 手)`"
              >
                <img :src="getHeroAvatar(pb.hero_id)" class="w-full h-full object-cover" />
                <span
                  :class="pb.is_pick ? 'bg-emerald-500 text-black' : 'bg-rose-900 text-rose-200'"
                  class="absolute bottom-0 right-0 text-[8px] font-mono font-black px-0.5 leading-tight"
                >
                  {{ pb.is_pick ? `P${pb.order + 1}` : `B${pb.order + 1}` }}
                </span>
              </div>
            </div>

            <!-- 夜魇 BP -->
            <div class="flex flex-wrap items-center gap-1.5 bg-[#0b0e14]/80 p-2.5 rounded-xl border border-rose-500/20">
              <span class="text-[10px] font-black text-rose-400 px-1.5">夜魇 BP:</span>
              <div
                v-for="pb in draftPicksBans.filter((x) => x.team === 1)"
                :key="pb.order"
                class="relative rounded overflow-hidden border"
                :class="pb.is_pick ? 'border-rose-400 w-10 h-7' : 'border-slate-700 w-8 h-6 opacity-50 grayscale'"
                :title="`${pb.is_pick ? '选用' : '禁用'}: ${getHeroName(pb.hero_id)} (第 ${pb.order + 1} 手)`"
              >
                <img :src="getHeroAvatar(pb.hero_id)" class="w-full h-full object-cover" />
                <span
                  :class="pb.is_pick ? 'bg-rose-500 text-white' : 'bg-slate-800 text-slate-300'"
                  class="absolute bottom-0 right-0 text-[8px] font-mono font-black px-0.5 leading-tight"
                >
                  {{ pb.is_pick ? `P${pb.order + 1}` : `B${pb.order + 1}` }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. 六大电竞战术模块切换导航栏 (Tactical Navigation Tabs) -->
      <nav class="bg-[#121721] border border-slate-800/80 rounded-2xl p-1.5 flex flex-wrap items-center gap-1.5 shadow-lg">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="
            activeTab === tab.key
              ? 'bg-gradient-to-r from-cyan-500/20 to-emerald-500/15 text-cyan-300 border-cyan-500/50 shadow-md'
              : 'text-slate-400 hover:text-slate-200 hover:bg-[#0b0e14]/60 border-transparent'
          "
          class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl border text-xs font-black tracking-wide transition-all flex items-center justify-center gap-2"
        >
          <span>{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </button>
      </nav>

      <!-- 3. 标签页内容视图 -->
      <!-- Tab 1: 赛后全景计分板 (Overview & Deep Telemetry Drawer) -->
      <div v-if="activeTab === 'overview'" class="space-y-6">
        <MatchTeamScoreboard
          :isRadiant="true"
          :isWinner="Boolean(matchMeta.radiantWin)"
          :teamPlayers="radiantPlayers"
          :highlightAccountId="highlightAccountId"
        />
        <MatchTeamScoreboard
          :isRadiant="false"
          :isWinner="!matchMeta.radiantWin"
          :teamPlayers="direPlayers"
          :highlightAccountId="highlightAccountId"
        />
      </div>

      <!-- Tab 2: 经济经验走势与分时成长曲线 -->
      <MatchGraphsTab
        v-else-if="activeTab === 'graphs'"
        :rawJson="rawJson"
        :players="enrichedPlayers"
        :highlightAccountId="highlightAccountId"
      />

      <!-- Tab 3: 出装时间轴与技能加点 -->
      <MatchBuildsTab
        v-else-if="activeTab === 'builds'"
        :players="enrichedPlayers"
        :highlightAccountId="highlightAccountId"
      />

      <!-- Tab 4: 战斗矩阵与伤害构成拆解 -->
      <MatchCombatTab
        v-else-if="activeTab === 'combat'"
        :players="enrichedPlayers"
      />

      <!-- Tab 5: 团战复盘与关键战略事件轴 -->
      <MatchTeamfightsTab
        v-else-if="activeTab === 'teamfights'"
        :rawJson="rawJson"
        :players="enrichedPlayers"
      />

      <!-- Tab 6: 三路对线详情与视野辅助遥测 -->
      <MatchLaningVisionTab
        v-else-if="activeTab === 'laning'"
        :players="enrichedPlayers"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchMatchDetail } from '../api/player';
import {
  getHeroName,
  getHeroAvatar,
  getItemMeta,
  getAbilityMeta,
  getPermanentBuffMeta,
  getGameModeName,
  getLobbyTypeName,
  getPatchName,
  getPositionMeta,
  getGoldReasonName,
  formatGameTime
} from '../utils/dotaHelper';

import MatchTeamScoreboard from '../components/match/MatchTeamScoreboard.vue';
import MatchGraphsTab from '../components/match/MatchGraphsTab.vue';
import MatchBuildsTab from '../components/match/MatchBuildsTab.vue';
import MatchCombatTab from '../components/match/MatchCombatTab.vue';
import MatchTeamfightsTab from '../components/match/MatchTeamfightsTab.vue';
import MatchLaningVisionTab from '../components/match/MatchLaningVisionTab.vue';

const props = defineProps<{
  matchId: string;
}>();

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const copied = ref(false);
const searchMatchInput = ref('');
const matchResponse = ref<any>(null);
const rawJson = ref<any>(null);

// 当前高亮视角玩家 ID (从历史战绩或个人看板跳转过来时自动携带)
const highlightAccountId = computed(() => String(route.query.accountId || ''));

// 六大战术标签页配置
type TabKey = 'overview' | 'graphs' | 'builds' | 'combat' | 'teamfights' | 'laning';
const activeTab = ref<TabKey>('overview');
const tabs: Array<{ key: TabKey; icon: string; label: string }> = [
  { key: 'overview', icon: '📊', label: '全景战报与深度计分板' },
  { key: 'graphs', icon: '📈', label: '经济经验差与发育曲线' },
  { key: 'builds', icon: '🛡️', label: '出装时间轴与技能加点' },
  { key: 'combat', icon: '⚔️', label: '对位击杀与伤害拆解' },
  { key: 'teamfights', icon: '🔥', label: '团战复盘与战略事件轴' },
  { key: 'laning', icon: '👁️', label: '三路对线与视野辅助' }
];

// 比赛基础概况对象
const matchMeta = computed(() => {
  if (matchResponse.value?.match) return matchResponse.value.match;
  if (rawJson.value) {
    return {
      matchId: rawJson.value.match_id,
      radiantWin: rawJson.value.radiant_win,
      duration: rawJson.value.duration,
      startTime: rawJson.value.start_time,
      gameMode: rawJson.value.game_mode,
      lobbyType: rawJson.value.lobby_type
    };
  }
  return null;
});

// BP 选人禁用列表
const draftPicksBans = computed<any[]>(() => {
  return Array.isArray(rawJson.value?.picks_bans) ? rawJson.value.picks_bans : [];
});

/**
 * 深度融合 dota_match_player 表计算指标与 dota_match_raw.raw_data 内的 10 人高精度原始 JSON
 */
const enrichedPlayers = computed(() => {
  const dbPlayers: any[] = Array.isArray(matchResponse.value?.players) ? matchResponse.value.players : [];
  const rawPlayers: any[] = Array.isArray(rawJson.value?.players) ? rawJson.value.players : [];

  // 构建 playerSlot -> rawPlayer 映射
  const rawBySlot: Record<number, any> = {};
  rawPlayers.forEach((rp) => {
    if (rp && rp.player_slot !== undefined) {
      rawBySlot[Number(rp.player_slot)] = rp;
    }
  });

  // 合并基础列表
  const baseList = (dbPlayers.length > 0 ? dbPlayers : rawPlayers).map((p) => {
    const slot = Number(p.playerSlot ?? p.player_slot ?? 0);
    const rp = rawBySlot[slot] || p;
    const isRadiant = p.isRadiant !== undefined ? Boolean(p.isRadiant) : slot < 128;

    const kills = Number(p.kills ?? rp.kills ?? 0);
    const deaths = Number(p.deaths ?? rp.deaths ?? 0);
    const assists = Number(p.assists ?? rp.assists ?? 0);
    const kda = Number(p.kda ?? (deaths === 0 ? kills + assists : ((kills + assists) / deaths).toFixed(2)));

    // 统计来自敌方英雄的承受伤害与总承受伤害
    const dmgTakenDict: Record<string, number> = rp.damage_taken || {};
    let heroDamageTaken = 0;
    let totalDamageTaken = 0;
    Object.entries(dmgTakenDict).forEach(([k, val]) => {
      const n = Number(val || 0);
      totalDamageTaken += n;
      if (k.startsWith('npc_dota_hero_')) heroDamageTaken += n;
    });

    // 出装购买时间映射 (用于给主装备栏标注购买分钟数)
    const purchaseTimeMap: Record<string, number> = {};
    if (Array.isArray(rp.purchase_log)) {
      rp.purchase_log.forEach((entry: any) => {
        if (entry?.key && entry.time !== undefined) {
          purchaseTimeMap[entry.key] = Number(entry.time);
        }
      });
    }

    // 解析 6 格主物品栏
    const mainItems = [0, 1, 2, 3, 4, 5].map((i) => {
      const itemId = rp[`item_${i}`];
      const meta = getItemMeta(itemId);
      if (!meta) return null;
      const pTime = purchaseTimeMap[meta.key];
      return {
        ...meta,
        purchaseTimeStr: pTime !== undefined ? formatGameTime(pTime) : '',
        purchaseMinStr: pTime !== undefined && meta.cost >= 1400 ? `${Math.max(0, Math.floor(pTime / 60))}m` : ''
      };
    });

    // 解析 3 格背包栏与中立物品
    const backpackItems = [0, 1, 2].map((i) => getItemMeta(rp[`backpack_${i}`]));
    const neutralItem = getItemMeta(rp.item_neutral);

    // 解析永久 Buff
    const permanentBuffs = (Array.isArray(rp.permanent_buffs) ? rp.permanent_buffs : []).map((b: any) =>
      getPermanentBuffMeta(Number(b.permanent_buff), Number(b.stack_count || 0))
    );

    const hasScepter =
      rp.aghanims_scepter === 1 ||
      permanentBuffs.some((b: any) => b.key === 'ultimate_scepter') ||
      mainItems.some((it) => it?.key === 'ultimate_scepter');
    const hasShard =
      rp.aghanims_shard === 1 ||
      permanentBuffs.some((b: any) => b.key === 'aghanims_shard');

    // 10 分钟对线快照
    const goldT: number[] = Array.isArray(rp.gold_t) ? rp.gold_t : [];
    const lhT: number[] = Array.isArray(rp.lh_t) ? rp.lh_t : [];
    const dnT: number[] = Array.isArray(rp.dn_t) ? rp.dn_t : [];
    const gold10m = goldT.length > 10 ? goldT[10] : Number(p.goldPerMin ?? rp.gold_per_min ?? 0) * 10;
    const lh10m = lhT.length > 10 ? lhT[10] : 0;
    const dn10m = dnT.length > 10 ? dnT[10] : 0;
    const laneEffPct = Number(
      rp.lane_efficiency_pct ?? Math.round(Number(p.laneEfficiency ?? rp.lane_efficiency ?? 0.5) * 100)
    );

    // OpenDota 全球同英雄百分位排名 (benchmarks)
    const bmRaw = rp.benchmarks || {};
    const bmLabels: Array<{ key: string; label: string }> = [
      { key: 'gold_per_min', label: '分均经济 (GPM)' },
      { key: 'xp_per_min', label: '分均经验 (XPM)' },
      { key: 'hero_damage_per_min', label: '分均英雄伤害' },
      { key: 'last_hits_per_min', label: '分均正补' },
      { key: 'kills_per_min', label: '分均击杀' },
      { key: 'tower_damage', label: '建筑推进伤害' }
    ];
    const benchmarksList = bmLabels
      .filter((b) => bmRaw[b.key] !== undefined)
      .map((b) => ({
        key: b.key,
        label: b.label,
        raw: Number(bmRaw[b.key].raw || 0).toFixed(b.key.includes('kills') || b.key.includes('last_hits') ? 2 : 0),
        pct: Number(((Number(bmRaw[b.key].pct || 0)) * 100).toFixed(1))
      }));

    // 金币来源拆解
    const goldReasonsList = Object.entries(rp.gold_reasons || {})
      .map(([code, val]) => ({
        code,
        name: getGoldReasonName(code),
        val: Number(val || 0)
      }))
      .filter((g) => g.val !== 0)
      .sort((a, b) => Math.abs(b.val) - Math.abs(a.val));

    // 最大单次爆发伤害
    const maxHitObj = rp.max_hero_hit || {};
    const maxHitAbility = getAbilityMeta(maxHitObj.inflictor) || getItemMeta(maxHitObj.inflictor);
    const maxHit = {
      value: Number(maxHitObj.value || 0),
      source: maxHitAbility?.name || (maxHitObj.inflictor ? String(maxHitObj.inflictor) : '普通攻击')
    };

    const heroDamage = Number(p.heroDamage ?? rp.hero_damage ?? 0);
    const towerDamage = Number(p.towerDamage ?? rp.tower_damage ?? 0);
    const heroHealing = Number(rp.hero_healing ?? 0);
    const netWorth = Number(rp.net_worth ?? rp.total_gold ?? 0);
    const stuns = Number(Number(rp.stuns || 0).toFixed(1));
    const obsPlaced = Number(rp.obs_placed ?? 0);
    const senPlaced = Number(rp.sen_placed ?? 0);
    const obsKilled = Number(rp.observer_kills ?? 0);
    const senKilled = Number(rp.sentry_kills ?? 0);
    const tfParticipation = Math.round(Number(rp.teamfight_participation || 0) * 100);

    // 计算个人战力评分 (IMP Score，用于评选全场 MVP 与败方 SVP)
    const impScore = Number(
      (
        kda * 1.6 +
        heroDamage / 2800 +
        towerDamage / 1500 +
        heroHealing / 2500 +
        (obsPlaced + senPlaced + obsKilled * 2) * 0.35 +
        stuns * 0.12 +
        tfParticipation * 0.08
      ).toFixed(1)
    );

    return {
      playerSlot: slot,
      accountId: Number(p.accountId ?? rp.account_id ?? 0),
      personaname: p.personaname || rp.personaname || '匿名玩家',
      isRadiant,
      win: Boolean(p.win ?? (rp.win === 1)),
      heroId: Number(p.heroId ?? rp.hero_id ?? 0),
      level: Number(rp.level ?? 1),
      rankTier: Number(rp.rank_tier ?? 0),
      partyId: p.partyId ?? rp.party_id ?? null,
      partySize: Number(p.partySize ?? rp.party_size ?? 1),
      lane: Number(p.lane ?? rp.lane ?? 0),
      laneRole: Number(p.laneRole ?? rp.lane_role ?? 2),
      laneResult: Number(p.laneResult ?? 2),
      laneEffPct,
      gold10m,
      lh10m,
      dn10m,
      kills,
      deaths,
      assists,
      kda,
      tfParticipation,
      netWorth,
      goldPerMin: Number(p.goldPerMin ?? rp.gold_per_min ?? 0),
      xpPerMin: Number(p.xpPerMin ?? rp.xp_per_min ?? 0),
      lastHits: Number(rp.last_hits ?? 0),
      denies: Number(rp.denies ?? 0),
      neutralKills: Number(rp.neutral_kills ?? 0),
      ancientKills: Number(rp.ancient_kills ?? 0),
      campsStacked: Number(rp.camps_stacked ?? 0),
      heroDamage,
      towerDamage,
      heroHealing,
      heroDamageTaken,
      totalDamageTaken,
      stuns,
      obsPlaced,
      senPlaced,
      obsKilled,
      senKilled,
      buybackCount: Number(rp.buyback_count ?? 0),
      mainItems,
      backpackItems,
      neutralItem,
      permanentBuffs,
      hasScepter,
      hasShard,
      benchmarksList,
      goldReasonsList,
      maxHit,
      impScore,
      isMatchMvp: false,
      isMatchSvp: false,
      damageShare: 0,
      tankShare: 0,
      netWorthShare: 0,
      partyTag: '',
      partyColorClass: '',
      honorBadges: [] as Array<{ text: string; cls: string }>,
      raw: rp
    };
  });

  // 按 playerSlot 排序 (0~4 天辉, 128~132 夜魇)
  baseList.sort((a, b) => a.playerSlot - b.playerSlot);

  // 结合阵营与地图上下路 (OpenDota lane: 1=下路, 2=中路, 3=上路) 及 10 分钟经济精准推导 Pos 1~5 与对线胜负
  const laneCoreGoldMap: Record<string, number> = {};
  const laneCoreSlotMap: Record<string, number> = {};
  baseList.forEach((x) => {
    const key = `${x.isRadiant ? 'R' : 'D'}_${x.lane}`;
    if (laneCoreGoldMap[key] === undefined || x.gold10m > laneCoreGoldMap[key]) {
      laneCoreGoldMap[key] = x.gold10m;
      laneCoreSlotMap[key] = x.playerSlot;
    }
  });

  const sumLaneGold = (isRad: boolean, targetLane: number) =>
    baseList
      .filter((x) => x.isRadiant === isRad && x.lane === targetLane)
      .reduce((acc, x) => acc + (x.gold10m || 0), 0);

  const radBotGold = sumLaneGold(true, 1);
  const direBotGold = sumLaneGold(false, 1);
  const radMidGold = sumLaneGold(true, 2);
  const direMidGold = sumLaneGold(false, 2);
  const radTopGold = sumLaneGold(true, 3);
  const direTopGold = sumLaneGold(false, 3);

  baseList.forEach((x) => {
    const key = `${x.isRadiant ? 'R' : 'D'}_${x.lane}`;
    const isLaneCore = laneCoreSlotMap[key] === x.playerSlot;
    // 天辉下路(lane=1)与夜魇上路(lane=3)为优势路；天辉上路(lane=3)与夜魇下路(lane=1)为劣势路
    const isSafeLane = (x.isRadiant && x.lane === 1) || (!x.isRadiant && x.lane === 3);
    const isOffLane = (x.isRadiant && x.lane === 3) || (!x.isRadiant && x.lane === 1);

    if (x.lane === 2) {
      x.laneRole = 2;
    } else if (isSafeLane) {
      x.laneRole = isLaneCore ? 1 : 5;
    } else if (isOffLane) {
      x.laneRole = isLaneCore ? 3 : 4;
    } else {
      x.laneRole = 4;
    }

    let diff = 0;
    if (x.lane === 2) {
      diff = x.isRadiant ? radMidGold - direMidGold : direMidGold - radMidGold;
    } else if (x.lane === 1) {
      diff = x.isRadiant ? radBotGold - direBotGold : direBotGold - radBotGold;
    } else if (x.lane === 3) {
      diff = x.isRadiant ? radTopGold - direTopGold : direTopGold - radTopGold;
    }

    if (diff >= 800) x.laneResult = 1;
    else if (diff <= -800) x.laneResult = 3;
    else x.laneResult = 2;
  });

  // 计算团队占比 (伤害占比、承伤占比、净资产占比)
  const calcShares = (teamList: typeof baseList) => {
    const totalDmg = teamList.reduce((acc, x) => acc + x.heroDamage, 0) || 1;
    const totalTank = teamList.reduce((acc, x) => acc + x.heroDamageTaken, 0) || 1;
    const totalNw = teamList.reduce((acc, x) => acc + x.netWorth, 0) || 1;
    teamList.forEach((x) => {
      x.damageShare = Number(((x.heroDamage * 100) / totalDmg).toFixed(1));
      x.tankShare = Number(((x.heroDamageTaken * 100) / totalTank).toFixed(1));
      x.netWorthShare = Number(((x.netWorth * 100) / totalNw).toFixed(1));
    });
  };

  const radList = baseList.filter((x) => x.isRadiant);
  const direList = baseList.filter((x) => !x.isRadiant);
  calcShares(radList);
  calcShares(direList);

  // 评选胜方最高评分 MVP 与败方最高评分 SVP
  const winners = baseList.filter((x) => x.win);
  const losers = baseList.filter((x) => !x.win);
  if (winners.length > 0) {
    const topW = [...winners].sort((a, b) => b.impScore - a.impScore)[0];
    topW.isMatchMvp = true;
  }
  if (losers.length > 0) {
    const topL = [...losers].sort((a, b) => b.impScore - a.impScore)[0];
    topL.isMatchSvp = true;
  }

  // 标记开黑车队编号 (相同 partyId 且人数 > 1)
  const partyCounts: Record<string, number> = {};
  baseList.forEach((x) => {
    if (x.partyId !== null && x.partyId !== undefined) {
      partyCounts[String(x.partyId)] = (partyCounts[String(x.partyId)] || 0) + 1;
    }
  });
  const partyPalette = [
    'text-purple-300 bg-purple-500/15 border-purple-500/40',
    'text-cyan-300 bg-cyan-500/15 border-cyan-500/40',
    'text-amber-300 bg-amber-500/15 border-amber-500/40',
    'text-pink-300 bg-pink-500/15 border-pink-500/40'
  ];
  const partyIndexMap: Record<string, number> = {};
  let nextPartyNum = 1;
  baseList.forEach((x) => {
    const pid = String(x.partyId);
    if (x.partyId !== null && x.partyId !== undefined && (partyCounts[pid] > 1 || x.partySize > 1)) {
      if (!partyIndexMap[pid]) {
        partyIndexMap[pid] = nextPartyNum++;
      }
      const idx = partyIndexMap[pid];
      x.partyTag = `车队#${idx}`;
      x.partyColorClass = partyPalette[(idx - 1) % partyPalette.length];
    }
  });

  // 颁发荣誉勋章 (一血、暴走、超神、输出王、承伤王、推塔王)
  const maxDmgPlayer = [...baseList].sort((a, b) => b.heroDamage - a.heroDamage)[0];
  const maxTankPlayer = [...baseList].sort((a, b) => b.heroDamageTaken - a.heroDamageTaken)[0];
  const maxTowerPlayer = [...baseList].sort((a, b) => b.towerDamage - a.towerDamage)[0];

  baseList.forEach((x) => {
    const badges: Array<{ text: string; cls: string }> = [];
    if (x.raw?.firstblood_claimed) {
      badges.push({ text: '🩸 一血', cls: 'text-rose-300 bg-rose-500/15 border-rose-500/40' });
    }
    const multiKills = x.raw?.multi_kills || {};
    if (multiKills['5']) {
      badges.push({ text: '🔥 暴走!', cls: 'text-amber-300 bg-amber-500/20 border-amber-400 font-black' });
    } else if (multiKills['4']) {
      badges.push({ text: '⚡ 疯狂杀戮', cls: 'text-purple-300 bg-purple-500/20 border-purple-400' });
    } else if (multiKills['3']) {
      badges.push({ text: '🗡️ 三杀', cls: 'text-cyan-300 bg-cyan-500/15 border-cyan-500/30' });
    }
    if (x === maxDmgPlayer && x.heroDamage > 0) {
      badges.push({ text: '💥 全场最高输出', cls: 'text-rose-300 bg-rose-500/15 border-rose-500/40' });
    }
    if (x === maxTankPlayer && x.heroDamageTaken > 0) {
      badges.push({ text: '🛡️ 全场最高承伤', cls: 'text-sky-300 bg-sky-500/15 border-sky-500/40' });
    }
    if (x === maxTowerPlayer && x.towerDamage >= 3000) {
      badges.push({ text: '🏰 拆迁统领', cls: 'text-amber-300 bg-amber-500/15 border-amber-500/40' });
    }
    x.honorBadges = badges;
  });

  return baseList;
});

const radiantPlayers = computed(() => enrichedPlayers.value.filter((p) => p.isRadiant));
const direPlayers = computed(() => enrichedPlayers.value.filter((p) => !p.isRadiant));

const mvpPlayer = computed(() => enrichedPlayers.value.find((p) => p.isMatchMvp) || null);
const svpPlayer = computed(() => enrichedPlayers.value.find((p) => p.isMatchSvp) || null);

const radiantScore = computed(() => {
  if (rawJson.value?.radiant_score !== undefined) return Number(rawJson.value.radiant_score);
  return radiantPlayers.value.reduce((acc, p) => acc + p.kills, 0);
});

const direScore = computed(() => {
  if (rawJson.value?.dire_score !== undefined) return Number(rawJson.value.dire_score);
  return direPlayers.value.reduce((acc, p) => acc + p.kills, 0);
});

// 团队四维对比条
const teamComparisonBars = computed(() => {
  const sum = (arr: any[], field: string) => arr.reduce((acc, p) => acc + Number(p[field] || 0), 0);
  const makeBar = (label: string, rVal: number, dVal: number) => {
    const total = rVal + dVal || 1;
    return {
      label,
      rVal,
      dVal,
      rPct: Number(((rVal * 100) / total).toFixed(1))
    };
  };
  return [
    makeBar('💰 团队总净资产', sum(radiantPlayers.value, 'netWorth'), sum(direPlayers.value, 'netWorth')),
    makeBar('⚔️ 团队英雄总伤害', sum(radiantPlayers.value, 'heroDamage'), sum(direPlayers.value, 'heroDamage')),
    makeBar('🏰 团队建筑总伤害', sum(radiantPlayers.value, 'towerDamage'), sum(direPlayers.value, 'towerDamage')),
    makeBar('🛡️ 团队英雄总承伤', sum(radiantPlayers.value, 'heroDamageTaken'), sum(direPlayers.value, 'heroDamageTaken'))
  ];
});

const formatDate = (timestamp: number | undefined) => {
  if (!timestamp) return '-';
  const date = new Date(timestamp * 1000);
  return date.toLocaleString('zh-CN', { hour12: false });
};

const copyMatchId = async () => {
  try {
    await navigator.clipboard.writeText(String(props.matchId));
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (e) {}
};

const jumpToMatch = () => {
  const target = searchMatchInput.value.trim();
  if (target && /^\d+$/.test(target)) {
    router.push({ name: 'MatchDetail', params: { matchId: target }, query: route.query });
    searchMatchInput.value = '';
  }
};

const goBackOrHome = () => {
  if (highlightAccountId.value) {
    router.push(`/player/${highlightAccountId.value}`);
  } else {
    router.push('/');
  }
};

const loadDetail = async () => {
  loading.value = true;
  try {
    const res = await fetchMatchDetail(props.matchId);
    if (res?.data) {
      matchResponse.value = res.data;
      if (res.data.rawJson) {
        rawJson.value = res.data.rawJson;
      } else if (res.data.match?.rawData) {
        try {
          rawJson.value = JSON.parse(res.data.match.rawData);
        } catch (err) {
          console.error('解析 rawData JSON 字符串失败', err);
        }
      }
    }
  } catch (e) {
    console.error('加载比赛详情失败', e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadDetail);

watch(
  () => props.matchId,
  (newId) => {
    if (newId) loadDetail();
  }
);
</script>
