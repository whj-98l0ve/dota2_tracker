<template>
  <div class="dota-container">
    <!-- 顶部管理栏：录入关注选手ID & 切换选手 -->
    <a-card class="header-bar" :bordered="false">
      <div class="flex-between">
        <div class="brand">
          <span class="title">DOTA2 选手高阶数据监控台</span>
          <a-select
            v-model:value="currentAccountId"
            style="width: 240px; margin-left: 20px"
            placeholder="选择已关注选手"
            @change="loadDashboard"
          >
            <a-select-option v-for="p in playerList" :key="p.accountId" :value="p.accountId">
              {{ p.personaName || p.accountId }} ({{ p.syncStatus === 'SYNCING' ? '同步中...' : '已就绪' }})
            </a-select-option>
          </a-select>
          <a-button type="link" @click="manualRefresh" :loading="loading">刷新最新战绩</a-button>
        </div>

        <a-space>
          <a-input
            v-model:value="newAccountId"
            placeholder="输入32位Steam ID (如 290810906)"
            style="width: 240px"
          />
          <a-button type="primary" @click="handleRegisterPlayer" :loading="registering">
            + 录入并抓取50把
          </a-button>
        </a-space>
      </div>
    </a-card>

    <a-spin :spinning="loading" tip="正在加载高精度对局数据...">
      <div v-if="dashboardData" class="main-content">
        <!-- 选手个人信息与视图切换开关 -->
        <a-card class="profile-card" :bordered="false">
          <div class="flex-between">
            <div class="player-info">
              <a-avatar :size="64" :src="dashboardData.playerInfo.avatarUrl" />
              <div class="player-meta">
                <h2>
                  {{ dashboardData.playerInfo.personaName || '未知选手' }}
                  <a-tag color="gold">段位代码: {{ dashboardData.playerInfo.rankTier || '未校准' }}</a-tag>
                  <a-tag v-if="dashboardData.playerInfo.syncStatus === 'SYNCING'" color="processing">
                    后台正在拉取解析录像...
                  </a-tag>
                </h2>
                <p>Steam 32位 ID: {{ dashboardData.playerInfo.accountId }}</p>
              </div>
            </div>

            <!-- 维度切换：近20把 vs 生涯/全量总体 -->
            <a-radio-group v-model:value="viewMode" button-style="solid" size="large">
              <a-radio-button value="recent20">近 20 把高精数据</a-radio-button>
              <a-radio-button value="career">生涯总体 & 本地全量库</a-radio-button>
            </a-radio-group>
          </div>
        </a-card>

        <!-- 第一行：核心指标卡片 (KDA、单排/组排/总胜率、参战率、10分钟线优率) -->
        <a-row :gutter="16" style="margin-top: 16px">
          <!-- 1. KDA 表现 -->
          <a-col :span="6">
            <a-card title="场均 KDA 表现" :bordered="false" class="metric-card">
              <div class="big-num">{{ activeStats.kdaRatio || 0 }} <span class="sub-text">KDA</span></div>
              <div class="detail-row">
                <span class="k">{{ activeStats.avgKills || 0 }} 杀</span> /
                <span class="d">{{ activeStats.avgDeaths || 0 }} 死</span> /
                <span class="a">{{ activeStats.avgAssists || 0 }} 助</span>
              </div>
              <div class="foot-note">统计样本：{{ activeStats.totalGames || 0 }} 场高精对局</div>
            </a-card>
          </a-col>

          <!-- 2. 胜率细分 (总胜率 / 单排 / 组排) -->
          <a-col :span="6">
            <a-card title="胜率结构 (总 / 单排 / 组排)" :bordered="false" class="metric-card">
              <div class="big-num" :style="{ color: winRateColor(displayWinRate.totalWinRate) }">
                {{ displayWinRate.totalWinRate }}%
                <span class="sub-text">({{ displayWinRate.totalGames }}场)</span>
              </div>
              <div class="detail-row">
                <span>单排: <b>{{ displayWinRate.soloWinRate }}%</b> ({{ displayWinRate.soloGames }}场)</span>
                <a-divider type="vertical" />
                <span>组排: <b>{{ displayWinRate.partyWinRate }}%</b> ({{ displayWinRate.partyGames }}场)</span>
              </div>
              <div class="foot-note">{{ viewMode === 'career' ? '数据来源：OpenDota 生涯全量' : '数据来源：最近 20 把对局' }}</div>
            </a-card>
          </a-col>

          <!-- 3. 参战率 (前25分钟 vs 全场) -->
          <a-col :span="6">
            <a-card title="参战率 (前25分钟 vs 整局)" :bordered="false" class="metric-card">
              <div class="flex-around">
                <div>
                  <div class="label-sm">前25分钟参战率</div>
                  <div class="big-num" style="color: #1890ff">{{ activeStats.tfRate25m || 0 }}%</div>
                </div>
                <a-divider type="vertical" style="height: 50px" />
                <div>
                  <div class="label-sm">整局总参战率</div>
                  <div class="big-num" style="color: #52c41a">{{ activeStats.tfRateFull || 0 }}%</div>
                </div>
              </div>
              <div class="foot-note">基于录像 teamfights 节点团战参与度计算</div>
            </a-card>
          </a-col>

          <!-- 4. 10分钟对位线优率 -->
          <a-col :span="6">
            <a-card title="10分钟对位线优率" :bordered="false" class="metric-card">
              <div class="big-num" style="color: #faad14">{{ activeStats.laneWinRateTotal || 0 }}%</div>
              <div class="detail-row">
                <span>中路对位: <b>{{ activeStats.midLaneWinRate }}%</b> (均差 {{ formatDiff(activeStats.midAvgGoldDiff) }})</span>
              </div>
              <div class="detail-row">
                <span>优/劣对位: <b>{{ activeStats.sideLaneWinRate }}%</b> (均差 {{ formatDiff(activeStats.sideAvgGoldDiff) }})</span>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 第二行：五个位置胜率 & 开黑队友胜率(>3场) -->
        <a-row :gutter="16" style="margin-top: 16px">
          <!-- 五个位置场次与胜率 -->
          <a-col :span="12">
            <a-card title="各位置场次与胜率 (1~5号位)" :bordered="false">
              <a-table
                :dataSource="activeStats.positions"
                :columns="posColumns"
                :pagination="false"
                rowKey="position"
                size="middle"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'winRate'">
                    <a-progress :percent="record.winRate" :status="record.winRate >= 50 ? 'success' : 'exception'" />
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>

          <!-- 开黑队友榜单 (>3场) -->
          <a-col :span="12">
            <a-card :title="viewMode === 'recent20' ? '近期开黑队友 (>3场)' : '生涯开黑队友榜 (>3场)'" :bordered="false">
              <a-table
                :dataSource="displayTeammates"
                :columns="peerColumns"
                :pagination="{ pageSize: 5 }"
                rowKey="accountId"
                size="middle"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'winRate'">
                    <a-tag :color="Number(record.winRate) >= 50 ? 'green' : 'volcano'">
                      {{ record.winRate }}%
                    </a-tag>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>
        </a-row>

        <!-- 第三行：常用英雄池 & 最近20把高精对局明细 -->
        <a-row :gutter="16" style="margin-top: 16px">
          <!-- 常用英雄 Top 10 -->
          <a-col :span="9">
            <a-card :title="viewMode === 'recent20' ? '近 20 把常用英雄' : '生涯常用英雄 Top 10'" :bordered="false">
              <a-table
                :dataSource="displayHeroes"
                :columns="heroColumns"
                :pagination="false"
                rowKey="heroId"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'hero'">
                    <div class="hero-cell">
                      <img v-if="heroMap[record.heroId]" :src="heroMap[record.heroId].img" class="hero-img" />
                      <span>{{ heroMap[record.heroId]?.name || `英雄#${record.heroId}` }}</span>
                    </div>
                  </template>
                  <template v-if="column.key === 'winRate'">
                    <span :style="{ color: winRateColor(record.winRate), fontWeight: 'bold' }">
                      {{ record.winRate }}%
                    </span>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>

          <!-- 最近20把单场高精流水 -->
          <a-col :span="15">
            <a-card title="最近 20 把高精解析比赛明细" :bordered="false">
              <a-table
                :dataSource="dashboardData.recentMatchList"
                :columns="matchColumns"
                :pagination="{ pageSize: 8 }"
                rowKey="matchId"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'hero'">
                    <div class="hero-cell">
                      <img v-if="heroMap[record.heroId]" :src="heroMap[record.heroId].img" class="hero-img" />
                      <span>{{ heroMap[record.heroId]?.name || record.heroId }}</span>
                    </div>
                  </template>
                  <template v-if="column.key === 'win'">
                    <a-tag :color="record.win ? 'success' : 'error'">{{ record.win ? '胜利' : '失败' }}</a-tag>
                    <a-tag :color="record.isSolo ? 'default' : 'blue'">{{ record.isSolo ? '单排' : '组排' }}</a-tag>
                  </template>
                  <template v-if="column.key === 'position'">
                    {{ posText(record.position) }}
                  </template>
                  <template v-if="column.key === 'kda'">
                    {{ record.kills }} / {{ record.deaths }} / {{ record.assists }}
                  </template>
                  <template v-if="column.key === 'tf'">
                    <span v-if="record.isParsed">
                      前25分: <b>{{ (record.tfRate25m * 100).toFixed(0) }}%</b> | 全场: {{ (record.tfRateFull * 100).toFixed(0) }}%
                    </span>
                    <a-tag v-else color="warning">待解析录像</a-tag>
                  </template>
                  <template v-if="column.key === 'lane'">
                    <span v-if="record.laneWin !== null">
                      <a-tag :color="record.laneWin === 1 ? 'green' : 'red'">
                        {{ record.laneWin === 1 ? '线优' : '线劣' }} ({{ formatDiff(record.goldDiff10m) }})
                      </a-tag>
                    </span>
                    <span v-else>-</span>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>
  </div>

