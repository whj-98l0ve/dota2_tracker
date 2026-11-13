<template>
  <div class="space-y-6">
    <!-- 1. 关键团战深度复盘面板 (Teamfight Telemetry) -->
    <section class="bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
      <div class="pb-4 border-b border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <h3 class="text-base font-black text-white flex items-center gap-2">
            <span class="w-2 h-5 bg-rose-500 rounded-full"></span>
            关键团战复盘分析器 (Teamfights Replay Breakdown)
          </h3>
          <p class="text-xs text-slate-400 mt-1">
            全场共爆发 <strong class="text-cyan-400">{{ teamfights.length }}</strong> 波关键团战，点击任意团战卡片查看该波团战 10 名选手的技能释放、输出与经济得失
          </p>
        </div>
      </div>

      <div v-if="teamfights.length > 0" class="mt-5 space-y-5">
        <!-- 团战列表选择器 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          <button
            v-for="(tf, idx) in teamfights"
            :key="idx"
            @click="selectedTfIndex = idx"
            :class="[
              selectedTfIndex === idx
                ? 'bg-cyan-950/30 border-cyan-400 ring-1 ring-cyan-400/50'
                : 'bg-[#0b0e14]/80 border-slate-800 hover:border-slate-700'
            ]"
            class="p-3.5 rounded-xl border text-left transition-all font-mono"
          >
            <div class="flex items-center justify-between text-xs mb-2">
              <span class="font-black text-white">⚔️ 团战 #{{ idx + 1 }}</span>
              <span class="text-[11px] text-cyan-400">{{ formatGameTime(tf.start) }} - {{ formatGameTime(tf.end) }}</span>
            </div>
            <div class="flex items-center justify-between text-xs my-1.5">
              <span class="text-emerald-400 font-bold">天辉 {{ tf.radiantKills }} 杀</span>
              <span class="text-slate-500 text-[10px]">阵亡 {{ tf.deaths }} 人</span>
              <span class="text-rose-400 font-bold">夜魇 {{ tf.direKills }} 杀</span>
            </div>
            <div class="pt-2 border-t border-slate-800/70 flex items-center justify-between text-[11px]">
              <span class="text-slate-400">经济转换:</span>
              <span :class="tf.goldSwing >= 0 ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'">
                {{ tf.goldSwing >= 0 ? `天辉 +${tf.goldSwing.toLocaleString()}` : `夜魇 +${Math.abs(tf.goldSwing).toLocaleString()}` }}
              </span>
            </div>
          </button>
        </div>

        <!-- 当前选中团战的 10 人明细表 -->
        <div v-if="activeTf" class="bg-[#0b0e14] border border-slate-800 rounded-xl overflow-hidden">
          <div class="px-5 py-3 bg-[#121721] border-b border-slate-800 flex flex-wrap items-center justify-between gap-4 text-xs font-mono">
            <div class="flex items-center gap-3">
              <span class="px-2.5 py-1 rounded bg-cyan-500/20 text-cyan-300 font-black">
                团战 #{{ selectedTfIndex + 1 }} 详情 ({{ formatGameTime(activeTf.start) }} ~ {{ formatGameTime(activeTf.end) }})
              </span>
              <span class="text-slate-400">持续 {{ activeTf.end - activeTf.start }} 秒</span>
            </div>
            <div class="flex items-center gap-6">
              <span>天辉团战总伤: <strong class="text-emerald-400">{{ activeTf.radiantDmg.toLocaleString() }}</strong></span>
              <span>夜魇团战总伤: <strong class="text-rose-400">{{ activeTf.direDmg.toLocaleString() }}</strong></span>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs font-mono">
              <thead class="bg-[#0b0e14] text-slate-500 border-b border-slate-800 text-[10px]">
                <tr>
                  <th class="py-2.5 px-4">参战英雄 / 选手</th>
                  <th class="py-2.5 px-3 text-center">状态</th>
                  <th class="py-2.5 px-3">团战伤害</th>
                  <th class="py-2.5 px-3 text-center">团战治疗</th>
                  <th class="py-2.5 px-3 text-center">经济 / 经验变动</th>
                  <th class="py-2.5 px-3">释放技能 (次数)</th>
                  <th class="py-2.5 px-3">使用物品</th>
                  <th class="py-2.5 px-3">击杀目标</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800/50">
                <tr
                  v-for="(row, rIdx) in activeTf.playerRows"
                  :key="rIdx"
                  :class="row.player?.isRadiant ? 'hover:bg-emerald-500/5' : 'hover:bg-rose-500/5'"
                >
                  <td class="py-2.5 px-4">
                    <div class="flex items-center gap-2.5">
                      <span :class="row.player?.isRadiant ? 'bg-emerald-400' : 'bg-rose-500'" class="w-1.5 h-6 rounded-full"></span>
                      <img :src="getHeroAvatar(row.player?.heroId)" class="w-9 h-6 rounded object-cover" />
                      <div>
                        <div class="font-bold text-white text-[11px]">{{ getHeroName(row.player?.heroId) }}</div>
                        <div class="text-[10px] text-slate-500">{{ row.player?.personaname }}</div>
                      </div>
                    </div>
                  </td>

                  <td class="py-2.5 px-3 text-center">
                    <div class="flex flex-col items-center gap-1">
                      <span v-if="row.deaths > 0" class="px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-400 border border-rose-500/30 text-[10px] font-bold">
                        💀 阵亡{{ row.deaths > 1 ? `x${row.deaths}` : '' }}
                      </span>
                      <span v-else class="text-[10px] text-emerald-400">存活</span>
                      <span v-if="row.buybacks > 0" class="px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-500/40 text-[9px] font-black">
                        💰 买活
                      </span>
                    </div>
                  </td>

                  <td class="py-2.5 px-3 min-w-[140px]">
                    <div class="flex justify-between text-[11px] mb-1">
                      <span class="text-white font-bold">{{ row.damage.toLocaleString() }}</span>
                    </div>
                    <div class="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                      <div
                        :class="row.player?.isRadiant ? 'bg-emerald-400' : 'bg-rose-500'"
                        class="h-full rounded-full"
                        :style="{ width: Math.min(100, (row.damage * 100) / (activeTf.maxPlayerDmg || 1)) + '%' }"
                      ></div>
                    </div>
                  </td>

                  <td class="py-2.5 px-3 text-center text-emerald-300">
                    {{ row.healing > 0 ? `+${row.healing.toLocaleString()}` : '-' }}
                  </td>

                  <td class="py-2.5 px-3 text-center">
                    <div :class="row.goldDelta >= 0 ? 'text-amber-400 font-bold' : 'text-rose-400'">
                      {{ row.goldDelta >= 0 ? `+${row.goldDelta}` : row.goldDelta }} G
                    </div>
                    <div class="text-[10px] text-sky-400">+{{ row.xpDelta }} XP</div>
                  </td>

                  <td class="py-2.5 px-3">
                    <div class="flex flex-wrap gap-1">
                      <div
                        v-for="ab in row.abilities"
                        :key="ab.key"
                        class="relative w-7 h-7 rounded bg-[#121721] border border-slate-700 overflow-hidden flex items-center justify-center"
                        :title="`${ab.name} 释放 ${ab.count} 次`"
                      >
                        <img v-if="ab.img" :src="ab.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                        <span v-else class="text-[8px] text-slate-300">技</span>
                        <span class="absolute bottom-0 right-0 bg-black/80 text-cyan-300 text-[9px] px-1 leading-tight font-bold">
                          {{ ab.count }}
                        </span>
                      </div>
                    </div>
                  </td>

                  <td class="py-2.5 px-3">
                    <div class="flex flex-wrap gap-1">
                      <div
                        v-for="it in row.items"
                        :key="it.key"
                        class="relative w-8 h-6 rounded bg-[#121721] border border-slate-700 overflow-hidden"
                        :title="`${it.name} 使用 ${it.count} 次`"
                      >
                        <img v-if="it.img" :src="it.img" class="w-full h-full object-cover" @error="hideBrokenImg" />
                        <span class="absolute bottom-0 right-0 bg-black/80 text-amber-300 text-[9px] px-0.5 leading-tight font-bold">
                          {{ it.count }}
                        </span>
                      </div>
                    </div>
                  </td>

                  <td class="py-2.5 px-3">
                    <div class="flex flex-wrap gap-1">
                      <img
                        v-for="(kh, kIdx) in row.killedHeroes"
                        :key="kIdx"
                        :src="kh.img"
                        :title="`击杀 ${kh.name}`"
                        class="w-8 h-5 rounded border border-rose-500/60 object-cover"
                      />
                      <span v-if="row.killedHeroes.length === 0" class="text-slate-600">-</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div v-else class="py-10 text-center text-slate-500 text-xs font-mono">
        该对局无团战遥测记录
      </div>
    </section>

    <!-- 2. 赛场建筑损毁状态与全局里程碑事件轴 -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- 左侧：防御塔与兵营存活状态微缩沙盘 -->
      <section class="lg:col-span-5 bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl flex flex-col justify-between">
        <div>
          <div class="pb-4 border-b border-slate-800/80">
            <h3 class="text-base font-black text-white flex items-center gap-2">
              <span class="w-2 h-5 bg-emerald-400 rounded-full"></span>
              终局建筑存活状态 (Structures Status)
            </h3>
            <p class="text-xs text-slate-400 mt-1">
              解析 16 位防御塔与兵营状态码，展示终局双方建筑完整度
            </p>
          </div>

          <div class="mt-5 space-y-4 font-mono text-xs">
            <!-- 天辉建筑存活卡 -->
            <div class="p-4 rounded-xl bg-[#0b0e14] border border-emerald-500/20">
              <div class="flex items-center justify-between mb-3">
                <span class="text-emerald-400 font-black">✦ 天辉建筑 (RADIANT)</span>
                <span class="text-slate-400">
                  存活塔: <strong class="text-emerald-400">{{ radiantTowers.aliveCount }}/11</strong> |
                  兵营: <strong class="text-emerald-400">{{ radiantRax.aliveCount }}/6</strong>
                </span>
              </div>
              <div class="grid grid-cols-3 gap-2 text-[11px]">
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">上路 (Top)</div>
                  <div class="flex gap-1">
                    <span :class="radiantTowers.topT1 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="radiantTowers.topT2 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="radiantTowers.topT3 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="radiantRax.topMelee ? 'text-emerald-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="radiantRax.topRanged ? 'text-emerald-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">中路 (Mid)</div>
                  <div class="flex gap-1">
                    <span :class="radiantTowers.midT1 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="radiantTowers.midT2 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="radiantTowers.midT3 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="radiantRax.midMelee ? 'text-emerald-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="radiantRax.midRanged ? 'text-emerald-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">下路 (Bot)</div>
                  <div class="flex gap-1">
                    <span :class="radiantTowers.botT1 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="radiantTowers.botT2 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="radiantTowers.botT3 ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="radiantRax.botMelee ? 'text-emerald-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="radiantRax.botRanged ? 'text-emerald-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 夜魇建筑存活卡 -->
            <div class="p-4 rounded-xl bg-[#0b0e14] border border-rose-500/20">
              <div class="flex items-center justify-between mb-3">
                <span class="text-rose-400 font-black">✦ 夜魇建筑 (DIRE)</span>
                <span class="text-slate-400">
                  存活塔: <strong class="text-rose-400">{{ direTowers.aliveCount }}/11</strong> |
                  兵营: <strong class="text-rose-400">{{ direRax.aliveCount }}/6</strong>
                </span>
              </div>
              <div class="grid grid-cols-3 gap-2 text-[11px]">
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">上路 (Top)</div>
                  <div class="flex gap-1">
                    <span :class="direTowers.topT1 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="direTowers.topT2 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="direTowers.topT3 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="direRax.topMelee ? 'text-rose-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="direRax.topRanged ? 'text-rose-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">中路 (Mid)</div>
                  <div class="flex gap-1">
                    <span :class="direTowers.midT1 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="direTowers.midT2 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="direTowers.midT3 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="direRax.midMelee ? 'text-rose-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="direRax.midRanged ? 'text-rose-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
                <div class="p-2 rounded bg-[#121721] border border-slate-800">
                  <div class="text-slate-400 mb-1">下路 (Bot)</div>
                  <div class="flex gap-1">
                    <span :class="direTowers.botT1 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T1</span>
                    <span :class="direTowers.botT2 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T2</span>
                    <span :class="direTowers.botT3 ? 'bg-rose-500/20 text-rose-300 border-rose-500/40' : 'bg-slate-900 text-slate-600 border-slate-800 line-through'" class="px-1.5 py-0.5 rounded border">T3</span>
                  </div>
                  <div class="flex gap-1 mt-1 text-[10px]">
                    <span :class="direRax.botMelee ? 'text-rose-400' : 'text-slate-600 line-through'">近战营</span>
                    <span :class="direRax.botRanged ? 'text-rose-400' : 'text-slate-600 line-through'">远程营</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-4 pt-3 border-t border-slate-800/80 text-[11px] text-slate-500 font-mono text-center">
          推塔与高地兵营状态来自录像终局快照
        </div>
      </section>

      <!-- 右侧：全局关键战略事件时间轴 (Objectives & Buybacks) -->
      <section class="lg:col-span-7 bg-[#121721] border border-slate-800/80 rounded-2xl p-6 shadow-xl">
        <div class="pb-4 border-b border-slate-800/80 flex items-center justify-between">
          <div>
            <h3 class="text-base font-black text-white flex items-center gap-2">
              <span class="w-2 h-5 bg-amber-400 rounded-full"></span>
              赛场战略里程碑时间轴 (Objectives & Buybacks Log)
            </h3>
            <p class="text-xs text-slate-400 mt-1">
              一血、肉山击杀、不朽之守护拾取、防御塔兵营摧毁、信使阵亡与关键买活完整时间线
            </p>
          </div>
          <span class="text-xs font-mono text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded border border-cyan-800/50">
            共 {{ timelineEvents.length }} 个节点
          </span>
        </div>

        <div class="mt-4 max-h-[420px] overflow-y-auto pr-2 space-y-2 font-mono text-xs">
          <div
            v-for="(ev, idx) in timelineEvents"
            :key="idx"
            class="flex items-center justify-between p-2.5 rounded-xl bg-[#0b0e14]/90 border border-slate-800/80 hover:border-slate-700 transition-colors"
          >
            <div class="flex items-center gap-3">
              <span class="px-2 py-0.5 rounded bg-slate-800 text-cyan-300 font-bold text-[11px] w-14 text-center">
                {{ formatGameTime(ev.time) }}
              </span>
              <span class="text-base">{{ ev.icon }}</span>
              <img v-if="ev.heroImg" :src="ev.heroImg" class="w-8 h-5 rounded object-cover" />
              <span :class="ev.colorClass" class="font-semibold">{{ ev.text }}</span>
            </div>
            <span class="text-[10px] px-2 py-0.5 rounded border border-slate-800 text-slate-400">
              {{ ev.tag }}
            </span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  getHeroName,
  getHeroAvatar,
  getHeroByNpcName,
  getAbilityMeta,
  getItemMeta,
  getBuildingCnName,
  decodeTowerStatus,
  decodeBarracksStatus,
  formatGameTime
} from '../../utils/dotaHelper';

