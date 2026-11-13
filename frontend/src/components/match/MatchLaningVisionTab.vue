<template>
  <div class="space-y-6">
    <!-- 1. 三路对线期 10 分钟正面对决 (Laning Phase 10m Showdown) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80">
        <h3 class="text-base font-black text-white flex items-center gap-2">
          <span class="w-2 h-5 bg-emerald-400 rounded-full"></span>
          三路 10 分钟对线期正面对决 (10-Minute Laning Showdown)
        </h3>
        <p class="text-xs text-slate-400 mt-1">
          基于前 10 分钟分路数据（正补/反补、10分钟经济、对线效率及 800 经济差阈值）还原上、中、下三路对线优劣势
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mt-5">
        <div
          v-for="laneGroup in laneMatchups"
          :key="laneGroup.key"
          class="bg-[#0b0e14]/90 border border-slate-800 rounded-2xl p-4 flex flex-col justify-between"
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

    <!-- 2. 视野博弈、堆野、控符与操作遥测全表 (Vision, Support & APM Telemetry) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-cyan-400 rounded-full"></span>
            视野博弈、辅助拉野、神符控制与 APM 遥测 (Vision & Utility Telemetry)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            全面统计真假眼安插与排眼、野怪营地拉野堆叠、神符拾取分类、操作手速 (APM) 与阵亡时长
          </p>
        </div>

        <div class="flex items-center gap-4 text-xs font-mono">
          <span class="px-3 py-1 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            天辉总视野: {{ teamVisionStats.radiantObs }}假 / {{ teamVisionStats.radiantSen }}真 (排眼 {{ teamVisionStats.radiantDewards }})
          </span>
          <span class="px-3 py-1 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400">
            夜魇总视野: {{ teamVisionStats.direObs }}假 / {{ teamVisionStats.direSen }}真 (排眼 {{ teamVisionStats.direDewards }})
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
import { computed } from 'vue';
import {
  getHeroName,
  getHeroAvatar,
  getPositionMeta,
  getRuneName
} from '../../utils/dotaHelper';

const props = defineProps<{
  players: any[];
}>();

// 构建上/中/下三路对决分组
const laneMatchups = computed(() => {
  // Dota2 地图：
  // 下路：天辉优势路(lane=1) vs 夜魇劣势路(lane=3)
  // 中路：天辉中路(lane=2) vs 夜魇中路(lane=2)
  // 上路：天辉劣势路(lane=3) vs 夜魇优势路(lane=1)
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

  // OpenDota 地图分路编号 lane：1=下路 (天辉优势路 vs 夜魇劣势路), 2=中路, 3=上路 (天辉劣势路 vs 夜魇优势路)
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
