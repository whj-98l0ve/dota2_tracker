<template>
  <div class="text-slate-100 min-h-screen font-sans antialiased pb-16">
    <!-- 顶部极光毛玻璃导航栏 -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div
            class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 cursor-pointer shrink-0"
            @click="router.push('/')"
          >
            <div class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-base sm:text-xl font-black tracking-wide text-white cursor-pointer shrink-0" @click="router.push('/')">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden sm:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-amber-400/15 text-amber-200 font-bold border border-amber-400/35 backdrop-blur-md">
              ADMIN CONSOLE
            </span>
          </div>
        </div>

        <div class="flex items-center space-x-2 sm:space-x-3 shrink-0">
          <button
            @click="router.push('/')"
            class="px-3 sm:px-3.5 py-1.5 bg-white/[0.06] hover:bg-white/[0.12] text-slate-200 border border-white/15 hover:border-cyan-400/35 rounded-full text-xs font-medium backdrop-blur-md transition-all"
          >
            ← 返回看板
          </button>
          <button
            v-if="isLoggedIn"
            @click="handleLogout"
            class="px-3 sm:px-3.5 py-1.5 bg-rose-500/15 hover:bg-rose-500/25 text-rose-300 border border-rose-400/35 rounded-full text-xs font-semibold backdrop-blur-md transition-all"
          >
            退出
          </button>
        </div>
      </div>
    </header>

    <!-- 未登录：管理员极光毛玻璃登录卡片 -->
    <main v-if="!isLoggedIn" class="max-w-md mx-auto px-4 mt-24">
      <div class="aurora-glass rounded-3xl p-8 relative overflow-hidden">
        <div class="absolute -top-16 -right-16 w-48 h-48 bg-cyan-400/15 rounded-full blur-3xl pointer-events-none"></div>
        <div class="text-center mb-6 relative z-10">
          <div class="w-12 h-12 rounded-2xl bg-cyan-400/15 border border-cyan-300/35 text-cyan-300 flex items-center justify-center mx-auto mb-3 text-xl shadow-lg shadow-cyan-500/15">
            🔐
          </div>
          <h1 class="text-xl font-black text-white">管理员后台登录</h1>
          <p class="text-xs text-slate-400 mt-1">管理自动巡检关注的 Steam32 玩家 ID 列表</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4 relative z-10">
          <div>
            <label class="block text-xs text-slate-300 mb-1.5 font-medium">管理员用户名</label>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full aurora-subcard rounded-xl px-3.5 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-300"
              required
            />
          </div>

          <div>
            <label class="block text-xs text-slate-300 mb-1.5 font-medium">管理员密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              class="w-full aurora-subcard rounded-xl px-3.5 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-300"
              required
            />
          </div>

          <div v-if="loginError" class="text-xs text-rose-300 bg-rose-500/15 border border-rose-400/35 rounded-xl px-3 py-2">
            {{ loginError }}
          </div>

          <button
            type="submit"
            :disabled="loggingIn"
            class="w-full py-2.5 rounded-xl aurora-btn-primary font-black text-sm active:scale-[0.99] disabled:opacity-50"
          >
            {{ loggingIn ? '正在验证...' : '登录管理后台' }}
          </button>
        </form>
      </div>
    </main>

    <!-- 已登录：关注玩家管理与每小时错峰巡检控制台 -->
    <main v-else class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">
      <!-- 1. 顶部定时策略说明与新增关注栏 -->
      <section class="aurora-glass rounded-2xl p-6">
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div>
            <div class="flex items-center gap-2.5">
              <h1 class="text-xl font-black text-white">关注玩家 ID 自动巡检管理</h1>
              <span class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-400/15 text-emerald-300 border border-emerald-400/30">
                每 1 小时自动检查更新
              </span>
            </div>
            <p class="text-xs text-slate-300/80 mt-1.5 leading-relaxed">
              所有被关注的玩家 ID，后台每隔 <strong class="text-cyan-300">1 小时</strong> 自动排入错峰巡检队列，
              每隔 <strong class="text-amber-300">20 秒</strong> 逐个向 OpenDota 请求检查是否有新比赛（优先请求 <code class="text-cyan-200">matches?date=7</code>，异常自动降级 <code class="text-cyan-200">recentMatches</code>），绝不并发请求。
            </p>
          </div>

          <!-- 新增关注 ID 表单（支持同时填写备注） -->
          <div class="flex flex-wrap sm:flex-nowrap items-center gap-2.5">
            <input
              v-model="newAccountId"
              type="text"
              placeholder="输入 Steam32 ID (如 199666669)"
              class="w-full sm:w-52 aurora-subcard text-xs rounded-xl px-3.5 py-2.5 text-slate-100 placeholder-slate-500 font-mono focus:outline-none focus:border-cyan-300"
              @keydown.enter="handleAddPlayer"
            />
            <input
              v-model="newRemark"
              type="text"
              placeholder="备注名称 (选填，如: 老王)"
              class="w-full sm:w-44 aurora-subcard text-xs rounded-xl px-3.5 py-2.5 text-slate-100 placeholder-slate-500 focus:outline-none focus:border-amber-300"
              @keydown.enter="handleAddPlayer"
            />
            <button
              @click="handleAddPlayer"
              :disabled="adding || !newAccountId.trim()"
              class="px-4 py-2.5 rounded-xl aurora-btn-primary font-black text-xs whitespace-nowrap disabled:opacity-50"
            >
              {{ adding ? '正在添加并同步...' : '+ 增加关注 ID' }}
            </button>
          </div>
        </div>

        <!-- 状态提示条 -->
        <div
          v-if="statusBanner"
          class="mt-4 px-4 py-2.5 rounded-xl text-xs font-mono flex items-center justify-between border backdrop-blur-md"
          :class="statusBanner.type === 'error' ? 'bg-rose-500/15 border-rose-400/35 text-rose-200' : 'bg-cyan-400/15 border-cyan-300/35 text-cyan-200'"
        >
          <span>{{ statusBanner.msg }}</span>
          <button @click="statusBanner = null" class="text-slate-400 hover:text-white ml-4">✕</button>
        </div>
      </section>

      <!-- 2. 巡检调度监控指标卡 -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="aurora-glass rounded-2xl p-5 flex items-center justify-between">
          <div>
            <span class="text-xs text-slate-300 block">当前已关注玩家总数</span>
            <span class="text-3xl font-black text-white font-mono mt-1 block">{{ trackedPlayers.length }}</span>
            <span class="text-[11px] text-emerald-300 mt-1 block">全部纳入每小时自动检查</span>
          </div>
          <div class="w-11 h-11 rounded-xl bg-cyan-400/15 border border-cyan-300/30 text-cyan-300 flex items-center justify-center text-lg">
            👥
          </div>
        </div>

        <div class="aurora-glass rounded-2xl p-5 flex items-center justify-between">
          <div>
            <span class="text-xs text-slate-300 block">错峰队列待检查人数</span>
            <span class="text-3xl font-black text-amber-300 font-mono mt-1 block">{{ queueSize }}</span>
            <span class="text-[11px] text-slate-400 mt-1 block">每 20 秒消费 1 人，防止接口限流报错</span>
          </div>
          <div class="w-11 h-11 rounded-xl bg-amber-400/15 border border-amber-300/30 text-amber-300 flex items-center justify-center text-lg">
            ⏳
          </div>
        </div>

        <div class="aurora-glass rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-300">手动调度与刷新</span>
            <span class="text-[11px] font-mono text-slate-400">下次整点轮询: {{ formatTime(nextHourlyScheduleTime) }}</span>
          </div>
          <div class="flex items-center gap-2.5 mt-3">
            <button
              @click="handleCheckAll"
              :disabled="triggeringAll"
              class="flex-1 py-2 px-3 rounded-xl bg-emerald-400/15 hover:bg-emerald-400/25 text-emerald-300 border border-emerald-400/35 text-xs font-bold transition-all disabled:opacity-50"
            >
              {{ triggeringAll ? '排队中...' : '🔄 立即错峰巡检全部' }}
            </button>
            <button
              @click="loadTrackedPlayers"
              :disabled="loadingList"
              class="py-2 px-3 rounded-xl bg-white/[0.06] hover:bg-white/[0.14] text-slate-200 border border-white/15 text-xs font-semibold transition-all"
            >
              刷新状态
            </button>
          </div>
        </div>
      </section>

      <!-- 3. 关注玩家列表表格 -->
      <section class="aurora-glass rounded-2xl p-4 sm:p-6">
        <div class="flex items-center justify-between pb-4 border-b border-white/10">
          <div class="flex items-center gap-3">
            <h2 class="text-sm sm:text-base font-bold text-white">已关注玩家列表</h2>
            <span class="text-xs px-2.5 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-slate-300 font-mono">
              共 {{ trackedPlayers.length }} 个 ID
            </span>
          </div>
        </div>

        <!-- 移动端专属：关注玩家管理卡片列表 (md 以下显示，免横向滚动) -->
        <div class="md:hidden mt-4 space-y-3 font-mono text-xs">
          <div
            v-for="p in trackedPlayers"
            :key="'m-' + p.accountId"
            class="aurora-subcard rounded-2xl p-3.5 space-y-3"
          >
            <!-- 第一行：头像 + 玩家名 + 段位 -->
            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-2.5 min-w-0">
                <img
                  :src="p.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                  class="w-10 h-10 rounded-xl border border-white/15 object-cover shrink-0"
                />
                <div class="min-w-0">
                  <div
                    class="font-bold text-white hover:text-cyan-300 truncate cursor-pointer"
                    @click="router.push(`/player/${p.accountId}`)"
                  >
                    {{ p.personaname || `Player#${p.accountId}` }}
                  </div>
                  <div class="text-[11px] text-cyan-300 font-bold mt-0.5">ID: {{ p.accountId }}</div>
                </div>
              </div>
              <span
                :class="getRankMeta(p.rankTier, p.leaderboardRank).colorClass"
                class="px-2 py-0.5 rounded border text-[10px] font-bold shrink-0"
              >
                👑 {{ getRankMeta(p.rankTier, p.leaderboardRank).shortLabel }}
              </span>
            </div>

            <!-- 第二行：备注编辑栏 -->
            <div class="flex items-center justify-between gap-2 py-2 px-2.5 rounded-xl bg-[#060a15]/60 border border-white/[0.05]">
              <span class="text-[11px] text-slate-400 shrink-0">玩家备注:</span>
              <div v-if="editingRemarkId === p.accountId" class="flex items-center gap-1.5 flex-1 justify-end">
                <input
                  v-model="editingRemarkValue"
                  type="text"
                  placeholder="输入备注"
                  class="w-28 aurora-subcard border-amber-300/60 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                  @keydown.enter="savePlayerRemark(p.accountId)"
                  @keydown.esc="cancelEditRemark"
                />
                <button
                  @click="savePlayerRemark(p.accountId)"
                  :disabled="savingRemarkMap[p.accountId]"
                  class="px-2 py-1 rounded bg-amber-400 text-black font-black text-[10px]"
                >
                  保存
                </button>
                <button
                  @click="cancelEditRemark"
                  class="px-2 py-1 rounded bg-white/10 text-slate-200 text-[10px]"
                >
                  取消
                </button>
              </div>
              <div v-else class="flex items-center gap-2">
                <span v-if="p.remark" class="px-2 py-0.5 rounded bg-amber-400/15 text-amber-200 border border-amber-400/30 font-bold text-[11px]">
                  {{ p.remark }}
                </span>
                <span v-else class="text-slate-500 text-[11px]">未设置</span>
                <button
                  @click="startEditRemark(p)"
                  class="px-2 py-0.5 rounded bg-white/[0.06] text-slate-300 border border-white/10 text-[10px]"
                >
                  {{ p.remark ? '✏️ 修改' : '+ 备注' }}
                </button>
              </div>
            </div>

            <!-- 第三行：收录场次 / 胜率 / 状态 -->
            <div class="flex items-center justify-between text-[11px]">
              <div>
                <span class="text-white font-bold">{{ p.totalMatches || 0 }} 场</span>
                <span class="mx-1.5 text-slate-600">|</span>
                <span :class="(p.winRate || 0) >= 50 ? 'text-emerald-300' : 'text-rose-400'" class="font-bold">
                  {{ p.winRate || 0 }}% 胜率
                </span>
              </div>
              <span
                v-if="p.isTracked === 2"
                class="px-2 py-0.5 rounded bg-slate-500/20 text-slate-300 border border-slate-500/40 text-[10px] font-semibold"
              >
                😴 已休眠 (暂停监控)
              </span>
              <span
                v-else-if="queuedIds.includes(p.accountId)"
                class="px-2 py-0.5 rounded bg-amber-400/15 text-amber-300 border border-amber-400/30 text-[10px] font-semibold"
              >
                ⏳ 队列等待中
              </span>
              <span
                v-else
                class="px-2 py-0.5 rounded bg-emerald-400/15 text-emerald-300 border border-emerald-400/25 text-[10px]"
              >
                ✓ 定时监控中
              </span>
            </div>

            <!-- 第四行：同步时间与操作按钮 -->
            <div class="pt-2 border-t border-white/[0.06] flex items-center justify-between gap-2">
              <span class="text-[10px] text-slate-400 truncate">同步: {{ formatTime(p.lastSyncAt || p.updatedAt) }}</span>
              <div class="flex items-center gap-2 shrink-0">
                <button
                  @click="handleToggleSleep(p)"
                  :disabled="sleepingMap[p.accountId]"
                  :class="
                    p.isTracked === 2
                      ? 'bg-emerald-400/15 text-emerald-300 border-emerald-400/30'
                      : 'bg-amber-400/15 text-amber-300 border-amber-400/30'
                  "
                  class="px-2.5 py-1 rounded-lg border text-[11px] font-semibold disabled:opacity-50"
                >
                  {{ sleepingMap[p.accountId] ? '处理中...' : p.isTracked === 2 ? '唤醒' : '休眠' }}
                </button>
                <button
                  @click="handleSyncSingle(p.accountId)"
                  :disabled="syncingMap[p.accountId]"
                  class="px-2.5 py-1 rounded-lg bg-cyan-400/15 text-cyan-300 border border-cyan-400/30 text-[11px] font-semibold disabled:opacity-50"
                >
                  {{ syncingMap[p.accountId] ? '同步中...' : '立即检查' }}
                </button>
                <button
                  @click="handleRemovePlayer(p.accountId, p.personaname)"
                  class="px-2.5 py-1 rounded-lg bg-rose-500/15 text-rose-300 border border-rose-400/30 text-[11px] font-semibold"
                >
                  删除
                </button>
              </div>
            </div>
          </div>

          <div v-if="trackedPlayers.length === 0" class="py-10 text-center text-slate-500">
            当前暂无关注的玩家 ID，请在上方输入框添加
          </div>
        </div>

        <!-- 桌面端：完整数据表格 -->
        <div class="hidden md:block mt-4 overflow-x-auto">
          <table class="w-full text-left text-xs font-mono">
            <thead class="bg-[#060a15]/70 text-slate-400 border-b border-white/10">
              <tr>
                <th class="py-3 px-4">玩家信息</th>
                <th class="py-3 px-4">玩家备注</th>
                <th class="py-3 px-4">Steam32 ID</th>
                <th class="py-3 px-4">天梯段位 / 排名</th>
                <th class="py-3 px-4">本地收录对局 / 胜率</th>
                <th class="py-3 px-4">最近同步检查时间</th>
                <th class="py-3 px-4">巡检状态</th>
                <th class="py-3 px-4 text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/[0.06]">
              <tr
                v-for="p in trackedPlayers"
                :key="p.accountId"
                class="hover:bg-cyan-400/[0.06] transition-colors"
              >
                <td class="py-3.5 px-4">
                  <div class="flex items-center gap-3">
                    <img
                      :src="p.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                      class="w-9 h-9 rounded-lg border border-white/15 object-cover"
                    />
                    <div>
                      <div
                        class="font-bold text-white hover:text-cyan-300 cursor-pointer transition-colors"
                        @click="router.push(`/player/${p.accountId}`)"
                      >
                        {{ p.personaname || `Player#${p.accountId}` }}<span v-if="p.remark" class="text-amber-300 font-semibold ml-0.5">（{{ p.remark }}）</span>
                      </div>
                      <div class="text-[10px] text-slate-400">点击查看战绩看板</div>
                    </div>
                  </div>
                </td>
                <td class="py-3.5 px-4">
                  <div v-if="editingRemarkId === p.accountId" class="flex items-center gap-1.5">
                    <input
                      v-model="editingRemarkValue"
                      type="text"
                      placeholder="输入备注 (留空清除)"
                      class="w-32 aurora-subcard border-amber-300/60 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
                      @keydown.enter="savePlayerRemark(p.accountId)"
                      @keydown.esc="cancelEditRemark"
                    />
                    <button
                      @click="savePlayerRemark(p.accountId)"
                      :disabled="savingRemarkMap[p.accountId]"
                      class="px-2 py-1 rounded bg-amber-400 hover:bg-amber-300 text-black font-black text-[11px] transition-colors disabled:opacity-50"
                    >
                      保存
                    </button>
                    <button
                      @click="cancelEditRemark"
                      class="px-2 py-1 rounded bg-white/10 hover:bg-white/20 text-slate-200 text-[11px] transition-colors"
                    >
                      取消
                    </button>
                  </div>
                  <div v-else class="flex items-center gap-2">
                    <span v-if="p.remark" class="px-2 py-0.5 rounded bg-amber-400/15 text-amber-200 border border-amber-400/30 font-bold text-[11px]">
                      {{ p.remark }}
                    </span>
                    <span v-else class="text-slate-500 text-[11px]">未设置</span>
                    <button
                      @click="startEditRemark(p)"
                      class="px-2 py-0.5 rounded bg-white/[0.06] hover:bg-white/[0.14] text-slate-300 border border-white/10 text-[10px] transition-colors"
                    >
                      {{ p.remark ? '✏️ 修改' : '+ 备注' }}
                    </button>
                  </div>
                </td>
                <td class="py-3.5 px-4 text-cyan-300 font-bold">
                  {{ p.accountId }}
                </td>
                <td class="py-3.5 px-4">
                  <span
                    :class="getRankMeta(p.rankTier, p.leaderboardRank).colorClass"
                    class="px-2 py-0.5 rounded border text-[11px] font-bold"
                  >
                    👑 {{ getRankMeta(p.rankTier, p.leaderboardRank).shortLabel }}
                  </span>
                </td>
                <td class="py-3.5 px-4">
                  <span class="text-white font-bold">{{ p.totalMatches || 0 }} 场</span>
                  <span class="mx-1.5 text-slate-600">|</span>
                  <span :class="(p.winRate || 0) >= 50 ? 'text-emerald-300' : 'text-rose-400'" class="font-bold">
                    {{ p.winRate || 0 }}% 胜率
                  </span>
                </td>
                <td class="py-3.5 px-4 text-slate-400">
                  {{ formatTime(p.lastSyncAt || p.updatedAt) }}
                </td>
                <td class="py-3.5 px-4">
                  <span
                    v-if="p.isTracked === 2"
                    class="px-2 py-0.5 rounded bg-slate-500/20 text-slate-300 border border-slate-500/40 text-[10px] font-semibold"
                  >
                    😴 已休眠 (暂停监控)
                  </span>
                  <span
                    v-else-if="queuedIds.includes(p.accountId)"
                    class="px-2 py-0.5 rounded bg-amber-400/15 text-amber-300 border border-amber-400/30 text-[10px] font-semibold"
                  >
                    ⏳ 错峰队列等待中
                  </span>
                  <span
                    v-else
                    class="px-2 py-0.5 rounded bg-emerald-400/15 text-emerald-300 border border-emerald-400/25 text-[10px]"
                  >
                    ✓ 定时监控中
                  </span>
                </td>
                <td class="py-3.5 px-4 text-right space-x-2 whitespace-nowrap">
                  <button
                    @click="handleToggleSleep(p)"
                    :disabled="sleepingMap[p.accountId]"
                    :class="
                      p.isTracked === 2
                        ? 'bg-emerald-400/15 hover:bg-emerald-400/25 text-emerald-300 border-emerald-400/30'
                        : 'bg-amber-400/15 hover:bg-amber-400/25 text-amber-300 border-amber-400/30'
                    "
                    class="px-2.5 py-1 rounded-lg border text-[11px] font-semibold transition-all disabled:opacity-50"
                  >
                    {{ sleepingMap[p.accountId] ? '处理中...' : p.isTracked === 2 ? '☀️ 唤醒监控' : '😴 设为休眠' }}
                  </button>
                  <button
                    @click="handleSyncSingle(p.accountId)"
                    :disabled="syncingMap[p.accountId]"
                    class="px-2.5 py-1 rounded-lg bg-cyan-400/15 hover:bg-cyan-400/25 text-cyan-300 border border-cyan-400/30 text-[11px] font-semibold transition-all disabled:opacity-50"
                  >
                    {{ syncingMap[p.accountId] ? '同步中...' : '立即检查' }}
                  </button>
                  <button
                    @click="handleRemovePlayer(p.accountId, p.personaname)"
                    class="px-2.5 py-1 rounded-lg bg-rose-500/15 hover:bg-rose-500/25 text-rose-300 border border-rose-400/30 text-[11px] font-semibold transition-all"
                  >
                    删除关注
                  </button>
                </td>
              </tr>
              <tr v-if="trackedPlayers.length === 0">
                <td colspan="8" class="py-10 text-center text-slate-500">
                  当前暂无关注的玩家 ID，请在上方输入框添加
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 4. DeepSeek AI 提示词实时配置中心 -->
      <section class="aurora-glass rounded-2xl p-5 sm:p-6 space-y-5">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-white/10">
          <div>
            <div class="flex items-center gap-2.5">
              <span class="text-xl">🤖</span>
              <h2 class="text-sm sm:text-base font-black text-white">DeepSeek AI 提示词实时配置</h2>
              <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-400/15 text-cyan-300 border border-cyan-400/30">
                保存后立即生效 · 无需重启
              </span>
            </div>
            <p class="text-xs text-slate-300/80 mt-1.5 leading-relaxed">
              支持使用动态阵营占位符：
              <code class="px-1.5 py-0.5 rounded bg-white/10 text-amber-300 font-mono text-[11px]" v-text="'{{winningTeam}}'"></code>（胜方阵营）与
              <code class="px-1.5 py-0.5 rounded bg-white/10 text-rose-300 font-mono text-[11px]" v-text="'{{losingTeam}}'"></code>（败方阵营），系统会自动替换为“天辉”或“夜魇”。
            </p>
          </div>

          <div class="flex items-center gap-2.5 shrink-0">
            <button
              @click="handleResetPrompts"
              :disabled="savingPrompts || loadingPrompts"
              class="px-3.5 py-2 rounded-xl bg-white/[0.06] hover:bg-white/[0.12] border border-white/15 text-xs font-bold text-slate-200 transition-all disabled:opacity-50"
            >
              ↺ 恢复默认
            </button>
            <button
              @click="handleSavePrompts"
              :disabled="savingPrompts || loadingPrompts"
              class="px-4 py-2 rounded-xl aurora-btn-primary text-xs font-black transition-all disabled:opacity-50"
            >
              {{ savingPrompts ? '保存中...' : '💾 保存提示词配置' }}
            </button>
          </div>
        </div>

        <div
          v-if="promptBanner"
          class="px-4 py-2.5 rounded-xl text-xs font-mono flex items-center justify-between border backdrop-blur-md"
          :class="promptBanner.type === 'error' ? 'bg-rose-500/15 border-rose-400/35 text-rose-200' : 'bg-emerald-400/15 border-emerald-400/35 text-emerald-200'"
        >
          <span>{{ promptBanner.msg }}</span>
          <button @click="promptBanner = null" class="text-slate-400 hover:text-white ml-4">✕</button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <!-- 到底谁的问题？ 提示词 -->
          <div class="aurora-subcard rounded-2xl p-4 space-y-2.5 border border-rose-500/20">
            <div class="flex items-center justify-between">
              <label class="text-xs sm:text-sm font-black text-rose-300 flex items-center gap-1.5">
                <span>🦹</span>
                <span>到底谁的问题？（败方背锅提示词）</span>
              </label>
              <span class="text-[10px] font-mono text-slate-400">{{ blamePrompt.length }} 字</span>
            </div>
            <textarea
              v-model="blamePrompt"
              rows="14"
              placeholder="输入“到底谁的问题？”系统提示词..."
              class="w-full rounded-xl bg-[#060a15]/80 border border-white/10 focus:border-rose-400/60 p-3 text-xs text-slate-100 leading-relaxed font-mono focus:outline-none resize-y"
            ></textarea>
          </div>

          <!-- 谁是天降猛男！ 提示词 -->
          <div class="aurora-subcard rounded-2xl p-4 space-y-2.5 border border-amber-500/20">
            <div class="flex items-center justify-between">
              <label class="text-xs sm:text-sm font-black text-amber-300 flex items-center gap-1.5">
                <span>💪</span>
                <span>谁是天降猛男！（胜方功臣提示词）</span>
              </label>
              <span class="text-[10px] font-mono text-slate-400">{{ mvpPrompt.length }} 字</span>
            </div>
            <textarea
              v-model="mvpPrompt"
              rows="14"
              placeholder="输入“谁是天降猛男！”系统提示词..."
              class="w-full rounded-xl bg-[#060a15]/80 border border-white/10 focus:border-amber-400/60 p-3 text-xs text-slate-100 leading-relaxed font-mono focus:outline-none resize-y"
            ></textarea>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  adminLogin,
  fetchTrackedPlayers,
  addTrackedPlayer,
  updateTrackedPlayerRemark,
  toggleTrackedPlayerSleep,
  removeTrackedPlayer,
  triggerStaggeredCheckAll,
  syncPlayerMatches,
  fetchAiPrompts,
  saveAiPrompts,
  resetAiPrompts
} from '../api/player';
import { getRankMeta } from '../utils/dotaHelper';