</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';

const API_BASE = 'http://localhost:8080/api/dota';

const playerList = ref<any[]>([]);
const currentAccountId = ref<number | null>(null);
const newAccountId = ref<string>('290810906'); // 默认填入你示例中的祈求者ID
const viewMode = ref<'recent20' | 'career'>('recent20');
const loading = ref(false);
const registering = ref(false);
const dashboardData = ref<any>(null);
const heroMap = ref<Record<number, { name: string; img: string }>>({});

// 表格列配置
const posColumns = [
  { title: '位置 (分路角色)', dataIndex: 'name', key: 'name' },
  { title: '场次', dataIndex: 'games', key: 'games', width: 80 },
  { title: '胜场', dataIndex: 'wins', key: 'wins', width: 80 },
  { title: '胜率', dataIndex: 'winRate', key: 'winRate', width: 180 }
];

const peerColumns = [
  { title: '队友昵称', dataIndex: 'name', key: 'name' },
  { title: '队友ID', dataIndex: 'accountId', key: 'accountId' },
  { title: '同队场次', dataIndex: 'games', key: 'games' },
  { title: '同队胜场', dataIndex: 'wins', key: 'wins' },
  { title: '开黑胜率', dataIndex: 'winRate', key: 'winRate' }
];

const heroColumns = [
  { title: '英雄', key: 'hero' },
  { title: '场次', dataIndex: 'games', key: 'games' },
  { title: '胜率', dataIndex: 'winRate', key: 'winRate' },
  { title: '场均KDA', dataIndex: 'kda', key: 'kda' }
];

