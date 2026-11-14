import axios from 'axios';

// 使用相对路径 /api，配合 vite.config.ts 的 proxy 或 Nginx 反向代理自动转发到服务器后端 8088 端口
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
});

export interface PlayerSummary {
  accountId: number;
  personaname: string;
  remark?: string | null;
  avatar: string;
  rankTier: number;
  leaderboardRank?: number | null;
  isTracked?: number;
  lastSyncAt?: string;
  totalMatches: number;
  totalWins: number;
  winRate: number;
  avgKda: number;
  avgKills: number;
  avgDeaths: number;
  avgAssists: number;
  avgGpm: number;
  avgXpm: number;
  radiantMatches: number;
  radiantWins: number;
  direMatches: number;
  direWins: number;
  laneWinRate: number;
  soloWinRate: number;
  duoTrioWinRate: number;
  fiveStackWinRate: number;
}

export interface PlayerHeroStats {
  heroId: number;
  matches: number;
  wins: number;
  losses: number;
  winRate: number;
  kda?: number;
  radiantMatches?: number;
  radiantWins?: number;
  radiantWinRate?: number;
  direMatches?: number;
  direWins?: number;
  direWinRate?: number;
  laneWinCount?: number;
  laneEvenCount?: number;
  laneLossCount?: number;
  laneWinRate: number;
  laneEvenRate?: number;
  laneLossRate: number;
  lastPlayedTime?: number;
  avgKills?: number;
  avgDeaths?: number;
  avgAssists?: number;
  avgGpm?: number;
  avgXpm?: number;
  perfScore?: number;
}

export interface RecentMatch {
  matchId: number;
  heroId: number;
  win: boolean;
  kills: number;
  deaths: number;
  assists: number;
  kda: number;
  goldPerMin: number;
  xpPerMin: number;
  laneResult: number;
  partySize: number;
  isMvp: boolean;
  startTime: number;
}

export interface TeammateSynergy {
  teammateId: number;
  teammateName: string;
  matchesTogether: number;
  winsTogether: number;
  winRate: number;
}

export const syncPlayerMatches = (accountId: string) => {
  return api.post(`/player/${accountId}/sync`);
};

export const fetchHomeOverview = () => {
  return api.get('/player/home');
};

export const fetchTaskStatus = (accountId: string) => {
  return api.get(`/player/${accountId}/tasks`);
};

export const fetchDashboardData = (accountId: string) => {
  return api.get(`/player/${accountId}/dashboard`);
};

export const fetchAllMatches = (accountId: string) => {
  return api.get(`/player/${accountId}/matches`);
};

// 查询单场比赛高精详情数据 (对应后端 /api/player/match/{matchId})
export const fetchMatchDetail = (matchId: string) => {
  return api.get(`/player/match/${matchId}`);
};

// ================= 管理员关注玩家与定时同步相关接口 =================

export const adminLogin = (username: string, password: string) => {
  return api.post('/admin/login', { username, password });
};

export const fetchTrackedPlayers = () => {
  return api.get('/admin/players');
};

export const addTrackedPlayer = (accountId: string, remark?: string) => {
  return api.post(`/admin/players/${accountId}`, remark ? { remark } : {});
};

export const updateTrackedPlayerRemark = (accountId: string | number, remark: string) => {
  return api.post(`/admin/players/${accountId}/remark`, { remark });
};

export const removeTrackedPlayer = (accountId: string | number) => {
  return api.delete(`/admin/players/${accountId}`);
};

export const triggerStaggeredCheckAll = () => {
  return api.post('/admin/check-all');
};