const router = useRouter();
const SESSION_KEY = 'dota2_tracker_admin_auth';

const isLoggedIn = ref(sessionStorage.getItem(SESSION_KEY) === 'true');
const loggingIn = ref(false);
const loginError = ref('');
const loginForm = ref({
  username: '',
  password: ''
});

const trackedPlayers = ref<any[]>([]);
const queueSize = ref(0);
const queuedIds = ref<number[]>([]);
const nextHourlyScheduleTime = ref<string | null>(null);
const loadingList = ref(false);

const newAccountId = ref('');
const newRemark = ref('');
const adding = ref(false);
const triggeringAll = ref(false);
const syncingMap = ref<Record<number, boolean>>({});
const sleepingMap = ref<Record<number, boolean>>({});
const editingRemarkId = ref<number | null>(null);
const editingRemarkValue = ref('');
const savingRemarkMap = ref<Record<number, boolean>>({});
const statusBanner = ref<{ type: 'info' | 'error'; msg: string } | null>(null);

// AI 提示词配置状态
const blamePrompt = ref('');
const mvpPrompt = ref('');
const loadingPrompts = ref(false);
const savingPrompts = ref(false);
const promptBanner = ref<{ type: 'info' | 'error'; msg: string } | null>(null);

let refreshTimer: ReturnType<typeof setInterval> | null = null;

