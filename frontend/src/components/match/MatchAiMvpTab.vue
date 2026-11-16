<template>
  <div class="space-y-6">
    <!-- 顶部荣誉殿堂横幅与胜方5人猛男候选档案卡 -->
    <section class="aurora-glass rounded-2xl p-4 sm:p-6 relative overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-br from-amber-500/10 via-emerald-500/5 to-transparent pointer-events-none"></div>

      <div class="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-white/10">
        <div>
          <div class="flex items-center gap-2.5">
            <span class="text-xl sm:text-2xl">💪</span>
            <h3 class="text-base sm:text-lg font-black text-white tracking-wide">
              谁是天降猛男！ · DeepSeek AI 赛后功臣荣誉殿堂
            </h3>
            <span
              v-if="cachedFlag && analysisText"
              class="px-2.5 py-0.5 rounded-full bg-emerald-500/15 border border-emerald-400/40 text-emerald-300 text-[10px] font-mono font-bold"
            >
              ⚡ 数据库秒级缓存
            </span>
          </div>
          <p class="text-xs text-slate-300/80 mt-1.5">
            本局 <strong :class="radiantWin ? 'text-emerald-300' : 'text-amber-300'">{{ winningTeamName }}</strong> 获胜！将整场高精度录像遥测 JSON 交由 DeepSeek 大模型深度复盘，评出最功不可没的天降猛男。
          </p>
        </div>

        <div class="flex items-center gap-2.5 shrink-0">
          <button
            v-if="analysisText && !loading"
            @click="triggerAnalysis(true)"
            class="px-3.5 py-2 rounded-xl bg-white/[0.06] hover:bg-white/[0.12] border border-white/15 hover:border-amber-400/50 text-xs font-bold text-amber-300 transition-all flex items-center gap-1.5"
            title="忽略数据库已有缓存，重新调用 DeepSeek API 评选猛男"
          >
            <span>🔄 重新评定猛男</span>
          </button>
          <button
            v-if="!analysisText && !loading"
            @click="triggerAnalysis(false)"
            class="px-4 py-2 rounded-xl aurora-btn-primary text-xs font-black flex items-center gap-1.5"
          >
            <span>💪 立即评选猛男</span>
          </button>
        </div>
      </div>

      <!-- 胜方 5 名猛男候选快览席 -->
      <div class="relative z-10 mt-5">
        <div class="text-xs font-mono text-amber-300 font-bold mb-3 flex items-center justify-between">
          <span>🏆 胜方（{{ winningTeamName }}）五名功臣候选人本局档案：</span>
          <span class="text-[11px] text-slate-400">综合对线、经济碾压与团战贡献</span>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
          <div
            v-for="p in winningPlayers"
            :key="p.playerSlot"
            class="aurora-subcard rounded-xl p-3 border border-amber-500/20 hover:border-amber-400/50 transition-all flex flex-col justify-between"
          >
            <div class="flex items-center gap-2.5">
              <img :src="getHeroAvatar(p.heroId)" class="w-11 h-7 rounded-lg border border-slate-700 object-cover shrink-0" />
              <div class="min-w-0">
                <div class="text-xs font-black text-white truncate">{{ p.personaname }}</div>
                <div class="flex items-center gap-1 mt-0.5">
                  <span class="text-[10px] text-slate-300 font-semibold truncate">{{ getHeroName(p.heroId) }}</span>
                  <span :class="getPositionMeta(p.laneRole).badgeClass" class="text-[9px] px-1 rounded border font-mono">
                    {{ getPositionMeta(p.laneRole).short }}
                  </span>
                </div>
              </div>
            </div>

            <div class="mt-2.5 pt-2 border-t border-slate-800/80 grid grid-cols-2 gap-y-1 text-[10px] font-mono">
              <div>
                <span class="text-slate-500">KDA: </span>
                <span class="text-white font-bold">{{ p.kills }}/{{ p.deaths }}/{{ p.assists }}</span>
              </div>
              <div class="text-right">
                <span class="text-slate-500">参战: </span>
                <span class="text-cyan-300 font-bold">{{ p.tfParticipation }}%</span>
              </div>
              <div>
                <span class="text-slate-500">伤害: </span>
                <span class="text-amber-300 font-bold">{{ p.damageShare }}%</span>
              </div>
              <div class="text-right">
                <span class="text-slate-500">10m: </span>
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
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 加载中动画状态 -->
    <section
      v-if="loading"
      class="aurora-glass rounded-2xl p-8 sm:p-12 flex flex-col items-center justify-center text-center space-y-4"
    >
      <div class="relative flex items-center justify-center">
        <div class="w-16 h-16 border-4 border-amber-500/20 border-t-amber-400 rounded-full animate-spin"></div>
        <span class="absolute text-2xl">💪</span>
      </div>
      <div class="text-sm sm:text-base font-black text-amber-300 tracking-wider animate-pulse">
        DeepSeek 主裁判正在检阅 {{ winningTeamName }} 胜方五人的高光录像卷宗...
      </div>
      <p class="text-xs text-slate-400 font-mono max-w-md">
        正在交叉比对 10 分钟对线压制、GPM/XPM 发育雪球、团战参战率、推塔贡献与视野掌控数据，首次生成约需 10~25 秒，完成后将永久存入数据库供所有人秒开查看。
      </p>
    </section>

    <!-- 错误提示状态 -->
    <section
      v-else-if="errorMsg"
      class="aurora-glass rounded-2xl p-6 sm:p-8 border border-rose-500/40 text-center space-y-3"
    >
      <div class="text-3xl">⚠️</div>
      <div class="text-sm sm:text-base font-black text-rose-300">AI 荣誉殿堂连接异常</div>
      <p class="text-xs text-slate-300 font-mono max-w-xl mx-auto leading-relaxed">
        {{ errorMsg }}
      </p>
      <div class="pt-2">
        <button
          @click="triggerAnalysis(false)"
          class="px-5 py-2 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 border border-amber-400/50 text-amber-200 text-xs font-bold transition-all"
        >
          重试评选
        </button>
      </div>
    </section>

    <!-- AI 猛男评选报告正文展示区 -->
    <section
      v-else-if="analysisText"
      class="aurora-glass rounded-2xl p-5 sm:p-8 relative overflow-hidden"
    >
      <div class="flex items-center justify-between pb-4 mb-5 border-b border-white/10">
        <div class="flex items-center gap-2">
          <span class="w-2 h-5 bg-amber-400 rounded-full shadow-[0_0_8px_#fbbf24]"></span>
          <h4 class="text-sm sm:text-base font-black text-white">
            🏆 DeepSeek 天降猛男授勋卷宗（{{ winningTeamName }}功臣评选报告）
          </h4>
        </div>
        <span class="text-[11px] font-mono text-slate-400">
          对局 ID: {{ matchId }} · 已归档入库
        </span>
      </div>

      <!-- 结构化渲染 Markdown 内容 -->
      <div class="space-y-3 text-xs sm:text-sm text-slate-200 leading-relaxed font-sans" v-html="renderedHtml"></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { fetchMatchAiMvp } from '../../api/player';
