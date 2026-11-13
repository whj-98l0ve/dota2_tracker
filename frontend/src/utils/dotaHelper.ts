import heroConstants from '../assets/dota2_heroes_constants.json';
import gameConstants from '../assets/dota2_game_constants.json';

// Steam 官方 CDN 静态资源前缀
const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com';

// 英雄字典类型定义
export interface HeroMeta {
  id: number;
  name: string;
  localized_name: string;
  primary_attr: string;
  attack_type: string;
  roles: string[];
  img: string;
  icon: string;
}

const heroMap = heroConstants as Record<string, HeroMeta>;

// 构建 npc_dota_hero_xxx -> HeroMeta 的反向索引，便于从 killed / damage_taken 字典直接解析英雄
const heroByNameMap: Record<string, HeroMeta> = {};
Object.values(heroMap).forEach((h) => {
  if (h && h.name) {
    heroByNameMap[h.name] = h;
  }
});

const itemIdsMap = (gameConstants as any).itemIds as Record<string, string>;
const itemsMap = (gameConstants as any).items as Record<string, { d: string; c: number; q: string }>;
const abilityIdsMap = (gameConstants as any).abilityIds as Record<string, string>;
const abilitiesMap = (gameConstants as any).abilities as Record<string, { d: string; i: number; t: string }>;
const permanentBuffsMap = (gameConstants as any).permanentBuffs as Record<string, string>;
const patchList = (gameConstants as any).patch as Array<{ name: string; id: number }>;

