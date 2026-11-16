<template>
  <div class="text-slate-100 min-h-screen font-sans antialiased pb-20">
    <!-- 顶部极光毛玻璃导航栏 -->
    <header class="aurora-nav sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between gap-2">
        <div class="flex items-center space-x-2.5 sm:space-x-3.5 min-w-0">
          <div
            class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-cyan-400 via-teal-300 to-indigo-500 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-400/25 cursor-pointer shrink-0"
            @click="router.push('/')"
          >
            <div class="w-full h-full bg-[#060a15]/90 backdrop-blur-md rounded-[10px] flex items-center justify-center text-cyan-300 font-black text-sm sm:text-base">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2 truncate">
            <span class="text-base sm:text-xl font-black tracking-wide text-white cursor-pointer shrink-0" @click="router.push('/')">
              毒瘤榜<span class="bg-gradient-to-r from-cyan-300 to-emerald-300 bg-clip-text text-transparent">.GG</span>
            </span>
            <span class="hidden sm:inline-block text-[10px] uppercase tracking-widest px-2.5 py-0.5 rounded-full bg-amber-500/15 text-amber-200 font-bold border border-amber-400/35 backdrop-blur-md">
              HALL OF FAME · 特别鸣谢
            </span>
          </div>
        </div>

        <button
          @click="router.push('/')"
          class="px-3 sm:px-4 py-1.5 bg-white/[0.06] hover:bg-white/[0.12] text-slate-100 border border-white/15 hover:border-cyan-400/35 rounded-full text-xs font-semibold backdrop-blur-md transition-all shrink-0"
        >
          ← 返回首页
        </button>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="max-w-6xl mx-auto px-3 sm:px-6 lg:px-8 mt-6 sm:mt-10 space-y-6 sm:space-y-10">
      <!-- 顶部极光荣誉横幅 -->
      <section class="relative rounded-2xl sm:rounded-3xl aurora-glass border border-amber-400/35 p-5 sm:p-10 text-center overflow-hidden">
        <div class="absolute -top-24 left-1/3 w-96 h-48 bg-amber-400/15 blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 right-1/3 w-96 h-48 bg-rose-500/15 blur-3xl pointer-events-none"></div>

        <div class="relative z-10">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-400/15 border border-amber-300/35 text-amber-200 text-[11px] sm:text-xs font-bold mb-3 sm:mb-4 backdrop-blur-md">
            <span>🏆</span>
            <span>SPECIAL THANKS & TOXIC HALL OF FAME</span>
          </div>
          <h1 class="text-2xl sm:text-4xl font-black tracking-tight bg-gradient-to-r from-white via-amber-100 to-rose-200 bg-clip-text text-transparent">
            毒瘤榜 · 特别鸣谢荣誉殿堂
          </h1>
          <p class="text-xs sm:text-sm text-slate-300/85 mt-2.5 sm:mt-3 max-w-2xl mx-auto">
            每一局都算数，每一句圣经都永久载入史册。感谢以下名场面与官方认证为本榜提供源源不断的精神动力。
          </p>
        </div>
      </section>

      <!-- 三张典藏名场面展示区 -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <!-- 左侧：语录名场面 1 & 2 -->
        <div class="lg:col-span-5 space-y-6">
          <!-- 图 1：你们打不过我们的 -->
          <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 transition-all group">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-bold text-cyan-300 flex items-center gap-1.5">
                <span>💬</span> 典藏圣经 · 卷一
              </span>
              <span class="text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-slate-300">名言石刻</span>
            </div>
            <div
              class="rounded-xl overflow-hidden aurora-subcard p-4 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-1.png'"
            >
              <img
                src="/thanks/thanks-1.png"
                alt="你们打不过我们的"
                class="max-w-full h-auto rounded-lg shadow-lg group-hover:scale-[1.02] transition-transform"
              />
            </div>
            <p class="text-xs text-cyan-200/80 mt-3 font-mono text-center">
              “你们打不过我们的”
            </p>
          </div>

          <!-- 图 2：这盘早知道我玩PUCK了 -->
          <div class="aurora-glass aurora-glass-hover rounded-2xl p-5 transition-all group">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-bold text-fuchsia-300 flex items-center gap-1.5">
                <span>🔥</span> 典藏圣经 · 卷二
              </span>
              <span class="text-[10px] font-mono px-2.5 py-0.5 rounded-full bg-white/[0.06] border border-white/10 text-slate-300">赛后复盘连击</span>
            </div>
            <div
              class="rounded-xl overflow-hidden aurora-subcard p-4 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-2.png'"
            >
              <img
                src="/thanks/thanks-2.png"
                alt="这哪里是压力大 这盘早知道我玩PUCK了"
                class="max-w-full h-auto rounded-lg shadow-lg group-hover:scale-[1.02] transition-transform"
              />
            </div>
            <p class="text-xs text-fuchsia-200/80 mt-3 font-mono text-center">
              “这哪里是压力大 · 这盘早知道我玩PUCK了”
            </p>
          </div>
        </div>

        <!-- 右侧：图 3 官方毒瘤认证证书 -->
        <div class="lg:col-span-7">
          <div class="aurora-glass border border-amber-400/40 rounded-2xl p-6 transition-all group relative overflow-hidden">
            <div class="absolute -top-16 -right-16 w-52 h-52 bg-amber-400/15 rounded-full blur-3xl pointer-events-none"></div>
            <div class="relative z-10 flex items-center justify-between mb-4">
              <span class="text-sm font-black text-amber-300 flex items-center gap-2">
                <span>🏅</span> CERTIFICATE OF TOXICITY · 官方毒瘤认证
              </span>
              <span class="text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-amber-400/15 text-amber-200 border border-amber-300/35">
                点击图片可放大检阅
              </span>
            </div>
            <div
              class="relative z-10 rounded-2xl overflow-hidden aurora-subcard p-3 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-3.jpg'"
            >
              <img
                src="/thanks/thanks-3.jpg"
                alt="毒瘤认证证书"
                class="w-full h-auto rounded-xl shadow-2xl group-hover:scale-[1.01] transition-transform"
              />
            </div>
            <div class="relative z-10 mt-4 flex flex-wrap items-center justify-between text-xs text-slate-300 font-mono gap-2">
              <span>证书编号: TOXIC-CERT-001</span>
              <span class="text-amber-300 font-bold">毒瘤榜官方认证委员会 永久收录</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 点击图片全屏极光毛玻璃预览弹层 -->
    <div
      v-if="previewImg"
      class="fixed inset-0 z-50 bg-black/85 backdrop-blur-xl flex items-center justify-center p-4 cursor-zoom-out"
      @click="previewImg = null"
    >
      <img :src="previewImg" class="max-w-full max-h-[92vh] rounded-2xl shadow-2xl border border-white/20" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const previewImg = ref<string | null>(null);
</script>
