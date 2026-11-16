<template>
  <div class="space-y-6">
    <!-- 1. 团队经济差与经验差走势图 -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 pb-4 border-b border-white/10">
        <div>
          <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-cyan-300 rounded-full shadow-[0_0_8px_#67e8f9]"></span>
            团队经济差与经验差走势 (Gold & XP Advantage)
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-300/80 mt-1">
            零轴上方 <span class="text-emerald-300 font-bold">翠绿区</span> 代表天辉领先，零轴下方 <span class="text-rose-400 font-bold">绯红区</span> 代表夜魇领先（滑动或移动鼠标查看每分钟差值）
          </p>
        </div>

        <!-- 极值统计徽章 -->
        <div class="flex flex-wrap items-center gap-2 sm:gap-3 text-[11px] sm:text-xs font-mono">
          <div class="px-2.5 sm:px-3 py-1 sm:py-1.5 rounded-xl bg-emerald-400/15 border border-emerald-400/35 text-emerald-300 backdrop-blur-md">
            天辉最大领先: <strong>+{{ maxRadiantGold.toLocaleString() }}</strong> ({{ maxRadiantMin }}:00)
          </div>
          <div class="px-2.5 sm:px-3 py-1 sm:py-1.5 rounded-xl bg-rose-500/15 border border-rose-400/35 text-rose-300 backdrop-blur-md">
            夜魇最大领先: <strong>+{{ Math.abs(minRadiantGold).toLocaleString() }}</strong> ({{ minRadiantMin }}:00)
          </div>
        </div>
      </div>

      <!-- SVG 交互式面积折线图 -->
      <div v-if="goldAdv.length > 0" class="mt-4 sm:mt-6 relative select-none">
        <!-- 悬停分钟实时指示栏 -->
        <div class="flex flex-wrap items-center justify-between gap-2 mb-3 px-3 sm:px-3.5 py-2 rounded-xl aurora-subcard text-[11px] sm:text-xs font-mono">
          <div class="flex items-center gap-2">
            <span class="text-slate-300">当前时间点:</span>
            <span class="px-2 py-0.5 rounded bg-cyan-400/20 text-cyan-200 font-bold">{{ activeHoverMin }}:00</span>
          </div>
          <div class="flex items-center gap-4 sm:gap-6">
            <div>
              <span class="text-slate-400 mr-1">经济差:</span>
              <span :class="hoverGoldVal >= 0 ? 'text-emerald-300 font-bold' : 'text-rose-400 font-bold'">
                {{ hoverGoldVal >= 0 ? `天辉 +${hoverGoldVal.toLocaleString()}` : `夜魇 +${Math.abs(hoverGoldVal).toLocaleString()}` }}
              </span>
            </div>
            <div>
              <span class="text-slate-400 mr-1">经验差:</span>
              <span :class="hoverXpVal >= 0 ? 'text-cyan-300 font-bold' : 'text-purple-300 font-bold'">
                {{ hoverXpVal >= 0 ? `天辉 +${hoverXpVal.toLocaleString()}` : `夜魇 +${Math.abs(hoverXpVal).toLocaleString()}` }}
              </span>
            </div>
          </div>
        </div>

        <div class="relative w-full h-56 sm:h-72 aurora-subcard rounded-xl p-3 sm:p-4 overflow-hidden">
          <svg
            class="w-full h-full overflow-visible touch-pan-y"
            :viewBox="`0 0 ${svgW} ${svgH}`"
            preserveAspectRatio="none"
            @mousemove="onAdvMouseMove"
            @touchstart.passive="onAdvTouchMove"
            @touchmove.passive="onAdvTouchMove"
            @mouseleave="hoverIndex = null"
          >
            <defs>
              <linearGradient id="radiantGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#10b981" stop-opacity="0.45" />
                <stop offset="100%" stop-color="#10b981" stop-opacity="0.0" />
              </linearGradient>
              <linearGradient id="direGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#f43f5e" stop-opacity="0.0" />
                <stop offset="100%" stop-color="#f43f5e" stop-opacity="0.45" />
              </linearGradient>
            </defs>

            <!-- 水平网格线 -->
            <line :x1="0" :y1="svgH * 0.15" :x2="svgW" :y2="svgH * 0.15" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
            <line :x1="0" :y1="svgH * 0.5" :x2="svgW" :y2="svgH * 0.5" stroke="#475569" stroke-width="1.5" />
            <line :x1="0" :y1="svgH * 0.85" :x2="svgW" :y2="svgH * 0.85" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />

            <!-- 天辉/夜魇分界文字 -->
            <text x="10" :y="20" fill="#10b981" font-size="11" font-family="monospace" font-weight="bold">▲ RADIANT (+{{ advMaxAbs.toLocaleString() }})</text>
            <text x="10" :y="svgH * 0.5 - 6" fill="#94a3b8" font-size="10" font-family="monospace">0 均势线</text>
            <text x="10" :y="svgH - 8" fill="#f43f5e" font-size="11" font-family="monospace" font-weight="bold">▼ DIRE (-{{ advMaxAbs.toLocaleString() }})</text>

            <!-- 经济差面积填充 -->
            <path :d="goldAreaRadiantPath" fill="url(#radiantGrad)" />
            <path :d="goldAreaDirePath" fill="url(#direGrad)" />

            <!-- 经验差虚线 -->
            <path :d="xpLinePath" fill="none" stroke="#38bdf8" stroke-width="2" stroke-dasharray="5 3" opacity="0.8" />

            <!-- 经济差主折线 -->
            <path :d="goldLinePath" fill="none" stroke="#fbbf24" stroke-width="2.5" />

            <!-- 悬停十字准线与圆点 -->
            <g v-if="activeIdx >= 0 && goldAdv.length > 1">
              <line
                :x1="getX(activeIdx, goldAdv.length)"
                :y1="0"
                :x2="getX(activeIdx, goldAdv.length)"
                :y2="svgH"
                stroke="#22d3ee"
                stroke-width="1.5"
                stroke-dasharray="3 3"
              />
              <circle
                :cx="getX(activeIdx, goldAdv.length)"
                :cy="getAdvY(goldAdv[activeIdx] || 0)"
                r="5"
                fill="#fbbf24"
                stroke="#0b0e14"
                stroke-width="2"
              />
              <circle
                v-if="xpAdv[activeIdx] !== undefined"
                :cx="getX(activeIdx, goldAdv.length)"
                :cy="getAdvY(xpAdv[activeIdx] || 0)"
                r="4"
                fill="#38bdf8"
                stroke="#0b0e14"
                stroke-width="1.5"
              />
            </g>
          </svg>
        </div>

        <!-- 图例说明 -->
        <div class="flex flex-wrap items-center justify-center gap-x-4 gap-y-1.5 sm:gap-6 mt-3 text-[11px] sm:text-xs font-mono text-slate-400">
          <span class="flex items-center gap-1.5"><span class="w-4 h-0.5 bg-amber-400 inline-block"></span>团队经济差</span>
          <span class="flex items-center gap-1.5"><span class="w-4 h-0.5 bg-sky-400 border-b border-dashed inline-block"></span>团队经验差</span>
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-sm bg-emerald-500/40 border border-emerald-400 inline-block"></span>天辉优势域</span>
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-sm bg-rose-500/40 border border-rose-400 inline-block"></span>夜魇优势域</span>
        </div>
      </div>
      <div v-else class="py-12 text-center text-slate-500 text-xs font-mono">
        该对局暂无分时团队经济/经验差曲线数据
      </div>
    </section>

    <!-- 2. 10名选手个人分时成长曲线对比 -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-3 sm:gap-4 pb-4 border-b border-white/10">
        <div>
          <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-emerald-400 rounded-full shadow-[0_0_8px_#34d399]"></span>
            选手分时个人发育曲线 (Player Telemetry Curves)
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-300/80 mt-1">
            对比 10 名选手每分钟的净资产、累计经济、累计经验与正反补走势（点击下方英雄按钮可单独开关某位选手）
          </p>
        </div>

        <!-- 指标切换与快捷过滤按钮组 -->
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex flex-wrap aurora-subcard p-1 rounded-xl text-[11px] sm:text-xs font-mono">
            <button
              v-for="m in metricOptions"
              :key="m.key"
              @click="selectedMetric = m.key"
              :class="selectedMetric === m.key ? 'bg-cyan-400/20 text-cyan-200 border border-cyan-300/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
              class="px-2 sm:px-3 py-1 rounded-lg transition-all"
            >
              {{ m.label }}
            </button>
          </div>

          <div class="flex flex-wrap aurora-subcard p-1 rounded-xl text-[11px] sm:text-xs font-mono">
            <button @click="filterPreset('all')" class="px-2 sm:px-2.5 py-1 rounded-lg text-slate-200 hover:bg-white/10">全员</button>
            <button @click="filterPreset('cores')" class="px-2 sm:px-2.5 py-1 rounded-lg text-amber-300 hover:bg-white/10">仅核心(Pos1/2)</button>
            <button @click="filterPreset('radiant')" class="px-2 sm:px-2.5 py-1 rounded-lg text-emerald-300 hover:bg-white/10">仅天辉</button>
            <button @click="filterPreset('dire')" class="px-2 sm:px-2.5 py-1 rounded-lg text-rose-400 hover:bg-white/10">仅夜魇</button>
          </div>
        </div>
      </div>

      <!-- 选手开关图例胶囊 -->
      <div class="grid grid-cols-2 sm:grid-cols-5 gap-2 mt-4">
        <button
          v-for="p in players"
          :key="p.playerSlot"
          @click="togglePlayerSlot(p.playerSlot)"
          :class="[
            hiddenSlots.has(p.playerSlot) ? 'opacity-35 aurora-subcard' : 'aurora-subcard border-white/15 shadow-sm',
            p.isRadiant ? 'hover:border-emerald-400/50' : 'hover:border-rose-400/50'
          ]"
          class="flex items-center justify-between px-2.5 py-1.5 rounded-xl border text-left transition-all"
        >
          <div class="flex items-center gap-2 min-w-0">
            <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: getPlayerColor(p.playerSlot) }"></span>
            <img :src="getHeroAvatar(p.heroId)" class="w-7 h-5 rounded object-cover shrink-0" />
            <div class="truncate">
              <div class="text-[11px] font-bold text-slate-200 truncate">{{ getHeroName(p.heroId) }}</div>
              <div class="text-[9px] text-slate-400 font-mono truncate">{{ p.personaname }}</div>
            </div>
          </div>
          <span class="text-[11px] font-mono font-bold ml-1" :style="{ color: getPlayerColor(p.playerSlot) }">
            {{ getPlayerSeriesValue(p, playerHoverIdx).toLocaleString() }}
          </span>
        </button>
      </div>

      <!-- 多人折线 SVG + 同步动态排序悬浮面板（仿赛事级分时数据浮窗） -->
      <div
        v-if="maxSeriesLength > 1"
        class="mt-4 relative w-full h-80 sm:h-96 aurora-subcard rounded-xl pt-4 pb-8 pl-11 sm:pl-14 pr-3 sm:pr-4 select-none"
      >
        <!-- 左侧 Y 轴刻度标签 -->
        <div class="absolute left-1.5 sm:left-2 top-4 bottom-8 w-8 sm:w-10 flex flex-col justify-between text-right text-[10px] sm:text-[11px] font-mono text-slate-400 pointer-events-none">
          <span>{{ formatAxisTick(maxPlayerMetricVal) }}</span>
          <span>{{ formatAxisTick(maxPlayerMetricVal * 0.75) }}</span>
          <span>{{ formatAxisTick(maxPlayerMetricVal * 0.5) }}</span>
          <span>{{ formatAxisTick(maxPlayerMetricVal * 0.25) }}</span>
          <span>0</span>
        </div>

        <!-- 底部 X 轴起止时间标签 -->
        <div class="absolute left-11 sm:left-14 right-3 sm:right-4 bottom-2 flex justify-between text-[10px] sm:text-[11px] font-mono text-slate-400 pointer-events-none">
          <span>00:00</span>
          <span>{{ String(Math.floor((maxSeriesLength - 1) / 2)).padStart(2, '0') }}:00</span>
          <span>{{ String(maxSeriesLength - 1).padStart(2, '0') }}:00</span>
        </div>

        <div class="relative w-full h-full">
          <svg
            class="w-full h-full overflow-visible cursor-crosshair touch-pan-y"
            :viewBox="`0 0 ${svgW} ${svgH}`"
            preserveAspectRatio="none"
            @mousemove="onPlayerChartMove"
            @touchstart.passive="onPlayerChartTouchMove"
            @touchmove.passive="onPlayerChartTouchMove"
            @mouseleave="playerHoverIndex = null"
          >
            <!-- 水平网格线 -->
            <line v-for="g in [0, 0.25, 0.5, 0.75, 1]" :key="g" :x1="0" :y1="getPlayerY(maxPlayerMetricVal * (1 - g))" :x2="svgW" :y2="getPlayerY(maxPlayerMetricVal * (1 - g))" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />

            <!-- 悬停指示竖线 -->
            <line
              :x1="getX(playerHoverIdx, maxSeriesLength)"
              :y1="0"
              :x2="getX(playerHoverIdx, maxSeriesLength)"
              :y2="svgH"
              stroke="#475569"
              stroke-width="1.5"
            />

            <!-- 每位选手的折线 -->
            <g v-for="p in players" :key="p.playerSlot">
              <path
                v-if="!hiddenSlots.has(p.playerSlot)"
                :d="buildPlayerLinePath(p)"
                fill="none"
                :stroke="getPlayerColor(p.playerSlot)"
                :stroke-width="String(p.accountId) === String(highlightAccountId) ? 3.2 : 2.2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </g>

            <!-- 悬停竖线与各条折线交点圆环 -->
            <g v-for="p in players" :key="'dot-' + p.playerSlot">
              <circle
                v-if="!hiddenSlots.has(p.playerSlot)"
                :cx="getX(playerHoverIdx, maxSeriesLength)"
                :cy="getPlayerY(getPlayerSeriesValue(p, playerHoverIdx))"
                r="4.5"
                fill="#0b0e14"
                :stroke="getPlayerColor(p.playerSlot)"
                stroke-width="2.5"
              />
            </g>
          </svg>

          <!-- 跟随时间轴竖线的动态从高到低排序浮窗（英雄头像 + 当前数值 + 本分钟增量，桌面端与移动端均保留） -->
          <div
            v-if="sortedHoverPlayers.length > 0"
            class="absolute top-1.5 sm:top-2 z-20 pointer-events-none bg-[#0d1119]/95 backdrop-blur-md border border-slate-700/90 rounded-xl px-2.5 sm:px-3 py-1.5 sm:py-2.5 shadow-2xl min-w-[140px] sm:min-w-[175px] transition-all duration-75"
            :style="getFloatingTooltipStyle(playerHoverIdx, maxSeriesLength)"
          >
            <div class="flex items-center gap-1.5 text-[11px] sm:text-xs font-mono font-bold text-slate-200 pb-1 sm:pb-1.5 mb-1 sm:mb-1.5 border-b border-slate-800">
              <span>🕒</span>
              <span>{{ String(playerHoverIdx).padStart(2, '0') }}:00</span>
            </div>
            <div class="space-y-0.5 sm:space-y-1 font-mono text-[11px] sm:text-xs">
              <div
                v-for="item in sortedHoverPlayers"
                :key="item.playerSlot"
                class="flex items-center justify-between gap-2 sm:gap-3"
              >
                <div class="flex items-center gap-1.5 sm:gap-2 min-w-0">
                  <img :src="getHeroAvatar(item.heroId)" class="w-5 sm:w-6 h-3.5 sm:h-4 rounded-sm object-cover border border-slate-700 shrink-0" />
                  <span
                    class="font-black text-[11px] sm:text-[12px] tracking-tight"
                    :style="{ color: item.color }"
                  >
                    {{ item.value.toLocaleString() }}
                  </span>
                </div>
                <span class="text-[10px] sm:text-[11px] text-slate-400 shrink-0">
                  ({{ item.delta >= 0 ? item.delta.toLocaleString() : item.delta }})
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { getHeroName, getHeroAvatar } from '../../utils/dotaHelper';