// 常用核心装备中文译名映射表（提升国内 Dota2 玩家阅读体验）
const ITEM_CN_NAMES: Record<string, string> = {
  blink: '闪烁匕首 (跳刀)',
  overwhelming_blink: '盛势闪光 (力量跳)',
  swift_blink: '迅疾闪光 (敏捷跳)',
  arcane_blink: '秘奥闪光 (智力跳)',
  black_king_bar: '黑皇杖 (BKB)',
  power_treads: '动力鞋 (假腿)',
  phase_boots: '相位鞋',
  arcane_boots: '奥术鞋 (秘法)',
  tranquil_boots: '静谧之鞋 (绿鞋)',
  travel_boots: '远行鞋 (飞鞋)',
  travel_boots_2: '远行鞋 II (大飞鞋)',
  boots_of_bearing: '宽容之靴 (大绿鞋)',
  guardian_greaves: '卫士胫甲 (大鞋)',
  magic_wand: '魔杖',
  magic_stick: '魔棒',
  bracer: '护腕',
  wraith_band: '怨灵系带',
  null_talisman: '空灵挂件',
  soul_ring: '灵魂之戒 (魂戒)',
  bottle: '魔瓶',
  hand_of_midas: '迈达斯之手 (点金手)',
  bfury: '狂战斧',
  maelstrom: '漩涡 (小电锤)',
  mjollnir: '雷神之锤 (大电锤)',
  gleipnir: '缚灵索',
  radiance: '辉耀',
  desolator: '黯灭',
  lesser_crit: '水晶剑',
  greater_crit: '代达罗斯之殇 (大炮)',
  monkey_king_bar: '金箍棒 (MKB)',
  butterfly: '蝴蝶',
  satanic: '撒旦之邪力',
  skadi: '斯嘉蒂之眼 (冰眼)',
  heart: '恐鳌之心 (龙心)',
  assault: '强袭胸甲',
  shivas_guard: '希瓦的守护 (冰甲)',
  sphere: '林肯法球',
  lotus_orb: '清莲宝珠 (莲花)',
  manta: '幻影斧 (分身斧)',
  sange_and_yasha: '散夜对剑 (双刀)',
  kaya_and_sange: '慧夜对剑',
  yasha_and_kaya: '夜叉与慧光',
  sange: '散华',
  yasha: '夜叉',
  kaya: '慧光',
  echo_sabre: '回音战刃 (连击刀)',
  harpoon: '鱼叉',
  diffusal_blade: '净魂之刃 (散失)',
  disperser: '散魂剑 (大散失)',
  basher: '碎颅锤 (晕锤)',
  abyssal_blade: '深渊之刃 (大晕锤)',
  armlet: '莫尔迪基安的臂章',
  mask_of_madness: '疯狂面具 (疯脸)',
  vladmir: '弗拉迪米尔的祭品',
  crimson_guard: '赤红甲',
  pipe: '洞察烟斗 (笛子)',
  Vanguard: '先锋盾',
  vanguard: '先锋盾',
  blade_mail: '刃甲',
  eternal_shroud: '永世法衣',
  mage_slayer: '法师克星',
  orchid: '紫怨',
  bloodthorn: '血棘 (大紫怨)',
  sheepstick: '邪镰 (羊刀)',
  cyclone: 'Eul的神圣法杖 (吹风)',
  wind_waker: '风之杖 (大吹风)',
  force_staff: '原力法杖 (推推)',
  hurricane_pike: '飓风长戟 (大推推)',
  glimmer_cape: '微光披风',
  aeon_disk: '永恒之盘 (盘子)',
  ghost: '幽魂权杖 (绿杖)',
  ethereal_blade: '虚灵之刃 (虚灵刀)',
  dagon_5: '达贡之神力 (5级红杖)',
  octarine_core: '玲珑心',
  refresher: '刷新球',
  ultimate_scepter: '阿哈利姆神杖 (A杖)',
  ultimate_scepter_2: '阿哈利姆神杖加成',
  aghanims_shard: '阿哈利姆魔晶',
  moon_shard: '银月之晶',
  rapier: '圣剑',
  nullifier: '否决坠饰',
  silver_edge: '白银之锋 (大隐刀)',
  invis_sword: '影刃 (隐刀)',
  phylactery: '灵匣',
  khanda: '坎达 (大灵匣)',
  parasma: '巫师之刃 (大巫师)',
  witch_blade: '巫师之刃',
  aether_lens: '以太之透镜',
  rod_of_atos: '阿托斯之棍',
  spirit_vessel: '魂之灵瓮 (大骨灰)',
  urn_of_shadows: '影之灵龛 (骨灰盒)',
  solar_crest: '炎阳纹章 (大勋章)',
  medallion_of_courage: '勇气勋章',
  holy_locket: '圣洁吊坠',
  mekansm: '梅肯斯姆',
  pavise: '长盾',
  drums_of_endurance: '韧鼓',
  ancient_janggo: '韧鼓',
  veil_of_discord: '纷争面纱',
  bloodstone: '血精石',
  meteor_hammer: '陨星锤',
  heaven_halberd: '天堂之戟',
  heavens_halberd: '天堂之戟',
  falcon_blade: '猎鹰战刃',
  corrosion_orb: '腐蚀之球',
  orb_of_corrosion: '腐蚀之球',
  aegis: '不朽之守护',
  cheese: '奶酪',
  refresher_shard: '刷新球碎片',
  gem: '真视宝石',
  smoke_of_deceit: '诡计之雾',
  dust: '显影之尘 (粉)',
  ward_observer: '侦查守卫 (假眼)',
  ward_sentry: '岗哨守卫 (真眼)',
  ward_dispenser: '真假眼合并槽',
  blood_grenade: '血腥榴弹',
  tango: '树之祭祀 (吃树)',
  flask: '治疗药膏 (大药)',
  clarity: '净化药水 (小蓝)',
  enchanted_mango: '魔法芒果',
  faerie_fire: '仙灵之火',
  tpscroll: '回城卷轴 (TP)',
  boots: '速度之靴 (草鞋)',
  branches: '铁树枝干',
  quelling_blade: '压制之刃 (补刀斧)'
};

// 消耗品与基础出门散件集合（用于出装时间轴过滤核心装备）
export const CONSUMABLE_KEYS = new Set([
  'tango', 'tango_single', 'flask', 'clarity', 'faerie_fire', 'enchanted_mango',
  'blood_grenade', 'smoke_of_deceit', 'dust', 'ward_observer', 'ward_sentry',
  'ward_dispenser', 'tpscroll', 'branches', 'gauntlets', 'slippers', 'mantle',
  'circlet', 'ring_of_protection', 'quelling_blade', 'sobi_mask', 'ring_of_regen',
  'infused_raindrop', 'famango', 'great_famango', 'greater_famango'
]);

