<template>
  <section class="bg-[#121721] border border-slate-800/80 rounded-2xl overflow-hidden shadow-2xl">
    <!-- 阵营标题头 -->
    <div
      :class="isRadiant ? 'bg-gradient-to-r from-emerald-500/15 via-emerald-500/5 to-transparent' : 'bg-gradient-to-r from-rose-500/15 via-rose-500/5 to-transparent'"
      class="px-6 py-3.5 border-b border-slate-800/80 flex flex-wrap items-center justify-between gap-4"
    >
      <div class="flex items-center gap-3">
        <span :class="isRadiant ? 'text-emerald-400' : 'text-rose-500'" class="text-lg">✦</span>
        <span :class="isRadiant ? 'text-emerald-400' : 'text-rose-500'" class="text-sm font-black tracking-widest uppercase">
          {{ isRadiant ? '天辉阵营 RADIANT FORCES' : '夜魇阵营 DIRE FORCES' }}
        </span>
        <span
          v-if="isWinner"
          :class="isRadiant ? 'bg-emerald-400 text-black shadow-lg shadow-emerald-500/20' : 'bg-rose-500 text-white shadow-lg shadow-rose-500/20'"
          class="text-[10px] px-2.5 py-0.5 rounded font-black tracking-wider uppercase"
        >
          🏆 WINNER 胜利方
        </span>
      </div>

      <!-- 阵营汇总指标 -->
      <div class="flex flex-wrap items-center gap-5 text-xs font-mono text-slate-400">
        <span>总击杀: <strong class="text-white">{{ teamSummary.kills }}</strong></span>
        <span>总经济: <strong class="text-amber-400">{{ teamSummary.netWorth.toLocaleString() }}</strong></span>
        <span>总伤害: <strong class="text-rose-400">{{ teamSummary.heroDamage.toLocaleString() }}</strong></span>
        <span>推塔伤害: <strong class="text-cyan-400">{{ teamSummary.towerDamage.toLocaleString() }}</strong></span>
        <span>视野(假/真): <strong class="text-emerald-400">{{ teamSummary.obs }}/{{ teamSummary.sen }}</strong></span>
      </div>
    </div>

    <!-- 详单数据表 (支持点击任意行展开选手深度遥测抽屉) -->
    <div class="overflow-x-auto">
      <table class="w-full text-left text-xs font-mono">
        <thead class="bg-[#0b0e14] text-slate-400 border-b border-slate-800 text-[10px] uppercase tracking-wider">
          <tr>
            <th class="py-3.5 px-4 min-w-[235px]">选手 / 英雄 / 段位 / 荣誉</th>
            <th class="py-3.5 px-3 text-center">位置 / 10m对线</th>
            <th class="py-3.5 px-3 text-center">K / D / A (参战)</th>
            <th class="py-3.5 px-3 text-center">净资产 / GPM·XPM</th>
            <th class="py-3.5 px-3 text-center">正反补 / 野怪</th>
            <th class="py-3.5 px-3 min-w-[145px]">英雄伤害 / 建筑</th>
            <th class="py-3.5 px-3 min-w-[135px]">英雄承伤 / 治疗</th>
            <th class="py-3.5 px-3 text-center">控制 / 视野</th>
            <th class="py-3.5 px-4 min-w-[295px]">终局六神装 / 背包 / 中立 / 魔晶A杖</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-800/50">
          <template v-for="p in teamPlayers" :key="p.playerSlot">
            <tr
              @click="toggleExpand(p.playerSlot)"
              :class="[
                isRadiant ? 'hover:bg-emerald-500/5' : 'hover:bg-rose-500/5',
                String(p.accountId) === String(highlightAccountId)
                  ? 'bg-cyan-500/10 border-l-4 border-l-cyan-400'
                  : ''
              ]"
              class="transition-colors cursor-pointer group"
            >
              <!-- 1. 选手与英雄 -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                  <div class="relative shrink-0">
                    <img
                      :src="getHeroAvatar(p.heroId)"
                      class="w-14 h-9 rounded-lg border border-slate-700 object-cover shadow-md group-hover:border-cyan-400 transition-colors"
                    />
                    <span class="absolute -bottom-1.5 -right-1.5 bg-[#0b0e14] border border-slate-700 text-amber-400 text-[9px] font-black px-1 rounded">
                      Lv.{{ p.level }}
                    </span>
                    <span
                      v-if="p.isMatchMvp"
                      class="absolute -top-2 -left-1.5 bg-gradient-to-r from-amber-400 to-yellow-500 text-black text-[8px] font-black px-1.5 py-0.2 rounded shadow"
                    >
                      MVP
                    </span>
                    <span
                      v-else-if="p.isMatchSvp"
                      class="absolute -top-2 -left-1.5 bg-gradient-to-r from-purple-500 to-indigo-500 text-white text-[8px] font-black px-1.5 py-0.2 rounded shadow"
                    >
                      SVP
                    </span>
                  </div>

                  <div class="min-w-0">
                    <div class="flex items-center gap-1.5 flex-wrap">
                      <button
                        v-if="p.accountId && p.accountId > 0"
                        @click.stop="openPlayerProfile(p.accountId)"
                        class="font-bold text-slate-100 hover:text-cyan-400 truncate max-w-[115px] text-left transition-colors"
                        :title="`点击查看 ${p.personaname} 的个人战绩主页`"
                      >
                        {{ p.personaname }}
                      </button>
                      <span v-else class="font-bold text-slate-400">匿名玩家</span>

                      <span
                        v-if="String(p.accountId) === String(highlightAccountId)"
                        class="px-1.5 py-0.2 rounded bg-cyan-500 text-black text-[9px] font-black"
                      >
                        ME
                      </span>

                      <!-- 开黑车队标记 -->
                      <span
                        v-if="p.partyTag"
                        :class="p.partyColorClass"
                        class="px-1.5 py-0.2 rounded text-[9px] border font-bold"
                      >
                        {{ p.partyTag }}
                      </span>
                    </div>

                    <div class="flex items-center gap-1.5 mt-1 flex-wrap">
                      <span class="text-[11px] text-slate-300 font-semibold">{{ getHeroName(p.heroId) }}</span>
                      <span :class="getRankMeta(p.rankTier).colorClass" class="text-[9px] px-1.5 py-0.2 rounded border">
                        {{ getRankMeta(p.rankTier).shortLabel }}
                      </span>
                      <span
                        v-for="badge in p.honorBadges"
                        :key="badge.text"
                        :class="badge.cls"
                        class="text-[9px] px-1 rounded border"
                      >
                        {{ badge.text }}
                      </span>
                    </div>
                  </div>
                </div>
              </td>

              <!-- 2. 分路与对线表现 -->
              <td class="py-3.5 px-3 text-center">
                <div class="flex flex-col items-center gap-1">
                  <span :class="getPositionMeta(p.laneRole).badgeClass" class="px-2 py-0.5 rounded border text-[10px] font-bold">
                    {{ getPositionMeta(p.laneRole).short }}
                  </span>
                  <div class="flex items-center gap-1.5 text-[10px]">
                    <span
                      :class="
                        p.laneResult === 1
                          ? 'text-emerald-400 font-bold'
                          : p.laneResult === 3
                          ? 'text-rose-400 font-bold'
                          : 'text-amber-400 font-bold'
                      "
                    >
                      {{ p.laneResult === 1 ? '线优' : p.laneResult === 3 ? '线劣' : '均势' }}
                    </span>
                    <span class="text-slate-500">|</span>
                    <span class="text-slate-400" title="10分钟经济 / 对线效率">{{ p.gold10m }}G ({{ p.laneEffPct }}%)</span>
                  </div>
                </div>
              </td>

              <!-- 3. KDA 与参战率 -->
              <td class="py-3.5 px-3 text-center">
                <div class="font-bold text-sm">
                  <span class="text-emerald-400">{{ p.kills }}</span>
                  <span class="text-slate-600 mx-0.5">/</span>
                  <span class="text-rose-400">{{ p.deaths }}</span>
                  <span class="text-slate-600 mx-0.5">/</span>
                  <span class="text-cyan-300">{{ p.assists }}</span>
                </div>
                <div class="flex items-center justify-center gap-1.5 text-[10px] mt-1">
                  <span class="px-1.5 py-0.2 rounded bg-slate-800 text-amber-300 font-bold">{{ p.kda }} KDA</span>
                  <span class="text-slate-400">参战 {{ p.tfParticipation }}%</span>
                </div>
              </td>

              <!-- 4. 净资产与 GPM/XPM -->
              <td class="py-3.5 px-3 text-center">
                <div class="text-amber-400 font-black text-sm flex items-center justify-center gap-1">
                  <span>{{ p.netWorth.toLocaleString() }}</span>
                  <span class="text-[9px] text-slate-500 font-normal">({{ p.netWorthShare }}%)</span>
                </div>
                <div class="text-[10px] text-slate-400 mt-1">
                  <span class="text-amber-300 font-bold">{{ p.goldPerMin }}</span> GPM ·
                  <span class="text-sky-300 font-bold">{{ p.xpPerMin }}</span> XPM
                </div>
              </td>

              <!-- 5. 正反补与野怪 -->
              <td class="py-3.5 px-3 text-center">
                <div class="text-white font-bold">
                  {{ p.lastHits }} <span class="text-slate-500">/</span> <span class="text-amber-300">{{ p.denies }}</span>
                </div>
                <div class="text-[10px] text-slate-500 mt-1" title="野怪击杀 (含远古野)">
                  野怪 {{ p.neutralKills }} (远古 {{ p.ancientKills }})
                </div>
              </td>

              <!-- 6. 英雄伤害与建筑伤害 -->
              <td class="py-3.5 px-3">
                <div class="flex justify-between text-[11px] mb-1">
                  <span class="text-white font-bold">{{ p.heroDamage.toLocaleString() }}</span>
                  <span class="text-rose-400 text-[10px]">{{ p.damageShare }}%</span>
                </div>
                <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                  <div class="bg-gradient-to-r from-rose-500 to-amber-400 h-full rounded-full" :style="{ width: p.damageShare + '%' }"></div>
                </div>
                <div class="text-[10px] text-slate-400 mt-1 flex justify-between">
                  <span>🏰 建筑: {{ p.towerDamage.toLocaleString() }}</span>
                </div>
              </td>

              <!-- 7. 承受伤害与治疗 -->
              <td class="py-3.5 px-3">
                <div class="flex justify-between text-[11px] mb-1">
                  <span class="text-sky-300 font-bold">{{ p.heroDamageTaken.toLocaleString() }}</span>
                  <span class="text-sky-400 text-[10px]">{{ p.tankShare }}%</span>
                </div>
                <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                  <div class="bg-gradient-to-r from-sky-500 to-indigo-400 h-full rounded-full" :style="{ width: p.tankShare + '%' }"></div>
                </div>
                <div class="text-[10px] text-emerald-400 mt-1">
                  💚 治疗: {{ p.heroHealing > 0 ? p.heroHealing.toLocaleString() : '-' }}
                </div>
              </td>

              <!-- 8. 控制与视野 -->
              <td class="py-3.5 px-3 text-center">
                <div class="text-purple-300 font-bold" title="造成眩晕控制总时长">
                  🌀 {{ p.stuns }}s
                </div>
                <div class="text-[10px] text-slate-400 mt-1" title="安插假眼 / 安插真眼 (排眼总数)">
                  👁️ <span class="text-amber-300">{{ p.obsPlaced }}</span>/<span class="text-sky-300">{{ p.senPlaced }}</span>
                  <span v-if="p.obsKilled + p.senKilled > 0" class="text-emerald-400"> (排{{ p.obsKilled + p.senKilled }})</span>
                </div>
              </td>

              <!-- 9. 终局六神装 + 背包 + 中立 + A杖魔晶 + 永久 Buff -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-2">
                  <!-- 6格主装备栏 -->
                  <div class="grid grid-cols-6 gap-1">
                    <div
                      v-for="(slotItem, sIdx) in p.mainItems"
                      :key="sIdx"
                      class="relative w-9 h-7 bg-[#0b0e14] border border-slate-700/90 rounded overflow-hidden flex items-center justify-center hover:border-amber-400 transition-colors"
                      :title="slotItem ? `${slotItem.name} (${slotItem.cost}金)${slotItem.purchaseTimeStr ? ' · 购于 ' + slotItem.purchaseTimeStr : ''}` : '空槽位'"
                    >
                      <img v-if="slotItem" :src="slotItem.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                      <span
                        v-if="slotItem && slotItem.purchaseMinStr"
                        class="absolute bottom-0 right-0 bg-black/85 text-[8px] text-amber-300 px-0.5 leading-tight"
                      >
                        {{ slotItem.purchaseMinStr }}
                      </span>
                    </div>
                  </div>

                  <!-- 中立装备槽 (圆形) -->
                  <div
                    class="w-7 h-7 rounded-full bg-[#0b0e14] border border-purple-500/50 overflow-hidden flex items-center justify-center shrink-0"
                    :title="p.neutralItem ? `中立物品: ${p.neutralItem.name}` : '无中立物品'"
                  >
                    <img v-if="p.neutralItem" :src="p.neutralItem.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                    <span v-else class="text-[8px] text-slate-700">N</span>
                  </div>

                  <!-- A杖与魔晶点亮指示 -->
                  <div class="flex flex-col gap-0.5 shrink-0">
                    <span
                      :class="p.hasScepter ? 'bg-blue-500/25 text-blue-300 border-blue-400/60' : 'bg-slate-900 text-slate-700 border-slate-800'"
                      class="text-[8px] px-1 rounded border font-black leading-tight"
                      title="阿哈利姆神杖 (A杖)"
                    >
                      A杖
                    </span>
                    <span
                      :class="p.hasShard ? 'bg-cyan-500/25 text-cyan-300 border-cyan-400/60' : 'bg-slate-900 text-slate-700 border-slate-800'"
                      class="text-[8px] px-1 rounded border font-black leading-tight"
                      title="阿哈利姆魔晶"
                    >
                      魔晶
                    </span>
                  </div>
                </div>

                <!-- 第二排：背包栏 + 永久 Buff + 展开详情提示 -->
                <div class="flex items-center justify-between mt-1.5">
                  <div class="flex items-center gap-1">
                    <!-- 背包 3 格 -->
                    <span class="text-[9px] text-slate-600 mr-0.5">背包:</span>
                    <div
                      v-for="(bpItem, bIdx) in p.backpackItems"
                      :key="bIdx"
                      class="w-6 h-4 bg-[#0b0e14]/70 border border-slate-800 rounded-sm overflow-hidden flex items-center justify-center opacity-75"
                      :title="bpItem ? `背包: ${bpItem.name}` : '空背包'"
                    >
                      <img v-if="bpItem" :src="bpItem.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                    </div>

                    <!-- 永久 Buff 列表 -->
                    <div
                      v-for="(buff, bfIdx) in p.permanentBuffs"
                      :key="bfIdx"
                      class="relative w-6 h-4 rounded-sm bg-amber-500/10 border border-amber-500/40 overflow-hidden flex items-center justify-center ml-1"
                      :title="`永久增益: ${buff.name} ${buff.stacks > 0 ? '(+' + buff.stacks + ')' : ''}`"
                    >
                      <img :src="buff.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                      <span v-if="buff.stacks > 0" class="absolute bottom-0 right-0 bg-black/85 text-amber-300 text-[7px] px-0.5 leading-none font-bold">
                        {{ buff.stacks }}
                      </span>
                    </div>
                  </div>

                  <span class="text-[9px] text-cyan-400/80 group-hover:text-cyan-300">
                    {{ expandedSlots.has(p.playerSlot) ? '▲ 收起详情' : '▼ 深度遥测' }}
                  </span>
                </div>
              </td>
            </tr>

            <!-- 展开行：该选手本场深度遥测抽屉 (OpenDota 全球同英雄排名、金币构成、高光明细) -->
            <tr v-if="expandedSlots.has(p.playerSlot)" class="bg-[#0b0e14]/95">
              <td colspan="9" class="p-5 border-l-4" :class="isRadiant ? 'border-l-emerald-400' : 'border-l-rose-500'">
                <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 font-mono text-xs">
                  <!-- 子面板 A: OpenDota 同英雄全球分位水平 (Benchmarks) -->
                  <div class="bg-[#121721] border border-slate-800 rounded-xl p-4">
                    <div class="text-xs font-black text-cyan-400 mb-3 flex items-center justify-between">
                      <span>🌍 同英雄全球表现百分位 (Hero Benchmarks)</span>
                      <span class="text-[10px] text-slate-400">{{ getHeroName(p.heroId) }}</span>
                    </div>
                    <div class="space-y-2.5">
                      <div v-for="bm in p.benchmarksList" :key="bm.key">
                        <div class="flex justify-between text-[11px] mb-1">
                          <span class="text-slate-400">{{ bm.label }}: <strong class="text-white">{{ bm.raw }}</strong></span>
                          <span
                            :class="bm.pct >= 80 ? 'text-amber-400 font-black' : bm.pct >= 50 ? 'text-emerald-400 font-bold' : 'text-slate-400'"
                          >
                            超越全球 {{ bm.pct }}% 玩家
                          </span>
                        </div>
                        <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div
                            :class="bm.pct >= 80 ? 'bg-amber-400' : bm.pct >= 50 ? 'bg-emerald-400' : 'bg-cyan-500'"
                            class="h-full rounded-full"
                            :style="{ width: bm.pct + '%' }"
                          ></div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 子面板 B: 金币获取与损耗来源拆解 (gold_reasons) -->
                  <div class="bg-[#121721] border border-slate-800 rounded-xl p-4">
                    <div class="text-xs font-black text-amber-400 mb-3 flex items-center justify-between">
                      <span>💰 经济收支来源构成 (Gold Reasons)</span>
                      <span class="text-[10px] text-slate-400">总获取: {{ (p.raw?.total_gold || p.netWorth).toLocaleString() }}</span>
                    </div>
                    <div class="space-y-2">
                      <div
                        v-for="gr in p.goldReasonsList"
                        :key="gr.code"
                        class="flex items-center justify-between py-1 border-b border-slate-800/60 text-[11px]"
                      >
                        <span class="text-slate-300">{{ gr.name }}</span>
                        <span :class="gr.val >= 0 ? 'text-amber-400 font-bold' : 'text-rose-400 font-bold'">
                          {{ gr.val >= 0 ? `+${gr.val.toLocaleString()}` : gr.val.toLocaleString() }} G
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- 子面板 C: 选手本场战斗高光与微观操作指标 -->
                  <div class="bg-[#121721] border border-slate-800 rounded-xl p-4 flex flex-col justify-between">
                    <div>
                      <div class="text-xs font-black text-purple-400 mb-3">⚡ 个人高光与微观遥测 (Micro Telemetry)</div>
                      <div class="grid grid-cols-2 gap-2.5 text-[11px]">
                        <div class="p-2.5 rounded-lg bg-[#0b0e14] border border-slate-800">
                          <span class="text-slate-500 block text-[10px]">单次最高爆发伤害</span>
                          <span class="text-rose-400 font-black text-sm">{{ p.maxHit.value.toLocaleString() }}</span>
                          <span class="text-slate-400 block text-[10px] truncate">{{ p.maxHit.source }}</span>
                        </div>
                        <div class="p-2.5 rounded-lg bg-[#0b0e14] border border-slate-800">
                          <span class="text-slate-500 block text-[10px]">操作频率 / 信号</span>
                          <span class="text-cyan-400 font-black text-sm">{{ p.raw?.actions_per_min || '-' }} APM</span>
                          <span class="text-slate-400 block text-[10px]">发信号 {{ p.raw?.pings || 0 }} 次</span>
                        </div>
                        <div class="p-2.5 rounded-lg bg-[#0b0e14] border border-slate-800">
                          <span class="text-slate-500 block text-[10px]">买活 / 阵亡时长</span>
                          <span class="text-amber-400 font-black text-sm">买活 {{ p.buybackCount }} 次</span>
                          <span class="text-slate-400 block text-[10px]">读秒共 {{ p.raw?.life_state_dead || 0 }} 秒</span>
                        </div>
                        <div class="p-2.5 rounded-lg bg-[#0b0e14] border border-slate-800">
                          <span class="text-slate-500 block text-[10px]">10分钟正反补 / 经济</span>
                          <span class="text-emerald-400 font-black text-sm">{{ p.lh10m }} / {{ p.dn10m }}</span>
                          <span class="text-slate-400 block text-[10px]">10m经济 {{ p.gold10m.toLocaleString() }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="mt-3 pt-2 border-t border-slate-800 flex items-center justify-between text-[11px]">
                      <span class="text-slate-400">Steam32 ID: <strong class="text-white">{{ p.accountId || '匿名' }}</strong></span>
                      <button
                        v-if="p.accountId && p.accountId > 0"
                        @click.stop="openPlayerProfile(p.accountId)"
                        class="text-cyan-400 hover:text-cyan-300 font-bold"
                      >
                        进入该玩家战绩档案 →
                      </button>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  getHeroName,
  getHeroAvatar,
  getRankMeta,
  getPositionMeta
} from '../../utils/dotaHelper';

