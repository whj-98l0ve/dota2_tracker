<template>
  <div class="text-slate-100 min-h-screen font-sans antialiased pb-20">
    <!-- 顶部极光毛玻璃导航栏（移动端紧凑适配） -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div
            class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 cursor-pointer shrink-0"
            @click="goBackOrHome"
            title="返回玩家主页"
          >
            <div class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-lg sm:text-xl font-black tracking-wide text-white cursor-pointer truncate" @click="goBackOrHome">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden md:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-white/[0.06] text-cyan-200 font-bold border border-white/15 backdrop-blur-md">
              MATCH TELEMETRY
            </span>
          </div>
        </div>

        <!-- 右侧：复制对局 ID 与返回 -->
        <div class="flex items-center gap-1.5 sm:gap-3 shrink-0">
          <button
            @click="copyMatchId"
            class="flex items-center gap-1 sm:gap-1.5 text-[11px] sm:text-xs text-slate-200 aurora-subcard hover:border-cyan-400/40 px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-full font-mono transition-all"
          >
            <span><span class="hidden sm:inline">对局 </span>ID: <strong class="text-cyan-300">{{ matchId }}</strong></span>
            <span class="text-[9px] sm:text-[10px] px-1.5 sm:px-2 py-0.2 rounded-full bg-cyan-400/20 text-cyan-200">{{ copied ? '已复制' : '复制' }}</span>
          </button>

          <a
            v-if="rawJson?.replay_url"
            :href="rawJson.replay_url"
            target="_blank"
            class="hidden md:inline-flex items-center gap-1 text-xs px-3.5 py-1.5 rounded-full bg-emerald-400/15 text-emerald-300 border border-emerald-400/35 hover:bg-emerald-400/25 font-semibold backdrop-blur-md transition-all"
          >
            🎬 下载录像
          </a>

          <button
            v-if="highlightAccountId"
            @click="goBackOrHome"
            class="text-[11px] sm:text-xs px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-full bg-white/[0.06] hover:bg-white/[0.14] text-slate-100 border border-white/15 hover:border-cyan-400/35 backdrop-blur-md transition-all"
          >
            ← 档案
          </button>
        </div>
      </div>
    </header>

    <!-- 加载中状态 -->
    <div v-if="loading" class="flex flex-col items-center justify-center min-h-[70vh] gap-4 px-4 text-center">
      <div class="w-12 h-12 border-4 border-cyan-400/20 border-t-cyan-300 rounded-full animate-spin"></div>
      <span class="text-cyan-300 font-black tracking-widest text-xs uppercase animate-pulse">
        正在从本地数据库解析高精度比赛 JSON 遥测数据...
      </span>
    </div>

    <!-- 未找到比赛 -->
    <div v-else-if="!matchMeta" class="max-w-3xl mx-auto mt-12 sm:mt-20 mx-3 aurora-glass rounded-3xl p-6 sm:p-10 text-center">
      <div class="text-4xl mb-3">⚠️</div>
      <h2 class="text-lg sm:text-xl font-black text-white">未查询到该场比赛的本地高精度存档</h2>
      <p class="text-xs text-slate-400 mt-2 font-mono">
        对局编号 {{ matchId }} 尚未入库或仍在等待 OpenDota 解析队列完成。
      </p>
      <button
        @click="goBackOrHome"
        class="mt-6 px-5 py-2.5 rounded-xl aurora-btn-primary font-black text-xs"
      >
        返回玩家战绩主页
      </button>
    </div>

    <!-- 主体内容区 -->
    <main v-else class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 mt-4 sm:mt-6 space-y-5 sm:space-y-6">
      <!-- 1. 赛事级全局战报大横幅 (Match Overview Hero Card) -->
      <section class="aurora-glass rounded-2xl sm:rounded-3xl p-4 sm:p-8 relative overflow-hidden">
        <!-- 动态阵营胜利极光光晕背景 -->
        <div
          :class="
            matchMeta.radiantWin
              ? 'bg-gradient-to-br from-emerald-400/20 via-cyan-400/10 to-transparent'
              : 'bg-gradient-to-br from-rose-500/20 via-fuchsia-500/10 to-transparent'
          "
          class="absolute inset-0 pointer-events-none"
        ></div>

        <!-- 顶部比赛环境元数据条 -->
        <div class="relative z-10 flex flex-wrap items-center justify-between gap-2.5 pb-4 sm:pb-5 mb-4 sm:mb-6 border-b border-white/10 text-[11px] sm:text-xs font-mono">
          <div class="flex flex-wrap items-center gap-1.5 sm:gap-2.5">
            <span class="px-2.5 sm:px-3 py-1 rounded-full aurora-subcard text-cyan-300 font-bold">
              🎮 {{ getGameModeName(matchMeta.gameMode) }}
            </span>
            <span class="px-2.5 sm:px-3 py-1 rounded-full aurora-subcard text-slate-200">
              🏆 {{ getLobbyTypeName(matchMeta.lobbyType) }}
            </span>
            <span class="px-2.5 sm:px-3 py-1 rounded-full aurora-subcard text-amber-300">
              ⚙️ {{ getPatchName(rawJson?.patch) }}
            </span>
            <span v-if="rawJson?.comeback > 2500" class="px-2.5 sm:px-3 py-1 rounded-full bg-purple-500/20 border border-purple-400/40 text-purple-200 font-bold backdrop-blur-md">
              🔥 翻盘 (逆转 {{ rawJson.comeback.toLocaleString() }})
            </span>
            <span v-else-if="rawJson?.stomp > 15000" class="px-2.5 sm:px-3 py-1 rounded-full bg-amber-500/20 border border-amber-400/40 text-amber-200 font-bold backdrop-blur-md">
              ⚡ 碾压 (领先 {{ rawJson.stomp.toLocaleString() }})
            </span>
          </div>

          <div class="text-slate-300/80 text-[11px] sm:text-xs">
            开赛: <strong class="text-white">{{ formatDate(matchMeta.startTime) }}</strong>
          </div>
        </div>

        <!-- 中央大比分与 MVP / SVP 荣誉殿堂（移动端比分置顶 order-first lg:order-none） -->
        <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-4 sm:gap-6 items-center">
          <!-- 左：全场最佳 MVP 卡 -->
          <div
            v-if="mvpPlayer"
            class="lg:col-span-4 aurora-subcard border border-amber-400/45 rounded-2xl p-3.5 sm:p-4 shadow-[0_0_25px_rgba(245,158,11,0.15)] relative overflow-hidden"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="px-2.5 py-0.5 rounded-full aurora-btn-gold text-[10px] font-black tracking-wider">
                👑 MATCH MVP 全场最佳
              </span>
              <span class="text-xs font-mono text-amber-300 font-black">战力评级 {{ mvpPlayer.impScore }}</span>
            </div>
            <div class="flex items-center gap-3">
              <img :src="getHeroAvatar(mvpPlayer.heroId)" class="w-14 sm:w-16 h-10 sm:h-11 rounded-xl border-2 border-amber-300 object-cover shadow-md shrink-0" />
              <div class="min-w-0 flex-1">
                <div class="text-sm font-black text-white truncate">{{ mvpPlayer.personaname }}</div>
                <div class="text-xs text-amber-200 font-bold truncate">{{ getHeroName(mvpPlayer.heroId) }} · {{ getPositionMeta(mvpPlayer.laneRole).short }}</div>
                <div class="text-[11px] font-mono text-slate-300 mt-1">
                  <span class="text-emerald-300 font-bold">{{ mvpPlayer.kills }}</span> /
                  <span class="text-rose-400 font-bold">{{ mvpPlayer.deaths }}</span> /
                  <span class="text-cyan-300 font-bold">{{ mvpPlayer.assists }}</span>
                  <span class="text-slate-400 ml-1">({{ mvpPlayer.kda }} KDA · 伤 {{ mvpPlayer.damageShare }}%)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 中：大比分对战牌（移动端优先置顶展示） -->
          <div class="order-first lg:order-none lg:col-span-4 flex flex-col items-center justify-center py-1 sm:py-2">
            <div
              :class="matchMeta.radiantWin ? 'text-emerald-300 border-emerald-400/45 bg-emerald-400/15 shadow-[0_0_20px_rgba(16,185,129,0.2)]' : 'text-rose-300 border-rose-400/45 bg-rose-500/15 shadow-[0_0_20px_rgba(244,63,94,0.2)]'"
              class="px-3.5 sm:px-4 py-1 rounded-full border text-[11px] sm:text-xs font-black tracking-widest uppercase mb-3 backdrop-blur-md"
            >
              {{ matchMeta.radiantWin ? '✦ 天辉获胜 RADIANT VICTORY' : '✦ 夜魇获胜 DIRE VICTORY' }}
            </div>

            <div class="flex items-center gap-5 sm:gap-7">
              <div class="text-center">
                <div class="text-4xl sm:text-5xl font-black font-mono leading-none drop-shadow-[0_0_15px_rgba(16,185,129,0.3)]" :class="matchMeta.radiantWin ? 'text-emerald-300' : 'text-slate-400'">
                  {{ radiantScore }}
                </div>
                <div class="text-[10px] sm:text-[11px] font-black tracking-widest text-emerald-300 mt-1.5 sm:mt-2">天辉 RADIANT</div>
              </div>

              <div class="flex flex-col items-center">
                <span class="px-3 py-0.5 sm:py-1 rounded-full aurora-subcard text-xs font-black text-cyan-300 tracking-widest">
                  VS
                </span>
                <span class="text-base sm:text-lg font-mono font-bold text-white mt-1.5 sm:mt-2">{{ formatGameTime(matchMeta.duration) }}</span>
                <span class="text-[10px] text-slate-400 font-mono">比赛总时长</span>
              </div>

              <div class="text-center">
                <div class="text-4xl sm:text-5xl font-black font-mono leading-none drop-shadow-[0_0_15px_rgba(244,63,94,0.3)]" :class="!matchMeta.radiantWin ? 'text-rose-400' : 'text-slate-400'">
                  {{ direScore }}
                </div>
                <div class="text-[10px] sm:text-[11px] font-black tracking-widest text-rose-400 mt-1.5 sm:mt-2">夜魇 DIRE</div>
              </div>
            </div>
          </div>

          <!-- 右：败方最佳 SVP 卡 -->
          <div
            v-if="svpPlayer"
            class="lg:col-span-4 aurora-subcard border border-purple-400/45 rounded-2xl p-3.5 sm:p-4 shadow-[0_0_25px_rgba(168,85,247,0.15)] relative overflow-hidden"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="px-2.5 py-0.5 rounded-full bg-gradient-to-r from-purple-500 to-indigo-500 text-white text-[10px] font-black tracking-wider shadow">
                🛡️ MATCH SVP 败方最佳
              </span>
              <span class="text-xs font-mono text-purple-300 font-black">战力评级 {{ svpPlayer.impScore }}</span>
            </div>
            <div class="flex items-center gap-3">
              <img :src="getHeroAvatar(svpPlayer.heroId)" class="w-14 sm:w-16 h-10 sm:h-11 rounded-xl border-2 border-purple-400 object-cover shadow-md shrink-0" />
              <div class="min-w-0 flex-1">
                <div class="text-sm font-black text-white truncate">{{ svpPlayer.personaname }}</div>
                <div class="text-xs text-purple-200 font-bold truncate">{{ getHeroName(svpPlayer.heroId) }} · {{ getPositionMeta(svpPlayer.laneRole).short }}</div>
                <div class="text-[11px] font-mono text-slate-300 mt-1">
                  <span class="text-emerald-300 font-bold">{{ svpPlayer.kills }}</span> /
                  <span class="text-rose-400 font-bold">{{ svpPlayer.deaths }}</span> /
                  <span class="text-cyan-300 font-bold">{{ svpPlayer.assists }}</span>
                  <span class="text-slate-400 ml-1">({{ svpPlayer.kda }} KDA · 伤 {{ svpPlayer.damageShare }}%)</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- BP 选人与禁用栏 (Draft Picks & Bans) -->
        <div v-if="draftPicksBans.length > 0" class="relative z-10 mt-5 sm:mt-6 pt-4 sm:pt-5 border-t border-white/10">
          <div class="text-[11px] font-mono text-slate-300 mb-3 flex items-center justify-between">
            <span class="font-bold text-white">🎯 赛前 BP 征召序列 (Draft Picks & Bans)</span>
            <span>共 {{ draftPicksBans.length }} 轮</span>
          </div>
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-3 sm:gap-4">
            <!-- 天辉 BP -->
            <div class="flex flex-wrap items-center gap-1.5 aurora-subcard p-2.5 rounded-xl border border-emerald-400/25">
              <span class="text-[10px] font-black text-emerald-300 px-1">天辉 BP:</span>
              <div
                v-for="pb in draftPicksBans.filter((x) => x.team === 0)"
                :key="pb.order"
                class="relative rounded overflow-hidden border"
                :class="pb.is_pick ? 'border-emerald-400 w-9 sm:w-10 h-6 sm:h-7 shadow-sm' : 'border-white/10 w-7 sm:w-8 h-5 sm:h-6 opacity-50 grayscale'"
                :title="`${pb.is_pick ? '选用' : '禁用'}: ${getHeroName(pb.hero_id)} (第 ${pb.order + 1} 手)`"
              >
                <img :src="getHeroAvatar(pb.hero_id)" class="w-full h-full object-cover" />
                <span
                  :class="pb.is_pick ? 'bg-emerald-400 text-black' : 'bg-rose-950 text-rose-200'"
                  class="absolute bottom-0 right-0 text-[8px] font-mono font-black px-0.5 leading-tight"
                >
                  {{ pb.is_pick ? `P${pb.order + 1}` : `B${pb.order + 1}` }}
                </span>
              </div>
            </div>

            <!-- 夜魇 BP -->
            <div class="flex flex-wrap items-center gap-1.5 aurora-subcard p-2.5 rounded-xl border border-rose-400/25">
              <span class="text-[10px] font-black text-rose-400 px-1">夜魇 BP:</span>
              <div
                v-for="pb in draftPicksBans.filter((x) => x.team === 1)"
                :key="pb.order"
                class="relative rounded overflow-hidden border"
                :class="pb.is_pick ? 'border-rose-400 w-9 sm:w-10 h-6 sm:h-7 shadow-sm' : 'border-white/10 w-7 sm:w-8 h-5 sm:h-6 opacity-50 grayscale'"
                :title="`${pb.is_pick ? '选用' : '禁用'}: ${getHeroName(pb.hero_id)} (第 ${pb.order + 1} 手)`"
              >
                <img :src="getHeroAvatar(pb.hero_id)" class="w-full h-full object-cover" />
                <span
                  :class="pb.is_pick ? 'bg-rose-500 text-white' : 'bg-slate-900 text-slate-300'"
                  class="absolute bottom-0 right-0 text-[8px] font-mono font-black px-0.5 leading-tight"
                >
                  {{ pb.is_pick ? `P${pb.order + 1}` : `B${pb.order + 1}` }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. 一级战术模块极光毛玻璃切换导航栏 -->
      <nav class="aurora-glass rounded-2xl p-1.5 grid grid-cols-2 sm:flex sm:flex-wrap items-center gap-1.5">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="
            activeTab === tab.key
              ? 'bg-gradient-to-r from-cyan-400/25 via-teal-400/20 to-indigo-500/20 text-cyan-200 border-cyan-300/50 shadow-[0_0_20px_rgba(34,211,238,0.2)]'
              : 'text-slate-400 hover:text-slate-100 hover:bg-white/[0.05] border-transparent'
          "
          class="sm:flex-1 sm:min-w-[140px] py-2 sm:py-2.5 px-2.5 sm:px-4 rounded-xl border text-[11px] sm:text-xs font-black tracking-wide transition-all flex items-center justify-center gap-1.5 sm:gap-2"
        >
          <span>{{ tab.icon }}</span>
          <span class="truncate">{{ tab.label }}</span>
        </button>
      </nav>

      <!-- 2.5 详情复盘二级子导航栏（仅选中"详情复盘"时展开） -->
      <nav
        v-if="activeTab === 'review'"
        class="aurora-glass rounded-2xl p-1.5 grid grid-cols-2 sm:flex sm:flex-wrap items-center gap-1.5 border border-cyan-400/20"
      >
        <button
          v-for="subTab in reviewSubTabs"
          :key="subTab.key"
          @click="activeReviewTab = subTab.key"
          :class="
            activeReviewTab === subTab.key
              ? 'bg-gradient-to-r from-amber-400/20 via-cyan-400/20 to-indigo-500/20 text-amber-200 border-amber-300/45 shadow-[0_0_16px_rgba(245,158,11,0.16)]'
              : 'text-slate-400 hover:text-slate-100 hover:bg-white/[0.05] border-transparent'
          "
          class="sm:flex-1 sm:min-w-[150px] py-2 px-2.5 sm:px-3.5 rounded-xl border text-[11px] sm:text-xs font-bold tracking-wide transition-all flex items-center justify-center gap-1.5"
        >
          <span>{{ subTab.icon }}</span>
          <span class="truncate">{{ subTab.label }}</span>
        </button>
      </nav>

      <!-- 3. 标签页内容视图 -->
      <!-- Tab 1: 简单战报 -->
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

      <!-- Tab 2: 到底谁的问题？ (DeepSeek AI 败方背锅审判分析) -->
      <MatchAiBlameTab
        v-else-if="activeTab === 'blame'"
        :matchId="String(matchId)"
        :radiantWin="Boolean(matchMeta.radiantWin)"
        :players="enrichedPlayers"
        :initialAnalysis="matchResponse?.match?.aiAnalysis"
        @analysisSaved="onAiAnalysisSaved"
      />

      <!-- Tab 3: 到底谁是天降猛男！ (DeepSeek AI 胜方功臣评选分析) -->
      <MatchAiMvpTab
        v-else-if="activeTab === 'mvp'"
        :matchId="String(matchId)"
        :radiantWin="Boolean(matchMeta.radiantWin)"
        :players="enrichedPlayers"
        :initialAnalysis="matchResponse?.match?.aiMvpAnalysis"
        @analysisSaved="onAiMvpAnalysisSaved"
      />

      <!-- Tab 4: 详情复盘（包含 4 个二级子模块） -->
      <template v-else-if="activeTab === 'review'">
        <MatchGraphsTab
          v-if="activeReviewTab === 'graphs'"
          :rawJson="rawJson"
          :players="enrichedPlayers"
          :highlightAccountId="highlightAccountId"
        />
        <MatchCombatTab
          v-else-if="activeReviewTab === 'combat'"
          :players="enrichedPlayers"
          :highlightAccountId="highlightAccountId"
        />
        <MatchBuildsTab
          v-else-if="activeReviewTab === 'builds'"
          :players="enrichedPlayers"
          :highlightAccountId="highlightAccountId"
        />
        <MatchTeamfightsTab
          v-else-if="activeReviewTab === 'teamfights'"
          :rawJson="rawJson"
          :players="enrichedPlayers"
        />
      </template>
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
import MatchAiBlameTab from '../components/match/MatchAiBlameTab.vue';
import MatchAiMvpTab from '../components/match/MatchAiMvpTab.vue';

const props = defineProps<{
  matchId: string;
}>();

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const copied = ref(false);
const matchResponse = ref<any>(null);
const rawJson = ref<any>(null);

// 当前高亮视角玩家 ID (从历史战绩或个人看板跳转过来时自动携带)
const highlightAccountId = computed(() => String(route.query.accountId || ''));

// 一级战术标签页配置（简单战报 / AI 锐评 / 详情复盘）
type TabKey = 'overview' | 'blame' | 'mvp' | 'review';
type ReviewSubTabKey = 'graphs' | 'combat' | 'builds' | 'teamfights';

const activeTab = ref<TabKey>('overview');
const activeReviewTab = ref<ReviewSubTabKey>('graphs');

const tabs: Array<{ key: TabKey; icon: string; label: string }> = [
  { key: 'overview', icon: '📊', label: '简单战报' },
  { key: 'blame', icon: '🦹', label: '到底谁的问题？' },
  { key: 'mvp', icon: '💪', label: '谁是天降猛男！' },
  { key: 'review', icon: '🔍', label: '详情复盘' }
];

// 详情复盘二级子分栏配置
const reviewSubTabs: Array<{ key: ReviewSubTabKey; icon: string; label: string }> = [
  { key: 'graphs', icon: '📈', label: '经济经验差与发育曲线' },
  { key: 'combat', icon: '⚔️', label: '对位击杀与伤害拆解' },
  { key: 'builds', icon: '🛡️', label: '出装加点与对线视野' },
  { key: 'teamfights', icon: '🔥', label: '团战复盘与战略事件轴' }
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

const goBackOrHome = () => {
  if (highlightAccountId.value) {
    router.push(`/player/${highlightAccountId.value}`);
  } else {
    router.push('/');
  }
};

const onAiAnalysisSaved = (text: string) => {
  if (!matchResponse.value) {
    matchResponse.value = { match: { aiAnalysis: text } };
  } else if (!matchResponse.value.match) {
    matchResponse.value.match = { aiAnalysis: text };
  } else {
    matchResponse.value.match.aiAnalysis = text;
  }
};

const onAiMvpAnalysisSaved = (text: string) => {
  if (!matchResponse.value) {
    matchResponse.value = { match: { aiMvpAnalysis: text } };
  } else if (!matchResponse.value.match) {
    matchResponse.value.match = { aiMvpAnalysis: text };
  } else {
    matchResponse.value.match.aiMvpAnalysis = text;
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