/**
 * 根据英雄 ID 获取英雄中文名称
 */
export function getHeroName(heroId: number | string | undefined): string {
  if (!heroId) return '未知英雄';
  return heroMap[String(heroId)]?.localized_name || `英雄 #${heroId}`;
}

/**
 * 根据英雄 ID 获取英雄横版头像 URL
 */
export function getHeroAvatar(heroId: number | string | undefined): string {
  if (!heroId) return '';
  const item = heroMap[String(heroId)];
  return item?.img ? `${STEAM_CDN}${item.img}` : '';
}

/**
 * 根据英雄 ID 获取英雄小图标 URL
 */
export function getHeroIcon(heroId: number | string | undefined): string {
  if (!heroId) return '';
  const item = heroMap[String(heroId)];
  return item?.icon ? `${STEAM_CDN}${item.icon}` : getHeroAvatar(heroId);
}

/**
 * 根据英雄内部代码 (如 npc_dota_hero_antimage) 获取英雄元数据
 */
export function getHeroByNpcName(npcName: string): HeroMeta | null {
  return heroByNameMap[npcName] || null;
}

/**
 * 根据物品数字 ID 获取物品 Key
 */
export function getItemKeyById(itemId: number | string | undefined): string {
  if (!itemId || Number(itemId) <= 0) return '';
  return itemIdsMap[String(itemId)] || '';
}

/**
 * 根据物品 Key 或数字 ID 获取物品完整展示信息 (图标、中英文名、价格)
 */
export function getItemMeta(idOrKey: number | string | undefined): {
  key: string;
  name: string;
  cost: number;
  qual: string;
  img: string;
} | null {
  if (!idOrKey || idOrKey === 0 || idOrKey === '0') return null;
  const key = typeof idOrKey === 'number' || /^\d+$/.test(String(idOrKey))
    ? getItemKeyById(idOrKey)
    : String(idOrKey);
  if (!key || key.startsWith('recipe_')) {
    if (key && key.startsWith('recipe_')) {
      return {
        key,
        name: `卷轴: ${ITEM_CN_NAMES[key.replace('recipe_', '')] || itemsMap[key]?.d || key}`,
        cost: itemsMap[key]?.c || 0,
        qual: 'component',
        img: `${STEAM_CDN}/apps/dota2/images/dota_react/items/recipe.png`
      };
    }
    return null;
  }
  const meta = itemsMap[key];
  const cnName = ITEM_CN_NAMES[key];
  const displayName = cnName || meta?.d || key;
  return {
    key,
    name: displayName,
    cost: meta?.c || 0,
    qual: meta?.q || '',
    img: `${STEAM_CDN}/apps/dota2/images/dota_react/items/${key}.png`
  };
}

/**
 * 根据技能数字 ID 或技能 Key 获取技能详情 (名称、图标、是否天赋)
 */
export function getAbilityMeta(idOrKey: number | string | undefined): {
  key: string;
  name: string;
  img: string;
  isTalent: boolean;
  dmgType: string;
} | null {
  if (!idOrKey) return null;
  const key = typeof idOrKey === 'number' || /^\d+$/.test(String(idOrKey))
    ? abilityIdsMap[String(idOrKey)] || ''
    : String(idOrKey);
  if (!key) return null;

  const isTalent = key.startsWith('special_bonus_');
  const meta = abilitiesMap[key];
  const name = meta?.d || (isTalent ? formatTalentKey(key) : key);
  const img = isTalent
    ? ''
    : `${STEAM_CDN}/apps/dota2/images/dota_react/abilities/${key}.png`;

  return {
    key,
    name,
    img,
    isTalent,
    dmgType: meta?.t || ''
  };
}

function formatTalentKey(key: string): string {
  return '天赋: ' + key.replace('special_bonus_unique_', '').replace('special_bonus_', '').replace(/_/g, ' ');
}

