package top.tiku;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

// 插件主类，继承自 JavaPlugin，负责插件的初始化、命令处理等核心功能
public class OllamaChatPlugin extends JavaPlugin {

    // Ollama API 客户端实例，用于与 Ollama 服务进行交互
    private OllamaClient ollamaClient;
    // 新 API 客户端实例，用于与其他自定义 API 服务进行交互
    private NewAPIClient newAPIClient;
    // 用于读取和管理配置文件的对象，可获取配置文件中的各项配置信息
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // 调用创建配置文件夹的方法，确保配置文件夹存在
        createConfigFolder();
        // 保存默认配置文件，如果配置文件不存在，会将默认配置保存到文件中
        saveDefaultConfig();
        // 获取配置文件内容，以便后续读取配置信息
        config = getConfig();
        // 记录插件启用的日志信息，方便调试和监控
        getLogger().info("Ollama Chat Plugin has been enabled!");

        // 从配置文件中读取 Ollama API 相关配置
        // "ollama.model" 是配置文件中指定 Ollama 模型的配置项，默认值为 "llama2:7b"
        String ollamaModel = config.getString("ollama.model", "llama2:7b");
        // "ollama.url" 是配置文件中指定 Ollama API 地址的配置项，默认值为 "http://localhost:11434/api/generate"
        String ollamaUrl = config.getString("ollama.url", "http://localhost:11434/api/generate");
        // 使用读取到的配置信息初始化 Ollama 客户端
        ollamaClient = new OllamaClient(this, ollamaModel, ollamaUrl);

        // 从配置文件中读取新 API 相关配置
        // "new-api.model" 是配置文件中指定新 API 模型的配置项，默认值为 "default-model"
        String newApiModel = config.getString("new-api.model", "default-model");
        // "new-api.url" 是配置文件中指定新 API 地址的配置项，默认值为 "https://example.com/api/generate"
        String newApiUrl = config.getString("new-api.url", "https://example.com/api/generate");
        // "new-api.key" 是配置文件中指定新 API 密钥的配置项，默认值为 "your_api_key_here"
        String newApiKey = config.getString("new-api.key", "your_api_key_here");
        // 使用读取到的配置信息初始化新 API 客户端
        newAPIClient = new NewAPIClient(this, newApiUrl, newApiKey, newApiModel);
    }

    @Override
    public void onDisable() {
        // 记录插件禁用的日志信息，方便调试和监控
        getLogger().info("Ollama Chat Plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 判断玩家输入的命令是否为 "/ollama"
        if (command.getName().equalsIgnoreCase("ollama")) {
            // 检查玩家是否输入了消息，如果没有输入，提示正确的命令使用方法
            if (args.length == 0) {
                sender.sendMessage("Usage: /ollama <message>");
                getLogger().info("Player used /ollama command without a message.");
                return true;
            }
            // 将玩家输入的参数拼接成完整的消息
            String userMessage = String.join(" ", args);
            // 记录玩家使用命令及输入消息的日志信息
            getLogger().info("Player used /ollama command with message: " + userMessage);
            // 向玩家发送正在生成回复的提示信息
            sender.sendMessage("正在生成...");

            // 异步执行请求操作，避免阻塞主线程，使用 CompletableFuture 实现异步处理
            CompletableFuture.runAsync(() -> {
                // 从配置文件中获取要使用的 API 类型，默认为 Ollama
                String apiType = config.getString("api-type", "ollama");
                String response;
                if ("ollama".equals(apiType)) {
                    // 若使用 Ollama API，调用 Ollama 客户端发送请求
                    response = ollamaClient.sendRequestToOllama(userMessage);
                } else if ("new-api".equals(apiType)) {
                    // 若使用新 API，调用新 API 客户端发送请求
                    response = newAPIClient.sendRequestToNewAPI(userMessage);
                } else {
                    // 若配置的 API 类型不支持，返回错误提示信息
                    response = "Unsupported API type.";
                }
                // 按行显示 API 的回复信息
                showTypingEffectByLine(sender, response);
            });
            return true;
        }
        return false;
    }

    // 按行显示回复信息并实现打字效果的方法
    private void showTypingEffectByLine(CommandSender sender, String response) {
        // 将回复信息按换行符分割成多行
        String[] lines = response.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final int lineIndex = i;
            // 按配置文件中设置的延迟时间逐行发送消息，模拟打字效果
            getServer().getScheduler().runTaskLater(this, () -> {
                sender.sendMessage(lines[lineIndex]);
            }, i * config.getLong("line-delay"));
        }
    }

    // 创建配置文件夹的方法
    private void createConfigFolder() {
        // 获取插件的数据文件夹
        File dataFolder = getDataFolder();
        // 定义配置文件夹的路径
        File configFolder = new File(dataFolder, "config");
        if (!configFolder.exists()) {
            if (configFolder.mkdirs()) {
                // 若配置文件夹创建成功，记录日志信息
                getLogger().info("Config folder created successfully.");
            } else {
                // 若配置文件夹创建失败，记录错误日志信息
                getLogger().severe("Failed to create config folder.");
            }
        }
    }
}