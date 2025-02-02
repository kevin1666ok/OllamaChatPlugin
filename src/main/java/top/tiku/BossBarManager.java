package top.tiku;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * BossBarManager 类负责管理玩家的 Boss 血条，提供显示、更新和隐藏血条的功能。
 */
public class BossBarManager {

    // 用于存储每个玩家对应的 Boss 血条对象，键为玩家对象，值为 BossBar 对象
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    /**
     * 为指定玩家显示 Boss 血条。
     *
     * @param player 要显示血条的玩家对象
     * @param title  血条的标题，会显示在血条上方
     */
    public void showBossBar(Player player, String title) {
        // 创建一个新的 Boss 血条，设置标题、颜色和样式
        BossBar bossBar = Bukkit.createBossBar(title, BarColor.BLUE, BarStyle.SOLID);
        // 将玩家添加到血条的可见列表中，使玩家能够看到该血条
        bossBar.addPlayer(player);
        // 设置血条为可见状态
        bossBar.setVisible(true);
        // 将玩家和对应的血条对象存入 map 中，方便后续管理
        bossBars.put(player, bossBar);
    }

    /**
     * 更新指定玩家的 Boss 血条进度。
     *
     * @param player   要更新血条进度的玩家对象
     * @param progress 血条的进度值，范围从 0.0 到 1.0，0.0 表示 0%，1.0 表示 100%
     */
    public void updateBossBarProgress(Player player, double progress) {
        // 从 map 中获取该玩家对应的 Boss 血条对象
        BossBar bossBar = bossBars.get(player);
        if (bossBar != null) {
            // 如果血条对象存在，设置血条的进度
            bossBar.setProgress(progress);
        }
    }

    /**
     * 隐藏指定玩家的 Boss 血条。
     *
     * @param player 要隐藏血条的玩家对象
     */
    public void hideBossBar(Player player) {
        // 从 map 中获取该玩家对应的 Boss 血条对象
        BossBar bossBar = bossBars.get(player);
        if (bossBar != null) {
            // 如果血条对象存在，将玩家从血条的可见列表中移除
            bossBar.removePlayer(player);
            // 设置血条为不可见状态
            bossBar.setVisible(false);
            // 从 map 中移除该玩家对应的血条对象
            bossBars.remove(player);
        }
    }
}