/**
 * 解析永久 Buff 信息 (如银月、A杖、魔晶、屠夫力量、沉默智力、军团攻击力、莱恩手指等)
 */
export function getPermanentBuffMeta(buffId: number, stackCount: number): {
  key: string;
  name: string;
  img: string;
  stacks: number;
} {
  const key = permanentBuffsMap[String(buffId)] || `buff_${buffId}`;
  const buffCnMap: Record<string, { name: string; isItem?: boolean }> = {
    moon_shard: { name: '银月之晶 (+60攻速)', isItem: true },
    ultimate_scepter: { name: '阿哈利姆神杖加成', isItem: true },
    aghanims_shard: { name: '阿哈利姆魔晶', isItem: true },
    tome_of_knowledge: { name: '知识之书', isItem: true },
    silencer_glaives_of_wisdom: { name: '智力窃取 (沉默术士)' },
    pudge_flesh_heap: { name: '腐肉堆积 (帕吉)' },
    legion_commander_duel: { name: '决斗胜利攻击力 (军团)' },
    lion_finger_of_death: { name: '死亡一指层数 (莱恩)' },
    slark_essence_shift: { name: '能量转移永久敏捷 (斯拉克)' },
    abyssal_underlord_atrophy_aura: { name: '衰退光环攻击力 (孽主)' },
    bounty_hunter_jinada: { name: '忍术窃取金币 (赏金猎人)' },
    axe_culling_blade: { name: '淘汰之刃永久护甲 (斧王)' },
    necrolyte_reapers_scythe: { name: '死神镰刀层数 (瘟疫法师)' },
    muerta_pierce_the_veil: { name: '穿透面纱层数 (琼英碧灵)' }
  };
  const info = buffCnMap[key];
  const isItem = info?.isItem || key === 'moon_shard' || key === 'ultimate_scepter' || key === 'aghanims_shard';
  const img = isItem
    ? `${STEAM_CDN}/apps/dota2/images/dota_react/items/${key}.png`
    : `${STEAM_CDN}/apps/dota2/images/dota_react/abilities/${key}.png`;

  return {
    key,
    name: info?.name || key,
    img,
    stacks: stackCount || 0
  };
}

/**
 * 游戏模式中文映射
 */
export function getGameModeName(mode: number | undefined): string {
  const map: Record<number, string> = {
    0: '未知模式',
    1: '全英雄选择 (All Pick)',
    2: '队长模式 (Captains Mode)',
    3: '随机征召 (Random Draft)',
    4: '单一征召 (Single Draft)',
    5: '全随机 (All Random)',
    11: '中路单挑 (Mid Only)',
    16: '队长征召 (Captains Draft)',
    18: '技能征召 (Ability Draft)',
    22: '全英雄选择 (Ranked AP)',
    23: '加速模式 (Turbo)'
  };
  return map[Number(mode)] || `标准模式 (#${mode ?? 0})`;
}

/**
 * 房间类型中文映射
 */
export function getLobbyTypeName(lobby: number | undefined): string {
  const map: Record<number, string> = {
    0: '普通匹配 (Normal)',
    1: '练习赛 (Practice)',
    2: '锦标赛 (Tournament)',
    4: '合作对抗电脑 (Co-op Bots)',
    5: '组队天梯 (Ranked Team)',
    6: '单排天梯 (Ranked Solo)',
    7: '天梯匹配 (Ranked)',
    8: '1v1 中路单挑',
    9: '勇士联赛 (Battle Cup)'
  };
  return map[Number(lobby)] || '普通对局';
}

/**
 * 游戏版本号转换 (如 56 -> 7.37)
 */
export function getPatchName(patchId: number | undefined): string {
  if (patchId === undefined || patchId === null) return '最新客户端';
  const found = patchList?.find((p) => p.id === Number(patchId));
  return found ? `Patch ${found.name}` : `Patch #${patchId}`;
}

/**
 * 段位 코드 (rank_tier, 如 75 -> 超凡入圣 [5], 80 -> 冠绝一世) 转换为中文名与配色
 */
