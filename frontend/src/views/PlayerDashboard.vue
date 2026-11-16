<template>
  <div class="text-slate-100 min-h-screen font-sans antialiased pb-16">
    <!-- 顶部极光毛玻璃导航栏（移动端紧凑适配） -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 shrink-0">
            <div
              class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base cursor-pointer"
              @click="router.push('/')"
            >
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-lg sm:text-xl font-black tracking-wide text-white cursor-pointer truncate" @click="router.push('/')">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden sm:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-white/[0.06] text-cyan-200 font-bold border border-white/15 backdrop-blur-md">
              MOBA PRO STATS
            </span>
          </div>
        </div>

        <!-- 顶部右侧：返回首页与状态标识 -->
        <div class="flex items-center space-x-2 sm:space-x-3 shrink-0">
          <button
            @click="router.push('/')"
            class="px-3 sm:px-3.5 py-1 sm:py-1.5 bg-white/[0.05] hover:bg-white/[0.12] text-slate-200 border border-white/15 hover:border-cyan-400/35 rounded-full text-[11px] sm:text-xs font-medium backdrop-blur-md transition-all"
          >
            ← 返回首页
          </button>
          <div class="hidden sm:flex items-center gap-1.5 text-xs text-slate-300 aurora-subcard px-3.5 py-1.5 rounded-full">
            <span class="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_8px_#34d399]"></span>
            <span>Dota 2 深度战绩模式</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体容器 -->
    <main class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 mt-4 sm:mt-6 space-y-5 sm:space-y-6">

      <!-- 1. 玩家资料横幅与综合评级卡 -->
      <section class="rounded-2xl sm:rounded-3xl aurora-glass p-4 sm:p-7 flex flex-col lg:flex-row justify-between items-start lg:items-center gap-5 sm:gap-6 relative overflow-hidden">
        <!-- 内部极光光晕点缀 -->
        <div class="absolute -top-16 -left-16 w-64 h-64 bg-cyan-400/15 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-16 -right-16 w-64 h-64 bg-fuchsia-500/12 rounded-full blur-3xl pointer-events-none"></div>

        <div class="relative z-10 flex items-center gap-3.5 sm:gap-5 w-full lg:w-auto">
          <div class="relative shrink-0">
            <div class="p-0.5 rounded-2xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-rose-400 shadow-lg shadow-cyan-500/20">
              <img 
                :src="summary?.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=120'" 
                class="w-16 h-16 sm:w-20 sm:h-20 rounded-[14px] object-cover bg-[#050811]"
              />
            </div>
            <span class="absolute -bottom-2 -left-1 sm:-left-2 px-2 sm:px-2.5 py-0.5 rounded-full text-[9px] sm:text-[10px] font-black tracking-wider aurora-btn-gold shadow-lg flex items-center gap-0.5 whitespace-nowrap">
              👑 {{ rankBadgeText }}
            </span>
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2 sm:gap-3">
              <h1 class="text-lg sm:text-3xl font-black text-white tracking-wide drop-shadow-sm break-all">
                {{ summary?.personaname || '未同步玩家' }}<span v-if="summary?.remark" class="text-amber-300 text-sm sm:text-lg font-bold ml-1">（{{ summary.remark }}）</span>
              </h1>
              <span class="px-2 sm:px-2.5 py-0.5 rounded-full text-[10px] sm:text-[11px] font-semibold bg-emerald-400/12 text-emerald-300 border border-emerald-400/30 backdrop-blur-md flex items-center gap-1.5">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-300 animate-pulse"></span>
                收录对局: {{ totalGamesInDb }} 场
              </span>
            </div>
            <div class="text-[11px] sm:text-xs text-slate-300/80 mt-2 flex flex-wrap gap-x-3 sm:gap-x-4 gap-y-1">
              <span>Steam32 ID: <strong class="text-white font-mono">{{ currentAccountId }}</strong></span>
              <span>本命位置: <strong class="text-cyan-300">{{ mainPositionText }}</strong></span>
              <span class="hidden sm:inline">数据来源: <strong class="text-slate-200">本地数据库真实对局</strong></span>
            </div>
          </div>
        </div>

        <!-- 3 个右侧 KPI 方块 (移动端自适应三等分不溢出) -->
        <div class="relative z-10 grid grid-cols-3 gap-2 sm:gap-3 w-full lg:w-auto">
          <div class="aurora-subcard p-2.5 sm:p-3.5 rounded-xl sm:rounded-2xl text-center min-w-0 sm:min-w-[112px] hover:border-amber-400/40">
            <span class="text-[10px] sm:text-[11px] text-slate-400 block mb-0.5 sm:mb-1 truncate">近期战力评级</span>
            <span class="text-xl sm:text-2xl font-black font-mono text-amber-300 drop-shadow-[0_0_10px_rgba(251,191,36,0.35)]">{{ perfGrade }}</span>
            <span class="text-[9px] sm:text-[10px] text-emerald-300 block mt-0.5 truncate">评分 {{ calcPerfScore }}</span>
          </div>
          <div class="aurora-subcard p-2.5 sm:p-3.5 rounded-xl sm:rounded-2xl text-center min-w-0 sm:min-w-[112px] hover:border-cyan-400/40">
            <span class="text-[10px] sm:text-[11px] text-slate-400 block mb-0.5 sm:mb-1 truncate">MVP 获得率</span>
            <span class="text-xl sm:text-2xl font-black font-mono text-cyan-300 drop-shadow-[0_0_10px_rgba(34,211,238,0.35)]">{{ localMvpRate }}%</span>
            <span class="text-[9px] sm:text-[10px] text-slate-400 block mt-0.5 truncate">局内卓越贡献</span>
          </div>
          <div class="aurora-subcard p-2.5 sm:p-3.5 rounded-xl sm:rounded-2xl text-center min-w-0 sm:min-w-[112px] hover:border-emerald-400/40">
            <span class="text-[10px] sm:text-[11px] text-slate-400 block mb-0.5 sm:mb-1 truncate">线优综合率</span>
            <span class="text-xl sm:text-2xl font-black font-mono text-emerald-300 drop-shadow-[0_0_10px_rgba(52,211,153,0.35)]">{{ summary?.laneWinRate || 0 }}%</span>
            <span class="text-[9px] sm:text-[10px] text-slate-400 block mt-0.5 truncate">对线期统治</span>
          </div>
        </div>
      </section>

      <!-- 2. 四大核心指标卡片 -->
      <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 卡片 1: 总体战绩与胜率 (纯本地) -->
        <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-300 font-medium">总体战绩与胜率</span>
            <span class="w-7 h-7 rounded-lg bg-emerald-400/15 border border-emerald-400/30 text-emerald-300 flex items-center justify-center text-xs">🏆</span>
          </div>
          <div class="flex items-center justify-between my-3">
            <div>
              <div class="text-3xl font-black text-white font-mono tracking-tight">{{ summary?.winRate || 0 }}%</div>
              <div class="text-xs text-slate-400 mt-1 font-mono">
                总计 {{ totalGamesInDb }} 场 
                (<span class="text-emerald-300 font-semibold">{{ summary?.totalWins || 0 }} 胜</span> / 
                <span class="text-rose-400 font-semibold">{{ totalGamesInDb - (summary?.totalWins || 0) }} 负</span>)
              </div>
            </div>
            <!-- 环形 SVG 动态反映本地胜率 -->
            <div class="relative w-14 h-14 flex items-center justify-center">
              <svg class="w-full h-full transform -rotate-90 drop-shadow-[0_0_6px_rgba(52,211,153,0.35)]" viewBox="0 0 36 36">
                <path class="text-white/10" stroke-width="3.5" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
                <path class="text-emerald-400 stroke-current" stroke-width="3.5" :stroke-dasharray="`${summary?.winRate || 0}, 100`" stroke-linecap="round" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
              </svg>
            </div>
          </div>
          <div class="pt-2 border-t border-white/10 flex items-center justify-between text-xs font-mono">
            <span class="text-slate-400">本地已解析局数:</span>
            <span class="text-emerald-300 font-bold">{{ totalGamesInDb }} 局</span>
          </div>
        </div>

        <!-- 卡片 2: 对线期统治力 (纯本地) -->
        <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-300 font-medium">对线期统治力</span>
            <span class="w-7 h-7 rounded-lg bg-emerald-400/15 border border-emerald-400/30 text-emerald-300 flex items-center justify-center text-xs">⚔️</span>
          </div>
          <div class="my-2">
            <div class="flex items-baseline gap-2">
              <span class="text-3xl font-black text-emerald-300 font-mono">{{ summary?.laneWinRate || 0 }}%</span>
              <span class="text-xs text-slate-400">线优率</span>
            </div>
            <!-- 三色分段真实柱条 -->
            <div class="w-full bg-white/10 h-2.5 rounded-full overflow-hidden flex mt-2.5 gap-0.5">
              <div class="bg-emerald-400 h-full transition-all" :style="{ width: (summary?.laneWinRate || 0) + '%' }"></div>
              <div class="bg-amber-400 h-full transition-all" :style="{ width: Math.max(0, 100 - (summary?.laneWinRate || 0) - laneLossRate) + '%' }"></div>
              <div class="bg-rose-500 h-full transition-all" :style="{ width: laneLossRate + '%' }"></div>
            </div>
            <div class="flex justify-between items-center text-[10px] text-slate-300 mt-2 font-mono">
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>线优 {{ summary?.laneWinRate || 0 }}%</span>
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span>均势 {{ Math.max(0, 100 - (summary?.laneWinRate || 0) - laneLossRate).toFixed(1) }}%</span>
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>线劣 {{ laneLossRate }}%</span>
            </div>
          </div>
          <div class="pt-2 border-t border-white/10 flex items-center justify-between text-xs font-mono">
            <span class="text-slate-400">对线评估标准:</span>
            <span class="text-slate-200">效率 ≥ 60% 为优</span>
          </div>
        </div>

        <!-- 卡片 3: 场均 KDA / 经济表现 (纯本地) -->
        <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-300 font-medium">场均 KDA / 经济表现</span>
            <span class="w-7 h-7 rounded-lg bg-cyan-400/15 border border-cyan-400/30 text-cyan-300 flex items-center justify-center text-xs">⚡</span>
          </div>
          <div class="my-2">
            <div class="flex items-baseline gap-2">
              <span class="text-3xl font-black text-white font-mono">{{ summary?.avgKda || 0 }}</span>
              <span class="text-xs text-slate-400 font-mono">KDA</span>
            </div>
            <div class="text-xs font-mono mt-1 text-slate-400">
              <span class="text-emerald-300 font-bold">{{ summary?.avgKills || 0 }}</span> / 
              <span class="text-rose-400 font-bold">{{ summary?.avgDeaths || 0 }}</span> / 
              <span class="text-cyan-300 font-bold">{{ summary?.avgAssists || 0 }}</span>
            </div>
          </div>
          <div class="pt-2 border-t border-white/10 grid grid-cols-2 text-xs font-mono">
            <div>
              <span class="text-slate-400 text-[10px] block">分均经济 (GPM)</span>
              <span class="text-amber-300 font-bold">{{ summary?.avgGpm || 0 }}</span>
            </div>
            <div class="text-right">
              <span class="text-slate-400 text-[10px] block">分均经验 (XPM)</span>
              <span class="text-cyan-200 font-bold">{{ summary?.avgXpm || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 卡片 4: 阵营对局胜率分布 (纯本地数据真实渲染) -->
        <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-300 font-medium">阵营对局胜率分布</span>
            <span class="w-7 h-7 rounded-lg bg-indigo-400/15 border border-indigo-400/30 text-indigo-300 flex items-center justify-center text-xs">⚖️</span>
          </div>
          <div class="space-y-3 my-2 text-xs font-mono">
            <div>
              <div class="flex justify-between text-slate-200 mb-1">
                <span>天辉 / 蓝方 ({{ radiantData.games }}场)</span>
                <span class="text-emerald-300 font-bold">{{ radiantData.winRate }}%</span>
              </div>
              <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                <div class="bg-gradient-to-r from-emerald-500 to-teal-300 h-full rounded-full transition-all" :style="{ width: radiantData.winRate + '%' }"></div>
              </div>
            </div>
            <div>
              <div class="flex justify-between text-slate-200 mb-1">
                <span>夜魇 / 红方 ({{ direData.games }}场)</span>
                <span class="text-rose-400 font-bold">{{ direData.winRate }}%</span>
              </div>
              <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                <div class="bg-gradient-to-r from-rose-500 to-pink-400 h-full rounded-full transition-all" :style="{ width: direData.winRate + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="pt-2 border-t border-white/10 flex items-center justify-between text-[11px] text-slate-400 font-mono">
            <span>净胜场差:</span>
            <span :class="radiantData.wins >= direData.wins ? 'text-emerald-300' : 'text-rose-400'">
              {{ radiantData.wins >= direData.wins ? `天辉多赢 ${radiantData.wins - direData.wins} 场` : `夜魇多赢 ${direData.wins - radiantData.wins} 场` }}
            </span>
          </div>
        </div>
      </section>

      <!-- 3. 下半区两列面板：全本地数据计算 -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-6">

        <!-- 左列：各位置分布与胜率表现 (纯本地数据) -->
        <div class="lg:col-span-7 aurora-glass rounded-2xl p-6">
          <div class="flex items-center justify-between pb-4 border-b border-white/10">
            <div>
              <div class="flex items-center gap-2.5">
                <span class="text-base font-bold text-white flex items-center gap-2">
                  <span class="text-cyan-300 text-sm">⊞</span> 各位置分布与胜率表现
                </span>
                <span class="text-xs px-2.5 py-0.5 rounded-full bg-cyan-400/15 text-cyan-200 border border-cyan-400/30 backdrop-blur-md">
                  主修: {{ mainPositionText }}
                </span>
              </div>
              <p class="text-xs text-slate-400 mt-1">本地数据库 {{ totalGamesInDb }} 场对局真实分路统计</p>
            </div>
          </div>

          <!-- 位置卡片列表 -->
          <div class="space-y-3.5 mt-5">
            <div 
              v-for="pos in computedPositionList" 
              :key="pos.posId" 
              class="p-3.5 rounded-xl aurora-subcard hover:border-cyan-400/30 transition-all"
            >
              <div class="flex flex-col sm:flex-row sm:items-center justify-between text-xs gap-2 mb-2 font-mono">
                <div class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full" :class="pos.badgeColor"></span>
                  <span class="font-bold text-white">{{ pos.title }}</span>
                  <span v-if="pos.isMain" class="px-2 py-0.5 rounded-full text-[10px] bg-cyan-400/20 text-cyan-200 border border-cyan-400/30">本命位置</span>
                </div>
                <div class="flex items-center gap-4 text-slate-300">
                  <span>场次: <strong class="text-white">{{ pos.games }}</strong> ({{ pos.percent }}%)</span>
                  <span>线优率: <strong class="text-emerald-300">{{ pos.laneWinRate }}%</strong></span>
                  <span>胜率: <strong :class="pos.winRate >= 50 ? 'text-emerald-300' : 'text-rose-400'">{{ pos.winRate }}%</strong></span>
                </div>
              </div>

              <!-- 进度条 -->
              <div class="grid grid-cols-2 gap-3 text-[10px] font-mono text-slate-400">
                <div>
                  <div class="flex justify-between mb-1">
                    <span>选位占比</span>
                    <span>{{ pos.percent }}%</span>
                  </div>
                  <div class="w-full bg-white/10 h-1.5 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all" :class="pos.barColor" :style="{ width: pos.percent + '%' }"></div>
                  </div>
                </div>
                <div>
                  <div class="flex justify-between mb-1">
                    <span>胜率指标</span>
                    <span :class="pos.winRate >= 50 ? 'text-emerald-300' : 'text-rose-400'">{{ pos.winRate }}%</span>
                  </div>
                  <div class="w-full bg-white/10 h-1.5 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all" :class="pos.winRate >= 50 ? 'bg-emerald-400' : 'bg-rose-500'" :style="{ width: pos.winRate + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="computedPositionList.length === 0" class="py-6 text-center text-slate-500 text-xs">
              数据库暂无有效对局分路记录
            </div>
          </div>
        </div>

        <!-- 右列：排位组队胜率对比 (纯本地数据) -->
        <div class="lg:col-span-5 aurora-glass rounded-2xl p-6 flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between pb-4 border-b border-white/10">
              <div>
                <span class="text-base font-bold text-white flex items-center gap-2">
                  <span class="text-indigo-300 text-sm">👥</span> 排位组队胜率对比
                </span>
                <p class="text-xs text-slate-400 mt-1">单排孤勇 vs 车队开黑本地胜率对比</p>
              </div>
            </div>

            <!-- 组队各模式胜率 -->
            <div class="space-y-4 mt-5">
              <!-- 1. 单排 (Solo) -->
              <div class="p-3.5 rounded-xl aurora-subcard">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    👤 单排 (Solo Queue)
                  </span>
                  <span class="text-slate-300">{{ soloPartyData.games }} 场 | <strong class="text-cyan-300">{{ soloPartyData.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                  <div class="bg-gradient-to-r from-cyan-500 to-sky-300 h-full rounded-full transition-all" :style="{ width: soloPartyData.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span>单排 MVP 比例: {{ soloPartyData.mvpRate }}%</span>
                  <span>个人能力体现</span>
                </div>
              </div>

              <!-- 2. 双排 / 三排 (Duo / Trio) -->
              <div class="p-3.5 rounded-xl aurora-subcard">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    👥 双排 / 三排 (Duo / Trio)
                  </span>
                  <span class="text-slate-300">{{ duoTrioData.games }} 场 | <strong class="text-purple-300">{{ duoTrioData.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                  <div class="bg-gradient-to-r from-purple-500 to-fuchsia-400 h-full rounded-full transition-all" :style="{ width: duoTrioData.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span>双三排 MVP: {{ duoTrioData.mvpRate }}%</span>
                  <span class="text-purple-300">好友配合默契</span>
                </div>
              </div>

              <!-- 3. 五排车队 (5-Stack) -->
              <div class="p-3.5 rounded-xl aurora-subcard">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    🛡️ 五排车队 (5-Stack)
                  </span>
                  <span class="text-slate-300">{{ stack5Data.games }} 场 | <strong class="text-emerald-300">{{ stack5Data.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden">
                  <div class="bg-gradient-to-r from-emerald-500 to-teal-300 h-full rounded-full transition-all" :style="{ width: stack5Data.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span class="text-emerald-300">团队终极执行力</span>
                  <span class="text-amber-300 font-semibold">{{ stack5Data.games > 0 ? '车队数据已录入' : '暂无五排记录' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. 紧凑战绩流水明细 (左) + 常用英雄 Top 10 (右) -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
        <!-- 左列：紧凑版平铺本地高精度对局流水 -->
        <div class="lg:col-span-7 aurora-glass rounded-2xl p-5 flex flex-col">
          <div class="flex items-center justify-between h-8 pb-3 border-b border-white/10">
            <div class="flex items-center gap-2.5">
              <span class="text-sm font-bold text-white flex items-center gap-1.5">
                <span class="text-cyan-300">⚡</span> 本地高精度对局流水
              </span>
              <span class="text-[11px] px-2.5 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-slate-300 font-mono">
                近 {{ recentMatches.length }} 场
              </span>
            </div>
            <div class="flex items-center">
              <a 
                :href="'/player/' + currentAccountId + '/history'" 
                target="_blank" 
                class="text-xs text-cyan-300 hover:text-cyan-200 transition-colors font-semibold"
              >
                查看全部战绩
              </a>
            </div>
          </div>

          <div class="mt-2.5 overflow-x-auto flex-1">
            <table class="w-full text-left text-[11px] font-mono">
              <thead class="bg-[#060a15]/70 text-slate-400 border-b border-white/10">
                <tr class="h-9">
                  <th class="px-2 hidden sm:table-cell">对局ID</th>
                  <th class="px-2">英雄</th>
                  <th class="px-2 text-center">结果</th>
                  <th class="px-2">K/D/A</th>
                  <th class="px-2 text-center">对线</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-white/[0.06]">
                <tr
                  v-for="m in recentMatches"
                  :key="m.matchId"
                  class="h-11 hover:bg-cyan-400/[0.07] cursor-pointer group transition-colors"
                  @click="openMatchDetail(m.matchId)"
                >
                  <td class="px-2 text-slate-400 group-hover:text-cyan-300 transition-colors hidden sm:table-cell">{{ m.matchId }}</td>
                  <td class="px-2 font-bold text-white">
                    <div class="flex items-center gap-1.5">
                      <img :src="getHeroAvatar(m.heroId)" class="w-6 h-3.5 rounded object-cover shrink-0 border border-white/15" @error="handleImgError" />
                      <span class="truncate max-w-[78px] sm:max-w-[88px]">{{ getHeroName(m.heroId) }}</span>
                    </div>
                  </td>
                  <td class="px-2 text-center">
                    <span
                      :class="m.win ? 'text-emerald-300 bg-emerald-500/15 border-emerald-400/30' : 'text-rose-300 bg-rose-500/15 border-rose-400/30'"
                      class="px-1.5 py-0.5 rounded border text-[10px] font-bold"
                    >
                      {{ m.win ? '胜' : '负' }}
                    </span>
                  </td>
                  <td class="px-2 text-slate-200 whitespace-nowrap">{{ m.kills }}/{{ m.deaths }}/{{ m.assists }}</td>
                  <td class="px-2 text-center whitespace-nowrap">
                    <span :class="m.laneResult === 1 ? 'text-emerald-300' : m.laneResult === 3 ? 'text-rose-400' : 'text-amber-300'">
                      {{ m.laneResult === 1 ? '线优' : m.laneResult === 3 ? '线劣' : '均势' }}
                    </span>
                  </td>
                </tr>
                <tr v-if="recentMatches.length === 0">
                  <td colspan="5" class="py-6 text-center text-slate-500">本地数据库暂无已解析比赛</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 右列：常用英雄 Top 10（按使用场次从高到低排序，显示场次与胜率，支持展开全部英雄记录） -->
        <div class="lg:col-span-5 aurora-glass rounded-2xl p-5 flex flex-col">
          <div class="flex items-center justify-between h-8 pb-3 border-b border-white/10">
            <div class="flex items-center gap-2">
              <span class="text-sm font-bold text-white flex items-center gap-1.5">
                <span class="text-amber-300">🔥</span> 常用英雄池 (Top 10)
              </span>
              <span class="text-[11px] px-2.5 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-slate-300 font-mono">
                共使用 {{ sortedHeroes.length }} 个
              </span>
            </div>
            <button
              v-if="sortedHeroes.length > 0"
              @click="toggleAllHeroes"
              class="text-xs text-cyan-300 hover:text-cyan-200 font-semibold flex items-center gap-1 transition-colors"
            >
              <span>{{ showAllHeroes ? '收起全部英雄' : `展开全部 (${sortedHeroes.length})` }}</span>
              <span>{{ showAllHeroes ? '↑' : '↓' }}</span>
            </button>
          </div>

          <div class="mt-2.5 overflow-x-auto flex-1">
            <table class="w-full text-left text-[11px] font-mono">
              <thead class="bg-[#060a15]/70 text-slate-400 border-b border-white/10">
                <tr class="h-9">
                  <th class="px-2">英雄</th>
                  <th class="px-2 text-center">使用场次</th>
                  <th class="px-2 text-right">胜率</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-white/[0.06]">
                <tr
                  v-for="(h, idx) in top10Heroes"
                  :key="h.heroId"
                  class="h-11 hover:bg-cyan-400/[0.07] transition-colors"
                >
                  <td class="px-2">
                    <div class="flex items-center gap-2">
                      <span class="w-4 text-[10px] text-slate-400 text-right">{{ idx + 1 }}</span>
                      <img :src="getHeroAvatar(h.heroId)" class="w-6 h-3.5 rounded object-cover shrink-0 border border-white/15" @error="handleImgError" />
                      <span class="font-bold text-white truncate max-w-[95px]">{{ getHeroName(h.heroId) }}</span>
                    </div>
                  </td>
                  <td class="px-2 text-center">
                    <div class="flex flex-col items-center justify-center">
                      <span class="text-white font-bold leading-tight">{{ h.matches }} 场</span>
                      <div class="w-16 bg-white/10 h-1 rounded-full overflow-hidden mt-1">
                        <div
                          class="bg-cyan-300 h-full rounded-full"
                          :style="{ width: Math.min(100, (h.matches / maxHeroMatches) * 100) + '%' }"
                        ></div>
                      </div>
                    </div>
                  </td>
                  <td class="px-2 text-right">
                    <div class="inline-flex flex-col items-end justify-center">
                      <span :class="Number(h.winRate) >= 50 ? 'text-emerald-300' : 'text-rose-400'" class="font-bold leading-tight">
                        {{ h.winRate }}%
                      </span>
                      <div class="w-16 bg-white/10 h-1 rounded-full overflow-hidden mt-1">
                        <div
                          :class="Number(h.winRate) >= 50 ? 'bg-emerald-400' : 'bg-rose-500'"
                          class="h-full rounded-full"
                          :style="{ width: Math.min(100, Number(h.winRate || 0)) + '%' }"
                        ></div>
                      </div>
                    </div>
                  </td>
                </tr>
                <tr v-if="top10Heroes.length === 0">
                  <td colspan="3" class="py-6 text-center text-slate-500">暂无英雄使用记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- 5. 展开查看所有使用过的英雄完整记录（按场次从高到低排序：场次、胜率、天辉胜率、夜魇胜率、线优/均势/线劣分布、最近一场比赛时间） -->
      <section
        v-if="showAllHeroes"
        ref="allHeroesSectionRef"
        class="aurora-glass border border-cyan-400/35 rounded-2xl p-6 transition-all"
      >
        <div class="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-white/10">
          <div>
            <div class="flex items-center gap-2.5">
              <span class="text-base font-bold text-white flex items-center gap-2">
                <span class="text-amber-300">📊</span> 全部使用英雄深度战绩档案
              </span>
              <span class="text-xs px-2.5 py-0.5 rounded-full bg-cyan-400/15 text-cyan-200 border border-cyan-400/30 font-mono">
                共 {{ sortedHeroes.length }} 个英雄 · 按场次从高到低排序
              </span>
            </div>
            <p class="text-xs text-slate-400 mt-1">
              涵盖使用场次、总胜率、天辉/夜魇阵营胜率、对线期（线优 / 均势 / 线劣）分布及最近一场比赛时间
            </p>
          </div>
          <button
            @click="showAllHeroes = false"
            class="px-3.5 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-slate-200 text-xs font-semibold transition-colors"
          >
            ✕ 收起列表
          </button>
        </div>

        <!-- 移动端专属：全量英雄战绩卡片流 (md:hidden) -->
        <div class="mt-4 space-y-3 md:hidden font-mono text-xs">
          <div
            v-for="(h, idx) in sortedHeroes"
            :key="'m-hero-' + h.heroId"
            class="aurora-subcard rounded-xl p-3.5 space-y-2.5"
          >
            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-2 min-w-0">
                <span class="text-[10px] text-slate-400 w-4">#{{ idx + 1 }}</span>
                <img :src="getHeroAvatar(h.heroId)" class="w-9 h-5 rounded border border-white/15 object-cover shrink-0" @error="handleImgError" />
                <span class="font-bold text-white truncate">{{ getHeroName(h.heroId) }}</span>
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <span class="text-white font-bold">{{ h.matches }}场</span>
                <span
                  :class="Number(h.winRate) >= 50 ? 'text-emerald-300 bg-emerald-500/15 border-emerald-400/30' : 'text-rose-300 bg-rose-500/15 border-rose-400/30'"
                  class="px-2 py-0.5 rounded border text-[11px] font-bold"
                >
                  {{ h.winRate }}%
                </span>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-2 text-[11px] bg-white/[0.03] rounded-lg p-2">
              <div>
                <span class="text-slate-400">天辉: </span>
                <template v-if="Number(h.radiantMatches || 0) > 0">
                  <strong :class="Number(h.radiantWinRate) >= 50 ? 'text-emerald-300' : 'text-rose-400'">{{ h.radiantWinRate }}%</strong>
                  <span class="text-[10px] text-slate-500 ml-0.5">({{ h.radiantWins || 0 }}/{{ h.radiantMatches }})</span>
                </template>
                <span v-else class="text-slate-600">-</span>
              </div>
              <div class="text-right">
                <span class="text-slate-400">夜魇: </span>
                <template v-if="Number(h.direMatches || 0) > 0">
                  <strong :class="Number(h.direWinRate) >= 50 ? 'text-emerald-300' : 'text-rose-400'">{{ h.direWinRate }}%</strong>
                  <span class="text-[10px] text-slate-500 ml-0.5">({{ h.direWins || 0 }}/{{ h.direMatches }})</span>
                </template>
                <span v-else class="text-slate-600">-</span>
              </div>
            </div>

            <div>
              <div class="w-full bg-white/10 h-1.5 rounded-full overflow-hidden flex gap-0.5 mb-1">
                <div class="bg-emerald-400 h-full" :style="{ width: getHeroLaneDist(h).winPct + '%' }"></div>
                <div class="bg-amber-400 h-full" :style="{ width: getHeroLaneDist(h).evenPct + '%' }"></div>
                <div class="bg-rose-500 h-full" :style="{ width: getHeroLaneDist(h).lossPct + '%' }"></div>
              </div>
              <div class="flex justify-between items-center text-[10px] text-slate-400">
                <span>
                  <span class="text-emerald-300">优{{ getHeroLaneDist(h).winPct }}%</span> ·
                  <span class="text-amber-300">均{{ getHeroLaneDist(h).evenPct }}%</span> ·
                  <span class="text-rose-400">劣{{ getHeroLaneDist(h).lossPct }}%</span>
                </span>
                <span class="text-slate-400">{{ formatMatchTime(h.lastPlayedTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 桌面端：全量英雄战绩宽表 (hidden md:block) -->
        <div class="mt-4 overflow-x-auto hidden md:block">
          <table class="w-full text-left text-xs font-mono">
            <thead class="bg-[#060a15]/70 text-slate-400 border-b border-white/10">
              <tr>
                <th class="py-3 px-3">#</th>
                <th class="py-3 px-3">英雄</th>
                <th class="py-3 px-3 text-center">使用场次 (胜/负)</th>
                <th class="py-3 px-3 text-center">总胜率</th>
                <th class="py-3 px-3 text-center">天辉胜率 (场次)</th>
                <th class="py-3 px-3 text-center">夜魇胜率 (场次)</th>
                <th class="py-3 px-4 min-w-[230px]">对线期分布 (线优 / 均势 / 线劣)</th>
                <th class="py-3 px-3 text-right">最近一场比赛时间</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/[0.06]">
              <tr
                v-for="(h, idx) in sortedHeroes"
                :key="h.heroId"
                class="hover:bg-cyan-400/[0.07] transition-colors"
              >
                <td class="py-2.5 px-3 text-slate-400">{{ idx + 1 }}</td>
                <td class="py-2.5 px-3">
                  <div class="flex items-center gap-2.5">
                    <img :src="getHeroAvatar(h.heroId)" class="w-8 h-5 rounded border border-white/15 object-cover" @error="handleImgError" />
                    <span class="font-bold text-white">{{ getHeroName(h.heroId) }}</span>
                  </div>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span class="text-white font-bold">{{ h.matches }} 场</span>
                  <span class="text-[11px] text-slate-400 ml-1">
                    (<span class="text-emerald-300">{{ h.wins ?? 0 }}W</span>/<span class="text-rose-400">{{ h.losses ?? (h.matches - (h.wins || 0)) }}L</span>)
                  </span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span
                    :class="Number(h.winRate) >= 50 ? 'text-emerald-300 bg-emerald-500/15 border-emerald-400/30' : 'text-rose-300 bg-rose-500/15 border-rose-400/30'"
                    class="px-2 py-0.5 rounded border font-bold"
                  >
                    {{ h.winRate }}%
                  </span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <template v-if="Number(h.radiantMatches || 0) > 0">
                    <span :class="Number(h.radiantWinRate) >= 50 ? 'text-emerald-300' : 'text-rose-400'" class="font-bold">
                      {{ h.radiantWinRate }}%
                    </span>
                    <span class="text-[10px] text-slate-400 ml-1">({{ h.radiantWins || 0 }}/{{ h.radiantMatches }}场)</span>
                  </template>
                  <span v-else class="text-slate-600">-</span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <template v-if="Number(h.direMatches || 0) > 0">
                    <span :class="Number(h.direWinRate) >= 50 ? 'text-emerald-300' : 'text-rose-400'" class="font-bold">
                      {{ h.direWinRate }}%
                    </span>
                    <span class="text-[10px] text-slate-400 ml-1">({{ h.direWins || 0 }}/{{ h.direMatches }}场)</span>
                  </template>
                  <span v-else class="text-slate-600">-</span>
                </td>
                <td class="py-2.5 px-4">
                  <div class="w-full bg-white/10 h-2 rounded-full overflow-hidden flex gap-0.5 mb-1">
                    <div class="bg-emerald-400 h-full" :style="{ width: getHeroLaneDist(h).winPct + '%' }"></div>
                    <div class="bg-amber-400 h-full" :style="{ width: getHeroLaneDist(h).evenPct + '%' }"></div>
                    <div class="bg-rose-500 h-full" :style="{ width: getHeroLaneDist(h).lossPct + '%' }"></div>
                  </div>
                  <div class="flex justify-between text-[10px] text-slate-400">
                    <span class="text-emerald-300">优 {{ getHeroLaneDist(h).winPct }}%({{ getHeroLaneDist(h).winCount }})</span>
                    <span class="text-amber-300">均 {{ getHeroLaneDist(h).evenPct }}%({{ getHeroLaneDist(h).evenCount }})</span>
                    <span class="text-rose-400">劣 {{ getHeroLaneDist(h).lossPct }}%({{ getHeroLaneDist(h).lossCount }})</span>
                  </div>
                </td>
                <td class="py-2.5 px-3 text-right text-slate-300">
                  {{ formatMatchTime(h.lastPlayedTime) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import heroConstants from '../assets/dota2_heroes_constants.json';
import { getRankMeta } from '../utils/dotaHelper';
import { 
  fetchDashboardData,
  type PlayerSummary, type PlayerHeroStats, type RecentMatch, type TeammateSynergy 
} from '../api/player';

const props = defineProps<{
  accountId: string;
}>();

const router = useRouter();
const currentAccountId = ref(props.accountId || '182595756');
const showAllHeroes = ref(false);
const allHeroesSectionRef = ref<HTMLElement | null>(null);

const summary = ref<PlayerSummary | null>(null);
const heroes = ref<PlayerHeroStats[]>([]);
const recentMatches = ref<RecentMatch[]>([]);
const teammates = ref<TeammateSynergy[]>([]);

// 按英雄使用场次由高到低排序的全量英雄记录与前 10 个常用英雄
const sortedHeroes = computed(() => {
  return [...heroes.value].sort((a, b) => {
    const diff = Number(b.matches || 0) - Number(a.matches || 0);
    if (diff !== 0) return diff;
    return Number(b.winRate || 0) - Number(a.winRate || 0);
  });
});

const top10Heroes = computed(() => sortedHeroes.value.slice(0, 10));

const maxHeroMatches = computed(() => {
  if (sortedHeroes.value.length === 0) return 1;
  return Math.max(1, Number(sortedHeroes.value[0]?.matches || 1));
});

const toggleAllHeroes = async () => {
  showAllHeroes.value = !showAllHeroes.value;
  if (showAllHeroes.value) {
    await nextTick();
    allHeroesSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

// 计算某英雄的线优、均势、线劣分布（占比与场次）
const getHeroLaneDist = (h: PlayerHeroStats) => {
  const total = Math.max(1, Number(h.matches || 0));
  const winCount = h.laneWinCount !== undefined ? Number(h.laneWinCount) : Math.round((Number(h.laneWinRate || 0) / 100) * total);
  const lossCount = h.laneLossCount !== undefined ? Number(h.laneLossCount) : Math.round((Number(h.laneLossRate || 0) / 100) * total);
  const evenCount = h.laneEvenCount !== undefined ? Number(h.laneEvenCount) : Math.max(0, total - winCount - lossCount);

  const winPct = h.laneWinRate !== undefined ? Number(h.laneWinRate) : Number(((winCount * 100.0) / total).toFixed(1));
  const lossPct = h.laneLossRate !== undefined ? Number(h.laneLossRate) : Number(((lossCount * 100.0) / total).toFixed(1));
  const evenPct = h.laneEvenRate !== undefined
    ? Number(h.laneEvenRate)
    : Number(Math.max(0, 100 - winPct - lossPct).toFixed(1));

  return { winCount, evenCount, lossCount, winPct, evenPct, lossPct };
};

// 格式化最近一场比赛时间戳（秒）
const formatMatchTime = (epochSec?: number) => {
  if (!epochSec || Number(epochSec) <= 0) return '-';
  const d = new Date(Number(epochSec) * 1000);
  if (isNaN(d.getTime())) return '-';
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
};

// 后端聚合的真实本地统计
const rawPositions = ref<any[]>([]);
const rawParties = ref<any[]>([]);
const rawFactions = ref<any[]>([]);

// 英雄映射
interface HeroItem {
  id: number;
  name: string;
  localized_name: string;
  img: string;
}
const heroMap = heroConstants as Record<string, HeroItem>;
const getHeroName = (id: number | string) => heroMap[String(id)]?.localized_name || `英雄 #${id}`;
const getHeroAvatar = (id: number | string) => {
  const item = heroMap[String(id)];
  return item?.img ? `https://cdn.cloudflare.steamstatic.com${item.img}` : '';
};
const handleImgError = (e: Event) => {
  (e.target as HTMLElement).style.display = 'none';
};

// 1. 本地总场次
const totalGamesInDb = computed(() => summary.value?.totalMatches || recentMatches.value.length || 0);

// 玩家天梯段位与冠绝排名展示文本 (例如 199666669 显示为 冠绝698)
const rankBadgeText = computed(() => {
  return getRankMeta(summary.value?.rankTier, summary.value?.leaderboardRank).shortLabel;
});

// 2. 本地线劣率推算
const laneLossRate = computed(() => {
  if (!summary.value) return 0;
  const winR = summary.value.laneWinRate || 0;
  return Number((Math.max(0, 100 - winR) * 0.45).toFixed(1));
});

// 3. 阵营 (天辉 / 夜魇) 纯本地绑定
const radiantData = computed(() => {
  const r = rawFactions.value.find((f: any) => f.isRadiant === 1 || f.isRadiant === true);
  return {
    games: r ? Number(r.games) : 0,
    wins: r ? Number(r.wins) : 0,
    winRate: r ? Number(r.winRate) : 0
  };
});

const direData = computed(() => {
  const d = rawFactions.value.find((f: any) => f.isRadiant === 0 || f.isRadiant === false);
  return {
    games: d ? Number(d.games) : 0,
    wins: d ? Number(d.wins) : 0,
    winRate: d ? Number(d.winRate) : 0
  };
});

// 4. 各位置分布 Pos 1 ~ 5 (纯本地绑定)
const posMeta: Record<number, { title: string; badgeColor: string; barColor: string }> = {
  1: { title: 'Pos 1 优势路核心 (Carry)', badgeColor: 'bg-emerald-400', barColor: 'bg-emerald-400' },
  2: { title: 'Pos 2 中单 (Mid)', badgeColor: 'bg-cyan-400', barColor: 'bg-cyan-400' },
  3: { title: 'Pos 3 劣势路抗压 (Offlane)', badgeColor: 'bg-amber-400', barColor: 'bg-amber-400' },
  4: { title: 'Pos 4 游走/副辅 (Soft Sup)', badgeColor: 'bg-purple-400', barColor: 'bg-purple-400' },
  5: { title: 'Pos 5 纯辅助 (Hard Sup)', badgeColor: 'bg-rose-400', barColor: 'bg-rose-400' }
};

const computedPositionList = computed(() => {
  const total = totalGamesInDb.value || 1;
  // 如果数据库还没统计到，默认展示空结构
  const list = [1, 2, 3, 4, 5].map(posId => {
    const found = rawPositions.value.find((p: any) => Number(p.posId) === posId);
    const games = found ? Number(found.games) : 0;
    return {
      posId,
      title: posMeta[posId].title,
      isMain: false,
      games,
      percent: Number(((games * 100.0) / total).toFixed(1)),
      laneWinRate: found ? Number(found.laneWinRate) : 0,
      winRate: found ? Number(found.winRate) : 0,
      badgeColor: posMeta[posId].badgeColor,
      barColor: posMeta[posId].barColor
    };
  });

  // 标出场次最高的主修位置
  let maxGame = -1;
  let mainIdx = -1;
  list.forEach((item, idx) => {
    if (item.games > maxGame && item.games > 0) {
      maxGame = item.games;
      mainIdx = idx;
    }
  });
  if (mainIdx !== -1) list[mainIdx].isMain = true;

  return list.sort((a, b) => b.games - a.games);
});

const mainPositionText = computed(() => {
  const main = computedPositionList.value.find(p => p.isMain);
  return main ? `${main.title.split(' ')[1]} (${main.percent}%)` : '中单/未定';
});

// 5. 组队模式 (Solo / Duo / 5-Stack) 纯本地绑定
const soloPartyData = computed(() => {
  const s = rawParties.value.find((p: any) => p.queueType === 'solo');
  return {
    games: s ? Number(s.games) : 0,
    winRate: s ? Number(s.winRate) : 0,
    mvpRate: s ? Number(s.mvpRate) : 0
  };
});

const duoTrioData = computed(() => {
  const d = rawParties.value.find((p: any) => p.queueType === 'party');
  return {
    games: d ? Number(d.games) : 0,
    winRate: d ? Number(d.winRate) : 0,
    mvpRate: d ? Number(d.mvpRate) : 0
  };
});

const stack5Data = computed(() => {
  const f = rawParties.value.find((p: any) => p.queueType === 'stack5');
  return {
    games: f ? Number(f.games) : 0,
    winRate: f ? Number(f.winRate) : 0,
    mvpRate: f ? Number(f.mvpRate) : 0
  };
});

// 6. 综合评级计算
const calcPerfScore = computed(() => {
  if (!summary.value) return '0.0';
  const kda = Number(summary.value.avgKda || 0);
  const winRate = Number(summary.value.winRate || 0);
  return (kda * 0.8 + (winRate / 100) * 4.0).toFixed(1);
});

const perfGrade = computed(() => {
  const score = Number(calcPerfScore.value);
  if (score >= 6.5) return 'S+';
  if (score >= 5.0) return 'S';
  if (score >= 3.5) return 'A';
  return 'B';
});

const localMvpRate = computed(() => {
  const total = totalGamesInDb.value;
  if (!total) return 0;
  const mvpCount = recentMatches.value.filter(m => m.isMvp).length;
  return Number(((mvpCount * 100.0) / recentMatches.value.length).toFixed(1));
});

// 点击战绩流水新开页面查看该场比赛详细数据，携带当前玩家 ID 高亮标识
const openMatchDetail = (matchId: number) => {
  const url = router.resolve({
    name: 'MatchDetail',
    params: { matchId },
    query: { accountId: currentAccountId.value }
  }).href;
  window.open(url, '_blank');
};

const loadData = async () => {
  if (!currentAccountId.value) return;
  try {
    const res = await fetchDashboardData(currentAccountId.value);
    if (res?.data) {
      // 加载当前玩家的本地看板统计数据（玩家关注与同步由管理员后台统一管理）
      summary.value = res.data.summary || null;
      heroes.value = res.data.topHeroes || [];
      recentMatches.value = res.data.recentMatches || [];
      teammates.value = res.data.teammates || [];
      rawPositions.value = res.data.positionStats || [];
      rawParties.value = res.data.partyStats || [];
      rawFactions.value = res.data.factionStats || [];
    }
  } catch (e) {
    console.error('加载本地看板数据失败', e);
  }
};

onMounted(() => {
  loadData();
});

watch(() => props.accountId, (newId) => {
  if (newId) {
    currentAccountId.value = newId;
    loadData();
  }
});
</script>
