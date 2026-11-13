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

    <!-- 2. 选手技能与物品伤害构成拆解 (Damage Inflictor Breakdown) -->
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
import { computed } from 'vue';
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
}>();

const heroMap = heroConstants as Record<string, any>;

const radiantPlayers = computed(() => props.players.filter((p) => p.isRadiant));
const direPlayers = computed(() => props.players.filter((p) => !p.isRadiant));

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
