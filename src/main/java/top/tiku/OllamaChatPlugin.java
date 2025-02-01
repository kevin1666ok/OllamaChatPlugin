package top.tiku;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * OllamaChatPlugin 是一个 Minecraft 插件，用于在游戏中与 Ollama 服务进行交互，实现聊天功能。
 * 该插件允许玩家使用 /ollama 命令向 Ollama 服务发送消息，并接收回复。
 */
public class OllamaChatPlugin extends JavaPlugin {

    // 用于与 Ollama 服务进行通信的客户端实例
    private OllamaClient ollamaClient;
    // 用于存储和读取插件配置信息的对象
    private FileConfiguration config;

    /**
     * 插件启用时调用的方法，负责初始化配置、创建配置文件夹和启动 Ollama 客户端。
     */
    @Override
    public void onEnable() {
        // 创建配置文件夹
        createConfigFolder();
        // 保存默认配置文件到插件的数据文件夹中
        saveDefaultConfig();
        // 获取配置文件内容
        config = getConfig();
        // 记录插件启用日志
        getLogger().info("Ollama Chat Plugin has been enabled!");
        // 初始化 Ollama 客户端，传入插件实例、配置文件中的模型名称和 Ollama 服务的 URL
        ollamaClient = new OllamaClient(this, config.getString("model"), config.getString("ollama-url"));
    }

    /**
     * 插件禁用时调用的方法，用于记录插件禁用日志。
     */
    @Override
    public void onDisable() {
        getLogger().info("Ollama Chat Plugin has been disabled!");
    }

    /**
     * 处理玩家命令的方法，当玩家输入 /ollama 命令时，向 Ollama 服务发送消息并显示回复。
     *
     * @param sender 命令发送者
     * @param command 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 如果命令处理成功返回 true，否则返回 false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ollama")) {
            if (args.length == 0) {
                // 如果玩家没有提供消息，提示使用方法
                sender.sendMessage("Usage: /ollama <message>");
                getLogger().info("Player used /ollama command without a message.");
                return true;
            }
            // 将玩家输入的参数拼接成完整的消息
            String userMessage = String.join(" ", args);
            getLogger().info("Player used /ollama command with message: " + userMessage);
            // 向玩家发送正在生成的提示消息
            sender.sendMessage("正在生成...");
            // 异步执行向 Ollama 服务发送请求的操作
            CompletableFuture.runAsync(() -> {
                String ollamaResponse = ollamaClient.sendRequestToOllama(userMessage);
                // 按行显示 Ollama 服务的回复
                showTypingEffectByLine(sender, ollamaResponse);
            });
            return true;
        }
        return false;
    }

    /**
     * 按行显示 Ollama 服务回复的方法，实现打字效果。
     *
     * @param sender 消息发送目标
     * @param response 完整的响应消息
     */
    private void showTypingEffectByLine(CommandSender sender, String response) {
        // 将响应消息按换行符分割成多行
        String[] lines = response.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final int lineIndex = i;
            // 按配置文件中的延迟时间逐行发送消息
            getServer().getScheduler().runTaskLater(this, () -> {
                sender.sendMessage(lines[lineIndex]);
            }, i * config.getLong("line-delay"));
        }
    }

    /**
     * 创建配置文件夹的方法，如果文件夹不存在则创建。
     */
    private void createConfigFolder() {
        // 获取插件的数据文件夹
        File dataFolder = getDataFolder();
        // 定义配置文件夹的路径
        File configFolder = new File(dataFolder, "config");
        // 检查配置文件夹是否存在
        if (!configFolder.exists()) {
            // 尝试创建配置文件夹及其父文件夹
            if (configFolder.mkdirs()) {
                getLogger().info("Config folder created successfully.");
            } else {
                getLogger().severe("Failed to create config folder.");
            }
        }
    }
}