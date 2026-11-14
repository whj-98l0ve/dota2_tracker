<template>
  <div class="space-y-6">
    <!-- 1. 5v5 英雄对位击杀矩阵 (Kill Grid Matrix) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80">
        <h3 class="text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-rose-500 rounded-full"></span>
          5v5 英雄对位击杀矩阵 (Head-to-Head Kill Matrix)
        </h3>
        <p class="text-xs text-slate-400 mt-1">
          左侧行代表 <span class="text-emerald-400 font-bold">天辉英雄</span>，顶部列代表 <span class="text-rose-400 font-bold">夜魇英雄</span>。每个单元格显示 <span class="text-emerald-400">天辉击杀夜魇</span> : <span class="text-rose-400">夜魇击杀天辉</span> 的正面对决战果
        </p>
      </div>

      <div class="mt-5 overflow-x-auto">
        <table class="w-full text-xs font-mono border-collapse">
          <thead>
            <tr class="bg-[#0b0e14] border-b border-slate-800">
              <th class="p-3 text-left text-slate-400">天辉 ＼ 夜魇</th>
              <th v-for="dp in direPlayers" :key="dp.playerSlot" class="p-3 text-center">
                <div class="flex flex-col items-center gap-1">
                  <img :src="getHeroAvatar(dp.heroId)" class="w-11 h-7 rounded border border-rose-500/40 object-cover" />
                  <span class="text-[11px] text-rose-400 font-bold">{{ getHeroName(dp.heroId) }}</span>
                  <span class="text-[9px] text-slate-500 truncate max-w-[85px]">{{ dp.personaname }}</span>
                </div>
              </th>
              <th class="p-3 text-center text-emerald-400">天辉总击杀</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/50">
            <tr v-for="rp in radiantPlayers" :key="rp.playerSlot" class="hover:bg-slate-800/20">
              <td class="p-3 bg-[#0b0e14]/60 border-r border-slate-800">
                <div class="flex items-center gap-2.5">
                  <img :src="getHeroAvatar(rp.heroId)" class="w-11 h-7 rounded border border-emerald-500/40 object-cover" />
                  <div>
                    <div class="text-[11px] font-bold text-emerald-400">{{ getHeroName(rp.heroId) }}</div>
                    <div class="text-[9px] text-slate-500 truncate max-w-[100px]">{{ rp.personaname }}</div>
                  </div>
                </div>
              </td>

              <td
                v-for="dp in direPlayers"
                :key="dp.playerSlot"
                class="p-3 text-center border-r border-slate-800/40"
              >
                <div
                  :class="getCellBgClass(getHeroKillCount(rp, dp.heroId), getHeroKillCount(dp, rp.heroId))"
                  class="inline-flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-lg border min-w-[74px]"
                >
                  <span class="text-emerald-400 font-black text-sm">{{ getHeroKillCount(rp, dp.heroId) }}</span>
                  <span class="text-slate-600">:</span>
                  <span class="text-rose-400 font-black text-sm">{{ getHeroKillCount(dp, rp.heroId) }}</span>
                </div>
              </td>

              <td class="p-3 text-center font-black text-sm text-emerald-400 bg-emerald-500/5">
                {{ rp.kills }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 2. 10名选手每分钟累计造成伤害曲线与统计表 (类似于选手分时个人发育曲线) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-4 border-b border-slate-800/80">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-amber-400 rounded-full"></span>
            选手分时累计造成伤害曲线 (Per-Minute Cumulative Damage Telemetry)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            统计 10 名选手每分钟总计造成的英雄伤害、建筑伤害及英雄+建筑总伤害走势（移动鼠标查看每分钟精确累计伤害，点击下方英雄按钮可单独开关选手）
          </p>
        </div>

        <!-- 伤害指标切换与快捷过滤按钮组 -->
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex bg-[#0b0e14] p-1 rounded-xl border border-slate-800 text-xs font-mono">
            <button
              v-for="m in dmgMetricOptions"
              :key="m.key"
              @click="selectedDmgMetric = m.key"
              :class="selectedDmgMetric === m.key ? 'bg-rose-500/20 text-rose-300 border border-rose-500/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
              class="px-3 py-1 rounded-lg transition-all"
            >
              {{ m.label }}
            </button>
          </div>

          <div class="flex bg-[#0b0e14] p-1 rounded-xl border border-slate-800 text-xs font-mono">
            <button @click="filterDmgPreset('all')" class="px-2.5 py-1 rounded-lg text-slate-300 hover:bg-slate-800">全员</button>
            <button @click="filterDmgPreset('cores')" class="px-2.5 py-1 rounded-lg text-amber-400 hover:bg-slate-800">仅核心(Pos1/2)</button>
            <button @click="filterDmgPreset('radiant')" class="px-2.5 py-1 rounded-lg text-emerald-400 hover:bg-slate-800">仅天辉</button>
            <button @click="filterDmgPreset('dire')" class="px-2.5 py-1 rounded-lg text-rose-400 hover:bg-slate-800">仅夜魇</button>
          </div>
        </div>
      </div>

      <!-- 悬停分钟实时团队总伤害对比指示栏 -->
      <div v-if="maxDmgSeriesLength > 1" class="flex flex-wrap items-center justify-between mt-4 px-3 py-2 rounded-xl bg-[#0b0e14] border border-slate-800 text-xs font-mono">
        <div class="flex items-center gap-2">
          <span class="text-slate-400">当前统计时间点:</span>
          <span class="px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 font-bold">{{ dmgHoverIdx }}:00</span>
          <span class="text-[11px] text-slate-500">(鼠标在下方图表移动可查看任意分钟每个人总计造成的伤害)</span>
        </div>
        <div class="flex items-center gap-5">
          <div>
            <span class="text-slate-400 mr-1.5">天辉累计总伤:</span>
            <strong class="text-emerald-400">{{ hoverRadiantTotalDmg.toLocaleString() }}</strong>
          </div>
          <div>
            <span class="text-slate-400 mr-1.5">夜魇累计总伤:</span>
            <strong class="text-rose-400">{{ hoverDireTotalDmg.toLocaleString() }}</strong>
          </div>
          <div>
            <span class="text-slate-400 mr-1.5">团队伤害差:</span>
            <span :class="hoverRadiantTotalDmg >= hoverDireTotalDmg ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'">
              {{
                hoverRadiantTotalDmg >= hoverDireTotalDmg
                  ? `天辉 +${(hoverRadiantTotalDmg - hoverDireTotalDmg).toLocaleString()}`
                  : `夜魇 +${(hoverDireTotalDmg - hoverRadiantTotalDmg).toLocaleString()}`
              }}
            </span>
          </div>
        </div>
      </div>

      <!-- 10名选手开关图例胶囊（实时显示当前悬停分钟每个人的累计伤害） -->
      <div class="grid grid-cols-2 sm:grid-cols-5 gap-2 mt-3">
        <button
          v-for="p in players"
          :key="p.playerSlot"
          @click="toggleDmgPlayerSlot(p.playerSlot)"
          :class="[
            hiddenDmgSlots.has(p.playerSlot) ? 'opacity-35 bg-[#0b0e14] border-slate-800' : 'bg-[#0b0e14] border-slate-700 shadow-sm',
            p.isRadiant ? 'hover:border-emerald-500/50' : 'hover:border-rose-500/50'
          ]"
          class="flex items-center justify-between px-2.5 py-1.5 rounded-xl border text-left transition-all"
        >
          <div class="flex items-center gap-2 min-w-0">
            <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: getPlayerColor(p.playerSlot) }"></span>
            <img :src="getHeroAvatar(p.heroId)" class="w-7 h-5 rounded object-cover shrink-0" />
            <div class="truncate">
              <div class="text-[11px] font-bold text-slate-200 truncate">{{ getHeroName(p.heroId) }}</div>
              <div class="text-[9px] text-slate-500 font-mono truncate">{{ p.personaname }}</div>
            </div>
          </div>
          <span class="text-[11px] font-mono font-bold ml-1" :style="{ color: getPlayerColor(p.playerSlot) }">
            {{ getPlayerDmgValue(p, dmgHoverIdx).toLocaleString() }}
          </span>
        </button>
      </div>

      <!-- 多人分时累计伤害折线 SVG + 同步动态从高到低排序悬浮面板 -->
      <div
        v-if="maxDmgSeriesLength > 1"
        class="mt-4 relative w-full h-96 bg-[#0b0e14]/95 rounded-xl border border-slate-800/80 pt-4 pb-8 pl-14 pr-4 select-none"
      >
        <!-- 左侧 Y 轴刻度标签 -->
        <div class="absolute left-2 top-4 bottom-8 w-10 flex flex-col justify-between text-right text-[11px] font-mono text-slate-400 pointer-events-none">
          <span>{{ formatAxisTick(maxPlayerDmgVal) }}</span>
          <span>{{ formatAxisTick(maxPlayerDmgVal * 0.75) }}</span>
          <span>{{ formatAxisTick(maxPlayerDmgVal * 0.5) }}</span>
          <span>{{ formatAxisTick(maxPlayerDmgVal * 0.25) }}</span>
          <span>0</span>
        </div>

        <!-- 底部 X 轴起止时间标签 -->
        <div class="absolute left-14 right-4 bottom-2 flex justify-between text-[11px] font-mono text-slate-400 pointer-events-none">
          <span>00:00</span>
          <span>{{ String(Math.floor((maxDmgSeriesLength - 1) / 2)).padStart(2, '0') }}:00</span>
          <span>{{ String(maxDmgSeriesLength - 1).padStart(2, '0') }}:00</span>
        </div>

        <div class="relative w-full h-full">
          <svg
            class="w-full h-full overflow-visible cursor-crosshair"
            :viewBox="`0 0 ${svgW} ${svgH}`"
            preserveAspectRatio="none"
            @mousemove="onDmgChartMove"
            @mouseleave="dmgHoverIndex = null"
          >
            <!-- 水平网格线 -->
            <line v-for="g in [0, 0.25, 0.5, 0.75, 1]" :key="g" :x1="0" :y1="getDmgY(maxPlayerDmgVal * (1 - g))" :x2="svgW" :y2="getDmgY(maxPlayerDmgVal * (1 - g))" stroke="#1e293b" stroke-dasharray="4 4" stroke-width="1" />

            <!-- 悬停指示竖线 -->
            <line
              :x1="getX(dmgHoverIdx, maxDmgSeriesLength)"
              :y1="0"
              :x2="getX(dmgHoverIdx, maxDmgSeriesLength)"
              :y2="svgH"
              stroke="#475569"
              stroke-width="1.5"
            />

            <!-- 每位选手的累计伤害折线 -->
            <g v-for="p in players" :key="p.playerSlot">
              <path
                v-if="!hiddenDmgSlots.has(p.playerSlot)"
                :d="buildPlayerDmgLinePath(p)"
                fill="none"
                :stroke="getPlayerColor(p.playerSlot)"
                :stroke-width="String(p.accountId) === String(highlightAccountId) ? 3.2 : 2.2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </g>

            <!-- 当前悬停分钟的圆环节点 -->
            <g v-for="p in players" :key="'dmg-dot-' + p.playerSlot">
              <circle
                v-if="!hiddenDmgSlots.has(p.playerSlot)"
                :cx="getX(dmgHoverIdx, maxDmgSeriesLength)"
                :cy="getDmgY(getPlayerDmgValue(p, dmgHoverIdx))"
                r="4.5"
                fill="#0b0e14"
                :stroke="getPlayerColor(p.playerSlot)"
                stroke-width="2.5"
              />
            </g>
          </svg>

          <!-- 跟随时间轴竖线的动态从高到低排序浮窗（英雄头像 + 当前累计伤害 + 本分钟增量） -->
          <div
            v-if="sortedHoverDmgPlayers.length > 0"
            class="absolute top-2 z-20 pointer-events-none bg-[#0d1119]/95 backdrop-blur-md border border-slate-700/90 rounded-xl px-3 py-2.5 shadow-2xl min-w-[175px] transition-all duration-75"
            :style="getFloatingTooltipStyle(dmgHoverIdx, maxDmgSeriesLength)"
          >
            <div class="flex items-center gap-1.5 text-xs font-mono font-bold text-slate-200 pb-1.5 mb-1.5 border-b border-slate-800">
              <span>🕒</span>
              <span>{{ String(dmgHoverIdx).padStart(2, '0') }}:00</span>
            </div>
            <div class="space-y-1 font-mono text-xs">
              <div
                v-for="item in sortedHoverDmgPlayers"
                :key="item.playerSlot"
                class="flex items-center justify-between gap-3"
              >
                <div class="flex items-center gap-2 min-w-0">
                  <img :src="getHeroAvatar(item.heroId)" class="w-6 h-4 rounded-sm object-cover border border-slate-700 shrink-0" />
                  <span
                    class="font-black text-[12px] tracking-tight"
                    :style="{ color: item.color }"
                  >
                    {{ item.value.toLocaleString() }}
                  </span>
                </div>
                <span class="text-[11px] text-slate-400 shrink-0">
                  ({{ item.delta >= 0 ? item.delta.toLocaleString() : item.delta }})
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分时节点每个人的累计造成伤害统计明细表（按当前时间点伤害从高到低同步排序） -->
      <div v-if="maxDmgSeriesLength > 1" class="mt-5 overflow-x-auto">
        <table class="w-full text-left text-xs font-mono border-collapse">
          <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 text-[10px]">
            <tr>
              <th class="py-2.5 px-3">当前排名 / 英雄 / 选手</th>
              <th class="py-2.5 px-3 text-right">10:00 累计伤害</th>
              <th class="py-2.5 px-3 text-right">20:00 累计伤害</th>
              <th class="py-2.5 px-3 text-right">30:00 累计伤害</th>
              <th class="py-2.5 px-3 text-right text-cyan-300 bg-cyan-500/5">当前时间点 ({{ dmgHoverIdx }}:00) ↓</th>
              <th class="py-2.5 px-3 text-right text-rose-400">终局总计伤害</th>
              <th class="py-2.5 px-3 text-right">分均造成伤害 (DPM)</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/50">
            <tr
              v-for="(p, rankIdx) in sortedTablePlayers"
              :key="'dmg-row-' + p.playerSlot"
              :class="p.isRadiant ? 'hover:bg-emerald-500/5' : 'hover:bg-rose-500/5'"
            >
              <td class="py-2.5 px-3">
                <div class="flex items-center gap-2">
                  <span class="text-[10px] text-slate-500 w-4 font-bold">#{{ rankIdx + 1 }}</span>
                  <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: getPlayerColor(p.playerSlot) }"></span>
                  <img :src="getHeroAvatar(p.heroId)" class="w-8 h-5 rounded object-cover shrink-0" />
                  <span :class="p.isRadiant ? 'text-emerald-400' : 'text-rose-400'" class="font-bold text-[11px]">{{ getHeroName(p.heroId) }}</span>
                  <span class="text-[10px] text-slate-500 truncate max-w-[110px]">{{ p.personaname }}</span>
                </div>
              </td>
              <td class="py-2.5 px-3 text-right text-slate-300">
                {{ maxDmgSeriesLength > 10 ? getPlayerDmgValue(p, 10).toLocaleString() : '-' }}
              </td>
              <td class="py-2.5 px-3 text-right text-slate-300">
                {{ maxDmgSeriesLength > 20 ? getPlayerDmgValue(p, 20).toLocaleString() : '-' }}
              </td>
              <td class="py-2.5 px-3 text-right text-slate-300">
                {{ maxDmgSeriesLength > 30 ? getPlayerDmgValue(p, 30).toLocaleString() : '-' }}
              </td>
              <td class="py-2.5 px-3 text-right font-bold text-cyan-300 bg-cyan-500/5">
                {{ getPlayerDmgValue(p, dmgHoverIdx).toLocaleString() }}
              </td>
              <td class="py-2.5 px-3 text-right font-black text-rose-400">
                {{ getPlayerDmgValue(p, maxDmgSeriesLength - 1).toLocaleString() }}
              </td>
              <td class="py-2.5 px-3 text-right text-amber-400 font-bold">
                {{ Math.round(getPlayerDmgValue(p, maxDmgSeriesLength - 1) / Math.max(1, maxDmgSeriesLength - 1)).toLocaleString() }} /分
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="py-10 text-center text-slate-500 text-xs font-mono">
        该对局暂无分时每分钟伤害遥测曲线数据
      </div>
    </section>

    <!-- 3. 选手技能与物品伤害构成拆解 (Damage Inflictor Breakdown) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80">
        <h3 class="text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-cyan-400 rounded-full"></span>
          选手伤害来源与承受伤害深度拆解 (Damage Inflictor & Tanking Telemetry)
        </h3>
        <p class="text-xs text-slate-400 mt-1">
          精确统计每位选手由物理普攻、各主动/被动技能、攻击特效物品造成的伤害构成，以及承受敌方英雄的伤害分布
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mt-5">
        <div
          v-for="p in players"
          :key="p.playerSlot"
          :class="p.isRadiant ? 'border-l-4 border-l-emerald-500' : 'border-l-4 border-l-rose-500'"
          class="bg-[#0b0e14]/85 rounded-xl border border-slate-800 p-4 flex flex-col justify-between"
        >
          <!-- 顶部选手汇总 -->
          <div>
            <div class="flex items-center justify-between pb-3 mb-3 border-b border-slate-800/80">
              <div class="flex items-center gap-3">
                <img :src="getHeroAvatar(p.heroId)" class="w-11 h-7 rounded border border-slate-700 object-cover" />
                <div>
                  <div class="text-sm font-black text-white">{{ getHeroName(p.heroId) }}</div>
                  <div class="text-[10px] text-slate-400 font-mono">{{ p.personaname }}</div>
                </div>
              </div>
              <div class="text-right font-mono text-xs">
                <div>总输出: <strong class="text-rose-400">{{ (p.heroDamage || 0).toLocaleString() }}</strong></div>
                <div class="text-[10px] text-slate-400">英雄承伤: <strong class="text-sky-400">{{ p.heroDamageTaken.toLocaleString() }}</strong></div>
              </div>
            </div>

            <!-- 造成伤害前 6 技能/物品明细 -->
            <div class="text-[11px] font-mono text-slate-400 mb-2 font-bold">⚔️ 伤害来源构成 (Top Sources):</div>
            <div class="space-y-2">
              <div
                v-for="src in getDamageSources(p)"
                :key="src.key"
                class="flex items-center gap-2.5 text-xs font-mono"
              >
                <div class="w-7 h-7 rounded bg-[#121721] border border-slate-700 flex items-center justify-center overflow-hidden shrink-0">
                  <img v-if="src.img" :src="src.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                  <span v-else class="text-xs">🗡️</span>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex justify-between items-center text-[11px] mb-0.5">
                    <span class="text-slate-200 truncate font-semibold">
                      {{ src.name }}
                      <span v-if="src.uses > 0" class="text-[10px] text-cyan-400 ml-1">({{ src.uses }}次)</span>
                    </span>
                    <span class="text-slate-300 shrink-0">
                      <strong class="text-white">{{ src.dmg.toLocaleString() }}</strong>
                      <span class="text-slate-500 text-[10px] ml-1">({{ src.pct }}%)</span>
                    </span>
                  </div>
                  <div class="w-full bg-slate-800/80 h-1.5 rounded-full overflow-hidden">
                    <div
                      :class="src.isBasicAttack ? 'bg-amber-400' : src.isItem ? 'bg-purple-400' : 'bg-cyan-400'"
                      class="h-full rounded-full"
                      :style="{ width: src.pct + '%' }"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部：承受来自哪些敌方英雄的伤害最多 -->
          <div v-if="getTopDamageTakenFromHeroes(p).length > 0" class="mt-4 pt-3 border-t border-slate-800/70">
            <div class="text-[10px] font-mono text-slate-500 mb-1.5">🛡️ 主要承受伤害来源敌方英雄:</div>
            <div class="flex flex-wrap gap-2">
              <div
                v-for="dt in getTopDamageTakenFromHeroes(p)"
                :key="dt.npcName"
                class="flex items-center gap-1.5 px-2 py-1 rounded-lg bg-[#121721] border border-slate-800 text-[11px] font-mono"
              >
                <img v-if="dt.img" :src="dt.img" class="w-6 h-4 rounded object-cover" />
                <span class="text-slate-300">{{ dt.heroName }}</span>
                <span class="text-rose-400 font-bold">{{ dt.dmg.toLocaleString() }}</span>
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
import heroConstants from '../../assets/dota2_heroes_constants.json';
import {
  getHeroName,
  getHeroAvatar,
  getHeroByNpcName,
  getAbilityMeta,
  getItemMeta
} from '../../utils/dotaHelper';