export function getRankMeta(rankTier: number | undefined): {
  label: string;
  shortLabel: string;
  colorClass: string;
} {
  if (!rankTier || rankTier <= 0) {
    return { label: '未校准', shortLabel: '未校准', colorClass: 'text-slate-400 border-slate-700 bg-slate-800/60' };
  }
  const medal = Math.floor(rankTier / 10);
  const stars = rankTier % 10;
  const starStr = medal < 8 && stars > 0 ? ` [${stars}星]` : '';
  const shortStar = medal < 8 && stars > 0 ? `${stars}` : '';
  const medalMap: Record<number, { name: string; short: string; cls: string }> = {
    1: { name: '先锋', short: '先锋', cls: 'text-stone-300 border-stone-600/60 bg-stone-800/40' },
    2: { name: '卫士', short: '卫士', cls: 'text-amber-600 border-amber-700/60 bg-amber-950/40' },
    3: { name: '中军', short: '中军', cls: 'text-emerald-400 border-emerald-600/50 bg-emerald-950/40' },
    4: { name: '统帅', short: '统帅', cls: 'text-teal-300 border-teal-500/50 bg-teal-950/40' },
    5: { name: '传奇', short: '传奇', cls: 'text-rose-400 border-rose-500/50 bg-rose-950/40' },
    6: { name: '万古流芳', short: '万古', cls: 'text-purple-400 border-purple-500/50 bg-purple-950/40' },
    7: { name: '超凡入圣', short: '超凡', cls: 'text-cyan-300 border-cyan-400/50 bg-cyan-950/50' },
    8: { name: '冠绝一世', short: '冠绝', cls: 'text-amber-300 border-amber-400/60 bg-amber-500/15' }
  };
  const m = medalMap[medal] || { name: `段位${medal}`, short: `R${medal}`, cls: 'text-slate-300 border-slate-700 bg-slate-800' };
  return {
    label: `${m.name}${starStr}`,
    shortLabel: `${m.short}${shortStar}`,
    colorClass: m.cls
  };
}

/**
 * 1~5号位元数据
 */
