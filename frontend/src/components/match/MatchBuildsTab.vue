<template>
  <div class="space-y-6">
    <!-- 1. 分阶段出装时间轴 (Item Purchase Timeline) -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 pb-4 border-b border-white/10">
        <div>
          <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-amber-300 rounded-full shadow-[0_0_8px_#fcd34d]"></span>
            选手分阶段出装时间轴 (Item Progression & Purchase Log)
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-300/80 mt-1">
            还原数据库保存的完整购买日志，按出门装、对线期(0-10m)、中期推进(10-25m)、后期神装(25m+)四阶段展现
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-1.5 sm:gap-2 aurora-subcard p-1 rounded-xl text-[11px] sm:text-xs font-mono">
          <button
            @click="filterMode = 'core'"
            :class="filterMode === 'core' ? 'bg-amber-400/20 text-amber-200 border border-amber-400/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
            class="px-2.5 sm:px-3 py-1 rounded-lg transition-all"
          >
            核心成装/配件 (过滤消耗品)
          </button>
          <button
            @click="filterMode = 'all'"
            :class="filterMode === 'all' ? 'bg-cyan-400/20 text-cyan-200 border border-cyan-300/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
            class="px-2.5 sm:px-3 py-1 rounded-lg transition-all"
          >
            全部购买流水 (含补给/眼/雾)
          </button>
        </div>
      </div>

      <div class="space-y-4 mt-5">
        <div
          v-for="p in players"
          :key="p.playerSlot"
          :class="[
            p.isRadiant ? 'border-l-4 border-l-emerald-400' : 'border-l-4 border-l-rose-500',
            String(p.accountId) === String(highlightAccountId) ? 'ring-1 ring-cyan-400/50 bg-cyan-950/20' : 'aurora-subcard'
          ]"
          class="rounded-xl p-4"
        >
          <!-- 选手信息头 -->
          <div class="flex flex-wrap items-center justify-between gap-2 pb-3 mb-3 border-b border-slate-800/70">
            <div class="flex items-center gap-3">
              <img :src="getHeroAvatar(p.heroId)" class="w-11 h-7 rounded border border-slate-700 object-cover" />
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-sm font-black text-white">{{ getHeroName(p.heroId) }}</span>
                  <span :class="getPositionMeta(p.laneRole).badgeClass" class="text-[10px] px-1.5 py-0.5 rounded border font-mono">
                    {{ getPositionMeta(p.laneRole).short }}
                  </span>
                  <span class="text-xs text-slate-400 font-mono">{{ p.personaname }}</span>
                </div>
              </div>
            </div>

            <!-- 中立装备演进记录 -->
            <div v-if="getNeutralHistory(p).length > 0" class="flex items-center gap-2 text-[11px] font-mono">
              <span class="text-slate-500">中立物品演进:</span>
              <div class="flex items-center gap-1.5">
                <div
                  v-for="(nh, nIdx) in getNeutralHistory(p)"
                  :key="nIdx"
                  class="flex items-center gap-1 px-2 py-0.5 rounded bg-[#121721] border border-purple-500/30 text-purple-300"
                  :title="`${nh.meta?.name || nh.key} (${formatGameTime(nh.time)})`"
                >
                  <img v-if="nh.meta?.img" :src="nh.meta.img" class="w-5 h-4 rounded object-cover" @error="hideBrokenImg" />
                  <span>{{ formatGameTime(nh.time) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 四阶段装备网格 -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-3 text-xs font-mono">
            <div
              v-for="phase in getPhasedPurchases(p)"
              :key="phase.title"
              class="bg-[#121721]/90 rounded-lg p-2.5 border border-slate-800/70"
            >
              <div class="flex items-center justify-between text-[10px] text-slate-400 mb-2 pb-1 border-b border-slate-800/60">
                <span class="font-bold text-slate-300">{{ phase.title }}</span>
                <span>{{ phase.items.length }} 件</span>
              </div>
              <div v-if="phase.items.length > 0" class="flex flex-wrap gap-1.5">
                <div
                  v-for="(item, idx) in phase.items"
                  :key="idx"
                  class="group relative flex flex-col items-center bg-[#0b0e14] rounded border border-slate-800 hover:border-amber-400/60 p-0.5 transition-all"
                  :title="`${item.meta.name} | 价格: ${item.meta.cost} | 购买时间: ${formatGameTime(item.time)}`"
                >
                  <img :src="item.meta.img" class="w-8 h-6 rounded-sm object-cover" @error="hideBrokenImg" />
                  <span
                    :class="item.meta.cost >= 2000 ? 'text-amber-400 font-bold' : 'text-slate-400'"
                    class="text-[9px] leading-tight mt-0.5"
                  >
                    {{ formatGameTime(item.time) }}
                  </span>
                </div>
              </div>
              <div v-else class="text-[10px] text-slate-600 py-2 text-center">无记录</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. 10名选手技能加点顺序矩阵 (Ability Upgrade Matrix) -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="pb-4 border-b border-white/10">
        <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-purple-400 rounded-full shadow-[0_0_8px_#c084fc]"></span>
          全员技能加点顺序与天赋树选择 (Ability Upgrades & Talents)
        </h3>
        <p class="text-[11px] sm:text-xs text-slate-300/80 mt-1">
          按英雄升级顺序展示每次学习的技能图标与天赋分支（悬停查看技能/天赋全称）
        </p>
      </div>

      <div class="mt-4 overflow-x-auto">
        <table class="w-full text-left text-xs font-mono border-collapse">
          <thead>
            <tr class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 text-[10px]">
              <th class="py-3 px-2.5 sm:px-3 sticky left-0 bg-[#0b0e14] z-10 min-w-[115px] sm:min-w-[170px]">英雄 / 选手</th>
              <th v-for="lvl in maxAbilityLen" :key="lvl" class="py-2 px-1 text-center min-w-[30px]">
                {{ lvl }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/50">
            <tr
              v-for="p in players"
              :key="p.playerSlot"
              :class="p.isRadiant ? 'hover:bg-emerald-500/5' : 'hover:bg-rose-500/5'"
            >
              <td class="py-2.5 px-2.5 sm:px-3 sticky left-0 bg-[#121721] z-10 border-r border-slate-800/80">
                <div class="flex items-center gap-1.5 sm:gap-2">
                  <span :class="p.isRadiant ? 'bg-emerald-400' : 'bg-rose-500'" class="w-1.5 h-6 rounded-full shrink-0"></span>
                  <img :src="getHeroAvatar(p.heroId)" class="w-7 sm:w-8 h-5 rounded object-cover shrink-0" />
                  <div class="truncate max-w-[65px] sm:max-w-[110px]">
                    <div class="text-white font-bold text-[10px] sm:text-[11px] truncate">{{ getHeroName(p.heroId) }}</div>
                    <div class="text-[9px] text-slate-500 truncate">{{ p.personaname }}</div>
                  </div>
                </div>
              </td>

              <td
                v-for="lvl in maxAbilityLen"
                :key="lvl"
                class="p-1 text-center"
              >
                <template v-if="getPlayerAbilities(p)[lvl - 1]">
                  <div
                    v-if="getPlayerAbilities(p)[lvl - 1]?.isTalent"
                    class="w-7 h-7 mx-auto rounded bg-amber-500/15 border border-amber-400/50 flex items-center justify-center text-amber-300 font-black text-[10px] cursor-help"
                    :title="getPlayerAbilities(p)[lvl - 1]?.name"
                  >
                    🌳
                  </div>
                  <div
                    v-else
                    class="w-7 h-7 mx-auto rounded bg-[#0b0e14] border border-slate-700 overflow-hidden flex items-center justify-center cursor-help hover:border-cyan-400 transition-colors"
                    :title="getPlayerAbilities(p)[lvl - 1]?.name"
                  >
                    <img
                      v-if="getPlayerAbilities(p)[lvl - 1]?.img"
                      :src="getPlayerAbilities(p)[lvl - 1]?.img"
                      class="w-full h-full object-cover"
                      @error="hideBrokenImg"
                    />
                    <span v-else class="text-[8px] text-slate-400">技能</span>
                  </div>
                </template>
                <div v-else class="w-7 h-7 mx-auto rounded bg-[#0b0e14]/40 border border-slate-800/40"></div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 3. 三路对线期 10 分钟正面对决 (Laning Phase 10m Showdown) -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="pb-4 border-b border-white/10">
        <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-emerald-400 rounded-full shadow-[0_0_8px_#34d399]"></span>
          三路 10 分钟对线期正面对决 (10-Minute Laning Showdown)
        </h3>
        <p class="text-[11px] sm:text-xs text-slate-300/80 mt-1">
          基于前 10 分钟分路数据（正补/反补、10分钟经济、对线效率及 800 经济差阈值）还原上、中、下三路对线优劣势
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mt-5">
        <div
          v-for="laneGroup in laneMatchups"
          :key="laneGroup.key"
          class="aurora-subcard rounded-2xl p-4 flex flex-col justify-between"
        >
          <div>
            <!-- 分路标题与胜负判定徽章 -->
            <div class="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
              <div>
                <div class="text-sm font-black text-white">{{ laneGroup.title }}</div>
                <div class="text-[10px] text-slate-500 font-mono">{{ laneGroup.subtitle }}</div>
              </div>
              <span
                :class="
                  laneGroup.goldDiff >= 800
                    ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30'
                    : laneGroup.goldDiff <= -800
                    ? 'text-rose-400 bg-rose-500/10 border-rose-500/30'
                    : 'text-amber-400 bg-amber-500/10 border-amber-500/30'
                "
                class="px-2.5 py-1 rounded-full text-[11px] font-black border font-mono"
              >
                {{
                  laneGroup.goldDiff >= 800
                    ? `天辉线优 (+${laneGroup.goldDiff})`
                    : laneGroup.goldDiff <= -800
                    ? `夜魇线优 (+${Math.abs(laneGroup.goldDiff)})`
                    : `均势对线 (${laneGroup.goldDiff >= 0 ? '+' : ''}${laneGroup.goldDiff})`
                }}
              </span>
            </div>

            <!-- 天辉该路选手 -->
            <div class="mb-3">
              <div class="text-[10px] font-mono text-emerald-400 font-bold mb-1.5 flex justify-between">
                <span>✦ 天辉分路阵容</span>
                <span>10m总经济: {{ laneGroup.radiantGold.toLocaleString() }}</span>
              </div>
              <div class="space-y-2">
                <div
                  v-for="p in laneGroup.radiant"
                  :key="p.playerSlot"
                  class="p-2.5 rounded-xl bg-[#121721] border border-emerald-500/20 flex items-center justify-between text-xs font-mono"
                >
                  <div class="flex items-center gap-2 min-w-0">
                    <img :src="getHeroAvatar(p.heroId)" class="w-9 h-6 rounded object-cover shrink-0" />
                    <div class="truncate">
                      <div class="text-white font-bold text-[11px] truncate">{{ getHeroName(p.heroId) }}</div>
                      <div class="text-[9px] text-slate-400 truncate">{{ p.personaname }} ({{ getPositionMeta(p.laneRole).short }})</div>
                    </div>
                  </div>
                  <div class="text-right shrink-0 ml-2">
                    <div class="text-amber-400 font-bold">{{ p.gold10m.toLocaleString() }} G</div>
                    <div class="text-[10px] text-slate-400">
                      补刀 {{ p.lh10m }}/{{ p.dn10m }} · 效率 {{ p.laneEffPct }}%
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 夜魇该路选手 -->
            <div>
              <div class="text-[10px] font-mono text-rose-400 font-bold mb-1.5 flex justify-between">
                <span>✦ 夜魇分路阵容</span>
                <span>10m总经济: {{ laneGroup.direGold.toLocaleString() }}</span>
              </div>
              <div class="space-y-2">
                <div
                  v-for="p in laneGroup.dire"
                  :key="p.playerSlot"
                  class="p-2.5 rounded-xl bg-[#121721] border border-rose-500/20 flex items-center justify-between text-xs font-mono"
                >
                  <div class="flex items-center gap-2 min-w-0">
                    <img :src="getHeroAvatar(p.heroId)" class="w-9 h-6 rounded object-cover shrink-0" />
                    <div class="truncate">
                      <div class="text-white font-bold text-[11px] truncate">{{ getHeroName(p.heroId) }}</div>
                      <div class="text-[9px] text-slate-400 truncate">{{ p.personaname }} ({{ getPositionMeta(p.laneRole).short }})</div>
                    </div>
                  </div>
                  <div class="text-right shrink-0 ml-2">
                    <div class="text-amber-400 font-bold">{{ p.gold10m.toLocaleString() }} G</div>
                    <div class="text-[10px] text-slate-400">
                      补刀 {{ p.lh10m }}/{{ p.dn10m }} · 效率 {{ p.laneEffPct }}%
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部对线经济占比条 -->
          <div class="mt-4 pt-3 border-t border-slate-800/80">
            <div class="w-full h-2 bg-rose-500/70 rounded-full overflow-hidden flex">
              <div
                class="bg-emerald-400 h-full transition-all"
                :style="{ width: laneGroup.radiantShare + '%' }"
              ></div>
            </div>
            <div class="flex justify-between text-[10px] font-mono text-slate-400 mt-1">
              <span>天辉占比 {{ laneGroup.radiantShare }}%</span>
              <span>夜魇占比 {{ (100 - laneGroup.radiantShare).toFixed(1) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 视野博弈、堆野、控符与操作遥测全表 (Vision, Support & APM Telemetry) -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6">
      <div class="pb-4 border-b border-white/10 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 class="text-sm sm:text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-cyan-400 rounded-full"></span>
            视野博弈、辅助拉野、神符控制与 APM 遥测 (Vision & Utility Telemetry)
          </h3>
          <p class="text-[11px] sm:text-xs text-slate-400 mt-1">
            全面统计真假眼安插与排眼、野怪营地拉野堆叠、神符拾取分类、操作手速 (APM) 与阵亡时长
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-2 sm:gap-4 text-[11px] sm:text-xs font-mono">
          <span class="px-2.5 sm:px-3 py-1 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            天辉视野: {{ teamVisionStats.radiantObs }}假/{{ teamVisionStats.radiantSen }}真 (排{{ teamVisionStats.radiantDewards }})
          </span>
          <span class="px-2.5 sm:px-3 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400">
            夜魇视野: {{ teamVisionStats.direObs }}假/{{ teamVisionStats.direSen }}真 (排{{ teamVisionStats.direDewards }})
          </span>
        </div>
      </div>

      <div class="mt-4 overflow-x-auto">
        <table class="w-full text-left text-xs font-mono">
          <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 text-[10px] uppercase">
            <tr>
              <th class="py-3 px-4">英雄 / 选手</th>
              <th class="py-3 px-3 text-center">位置</th>
              <th class="py-3 px-3 text-center">侦查守卫(假眼) 买/插/排</th>
              <th class="py-3 px-3 text-center">岗哨守卫(真眼) 买/插/排</th>
              <th class="py-3 px-3 text-center">堆野次数</th>
              <th class="py-3 px-3 text-center">控符总数 / 明细</th>
              <th class="py-3 px-3 text-center">手速 (APM)</th>
              <th class="py-3 px-3 text-center">地图信号 (Pings)</th>
              <th class="py-3 px-3 text-center">累计阵亡时长</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800/50">
            <tr
              v-for="p in players"
              :key="p.playerSlot"
              :class="p.isRadiant ? 'hover:bg-emerald-500/5' : 'hover:bg-rose-500/5'"
            >
              <td class="py-3 px-4">
                <div class="flex items-center gap-2.5">
                  <span :class="p.isRadiant ? 'bg-emerald-400' : 'bg-rose-500'" class="w-1.5 h-6 rounded-full"></span>
                  <img :src="getHeroAvatar(p.heroId)" class="w-9 h-6 rounded object-cover" />
                  <div>
                    <div class="font-bold text-white">{{ getHeroName(p.heroId) }}</div>
                    <div class="text-[10px] text-slate-500">{{ p.personaname }}</div>
                  </div>
                </div>
              </td>

              <td class="py-3 px-3 text-center">
                <span :class="getPositionMeta(p.laneRole).badgeClass" class="px-2 py-0.5 rounded border text-[10px]">
                  {{ getPositionMeta(p.laneRole).short }}
                </span>
              </td>

              <td class="py-3 px-3 text-center">
                <span class="text-amber-300 font-bold">{{ p.raw?.purchase_ward_observer || 0 }}</span> /
                <span class="text-amber-400 font-bold">{{ p.obsPlaced }}</span> /
                <span class="text-emerald-400 font-bold">{{ p.obsKilled }}</span>
              </td>

              <td class="py-3 px-3 text-center">
                <span class="text-sky-300 font-bold">{{ p.raw?.purchase_ward_sentry || 0 }}</span> /
                <span class="text-sky-400 font-bold">{{ p.senPlaced }}</span> /
                <span class="text-emerald-400 font-bold">{{ p.senKilled }}</span>
              </td>

              <td class="py-3 px-3 text-center">
                <span :class="p.campsStacked > 0 ? 'text-purple-400 font-bold' : 'text-slate-500'">
                  {{ p.campsStacked || 0 }} 处
                </span>
              </td>

              <td class="py-3 px-3 text-center">
                <div class="text-cyan-400 font-bold">{{ p.raw?.rune_pickups || 0 }} 个</div>
                <div v-if="getRuneBreakdownText(p)" class="text-[9px] text-slate-500 max-w-[180px] mx-auto truncate" :title="getRuneBreakdownText(p)">
                  {{ getRuneBreakdownText(p) }}
                </div>
              </td>

              <td class="py-3 px-3 text-center">
                <span class="text-white font-bold">{{ p.raw?.actions_per_min || '-' }}</span>
              </td>

              <td class="py-3 px-3 text-center text-slate-300">
                {{ p.raw?.pings || 0 }} 次
              </td>

              <td class="py-3 px-3 text-center text-rose-400">
                {{ p.raw?.life_state_dead ? `${p.raw.life_state_dead}秒` : '0秒' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  getHeroName,
  getHeroAvatar,
  getPositionMeta,
  getItemMeta,
  getAbilityMeta,
  getRuneName,
  formatGameTime,
  CONSUMABLE_KEYS
} from '../../utils/dotaHelper';

const props = defineProps<{
  players: any[];
  highlightAccountId?: string;
}>();

const filterMode = ref<'core' | 'all'>('core');

const hideBrokenImg = (e: Event) => {
  (e.target as HTMLElement).style.display = 'none';
};

// 解析某名选手的分阶段购买记录
const isConsumableOrCheap = (key: string, time: number, cost: number) => {
  if (time < 0) {
    return ['ward_observer', 'ward_sentry', 'smoke_of_deceit', 'dust', 'tpscroll', 'clarity', 'flask', 'tango'].includes(key);
  }
  if (CONSUMABLE_KEYS.has(key)) return true;
  return cost > 0 && cost < 450 && !['boots', 'magic_stick', 'magic_wand', 'soul_ring', 'bottle', 'wind_lace', 'orb_of_venom', 'blight_stone'].includes(key);
};

const getPhasedPurchases = (p: any) => {
  const rawLog: Array<{ time: number; key: string }> = Array.isArray(p.raw?.purchase_log) ? p.raw.purchase_log : [];
  const filtered = rawLog
    .map((entry) => {
      const meta = getItemMeta(entry.key);
      return meta ? { time: entry.time, key: entry.key, meta } : null;
    })
    .filter((item): item is NonNullable<typeof item> => {
      if (!item) return false;
      if (item.key.startsWith('recipe_')) return false;
      if (filterMode.value === 'core') {
        if (isConsumableOrCheap(item.key, item.time, item.meta.cost)) return false;
      }
      return true;
    });

  return [
    {
      title: '🌅 出门装 (< 00:00)',
      items: filtered.filter((x) => x.time < 0)
    },
    {
      title: '⚔️ 对线期 (00:00 - 10:00)',
      items: filtered.filter((x) => x.time >= 0 && x.time < 600)
    },
    {
      title: '🔥 中期节奏 (10:00 - 25:00)',
      items: filtered.filter((x) => x.time >= 600 && x.time < 1500)
    },
    {
      title: '👑 后期神装 (25:00+)',
      items: filtered.filter((x) => x.time >= 1500)
    }
  ];
};

// 解析中立物品更换历史
const getNeutralHistory = (p: any) => {
  const hist: Array<any> = Array.isArray(p.raw?.neutral_item_history) ? p.raw.neutral_item_history : [];
  return hist
    .filter((h) => h.item_neutral)
    .map((h) => ({
      time: h.time,
      key: h.item_neutral,
      meta: getItemMeta(h.item_neutral)
    }));
};

// 解析技能升级序列
const getPlayerAbilities = (p: any) => {
  const arr: number[] = Array.isArray(p.raw?.ability_upgrades_arr) ? p.raw.ability_upgrades_arr : [];
  return arr.map((id) => getAbilityMeta(id));
};

const maxAbilityLen = computed(() => {
  let max = 18;
  props.players.forEach((p) => {
    const len = Array.isArray(p.raw?.ability_upgrades_arr) ? p.raw.ability_upgrades_arr.length : 0;
    if (len > max) max = len;
  });
  return Math.min(max, 30);
});

// 构建上/中/下三路对决分组
const laneMatchups = computed(() => {
  // OpenDota 地图分路编号 lane：1=下路 (天辉优势路 vs 夜魇劣势路), 2=中路, 3=上路 (天辉劣势路 vs 夜魇优势路)
  const buildGroup = (key: string, title: string, subtitle: string, radLane: number, direLane: number) => {
    const radiant = props.players.filter((p) => p.isRadiant && Number(p.lane || p.raw?.lane) === radLane);
    const dire = props.players.filter((p) => !p.isRadiant && Number(p.lane || p.raw?.lane) === direLane);
    const radiantGold = radiant.reduce((acc, p) => acc + (p.gold10m || 0), 0);
    const direGold = dire.reduce((acc, p) => acc + (p.gold10m || 0), 0);
    const total = radiantGold + direGold || 1;
    return {
      key,
      title,
      subtitle,
      radiant,
      dire,
      radiantGold,
      direGold,
      goldDiff: radiantGold - direGold,
      radiantShare: Number(((radiantGold * 100) / total).toFixed(1))
    };
  };

  return [
    buildGroup('top', '🌲 上路对线 (Top Lane)', '天辉劣势路 (Pos 3/4) vs 夜魇优势路 (Pos 1/5)', 3, 3),
    buildGroup('mid', '⚡ 中路对决 (Mid 1v1)', '天辉中单 (Pos 2) vs 夜魇中单 (Pos 2)', 2, 2),
    buildGroup('bot', '🌊 下路对线 (Bot Lane)', '天辉优势路 (Pos 1/5) vs 夜魇劣势路 (Pos 3/4)', 1, 1)
  ];
});

// 团队总视野汇总
const teamVisionStats = computed(() => {
  let radiantObs = 0, radiantSen = 0, radiantDewards = 0;
  let direObs = 0, direSen = 0, direDewards = 0;

  props.players.forEach((p) => {
    if (p.isRadiant) {
      radiantObs += p.obsPlaced || 0;
      radiantSen += p.senPlaced || 0;
      radiantDewards += (p.obsKilled || 0) + (p.senKilled || 0);
    } else {
      direObs += p.obsPlaced || 0;
      direSen += p.senPlaced || 0;
      direDewards += (p.obsKilled || 0) + (p.senKilled || 0);
    }
  });

  return { radiantObs, radiantSen, radiantDewards, direObs, direSen, direDewards };
});

const getRuneBreakdownText = (p: any): string => {
  const runes = p.raw?.runes || {};
  return Object.entries(runes)
    .map(([k, v]) => `${getRuneName(k)}x${v}`)
    .join(' · ');
};
</script>
