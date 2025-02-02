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