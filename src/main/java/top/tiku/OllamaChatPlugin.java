package top.tiku;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * 主插件类，继承自 JavaPlugin，负责插件的整体生命周期管理和命令处理。
 */
public class OllamaChatPlugin extends JavaPlugin {

    // 配置管理器，用于读取和管理配置文件
    private ConfigManager configManager;
    // Ollama 服务，处理与 Ollama 相关的操作
    private OllamaService ollamaService;
    // 新 API 服务，处理与新 API 相关的操作
    private NewAPIService newAPIService;

    /**
     * 插件启用时调用的方法，进行初始化操作。
     */
    @Override
    public void onEnable() {
        // 创建配置管理器实例，传入当前插件实例
        configManager = new ConfigManager(this);
        // 保存默认配置文件，如果配置文件不存在则创建
        configManager.saveDefaultConfig();

        // 创建 Ollama 服务实例，传入当前插件实例和配置管理器
        ollamaService = new OllamaService(this, configManager);
        // 创建新 API 服务实例，传入当前插件实例和配置管理器
        newAPIService = new NewAPIService(this, configManager);

        // 记录插件启用的日志信息
        getLogger().info("Ollama Chat Plugin has been enabled!");
    }

    /**
     * 插件禁用时调用的方法，进行资源释放和服务停止操作。
     */
    @Override
    public void onDisable() {
        // 停止 Ollama 服务
        ollamaService.stopOllama();
        // 记录插件禁用的日志信息
        getLogger().info("Ollama Chat Plugin has been disabled!");
    }

    /**
     * 处理插件命令的方法。
     *
     * @param sender  命令发送者
     * @param command 命令对象
     * @param label   命令标签
     * @param args    命令参数
     * @return 如果命令处理成功返回 true，否则返回 false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查命令是否为 /ollama
        if (command.getName().equalsIgnoreCase("ollama")) {
            // 如果没有提供参数，提示用户正确的使用方法
            if (args.length == 0) {
                sender.sendMessage("Usage: /ollama <message>");
                getLogger().info("Player used /ollama command without a message.");
                return true;
            }
            // 如果参数为 setmodel，调用 Ollama 服务的切换模型方法
            if (args[0].equalsIgnoreCase("setmodel")) {
                // 检查是否提供了新的模型名称
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ollama setmodel <model_name>");
                    return true;
                }
                String newModel = args[1];
                // 尝试切换模型
                if (ollamaService.changeModel(newModel)) {
                    sender.sendMessage("Successfully changed the model to " + newModel);
                } else {
                    sender.sendMessage("Invalid model. Available models: " + configManager.getAvailableModels());
                }
                return true;
            }
            // 如果参数为 setmode，调用 Ollama 服务的切换模式方法
            if (args[0].equalsIgnoreCase("setmode")) {
                // 检查是否提供了新的服务模式
                if (args.length < 2) {
                    sender.sendMessage("Usage: /ollama setmode <built-in|standalone>");
                    return true;
                }
                String newMode = args[1];
                // 尝试切换服务模式
                if (ollamaService.changeMode(newMode)) {
                    sender.sendMessage("Successfully changed the mode to " + newMode);
                } else {
                    sender.sendMessage("Invalid mode. Available modes: built-in, standalone");
                }
                return true;
            }
            // 将用户输入的参数拼接成完整的消息
            String userMessage = String.join(" ", args);
            // 记录用户使用 /ollama 命令发送的消息日志
            getLogger().info("Player used /ollama command with message: " + userMessage);
            // 告知用户正在生成回复
            sender.sendMessage("正在生成...");

            // 异步执行请求操作
            CompletableFuture.runAsync(() -> {
                // 获取当前使用的 API 类型
                String apiType = configManager.getApiType();
                String response;
                // 根据 API 类型选择调用相应的服务发送请求
                if ("ollama".equals(apiType)) {
                    response = ollamaService.sendRequest(userMessage);
                } else if ("new-api".equals(apiType)) {
                    response = newAPIService.sendRequest(userMessage);
                } else {
                    response = "Unsupported API type.";
                }
                // 调用 Ollama 服务的显示打字效果方法显示回复
                ollamaService.showTypingEffectByLine(sender, response);
            });
            return true;
        }
        return false;
    }
}