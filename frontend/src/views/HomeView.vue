<template>
  <div
    class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-20 selection:bg-cyan-500 selection:text-white"
    @click="closeDropdown"
  >
    <!-- 顶部导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white">毒瘤榜<span class="text-cyan-400">.GG</span></span>
            <span class="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-cyan-950/80 text-cyan-400 font-bold border border-cyan-800/50">
              DOTA2 GROUP TRACKER
            </span>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <!-- 特别鸣谢链接（点击单开一个新页面） -->
          <a
            href="/special-thanks"
            target="_blank"
            rel="noopener noreferrer"
            class="px-3.5 py-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 text-xs font-bold flex items-center gap-1.5 transition-all"
          >
            <span>🏅</span>
            <span>特别鸣谢</span>
            <span class="text-[10px] opacity-75">↗</span>
          </a>

          <button
            @click="router.push('/admin')"
            class="px-3 py-1.5 rounded-lg bg-[#121721] hover:bg-slate-800 text-slate-400 hover:text-slate-200 border border-slate-800 text-xs transition-colors"
          >
            管理员后台
          </button>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8 space-y-8">
      <!-- 1. 英雄主横幅：标题（毒瘤榜）、副标题（每一局都算数，每一局都记录）、核心统计指标与“选择你的身份” -->
      <section class="relative rounded-3xl bg-gradient-to-br from-[#121721] via-[#101827] to-[#0d121c] border border-slate-800/90 p-7 sm:p-10 shadow-2xl overflow-visible">
        <!-- 背景装饰光晕 -->
        <div class="absolute -top-24 -right-24 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none"></div>

        <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- 左侧：标题、标语与两大核心数据库计数 -->
          <div class="lg:col-span-7 space-y-6">
            <div>
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 text-xs font-semibold mb-4">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                <span>DOTA 2 群友高精录像实时追踪系统</span>
              </div>
              <h1 class="text-4xl sm:text-5xl font-black text-white tracking-tight">
                毒瘤榜
              </h1>
              <p class="text-lg sm:text-xl text-cyan-400/90 font-bold mt-3 tracking-wide">
                每一局都算数，每一局都记录
              </p>
            </div>

            <!-- 核心统计展示：数据已同步多少场、开黑对局多少场 -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <div class="bg-[#0b0e14]/85 border border-slate-800/90 rounded-2xl p-5 flex items-center justify-between shadow-lg">
                <div>
                  <span class="text-xs text-slate-400 font-medium block">数据已同步对局</span>
                  <div class="flex items-baseline gap-2 mt-1.5">
                    <span class="text-3xl sm:text-4xl font-black text-white font-mono">{{ totalSyncedMatches }}</span>
                    <span class="text-xs text-cyan-400 font-bold">场</span>
                  </div>
                  <span class="text-[11px] text-slate-500 mt-1 block font-mono">数据库已收录高精度录像数据</span>
                </div>
                <div class="w-12 h-12 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 flex items-center justify-center text-xl">
                  📊
                </div>
              </div>

              <div class="bg-[#0b0e14]/85 border border-slate-800/90 rounded-2xl p-5 flex items-center justify-between shadow-lg">
                <div>
                  <span class="text-xs text-slate-400 font-medium block">群友开黑对局</span>
                  <div class="flex items-baseline gap-2 mt-1.5">
                    <span class="text-3xl sm:text-4xl font-black text-emerald-400 font-mono">{{ totalPartyMatches }}</span>
                    <span class="text-xs text-emerald-400 font-bold">场</span>
                  </div>
                  <span class="text-[11px] text-slate-500 mt-1 block font-mono">关注玩家同场多人开黑仅计 1 场</span>
                </div>
                <div class="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xl">
                  🤝
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：选择你的身份（下拉菜单包含头像、玩家ID、最近比赛时间 + 进入我的页面按钮） -->
          <div class="lg:col-span-5">
            <div class="bg-[#0b0e14]/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
              <div class="flex items-center justify-between">
                <div>
                  <h2 class="text-base font-black text-white flex items-center gap-2">
                    <span class="text-cyan-400">🎮</span> 选择你的身份
                  </h2>
                  <p class="text-xs text-slate-400 mt-0.5">
                    从关注玩家列表中选择你的账号，一键进入个人深度战绩档案
                  </p>
                </div>
                <span class="text-[11px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">
                  {{ trackedPlayers.length }} 位群友
                </span>
              </div>

              <!-- 自定义下拉菜单 + 进入我的页面按钮 -->
              <div class="space-y-3" @click.stop>
                <div class="relative">
                  <button
                    type="button"
                    @click="dropdownOpen = !dropdownOpen"
                    class="w-full bg-[#121721] hover:bg-[#161d2a] border border-slate-700/80 focus:border-cyan-500 rounded-xl p-3 text-left flex items-center justify-between transition-all"
                  >
                    <div v-if="selectedPlayer" class="flex items-center gap-3 min-w-0">
                      <img
                        :src="selectedPlayer.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                        class="w-10 h-10 rounded-xl border border-slate-700 object-cover shrink-0"
                      />
                      <div class="min-w-0">
                        <div class="flex items-center gap-2">
                          <span class="font-bold text-white text-sm truncate">{{ selectedPlayer.personaname || '未知玩家' }}</span>
                          <span
                            :class="getRankMeta(selectedPlayer.rankTier, selectedPlayer.leaderboardRank).colorClass"
                            class="text-[10px] px-1.5 py-0.2 rounded border font-mono font-bold shrink-0"
                          >
                            {{ getRankMeta(selectedPlayer.rankTier, selectedPlayer.leaderboardRank).shortLabel }}
                          </span>
                        </div>
                        <div class="text-[11px] text-slate-400 font-mono mt-0.5 flex flex-wrap items-center gap-x-3">
                          <span>ID: <strong class="text-cyan-400">{{ selectedPlayer.accountId }}</strong></span>
                          <span>最近比赛: {{ formatLastMatchTime(selectedPlayer.lastMatchTime) }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="text-xs text-slate-400 py-2">
                      点击选择关注玩家身份...
                    </div>
                    <span class="text-slate-400 text-xs ml-2 transition-transform" :class="dropdownOpen ? 'rotate-180' : ''">▼</span>
                  </button>

                  <!-- 下拉列表浮层 -->
                  <div
                    v-if="dropdownOpen"
                    class="absolute left-0 right-0 mt-2 bg-[#121721] border border-slate-700 rounded-xl shadow-2xl z-50 max-h-72 overflow-y-auto divide-y divide-slate-800/70"
                  >
                    <div
                      v-for="p in trackedPlayers"
                      :key="p.accountId"
                      @click="selectPlayer(p)"
                      class="p-3 hover:bg-[#192132] cursor-pointer flex items-center justify-between gap-3 transition-colors"
                      :class="selectedPlayer?.accountId === p.accountId ? 'bg-cyan-500/10' : ''"
                    >
                      <div class="flex items-center gap-3 min-w-0">
                        <img
                          :src="p.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                          class="w-9 h-9 rounded-lg border border-slate-700 object-cover shrink-0"
                        />
                        <div class="min-w-0">
                          <div class="flex items-center gap-2">
                            <span class="font-bold text-white text-xs truncate">{{ p.personaname }}</span>
                            <span
                              :class="getRankMeta(p.rankTier, p.leaderboardRank).colorClass"
                              class="text-[10px] px-1.5 py-0.2 rounded border font-mono shrink-0"
                            >
                              {{ getRankMeta(p.rankTier, p.leaderboardRank).shortLabel }}
                            </span>
                          </div>
                          <div class="text-[11px] text-slate-400 font-mono mt-0.5">
                            玩家 ID: <span class="text-cyan-400">{{ p.accountId }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="text-right shrink-0 font-mono">
                        <div class="text-[10px] text-slate-400">最近比赛时间</div>
                        <div class="text-[11px] text-emerald-400 font-semibold mt-0.5">
                          {{ formatLastMatchTime(p.lastMatchTime) }}
                        </div>
                      </div>
                    </div>

                    <div v-if="trackedPlayers.length === 0" class="p-4 text-center text-xs text-slate-500">
                      暂无已关注玩家，请前往管理员页面添加
                    </div>
                  </div>
                </div>

                <!-- 进入我的页面按钮 -->
                <button
                  type="button"
                  @click="goToSelectedPlayer"
                  :disabled="!selectedPlayer"
                  class="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-cyan-500 to-emerald-400 hover:opacity-95 text-black font-black text-sm shadow-lg shadow-cyan-500/20 flex items-center justify-center gap-2 active:scale-[0.99] transition-all disabled:opacity-50"
                >
                  <span>进入我的页面</span>
                  <span>→</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 2. 群活动：最近 20 天动态图表（每天的开黑场次 & 每天的单排场次） -->
      <section class="space-y-4">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 class="text-lg font-black text-white flex items-center gap-2">
              <span class="w-2 h-6 rounded-full bg-cyan-400"></span>
              <span>群活动 · 最近 20 天对局趋势</span>
            </h2>
            <p class="text-xs text-slate-400 mt-1">
              仅统计已关注的群友玩家；若 2 人、3 人或 5 人在同一场比赛中开黑，按 1 场对局去重统计
            </p>
          </div>
          <div class="text-xs font-mono text-slate-400 bg-[#121721] px-3 py-1.5 rounded-lg border border-slate-800">
            近20天合计：开黑 <strong class="text-emerald-400">{{ sum20Party }}</strong> 场 · 单排 <strong class="text-cyan-400">{{ sum20Solo }}</strong> 场
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 动态图表 1：最近 20 天每天的开黑场次 -->
          <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl flex flex-col justify-between">
            <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping"></span>
                  <h3 class="text-base font-bold text-white">最近 20 天 · 每日开黑场次</h3>
                </div>
                <p class="text-xs text-slate-400 mt-1">车队集结热度（多人同局开黑仅算 1 场）</p>
              </div>
              <div class="text-right font-mono">
                <span class="text-[11px] text-slate-400 block">单日最高峰值</span>
                <span class="text-lg font-black text-emerald-400">{{ maxPartyDay }} 场</span>
              </div>
            </div>

            <!-- 动态柱状 + 趋势折线交互图 -->
            <div class="mt-6 relative">
              <!-- SVG 折线背景层 -->
              <svg class="w-full h-44 overflow-visible" viewBox="0 0 600 160" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="partyAreaGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#10b981" stop-opacity="0.32" />
                    <stop offset="100%" stop-color="#10b981" stop-opacity="0.0" />
                  </linearGradient>
                </defs>
                <!-- 水平参考虚线 -->
                <line x1="0" y1="30" x2="600" y2="30" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="85" x2="600" y2="85" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="140" x2="600" y2="140" stroke="#1e293b" stroke-width="1" />
                <!-- 渐变面积与折线 -->
                <path v-if="partyAreaPath" :d="partyAreaPath" fill="url(#partyAreaGrad)" class="transition-all duration-700" />
                <path
                  v-if="partyLinePath"
                  :d="partyLinePath"
                  fill="none"
                  stroke="#34d399"
                  stroke-width="2.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="transition-all duration-700"
                />
              </svg>

              <!-- 交互式动态柱条层 -->
              <div class="absolute inset-0 flex items-end justify-between px-1 pb-5 pt-3">
                <div
                  v-for="(item, idx) in dailyActivity"
                  :key="'party-' + item.date"
                  class="flex-1 flex flex-col items-center justify-end h-full group relative cursor-pointer"
                >
                  <!-- 悬浮提示气泡 -->
                  <div class="opacity-0 group-hover:opacity-100 pointer-events-none absolute -top-9 z-20 bg-[#0b0e14] border border-emerald-500/50 text-white text-[10px] font-mono px-2 py-1 rounded shadow-lg whitespace-nowrap transition-opacity">
                    {{ item.date }}: <strong class="text-emerald-400">{{ item.partyGames }} 场开黑</strong>
                  </div>

                  <!-- 顶部数值 -->
                  <span
                    v-if="item.partyGames > 0"
                    class="text-[10px] font-mono font-bold text-emerald-300 mb-1 transition-transform group-hover:-translate-y-0.5"
                  >
                    {{ item.partyGames }}
                  </span>

                  <!-- 动画柱体 -->
                  <div class="w-2.5 sm:w-3.5 bg-slate-800/40 rounded-t-md flex items-end overflow-hidden h-[110px]">
                    <div
                      class="w-full rounded-t-md bg-gradient-to-t from-emerald-600 to-emerald-400 group-hover:from-emerald-500 group-hover:to-teal-300 transition-all duration-700 shadow-lg shadow-emerald-500/20"
                      :style="{
                        height: chartAnimated ? `${Math.max(item.partyGames > 0 ? 8 : 2, (item.partyGames / Math.max(1, maxPartyDay)) * 100)}%` : '0%',
                        transitionDelay: `${idx * 25}ms`
                      }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- X 轴日期刻度 -->
              <div class="flex justify-between text-[10px] font-mono text-slate-500 pt-1 px-1">
                <span
                  v-for="(item, idx) in dailyActivity"
                  :key="'party-label-' + item.date"
                  class="flex-1 text-center"
                >
                  {{ idx % 3 === 0 || idx === dailyActivity.length - 1 ? item.shortDate : '' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 动态图表 2：最近 20 天每天的单排场次 -->
          <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl flex flex-col justify-between">
            <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-cyan-400 animate-ping"></span>
                  <h3 class="text-base font-bold text-white">最近 20 天 · 每日单排场次</h3>
                </div>
                <p class="text-xs text-slate-400 mt-1">孤狼冲分活跃度（仅统计关注玩家单排对局）</p>
              </div>
              <div class="text-right font-mono">
                <span class="text-[11px] text-slate-400 block">单日最高峰值</span>
                <span class="text-lg font-black text-cyan-400">{{ maxSoloDay }} 场</span>
              </div>
            </div>

            <!-- 动态柱状 + 趋势折线交互图 -->
            <div class="mt-6 relative">
              <!-- SVG 折线背景层 -->
              <svg class="w-full h-44 overflow-visible" viewBox="0 0 600 160" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="soloAreaGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#06b6d4" stop-opacity="0.32" />
                    <stop offset="100%" stop-color="#06b6d4" stop-opacity="0.0" />
                  </linearGradient>
                </defs>
                <line x1="0" y1="30" x2="600" y2="30" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="85" x2="600" y2="85" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="140" x2="600" y2="140" stroke="#1e293b" stroke-width="1" />
                <path v-if="soloAreaPath" :d="soloAreaPath" fill="url(#soloAreaGrad)" class="transition-all duration-700" />
                <path
                  v-if="soloLinePath"
                  :d="soloLinePath"
                  fill="none"
                  stroke="#22d3ee"
                  stroke-width="2.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="transition-all duration-700"
                />
              </svg>

              <!-- 交互式动态柱条层 -->
              <div class="absolute inset-0 flex items-end justify-between px-1 pb-5 pt-3">
                <div
                  v-for="(item, idx) in dailyActivity"
                  :key="'solo-' + item.date"
                  class="flex-1 flex flex-col items-center justify-end h-full group relative cursor-pointer"
                >
                  <!-- 悬浮提示气泡 -->
                  <div class="opacity-0 group-hover:opacity-100 pointer-events-none absolute -top-9 z-20 bg-[#0b0e14] border border-cyan-500/50 text-white text-[10px] font-mono px-2 py-1 rounded shadow-lg whitespace-nowrap transition-opacity">
                    {{ item.date }}: <strong class="text-cyan-400">{{ item.soloGames }} 场单排</strong>
                  </div>

                  <!-- 顶部数值 -->
                  <span
                    v-if="item.soloGames > 0"
                    class="text-[10px] font-mono font-bold text-cyan-300 mb-1 transition-transform group-hover:-translate-y-0.5"
                  >
                    {{ item.soloGames }}
                  </span>

                  <!-- 动画柱体 -->
                  <div class="w-2.5 sm:w-3.5 bg-slate-800/40 rounded-t-md flex items-end overflow-hidden h-[110px]">
                    <div
                      class="w-full rounded-t-md bg-gradient-to-t from-cyan-600 to-cyan-400 group-hover:from-cyan-500 group-hover:to-sky-300 transition-all duration-700 shadow-lg shadow-cyan-500/20"
                      :style="{
                        height: chartAnimated ? `${Math.max(item.soloGames > 0 ? 8 : 2, (item.soloGames / Math.max(1, maxSoloDay)) * 100)}%` : '0%',
                        transitionDelay: `${idx * 25}ms`
                      }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- X 轴日期刻度 -->
              <div class="flex justify-between text-[10px] font-mono text-slate-500 pt-1 px-1">
                <span
                  v-for="(item, idx) in dailyActivity"
                  :key="'solo-label-' + item.date"
                  class="flex-1 text-center"
                >
                  {{ idx % 3 === 0 || idx === dailyActivity.length - 1 ? item.shortDate : '' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. 底部特别鸣谢入口横幅 -->
      <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-xl">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400 flex items-center justify-center text-2xl shrink-0">
            🏆
          </div>
          <div>
            <h3 class="text-base font-black text-white">毒瘤榜 · 特别鸣谢与官方认证殿堂</h3>
            <p class="text-xs text-slate-400 mt-0.5">
              收录群内传世圣经语录与《CERTIFICATE OF TOXICITY 毒瘤认证》官方授权证书
            </p>
          </div>
        </div>

        <a
          href="/special-thanks"
          target="_blank"
          rel="noopener noreferrer"
          class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-yellow-400 hover:opacity-95 text-black font-black text-xs whitespace-nowrap shadow-lg shadow-amber-500/20 flex items-center gap-2 transition-all"
        >
          <span>✨ 特别鸣谢（新窗口打开）</span>
          <span>↗</span>
        </a>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchHomeOverview } from '../api/player';
import { getRankMeta } from '../utils/dotaHelper';

interface DailyActivityItem {
  date: string;
  shortDate: string;
  partyGames: number;
  soloGames: number;
}

interface TrackedPlayerOption {
  accountId: number;
  personaname: string;
  avatar: string;
  rankTier: number;
  leaderboardRank?: number | null;
  totalMatches: number;
  winRate: number;
  lastMatchTime?: number | null;
}

const router = useRouter();

const totalSyncedMatches = ref(0);
const totalPartyMatches = ref(0);
const totalSoloMatches = ref(0);
const dailyActivity = ref<DailyActivityItem[]>([]);
const trackedPlayers = ref<TrackedPlayerOption[]>([]);

const selectedPlayer = ref<TrackedPlayerOption | null>(null);
const dropdownOpen = ref(false);
const chartAnimated = ref(false);

const closeDropdown = () => {
  dropdownOpen.value = false;
};

const selectPlayer = (p: TrackedPlayerOption) => {
  selectedPlayer.value = p;
  dropdownOpen.value = false;
};

const goToSelectedPlayer = () => {
  if (!selectedPlayer.value) return;
  router.push(`/player/${selectedPlayer.value.accountId}`);
};

// 近 20 天汇总与峰值计算
const sum20Party = computed(() =>
  dailyActivity.value.reduce((acc, cur) => acc + Number(cur.partyGames || 0), 0)
);
const sum20Solo = computed(() =>
  dailyActivity.value.reduce((acc, cur) => acc + Number(cur.soloGames || 0), 0)
);
const maxPartyDay = computed(() =>
  Math.max(1, ...dailyActivity.value.map((d) => Number(d.partyGames || 0)))
);
const maxSoloDay = computed(() =>
  Math.max(1, ...dailyActivity.value.map((d) => Number(d.soloGames || 0)))
);

// 生成 SVG 折线与面积路径 (宽 600, 底边 Y=140, 顶边 Y=22)
const buildSvgPaths = (values: number[], maxVal: number) => {
  if (!values.length) return { line: '', area: '' };
  const width = 600;
  const baseY = 140;
  const usableHeight = 110;
  const n = values.length;
  const pts = values.map((v, idx) => {
    const x = n === 1 ? width / 2 : (idx / (n - 1)) * (width - 30) + 15;
    const ratio = chartAnimated.value ? Math.min(1, v / Math.max(1, maxVal)) : 0;
    const y = baseY - ratio * usableHeight;
    return { x: Number(x.toFixed(1)), y: Number(y.toFixed(1)) };
  });
  const line = pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  const area = `${line} L ${pts[pts.length - 1].x} ${baseY} L ${pts[0].x} ${baseY} Z`;
  return { line, area };
};

const partyLinePath = computed(() =>
  buildSvgPaths(
    dailyActivity.value.map((d) => Number(d.partyGames || 0)),
    maxPartyDay.value
  ).line
);
const partyAreaPath = computed(() =>
  buildSvgPaths(
    dailyActivity.value.map((d) => Number(d.partyGames || 0)),
    maxPartyDay.value
  ).area
);
const soloLinePath = computed(() =>
  buildSvgPaths(
    dailyActivity.value.map((d) => Number(d.soloGames || 0)),
    maxSoloDay.value
  ).line
);
const soloAreaPath = computed(() =>
  buildSvgPaths(
    dailyActivity.value.map((d) => Number(d.soloGames || 0)),
    maxSoloDay.value
  ).area
);

// 格式化玩家最近一场比赛时间
const formatLastMatchTime = (epochSec?: number | null) => {
  if (!epochSec || Number(epochSec) <= 0) return '暂无对局记录';
  const d = new Date(Number(epochSec) * 1000);
  if (isNaN(d.getTime())) return '暂无对局记录';
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
};

const loadHomeData = async () => {
  try {
    const res = await fetchHomeOverview();
    if (res?.data) {
      totalSyncedMatches.value = Number(res.data.totalSyncedMatches || 0);
      totalPartyMatches.value = Number(res.data.totalPartyMatches || 0);
      totalSoloMatches.value = Number(res.data.totalSoloMatches || 0);
      dailyActivity.value = res.data.dailyActivity || [];
      trackedPlayers.value = res.data.trackedPlayers || [];
      if (trackedPlayers.value.length > 0 && !selectedPlayer.value) {
        selectedPlayer.value = trackedPlayers.value[0];
      }
      setTimeout(() => {
        chartAnimated.value = true;
      }, 80);
    }
  } catch (e) {
    console.error('加载首页统计数据失败', e);
  }
};

onMounted(() => {
  loadHomeData();
});
</script>