// 1. 管理员登录校验
const handleLogin = async () => {
  loginError.value = '';
  loggingIn.value = true;
  try {
    const res = await adminLogin(loginForm.value.username.trim(), loginForm.value.password);
    if (res?.data?.success) {
      isLoggedIn.value = true;
      sessionStorage.setItem(SESSION_KEY, 'true');
      await Promise.all([loadTrackedPlayers(), loadAiPrompts()]);
      startAutoRefresh();
    } else {
      loginError.value = res?.data?.msg || '用户名或密码错误';
    }
  } catch (e: any) {
    loginError.value = '登录请求失败，请检查后端服务是否已启动';
  } finally {
    loggingIn.value = false;
  }
};

const handleLogout = () => {
  sessionStorage.removeItem(SESSION_KEY);
  isLoggedIn.value = false;
  stopAutoRefresh();
};

// 2. 加载关注玩家列表与队列状态
const loadTrackedPlayers = async () => {
  if (!isLoggedIn.value) return;
  loadingList.value = true;
  try {
    const res = await fetchTrackedPlayers();
    if (res?.data) {
      trackedPlayers.value = res.data.players || [];
      queueSize.value = Number(res.data.queueSize || 0);
      queuedIds.value = res.data.queuedIds || [];
      nextHourlyScheduleTime.value = res.data.nextHourlyScheduleTime || null;
    }
  } catch (e) {
    console.error('获取关注玩家列表失败', e);
  } finally {
    loadingList.value = false;
  }
};

