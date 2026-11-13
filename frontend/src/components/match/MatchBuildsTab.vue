<template>
  <div class="space-y-6">
    <!-- 1. 分阶段出装时间轴 (Item Purchase Timeline) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800/80">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-amber-400 rounded-full"></span>
            选手分阶段出装时间轴 (Item Progression & Purchase Log)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            还原数据库保存的完整购买日志，按出门装、对线期(0-10m)、中期推进(10-25m)、后期神装(25m+)四阶段展现
          </p>
        </div>

        <div class="flex items-center gap-2 bg-[#0b0e14] p-1 rounded-xl border border-slate-800 text-xs font-mono">
          <button
            @click="filterMode = 'core'"
            :class="filterMode === 'core' ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
            class="px-3 py-1 rounded-lg transition-all"
          >
            核心成装/配件 (过滤消耗品)
          </button>
          <button
            @click="filterMode = 'all'"
            :class="filterMode === 'all' ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-bold' : 'text-slate-400 hover:text-slate-200'"
            class="px-3 py-1 rounded-lg transition-all"
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
            p.isRadiant ? 'border-l-4 border-l-emerald-500' : 'border-l-4 border-l-rose-500',
            String(p.accountId) === String(highlightAccountId) ? 'ring-1 ring-cyan-500/50 bg-cyan-950/10' : 'bg-[#0b0e14]/80'
          ]"
          class="rounded-xl border border-slate-800 p-4"
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
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80">
        <h3 class="text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-purple-400 rounded-full"></span>
          全员技能加点顺序与天赋树选择 (Ability Upgrades & Talents)
        </h3>
        <p class="text-xs text-slate-400 mt-1">
          按英雄升级顺序展示每次学习的技能图标与天赋分支（悬停查看技能/天赋全称）
        </p>
      </div>

      <div class="mt-4 overflow-x-auto">
        <table class="w-full text-left text-xs font-mono border-collapse">
          <thead>
            <tr class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 text-[10px]">
              <th class="py-3 px-3 sticky left-0 bg-[#0b0e14] z-10 min-w-[170px]">英雄 / 选手</th>
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
              <td class="py-2.5 px-3 sticky left-0 bg-[#121721] z-10 border-r border-slate-800/80">
                <div class="flex items-center gap-2">
                  <span :class="p.isRadiant ? 'bg-emerald-400' : 'bg-rose-500'" class="w-1.5 h-6 rounded-full shrink-0"></span>
                  <img :src="getHeroAvatar(p.heroId)" class="w-8 h-5 rounded object-cover shrink-0" />
                  <div class="truncate max-w-[110px]">
                    <div class="text-white font-bold text-[11px] truncate">{{ getHeroName(p.heroId) }}</div>
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
</script>
