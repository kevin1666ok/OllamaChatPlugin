package top.tiku;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.CompletableFuture;

/**
 * NewAPIService 类负责处理与新 API 服务相关的操作，
 * 包括发送请求、显示响应等功能。
 */
public class NewAPIService {

    // 插件实例，用于获取日志、调度器等功能
    private JavaPlugin plugin;
    // 配置管理器，用于获取和修改配置信息
    private ConfigManager configManager;
    // 新 API 客户端，用于与新 API 服务进行通信
    private NewAPIClient newAPIClient;

    /**
     * 构造函数，初始化 NewAPIService 实例。
     *
     * @param plugin        插件实例
     * @param configManager 配置管理器
     */
    public NewAPIService(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        // 获取新 API 的相关配置信息
        String newApiModel = configManager.getNewApiModel();
        String newApiUrl = configManager.getNewApiUrl();
        String newApiKey = configManager.getNewApiKey();

        // 创建新 API 客户端实例
        newAPIClient = new NewAPIClient(plugin, newApiUrl, newApiKey, newApiModel);
    }

    /**
     * 向新 API 服务发送请求并获取响应。
     *
     * @param userMessage 用户输入的消息
     * @return 新 API 服务返回的响应
     */
    public String sendRequest(String userMessage) {
        return newAPIClient.sendRequestToNewAPI(userMessage);
    }

    /**
     * 按行显示响应消息，并模拟打字效果。
     *
     * @param sender   消息的接收者
     * @param response 新 API 服务返回的响应消息
     */
    public void showTypingEffectByLine(CommandSender sender, String response) {
        String[] lines = response.split("\n");
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long lineDelay = configManager.getLineDelay();
        for (int i = 0; i < lines.length; i++) {
            final int lineIndex = i;
            scheduler.runTaskLater(plugin, () -> {
                sender.sendMessage(lines[lineIndex]);
            }, i * lineDelay);
        }
    }

    /**
     * 切换新 API 使用的模型。
     *
     * @param newModel 要切换到的新模型名称
     */
    public void changeModel(String newModel) {
        configManager.setNewApiModel(newModel);
        newAPIClient.setModel(newModel);
    }
}