// 3. 新增关注玩家 ID（支持同时保存备注）
const handleAddPlayer = async () => {
  const idStr = newAccountId.value.trim();
  const remarkStr = newRemark.value.trim();
  if (!/^\d+$/.test(idStr)) {
    statusBanner.value = { type: 'error', msg: '请输入纯数字 Steam32 玩家 ID（例如 199666669）' };
    return;
  }
  adding.value = true;
  statusBanner.value = null;
  try {
    const res = await addTrackedPlayer(idStr, remarkStr || undefined);
    newAccountId.value = '';
    newRemark.value = '';
    const queued = res?.data?.queuedTasks ?? 0;
    const found = res?.data?.totalFound ?? 0;
    statusBanner.value = {
      type: 'info',
      msg: `已成功添加关注 ID [${idStr}]${remarkStr ? `（备注: ${remarkStr}）` : ''}！检索到近期比赛 ${found} 场，新增解析入队 ${queued} 场。`
    };
    await loadTrackedPlayers();
  } catch (e: any) {
    statusBanner.value = { type: 'error', msg: `添加关注 ID [${idStr}] 失败，请稍后重试` };
  } finally {
    adding.value = false;
  }
};

// 3.1 编辑与保存玩家备注
const startEditRemark = (p: any) => {
  editingRemarkId.value = p.accountId;
  editingRemarkValue.value = p.remark || '';
};

