<template>
  <div class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-16 selection:bg-cyan-500 selection:text-white">
    <!-- 顶部导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black" @click="router.push('/')" style="cursor: pointer;">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white">毒瘤榜<span class="text-cyan-400">.GG</span></span>
            <span class="text-[10px] uppercase tracking-wider px-1.5 py-0.5 rounded bg-cyan-950/80 text-cyan-400 font-bold border border-cyan-800/50">MOBA PRO STATS</span>
          </div>
        </div>

        <!-- 搜索与数据模式切换 -->
        <div class="flex items-center space-x-4">
          <div class="relative">
            <input 
              v-model="inputAccountId" 
              type="text" 
              placeholder="搜索玩家 ID / 英雄 / 对局编号..." 
              class="w-72 bg-[#121721] border border-slate-700/60 text-xs rounded-lg px-3 py-2 pl-8 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 text-slate-200 placeholder-slate-500 transition-all"
              @keydown.enter="handleSearch"
            />
            <span class="absolute left-2.5 top-2 text-slate-500 text-xs">🔍</span>
          </div>
          <button 
            @click="handleStartSync" 
            :disabled="syncing"
            class="px-3 py-1.5 bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 rounded-lg text-xs font-semibold hover:bg-cyan-500/20 active:scale-95 transition-all disabled:opacity-50"
          >
            {{ syncing ? '同步中...' : '录入/刷新' }}
          </button>
          <div class="flex items-center gap-1.5 text-xs text-slate-400 bg-[#121721] px-3 py-1.5 rounded-lg border border-slate-800">
            <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
            <span>Dota 2 深度战绩模式</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体容器 -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">

      <!-- 1. 玩家资料横幅与综合评级卡 -->
      <section class="rounded-2xl bg-[#121721] border border-slate-800/80 p-6 flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6 shadow-xl shadow-black/40 relative overflow-hidden">
        <div class="flex items-center gap-5">
          <div class="relative">
            <img 
              :src="summary?.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=120'" 
              class="w-20 h-20 rounded-2xl border-2 border-slate-700/80 object-cover shadow-md"
            />
            <span class="absolute -bottom-2 -left-2 px-2 py-0.5 rounded text-[10px] font-black tracking-wider bg-amber-500 text-black shadow-md flex items-center gap-0.5">
              👑 Rank {{ summary?.rankTier || 0 }}
            </span>
          </div>
          <div>
            <div class="flex items-center gap-3">
              <h1 class="text-2xl font-black text-white tracking-wide">{{ summary?.personaname || '未同步玩家' }}</h1>
              <span class="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 flex items-center gap-1">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                数据库记录对局: {{ totalGamesInDb }} 场
              </span>
            </div>
            <div class="text-xs text-slate-400 mt-2 flex flex-wrap gap-x-4 gap-y-1">
              <span>Steam32 ID: <strong class="text-white font-mono">{{ currentAccountId }}</strong></span>
              <span>核心本命位置: <strong class="text-cyan-400">{{ mainPositionText }}</strong></span>
              <span>数据来源: <strong class="text-slate-300">本地数据库真实对局</strong></span>
            </div>
          </div>
        </div>

        <!-- 3 个右侧 KPI 方块 (全部根据本地库动态算) -->
        <div class="grid grid-cols-3 gap-3 w-full lg:w-auto">
          <div class="bg-[#0b0e14]/70 p-3.5 rounded-xl border border-slate-800 text-center min-w-[110px]">
            <span class="text-[11px] text-slate-400 block mb-1">近期战力评级</span>
            <span class="text-2xl font-black font-mono text-amber-400">{{ perfGrade }}</span>
            <span class="text-[10px] text-emerald-400 block mt-0.5">综合评分 {{ calcPerfScore }}</span>
          </div>
          <div class="bg-[#0b0e14]/70 p-3.5 rounded-xl border border-slate-800 text-center min-w-[110px]">
            <span class="text-[11px] text-slate-400 block mb-1">MVP 获得率</span>
            <span class="text-2xl font-black font-mono text-cyan-400">{{ localMvpRate }}%</span>
            <span class="text-[10px] text-slate-400 block mt-0.5">局内卓越贡献</span>
          </div>
          <div class="bg-[#0b0e14]/70 p-3.5 rounded-xl border border-slate-800 text-center min-w-[110px]">
            <span class="text-[11px] text-slate-400 block mb-1">线优综合率</span>
            <span class="text-2xl font-black font-mono text-emerald-400">{{ summary?.laneWinRate || 0 }}%</span>
            <span class="text-[10px] text-slate-400 block mt-0.5">对线期统治</span>
          </div>
        </div>
      </section>

      <!-- 2. 四大核心指标卡片 -->
      <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 卡片 1: 总体战绩与胜率 (纯本地) -->
        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 shadow-lg flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-400 font-medium">总体战绩与胜率</span>
            <span class="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center text-xs">🏆</span>
          </div>
          <div class="flex items-center justify-between my-3">
            <div>
              <div class="text-3xl font-black text-white font-mono tracking-tight">{{ summary?.winRate || 0 }}%</div>
              <div class="text-xs text-slate-400 mt-1 font-mono">
                总计 {{ totalGamesInDb }} 场 
                (<span class="text-emerald-400 font-semibold">{{ summary?.totalWins || 0 }} 胜</span> / 
                <span class="text-rose-400 font-semibold">{{ totalGamesInDb - (summary?.totalWins || 0) }} 负</span>)
              </div>
            </div>
            <!-- 环形 SVG 动态反映本地胜率 -->
            <div class="relative w-14 h-14 flex items-center justify-center">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <path class="text-slate-800" stroke-width="3.5" stroke="currentColor" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
                <path class="text-emerald-400 stroke-current" stroke-width="3.5" :stroke-dasharray="`${summary?.winRate || 0}, 100`" stroke-linecap="round" fill="none" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"/>
              </svg>
            </div>
          </div>
          <div class="pt-2 border-t border-slate-800/60 flex items-center justify-between text-xs font-mono">
            <span class="text-slate-400">本地已解析局数:</span>
            <span class="text-emerald-400 font-bold">{{ totalGamesInDb }} 局</span>
          </div>
        </div>

        <!-- 卡片 2: 对线期统治力 (纯本地) -->
        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 shadow-lg flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-400 font-medium">对线期统治力</span>
            <span class="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center text-xs">⚔️</span>
          </div>
          <div class="my-2">
            <div class="flex items-baseline gap-2">
              <span class="text-3xl font-black text-emerald-400 font-mono">{{ summary?.laneWinRate || 0 }}%</span>
              <span class="text-xs text-slate-400">线优率</span>
            </div>
            <!-- 三色分段真实柱条 -->
            <div class="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden flex mt-2.5 gap-0.5">
              <div class="bg-emerald-400 h-full transition-all" :style="{ width: (summary?.laneWinRate || 0) + '%' }"></div>
              <div class="bg-amber-400 h-full transition-all" :style="{ width: Math.max(0, 100 - (summary?.laneWinRate || 0) - laneLossRate) + '%' }"></div>
              <div class="bg-rose-500 h-full transition-all" :style="{ width: laneLossRate + '%' }"></div>
            </div>
            <div class="flex justify-between items-center text-[10px] text-slate-400 mt-2 font-mono">
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>线优 {{ summary?.laneWinRate || 0 }}%</span>
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span>均势 {{ Math.max(0, 100 - (summary?.laneWinRate || 0) - laneLossRate).toFixed(1) }}%</span>
              <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>线劣 {{ laneLossRate }}%</span>
            </div>
          </div>
          <div class="pt-2 border-t border-slate-800/60 flex items-center justify-between text-xs font-mono">
            <span class="text-slate-400">对线评估标准:</span>
            <span class="text-slate-300">效率 ≥ 60% 为优</span>
          </div>
        </div>

        <!-- 卡片 3: 场均 KDA / 经济表现 (纯本地) -->
        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 shadow-lg flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-400 font-medium">场均 KDA / 经济表现</span>
            <span class="w-7 h-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center text-xs">⚡</span>
          </div>
          <div class="my-2">
            <div class="flex items-baseline gap-2">
              <span class="text-3xl font-black text-white font-mono">{{ summary?.avgKda || 0 }}</span>
              <span class="text-xs text-slate-400 font-mono">KDA</span>
            </div>
            <div class="text-xs font-mono mt-1 text-slate-400">
              <span class="text-emerald-400 font-bold">{{ summary?.avgKills || 0 }}</span> / 
              <span class="text-rose-400 font-bold">{{ summary?.avgDeaths || 0 }}</span> / 
              <span class="text-cyan-400 font-bold">{{ summary?.avgAssists || 0 }}</span>
            </div>
          </div>
          <div class="pt-2 border-t border-slate-800/60 grid grid-cols-2 text-xs font-mono">
            <div>
              <span class="text-slate-500 text-[10px] block">分均经济 (GPM)</span>
              <span class="text-amber-400 font-bold">{{ summary?.avgGpm || 0 }}</span>
            </div>
            <div class="text-right">
              <span class="text-slate-500 text-[10px] block">分均经验 (XPM)</span>
              <span class="text-slate-200 font-bold">{{ summary?.avgXpm || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 卡片 4: 阵营对局胜率分布 (纯本地数据真实渲染) -->
        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 shadow-lg flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-400 font-medium">阵营对局胜率分布</span>
            <span class="w-7 h-7 rounded-lg bg-indigo-500/10 text-indigo-400 flex items-center justify-center text-xs">⚖️</span>
          </div>
          <div class="space-y-3 my-2 text-xs font-mono">
            <div>
              <div class="flex justify-between text-slate-300 mb-1">
                <span>天辉 / 蓝方 ({{ radiantData.games }}场)</span>
                <span class="text-emerald-400 font-bold">{{ radiantData.winRate }}%</span>
              </div>
              <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                <div class="bg-emerald-400 h-full rounded-full transition-all" :style="{ width: radiantData.winRate + '%' }"></div>
              </div>
            </div>
            <div>
              <div class="flex justify-between text-slate-300 mb-1">
                <span>夜宴 / 红方 ({{ direData.games }}场)</span>
                <span class="text-rose-400 font-bold">{{ direData.winRate }}%</span>
              </div>
              <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                <div class="bg-rose-400 h-full rounded-full transition-all" :style="{ width: direData.winRate + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="pt-2 border-t border-slate-800/60 flex items-center justify-between text-[11px] text-slate-400 font-mono">
            <span>净胜场差:</span>
            <span :class="radiantData.wins >= direData.wins ? 'text-emerald-400' : 'text-rose-400'">
              {{ radiantData.wins >= direData.wins ? `天辉多赢 ${radiantData.wins - direData.wins} 场` : `夜宴多赢 ${direData.wins - radiantData.wins} 场` }}
            </span>
          </div>
        </div>
      </section>

      <!-- 3. 下半区两列面板：全本地数据计算 -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-6">

        <!-- 左列：各位置分布与胜率表现 (纯本地数据) -->
        <div class="lg:col-span-7 bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
          <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
            <div>
              <div class="flex items-center gap-2">
                <span class="text-base font-bold text-white flex items-center gap-2">
                  <span class="text-cyan-400 text-sm">⊞</span> 各位置分布与胜率表现
                </span>
                <span class="text-xs px-2 py-0.5 rounded bg-cyan-950 text-cyan-400 border border-cyan-800/50">
                  主修: {{ mainPositionText }}
                </span>
              </div>
              <p class="text-xs text-slate-400 mt-1">本地数据库 {{ totalGamesInDb }} 场对局真实分路统计</p>
            </div>
          </div>

          <!-- 位置卡片列表 -->
          <div class="space-y-4 mt-5">
            <div 
              v-for="pos in computedPositionList" 
              :key="pos.posId" 
              class="p-3.5 rounded-xl bg-[#0c1017]/80 border border-slate-800 hover:border-slate-700 transition-all"
            >
              <div class="flex flex-col sm:flex-row sm:items-center justify-between text-xs gap-2 mb-2 font-mono">
                <div class="flex items-center gap-2">
                  <span class="w-2 h-2 rounded-full" :class="pos.badgeColor"></span>
                  <span class="font-bold text-white">{{ pos.title }}</span>
                  <span v-if="pos.isMain" class="px-1.5 py-0.5 rounded text-[10px] bg-cyan-500/20 text-cyan-400">本命位置</span>
                </div>
                <div class="flex items-center gap-4 text-slate-400">
                  <span>场次: <strong class="text-white">{{ pos.games }}</strong> ({{ pos.percent }}%)</span>
                  <span>线优率: <strong class="text-emerald-400">{{ pos.laneWinRate }}%</strong></span>
                  <span>胜率: <strong :class="pos.winRate >= 50 ? 'text-emerald-400' : 'text-rose-400'">{{ pos.winRate }}%</strong></span>
                </div>
              </div>

              <!-- 进度条 -->
              <div class="grid grid-cols-2 gap-3 text-[10px] font-mono text-slate-500">
                <div>
                  <div class="flex justify-between mb-1">
                    <span>选位占比</span>
                    <span>{{ pos.percent }}%</span>
                  </div>
                  <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all" :class="pos.barColor" :style="{ width: pos.percent + '%' }"></div>
                  </div>
                </div>
                <div>
                  <div class="flex justify-between mb-1">
                    <span>胜率指标</span>
                    <span :class="pos.winRate >= 50 ? 'text-emerald-400' : 'text-rose-400'">{{ pos.winRate }}%</span>
                  </div>
                  <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
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
        <div class="lg:col-span-5 bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
              <div>
                <span class="text-base font-bold text-white flex items-center gap-2">
                  <span class="text-indigo-400 text-sm">👥</span> 排位组队胜率对比
                </span>
                <p class="text-xs text-slate-400 mt-1">单排孤勇 vs 车队开黑本地胜率对比</p>
              </div>
            </div>

            <!-- 组队各模式胜率 -->
            <div class="space-y-4 mt-5">
              <!-- 1. 单排 (Solo) -->
              <div class="p-3.5 rounded-xl bg-[#0c1017]/80 border border-slate-800">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    👤 单排 (Solo Queue)
                  </span>
                  <span class="text-slate-400">{{ soloPartyData.games }} 场 | <strong class="text-cyan-400">{{ soloPartyData.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div class="bg-cyan-500 h-full rounded-full transition-all" :style="{ width: soloPartyData.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span>单排 MVP 比例: {{ soloPartyData.mvpRate }}%</span>
                  <span>个人能力体现</span>
                </div>
              </div>

              <!-- 2. 双排 / 三排 (Duo / Trio) -->
              <div class="p-3.5 rounded-xl bg-[#0c1017]/80 border border-slate-800">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    👥 双排 / 三排 (Duo / Trio)
                  </span>
                  <span class="text-slate-400">{{ duoTrioData.games }} 场 | <strong class="text-purple-400">{{ duoTrioData.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div class="bg-purple-500 h-full rounded-full transition-all" :style="{ width: duoTrioData.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span>双三排 MVP: {{ duoTrioData.mvpRate }}%</span>
                  <span class="text-purple-400">好友配合默契</span>
                </div>
              </div>

              <!-- 3. 五排车队 (5-Stack) -->
              <div class="p-3.5 rounded-xl bg-[#0c1017]/80 border border-slate-800">
                <div class="flex justify-between items-center text-xs font-mono mb-2">
                  <span class="text-white font-bold flex items-center gap-1.5">
                    🛡️ 五排车队 (5-Stack)
                  </span>
                  <span class="text-slate-400">{{ stack5Data.games }} 场 | <strong class="text-emerald-400">{{ stack5Data.winRate }}%</strong></span>
                </div>
                <div class="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div class="bg-emerald-400 h-full rounded-full transition-all" :style="{ width: stack5Data.winRate + '%' }"></div>
                </div>
                <div class="flex justify-between text-[11px] text-slate-400 mt-2 font-mono">
                  <span class="text-emerald-400">团队终极执行力</span>
                  <span class="text-amber-400 font-semibold">{{ stack5Data.games > 0 ? '车队数据已录入' : '暂无五排记录' }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-4 pt-3 border-t border-slate-800 text-[11px] text-slate-500 font-mono text-center">
            基于本地数据库比赛 party_size 字段真实聚类
          </div>
        </div>
      </section>

      <!-- 4. 战绩流水明细 -->
      <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
          <div class="flex items-center gap-3">
            <span class="text-base font-bold text-white">本地高精度对局流水</span>
            <span class="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-400">已入库 {{ recentMatches.length }} 场</span>
          </div>
          <div class="flex items-center gap-4">
            <a 
              :href="'/player/' + currentAccountId + '/history'" 
              target="_blank" 
              class="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1 group transition-colors"
            >
              查看全部战绩
              <span class="group-hover:translate-x-0.5 transition-transform">→</span>
            </a>
            <button @click="showRecentTable = !showRecentTable" class="text-xs text-slate-400 hover:text-slate-300">
              {{ showRecentTable ? '折叠' : '展开' }}
            </button>
          </div>
        </div>

        <div v-show="showRecentTable" class="mt-4 overflow-x-auto">
          <table class="w-full text-left text-xs font-mono">
            <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800">
              <tr>
                <th class="py-2.5 px-3">对局ID</th>
                <th class="py-2.5 px-3">英雄</th>
                <th class="py-2.5 px-3">结果</th>
                <th class="py-2.5 px-3">K / D / A</th>
                <th class="py-2.5 px-3">分路/结果</th>
                <th class="py-2.5 px-3">GPM / XPM</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/60">
              <tr v-for="m in recentMatches" :key="m.matchId" class="hover:bg-[#151c2a] cursor-pointer group" @click="openMatchDetail(m.matchId)">
                <td class="py-2.5 px-3 text-slate-500 group-hover:text-cyan-400 transition-colors">{{ m.matchId }}</td>
                <td class="py-2.5 px-3 font-bold text-white flex items-center gap-2">
                  <img :src="getHeroAvatar(m.heroId)" class="w-6 h-4 rounded object-cover" @error="handleImgError" />
                  <span>{{ getHeroName(m.heroId) }}</span>
                </td>
                <td class="py-2.5 px-3">
                  <span :class="m.win ? 'text-emerald-400 bg-emerald-500/10' : 'text-rose-400 bg-rose-500/10'" class="px-2 py-0.5 rounded font-bold">
                    {{ m.win ? '胜利' : '失败' }}
                  </span>
                </td>
                <td class="py-2.5 px-3">{{ m.kills }}/{{ m.deaths }}/{{ m.assists }}</td>
                <td class="py-2.5 px-3">
                  <span :class="m.laneResult === 1 ? 'text-emerald-400' : m.laneResult === 3 ? 'text-rose-400' : 'text-amber-400'">
                    {{ m.laneResult === 1 ? '线优' : m.laneResult === 3 ? '线劣' : '均势' }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-amber-400">{{ m.goldPerMin }} / {{ m.xpPerMin }}</td>
              </tr>
              <tr v-if="recentMatches.length === 0">
                <td colspan="6" class="py-4 text-center text-slate-500">本地数据库暂无已解析比赛</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import heroConstants from '../assets/dota2_heroes_constants.json';
import { 
  syncPlayerMatches, fetchDashboardData, fetchTaskStatus,
  type PlayerSummary, type PlayerHeroStats, type RecentMatch, type TeammateSynergy 
} from '../api/player';

const props = defineProps<{
  accountId: string;
}>();

const router = useRouter();
const inputAccountId = ref(props.accountId || '182595756');
const currentAccountId = ref(props.accountId || '182595756');
const syncing = ref(false);
const showRecentTable = ref(true);

const summary = ref<PlayerSummary | null>(null);
const heroes = ref<PlayerHeroStats[]>([]);
const recentMatches = ref<RecentMatch[]>([]);
const teammates = ref<TeammateSynergy[]>([]);

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

// 2. 本地线劣率推算
const laneLossRate = computed(() => {
  if (!summary.value) return 0;
  const winR = summary.value.laneWinRate || 0;
  return Number((Math.max(0, 100 - winR) * 0.45).toFixed(1));
});

// 3. 阵营 (天辉 / 夜宴) 纯本地绑定
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
  2: { title: 'Pos 2 中单 (Mid)', badgeColor: 'bg-cyan-400', barColor: 'bg-cyan-500' },
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

const handleSearch = () => {
  if (inputAccountId.value.trim()) {
    router.push({ name: 'PlayerDashboard', params: { accountId: inputAccountId.value.trim() } });
  }
};

let pollTimer: ReturnType<typeof setInterval> | null = null;

const stopPollingTasks = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};

// 轮询后台录像解析进度，有新比赛入库时自动刷新看板数据
const startPollingTasks = (accountId: string) => {
  stopPollingTasks();
  let lastSuccessCount = -1;
  pollTimer = setInterval(async () => {
    try {
      const res = await fetchTaskStatus(accountId);
      const pending = Number(res?.data?.pending || 0);
      const success = Number(res?.data?.success || 0);
      if (lastSuccessCount !== -1 && success > lastSuccessCount) {
        await loadData();
      }
      lastSuccessCount = success;
      if (pending === 0) {
        await loadData();
        stopPollingTasks();
      }
    } catch (e) {
      stopPollingTasks();
    }
  }, 6000);
};

// 触发同步与加载
const handleStartSync = async () => {
  const accountId = inputAccountId.value.trim();
  if (!accountId) return;

  currentAccountId.value = accountId;
  syncing.value = true;
  try {
    const syncRes = await syncPlayerMatches(accountId);
    await loadData();
    if (Number(syncRes?.data?.queuedTasks || 0) > 0) {
      startPollingTasks(accountId);
    }
  } catch (err: any) {
    console.error('同步异常，继续加载本地数据库', err);
    await loadData();
  } finally {
    syncing.value = false;
  }
};

const loadData = async () => {
  if (!currentAccountId.value) return;
  try {
    const res = await fetchDashboardData(currentAccountId.value);
    if (res?.data) {
      if (res.data.summary) summary.value = res.data.summary;
      if (res.data.topHeroes) heroes.value = res.data.topHeroes;
      if (res.data.recentMatches) recentMatches.value = res.data.recentMatches;
      if (res.data.teammates) teammates.value = res.data.teammates;
      if (res.data.positionStats) rawPositions.value = res.data.positionStats;
      if (res.data.partyStats) rawParties.value = res.data.partyStats;
      if (res.data.factionStats) rawFactions.value = res.data.factionStats;
    }
  } catch (e) {
    console.error('加载本地看板数据失败', e);
  }
};

onMounted(() => {
  loadData();
});

onUnmounted(() => {
  stopPollingTasks();
});

watch(() => props.accountId, (newId) => {
  if (newId) {
    stopPollingTasks();
    currentAccountId.value = newId;
    inputAccountId.value = newId;
    loadData();
  }
});
</script>