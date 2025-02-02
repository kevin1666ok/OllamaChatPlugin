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

// 该类用于与 Ollama API 进行交互
public class OllamaClient {

    // 引用插件主类，用于记录日志等操作
    private JavaPlugin plugin;
    // 指定要使用的 Ollama 模型
    private String model;
    // Ollama API 的请求地址
    private String ollamaUrl;

    // 构造函数，初始化插件实例、Ollama 模型名称和 API 地址
    public OllamaClient(JavaPlugin plugin, String model, String ollamaUrl) {
        this.plugin = plugin;
        this.model = model;
        this.ollamaUrl = ollamaUrl;
    }

    // 向 Ollama API 发送请求并获取响应的方法
    public String sendRequestToOllama(String message) {
        // 记录发送请求的日志信息
        plugin.getLogger().info("Sending request to Ollama with message: " + message);
        // 用于存储完整的响应信息
        StringBuilder fullResponse = new StringBuilder();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建一个 HTTP POST 请求对象，使用 Ollama API 的地址
            HttpPost request = new HttpPost(ollamaUrl);
            // 创建一个 JSON 对象用于存储请求体参数
            JSONObject requestBody = new JSONObject();
            // 将使用的 Ollama 模型添加到请求体中
            requestBody.put("model", model);
            // 将用户输入的消息作为提示信息添加到请求体中
            requestBody.put("prompt", message);
            // 设置预测的最大字符数
            requestBody.put("num_predict", 200);
            // 记录请求体的日志信息
            plugin.getLogger().info("Request body: " + requestBody.toString());
            // 创建一个 StringEntity 对象，将请求体以 UTF - 8 编码设置到请求中
            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
            request.setEntity(entity);
            // 设置请求头，指定请求体的内容类型为 JSON 且编码为 UTF - 8
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // 执行 HTTP 请求并获取响应
            HttpResponse response = httpClient.execute(request);
            // 创建一个 BufferedReader 用于读取响应内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String line;
            boolean isDone = false;
            // 逐行读取响应内容
            while ((line = reader.readLine()) != null && !isDone) {
                if (!line.isEmpty()) {
                    // 将每行响应内容解析为 JSON 对象
                    JSONObject jsonResponse = new JSONObject(line);
                    // 将响应中的回复信息添加到完整响应中
                    fullResponse.append(jsonResponse.getString("response"));
                    // 判断是否已经完成响应
                    isDone = jsonResponse.getBoolean("done");
                }
            }
            // 记录完整响应的日志信息
            plugin.getLogger().info("Full response from Ollama: " + fullResponse.toString());
            return fullResponse.toString();
        } catch (IOException e) {
            // 若发生网络异常，记录错误日志并返回错误提示信息
            plugin.getLogger().severe("Error communicating with Ollama: " + e.getMessage());
            return "An error occurred while communicating with Ollama.";
        }
    }
}