const props = defineProps<{
  rawJson: any;
  players: any[];
  highlightAccountId?: string;
}>();

const svgW = 900;
const svgH = 260;

// 10 名选手高辨识度电竞色板（前5天辉冷绿青色系，后5夜魇暖红橙紫系）
const playerColors = [
  '#10b981', '#06b6d4', '#3b82f6', '#84cc16', '#14b8a6',
  '#f43f5e', '#f97316', '#eab308', '#a855f7', '#ec4899'
];

// 团队经济差与经验差数组
const goldAdv = computed<number[]>(() => Array.isArray(props.rawJson?.radiant_gold_adv) ? props.rawJson.radiant_gold_adv : []);
const xpAdv = computed<number[]>(() => Array.isArray(props.rawJson?.radiant_xp_adv) ? props.rawJson.radiant_xp_adv : []);

const hoverIndex = ref<number | null>(null);
const activeIdx = computed(() => {
  if (hoverIndex.value !== null) return hoverIndex.value;
  return Math.max(0, goldAdv.value.length - 1);
});
const activeHoverMin = computed(() => activeIdx.value);
const hoverGoldVal = computed(() => goldAdv.value[activeIdx.value] || 0);
const hoverXpVal = computed(() => xpAdv.value[activeIdx.value] || 0);