import { getHeroName, getHeroAvatar, getPositionMeta } from '../../utils/dotaHelper';
import { renderAiMarkdown } from '../../utils/aiMarkdown';

const props = defineProps<{
  matchId: string;
  radiantWin: boolean;
  players: any[];
  initialAnalysis?: string | null;
}>();

const emit = defineEmits<{
  (e: 'analysisSaved', text: string): void;
}>();

const loading = ref(false);
const errorMsg = ref('');
const analysisText = ref<string>(props.initialAnalysis || '');
const cachedFlag = ref<boolean>(Boolean(props.initialAnalysis));

const winningTeamName = computed(() => (props.radiantWin ? '天辉阵营' : '夜魇阵营'));

const winningPlayers = computed(() => {
  return props.players.filter((p) => (props.radiantWin ? p.isRadiant : !p.isRadiant));
});

const renderedHtml = computed(() => {
  return renderAiMarkdown(analysisText.value, 'text-amber-400', 'text-emerald-300');
});

const triggerAnalysis = async (force: boolean) => {
  if (force) {
    // 强制重新评定时，先清空当前展示文本与缓存标记，后端会同步删除数据库旧缓存后重新调用 DeepSeek
    analysisText.value = '';
    cachedFlag.value = false;
    emit('analysisSaved', '');
  }
  loading.value = true;
  errorMsg.value = '';
  try {
    const res = await fetchMatchAiMvp(props.matchId, force);
    const data = res?.data;
    if (data?.success && data?.analysis) {
      analysisText.value = data.analysis;
      cachedFlag.value = Boolean(data.cached);
      emit('analysisSaved', data.analysis);
    } else {
      errorMsg.value = data?.msg || '未能获取到 AI 猛男评选结果，请检查后端 DeepSeek 配置。';
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.msg || e?.message || '请求后端 AI 猛男评选接口超时或异常';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (props.initialAnalysis && props.initialAnalysis.trim()) {
    analysisText.value = props.initialAnalysis;
    cachedFlag.value = true;
  } else {
    // 首次点进该标签页且数据库尚无记录时，自动向后端请求分析（若后端数据库已有则直接命中缓存返回）
    triggerAnalysis(false);
  }
});

watch(
  () => props.initialAnalysis,
  (val) => {
    if (val && val.trim()) {
      analysisText.value = val;
      cachedFlag.value = true;
    }
  }
);
</script>