const cancelEditRemark = () => {
  editingRemarkId.value = null;
  editingRemarkValue.value = '';
};

const savePlayerRemark = async (accountId: number) => {
  savingRemarkMap.value[accountId] = true;
  try {
    const val = editingRemarkValue.value.trim();
    await updateTrackedPlayerRemark(accountId, val);
    editingRemarkId.value = null;
    editingRemarkValue.value = '';
    statusBanner.value = {
      type: 'info',
      msg: val ? `已保存玩家 [${accountId}] 的备注为「${val}」` : `已清空玩家 [${accountId}] 的备注`
    };
    await loadTrackedPlayers();
  } catch (e) {
    statusBanner.value = { type: 'error', msg: `保存玩家 [${accountId}] 备注失败` };
  } finally {
    savingRemarkMap.value[accountId] = false;
  }
};

// 3.5 切换账号休眠 / 唤醒监控状态（保留历史数据，仅暂停/恢复定时巡检）
const handleToggleSleep = async (p: any) => {
  const toSleep = p.isTracked !== 2;
  sleepingMap.value[p.accountId] = true;
  try {
    const res = await toggleTrackedPlayerSleep(p.accountId, toSleep);
    statusBanner.value = {
      type: 'info',
      msg: res?.data?.msg || (toSleep ? `账号 [${p.accountId}] 已设为休眠` : `账号 [${p.accountId}] 已唤醒`)
    };
    await loadTrackedPlayers();
  } catch (e) {
    statusBanner.value = { type: 'error', msg: `更新账号 [${p.accountId}] 休眠状态失败` };
  } finally {
    sleepingMap.value[p.accountId] = false;
  }
};