const matchColumns = [
  { title: '比赛ID', dataIndex: 'matchId', key: 'matchId' },
  { title: '英雄', key: 'hero' },
  { title: '结果/模式', key: 'win' },
  { title: '位置', key: 'position' },
  { title: 'K / D / A', key: 'kda' },
  { title: '参战率 (前25分 / 整局)', key: 'tf' },
  { title: '10分钟对位经济差', key: 'lane' }
];

// 根据顶部开关动态计算当前激活的统计块
const activeStats = computed(() => {
  if (!dashboardData.value) return {};
  return viewMode.value === 'recent20'
    ? dashboardData.value.recent20Stats
    : dashboardData.value.localAllStats;
});

// 胜率展示：近20把用 recent20Stats，生涯用 careerMacro
const displayWinRate = computed(() => {
  if (!dashboardData.value) return {};
  const src = viewMode.value === 'recent20'
    ? dashboardData.value.recent20Stats
    : dashboardData.value.careerMacro;
  return {
    totalGames: src.totalGames || 0,
    totalWinRate: src.winRate || 0,
    soloGames: src.soloGames || 0,
    soloWinRate: src.soloWinRate || 0,
    partyGames: src.partyGames || 0,
    partyWinRate: src.partyWinRate || 0
  };
});

// 开黑队友展示 (>3场)
const displayTeammates = computed(() => {
  if (!dashboardData.value) return [];
  return viewMode.value === 'recent20'
    ? dashboardData.value.recent20Stats.teammates || []
    : dashboardData.value.careerMacro.peers || [];
});