// 统计天辉/夜魇最大经济领先及对应分钟数
const maxRadiantGold = computed(() => {
  let max = 0;
  goldAdv.value.forEach((v) => { if (v > max) max = v; });
  return max;
});
const maxRadiantMin = computed(() => {
  const idx = goldAdv.value.indexOf(maxRadiantGold.value);
  return idx >= 0 ? idx : 0;
});
const minRadiantGold = computed(() => {
  let min = 0;
  goldAdv.value.forEach((v) => { if (v < min) min = v; });
  return min;
});
const minRadiantMin = computed(() => {
  const idx = goldAdv.value.indexOf(minRadiantGold.value);
  return idx >= 0 ? idx : 0;
});

const advMaxAbs = computed(() => {
  let max = 5000;
  goldAdv.value.forEach((v) => { if (Math.abs(v) > max) max = Math.abs(v); });
  xpAdv.value.forEach((v) => { if (Math.abs(v) > max) max = Math.abs(v); });
  return Math.ceil(max / 1000) * 1000;
});

const getX = (idx: number, len: number) => {
  if (len <= 1) return 0;
  return Number(((idx / (len - 1)) * svgW).toFixed(1));
};

const getAdvY = (val: number) => {
  const half = svgH / 2;
  const ratio = Math.max(-1, Math.min(1, val / (advMaxAbs.value || 1)));
  return Number((half - ratio * (half - 16)).toFixed(1));
};

