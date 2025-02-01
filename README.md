# Ollama Chat Plugin 使用教程

## 一、插件简介

  

Ollama Chat Plugin 是一款用于 Minecraft 服务器的插件，它允许玩家在游戏内通过 `/ollama` 命令与 Ollama 服务进行交互，实现聊天功能。通过该插件，玩家能够向 Ollama 模型发送文本，并获取模型生成的回复。同时，本插件也支持拓展使用其他 API 服务，下面将详细介绍。

## 二、安装与配置

### 2.1 安装插件

  

-   确保你已经安装了 Spigot 或 Paper 服务器。
-   将插件的 JAR 文件放置在服务器的 `plugins` 目录下。
-   重启服务器，插件将自动加载并创建必要的配置文件和文件夹。

### 2.2 配置插件（使用 Ollama API）

  

插件的配置文件位于 `plugins/OllamaChatPlugin/config.yml`。你可以使用文本编辑器打开该文件进行配置：

  

-   `model`：指定要使用的 Ollama 模型，默认值为 `llama2:7b`。你可以根据需求修改为其他可用的模型，如 `llama2:13b`。
-   `ollama - url`：Ollama 服务的 API 地址，默认值为 `http://localhost:11434/api/generate`。如果你的 Ollama 服务部署在其他地址或端口，需要相应修改。
-   `line - delay`：按行输出时每行之间的延迟时间（游戏刻），默认值为 `60`。可根据喜好调整该值以改变打字效果的速度。

## 三、使用方法（使用 Ollama API）

### 3.1 发送消息

  

在游戏内，玩家可以使用 `/ollama <message>` 命令向 Ollama 服务发送消息。例如，输入 `/ollama 你好`，即可向 Ollama 模型发送 “你好” 这条消息。

### 3.2 获取回复

  

插件会异步处理请求，并按行以打字效果显示 Ollama 模型的回复。在等待回复时，玩家会看到 “正在生成...” 的提示。

## 四、添加其他 API 的教程

### 4.1 准备工作
（未来会更新直接添加到配置文件功能，此部分暂时AI替写）



在添加其他 API 之前，你需要了解该 API 的基本信息，包括：

  

-   **API 地址**：用于发送请求的 URL。
-   **请求方法**：通常为 `GET` 或 `POST`。
-   **请求参数**：API 所需的参数，如 API 密钥、模型名称、提示文本等。
-   **响应格式**：API 返回的响应数据格式，如 JSON、XML 等。

### 4.2 修改代码以支持新 API

#### 4.2.1 创建新的客户端类

  

在项目中创建一个新的 Java 类，例如 `NewAPIClient.java`，用于与新的 API 进行交互。以下是一个简单的示例：

  


```
package top.tiku;

import org.bukkit.plugin.java.JavaPlugin;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

// 该类用于与新的 API 进行交互
public class NewAPIClient {

    // 引用插件主类，用于记录日志等操作
    private JavaPlugin plugin;
    // 新 API 的请求地址
    private String apiUrl;
    // 访问新 API 所需的密钥
    private String apiKey;

    // 构造函数，初始化插件实例、API 地址和 API 密钥
    public NewAPIClient(JavaPlugin plugin, String apiUrl, String apiKey) {
        this.plugin = plugin;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    // 向新 API 发送请求并获取响应的方法
    public String sendRequestToNewAPI(String message) {
        // 记录发送请求的日志信息
        plugin.getLogger().info("Sending request to New API with message: " + message);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建一个 HTTP POST 请求对象，使用新 API 的地址
            HttpPost request = new HttpPost(apiUrl);
            // 创建一个 JSON 对象用于存储请求体参数
            JSONObject requestBody = new JSONObject();
            // 根据新 API 的要求，将用户输入的消息作为提示信息添加到请求体中
            requestBody.put("prompt", message);
            // 将 API 密钥添加到请求体中
            requestBody.put("api_key", apiKey);

            // 创建一个 StringEntity 对象，将请求体以 UTF-8 编码设置到请求中
            request.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));
            // 设置请求头，指定请求体的内容类型为 JSON 且编码为 UTF-8
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // 执行 HTTP 请求并获取响应
            HttpResponse response = httpClient.execute(request);
            // 将响应实体转换为字符串
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            // 记录原始响应的日志信息
            plugin.getLogger().info("Raw response from New API: " + responseBody);
            // 将响应体解析为 JSON 对象
            JSONObject jsonResponse = new JSONObject(responseBody);
            // 从 JSON 响应中提取所需的回复信息
            String responseStr = jsonResponse.getString("response");
            // 记录解析后的响应日志信息
            plugin.getLogger().info("Parsed response from New API: " + responseStr);
            return responseStr;
        } catch (IOException e) {
            // 若发生网络异常，记录错误日志并返回错误提示信息
            plugin.getLogger().severe("Error communicating with New API: " + e.getMessage());
            return "An error occurred while communicating with New API.";
        }
    }
}
```

