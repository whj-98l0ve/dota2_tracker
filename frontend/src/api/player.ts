import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8088/api',
  timeout: 30000
});

export interface PlayerSummary {
  accountId: number;
  personaname: string;
  avatar: string;
  rankTier: number;
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
  avgKills: number;
  avgDeaths: number;
  avgAssists: number;
  kda: number;
  laneWinRate: number;
  laneLossRate: number;
  avgGpm: number;
  avgXpm: number;
  perfScore: number;
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

export const fetchTaskStatus = (accountId: string) => {
  return api.get(`/player/${accountId}/tasks`);
};

export const fetchDashboardData = (accountId: string) => {
  return api.get(`/player/${accountId}/dashboard`);
};