// 生成团队经济差折线与正负面积路径
const goldLinePath = computed(() => {
  const arr = goldAdv.value;
  if (arr.length === 0) return '';
  return arr.map((v, i) => `${i === 0 ? 'M' : 'L'} ${getX(i, arr.length)} ${getAdvY(v)}`).join(' ');
});

const xpLinePath = computed(() => {
  const arr = xpAdv.value;
  if (arr.length === 0) return '';
  return arr.map((v, i) => `${i === 0 ? 'M' : 'L'} ${getX(i, arr.length)} ${getAdvY(v)}`).join(' ');
});

const goldAreaRadiantPath = computed(() => {
  const arr = goldAdv.value;
  if (arr.length === 0) return '';
  const midY = svgH / 2;
  const pts = arr.map((v, i) => `${getX(i, arr.length)} ${getAdvY(Math.max(0, v))}`).join(' L ');
  return `M 0 ${midY} L ${pts} L ${svgW} ${midY} Z`;
});

const goldAreaDirePath = computed(() => {
  const arr = goldAdv.value;
  if (arr.length === 0) return '';
  const midY = svgH / 2;
  const pts = arr.map((v, i) => `${getX(i, arr.length)} ${getAdvY(Math.min(0, v))}`).join(' L ');
  return `M 0 ${midY} L ${pts} L ${svgW} ${midY} Z`;
});

