package com.whj_98l0ve.dota2_tracker.controller;

import com.whj_98l0ve.dota2_tracker.service.OpenDotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器：管理定时自动巡检的关注玩家 ID 列表
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final OpenDotaService openDotaService;

    @Value("${admin.username:root}")
    private String adminUsername;

    @Value("${admin.password:123456}")
    private String adminPassword;

    /**
     * 1. 管理员登录校验（默认账号 root，密码 123456）
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        Map<String, Object> res = new HashMap<>();
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            res.put("success", true);
            res.put("msg", "登录成功");
        } else {
            res.put("success", false);
            res.put("msg", "用户名或密码错误");
        }
        return res;
    }

    /**
     * 2. 获取所有被关注的玩家 ID 列表及每小时错峰巡检状态
     */
    @GetMapping("/players")
    public Map<String, Object> getTrackedPlayers() {
        return openDotaService.getTrackedPlayersStatus();
    }

    /**
     * 3. 增加关注的玩家 ID（支持同时传入备注 remark，并立即触发一次比赛检查同步）
     */
    @PostMapping("/players/{accountId}")
    public Map<String, Object> addTrackedPlayer(
            @PathVariable Long accountId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String remark = body != null ? body.get("remark") : null;
        return openDotaService.addTrackedPlayer(accountId, remark);
    }

    /**
     * 3.1 修改或设置关注玩家的备注名称（同时支持 POST 与 PUT）
     */
    @RequestMapping(value = "/players/{accountId}/remark", method = {RequestMethod.POST, RequestMethod.PUT})
    public Map<String, Object> updatePlayerRemark(
            @PathVariable Long accountId,
            @RequestBody Map<String, String> body
    ) {
        String remark = body != null ? body.get("remark") : null;
        boolean updated = openDotaService.updateTrackedPlayerRemark(accountId, remark);
        Map<String, Object> res = new HashMap<>();
        res.put("success", updated);
        res.put("msg", updated ? "玩家备注已保存" : "未找到该玩家记录");
        return res;
    }

    /**
     * 4. 删除（取消关注）玩家 ID，不再参与每小时自动同步
     */
    @DeleteMapping("/players/{accountId}")
    public Map<String, Object> removeTrackedPlayer(@PathVariable Long accountId) {
        boolean removed = openDotaService.removeTrackedPlayer(accountId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", removed);
        res.put("msg", removed ? "已取消关注该玩家 ID" : "未找到该玩家记录");
        return res;
    }

    /**
     * 5. 手动将所有关注玩家加入错峰巡检队列（逐个间隔 20 秒请求，避免并发报错）
     */
    @PostMapping("/check-all")
    public Map<String, Object> triggerStaggeredCheckAll() {
        int count = openDotaService.triggerStaggeredCheckAllTracked();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("queueSize", count);
        res.put("msg", "已将关注玩家排入错峰巡检队列，后台将每隔 20 秒逐个向 OpenDota 检查更新");
        return res;
    }
}