export function getPositionMeta(pos: number | undefined): {
  id: number;
  label: string;
  short: string;
  badgeClass: string;
} {
  const map: Record<number, { label: string; short: string; badgeClass: string }> = {
    1: { label: 'POS 1 优势路核心', short: 'POS 1 一号位', badgeClass: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' },
    2: { label: 'POS 2 中单核心', short: 'POS 2 二号位', badgeClass: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30' },
    3: { label: 'POS 3 劣势路核心', short: 'POS 3 三号位', badgeClass: 'text-amber-400 bg-amber-500/10 border-amber-500/30' },
    4: { label: 'POS 4 游走辅助', short: 'POS 4 四号位', badgeClass: 'text-purple-400 bg-purple-500/10 border-purple-500/30' },
    5: { label: 'POS 5 纯辅助', short: 'POS 5 五号位', badgeClass: 'text-rose-400 bg-rose-500/10 border-rose-500/30' }
  };
  const id = Number(pos) || 0;
  const item = map[id] || { label: '未知位置', short: '机动位', badgeClass: 'text-slate-400 bg-slate-800 border-slate-700' };
  return { id, ...item };
}

/**
 * 金币变动原因 (gold_reasons) 中文映射
 */
export function getGoldReasonName(code: string): string {
  const map: Record<string, string> = {
    '0': '其他/基础工资',
    '1': '阵亡损失',
    '2': '买活消耗',
    '6': '放弃比赛分摊',
    '11': '摧毁建筑赏金',
    '12': '击杀英雄赏金',
    '13': '线上小兵击杀',
    '14': '中立野怪击杀',
    '15': '肉山赏金',
    '16': '助攻赏金',
    '17': '赏金神符',
    '19': '信使击杀赏金',
    '20': '侦查守卫排眼赏金',
    '21': '点金手/技能额外金币',
    '22': '中立营地堆野奖励'
  };
  return map[String(code)] || `来源 #${code}`;
}

/**
 * 神符类型中文映射
 */
export function getRuneName(runeKey: string): string {
  const map: Record<string, string> = {
    '0': '双倍伤害神符',
    '1': '极速神符',
    '2': '幻象神符',
    '3': '隐身神符',
    '4': '恢复神符',
    '5': '赏金神符',
    '6': '奥术神符',
    '7': '圣水神符',
    '8': '智慧神符',
    '9': '护盾神符'
  };
  return map[String(runeKey)] || `神符 #${runeKey}`;
}

/**
 * 建筑内部代码中文翻译
 */
export function getBuildingCnName(buildingKey: string | undefined): {
  name: string;
  isRadiantBuilding: boolean;
} {
  if (!buildingKey) return { name: '未知建筑', isRadiantBuilding: true };
  const isRadiantBuilding = buildingKey.includes('goodguys');
  const side = isRadiantBuilding ? '天辉' : '夜魇';

  let part = '建筑';
  if (buildingKey.includes('tower1_top')) part = '上路一塔';
  else if (buildingKey.includes('tower2_top')) part = '上路二塔';
  else if (buildingKey.includes('tower3_top')) part = '上路高地塔';
  else if (buildingKey.includes('tower1_mid')) part = '中路一塔';
  else if (buildingKey.includes('tower2_mid')) part = '中路二塔';
  else if (buildingKey.includes('tower3_mid')) part = '中路高地塔';
  else if (buildingKey.includes('tower1_bot')) part = '下路一塔';
  else if (buildingKey.includes('tower2_bot')) part = '下路二塔';
  else if (buildingKey.includes('tower3_bot')) part = '下路高地塔';
  else if (buildingKey.includes('tower4')) part = '基地门牙塔';
  else if (buildingKey.includes('melee_rax_top')) part = '上路近战兵营';
  else if (buildingKey.includes('range_rax_top')) part = '上路远程兵营';
  else if (buildingKey.includes('melee_rax_mid')) part = '中路近战兵营';
  else if (buildingKey.includes('range_rax_mid')) part = '中路远程兵营';
  else if (buildingKey.includes('melee_rax_bot')) part = '下路近战兵营';
  else if (buildingKey.includes('range_rax_bot')) part = '下路远程兵营';
  else if (buildingKey.includes('fort')) part = '远古遗迹 (主基地)';

  return {
    name: `${side}${part}`,
    isRadiantBuilding
  };
}

/**
 * 解析防御塔存活位掩码 (11位: 上1/2/3, 中1/2/3, 下1/2/3, 门牙1/2)
 */
export function decodeTowerStatus(statusMask: number | undefined) {
  const mask = Number(statusMask ?? 0);
  const check = (bit: number) => ((mask >> bit) & 1) === 1;
  return {
    topT1: check(0),
    topT2: check(1),
    topT3: check(2),
    midT1: check(3),
    midT2: check(4),
    midT3: check(5),
    botT1: check(6),
    botT2: check(7),
    botT3: check(8),
    ancient1: check(9),
    ancient2: check(10),
    aliveCount: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10].filter((b) => check(b)).length
  };
}

/**
 * 解析兵营存活位掩码 (6位: 上近/远, 中近/远, 下近/远)
 */
export function decodeBarracksStatus(statusMask: number | undefined) {
  const mask = Number(statusMask ?? 0);
  const check = (bit: number) => ((mask >> bit) & 1) === 1;
  return {
    topMelee: check(0),
    topRanged: check(1),
    midMelee: check(2),
    midRanged: check(3),
    botMelee: check(4),
    botRanged: check(5),
    aliveCount: [0, 1, 2, 3, 4, 5].filter((b) => check(b)).length
  };
}

/**
 * 格式化比赛秒数为 MM:SS (支持负数出门时间)
 */
export function formatGameTime(seconds: number | undefined): string {
  if (seconds === undefined || seconds === null || isNaN(seconds)) return '00:00';
  const isNeg = seconds < 0;
  const abs = Math.abs(Math.floor(seconds));
  const m = Math.floor(abs / 60);
  const s = abs % 60;
  return `${isNeg ? '-' : ''}${m < 10 ? '0' + m : m}:${s < 10 ? '0' + s : s}`;
}
