package com.whj_98l0ve.dota2_tracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.whj_98l0ve.dota2_tracker.entity.DotaAiPromptConfig;
import com.whj_98l0ve.dota2_tracker.entity.DotaMatchPlayer;
import com.whj_98l0ve.dota2_tracker.entity.DotaMatchRaw;
import com.whj_98l0ve.dota2_tracker.mapper.DotaAiPromptConfigMapper;
import com.whj_98l0ve.dota2_tracker.mapper.DotaMatchPlayerMapper;
import com.whj_98l0ve.dota2_tracker.mapper.DotaMatchRawMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeepSeek AI 比赛复盘分析服务
 * 负责提取比赛核心遥测 JSON 数据，调用 DeepSeek API 进行败方“到底谁的问题？”与胜方“到底谁是天降猛男！”分析，并将结果持久化到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DotaMatchRawMapper matchRawMapper;
    private final DotaMatchPlayerMapper matchPlayerMapper;
    private final DotaAiPromptConfigMapper aiPromptConfigMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String PROMPT_KEY_BLAME = "blame";
    public static final String PROMPT_KEY_MVP = "mvp";

    /** 默认“到底谁的问题？”系统提示词模板（支持 {{losingTeam}} / {{winningTeam}} 占位符） */
    public static final String DEFAULT_BLAME_PROMPT = """
            你是一位精通 Dota2 高端局的毒舌赛事复盘大法官（人称“甩锅局局长”）。
            你的任务是根据比赛 JSON 遥测数据，还原比赛节奏走向与关键团战，揪出败方（{{losingTeam}}）“到底谁的问题最大”。
            【核心要求】：
            1. 语言言辞犀利、一针见血、辛辣吐槽不留情面（但必须基于真实数据）。
            2. **拒绝流水账式堆砌数字！** 不要把每个人的 KDA/GPM/补刀/伤害/眼位全部报一遍，每人只挑 1~2 个最刺眼的数据点佐证即可，整体篇幅控制在 600~900 字，简单明了、直击要害。
            3. **着重分析比赛节奏走向、关键团战、关键击杀/被抓与致命失误**（结合分时经济差、关键团战记录、肉山/推塔事件和核心装备时间节点）。
            4. 必须全程使用简体中文，严禁夹杂英文属性名或代码术语（如把 item_black_king_bar 译为黑皇杖BKB）。
            请按以下精简 Markdown 结构输出：
            1. **🚔 终极审判（头号罪犯）**：直接点名 1 名选手（昵称 + 英雄 + 位置）、『犯罪评级』和 1~2 句犀利定性判词。
            2. **📊 败方锅位榜（从重到轻）**：列出败方 5 人座次与『背锅指数（总和100%）』，每人仅用 **1~2 句话**锐评核心罪状或致命短板，不准报菜名。
            3. **⏱️ 节奏走向与致命转折点**：重点还原对线期→中期→后期的局势崩盘脉络，点出 **2~3 个最关键的转折团战、关键被抓/买活死、丢盾或出装失误**。
            4. **💡 简明翻盘药方**：3 条以内的核心改进建议，每条一句话讲透。""";

    /** 默认“谁是天降猛男！”系统提示词模板（支持 {{losingTeam}} / {{winningTeam}} 占位符） */
    public static final String DEFAULT_MVP_PROMPT = """
            你是一位精通 Dota2 高端局的金牌赛事解说兼“天降猛男荣誉殿堂主裁判”。
            你的任务是根据比赛 JSON 遥测数据，还原比赛节奏走向与关键团战，评选出胜方（{{winningTeam}}）“谁是天降猛男！”（最功不可没的头号功臣）。
            【核心要求】：
            1. **拒绝流水账式堆砌数字！** 不要把每个人的所有面板数据全部罗列一遍，每人只挑 1~2 个最亮眼的数据作佐证，整体篇幅控制在 600~900 字，简单明了、爽快有力。
            2. **着重分析比赛节奏走向、关键团战、关键击杀/先手与高光转折点**（结合分时经济差、关键团战记录、肉山/推塔事件和核心装备发力期）。
            3. 必须全部使用简体中文，严禁夹杂英文属性名、英文英雄名或代码术语（如把 item_black_king_bar 译为黑皇杖BKB）。
            请按以下精简 Markdown 结构输出：
            1. **💪 终极授勋（天降猛男 / 头号功臣）**：直接点名胜方 1 名选手（昵称 + 英雄 + 位置）、『猛男评级』（如：盖世猛男 / 擎天柱 / 孤胆英雄）和 1~2 句霸气颁奖词。
            2. **🏆 胜方功劳榜（从高到低）**：列出胜方 5 人座次与『功勋指数（总和100%）』，每人仅用 **1~2 句话**点出最核心的高光作用，不准报菜名。
            3. **⚡ 节奏走向与制胜关键团**：重点还原胜方是如何建立优势或翻盘的，点出 **2~3 个奠定胜局的关键团战、关键击杀/先手、控盾或关键装备发力节点**。
            4. **🎖️ 赛后致敬评语**：用一句热血简短的中文评语致敬本场“天降猛男”。""";

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-flash}")
    private String model;

    /** 防止同一场比赛同一分析类型并发重复触发 DeepSeek 请求的锁集合（key 格式如 matchId:blame / matchId:mvp） */
    private final ConcurrentHashMap<String, Object> matchLocks = new ConcurrentHashMap<>();

    /** Dota2 全英雄 ID -> 中文名称映射表，确保发送给 DeepSeek 的数据包含准确的中文英雄名 */
    private static final Map<Integer, String> HERO_CN_MAP = new HashMap<>();

    static {
        HERO_CN_MAP.put(1, "敌法师");
        HERO_CN_MAP.put(2, "斧王");
        HERO_CN_MAP.put(3, "祸乱之源");
        HERO_CN_MAP.put(4, "血魔");
        HERO_CN_MAP.put(5, "水晶室女");
        HERO_CN_MAP.put(6, "卓尔游侠");
        HERO_CN_MAP.put(7, "撼地者");
        HERO_CN_MAP.put(8, "主宰");
        HERO_CN_MAP.put(9, "米拉娜");
        HERO_CN_MAP.put(10, "变体精灵");
        HERO_CN_MAP.put(11, "影魔");
        HERO_CN_MAP.put(12, "幻影长矛手");
        HERO_CN_MAP.put(13, "帕克");
        HERO_CN_MAP.put(14, "帕吉");
        HERO_CN_MAP.put(15, "剃刀");
        HERO_CN_MAP.put(16, "沙王");
        HERO_CN_MAP.put(17, "风暴之灵");
        HERO_CN_MAP.put(18, "斯文");
        HERO_CN_MAP.put(19, "小小");
        HERO_CN_MAP.put(20, "复仇之魂");
        HERO_CN_MAP.put(21, "风行者");
        HERO_CN_MAP.put(22, "宙斯");
        HERO_CN_MAP.put(23, "昆卡");
        HERO_CN_MAP.put(25, "莉娜");
        HERO_CN_MAP.put(26, "莱恩");
        HERO_CN_MAP.put(27, "暗影萨满");
        HERO_CN_MAP.put(28, "斯拉达");
        HERO_CN_MAP.put(29, "潮汐猎人");
        HERO_CN_MAP.put(30, "巫医");
        HERO_CN_MAP.put(31, "巫妖");
        HERO_CN_MAP.put(32, "力丸");
        HERO_CN_MAP.put(33, "谜团");
        HERO_CN_MAP.put(34, "修补匠");
        HERO_CN_MAP.put(35, "狙击手");
        HERO_CN_MAP.put(36, "瘟疫法师");
        HERO_CN_MAP.put(37, "术士");
        HERO_CN_MAP.put(38, "兽王");
        HERO_CN_MAP.put(39, "痛苦女王");
        HERO_CN_MAP.put(40, "剧毒术士");
        HERO_CN_MAP.put(41, "虚空假面");
        HERO_CN_MAP.put(42, "冥魂大帝");
        HERO_CN_MAP.put(43, "死亡先知");
        HERO_CN_MAP.put(44, "幻影刺客");
        HERO_CN_MAP.put(45, "帕格纳");
        HERO_CN_MAP.put(46, "圣堂刺客");
        HERO_CN_MAP.put(47, "冥界亚龙");
        HERO_CN_MAP.put(48, "露娜");
        HERO_CN_MAP.put(49, "龙骑士");
        HERO_CN_MAP.put(50, "戴泽");
        HERO_CN_MAP.put(51, "发条技师");
        HERO_CN_MAP.put(52, "拉席克");
        HERO_CN_MAP.put(53, "先知");
        HERO_CN_MAP.put(54, "噬魂鬼");
        HERO_CN_MAP.put(55, "黑暗贤者");
        HERO_CN_MAP.put(56, "克林克兹");
        HERO_CN_MAP.put(57, "全能骑士");
        HERO_CN_MAP.put(58, "魅惑魔女");
        HERO_CN_MAP.put(59, "哈斯卡");
        HERO_CN_MAP.put(60, "暗夜魔王");
        HERO_CN_MAP.put(61, "育母蜘蛛");
        HERO_CN_MAP.put(62, "赏金猎人");
        HERO_CN_MAP.put(63, "编织者");
        HERO_CN_MAP.put(64, "杰奇洛");
        HERO_CN_MAP.put(65, "蝙蝠骑士");
        HERO_CN_MAP.put(66, "陈");
        HERO_CN_MAP.put(67, "幽鬼");
        HERO_CN_MAP.put(68, "远古冰魄");
        HERO_CN_MAP.put(69, "末日使者");
        HERO_CN_MAP.put(70, "熊战士");
        HERO_CN_MAP.put(71, "裂魂人");
        HERO_CN_MAP.put(72, "矮人直升机");
        HERO_CN_MAP.put(73, "炼金术士");
        HERO_CN_MAP.put(74, "祈求者");
        HERO_CN_MAP.put(75, "沉默术士");
        HERO_CN_MAP.put(76, "殁境神蚀者");
        HERO_CN_MAP.put(77, "狼人");
        HERO_CN_MAP.put(78, "酒仙");
        HERO_CN_MAP.put(79, "暗影恶魔");
        HERO_CN_MAP.put(80, "德鲁伊");
        HERO_CN_MAP.put(81, "混沌骑士");
        HERO_CN_MAP.put(82, "米波");
        HERO_CN_MAP.put(83, "树精卫士");
        HERO_CN_MAP.put(84, "食人魔魔法师");
        HERO_CN_MAP.put(85, "不朽尸王");
        HERO_CN_MAP.put(86, "拉比克");
        HERO_CN_MAP.put(87, "干扰者");
        HERO_CN_MAP.put(88, "司夜刺客");
        HERO_CN_MAP.put(89, "娜迦海妖");
        HERO_CN_MAP.put(90, "光之守卫");
        HERO_CN_MAP.put(91, "艾欧");
        HERO_CN_MAP.put(92, "维萨吉");
        HERO_CN_MAP.put(93, "斯拉克");
        HERO_CN_MAP.put(94, "美杜莎");
        HERO_CN_MAP.put(95, "巨魔战将");
        HERO_CN_MAP.put(96, "半人马战行者");
        HERO_CN_MAP.put(97, "马格纳斯");
        HERO_CN_MAP.put(98, "伐木机");
        HERO_CN_MAP.put(99, "钢背兽");
        HERO_CN_MAP.put(100, "巨牙海民");
        HERO_CN_MAP.put(101, "天怒法师");
        HERO_CN_MAP.put(102, "亚巴顿");
        HERO_CN_MAP.put(103, "上古巨神");
        HERO_CN_MAP.put(104, "军团指挥官");
        HERO_CN_MAP.put(105, "工程师");
        HERO_CN_MAP.put(106, "灰烬之灵");
        HERO_CN_MAP.put(107, "大地之灵");
        HERO_CN_MAP.put(108, "孽主");
        HERO_CN_MAP.put(109, "恐怖利刃");
        HERO_CN_MAP.put(110, "凤凰");
        HERO_CN_MAP.put(111, "神谕者");
        HERO_CN_MAP.put(112, "寒冬飞龙");
        HERO_CN_MAP.put(113, "天穹守望者");
        HERO_CN_MAP.put(114, "齐天大圣");
        HERO_CN_MAP.put(119, "邪影芳灵");
        HERO_CN_MAP.put(120, "石鳞剑士");
        HERO_CN_MAP.put(121, "天涯墨客");
        HERO_CN_MAP.put(123, "森海飞霞");
        HERO_CN_MAP.put(126, "虚无之灵");
        HERO_CN_MAP.put(128, "电炎绝手");
        HERO_CN_MAP.put(129, "玛尔斯");
        HERO_CN_MAP.put(131, "百戏大王");
        HERO_CN_MAP.put(135, "破晓辰星");
        HERO_CN_MAP.put(136, "玛西");
        HERO_CN_MAP.put(137, "兽");
        HERO_CN_MAP.put(138, "琼英碧灵");
        HERO_CN_MAP.put(145, "凯");
        HERO_CN_MAP.put(155, "朗戈");
    }

    /**
     * 获取或生成比赛的 DeepSeek AI “到底谁的问题？”分析结果
     * 若数据库中已有缓存且未强制刷新，直接从数据库返回；否则调用 DeepSeek API 生成并落库保存
     */
    /**
     * 读取当前生效的 AI 提示词配置（同时返回系统默认模板，方便管理员参考或恢复）
     */
    public Map<String, Object> getAiPrompts() {
        Map<String, Object> res = new HashMap<>();
        DotaAiPromptConfig blameCfg = aiPromptConfigMapper.selectById(PROMPT_KEY_BLAME);
        DotaAiPromptConfig mvpCfg = aiPromptConfigMapper.selectById(PROMPT_KEY_MVP);

        String blamePrompt = (blameCfg != null && blameCfg.getPromptContent() != null && !blameCfg.getPromptContent().isBlank())
                ? blameCfg.getPromptContent()
                : DEFAULT_BLAME_PROMPT;
        String mvpPrompt = (mvpCfg != null && mvpCfg.getPromptContent() != null && !mvpCfg.getPromptContent().isBlank())
                ? mvpCfg.getPromptContent()
                : DEFAULT_MVP_PROMPT;

        res.put("success", true);
        res.put("blamePrompt", blamePrompt);
        res.put("mvpPrompt", mvpPrompt);
        res.put("defaultBlamePrompt", DEFAULT_BLAME_PROMPT);
        res.put("defaultMvpPrompt", DEFAULT_MVP_PROMPT);
        return res;
    }

    /**
     * 保存管理员自定义的 AI 提示词配置到数据库（立即生效，无需重启服务）
     */
    public Map<String, Object> saveAiPrompts(String blamePrompt, String mvpPrompt) {
        if (blamePrompt != null && !blamePrompt.isBlank()) {
            upsertPromptConfig(PROMPT_KEY_BLAME, blamePrompt.trim());
        }
        if (mvpPrompt != null && !mvpPrompt.isBlank()) {
            upsertPromptConfig(PROMPT_KEY_MVP, mvpPrompt.trim());
        }
        Map<String, Object> res = getAiPrompts();
        res.put("msg", "AI 提示词配置已保存并立即生效");
        return res;
    }

    /**
     * 恢复系统默认 AI 提示词（清空数据库自定义覆盖）
     */
    public Map<String, Object> resetAiPrompts() {
        aiPromptConfigMapper.deleteById(PROMPT_KEY_BLAME);
        aiPromptConfigMapper.deleteById(PROMPT_KEY_MVP);
        Map<String, Object> res = getAiPrompts();
        res.put("msg", "已恢复系统默认 AI 提示词");
        return res;
    }

    private void upsertPromptConfig(String key, String content) {
        DotaAiPromptConfig existing = aiPromptConfigMapper.selectById(key);
        if (existing == null) {
            DotaAiPromptConfig entity = new DotaAiPromptConfig();
            entity.setPromptKey(key);
            entity.setPromptContent(content);
            aiPromptConfigMapper.insert(entity);
        } else {
            existing.setPromptContent(content);
            aiPromptConfigMapper.updateById(existing);
        }
    }

    private String resolvePromptTemplate(String key, String defaultTemplate, String winningTeamName, String losingTeamName) {
        DotaAiPromptConfig cfg = aiPromptConfigMapper.selectById(key);
        String template = (cfg != null && cfg.getPromptContent() != null && !cfg.getPromptContent().isBlank())
                ? cfg.getPromptContent()
                : defaultTemplate;
        return template
                .replace("{{winningTeam}}", winningTeamName)
                .replace("{{losingTeam}}", losingTeamName);
    }

    public Map<String, Object> analyzeMatchBlame(Long matchId, boolean forceRefresh) {
        return executeMatchAnalysis(
                matchId,
                PROMPT_KEY_BLAME,
                forceRefresh,
                DotaMatchRaw::getAiAnalysis,
                (entity, reply) -> entity.setAiAnalysis(reply),
                id -> matchRawMapper.update(null, new LambdaUpdateWrapper<DotaMatchRaw>()
                        .eq(DotaMatchRaw::getMatchId, id)
                        .set(DotaMatchRaw::getAiAnalysis, null)),
                (radiantWin, telemetryJson) -> {
                    String losingTeamName = radiantWin ? "夜魇" : "天辉";
                    String winningTeamName = radiantWin ? "天辉" : "夜魇";

                    String systemPrompt = resolvePromptTemplate(PROMPT_KEY_BLAME, DEFAULT_BLAME_PROMPT, winningTeamName, losingTeamName);
                    String userPrompt = String.format(
                            "这场比赛（比赛ID：%d）最终【%s获胜，%s输了】。\n请问：%s输了，到底谁的问题最大？请言辞犀利地复盘比赛节奏走向、关键团战/失误并定责：\n\n%s",
                            matchId, winningTeamName, losingTeamName, losingTeamName, telemetryJson
                    );
                    return new String[]{systemPrompt, userPrompt};
                }
        );
    }

    /**
     * 获取或生成比赛的 DeepSeek AI “谁是天降猛男！”分析结果
     * 若数据库中已有缓存且未强制刷新，直接从数据库返回；若强制刷新，先清空数据库旧记录再重新生成
     */
    public Map<String, Object> analyzeMatchMvp(Long matchId, boolean forceRefresh) {
        return executeMatchAnalysis(
                matchId,
                PROMPT_KEY_MVP,
                forceRefresh,
                DotaMatchRaw::getAiMvpAnalysis,
                (entity, reply) -> entity.setAiMvpAnalysis(reply),
                id -> matchRawMapper.update(null, new LambdaUpdateWrapper<DotaMatchRaw>()
                        .eq(DotaMatchRaw::getMatchId, id)
                        .set(DotaMatchRaw::getAiMvpAnalysis, null)),
                (radiantWin, telemetryJson) -> {
                    String winningTeamName = radiantWin ? "天辉" : "夜魇";
                    String losingTeamName = radiantWin ? "夜魇" : "天辉";

                    String systemPrompt = resolvePromptTemplate(PROMPT_KEY_MVP, DEFAULT_MVP_PROMPT, winningTeamName, losingTeamName);
                    String userPrompt = String.format(
                            "这场比赛（比赛ID：%d）最终【%s获胜，%s输了】。\n请问：%s赢了，谁最功不可没（谁是天降猛男）？请复盘比赛节奏走向、关键团战与功臣评选：\n\n%s",
                            matchId, winningTeamName, losingTeamName, winningTeamName, telemetryJson
                    );
                    return new String[]{systemPrompt, userPrompt};
                }
        );
    }

    /**
     * 通用比赛 AI 分析执行器：处理本地数据校验、数据库缓存命中、强制刷新清库、并发加锁双重检查、遥测数据组装、DeepSeek 调用与落库持久化
     */
    private Map<String, Object> executeMatchAnalysis(
            Long matchId,
            String analysisType,
            boolean forceRefresh,
            java.util.function.Function<DotaMatchRaw, String> cacheGetter,
            java.util.function.BiConsumer<DotaMatchRaw, String> cacheSetter,
            java.util.function.Consumer<Long> cacheClearer,
            java.util.function.BiFunction<Boolean, String, String[]> promptBuilder
    ) {
        Map<String, Object> response = new HashMap<>();

        DotaMatchRaw raw = matchRawMapper.selectById(matchId);
        if (raw == null || raw.getRawData() == null || raw.getRawData().isBlank()) {
            response.put("success", false);
            response.put("msg", "未找到该场比赛的本地高精度录像数据，无法进行分析");
            return response;
        }

        // 1. 非强制刷新时，优先从数据库缓存直接查找返回
        String cachedValue = cacheGetter.apply(raw);
        if (!forceRefresh && cachedValue != null && !cachedValue.isBlank()) {
            response.put("success", true);
            response.put("cached", true);
            response.put("analysis", cachedValue);
            return response;
        }

        // 2. 检查 DeepSeek API Key 是否已配置
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.put("success", false);
            response.put("msg", "后端尚未配置 DeepSeek API Key，请在 backend/src/main/resources/application.yaml 的 deepseek.api-key 中填入密钥后重启后端服务。");
            return response;
        }

        // 3. 按比赛ID + 分析类型加锁，防止同一场比赛被多人同时点击触发重复调用
        String lockKey = matchId + ":" + analysisType;
        Object lock = matchLocks.computeIfAbsent(lockKey, k -> new Object());
        synchronized (lock) {
            try {
                if (forceRefresh) {
                    // 强制刷新：先显式清空数据库中保存的旧分析结果，再重新向 DeepSeek 请求
                    cacheClearer.accept(matchId);
                    log.info("🗑️ 已清空比赛 {} 的旧 [{}] 分析缓存，准备重新请求 DeepSeek...", matchId, analysisType);
                } else {
                    // 双重检查：若刚才已有线程完成了分析并入库，直接读取返回
                    DotaMatchRaw latestRaw = matchRawMapper.selectById(matchId);
                    String latestCached = latestRaw != null ? cacheGetter.apply(latestRaw) : null;
                    if (latestCached != null && !latestCached.isBlank()) {
                        response.put("success", true);
                        response.put("cached", true);
                        response.put("analysis", latestCached);
                        return response;
                    }
                }

                // 4. 组装比赛详细遥测 JSON 与提示词
                JsonNode matchRoot = objectMapper.readTree(raw.getRawData());
                List<DotaMatchPlayer> dbPlayers = matchPlayerMapper.selectList(
                        new LambdaQueryWrapper<DotaMatchPlayer>()
                                .eq(DotaMatchPlayer::getMatchId, matchId)
                                .orderByAsc(DotaMatchPlayer::getPlayerSlot)
                );

                boolean radiantWin = Boolean.TRUE.equals(raw.getRadiantWin());
                String telemetryJson = buildCompactTelemetryJson(matchRoot, dbPlayers, radiantWin);
                String[] prompts = promptBuilder.apply(radiantWin, telemetryJson);

                // 5. 调用 DeepSeek Chat Completions 接口
                String aiReply = callDeepSeekApi(prompts[0], prompts[1]);

                // 6. 保存至数据库 dota_match_raw 对应缓存字段
                DotaMatchRaw updateEntity = new DotaMatchRaw();
                updateEntity.setMatchId(matchId);
                cacheSetter.accept(updateEntity, aiReply);
                matchRawMapper.updateById(updateEntity);

                log.info("🤖 比赛 {} DeepSeek AI [{}] 分析完成并已写入数据库缓存", matchId, analysisType);

                response.put("success", true);
                response.put("cached", false);
                response.put("analysis", aiReply);
                return response;
            } catch (Exception e) {
                log.error("调用 DeepSeek 分析比赛 {} [{}] 失败: {}", matchId, analysisType, e.getMessage(), e);
                response.put("success", false);
                response.put("msg", "DeepSeek AI 分析请求失败: " + e.getMessage());
                return response;
            } finally {
                matchLocks.remove(lockKey);
            }
        }
    }

    /**
     * 从原始大 JSON 和计算指标表中提取完整的核心比赛详情 JSON（剔除冗余坐标点以加快推理并突出关键比赛事实）
     */
    private String buildCompactTelemetryJson(JsonNode matchRoot, List<DotaMatchPlayer> dbPlayers, boolean radiantWin) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("比赛ID", matchRoot.path("match_id").asLong());
        root.put("获胜阵营", radiantWin ? "天辉 (Radiant)" : "夜魇 (Dire)");
        root.put("败方阵营", radiantWin ? "夜魇 (Dire)" : "天辉 (Radiant)");

        int durationSec = matchRoot.path("duration").asInt(0);
        root.put("比赛时长", String.format("%d分%02d秒", durationSec / 60, durationSec % 60));
        root.put("天辉总击杀", matchRoot.path("radiant_score").asInt(0));
        root.put("夜魇总击杀", matchRoot.path("dire_score").asInt(0));
        root.put("一血发生时间秒", matchRoot.path("first_blood_time").asInt(0));

        // 提取天辉经济领先优势曲线（正数代表天辉领先，负数代表夜魇领先，每5分钟采样一次）
        JsonNode goldAdv = matchRoot.path("radiant_gold_adv");
        if (goldAdv.isArray() && goldAdv.size() > 0) {
            ObjectNode advCurve = objectMapper.createObjectNode();
            for (int min = 0; min < goldAdv.size(); min += 5) {
                int val = goldAdv.get(min).asInt(0);
                advCurve.put(min + "分钟", (val >= 0 ? "天辉领先 " + val : "夜魇领先 " + Math.abs(val)) + " 金币");
            }
            int lastMin = goldAdv.size() - 1;
            int lastVal = goldAdv.get(lastMin).asInt(0);
            advCurve.put("终局(" + lastMin + "分钟)", (lastVal >= 0 ? "天辉领先 " + lastVal : "夜魇领先 " + Math.abs(lastVal)) + " 金币");
            root.set("分时团队经济差走势", advCurve);
        }

        // 建立 slot -> DotaMatchPlayer 映射
        Map<Integer, DotaMatchPlayer> dbPlayerBySlot = new HashMap<>();
        if (dbPlayers != null) {
            for (DotaMatchPlayer dp : dbPlayers) {
                if (dp.getPlayerSlot() != null) {
                    dbPlayerBySlot.put(dp.getPlayerSlot(), dp);
                }
            }
        }

        ArrayNode radiantArray = objectMapper.createArrayNode();
        ArrayNode direArray = objectMapper.createArrayNode();

        JsonNode rawPlayers = matchRoot.path("players");
        if (rawPlayers.isArray()) {
            for (JsonNode rp : rawPlayers) {
                int slot = rp.path("player_slot").asInt(0);
                boolean isRadiant = slot < 128;
                DotaMatchPlayer dp = dbPlayerBySlot.get(slot);

                int heroId = rp.path("hero_id").asInt(dp != null && dp.getHeroId() != null ? dp.getHeroId() : 0);
                String heroName = HERO_CN_MAP.getOrDefault(heroId, "英雄#" + heroId);

                int pos = dp != null && dp.getLaneRole() != null ? dp.getLaneRole() : rp.path("lane_role").asInt(0);
                String posName = switch (pos) {
                    case 1 -> "1号位(优势路核心)";
                    case 2 -> "2号位(中单核心)";
                    case 3 -> "3号位(劣势路核心)";
                    case 4 -> "4号位(游走辅助)";
                    case 5 -> "5号位(纯辅助)";
                    default -> "未知位置";
                };

                int laneResultCode = dp != null && dp.getLaneResult() != null ? dp.getLaneResult() : 2;
                String laneResultStr = switch (laneResultCode) {
                    case 1 -> "线优";
                    case 3 -> "线劣";
                    default -> "均势";
                };

                ObjectNode pNode = objectMapper.createObjectNode();
                pNode.put("选手昵称", rp.path("personaname").asText(dp != null && dp.getPersonaname() != null ? dp.getPersonaname() : "匿名玩家"));
                pNode.put("阵营", isRadiant ? "天辉" : "夜魇");
                pNode.put("使用英雄", heroName);
                pNode.put("位置角色", posName);
                pNode.put("最终等级", rp.path("level").asInt(1));
                pNode.put("击杀_死亡_助攻_KDA", String.format("%d/%d/%d (KDA: %s)",
                        rp.path("kills").asInt(0),
                        rp.path("deaths").asInt(0),
                        rp.path("assists").asInt(0),
                        dp != null && dp.getKda() != null ? dp.getKda().toPlainString() : "0"));
                pNode.put("参战率", Math.round(rp.path("teamfight_participation").asDouble(0) * 100) + "%");
                pNode.put("终局净资产", rp.path("net_worth").asInt(rp.path("total_gold").asInt(0)));
                pNode.put("GPM_XPM", rp.path("gold_per_min").asInt(0) + " / " + rp.path("xp_per_min").asInt(0));
                pNode.put("正补_反补_野怪", String.format("正补%d / 反补%d / 野怪%d(远古%d)",
                        rp.path("last_hits").asInt(0),
                        rp.path("denies").asInt(0),
                        rp.path("neutral_kills").asInt(0),
                        rp.path("ancient_kills").asInt(0)));

                // 10分钟对线期数据
                int gold10m = rp.path("gold_t").isArray() && rp.path("gold_t").size() > 10 ? rp.path("gold_t").get(10).asInt(0) : 0;
                int lh10m = rp.path("lh_t").isArray() && rp.path("lh_t").size() > 10 ? rp.path("lh_t").get(10).asInt(0) : 0;
                int dn10m = rp.path("dn_t").isArray() && rp.path("dn_t").size() > 10 ? rp.path("dn_t").get(10).asInt(0) : 0;
                pNode.put("10分钟对线表现", String.format("结果:%s | 10m经济:%d | 10m正反补:%d/%d | 对线效率:%d%%",
                        laneResultStr, gold10m, lh10m, dn10m, rp.path("lane_efficiency_pct").asInt(50)));

                // 伤害与承伤
                int heroDmgTaken = 0;
                JsonNode dmgTakenNode = rp.path("damage_taken");
                if (dmgTakenNode.isObject()) {
                    var fields = dmgTakenNode.fields();
                    while (fields.hasNext()) {
                        var entry = fields.next();
                        if (entry.getKey().startsWith("npc_dota_hero_")) {
                            heroDmgTaken += entry.getValue().asInt(0);
                        }
                    }
                }
                pNode.put("英雄伤害", rp.path("hero_damage").asInt(0));
                pNode.put("建筑推进伤害", rp.path("tower_damage").asInt(0));
                pNode.put("英雄治疗量", rp.path("hero_healing").asInt(0));
                pNode.put("承受敌方英雄伤害", heroDmgTaken);
                pNode.put("控制眩晕秒数", Math.round(rp.path("stuns").asDouble(0) * 10) / 10.0);

                // 视野与辅助
                pNode.put("视野与辅助贡献", String.format("插假眼:%d, 插真眼:%d, 排眼:%d, 堆野:%d, 买活次数:%d, 累计阵亡读秒:%d秒, 手速APM:%d",
                        rp.path("obs_placed").asInt(0),
                        rp.path("sen_placed").asInt(0),
                        rp.path("observer_kills").asInt(0) + rp.path("sentry_kills").asInt(0),
                        rp.path("camps_stacked").asInt(0),
                        rp.path("buyback_count").asInt(0),
                        rp.path("life_state_dead").asInt(0),
                        rp.path("actions_per_min").asInt(0)));

                // 核心购买日志时间轴（仅保留非消耗品或关键道具购买时间）
                JsonNode purchaseLog = rp.path("purchase_log");
                if (purchaseLog.isArray() && purchaseLog.size() > 0) {
                    ArrayNode itemsTimeline = objectMapper.createArrayNode();
                    for (JsonNode pl : purchaseLog) {
                        String key = pl.path("key").asText("");
                        int timeSec = pl.path("time").asInt(0);
                        if (!isTrivialConsumable(key)) {
                            itemsTimeline.add(String.format("%d分%02d秒:%s", timeSec / 60, Math.abs(timeSec % 60), key));
                        }
                    }
                    pNode.set("装备购买时间线", itemsTimeline);
                }

                // OpenDota 全球同英雄分位表现
                JsonNode bm = rp.path("benchmarks");
                if (bm.isObject()) {
                    pNode.put("全球同英雄百分位水平", String.format("GPM超越%.0f%%玩家, 伤害超越%.0f%%玩家, 正补超越%.0f%%玩家",
                            bm.path("gold_per_min").path("pct").asDouble(0) * 100,
                            bm.path("hero_damage_per_min").path("pct").asDouble(0) * 100,
                            bm.path("last_hits_per_min").path("pct").asDouble(0) * 100));
                }

                if (isRadiant) {
                    radiantArray.add(pNode);
                } else {
                    direArray.add(pNode);
                }
            }
        }

        root.set("天辉阵营5名选手详情", radiantArray);
        root.set("夜魇阵营5名选手详情", direArray);

        // 提取关键团战摘要（让 AI 看清比赛节奏转折与哪波团打输/打赢）
        ArrayNode teamfightsSummary = buildTeamfightsSummary(matchRoot, rawPlayers);
        if (teamfightsSummary.size() > 0) {
            root.set("关键团战记录", teamfightsSummary);
        }

        // 提取关键战略事件（肉山、高地塔/兵营、重要防御塔节点）
        ArrayNode objectivesSummary = buildObjectivesSummary(matchRoot);
        if (objectivesSummary.size() > 0) {
            root.set("关键战略事件", objectivesSummary);
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    /**
     * 提取团战摘要：每场团战起止时间、阵亡数、团队经济净波动以及阵亡/买活英雄
     */
    private ArrayNode buildTeamfightsSummary(JsonNode matchRoot, JsonNode rawPlayers) {
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode teamfights = matchRoot.path("teamfights");
        if (!teamfights.isArray() || teamfights.size() == 0) {
            return result;
        }

        // 建立玩家序号(0~9) -> 中文英雄名及阵营映射
        String[] heroNames = new String[10];
        boolean[] isRadiantArr = new boolean[10];
        if (rawPlayers.isArray()) {
            for (int i = 0; i < Math.min(rawPlayers.size(), 10); i++) {
                JsonNode rp = rawPlayers.get(i);
                int heroId = rp.path("hero_id").asInt(0);
                heroNames[i] = HERO_CN_MAP.getOrDefault(heroId, "英雄#" + heroId);
                isRadiantArr[i] = rp.path("player_slot").asInt(0) < 128;
            }
        }

        for (JsonNode tf : teamfights) {
            int start = tf.path("start").asInt(0);
            int end = tf.path("end").asInt(0);
            int deaths = tf.path("deaths").asInt(0);
            JsonNode tfPlayers = tf.path("players");

            int radiantDeaths = 0;
            int direDeaths = 0;
            int radiantGoldDelta = 0;
            int direGoldDelta = 0;
            StringBuilder deadHeroes = new StringBuilder();
            StringBuilder buybackHeroes = new StringBuilder();

            if (tfPlayers.isArray()) {
                for (int i = 0; i < Math.min(tfPlayers.size(), 10); i++) {
                    JsonNode p = tfPlayers.get(i);
                    int pDeaths = p.path("deaths").asInt(0);
                    int pGoldDelta = p.path("gold_delta").asInt(0);
                    int pBuybacks = p.path("buybacks").asInt(0);
                    String hName = heroNames[i] != null ? heroNames[i] : ("玩家" + i);
                    String side = isRadiantArr[i] ? "天辉" : "夜魇";

                    if (isRadiantArr[i]) {
                        radiantDeaths += pDeaths;
                        radiantGoldDelta += pGoldDelta;
                    } else {
                        direDeaths += pDeaths;
                        direGoldDelta += pGoldDelta;
                    }
                    if (pDeaths > 0) {
                        if (deadHeroes.length() > 0) deadHeroes.append("、");
                        deadHeroes.append(side).append(hName).append(pDeaths > 1 ? "(" + pDeaths + "次)" : "");
                    }
                    if (pBuybacks > 0) {
                        if (buybackHeroes.length() > 0) buybackHeroes.append("、");
                        buybackHeroes.append(side).append(hName);
                    }
                }
            }

            int netSwing = radiantGoldDelta - direGoldDelta;
            String swingDesc = netSwing >= 0
                    ? "天辉净赚约" + netSwing + "金"
                    : "夜魇净赚约" + Math.abs(netSwing) + "金";

            String line = String.format("%d分%02d秒~%d分%02d秒：总阵亡%d人(天辉死%d/夜魇死%d)，%s；阵亡[%s]%s",
                    start / 60, start % 60,
                    end / 60, end % 60,
                    deaths, radiantDeaths, direDeaths, swingDesc,
                    deadHeroes.length() > 0 ? deadHeroes : "无",
                    buybackHeroes.length() > 0 ? "；买活[" + buybackHeroes + "]" : "");
            result.add(line);
        }
        return result;
    }

    /**
     * 提取关键战略事件摘要（一血、肉山、高地建筑及外塔摧毁）
     */
    private ArrayNode buildObjectivesSummary(JsonNode matchRoot) {
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode objectives = matchRoot.path("objectives");
        if (!objectives.isArray()) {
            return result;
        }
        for (JsonNode obj : objectives) {
            String type = obj.path("type").asText("");
            int timeSec = obj.path("time").asInt(0);
            String timeStr = String.format("%d分%02d秒", timeSec / 60, Math.abs(timeSec % 60));

            if ("CHAT_MESSAGE_ROSHAN_KILL".equals(type)) {
                int team = obj.path("team").asInt(0);
                result.add(timeStr + "：" + (team == 2 ? "天辉" : "夜魇") + "击杀肉山");
            } else if ("CHAT_MESSAGE_AEGIS".equals(type)) {
                result.add(timeStr + "：不朽之守护被拾取");
            } else if ("building_kill".equals(type)) {
                String key = obj.path("key").asText("");
                if (key.contains("rax") || key.contains("tower3") || key.contains("tower4") || key.contains("fort")) {
                    String targetTeam = key.contains("goodguys") ? "天辉" : "夜魇";
                    String desc = key.contains("rax") ? "高地兵营"
                            : key.contains("tower3") ? "三塔(高地塔)"
                            : key.contains("tower4") ? "基地双子塔"
                            : "遗迹";
                    result.add(timeStr + "：" + targetTeam + desc + "被摧毁");
                }
            }
        }
        return result;
    }

    private boolean isTrivialConsumable(String key) {
        return key.startsWith("recipe_") || switch (key) {
            case "tango", "tango_single", "flask", "clarity", "faerie_fire", "enchanted_mango",
                 "blood_grenade", "tpscroll", "branches", "ward_observer", "ward_sentry",
                 "ward_dispenser", "famango", "great_famango", "greater_famango" -> true;
            default -> false;
        };
    }

    /**
     * 调用 DeepSeek / OpenAI 兼容接口 (/chat/completions)
     * 使用 Java 标准 HttpClient，完整捕获 401/400/429 等错误响应体详情
     */
    private String callDeepSeekApi(String systemPrompt, String userPrompt) throws Exception {
        String cleanBase = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.trim() : "https://api.deepseek.com";
        while (cleanBase.endsWith("/")) {
            cleanBase = cleanBase.substring(0, cleanBase.length() - 1);
        }
        String url = cleanBase.endsWith("/chat/completions") ? cleanBase : cleanBase + "/chat/completions";

        String reqModel = (model != null && !model.isBlank()) ? model.trim() : "deepseek-flash";

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", reqModel);
        requestBody.put("temperature", 0.7);
        requestBody.put("stream", false);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode sysMsg = objectMapper.createObjectNode();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        ObjectNode usrMsg = objectMapper.createObjectNode();
        usrMsg.put("role", "user");
        usrMsg.put("content", userPrompt);
        messages.add(usrMsg);

        requestBody.set("messages", messages);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + apiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        String respBody = response.body() != null ? response.body() : "";

        if (status < 200 || status >= 300) {
            String errDetail = respBody;
            try {
                JsonNode errNode = objectMapper.readTree(respBody);
                if (errNode.has("error") && errNode.path("error").has("message")) {
                    errDetail = errNode.path("error").path("message").asText();
                }
            } catch (Exception ignored) {
            }
            if (status == 401) {
                throw new RuntimeException("HTTP 401 鉴权失败 (" + errDetail + ")。请检查 application.yaml 中的 deepseek.api-key 与 deepseek.base-url 是否匹配（DeepSeek 官方 Key 以 sk- 开头；若使用的是第三方/火山方舟等平台 Key，请将 base-url 改为对应平台的接口地址）。");
            }
            throw new RuntimeException("HTTP " + status + ": " + errDetail);
        }

        JsonNode resNode = objectMapper.readTree(respBody);
        JsonNode choices = resNode.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            String content = choices.get(0).path("message").path("content").asText("");
            if (!content.isBlank()) {
                return content.trim();
            }
        }
        throw new RuntimeException("AI 返回内容为空: " + respBody);
    }
}
