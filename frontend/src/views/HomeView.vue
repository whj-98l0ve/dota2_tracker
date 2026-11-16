<template>
  <div
    class="text-slate-100 min-h-screen font-sans antialiased pb-20"
    @click="closeDropdown"
  >
    <!-- 顶部极光毛玻璃导航栏（移动端紧凑适配） -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <!-- 左侧品牌标识 -->
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 shrink-0">
            <div class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base tracking-tighter">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-lg sm:text-xl font-black tracking-wide text-white drop-shadow-sm truncate">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden md:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-white/[0.06] text-cyan-200 font-bold border border-white/15 backdrop-blur-md">
              AURORA TELEMETRY
            </span>
          </div>
        </div>

        <!-- 右侧快捷入口 -->
        <div class="flex items-center gap-2 sm:gap-3 shrink-0">
          <!-- 特别鸣谢链接（点击单开一个新页面） -->
          <a
            href="/special-thanks"
            target="_blank"
            rel="noopener noreferrer"
            class="px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-full bg-gradient-to-r from-rose-500/15 via-amber-500/15 to-rose-500/15 hover:from-rose-500/25 hover:to-amber-500/25 text-amber-200 border border-amber-400/35 text-[11px] sm:text-xs font-bold flex items-center gap-1 backdrop-blur-md shadow-sm shadow-rose-500/10 transition-all"
          >
            <span>🏅</span>
            <span>特别鸣谢</span>
            <span class="text-[10px] opacity-75 hidden sm:inline">↗</span>
          </a>

          <button
            @click="router.push('/admin')"
            class="px-2.5 sm:px-3.5 py-1 sm:py-1.5 rounded-full bg-white/[0.05] hover:bg-white/[0.12] text-slate-300 hover:text-white border border-white/10 hover:border-cyan-400/30 text-[11px] sm:text-xs font-medium backdrop-blur-md transition-all"
          >
            管理后台
          </button>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 mt-4 sm:mt-8 space-y-6 sm:space-y-8">
      <!-- 1. 英雄主横幅：标题（毒瘤榜）、副标题（每一局都算数，每一局都记录）、核心统计指标与“选择你的身份” -->
      <section class="relative rounded-2xl sm:rounded-3xl aurora-glass p-5 sm:p-8 lg:p-10 overflow-visible">
        <!-- 卡片内部极光流光漫反射光斑 -->
        <div class="absolute -top-20 -left-20 w-64 sm:w-80 h-64 sm:h-80 bg-cyan-400/15 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-20 right-1/4 w-64 sm:w-80 h-64 sm:h-80 bg-rose-500/12 rounded-full blur-3xl pointer-events-none"></div>

        <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-center">
          <!-- 左侧：标题、标语与两大核心数据库计数 -->
          <div class="lg:col-span-7 space-y-5 sm:space-y-6">
            <div>
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-400/10 border border-cyan-300/30 text-cyan-200 text-[11px] sm:text-xs font-semibold mb-3 sm:mb-4 backdrop-blur-md shadow-inner">
                <span class="w-2 h-2 rounded-full bg-cyan-300 animate-pulse shadow-[0_0_8px_#67e8f9]"></span>
                <span>DOTA 2 群友高精录像实时追踪系统</span>
              </div>
              <h1 class="text-3xl sm:text-5xl font-black tracking-tight bg-gradient-to-r from-white via-cyan-100 to-teal-200 bg-clip-text text-transparent drop-shadow-sm">
                毒瘤榜
              </h1>
              <p class="text-base sm:text-xl bg-gradient-to-r from-cyan-300 via-teal-200 to-rose-300 bg-clip-text text-transparent font-bold mt-2 sm:mt-3 tracking-wide">
                每一局都算数，每一局都记录
              </p>
            </div>

            <!-- 核心统计展示：数据已同步多少场、开黑对局多少场（移动端双列紧凑或单列自适应） -->
            <div class="grid grid-cols-2 gap-3 sm:gap-4 pt-1 sm:pt-2">
              <div class="aurora-subcard rounded-2xl p-3.5 sm:p-5 flex items-center justify-between gap-2 group hover:border-cyan-400/35">
                <div class="min-w-0">
                  <span class="text-[11px] sm:text-xs text-slate-300/80 font-medium block truncate">数据已同步对局</span>
                  <div class="flex items-baseline gap-1.5 sm:gap-2 mt-1 sm:mt-1.5">
                    <span class="text-2xl sm:text-4xl font-black text-white font-mono tracking-tight drop-shadow-[0_0_12px_rgba(34,211,238,0.25)]">
                      {{ totalSyncedMatches }}
                    </span>
                    <span class="text-[11px] sm:text-xs text-cyan-300 font-bold">场</span>
                  </div>
                  <span class="text-[10px] sm:text-[11px] text-slate-400 mt-1 hidden sm:block font-mono">数据库已收录高精度录像数据</span>
                </div>
                <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-xl sm:rounded-2xl bg-gradient-to-br from-cyan-400/20 to-sky-500/10 border border-cyan-300/30 text-cyan-300 flex items-center justify-center text-base sm:text-xl shadow-lg shadow-cyan-500/15 shrink-0 group-hover:scale-105 transition-transform">
                  📊
                </div>
              </div>

              <div class="aurora-subcard rounded-2xl p-3.5 sm:p-5 flex items-center justify-between gap-2 group hover:border-emerald-400/35">
                <div class="min-w-0">
                  <span class="text-[11px] sm:text-xs text-slate-300/80 font-medium block truncate">群友开黑对局</span>
                  <div class="flex items-baseline gap-1.5 sm:gap-2 mt-1 sm:mt-1.5">
                    <span class="text-2xl sm:text-4xl font-black text-emerald-300 font-mono tracking-tight drop-shadow-[0_0_12px_rgba(16,185,129,0.28)]">
                      {{ totalPartyMatches }}
                    </span>
                    <span class="text-[11px] sm:text-xs text-emerald-300 font-bold">场</span>
                  </div>
                  <span class="text-[10px] sm:text-[11px] text-slate-400 mt-1 hidden sm:block font-mono">关注玩家同场多人开黑仅计 1 场</span>
                </div>
                <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-xl sm:rounded-2xl bg-gradient-to-br from-emerald-400/20 to-teal-500/10 border border-emerald-300/30 text-emerald-300 flex items-center justify-center text-base sm:text-xl shadow-lg shadow-emerald-500/15 shrink-0 group-hover:scale-105 transition-transform">
                  🤝
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：选择你的身份（下拉菜单包含头像、玩家ID、最近比赛时间 + 进入我的页面按钮） -->
          <div class="lg:col-span-5">
            <div class="aurora-subcard rounded-2xl p-4 sm:p-6 space-y-4 border border-white/15 shadow-2xl">
              <div class="flex items-center justify-between gap-2">
                <div>
                  <h2 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
                    <span class="text-cyan-300">🎮</span> 选择你的身份
                  </h2>
                  <p class="text-[11px] sm:text-xs text-slate-400 mt-0.5">
                    选择你的账号，一键进入个人深度战绩档案
                  </p>
                </div>
                <span class="text-[10px] sm:text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-white/[0.07] border border-white/10 text-cyan-200 shrink-0">
                  {{ trackedPlayers.length }} 位群友
                </span>
              </div>

              <!-- 自定义下拉菜单 + 进入我的页面按钮 -->
              <div class="space-y-3" @click.stop>
                <div class="relative">
                  <button
                    type="button"
                    @click="dropdownOpen = !dropdownOpen"
                    class="w-full bg-[#070b16]/85 hover:bg-[#0b1224]/90 border border-white/15 hover:border-cyan-400/40 focus:border-cyan-300 rounded-xl p-2.5 sm:p-3 text-left flex items-center justify-between backdrop-blur-md transition-all shadow-inner"
                  >
                    <div v-if="selectedPlayer" class="flex items-center gap-2.5 sm:gap-3 min-w-0">
                      <img
                        :src="selectedPlayer.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                        class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl border border-cyan-400/30 object-cover shrink-0 shadow-md"
                      />
                      <div class="min-w-0">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span class="font-bold text-white text-xs sm:text-sm truncate max-w-[140px] sm:max-w-[180px]">
                            {{ selectedPlayer.personaname || '未知玩家' }}<span v-if="selectedPlayer.remark" class="text-amber-300 font-semibold ml-0.5">（{{ selectedPlayer.remark }}）</span>
                          </span>
                          <span
                            :class="getRankMeta(selectedPlayer.rankTier, selectedPlayer.leaderboardRank).colorClass"
                            class="text-[10px] px-1.5 py-0.2 rounded-md border font-mono font-bold shrink-0"
                          >
                            {{ getRankMeta(selectedPlayer.rankTier, selectedPlayer.leaderboardRank).shortLabel }}
                          </span>
                        </div>
                        <div class="text-[10px] sm:text-[11px] text-slate-400 font-mono mt-0.5 flex flex-wrap items-center gap-x-2.5">
                          <span>ID: <strong class="text-cyan-300">{{ selectedPlayer.accountId }}</strong></span>
                          <span class="truncate">最近: {{ formatLastMatchTime(selectedPlayer.lastMatchTime) }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="text-xs text-slate-400 py-2">
                      点击选择关注玩家身份...
                    </div>
                    <span class="text-cyan-300/80 text-xs ml-2 transition-transform duration-200 shrink-0" :class="dropdownOpen ? 'rotate-180' : ''">▼</span>
                  </button>

                  <!-- 下拉列表极光毛玻璃浮层（移动端优化防溢出） -->
                  <div
                    v-if="dropdownOpen"
                    class="absolute left-0 right-0 mt-2 bg-[#080d1a]/95 backdrop-blur-2xl border border-white/15 rounded-xl shadow-[0_25px_60px_rgba(0,0,0,0.85)] z-50 max-h-72 overflow-y-auto divide-y divide-white/[0.06]"
                  >
                    <div
                      v-for="p in trackedPlayers"
                      :key="p.accountId"
                      @click="selectPlayer(p)"
                      class="p-2.5 sm:p-3 hover:bg-cyan-400/10 cursor-pointer flex items-center justify-between gap-2 transition-colors"
                      :class="selectedPlayer?.accountId === p.accountId ? 'bg-cyan-400/15 border-l-2 border-l-cyan-300' : ''"
                    >
                      <div class="flex items-center gap-2.5 min-w-0">
                        <img
                          :src="p.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                          class="w-8 h-8 sm:w-9 sm:h-9 rounded-lg border border-white/15 object-cover shrink-0"
                        />
                        <div class="min-w-0">
                          <div class="flex items-center gap-1.5 flex-wrap">
                            <span class="font-bold text-white text-xs truncate max-w-[115px] sm:max-w-[160px]">
                              {{ p.personaname }}<span v-if="p.remark" class="text-amber-300 font-semibold ml-0.5">（{{ p.remark }}）</span>
                            </span>
                            <span
                              :class="getRankMeta(p.rankTier, p.leaderboardRank).colorClass"
                              class="text-[9px] sm:text-[10px] px-1.5 py-0.2 rounded border font-mono shrink-0"
                            >
                              {{ getRankMeta(p.rankTier, p.leaderboardRank).shortLabel }}
                            </span>
                          </div>
                          <div class="text-[10px] sm:text-[11px] text-slate-400 font-mono mt-0.5">
                            ID: <span class="text-cyan-300">{{ p.accountId }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="text-right shrink-0 font-mono">
                        <div class="text-[9px] sm:text-[10px] text-slate-400">最近比赛</div>
                        <div class="text-[10px] sm:text-[11px] text-emerald-300 font-semibold mt-0.5">
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
                  class="w-full py-3 px-4 rounded-xl aurora-btn-primary font-black text-sm flex items-center justify-center gap-2 active:scale-[0.99] disabled:opacity-50"
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
            <h2 class="text-lg font-black text-white flex items-center gap-2.5">
              <span class="w-2 h-6 rounded-full bg-gradient-to-b from-cyan-300 to-emerald-400 shadow-[0_0_10px_rgba(34,211,238,0.5)]"></span>
              <span>群活动 · 最近 20 天对局趋势</span>
            </h2>
            <p class="text-xs text-slate-400 mt-1">
              仅统计已关注的群友玩家；若 2 人、3 人或 5 人在同一场比赛中开黑，按 1 场对局去重统计
            </p>
          </div>
          <div class="text-xs font-mono text-slate-300 aurora-subcard px-4 py-2 rounded-full">
            近20天合计：开黑 <strong class="text-emerald-300">{{ sum20Party }}</strong> 场 · 单排 <strong class="text-cyan-300">{{ sum20Solo }}</strong> 场
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
          <!-- 动态图表 1：最近 20 天每天的开黑场次 -->
          <div class="aurora-glass aurora-glass-hover rounded-2xl p-4 sm:p-6 flex flex-col justify-between">
            <div class="flex items-center justify-between gap-2 pb-3 sm:pb-4 border-b border-white/10">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping shrink-0"></span>
                  <h3 class="text-sm sm:text-base font-bold text-white">最近 20 天 · 每日开黑场次</h3>
                </div>
                <p class="text-[11px] sm:text-xs text-slate-400 mt-1">车队集结热度（多人同局开黑仅算 1 场）</p>
              </div>
              <div class="text-right font-mono aurora-subcard px-2.5 sm:px-3 py-1 sm:py-1.5 rounded-xl shrink-0">
                <span class="text-[9px] sm:text-[10px] text-slate-400 block">单日峰值</span>
                <span class="text-sm sm:text-lg font-black text-emerald-300">{{ maxPartyDay }} 场</span>
              </div>
            </div>

            <!-- 动态柱状 + 趋势折线交互图 -->
            <div class="mt-4 sm:mt-6 relative">
              <!-- SVG 折线背景层 -->
              <svg class="w-full h-36 sm:h-44 overflow-visible" viewBox="0 0 600 160" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="partyAreaGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#34d399" stop-opacity="0.38" />
                    <stop offset="100%" stop-color="#10b981" stop-opacity="0.0" />
                  </linearGradient>
                </defs>
                <!-- 水平参考虚线 -->
                <line x1="0" y1="30" x2="600" y2="30" stroke="rgba(255,255,255,0.07)" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="85" x2="600" y2="85" stroke="rgba(255,255,255,0.07)" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="140" x2="600" y2="140" stroke="rgba(255,255,255,0.12)" stroke-width="1" />
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
                  class="transition-all duration-700 drop-shadow-[0_0_8px_rgba(52,211,153,0.5)]"
                />
              </svg>

              <!-- 交互式动态柱条层 -->
              <div class="absolute inset-0 flex items-end justify-between px-0.5 sm:px-1 pb-5 pt-3">
                <div
                  v-for="(item, idx) in dailyActivity"
                  :key="'party-' + item.date"
                  class="flex-1 flex flex-col items-center justify-end h-full group relative cursor-pointer"
                >
                  <!-- 悬浮提示毛玻璃气泡（边缘防裁切） -->
                  <div
                    :class="idx < 3 ? 'left-0' : idx > dailyActivity.length - 4 ? 'right-0' : 'left-1/2 -translate-x-1/2'"
                    class="opacity-0 group-hover:opacity-100 pointer-events-none absolute -top-8 z-20 bg-[#060a15]/95 backdrop-blur-md border border-emerald-400/50 text-white text-[10px] font-mono px-2 py-1 rounded-lg shadow-xl whitespace-nowrap transition-opacity"
                  >
                    {{ item.date }}: <strong class="text-emerald-300">{{ item.partyGames }} 场开黑</strong>
                  </div>

                  <!-- 顶部数值 -->
                  <span
                    v-if="item.partyGames > 0"
                    class="text-[8px] sm:text-[10px] font-mono font-bold text-emerald-200 mb-0.5 sm:mb-1 transition-transform group-hover:-translate-y-0.5"
                  >
                    {{ item.partyGames }}
                  </span>

                  <!-- 动画柱体 -->
                  <div class="w-2 sm:w-3.5 bg-white/[0.04] rounded-t-md flex items-end overflow-hidden h-[90px] sm:h-[110px]">
                    <div
                      class="w-full rounded-t-md bg-gradient-to-t from-emerald-600/90 via-emerald-400 to-teal-200 group-hover:brightness-110 transition-all duration-700 shadow-[0_0_12px_rgba(52,211,153,0.35)]"
                      :style="{
                        height: chartAnimated ? `${Math.max(item.partyGames > 0 ? 8 : 2, (item.partyGames / Math.max(1, maxPartyDay)) * 100)}%` : '0%',
                        transitionDelay: `${idx * 25}ms`
                      }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- X 轴日期刻度 -->
              <div class="flex justify-between text-[9px] sm:text-[10px] font-mono text-slate-400 pt-1 px-0.5 sm:px-1">
                <span
                  v-for="(item, idx) in dailyActivity"
                  :key="'party-label-' + item.date"
                  class="flex-1 text-center whitespace-nowrap"
                >
                  {{ idx % 4 === 0 || idx === dailyActivity.length - 1 ? item.shortDate : '' }}
                </span>
              </div>
            </div>
          </div>

          <!-- 动态图表 2：最近 20 天每天的单排场次 -->
          <div class="aurora-glass aurora-glass-hover rounded-2xl p-4 sm:p-6 flex flex-col justify-between">
            <div class="flex items-center justify-between gap-2 pb-3 sm:pb-4 border-b border-white/10">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-2.5 h-2.5 rounded-full bg-cyan-300 animate-ping shrink-0"></span>
                  <h3 class="text-sm sm:text-base font-bold text-white">最近 20 天 · 每日单排场次</h3>
                </div>
                <p class="text-[11px] sm:text-xs text-slate-400 mt-1">孤狼冲分活跃度（仅统计关注玩家单排对局）</p>
              </div>
              <div class="text-right font-mono aurora-subcard px-2.5 sm:px-3 py-1 sm:py-1.5 rounded-xl shrink-0">
                <span class="text-[9px] sm:text-[10px] text-slate-400 block">单日峰值</span>
                <span class="text-sm sm:text-lg font-black text-cyan-300">{{ maxSoloDay }} 场</span>
              </div>
            </div>

            <!-- 动态柱状 + 趋势折线交互图 -->
            <div class="mt-4 sm:mt-6 relative">
              <!-- SVG 折线背景层 -->
              <svg class="w-full h-36 sm:h-44 overflow-visible" viewBox="0 0 600 160" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="soloAreaGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#22d3ee" stop-opacity="0.38" />
                    <stop offset="100%" stop-color="#06b6d4" stop-opacity="0.0" />
                  </linearGradient>
                </defs>
                <line x1="0" y1="30" x2="600" y2="30" stroke="rgba(255,255,255,0.07)" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="85" x2="600" y2="85" stroke="rgba(255,255,255,0.07)" stroke-dasharray="4 4" stroke-width="1" />
                <line x1="0" y1="140" x2="600" y2="140" stroke="rgba(255,255,255,0.12)" stroke-width="1" />
                <path v-if="soloAreaPath" :d="soloAreaPath" fill="url(#soloAreaGrad)" class="transition-all duration-700" />
                <path
                  v-if="soloLinePath"
                  :d="soloLinePath"
                  fill="none"
                  stroke="#22d3ee"
                  stroke-width="2.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="transition-all duration-700 drop-shadow-[0_0_8px_rgba(34,211,238,0.5)]"
                />
              </svg>

              <!-- 交互式动态柱条层 -->
              <div class="absolute inset-0 flex items-end justify-between px-0.5 sm:px-1 pb-5 pt-3">
                <div
                  v-for="(item, idx) in dailyActivity"
                  :key="'solo-' + item.date"
                  class="flex-1 flex flex-col items-center justify-end h-full group relative cursor-pointer"
                >
                  <!-- 悬浮提示毛玻璃气泡（边缘防裁切） -->
                  <div
                    :class="idx < 3 ? 'left-0' : idx > dailyActivity.length - 4 ? 'right-0' : 'left-1/2 -translate-x-1/2'"
                    class="opacity-0 group-hover:opacity-100 pointer-events-none absolute -top-8 z-20 bg-[#060a15]/95 backdrop-blur-md border border-cyan-400/50 text-white text-[10px] font-mono px-2 py-1 rounded-lg shadow-xl whitespace-nowrap transition-opacity"
                  >
                    {{ item.date }}: <strong class="text-cyan-300">{{ item.soloGames }} 场单排</strong>
                  </div>

                  <!-- 顶部数值 -->
                  <span
                    v-if="item.soloGames > 0"
                    class="text-[8px] sm:text-[10px] font-mono font-bold text-cyan-200 mb-0.5 sm:mb-1 transition-transform group-hover:-translate-y-0.5"
                  >
                    {{ item.soloGames }}
                  </span>

                  <!-- 动画柱体 -->
                  <div class="w-2 sm:w-3.5 bg-white/[0.04] rounded-t-md flex items-end overflow-hidden h-[90px] sm:h-[110px]">
                    <div
                      class="w-full rounded-t-md bg-gradient-to-t from-cyan-600/90 via-cyan-400 to-sky-200 group-hover:brightness-110 transition-all duration-700 shadow-[0_0_12px_rgba(34,211,238,0.35)]"
                      :style="{
                        height: chartAnimated ? `${Math.max(item.soloGames > 0 ? 8 : 2, (item.soloGames / Math.max(1, maxSoloDay)) * 100)}%` : '0%',
                        transitionDelay: `${idx * 25}ms`
                      }"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- X 轴日期刻度 -->
              <div class="flex justify-between text-[9px] sm:text-[10px] font-mono text-slate-400 pt-1 px-0.5 sm:px-1">
                <span
                  v-for="(item, idx) in dailyActivity"
                  :key="'solo-label-' + item.date"
                  class="flex-1 text-center whitespace-nowrap"
                >
                  {{ idx % 4 === 0 || idx === dailyActivity.length - 1 ? item.shortDate : '' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. 底部特别鸣谢入口横幅 -->
      <section class="aurora-glass aurora-glass-hover rounded-2xl p-4 sm:p-6 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 relative overflow-hidden">
        <div class="absolute -right-12 -bottom-12 w-48 h-48 bg-amber-500/15 rounded-full blur-2xl pointer-events-none"></div>
        <div class="flex items-center gap-3.5 sm:gap-4 relative z-10">
          <div class="w-11 h-11 sm:w-12 sm:h-12 rounded-2xl bg-gradient-to-br from-amber-400/20 to-rose-500/15 border border-amber-400/35 text-amber-300 flex items-center justify-center text-xl sm:text-2xl shrink-0 shadow-lg shadow-amber-500/15">
            🏆
          </div>
          <div>
            <h3 class="text-sm sm:text-base font-black text-white">毒瘤榜 · 特别鸣谢与官方认证殿堂</h3>
            <p class="text-[11px] sm:text-xs text-slate-300/80 mt-0.5">
              收录群内传世圣经语录与《CERTIFICATE OF TOXICITY 毒瘤认证》官方授权证书
            </p>
          </div>
        </div>

        <a
          href="/special-thanks"
          target="_blank"
          rel="noopener noreferrer"
          class="relative z-10 px-5 py-2.5 rounded-xl aurora-btn-gold font-black text-xs whitespace-nowrap flex items-center justify-center gap-2"
        >
          <span>✨ 特别鸣谢</span>
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
  remark?: string | null;
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