// 常用英雄展示
const displayHeroes = computed(() => {
  if (!dashboardData.value) return [];
  return viewMode.value === 'recent20'
    ? dashboardData.value.recent20Stats.heroes || []
    : dashboardData.value.careerMacro.heroes || [];
});

// 加载官方英雄中文名与头像字典
const loadHeroConstants = async () => {
  try {
    const res = await axios.get('https://api.opendota.com/api/constants/heroes');
    const map: Record<number, { name: string; img: string }> = {};
    Object.values(res.data).forEach((h: any) => {
      map[h.id] = {
        name: h.localized_name,
        img: `https://cdn.cloudflare.steamstatic.com${h.img}`
      };
    });
    heroMap.value = map;
  } catch (e) {
    console.warn('获取英雄常量字典失败');
  }
};

const fetchPlayerList = async () => {
  const res = await axios.get(`${API_BASE}/players`);
  playerList.value = res.data;
  if (playerList.value.length > 0 && !currentAccountId.value) {
    currentAccountId.value = playerList.value[0].accountId;
    loadDashboard();
  }
};

const loadDashboard = async () => {
  if (!currentAccountId.value) return;
  loading.value = true;
  try {
    const res = await axios.get(`${API_BASE}/players/${currentAccountId.value}/dashboard`);
    dashboardData.value = res.data;
  } catch (e) {
    message.error('加载看板数据失败');
  } finally {
    loading.value = false;
  }
};

const handleRegisterPlayer = async () => {
  if (!newAccountId.value) return;
  registering.value = true;
  try {
    await axios.post(`${API_BASE}/players/${newAccountId.value}`);
    message.success('选手已录入！后台正在按限流队列拉取50把数据，请稍后刷新查看');
    await fetchPlayerList();
    currentAccountId.value = Number(newAccountId.value);
    await loadDashboard();
  } catch (e) {
    message.error('录入失败');
  } finally {
    registering.value = false;
  }
};

const manualRefresh = async () => {
  if (!currentAccountId.value) return;
  await axios.post(`${API_BASE}/players/${currentAccountId.value}/refresh`);
  message.info('已触发后台增量同步，稍后将自动更新');
  setTimeout(loadDashboard, 3000);
};

const posText = (pos: number) => {
  const arr = ['未知', '1号位(优)', '2号位(中)', '3号位(劣)', '4号位(辅)', '5号位(纯辅)'];
  return arr[pos] || '未知';
};

const formatDiff = (val: number) => {
  if (!val) return '0';
  return val > 0 ? `+${val}` : `${val}`;
};

const winRateColor = (rate: number) => (Number(rate) >= 50 ? '#52c41a' : '#ff4d4f');

onMounted(() => {
  loadHeroConstants();
  fetchPlayerList();
});
</script>

<style scoped>
.dota-container {
  background: #f0f2f5;
  min-height: 100vh;
  padding: 16px 24px;
}
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.flex-around {
  display: flex;
  justify-content: space-around;
  align-items: center;
}
.header-bar {
  margin-bottom: 16px;
  border-radius: 8px;
}
.brand .title {
  font-size: 18px;
  font-weight: 700;
  color: #1f1f1f;
}
.profile-card {
  border-radius: 8px;
}
.player-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.player-meta h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
}
.player-meta p {
  margin: 0;
  color: #8c8c8c;
}
.metric-card {
  height: 170px;
  border-radius: 8px;
}
.big-num {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}
.sub-text {
  font-size: 14px;
  font-weight: normal;
  color: #8c8c8c;
}
.detail-row {
  font-size: 13px;
  color: #595959;
  margin-bottom: 4px;
}
.foot-note {
  font-size: 12px;
  color: #bfbfbf;
  margin-top: 8px;
}
.k { color: #52c41a; font-weight: bold; }
.d { color: #ff4d4f; font-weight: bold; }
.a { color: #1890ff; font-weight: bold; }
.hero-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hero-img {
  width: 42px;
  height: 24px;
  border-radius: 3px;
  object-fit: cover;
}
</style>