// 4. 删除关注玩家 ID
const handleRemovePlayer = async (accountId: number, name?: string) => {
  if (!confirm(`确定要取消关注玩家 ${name || accountId} (${accountId}) 吗？取消后将不再每小时自动同步其新比赛。`)) {
    return;
  }
  try {
    await removeTrackedPlayer(accountId);
    statusBanner.value = { type: 'info', msg: `已取消关注玩家 ${name || ''} [${accountId}]` };
    await loadTrackedPlayers();
  } catch (e) {
    statusBanner.value = { type: 'error', msg: `删除关注玩家 [${accountId}] 失败` };
  }
};

// 5. 手动检查单个玩家
const handleSyncSingle = async (accountId: number) => {
  syncingMap.value[accountId] = true;
  try {
    const res = await syncPlayerMatches(String(accountId));
    const found = res?.data?.totalFound ?? 0;
    const queued = res?.data?.queuedTasks ?? 0;
    statusBanner.value = {
      type: 'info',
      msg: `玩家 [${accountId}] 检查完成：发现近期比赛 ${found} 场，新入队待解析 ${queued} 场。`
    };
    await loadTrackedPlayers();
  } catch (e) {
    statusBanner.value = { type: 'error', msg: `检查玩家 [${accountId}] 失败` };
  } finally {
    syncingMap.value[accountId] = false;
  }
};