const props = defineProps<{
  rawJson: any;
  players: any[];
}>();

const selectedTfIndex = ref(0);

const hideBrokenImg = (e: Event) => {
  (e.target as HTMLElement).style.display = 'none';
};

// 解析团战数组
const teamfights = computed(() => {
  const rawTfs: any[] = Array.isArray(props.rawJson?.teamfights) ? props.rawJson.teamfights : [];
  return rawTfs.map((tf) => {
    const tfPlayers: any[] = Array.isArray(tf.players) ? tf.players : [];
    let radiantKills = 0;
    let direKills = 0;
    let radiantDmg = 0;
    let direDmg = 0;
    let goldSwing = 0;
    let maxPlayerDmg = 100;

    const playerRows = tfPlayers.map((tp, idx) => {
      const p = props.players[idx] || {};
      const isRad = idx < 5;
      const dmg = Number(tp.damage || 0);
      const deaths = Number(tp.deaths || 0);
      const goldDelta = Number(tp.gold_delta || 0);
      if (dmg > maxPlayerDmg) maxPlayerDmg = dmg;

      if (isRad) {
        radiantDmg += dmg;
        direKills += deaths;
        goldSwing += goldDelta;
      } else {
        direDmg += dmg;
        radiantKills += deaths;
        goldSwing -= goldDelta;
      }

      // 技能使用列表
      const abilities = Object.entries(tp.ability_uses || {})
        .map(([k, count]) => {
          const meta = getAbilityMeta(k);
          return {
            key: k,
            name: meta?.name || k,
            img: meta?.img || '',
            count: Number(count)
          };
        })
        .sort((a, b) => b.count - a.count)
        .slice(0, 6);

      // 物品使用列表
      const items = Object.entries(tp.item_uses || {})
        .map(([k, count]) => {
          const meta = getItemMeta(k);
          return {
            key: k,
            name: meta?.name || k,
            img: meta?.img || '',
            count: Number(count)
          };
        })
        .filter((it) => it.img)
        .slice(0, 5);

      // 击杀敌方英雄列表
      const killedHeroes: Array<{ name: string; img: string }> = [];
      Object.entries(tp.killed || {}).forEach(([npcName, cnt]) => {
        if (npcName.startsWith('npc_dota_hero_')) {
          const hm = getHeroByNpcName(npcName);
          if (hm) {
            for (let i = 0; i < Number(cnt); i++) {
              killedHeroes.push({
                name: hm.localized_name,
                img: getHeroAvatar(hm.id)
              });
            }
          }
        }
      });

      return {
        player: p,
        damage: dmg,
        healing: Number(tp.healing || 0),
        deaths,
        buybacks: Number(tp.buybacks || 0),
        goldDelta,
        xpDelta: Number(tp.xp_delta || 0),
        abilities,
        items,
        killedHeroes
      };
    });

    return {
      start: tf.start,
      end: tf.end,
      deaths: tf.deaths,
      radiantKills,
      direKills,
      radiantDmg,
      direDmg,
      goldSwing,
      maxPlayerDmg,
      playerRows
    };
  });
});