const props = defineProps<{
  players: any[];
  highlightAccountId?: string;
}>();

const heroMap = heroConstants as Record<string, any>;

const radiantPlayers = computed(() => props.players.filter((p) => p.isRadiant));
const direPlayers = computed(() => props.players.filter((p) => !p.isRadiant));

// ================= 选手每分钟累计造成伤害曲线逻辑 =================
const svgW = 900;
const svgH = 260;

// 10 名选手高辨识度电竞色板（前5天辉冷绿青色系，后5夜魇暖红橙紫系）
const playerColors = [
  '#10b981', '#06b6d4', '#3b82f6', '#84cc16', '#14b8a6',
  '#f43f5e', '#f97316', '#eab308', '#a855f7', '#ec4899'
];

type DmgMetricKey = 'hero_damage_t' | 'total_damage_t' | 'tower_damage_t' | 'hero_healing_t';
const dmgMetricOptions: Array<{ key: DmgMetricKey; label: string }> = [
  { key: 'hero_damage_t', label: '累计英雄伤害 (Hero Damage)' },
  { key: 'total_damage_t', label: '累计总伤害 (英雄+建筑)' },
  { key: 'tower_damage_t', label: '累计建筑伤害 (Tower Damage)' },
  { key: 'hero_healing_t', label: '累计英雄治疗 (Hero Healing)' }
];

