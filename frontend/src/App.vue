<template>
  <!-- 全站极光毛玻璃深空背景容器 -->
  <div class="relative min-h-screen bg-[#050811] text-slate-100 overflow-x-hidden selection:bg-cyan-400 selection:text-slate-950">
    <!-- 固定底层：深空科技微网格 + 天辉青与夜魇绯红动态极光光球 -->
    <div class="fixed inset-0 pointer-events-none z-0 overflow-hidden">
      <!-- 1. 精密微透坐标网格 -->
      <div class="absolute inset-0 aurora-grid-bg"></div>

      <!-- 2. 左上方：天辉苍翠极光漫反射光斑 -->
      <div
        class="absolute -top-40 -left-32 w-[560px] h-[560px] rounded-full bg-gradient-to-br from-cyan-500/18 via-teal-400/12 to-transparent blur-[120px] animate-aurora-slow"
      ></div>

      <!-- 3. 右上方：深空星云靛紫极光光斑 -->
      <div
        class="absolute top-12 -right-36 w-[520px] h-[520px] rounded-full bg-gradient-to-bl from-indigo-500/16 via-fuchsia-500/10 to-transparent blur-[130px] animate-aurora-reverse"
      ></div>

      <!-- 4. 左下方：翡翠绿极光光晕 -->
      <div
        class="absolute -bottom-48 left-1/4 w-[600px] h-[420px] rounded-full bg-gradient-to-tr from-emerald-500/12 via-cyan-500/8 to-transparent blur-[135px] animate-aurora-slow"
      ></div>

      <!-- 5. 右下方：夜魇绯红微光光斑 -->
      <div
        class="absolute -bottom-36 -right-24 w-[460px] h-[460px] rounded-full bg-gradient-to-tl from-rose-500/12 via-purple-500/8 to-transparent blur-[125px]"
      ></div>
    </div>

    <!-- 路由页面主体层 -->
    <div class="relative z-10">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
// 根组件：提供全局极光毛玻璃 (Aurora Glassmorphism) 动态星云背景层与路由出口
</script>
