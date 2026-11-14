<template>
  <div class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-16 selection:bg-cyan-500 selection:text-white">
    <!-- 顶部导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div
            class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20 cursor-pointer"
            @click="router.push('/')"
          >
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white cursor-pointer" @click="router.push('/')">
              毒瘤榜<span class="text-cyan-400">.GG</span>
            </span>
            <span class="text-[10px] uppercase tracking-wider px-2 py-0.5 rounded bg-amber-950/80 text-amber-400 font-bold border border-amber-700/50">
              ADMIN CONSOLE
            </span>
          </div>
        </div>

        <div class="flex items-center space-x-3">
          <button
            @click="router.push('/')"
            class="px-3 py-1.5 bg-[#121721] hover:bg-slate-800 text-slate-300 border border-slate-700/80 rounded-lg text-xs transition-colors"
          >
            ← 返回战绩看板
          </button>
          <button
            v-if="isLoggedIn"
            @click="handleLogout"
            class="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-lg text-xs font-semibold transition-colors"
          >
            退出管理员
          </button>
        </div>
      </div>
    </header>

    <!-- 未登录：管理员登录卡片 -->
    <main v-if="!isLoggedIn" class="max-w-md mx-auto px-4 mt-24">
      <div class="bg-[#121721] border border-slate-800/90 rounded-2xl p-8 shadow-2xl shadow-black/60">
        <div class="text-center mb-6">
          <div class="w-12 h-12 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 flex items-center justify-center mx-auto mb-3 text-xl">
            🔐
          </div>
          <h1 class="text-xl font-black text-white">管理员后台登录</h1>
          <p class="text-xs text-slate-400 mt-1">管理自动巡检关注的 Steam32 玩家 ID 列表</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label class="block text-xs text-slate-400 mb-1.5 font-medium">管理员用户名</label>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              class="w-full bg-[#0b0e14] border border-slate-700/80 rounded-xl px-3.5 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              required
            />
          </div>

          <div>
            <label class="block text-xs text-slate-400 mb-1.5 font-medium">管理员密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              class="w-full bg-[#0b0e14] border border-slate-700/80 rounded-xl px-3.5 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500"
              required
            />
          </div>

          <div v-if="loginError" class="text-xs text-rose-400 bg-rose-500/10 border border-rose-500/30 rounded-lg px-3 py-2">
            {{ loginError }}
          </div>

          <button
            type="submit"
            :disabled="loggingIn"
            class="w-full py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 to-emerald-500 text-black font-black text-sm hover:opacity-95 active:scale-[0.99] transition-all disabled:opacity-50"
          >
            {{ loggingIn ? '正在验证...' : '登录管理后台' }}
          </button>
        </form>
      </div>
    </main>

    <!-- 已登录：关注玩家管理与每小时错峰巡检控制台 -->
    <main v-else class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">
      <!-- 1. 顶部定时策略说明与新增关注栏 -->
      <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div>
            <div class="flex items-center gap-2.5">
              <h1 class="text-xl font-black text-white">关注玩家 ID 自动巡检管理</h1>
              <span class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                每 1 小时自动检查更新
              </span>
            </div>
            <p class="text-xs text-slate-400 mt-1.5 leading-relaxed">
              所有被关注的玩家 ID，后台每隔 <strong class="text-cyan-400">1 小时</strong> 自动排入错峰巡检队列，
              每隔 <strong class="text-amber-400">20 秒</strong> 逐个向 OpenDota 请求检查是否有新比赛（优先请求 <code class="text-slate-300">matches?date=7</code>，异常自动降级 <code class="text-slate-300">recentMatches</code>），绝不并发请求。
            </p>
          </div>

          <!-- 新增关注 ID 表单（支持同时填写备注） -->
          <div class="flex flex-wrap sm:flex-nowrap items-center gap-2.5">
            <input
              v-model="newAccountId"
              type="text"
              placeholder="输入 Steam32 ID (如 199666669)"
              class="w-full sm:w-52 bg-[#0b0e14] border border-slate-700/80 text-xs rounded-xl px-3.5 py-2.5 text-slate-200 placeholder-slate-500 font-mono focus:outline-none focus:border-cyan-500"
              @keydown.enter="handleAddPlayer"
            />
            <input
              v-model="newRemark"
              type="text"
              placeholder="备注名称 (选填，如: 老王)"
              class="w-full sm:w-44 bg-[#0b0e14] border border-slate-700/80 text-xs rounded-xl px-3.5 py-2.5 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-400"
              @keydown.enter="handleAddPlayer"
            />
            <button
              @click="handleAddPlayer"
              :disabled="adding || !newAccountId.trim()"
              class="px-4 py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-black font-black text-xs whitespace-nowrap transition-all disabled:opacity-50"
            >
              {{ adding ? '正在添加并同步...' : '+ 增加关注 ID' }}
            </button>
          </div>
        </div>

        <!-- 状态提示条 -->
        <div
          v-if="statusBanner"
          class="mt-4 px-4 py-2.5 rounded-xl text-xs font-mono flex items-center justify-between border"
          :class="statusBanner.type === 'error' ? 'bg-rose-500/10 border-rose-500/30 text-rose-300' : 'bg-cyan-500/10 border-cyan-500/30 text-cyan-300'"
        >
          <span>{{ statusBanner.msg }}</span>
          <button @click="statusBanner = null" class="text-slate-400 hover:text-white ml-4">✕</button>
        </div>
      </section>

      <!-- 2. 巡检调度监控指标卡 -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 flex items-center justify-between">
          <div>
            <span class="text-xs text-slate-400 block">当前已关注玩家总数</span>
            <span class="text-3xl font-black text-white font-mono mt-1 block">{{ trackedPlayers.length }}</span>
            <span class="text-[11px] text-emerald-400 mt-1 block">全部纳入每小时自动检查</span>
          </div>
          <div class="w-11 h-11 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 flex items-center justify-center text-lg">
            👥
          </div>
        </div>

        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 flex items-center justify-between">
          <div>
            <span class="text-xs text-slate-400 block">错峰队列待检查人数</span>
            <span class="text-3xl font-black text-amber-400 font-mono mt-1 block">{{ queueSize }}</span>
            <span class="text-[11px] text-slate-400 mt-1 block">每 20 秒消费 1 人，防止接口限流报错</span>
          </div>
          <div class="w-11 h-11 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400 flex items-center justify-center text-lg">
            ⏳
          </div>
        </div>

        <div class="bg-[#121721] border border-slate-800/80 rounded-2xl p-5 flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-400">手动调度与刷新</span>
            <span class="text-[11px] font-mono text-slate-400">下次整点轮询: {{ formatTime(nextHourlyScheduleTime) }}</span>
          </div>
          <div class="flex items-center gap-2.5 mt-3">
            <button
              @click="handleCheckAll"
              :disabled="triggeringAll"
              class="flex-1 py-2 px-3 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 text-emerald-400 border border-emerald-500/30 text-xs font-bold transition-all disabled:opacity-50"
            >
              {{ triggeringAll ? '排队中...' : '🔄 立即错峰巡检全部' }}
            </button>
            <button
              @click="loadTrackedPlayers"
              :disabled="loadingList"
              class="py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-semibold transition-all"
            >
              刷新状态
            </button>
          </div>
        </div>
      </section>

      <!-- 3. 关注玩家列表表格 -->
      <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800/80">
          <div class="flex items-center gap-3">
            <h2 class="text-base font-bold text-white">已关注玩家列表</h2>
            <span class="text-xs px-2 py-0.5 rounded bg-slate-800 text-slate-400 font-mono">
              共 {{ trackedPlayers.length }} 个 ID
            </span>
          </div>
        </div>

        <div class="mt-4 overflow-x-auto">
          <table class="w-full text-left text-xs font-mono">
            <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800">
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
            <tbody class="divide-y divide-slate-800/60">
              <tr
                v-for="p in trackedPlayers"
                :key="p.accountId"
                class="hover:bg-[#151c2a] transition-colors"
              >
                <td class="py-3.5 px-4">
                  <div class="flex items-center gap-3">
                    <img
                      :src="p.avatar || 'https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=80'"
                      class="w-9 h-9 rounded-lg border border-slate-700 object-cover"
                    />
                    <div>
                      <div
                        class="font-bold text-white hover:text-cyan-400 cursor-pointer transition-colors"
                        @click="router.push(`/player/${p.accountId}`)"
                      >
                        {{ p.personaname || `Player#${p.accountId}` }}<span v-if="p.remark" class="text-amber-400 font-semibold ml-0.5">（{{ p.remark }}）</span>
                      </div>
                      <div class="text-[10px] text-slate-500">点击查看战绩看板</div>
                    </div>
                  </div>
                </td>
                <td class="py-3.5 px-4">
                  <div v-if="editingRemarkId === p.accountId" class="flex items-center gap-1.5">
                    <input
                      v-model="editingRemarkValue"
                      type="text"
                      placeholder="输入备注 (留空清除)"
                      class="w-32 bg-[#0b0e14] border border-amber-400/60 rounded-lg px-2 py-1 text-xs text-white focus:outline-none"
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
                      class="px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] transition-colors"
                    >
                      取消
                    </button>
                  </div>
                  <div v-else class="flex items-center gap-2">
                    <span v-if="p.remark" class="px-2 py-0.5 rounded bg-amber-500/15 text-amber-300 border border-amber-500/30 font-bold text-[11px]">
                      {{ p.remark }}
                    </span>
                    <span v-else class="text-slate-600 text-[11px]">未设置</span>
                    <button
                      @click="startEditRemark(p)"
                      class="px-2 py-0.5 rounded bg-slate-800/80 hover:bg-slate-700 text-slate-300 border border-slate-700 text-[10px] transition-colors"
                    >
                      {{ p.remark ? '✏️ 修改' : '+ 备注' }}
                    </button>
                  </div>
                </td>
                <td class="py-3.5 px-4 text-cyan-400 font-bold">
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
                  <span :class="(p.winRate || 0) >= 50 ? 'text-emerald-400' : 'text-rose-400'" class="font-bold">
                    {{ p.winRate || 0 }}% 胜率
                  </span>
                </td>
                <td class="py-3.5 px-4 text-slate-400">
                  {{ formatTime(p.lastSyncAt || p.updatedAt) }}
                </td>
                <td class="py-3.5 px-4">
                  <span
                    v-if="queuedIds.includes(p.accountId)"
                    class="px-2 py-0.5 rounded bg-amber-500/15 text-amber-400 border border-amber-500/30 text-[10px] font-semibold"
                  >
                    ⏳ 错峰队列等待中
                  </span>
                  <span
                    v-else
                    class="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px]"
                  >
                    ✓ 定时监控中
                  </span>
                </td>
                <td class="py-3.5 px-4 text-right space-x-2">
                  <button
                    @click="handleSyncSingle(p.accountId)"
                    :disabled="syncingMap[p.accountId]"
                    class="px-2.5 py-1 rounded-lg bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 text-[11px] font-semibold transition-all disabled:opacity-50"
                  >
                    {{ syncingMap[p.accountId] ? '同步中...' : '立即检查' }}
                  </button>
                  <button
                    @click="handleRemovePlayer(p.accountId, p.personaname)"
                    class="px-2.5 py-1 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 text-[11px] font-semibold transition-all"
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
  removeTrackedPlayer,
  triggerStaggeredCheckAll,
  syncPlayerMatches
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
const editingRemarkId = ref<number | null>(null);
const editingRemarkValue = ref('');
const savingRemarkMap = ref<Record<number, boolean>>({});
const statusBanner = ref<{ type: 'info' | 'error'; msg: string } | null>(null);

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
      await loadTrackedPlayers();
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
    startAutoRefresh();
  }
});

onUnmounted(() => {
  stopAutoRefresh();
});
</script>