const onAdvMouseMove = (e: MouseEvent) => {
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.clientX - rect.left) / (rect.width || 1)));
  hoverIndex.value = Math.round(relX * (goldAdv.value.length - 1));
};

// 移动端手指触摸滑动支持
const onAdvTouchMove = (e: TouchEvent) => {
  if (!e.touches || e.touches.length === 0) return;
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.touches[0].clientX - rect.left) / (rect.width || 1)));
  hoverIndex.value = Math.round(relX * (goldAdv.value.length - 1));
};

// 2. 选手个人分时成长曲线逻辑
type MetricKey = 'networth_t' | 'gold_t' | 'xp_t' | 'lh_t' | 'dn_t';
const metricOptions: Array<{ key: MetricKey; label: string }> = [
  { key: 'networth_t', label: '净资产 (Net Worth)' },
  { key: 'gold_t', label: '累计经济 (Gold)' },
  { key: 'xp_t', label: '累计经验 (XP)' },
  { key: 'lh_t', label: '正补数 (Last Hits)' },
  { key: 'dn_t', label: '反补数 (Denies)' }
];

const selectedMetric = ref<MetricKey>('networth_t');
const hiddenSlots = ref<Set<number>>(new Set());
const playerHoverIndex = ref<number | null>(null);

const togglePlayerSlot = (slot: number) => {
  const next = new Set(hiddenSlots.value);
  if (next.has(slot)) next.delete(slot);
  else next.add(slot);
  hiddenSlots.value = next;
};

const filterPreset = (mode: 'all' | 'cores' | 'radiant' | 'dire') => {
  const next = new Set<number>();
  if (mode === 'cores') {
    props.players.forEach((p) => {
      if (p.laneRole !== 1 && p.laneRole !== 2) next.add(p.playerSlot);
    });
  } else if (mode === 'radiant') {
    props.players.forEach((p) => {
      if (!p.isRadiant) next.add(p.playerSlot);
    });
  } else if (mode === 'dire') {
    props.players.forEach((p) => {
      if (p.isRadiant) next.add(p.playerSlot);
    });
  }
  hiddenSlots.value = next;
};

