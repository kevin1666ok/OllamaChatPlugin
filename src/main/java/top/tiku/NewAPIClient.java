package top.tiku;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * NewAPIClient 类用于与新的 API 进行 HTTP 通信，
 * 发送请求并接收响应，处理与新 API 的交互逻辑。
 */
public class NewAPIClient {

    // 插件实例，用于记录日志等操作
    private JavaPlugin plugin;
    // 新 API 的访问密钥
    private String apiKey;
    // 当前使用的新 API 模型
    private String model;
    // 新 API 的请求地址
    private String apiUrl;

    /**
     * 构造函数，初始化 NewAPIClient 实例。
     *
     * @param plugin 插件实例，用于记录日志
     * @param apiUrl 新 API 的请求地址
     * @param apiKey 新 API 的访问密钥
     * @param model  当前使用的新 API 模型
     */
    public NewAPIClient(JavaPlugin plugin, String apiUrl, String apiKey, String model) {
        this.plugin = plugin;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * 向新 API 发送请求并获取响应的方法。
     *
     * @param userMessage 用户输入的消息
     * @return 新 API 返回的响应内容，如果出现异常则返回错误信息
     */
    public String sendRequestToNewAPI(String userMessage) {
        try {
            // 创建 URL 对象，指定新 API 的请求地址
            URL obj = new URL(apiUrl);
            // 打开 HTTP 连接
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // 设置请求方法为 POST
            con.setRequestMethod("POST");
            // 设置请求头，指定请求内容的类型为 JSON
            con.setRequestProperty("Content-Type", "application/json");
            // 设置请求头，添加 API 访问密钥
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            // 允许向连接中写入数据
            con.setDoOutput(true);

            // 创建 JSON 对象，包含请求所需的模型和用户消息
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("prompt", userMessage);

            // 获取输出流，将请求体以 UTF-8 编码写入连接
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 获取输入流，读取新 API 返回的响应
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                // 逐行读取响应内容并添加到 StringBuilder 中
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // 返回完整的响应内容
                return response.toString();
            }
        } catch (IOException e) {
            // 若出现 IO 异常，记录错误日志并返回错误信息
            plugin.getLogger().severe("Error sending request to New API: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 设置当前使用的新 API 模型。
     *
     * @param newModel 要设置的新模型名称
     */
    public void setModel(String newModel) {
        this.model = newModel;
    }
}