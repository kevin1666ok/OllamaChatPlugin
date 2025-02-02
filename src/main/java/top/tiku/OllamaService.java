package top.tiku;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

/**
 * OllamaService 类负责处理与 Ollama 服务相关的操作，
 * 包括启动和停止服务、切换模型、切换服务模式、发送请求以及显示响应等。
 */
public class OllamaService {

    // 插件实例，用于获取日志、调度器等功能
    private JavaPlugin plugin;
    // 配置管理器，用于获取和修改配置信息
    private ConfigManager configManager;
    // Ollama 客户端，用于与 Ollama 服务进行通信
    private OllamaClient ollamaClient;
    // 内置 Ollama 服务的进程对象，用于控制服务的启动和停止
    private Process ollamaProcess;
    // Boss 血条管理器，用于显示和更新模型下载进度
    private BossBarManager bossBarManager;

    /**
     * 构造函数，初始化 OllamaService 实例。
     *
     * @param plugin        插件实例
     * @param configManager 配置管理器
     */
    public OllamaService(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        // 创建 Boss 血条管理器实例
        this.bossBarManager = new BossBarManager();

        // 根据配置的服务模式确定 Ollama 服务的 URL
        String ollamaMode = configManager.getOllamaMode();
        String ollamaUrl;
        if ("built-in".equals(ollamaMode)) {
            startOllama();
            ollamaUrl = configManager.getOllamaBuiltInUrl();
        } else {
            ollamaUrl = configManager.getOllamaStandaloneUrl();
        }

        // 获取当前使用的 Ollama 模型
        String ollamaModel = configManager.getOllamaModel();
        // 创建 Ollama 客户端实例
        ollamaClient = new OllamaClient(plugin, ollamaModel, ollamaUrl);
    }

    /**
     * 启动内置的 Ollama 服务。
     */
    public void startOllama() {
        try {
            // 获取当前操作系统的名称
            String os = System.getProperty("os.name").toLowerCase();
            // 根据操作系统选择合适的 Ollama 可执行文件名称
            String executableName = os.contains("win") ? "ollama.exe" : "ollama";
            // 获取 Ollama 可执行文件的路径
            File ollamaExecutable = new File(plugin.getDataFolder(), "ollama/" + executableName);
            if (!ollamaExecutable.exists()) {
                plugin.getLogger().severe("Ollama executable not found!");
                return;
            }

            // 创建进程构建器，用于启动 Ollama 服务
            ProcessBuilder pb = new ProcessBuilder(ollamaExecutable.getAbsolutePath(), "serve");
            pb.directory(plugin.getDataFolder());
            // 启动 Ollama 服务并保存进程对象
            ollamaProcess = pb.start();
            plugin.getLogger().info("Ollama service started.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start Ollama service: " + e.getMessage());
        }
    }

    /**
     * 停止内置的 Ollama 服务。
     */
    public void stopOllama() {
        if (ollamaProcess != null && ollamaProcess.isAlive()) {
            ollamaProcess.destroy();
            plugin.getLogger().info("Ollama service stopped.");
        }
    }

    /**
     * 切换 Ollama 使用的模型。
     *
     * @param newModel 要切换到的新模型名称
     * @return 如果切换成功返回 true，否则返回 false
     */
    public boolean changeModel(String newModel) {
        if (configManager.getAvailableModels().contains(newModel)) {
            configManager.setOllamaModel(newModel);
            ollamaClient.setModel(newModel);
            return true;
        }
        return false;
    }

    /**
     * 切换 Ollama 服务的使用模式（内置或独立）。
     *
     * @param newMode 要切换到的新服务模式（"built-in" 或 "standalone"）
     * @return 如果切换成功返回 true，否则返回 false
     */
    public boolean changeMode(String newMode) {
        if ("built-in".equals(newMode) || "standalone".equals(newMode)) {
            configManager.setOllamaMode(newMode);
            // 先停止当前服务
            stopOllama();
            // 重新初始化 Ollama 客户端
            String ollamaUrl;
            if ("built-in".equals(newMode)) {
                startOllama();
                ollamaUrl = configManager.getOllamaBuiltInUrl();
            } else {
                ollamaUrl = configManager.getOllamaStandaloneUrl();
            }
            ollamaClient.setUrl(ollamaUrl);
            return true;
        }
        return false;
    }

    /**
     * 向 Ollama 服务发送请求并获取响应。
     *
     * @param userMessage 用户输入的消息
     * @return Ollama 服务返回的响应
     */
    public String sendRequest(String userMessage) {
        return ollamaClient.sendRequestToOllama(userMessage);
    }

    /**
     * 按行显示响应消息，并模拟打字效果。
     *
     * @param sender   消息的接收者
     * @param response Ollama 服务返回的响应消息
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
     * 使用游戏命令下载 Ollama 模型，并使用 Boss 血条显示下载进度。
     *
     * @param sender 命令发送者
     * @param model  要下载的模型名称
     */
    public void downloadModel(CommandSender sender, String model) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以下载模型。");
            return;
        }
        Player player = (Player) sender;
        // 显示 Boss 血条
        bossBarManager.showBossBar(player, "正在下载模型: " + model);

        try {
            // 获取当前操作系统的名称
            String os = System.getProperty("os.name").toLowerCase();
            // 根据操作系统选择合适的 Ollama 可执行文件名称
            String executableName = os.contains("win") ? "ollama.exe" : "ollama";
            // 获取 Ollama 可执行文件的路径
            File ollamaExecutable = new File(plugin.getDataFolder(), "ollama/" + executableName);
            if (!ollamaExecutable.exists()) {
                sender.sendMessage("Ollama executable not found!");
                bossBarManager.hideBossBar(player);
                return;
            }

            // 创建进程构建器，用于下载模型
            ProcessBuilder pb = new ProcessBuilder(ollamaExecutable.getAbsolutePath(), "pull", model);
            pb.directory(plugin.getDataFolder());
            Process process = pb.start();

            // 读取进程的输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            double progress = 0.0;
            while ((line = reader.readLine()) != null) {
                // 这里需要根据 Ollama 实际输出格式解析进度信息
                // 示例代码假设 Ollama 输出包含 "%" 来表示进度
                if (line.contains("%")) {
                    try {
                        int percentIndex = line.indexOf("%");
                        String progressStr = line.substring(0, percentIndex).trim();
                        progress = Double.parseDouble(progressStr) / 100.0;
                        bossBarManager.updateBossBarProgress(player, progress);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("无法解析下载进度: " + line);
                    }
                }
            }

            // 等待进程执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                sender.sendMessage("模型 " + model + " 下载成功。");
                sender.sendMessage("你可以使用 /ollama setmodel " + model + " 命令来启用该模型。");
            } else {
                sender.sendMessage("下载模型 " + model + " 失败。");
            }
        } catch (IOException | InterruptedException e) {
            sender.sendMessage("下载模型时出错: " + e.getMessage());
        } finally {
            // 隐藏 Boss 血条
            bossBarManager.hideBossBar(player);
        }
    }
}