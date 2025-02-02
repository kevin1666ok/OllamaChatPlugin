package top.tiku;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * ConfigManager 类负责管理插件的配置文件，提供对配置项的读取和写入操作。
 */
public class ConfigManager {

    // 插件实例，用于访问配置文件和保存配置
    private JavaPlugin plugin;
    // 配置文件对象，用于读取和修改配置项
    private FileConfiguration config;

    /**
     * 构造函数，初始化 ConfigManager 实例。
     *
     * @param plugin 插件实例，用于获取配置文件和保存配置
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * 保存默认配置文件。如果配置文件不存在，将创建并保存默认配置。
     */
    public void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    /**
     * 获取 Ollama 的使用模式（内置或独立）。
     *
     * @return Ollama 的使用模式，默认为 "built-in"
     */
    public String getOllamaMode() {
        return config.getString("ollama.mode", "built-in");
    }

    /**
     * 获取当前使用的 Ollama 模型。
     *
     * @return 当前使用的 Ollama 模型，默认为 "llama2:7b"
     */
    public String getOllamaModel() {
        return config.getString("ollama.current_model", "llama2:7b");
    }

    /**
     * 获取内置 Ollama 服务的 API 请求地址。
     *
     * @return 内置 Ollama 服务的 API 请求地址，默认为 "http://localhost:11434/api/generate"
     */
    public String getOllamaBuiltInUrl() {
        return config.getString("ollama.built_in_url", "http://localhost:11434/api/generate");
    }

    /**
     * 获取独立 Ollama 服务的 API 请求地址。
     *
     * @return 独立 Ollama 服务的 API 请求地址，默认为 "http://external-ollama-server:11434/api/generate"
     */
    public String getOllamaStandaloneUrl() {
        return config.getString("ollama.standalone_url", "http://external-ollama-server:11434/api/generate");
    }

    /**
     * 获取可供选择的 Ollama 模型列表。
     *
     * @return 可供选择的 Ollama 模型列表
     */
    public List<String> getAvailableModels() {
        return config.getStringList("ollama.available_models");
    }

    /**
     * 获取当前使用的 API 类型（ollama 或 new-api）。
     *
     * @return 当前使用的 API 类型，默认为 "ollama"
     */
    public String getApiType() {
        return config.getString("api-type", "ollama");
    }

    /**
     * 获取按行输出时每行之间的延迟时间（游戏刻）。
     *
     * @return 每行之间的延迟时间，默认为 60
     */
    public long getLineDelay() {
        return config.getLong("line-delay", 60);
    }

    /**
     * 设置当前使用的 Ollama 模型，并保存配置文件。
     *
     * @param model 要设置的新 Ollama 模型
     */
    public void setOllamaModel(String model) {
        config.set("ollama.current_model", model);
        plugin.saveConfig();
    }

    /**
     * 设置 Ollama 的使用模式（内置或独立），并保存配置文件。
     *
     * @param mode 要设置的新 Ollama 使用模式
     */
    public void setOllamaMode(String mode) {
        config.set("ollama.mode", mode);
        plugin.saveConfig();
    }

    /**
     * 获取新 API 的模型。
     *
     * @return 新 API 的模型，默认为 "default-model"
     */
    public String getNewApiModel() {
        return config.getString("new-api.model", "default-model");
    }

    /**
     * 获取新 API 的请求地址。
     *
     * @return 新 API 的请求地址，默认为 "https://example.com/api/generate"
     */
    public String getNewApiUrl() {
        return config.getString("new-api.url", "https://example.com/api/generate");
    }

    /**
     * 获取访问新 API 所需的密钥。
     *
     * @return 访问新 API 所需的密钥，默认为 "your_api_key_here"
     */
    public String getNewApiKey() {
        return config.getString("new-api.key", "your_api_key_here");
    }

    /**
     * 设置新 API 的模型，并保存配置文件。
     *
     * @param model 要设置的新 API 模型
     */
    public void setNewApiModel(String model) {
        config.set("new-api.model", model);
        plugin.saveConfig();
    }
}