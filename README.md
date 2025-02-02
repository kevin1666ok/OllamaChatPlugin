# Ollama Chat Plugin 使用教程

## 一、插件简介

  

Ollama Chat Plugin 是一款为 Minecraft 服务器量身打造的插件。借助此插件，玩家能在游戏里通过 `/ollama` 命令和 Ollama 服务开展交互，实现聊天功能。玩家输入文本后，可获取模型生成的回复。此外，该插件支持拓展使用其他 API 服务，且能直接在配置文件里修改 API 相关设置。

## 二、安装与配置

### 2.1 安装插件

  

1.  保证已安装 Spigot 或者 Paper 服务器。
2.  把插件的 JAR 文件放置到服务器的 `plugins` 目录下。
3.  重启服务器，插件会自动加载并创建必要的配置文件和文件夹。

### 2.2 配置插件

  

插件的配置文件位于 `plugins/OllamaChatPlugin/config.yml`。使用文本编辑器打开该文件进行配置：

  

收起

yaml

```
# 配置 Ollama API 相关信息
ollama:
  # 指定要使用的 Ollama 模型
  model: llama2:7b
  # 指定 Ollama API 的请求地址
  url: http://localhost:11434/api/generate

# 配置新 API 相关信息
new-api:
  # 指定要使用的新 API 模型
  model: default-model
  # 指定新 API 的请求地址
  url: https://example.com/api/generate
  # 指定访问新 API 所需的密钥
  key: your_api_key_here

# 按行输出时每行之间的延迟时间（游戏刻）
line-delay: 60
# 可选择使用的 API 类型，可选值为 "ollama" 或 "new-api"
api-type: ollama
```

  

-   **`ollama` 部分**：这部分配置与 Ollama API 相关的信息。
    -   `model`：可指定要使用的 Ollama 模型，默认是 `llama2:7b`，你可以按需改成其他可用的模型。
    -   `url`：Ollama API 的请求地址，默认是 `http://localhost:11434/api/generate`，若你的 Ollama 服务部署在其他地址，需进行相应修改。
-   **`new-api` 部分**：这是配置自定义新 API 服务的信息。
    -   `model`：指定要使用的新 API 模型，默认是 `default-model`。
    -   `url`：新 API 的请求地址，默认是 `https://example.com/api/generate`，需替换成实际的 API 地址。
    -   `key`：访问新 API 所需的密钥，默认是 `your_api_key_here`，需替换成真实的 API 密钥。
-   **`line-delay`**：按行输出时每行之间的延迟时间（游戏刻），默认值为 60，你可根据喜好调整以改变打字效果的速度。
-   **`api-type`**：可选择使用的 API 类型，可选值为 `"ollama"` 或 `"new-api"`，以此决定使用 Ollama API 还是新的 API 服务。

## 三、使用方法

### 3.1 发送消息

  

在游戏内，玩家使用 `/ollama <message>` 命令向所选的 API 服务发送消息。例如，输入 `/ollama 你好`，就会向对应 API 模型发送 “你好” 这条消息。

### 3.2 获取回复

  

插件会异步处理请求，按行以打字效果显示 API 模型的回复。等待回复时，玩家会看到 “正在生成...” 的提示。

## 四、添加其他 API 的详细步骤

### 4.1 准备工作

  

在添加其他 API 之前，你需要了解该 API 的以下基本信息：

  

-   **API 地址**：用于发送请求的 URL。
-   **请求方法**：通常为 `GET` 或 `POST`。
-   **请求参数**：如 API 密钥、模型名称、提示文本等。
-   **响应格式**：API 返回的响应数据格式，如 JSON、XML 等。

### 4.2 修改配置文件

  

在 `config.yml` 中添加或修改新 API 的相关配置：

  

收起

yaml

```
new-api:
  model: your_new_api_model
  url: your_new_api_url
  key: your_new_api_key
```

  

将 `your_new_api_model`、`your_new_api_url` 和 `your_new_api_key` 替换为实际的模型名称、API 地址和 API 密钥。

### 4.3 修改代码以适配新 API（若有必要）

  

如果新 API 的请求格式或响应格式与现有代码不兼容，可能需要对 `NewAPIClient.java` 类进行修改。例如，如果新 API 的请求参数名或响应字段名不同，需要相应调整代码中的 `JSONObject` 操作。以下是 `NewAPIClient.java` 的示例代码：

  

收起

java

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
    // 指定要使用的新 API 模型
    private String model;

    // 构造函数，初始化插件实例、新 API 地址、API 密钥和模型名称
    public NewAPIClient(JavaPlugin plugin, String apiUrl, String apiKey, String model) {
        this.plugin = plugin;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
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
            // 将用户输入的消息作为提示信息添加到请求体中
            requestBody.put("prompt", message);
            // 将 API 密钥添加到请求体中
            requestBody.put("api_key", apiKey);
            // 将使用的新 API 模型添加到请求体中
            requestBody.put("model", model);
            // 创建一个 StringEntity 对象，将请求体以 UTF - 8 编码设置到请求中
            request.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));
            // 设置请求头，指定请求体的内容类型为 JSON 且编码为 UTF - 8
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

### 4.4 切换 API 类型

  

修改 `config.yml` 中的 `api-type` 配置项，将其设置为 `"new-api"` 即可使用新配置的 API 服务：

  

收起

yaml

```
api-type: new-api
```

### 4.5 重新打包和部署

  

完成代码修改和配置文件更新后，使用 Maven 重新打包项目：

  

收起

bash

```
mvn clean package
```

  

把新生成的 JAR 文件部署到 Minecraft 服务器的 `plugins` 目录下，重启服务器，插件就会使用新配置的 API 进行交互。

## 五、常见问题与解决方法

### 5.1 配置文件夹丢失

  

若插件启动时提示配置文件夹创建失败，或者配置文件夹意外丢失，插件会在下次启动时自动尝试重新创建。若仍无法创建，请检查服务器对该文件夹的读写权限。

### 5.2 模型更换无效

  

更换 `config.yml` 中的模型名称后，确保服务器已正确重启，以使新配置生效。如果使用代码方式修改模型名称，修改后需要重新打包并部署插件。同时，确认更换的模型在相应 API 服务中已正确安装和配置。

### 5.3 网络连接问题

  

若插件无法连接到 API 服务，请检查 API 地址的配置是否正确，以及 API 服务是否正在运行且网络可达。可以通过在服务器上使用 `ping` 命令或尝试在浏览器中访问该 URL 来测试连接。

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

  

如果你在使用过程中遇到任何问题或有任何建议，欢迎通过 [你的联系方式] 与我们联系。我们将尽力为你提供帮助。
<!--stackedit_data:
eyJoaXN0b3J5IjpbODM0MDQ3MDIzLC01MzY4MjM1NzUsNDU2MD
UxNTgwXX0=
-->