// 6. 将所有关注玩家加入错峰队列
const handleCheckAll = async () => {
  triggeringAll.value = true;
  try {
    const res = await triggerStaggeredCheckAll();
    statusBanner.value = {
      type: 'info',
      msg: res?.data?.msg || '已排入错峰巡检队列'
    };
    await loadTrackedPlayers();
  } catch (e) {
    statusBanner.value = { type: 'error', msg: '触发错峰巡检失败' };
  } finally {
    triggeringAll.value = false;
  }
};

const formatTime = (timeStr?: string | null) => {
  if (!timeStr) return '暂无记录';
  const d = new Date(timeStr);
  if (isNaN(d.getTime())) return String(timeStr).replace('T', ' ').slice(0, 19);
  return d.toLocaleString('zh-CN', { hour12: false });
};

// 7. 加载、保存与恢复默认 AI 提示词配置
const loadAiPrompts = async () => {
  if (!isLoggedIn.value) return;
  loadingPrompts.value = true;
  try {
    const res = await fetchAiPrompts();
    if (res?.data?.success) {
      blamePrompt.value = res.data.blamePrompt || '';
      mvpPrompt.value = res.data.mvpPrompt || '';
    }
  } catch (e) {
    console.error('获取 AI 提示词配置失败', e);
  } finally {
    loadingPrompts.value = false;
  }
};

