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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * OllamaClient 类负责与 Ollama 服务进行通信，发送请求并处理响应。
 */
public class OllamaClient {

    // 插件实例，用于记录日志
    private JavaPlugin plugin;
    // 使用的模型名称
    private String model;
    // Ollama 服务的 URL
    private String ollamaUrl;

    /**
     * 构造函数，初始化插件实例、模型名称和 Ollama 服务的 URL。
     *
     * @param plugin 插件实例
     * @param model 模型名称
     * @param ollamaUrl Ollama 服务的 URL
     */
    public OllamaClient(JavaPlugin plugin, String model, String ollamaUrl) {
        this.plugin = plugin;
        this.model = model;
        this.ollamaUrl = ollamaUrl;
    }

    /**
     * 向 Ollama 服务发送请求并处理响应的方法。
     *
     * @param message 要发送的消息
     * @return Ollama 服务的响应消息
     */
    public String sendRequestToOllama(String message) {
        plugin.getLogger().info("Sending request to Ollama with message: " + message);
        StringBuilder fullResponse = new StringBuilder();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建 HTTP POST 请求
            HttpPost request = new HttpPost(ollamaUrl);
            // 创建请求体的 JSON 对象
            JSONObject requestBody = new JSONObject();
            // 设置使用的模型
            requestBody.put("model", model);
            // 设置请求的提示消息
            requestBody.put("prompt", message);
            // 设置最大预测的 token 数量
            requestBody.put("num_predict", 200);
            plugin.getLogger().info("Request body: " + requestBody.toString());
            // 创建请求体的实体对象，并指定字符编码为 UTF-8
            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
            request.setEntity(entity);
            // 设置请求头的 Content-Type
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // 执行 HTTP 请求并获取响应
            HttpResponse response = httpClient.execute(request);
            // 创建 BufferedReader 用于读取响应内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String line;
            boolean isDone = false;
            // 逐行读取响应内容
            while ((line = reader.readLine()) != null && !isDone) {
                if (!line.isEmpty()) {
                    // 将每行响应内容解析为 JSON 对象
                    JSONObject jsonResponse = new JSONObject(line);
                    // 拼接响应内容
                    fullResponse.append(jsonResponse.getString("response"));
                    // 检查响应是否结束
                    isDone = jsonResponse.getBoolean("done");
                }
            }
            plugin.getLogger().info("Full response from Ollama: " + fullResponse.toString());
            return fullResponse.toString();
        } catch (IOException e) {
            // 记录通信错误日志
            plugin.getLogger().severe("Error communicating with Ollama: " + e.getMessage());
            return "An error occurred while communicating with Ollama.";
        }
    }
}