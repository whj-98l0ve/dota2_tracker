<template>
  <div class="space-y-6">
    <!-- 1. 团队经济差与经验差走势图 -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800/80">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-cyan-400 rounded-full"></span>
            团队经济差与经验差走势 (Gold & XP Advantage)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            零轴上方 <span class="text-emerald-400 font-bold">翠绿区</span> 代表天辉领先，零轴下方 <span class="text-rose-400 font-bold">绯红区</span> 代表夜魇领先（移动鼠标查看每分钟精确差值）
          </p>
        </div>

        <!-- 极值统计徽章 -->
        <div class="flex flex-wrap items-center gap-3 text-xs font-mono">
          <div class="px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            天辉最大经济领先: <strong>+{{ maxRadiantGold.toLocaleString() }}</strong> ({{ maxRadiantMin }}:00)
          </div>
          <div class="px-3 py-1.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400">
            夜魇最大经济领先: <strong>+{{ Math.abs(minRadiantGold).toLocaleString() }}</strong> ({{ minRadiantMin }}:00)
          </div>
        </div>
      </div>

      <!-- SVG 交互式面积折线图 -->
      <div v-if="goldAdv.length > 0" class="mt-6 relative select-none">
        <!-- 悬停分钟实时指示栏 -->
        <div class="flex flex-wrap items-center justify-between mb-3 px-3 py-2 rounded-xl bg-[#0b0e14] border border-slate-800 text-xs font-mono">
          <div class="flex items-center gap-2">
            <span class="text-slate-400">当前时间点:</span>
            <span class="px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-300 font-bold">{{ activeHoverMin }}:00</span>
          </div>
          <div class="flex items-center gap-6">
            <div>
              <span class="text-slate-400 mr-1.5">经济差 (Gold):</span>
              <span :class="hoverGoldVal >= 0 ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'">
                {{ hoverGoldVal >= 0 ? `天辉 +${hoverGoldVal.toLocaleString()}` : `夜魇 +${Math.abs(hoverGoldVal).toLocaleString()}` }}
              </span>
            </div>
            <div>
              <span class="text-slate-400 mr-1.5">经验差 (XP):</span>
              <span :class="hoverXpVal >= 0 ? 'text-cyan-400 font-bold' : 'text-purple-400 font-bold'">
                {{ hoverXpVal >= 0 ? `天辉 +${hoverXpVal.toLocaleString()}` : `夜魇 +${Math.abs(hoverXpVal).toLocaleString()}` }}
              </span>
            </div>
          </div>
        </div>

        <div class="relative w-full h-72 bg-[#0b0e14]/90 rounded-xl border border-slate-800/80 p-4 overflow-hidden">
          <svg
            class="w-full h-full overflow-visible"
            :viewBox="`0 0 ${svgW} ${svgH}`"
            preserveAspectRatio="none"
            @mousemove="onAdvMouseMove"
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
        <div class="flex items-center justify-center gap-6 mt-3 text-xs font-mono text-slate-400">
          <span class="flex items-center gap-2"><span class="w-4 h-0.5 bg-amber-400 inline-block"></span>团队经济差 (Gold Adv)</span>
          <span class="flex items-center gap-2"><span class="w-4 h-0.5 bg-sky-400 border-b border-dashed inline-block"></span>团队经验差 (XP Adv)</span>
          <span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-sm bg-emerald-500/40 border border-emerald-400 inline-block"></span>天辉优势域</span>
          <span class="flex items-center gap-2"><span class="w-2.5 h-2.5 rounded-sm bg-rose-500/40 border border-rose-400 inline-block"></span>夜魇优势域</span>
        </div>
      </div>
      <div v-else class="py-12 text-center text-slate-500 text-xs font-mono">
        该对局暂无分时团队经济/经验差曲线数据
      </div>
    </section>

    <!-- 2. 10名选手个人分时成长曲线对比 -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-4 border-b border-slate-800/80">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-emerald-400 rounded-full"></span>
            选手分时个人发育曲线 (Player Telemetry Curves)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            对比 10 名选手每分钟的净资产、累计经济、累计经验与正反补走势（点击下方英雄按钮可单独开关某位选手）
          </p>
        </div>

        <!-- 指标切换与快捷过滤按钮组 -->
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex bg-[#0b0e14] p-1 rounded-xl border border-slate-800 text-xs font-mono">
            <button
              v-for="m in metricOptions"
              :key="m.key"
              @click="selectedMetric = m.key"
              :class="selectedMetric === m.key ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
              class="px-3 py-1 rounded-lg transition-all"
            >
              {{ m.label }}
            </button>
          </div>

          <div class="flex bg-[#0b0e14] p-1 rounded-xl border border-slate-800 text-xs font-mono">
            <button @click="filterPreset('all')" class="px-2.5 py-1 rounded-lg text-slate-300 hover:bg-slate-800">全员</button>
            <button @click="filterPreset('cores')" class="px-2.5 py-1 rounded-lg text-amber-400 hover:bg-slate-800">仅核心(Pos1/2)</button>
            <button @click="filterPreset('radiant')" class="px-2.5 py-1 rounded-lg text-emerald-400 hover:bg-slate-800">仅天辉</button>
            <button @click="filterPreset('dire')" class="px-2.5 py-1 rounded-lg text-rose-400 hover:bg-slate-800">仅夜魇</button>
          </div>
        </div>
      </div>

      <!-- 选手开关图例胶囊 -->
      <div class="grid grid-cols-2 sm:grid-cols-5 gap-2 mt-4">
        <button
          v-for="(p, idx) in players"
          :key="p.playerSlot"
          @click="togglePlayerSlot(p.playerSlot)"
          :class="[
            hiddenSlots.has(p.playerSlot) ? 'opacity-35 bg-[#0b0e14] border-slate-800' : 'bg-[#0b0e14] border-slate-700 shadow-sm',
            p.isRadiant ? 'hover:border-emerald-500/50' : 'hover:border-rose-500/50'
          ]"
          class="flex items-center justify-between px-2.5 py-1.5 rounded-xl border text-left transition-all"
        >
          <div class="flex items-center gap-2 min-w-0">
            <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: playerColors[idx % playerColors.length] }"></span>
            <img :src="getHeroAvatar(p.heroId)" class="w-7 h-5 rounded object-cover shrink-0" />
            <div class="truncate">
              <div class="text-[11px] font-bold text-slate-200 truncate">{{ getHeroName(p.heroId) }}</div>
              <div class="text-[9px] text-slate-500 font-mono truncate">{{ p.personaname }}</div>
            </div>
          </div>
          <span class="text-[11px] font-mono font-bold text-amber-400 ml-1">
            {{ getPlayerSeriesValue(p, playerHoverIdx).toLocaleString() }}
          </span>
        </button>
      </div>

      <!-- 多人折线 SVG -->
      <div v-if="maxSeriesLength > 1" class="mt-4 relative w-full h-80 bg-[#0b0e14]/90 rounded-xl border border-slate-800/80 p-4">
        <svg
          class="w-full h-full overflow-visible"
          :viewBox="`0 0 ${svgW} ${svgH}`"
          preserveAspectRatio="none"
          @mousemove="onPlayerChartMove"
          @mouseleave="playerHoverIndex = null"
        >
          <!-- 网格线 -->
          <line v-for="g in [0.2, 0.4, 0.6, 0.8]" :key="g" :x1="0" :y1="svgH * g" :x2="svgW" :y2="svgH * g" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />
          <text x="8" y="16" fill="#64748b" font-size="10" font-family="monospace">MAX: {{ maxPlayerMetricVal.toLocaleString() }}</text>
          <text x="8" :y="svgH - 8" fill="#64748b" font-size="10" font-family="monospace">0 ({{ playerHoverIdx }}:00)</text>

          <!-- 每位选手的折线 -->
          <g v-for="(p, idx) in players" :key="p.playerSlot">
            <path
              v-if="!hiddenSlots.has(p.playerSlot)"
              :d="buildPlayerLinePath(p)"
              fill="none"
              :stroke="playerColors[idx % playerColors.length]"
              :stroke-width="String(p.accountId) === String(highlightAccountId) ? 3.2 : 2"
              stroke-linecap="round"
            />
          </g>

          <!-- 悬停指示竖线 -->
          <line
            :x1="getX(playerHoverIdx, maxSeriesLength)"
            :y1="0"
            :x2="getX(playerHoverIdx, maxSeriesLength)"
            :y2="svgH"
            stroke="#38bdf8"
            stroke-width="1"
            stroke-dasharray="3 3"
          />
        </svg>
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
  return arr[safeIdx] || 0;
};

const buildPlayerLinePath = (p: any): string => {
  const arr = getSeries(p);
  if (arr.length === 0) return '';
  const maxVal = maxPlayerMetricVal.value || 1;
  return arr
    .map((v, i) => {
      const x = getX(i, maxSeriesLength.value);
      const y = Number((svgH - 12 - (v / maxVal) * (svgH - 28)).toFixed(1));
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

const onPlayerChartMove = (e: MouseEvent) => {
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.clientX - rect.left) / (rect.width || 1)));
  playerHoverIndex.value = Math.round(relX * (maxSeriesLength.value - 1));
};
</script>
