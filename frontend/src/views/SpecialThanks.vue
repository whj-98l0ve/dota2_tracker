<template>
  <div class="bg-[#0b0e14] text-slate-200 min-h-screen font-sans antialiased pb-20 selection:bg-cyan-500 selection:text-white">
    <!-- 顶部导航栏 -->
    <header class="border-b border-slate-800/80 bg-[#0c1220]/90 backdrop-blur sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div
            class="w-10 h-10 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 p-[1.5px] flex items-center justify-center shadow-lg shadow-cyan-500/20 cursor-pointer"
            @click="router.push('/')"
          >
            <div class="w-full h-full bg-[#0b0f19] rounded-[7px] flex items-center justify-center text-cyan-400 font-black">
              毒
            </div>
          </div>
          <div class="flex items-baseline space-x-2">
            <span class="text-xl font-black tracking-wide text-white cursor-pointer" @click="router.push('/')">
              毒瘤榜<span class="text-cyan-400">.GG</span>
            </span>
            <span class="text-[10px] uppercase tracking-wider px-2 py-0.5 rounded bg-amber-500/15 text-amber-400 font-bold border border-amber-500/30">
              HALL OF FAME · 特别鸣谢
            </span>
          </div>
        </div>

        <button
          @click="router.push('/')"
          class="px-3.5 py-1.5 bg-[#121721] hover:bg-slate-800 text-slate-200 border border-slate-700/80 rounded-lg text-xs font-semibold transition-colors"
        >
          ← 返回毒瘤榜首页
        </button>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 mt-10 space-y-10">
      <!-- 顶部荣誉横幅 -->
      <section class="relative rounded-3xl bg-gradient-to-br from-[#121721] via-[#131b2e] to-[#0f141d] border border-amber-500/30 p-8 sm:p-10 text-center shadow-2xl overflow-hidden">
        <div class="absolute -top-24 left-1/2 -translate-x-1/2 w-96 h-48 bg-amber-500/10 blur-3xl pointer-events-none"></div>
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-bold mb-4">
          <span>🏆</span>
          <span>SPECIAL THANKS & TOXIC HALL OF FAME</span>
        </div>
        <h1 class="text-3xl sm:text-4xl font-black text-white tracking-tight">
          毒瘤榜 · 特别鸣谢荣誉殿堂
        </h1>
        <p class="text-sm text-slate-400 mt-3 max-w-2xl mx-auto">
          每一局都算数，每一句圣经都永久载入史册。感谢以下名场面与官方认证为本榜提供源源不断的精神动力。
        </p>
      </section>

      <!-- 三张典藏名场面展示区 -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <!-- 左侧：语录名场面 1 & 2 -->
        <div class="lg:col-span-5 space-y-6">
          <!-- 图 1：你们打不过我们的 -->
          <div class="bg-[#121721] border border-slate-800/90 hover:border-cyan-500/40 rounded-2xl p-5 shadow-xl transition-all group">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-bold text-cyan-400 flex items-center gap-1.5">
                <span>💬</span> 典藏圣经 · 卷一
              </span>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">名言石刻</span>
            </div>
            <div
              class="rounded-xl overflow-hidden bg-[#0b0e14] border border-slate-800 p-4 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-1.png'"
            >
              <img
                src="/thanks/thanks-1.png"
                alt="你们打不过我们的"
                class="max-w-full h-auto rounded-lg shadow-md group-hover:scale-[1.02] transition-transform"
              />
            </div>
            <p class="text-xs text-slate-400 mt-3 font-mono text-center">
              “你们打不过我们的”
            </p>
          </div>

          <!-- 图 2：这盘早知道我玩PUCK了 -->
          <div class="bg-[#121721] border border-slate-800/90 hover:border-purple-500/40 rounded-2xl p-5 shadow-xl transition-all group">
            <div class="flex items-center justify-between mb-3">
              <span class="text-xs font-bold text-purple-400 flex items-center gap-1.5">
                <span>🔥</span> 典藏圣经 · 卷二
              </span>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400">赛后复盘连击</span>
            </div>
            <div
              class="rounded-xl overflow-hidden bg-[#0b0e14] border border-slate-800 p-4 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-2.png'"
            >
              <img
                src="/thanks/thanks-2.png"
                alt="这哪里是压力大 这盘早知道我玩PUCK了"
                class="max-w-full h-auto rounded-lg shadow-md group-hover:scale-[1.02] transition-transform"
              />
            </div>
            <p class="text-xs text-slate-400 mt-3 font-mono text-center">
              “这哪里是压力大 · 这盘早知道我玩PUCK了”
            </p>
          </div>
        </div>

        <!-- 右侧：图 3 官方毒瘤认证证书 -->
        <div class="lg:col-span-7">
          <div class="bg-[#121721] border border-amber-500/40 rounded-2xl p-6 shadow-2xl transition-all group">
            <div class="flex items-center justify-between mb-4">
              <span class="text-sm font-black text-amber-400 flex items-center gap-2">
                <span>🏅</span> CERTIFICATE OF TOXICITY · 官方毒瘤认证
              </span>
              <span class="text-[11px] font-mono px-2.5 py-0.5 rounded bg-amber-500/15 text-amber-300 border border-amber-500/30">
                点击图片可放大检阅
              </span>
            </div>
            <div
              class="rounded-2xl overflow-hidden bg-[#0b0e14] border border-slate-800 p-3 flex items-center justify-center cursor-zoom-in"
              @click="previewImg = '/thanks/thanks-3.jpg'"
            >
              <img
                src="/thanks/thanks-3.jpg"
                alt="毒瘤认证证书"
                class="w-full h-auto rounded-xl shadow-2xl group-hover:scale-[1.01] transition-transform"
              />
            </div>
            <div class="mt-4 flex flex-wrap items-center justify-between text-xs text-slate-400 font-mono gap-2">
              <span>证书编号: TOXIC-CERT-001</span>
              <span class="text-amber-400">毒瘤榜官方认证委员会 永久收录</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 点击图片全屏预览弹层 -->
    <div
      v-if="previewImg"
      class="fixed inset-0 z-50 bg-black/85 backdrop-blur-sm flex items-center justify-center p-4 cursor-zoom-out"
      @click="previewImg = null"
    >
      <img :src="previewImg" class="max-w-full max-h-[92vh] rounded-2xl shadow-2xl border border-slate-700" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const previewImg = ref<string | null>(null);
</script>