const props = defineProps<{
  isRadiant: boolean;
  isWinner: boolean;
  teamPlayers: any[];
  highlightAccountId?: string;
}>();

const router = useRouter();
const expandedSlots = ref<Set<number>>(new Set());

const toggleExpand = (slot: number) => {
  const next = new Set(expandedSlots.value);
  if (next.has(slot)) next.delete(slot);
  else next.add(slot);
  expandedSlots.value = next;
};

const openPlayerProfile = (accountId: number) => {
  const url = router.resolve({ name: 'PlayerDashboard', params: { accountId: String(accountId) } }).href;
  window.open(url, '_blank');
};

const hideBrokenImg = (e: Event) => {
  (e.target as HTMLElement).style.display = 'none';
};

// 阵营总量统计
const teamSummary = computed(() => {
  return props.teamPlayers.reduce(
    (acc, p) => {
      acc.kills += p.kills || 0;
      acc.netWorth += p.netWorth || 0;
      acc.heroDamage += p.heroDamage || 0;
      acc.towerDamage += p.towerDamage || 0;
      acc.obs += p.obsPlaced || 0;
      acc.sen += p.senPlaced || 0;
      return acc;
    },
    { kills: 0, netWorth: 0, heroDamage: 0, towerDamage: 0, obs: 0, sen: 0 }
  );
});
</script>