#### 4.2.2 修改主插件类

  

在 `OllamaChatPlugin.java` 中添加对新 API 客户端的支持。你可以通过配置文件来选择使用哪个 API，例如添加一个新的配置项 `api - type`，值可以是 `ollama` 或 `new - api`。

  
```
package top.tiku;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;

// 插件主类，继承自 JavaPlugin
public class OllamaChatPlugin extends JavaPlugin {

    // Ollama API 客户端实例
    private OllamaClient ollamaClient;
    // 新 API 客户端实例
    private NewAPIClient newAPIClient;
    // 用于读取和管理配置文件的对象
    private FileConfiguration config;

    // 插件启用时调用的方法，进行初始化操作
    @Override
    public void onEnable() {
        // 创建配置文件夹
        createConfigFolder();
        // 保存默认配置文件
        saveDefaultConfig();
        // 获取配置文件内容
        config = getConfig();
        // 记录插件启用的日志信息
        getLogger().info("Ollama Chat Plugin has been enabled!");

        // 初始化 Ollama 客户端，传入插件实例、Ollama 模型名称和 API 地址
        ollamaClient = new OllamaClient(this, config.getString("model"), config.getString("ollama-url"));
        // 初始化新 API 客户端，传入插件实例、新 API 地址和 API 密钥
        newAPIClient = new NewAPIClient(this, config.getString("new-api-url"), config.getString("new-api-key"));
    }

    // 插件禁用时调用的方法，记录插件禁用的日志信息
    @Override
    public void onDisable() {
        getLogger().info("Ollama Chat Plugin has been disabled!");
    }

    // 处理玩家命令的方法
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ollama")) {
            if (args.length == 0) {
                // 若玩家未输入消息，提示正确的命令使用方法
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

            // 异步执行请求操作，避免阻塞主线程
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
```

#### 4.2.3 更新配置文件

  

在 `config.yml` 中添加新 API 的相关配置：

  



```
model: llama2:7b
ollama-url: http://localhost:11434/api/generate
line-delay: 60
api-type: ollama  # 可选择 "ollama" 或 "new-api"
new-api-url: https://example.com/api/generate
new-api-key: your_api_key_here
```

### 4.3 重新打包和部署

  

完成代码修改和配置文件更新后，使用 Maven 重新打包项目：

  





```
mvn clean package
```

  

将新生成的 JAR 文件部署到 Minecraft 服务器的 `plugins` 目录下，重启服务器。现在，你可以通过修改 `api - type` 配置项来选择使用 Ollama API 还是新的 API。

## 五、常见问题与解决方法

### 5.1 配置文件夹丢失

  

如果插件启动时提示配置文件夹创建失败，或者配置文件夹意外丢失，插件会在下次启动时自动尝试重新创建。如果仍然无法创建，请检查服务器对该文件夹的读写权限。

### 5.2 模型更换无效

  

更换 `config.yml` 中的模型名称后，确保服务器已正确重启，以使新配置生效。如果使用代码方式修改模型名称，修改后需要重新打包并部署插件。同时，确认更换的模型在相应 API 服务中已正确安装和配置。

### 5.3 网络连接问题

  

如果插件无法连接到 API 服务，请检查 API 地址的配置是否正确，以及 API 服务是否正在运行且网络可达。可以通过在服务器上使用 `ping` 命令或尝试在浏览器中访问该 URL 来测试连接。

### 5.4 新 API 配置错误

  

确保新 API 的请求参数和响应格式在代码中正确处理。如果遇到问题，可以查看服务器日志，定位错误信息。

## 六、更新插件

### 6.1 备份

  

在更新插件之前，强烈建议备份 `plugins/OllamaChatPlugin` 目录下的所有文件，特别是 `config.yml` 文件，以防止配置丢失。

### 6.2 替换 JAR 文件

  

将新的插件 JAR 文件替换 `plugins` 目录下的旧文件。

### 6.3 重启服务器

  

重启 Minecraft 服务器，使新插件生效。

## 七、联系与支持

  

如果你在使用过程中遇到任何问题或有任何建议，欢迎通过QQ:2758622724[图片上传中...(image-ow9DX92kjxi8PVDl)]与我们联系。我们将尽力为你提供帮助。

  

希望本教程能帮助你顺利使用 Ollama Chat Plugin，享受在 Minecraft 中与不同 API 交互的乐趣！