const getSeries = (p: any): number[] => {
  const raw = p.raw || p;
  const arr = raw[selectedMetric.value];
  if (Array.isArray(arr) && arr.length > 0) return arr;
  // 若 networth_t 不存在则回退使用 gold_t
  if (selectedMetric.value === 'networth_t' && Array.isArray(raw.gold_t)) return raw.gold_t;
  return [];
};

const maxSeriesLength = computed(() => {
  let maxLen = 0;
  props.players.forEach((p) => {
    const len = getSeries(p).length;
    if (len > maxLen) maxLen = len;
  });
  return maxLen;
});

const playerHoverIdx = computed(() => {
  if (playerHoverIndex.value !== null) return playerHoverIndex.value;
  return Math.max(0, maxSeriesLength.value - 1);
});

const maxPlayerMetricVal = computed(() => {
  let max = 100;
  props.players.forEach((p) => {
    if (hiddenSlots.value.has(p.playerSlot)) return;
    getSeries(p).forEach((v) => {
      if (v > max) max = v;
    });
  });
  return max;
});

const getPlayerSeriesValue = (p: any, idx: number): number => {
  const arr = getSeries(p);
  if (arr.length === 0) return 0;
  const safeIdx = Math.min(idx, arr.length - 1);
  return Number(arr[safeIdx] || 0);
};

// 根据选手在列表中的固定索引分配专属颜色，确保动态排序时颜色始终与折线一一对应
const getPlayerColor = (playerSlot: number): string => {
  const idx = props.players.findIndex((p) => p.playerSlot === playerSlot);
  return playerColors[(idx >= 0 ? idx : 0) % playerColors.length];
};

const getPlayerY = (val: number): number => {
  const maxVal = maxPlayerMetricVal.value || 1;
  return Number((svgH - 8 - (val / maxVal) * (svgH - 20)).toFixed(1));
};

const buildPlayerLinePath = (p: any): string => {
  const arr = getSeries(p);
  if (arr.length === 0) return '';
  return arr
    .map((v, i) => {
      const x = getX(i, maxSeriesLength.value);
      const y = getPlayerY(Number(v || 0));
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

// 当前悬停分钟下，按数值从高到低实时排序的选手列表（含本分钟增量）
const sortedHoverPlayers = computed(() => {
  const idx = playerHoverIdx.value;
  return props.players
    .filter((p) => !hiddenSlots.value.has(p.playerSlot))
    .map((p) => {
      const val = getPlayerSeriesValue(p, idx);
      const prevVal = idx > 0 ? getPlayerSeriesValue(p, idx - 1) : 0;
      return {
        playerSlot: p.playerSlot,
        heroId: p.heroId,
        personaname: p.personaname,
        color: getPlayerColor(p.playerSlot),
        value: val,
        delta: val - prevVal
      };
    })
    .sort((a, b) => b.value - a.value);
});

// Y 轴刻度格式化（如 34千 / 1.2万 或数字）
const formatAxisTick = (val: number): string => {
  const rounded = Math.round(val);
  if (rounded >= 1000) {
    return `${Math.round(rounded / 1000)}千`;
  }
  return String(rounded);
};

// 计算跟随竖线的浮窗位置（靠左时显示在竖线右侧，过半时自动切到竖线左侧防止超出边界）
const getFloatingTooltipStyle = (idx: number, totalLen: number) => {
  const ratio = totalLen > 1 ? idx / (totalLen - 1) : 0;
  const pct = Number((ratio * 100).toFixed(2));
  if (ratio > 0.5) {
    return {
      left: `calc(${pct}% - 10px)`,
      transform: 'translateX(-100%)'
    };
  }
  return {
    left: `calc(${pct}% + 10px)`
  };
};

const onPlayerChartMove = (e: MouseEvent) => {
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.clientX - rect.left) / (rect.width || 1)));
  playerHoverIndex.value = Math.round(relX * (maxSeriesLength.value - 1));
};

// 移动端手指触摸滑动支持
const onPlayerChartTouchMove = (e: TouchEvent) => {
  if (!e.touches || e.touches.length === 0) return;
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.touches[0].clientX - rect.left) / (rect.width || 1)));
  playerHoverIndex.value = Math.round(relX * (maxSeriesLength.value - 1));
};
</script>
