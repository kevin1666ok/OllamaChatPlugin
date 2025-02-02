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

public class OllamaService {

    private JavaPlugin plugin;
    private ConfigManager configManager;
    private OllamaClient ollamaClient;
    private Process ollamaProcess;
    private BossBarManager bossBarManager;

    public OllamaService(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.bossBarManager = new BossBarManager();

        String ollamaMode = configManager.getOllamaMode();
        String ollamaUrl;
        if ("built-in".equals(ollamaMode)) {
            startOllama();
            ollamaUrl = configManager.getOllamaBuiltInUrl();
        } else {
            ollamaUrl = configManager.getOllamaStandaloneUrl();
        }

        String ollamaModel = configManager.getOllamaModel();
        ollamaClient = new OllamaClient(plugin, ollamaModel, ollamaUrl);
    }

    public void startOllama() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String executableName = os.contains("win") ? "ollama.exe" : "ollama";
            File ollamaExecutable = new File(plugin.getDataFolder(), "ollama/" + executableName);
            if (!ollamaExecutable.exists()) {
                plugin.getLogger().severe("Ollama executable not found!");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(ollamaExecutable.getAbsolutePath(), "serve");
            pb.directory(plugin.getDataFolder());
            ollamaProcess = pb.start();
            plugin.getLogger().info("Ollama service started.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start Ollama service: " + e.getMessage());
        }
    }

    public void stopOllama() {
        if (ollamaProcess != null && ollamaProcess.isAlive()) {
            ollamaProcess.destroy();
            plugin.getLogger().info("Ollama service stopped.");
        }
    }

    public boolean changeModel(String newModel) {
        if (configManager.getAvailableModels().contains(newModel)) {
            configManager.setOllamaModel(newModel);
            ollamaClient.setModel(newModel);
            return true;
        }
        return false;
    }

    public boolean changeMode(String newMode) {
        if ("built-in".equals(newMode) || "standalone".equals(newMode)) {
            configManager.setOllamaMode(newMode);
            stopOllama();
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

    public String sendRequest(String userMessage) {
        return ollamaClient.sendRequestToOllama(userMessage);
    }

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

    public void downloadModel(CommandSender sender, String model) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以下载模型。");
            return;
        }
        Player player = (Player) sender;
        bossBarManager.showBossBar(player, "正在下载模型: " + model);

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String executableName = os.contains("win") ? "ollama.exe" : "ollama";
            File ollamaExecutable = new File(plugin.getDataFolder(), "ollama/" + executableName);
            if (!ollamaExecutable.exists()) {
                sender.sendMessage("Ollama executable not found!");
                bossBarManager.hideBossBar(player);
                return;
            }

            // 创建模型存储文件夹
            File modelsFolder = new File(plugin.getDataFolder(), "models");
            if (!modelsFolder.exists()) {
                modelsFolder.mkdirs();
            }

            // 修改命令，指定模型存储路径
            ProcessBuilder pb = new ProcessBuilder(
                    ollamaExecutable.getAbsolutePath(),
                    "pull",
                    model,
                    "--dir",
                    modelsFolder.getAbsolutePath()
            );
            pb.directory(plugin.getDataFolder());
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            double progress = 0.0;
            while ((line = reader.readLine()) != null) {
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
            bossBarManager.hideBossBar(player);
        }
    }
}