const activeTf = computed(() => teamfights.value[selectedTfIndex.value] || null);

// 解析防御塔与兵营位掩码
const radiantTowers = computed(() => decodeTowerStatus(props.rawJson?.tower_status_radiant));
const direTowers = computed(() => decodeTowerStatus(props.rawJson?.tower_status_dire));
const radiantRax = computed(() => decodeBarracksStatus(props.rawJson?.barracks_status_radiant));
const direRax = computed(() => decodeBarracksStatus(props.rawJson?.barracks_status_dire));

// 构建全局事件轴 (合并 objectives 与选手买活记录)
const timelineEvents = computed(() => {
  const list: Array<{
    time: number;
    icon: string;
    heroImg?: string;
    text: string;
    colorClass: string;
    tag: string;
  }> = [];

  const findBySlot = (slot: number | undefined) =>
    props.players.find((p) => Number(p.playerSlot) === Number(slot));

  const objectives: any[] = Array.isArray(props.rawJson?.objectives) ? props.rawJson.objectives : [];
  objectives.forEach((obj) => {
    const time = Number(obj.time || 0);
    if (obj.type === 'CHAT_MESSAGE_FIRSTBLOOD') {
      const killer = findBySlot(obj.player_slot);
      const victim = findBySlot(obj.victim_player_slot);
      list.push({
        time,
        icon: '🩸',
        heroImg: killer ? getHeroAvatar(killer.heroId) : undefined,
        text: `${killer ? `${getHeroName(killer.heroId)} (${killer.personaname})` : '选手'} 拿下一血，击杀了 ${victim ? getHeroName(victim.heroId) : '敌方英雄'}`,
        colorClass: 'text-rose-400',
        tag: '第一滴血'
      });
    } else if (obj.type === 'CHAT_MESSAGE_ROSHAN_KILL') {
      const isRad = obj.team === 2;
      list.push({
        time,
        icon: '🐉',
        text: `${isRad ? '天辉阵营' : '夜魇阵营'} 击杀了肉山 (Roshan)！`,
        colorClass: isRad ? 'text-emerald-400' : 'text-rose-400',
        tag: '肉山讨伐'
      });
    } else if (obj.type === 'CHAT_MESSAGE_AEGIS') {
      const holder = findBySlot(obj.player_slot);
      list.push({
        time,
        icon: '🛡️',
        heroImg: holder ? getHeroAvatar(holder.heroId) : undefined,
        text: `${holder ? `${getHeroName(holder.heroId)} (${holder.personaname})` : '英雄'} 拾取了不朽之守护 (Aegis)`,
        colorClass: 'text-amber-300',
        tag: '不朽盾'
      });
    } else if (obj.type === 'building_kill') {
      const bInfo = getBuildingCnName(obj.key);
      const killer = findBySlot(obj.player_slot);
      list.push({
        time,
        icon: '🏰',
        heroImg: killer ? getHeroAvatar(killer.heroId) : undefined,
        text: `${bInfo.name} 被摧毁 ${killer ? `(终结者: ${getHeroName(killer.heroId)})` : ''}`,
        colorClass: bInfo.isRadiantBuilding ? 'text-rose-400' : 'text-emerald-400',
        tag: '建筑推进'
      });
    } else if (obj.type === 'CHAT_MESSAGE_COURIER_LOST') {
      const isRadLost = obj.team === 2;
      list.push({
        time,
        icon: '📦',
        text: `${isRadLost ? '天辉' : '夜魇'} 信使被击杀`,
        colorClass: 'text-amber-400',
        tag: '信使击杀'
      });
    }
  });

  // 加入所有选手买活事件
  props.players.forEach((p) => {
    const bbLog: any[] = Array.isArray(p.raw?.buyback_log) ? p.raw.buyback_log : [];
    bbLog.forEach((bb) => {
      list.push({
        time: Number(bb.time || 0),
        icon: '💰',
        heroImg: getHeroAvatar(p.heroId),
        text: `${getHeroName(p.heroId)} (${p.personaname}) 买活重返战场！`,
        colorClass: 'text-amber-300',
        tag: '英雄买活'
      });
    });
  });

  return list.sort((a, b) => a.time - b.time);
});
</script>