const selectedDmgMetric = ref<DmgMetricKey>('hero_damage_t');
const hiddenDmgSlots = ref<Set<number>>(new Set());
const dmgHoverIndex = ref<number | null>(null);

const toggleDmgPlayerSlot = (slot: number) => {
  const next = new Set(hiddenDmgSlots.value);
  if (next.has(slot)) next.delete(slot);
  else next.add(slot);
  hiddenDmgSlots.value = next;
};

const filterDmgPreset = (mode: 'all' | 'cores' | 'radiant' | 'dire') => {
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
  hiddenDmgSlots.value = next;
};

const getDmgSeries = (p: any): number[] => {
  const raw = p.raw || p;
  if (selectedDmgMetric.value === 'total_damage_t') {
    const hArr: number[] = Array.isArray(raw.hero_damage_t) ? raw.hero_damage_t : [];
    const tArr: number[] = Array.isArray(raw.tower_damage_t) ? raw.tower_damage_t : [];
    const len = Math.max(hArr.length, tArr.length);
    if (len === 0) return [];
    const sumArr: number[] = [];
    for (let i = 0; i < len; i++) {
      sumArr.push(Number(hArr[i] || 0) + Number(tArr[i] || 0));
    }
    return sumArr;
  }
  const arr = raw[selectedDmgMetric.value];
  return Array.isArray(arr) ? arr : [];
};