const handleSavePrompts = async () => {
  if (!blamePrompt.value.trim() || !mvpPrompt.value.trim()) {
    promptBanner.value = { type: 'error', msg: '两个提示词内容均不能为空' };
    return;
  }
  savingPrompts.value = true;
  promptBanner.value = null;
  try {
    const res = await saveAiPrompts(blamePrompt.value, mvpPrompt.value);
    if (res?.data?.success) {
      blamePrompt.value = res.data.blamePrompt || blamePrompt.value;
      mvpPrompt.value = res.data.mvpPrompt || mvpPrompt.value;
      promptBanner.value = { type: 'info', msg: res.data.msg || 'AI 提示词配置已保存并立即生效' };
    } else {
      promptBanner.value = { type: 'error', msg: res?.data?.msg || '保存 AI 提示词失败' };
    }
  } catch (e) {
    promptBanner.value = { type: 'error', msg: '保存 AI 提示词请求失败，请检查后端服务' };
  } finally {
    savingPrompts.value = false;
  }
};

const handleResetPrompts = async () => {
  if (!confirm('确定要恢复系统默认的两个 AI 提示词吗？当前自定义修改将被覆盖。')) {
    return;
  }
  savingPrompts.value = true;
  promptBanner.value = null;
  try {
    const res = await resetAiPrompts();
    if (res?.data?.success) {
      blamePrompt.value = res.data.blamePrompt || '';
      mvpPrompt.value = res.data.mvpPrompt || '';
      promptBanner.value = { type: 'info', msg: res.data.msg || '已恢复系统默认 AI 提示词' };
    } else {
      promptBanner.value = { type: 'error', msg: '恢复默认提示词失败' };
    }
  } catch (e) {
    promptBanner.value = { type: 'error', msg: '恢复默认提示词请求失败' };
  } finally {
    savingPrompts.value = false;
  }
};

const startAutoRefresh = () => {
  stopAutoRefresh();
  refreshTimer = setInterval(() => {
    if (isLoggedIn.value) {
      loadTrackedPlayers();
    }
  }, 10000);
};

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
};

onMounted(() => {
  if (isLoggedIn.value) {
    loadTrackedPlayers();
    loadAiPrompts();
    startAutoRefresh();
  }
});

onUnmounted(() => {
  stopAutoRefresh();
});
</script>