const maxDmgSeriesLength = computed(() => {
  let maxLen = 0;
  props.players.forEach((p) => {
    const len = getDmgSeries(p).length;
    if (len > maxLen) maxLen = len;
  });
  return maxLen;
});

const dmgHoverIdx = computed(() => {
  if (dmgHoverIndex.value !== null) return dmgHoverIndex.value;
  return Math.max(0, maxDmgSeriesLength.value - 1);
});

const maxPlayerDmgVal = computed(() => {
  let max = 100;
  props.players.forEach((p) => {
    if (hiddenDmgSlots.value.has(p.playerSlot)) return;
    getDmgSeries(p).forEach((v) => {
      if (v > max) max = v;
    });
  });
  return max;
});

const getPlayerDmgValue = (p: any, idx: number): number => {
  const arr = getDmgSeries(p);
  if (arr.length === 0) return 0;
  const safeIdx = Math.min(idx, arr.length - 1);
  return Number(arr[safeIdx] || 0);
};

// 根据选手在列表中的固定索引分配专属颜色，确保动态排序时颜色始终与折线一一对应
const getPlayerColor = (playerSlot: number): string => {
  const idx = props.players.findIndex((p) => p.playerSlot === playerSlot);
  return playerColors[(idx >= 0 ? idx : 0) % playerColors.length];
};

const hoverRadiantTotalDmg = computed(() =>
  radiantPlayers.value.reduce((acc, p) => acc + getPlayerDmgValue(p, dmgHoverIdx.value), 0)
);

const hoverDireTotalDmg = computed(() =>
  direPlayers.value.reduce((acc, p) => acc + getPlayerDmgValue(p, dmgHoverIdx.value), 0)
);

const getX = (idx: number, len: number) => {
  if (len <= 1) return 0;
  return Number(((idx / (len - 1)) * svgW).toFixed(1));
};

const getDmgY = (val: number) => {
  const maxVal = maxPlayerDmgVal.value || 1;
  return Number((svgH - 8 - (val / maxVal) * (svgH - 20)).toFixed(1));
};

const buildPlayerDmgLinePath = (p: any): string => {
  const arr = getDmgSeries(p);
  if (arr.length === 0) return '';
  return arr
    .map((v, i) => {
      const x = getX(i, maxDmgSeriesLength.value);
      const y = getDmgY(Number(v || 0));
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    })
    .join(' ');
};

// 当前悬停分钟下，按累计造成伤害从高到低实时排序的浮窗列表（含本分钟伤害增量）
const sortedHoverDmgPlayers = computed(() => {
  const idx = dmgHoverIdx.value;
  return props.players
    .filter((p) => !hiddenDmgSlots.value.has(p.playerSlot))
    .map((p) => {
      const val = getPlayerDmgValue(p, idx);
      const prevVal = idx > 0 ? getPlayerDmgValue(p, idx - 1) : 0;
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

// 表格按当前悬停时间点伤害从高到低同步排序
const sortedTablePlayers = computed(() => {
  const idx = dmgHoverIdx.value;
  return [...props.players].sort((a, b) => getPlayerDmgValue(b, idx) - getPlayerDmgValue(a, idx));
});

// Y 轴刻度格式化（如 34千 / 26千）
const formatAxisTick = (val: number): string => {
  const rounded = Math.round(val);
  if (rounded >= 1000) {
    return `${Math.round(rounded / 1000)}千`;
  }
  return String(rounded);
};

// 计算跟随竖线的浮窗位置（靠左时显示在竖线右侧，靠右时自动切到竖线左侧）
const getFloatingTooltipStyle = (idx: number, totalLen: number) => {
  const ratio = totalLen > 1 ? idx / (totalLen - 1) : 0;
  const pct = Number((ratio * 100).toFixed(2));
  if (ratio > 0.62) {
    return {
      left: `calc(${pct}% - 12px)`,
      transform: 'translateX(-100%)'
    };
  }
  return {
    left: `calc(${pct}% + 12px)`
  };
};

const onDmgChartMove = (e: MouseEvent) => {
  const target = e.currentTarget as SVGElement;
  const rect = target.getBoundingClientRect();
  const relX = Math.max(0, Math.min(1, (e.clientX - rect.left) / (rect.width || 1)));
  dmgHoverIndex.value = Math.round(relX * (maxDmgSeriesLength.value - 1));
};

const hideBrokenImg = (e: Event) => {
  (e.target as HTMLElement).style.display = 'none';
};

// 查询选手 A 击杀目标 heroId 的次数
const getHeroKillCount = (killerPlayer: any, victimHeroId: number): number => {
  const victimMeta = heroMap[String(victimHeroId)];
  if (!victimMeta || !victimMeta.name) return 0;
  const killedDict = killerPlayer.raw?.killed || {};
  return Number(killedDict[victimMeta.name] || 0);
};

const getCellBgClass = (rKills: number, dKills: number) => {
  if (rKills > dKills) return 'bg-emerald-500/15 border-emerald-500/40';
  if (dKills > rKills) return 'bg-rose-500/15 border-rose-500/40';
  return 'bg-[#0b0e14] border-slate-800';
};

// 解析选手造成伤害前 6 名的技能/普攻/物品
const getDamageSources = (p: any) => {
  const inflictor: Record<string, number> = p.raw?.damage_inflictor || {};
  const abilityUses: Record<string, number> = p.raw?.ability_uses || {};
  const itemUses: Record<string, number> = p.raw?.item_uses || {};

  const entries = Object.entries(inflictor)
    .map(([key, dmg]) => ({ key, dmg: Number(dmg || 0) }))
    .filter((e) => e.dmg > 0)
    .sort((a, b) => b.dmg - a.dmg);

  const total = entries.reduce((acc, cur) => acc + cur.dmg, 0) || 1;

  return entries.slice(0, 6).map((e) => {
    if (e.key === 'null') {
      return {
        key: 'null',
        name: '物理普通攻击 (Right Click)',
        img: '',
        dmg: e.dmg,
        pct: Number(((e.dmg * 100) / total).toFixed(1)),
        uses: 0,
        isBasicAttack: true,
        isItem: false
      };
    }

    // 优先检查是否为物品伤害，其次为技能伤害
    const itemMeta = getItemMeta(e.key);
    const abilityMeta = getAbilityMeta(e.key);
    const isItem = Boolean(itemMeta && (itemUses[e.key] !== undefined || !abilityMeta));

    return {
      key: e.key,
      name: isItem ? (itemMeta?.name || e.key) : (abilityMeta?.name || itemMeta?.name || e.key),
      img: isItem ? (itemMeta?.img || '') : (abilityMeta?.img || itemMeta?.img || ''),
      dmg: e.dmg,
      pct: Number(((e.dmg * 100) / total).toFixed(1)),
      uses: Number(abilityUses[e.key] || itemUses[e.key] || 0),
      isBasicAttack: false,
      isItem
    };
  });
};

// 解析选手承受来自哪些敌方英雄的伤害最高
const getTopDamageTakenFromHeroes = (p: any) => {
  const taken: Record<string, number> = p.raw?.damage_taken || {};
  return Object.entries(taken)
    .filter(([k, v]) => k.startsWith('npc_dota_hero_') && Number(v) > 0)
    .map(([npcName, dmg]) => {
      const hm = getHeroByNpcName(npcName);
      return {
        npcName,
        heroName: hm?.localized_name || npcName.replace('npc_dota_hero_', ''),
        img: hm ? getHeroAvatar(hm.id) : '',
        dmg: Number(dmg)
      };
    })
    .sort((a, b) => b.dmg - a.dmg)
    .slice(0, 5);
};
</script>
