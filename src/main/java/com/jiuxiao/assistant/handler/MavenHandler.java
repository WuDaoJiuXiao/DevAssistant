package com.jiuxiao.assistant.handler;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.util.net.HttpConfigurable;
import com.jiuxiao.assistant.dto.maven.AliMavenRequestParam;
import com.jiuxiao.assistant.dto.maven.NetEaseMavenRequestBody;
import com.jiuxiao.assistant.dto.maven.NetEaseMavenRequestData;
import com.jiuxiao.assistant.enums.MavenRepositoryEnum;
import com.jiuxiao.assistant.util.SystemHandleUtil;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Maven依赖处理器类，用于处理Maven依赖的搜索和获取功能。
 * 该类提供了从不同Maven仓库（如阿里云、网易、Maven中央仓库）搜索依赖的能力，
 * 并支持生成依赖XML和复制到剪贴板等功能。
 *
 * @author 悟道九霄
 * @date 2026/4/26
 */
public class MavenHandler {
    private static final int CONNECTION_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 30;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String currentMirror = "maven-central";
    private List<MavenArtifact> currentResults = new ArrayList<>();

    /**
     * 构造MavenHandler实例。
     * 初始化Gson和OkHttpClient，并配置代理设置。
     * 支持通过IDEA的代理配置进行网络请求。
     */
    public MavenHandler() {
        this.gson = new Gson();

        // 配置OkHttpClient，支持代理
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

        // 设置代理（使用IDEA的代理配置）
        HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        if (httpConfigurable.USE_HTTP_PROXY) {
            String proxyHost = httpConfigurable.PROXY_HOST;
            int proxyPort = httpConfigurable.PROXY_PORT;
            if (proxyHost != null && !proxyHost.isEmpty()) {
                java.net.Proxy proxy = new java.net.Proxy(
                        java.net.Proxy.Type.HTTP,
                        new java.net.InetSocketAddress(proxyHost, proxyPort)
                );
                builder.proxy(proxy);
            }
        }

        this.httpClient = builder.build();
    }

    /**
     * 获取镜像选择列表
     *
     * @return 镜像名称列表
     */
    public List<String> getMirrorList() {
        return MavenRepositoryEnum.descList();
    }

    /**
     * 设置当前使用的镜像
     *
     * @param mirrorName 镜像名称
     */
    public void setCurrentMirror(String mirrorName) {
        if (Objects.nonNull(MavenRepositoryEnum.findByDesc(mirrorName))) {
            this.currentMirror = mirrorName;
        }
    }

    /**
     * 重置搜索状态
     */
    private void resetSearchState() {
        currentResults.clear();
    }

    /**
     * 执行新搜索
     *
     * @param artifactId ArtifactId（支持模糊搜索）
     * @param version    Version（支持模糊搜索）
     * @param mirrorName 镜像名称
     * @return 搜索结果列表
     */
    public List<MavenArtifact> search(String artifactId, String version, String mirrorName) {
        resetSearchState();
        String lastArtifactId = artifactId != null ? artifactId.trim() : "";
        String lastMirror = mirrorName != null ? mirrorName : currentMirror;

        if (lastArtifactId.isEmpty()) {
            return new ArrayList<>();
        }

        setCurrentMirror(lastMirror);
        return doSearchMavenList(artifactId, version);
    }

    /**
     * 执行Maven依赖搜索
     *
     * @param artifactId ArtifactId（支持模糊搜索）
     * @param version    Version（支持模糊搜索）
     * @return 搜索结果列表
     */
    public List<MavenArtifact> doSearchMavenList(String artifactId, String version) {
        List<MavenArtifact> mavenArtifacts = new ArrayList<>();
        MavenRepositoryEnum repositoryEnum = MavenRepositoryEnum.findByDesc(currentMirror);
        if (Objects.isNull(repositoryEnum)) {
            return mavenArtifacts;
        }

        try {
            switch (repositoryEnum) {
                case ALI_YUN:
                    mavenArtifacts = searchFromAliYun(artifactId, version);
                    break;
                case NET_EASE:
                    mavenArtifacts = searchFromNetEase(artifactId, version);
                    break;
                default:
                    mavenArtifacts = searchFromMavenCentral(artifactId, version);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentResults = mavenArtifacts;
        return mavenArtifacts;
    }

    /**
     * 获取当前所有结果
     */
    public List<MavenArtifact> getCurrentResults() {
        return new ArrayList<>(currentResults);
    }

    /**
     * 从网易镜像搜索Maven依赖
     *
     * @param artifactId ArtifactId
     * @param version    版本
     * @return 搜索结果列表
     */
    private List<MavenArtifact> searchFromNetEase(String artifactId, String version) {
        List<MavenArtifact> mavenArtifacts = new ArrayList<>();
        String requestUrl = MavenRepositoryEnum.NET_EASE.getUrl();

        NetEaseMavenRequestData requestData = new NetEaseMavenRequestData();
        // 构建请求数据
        NetEaseMavenRequestData.SortParam sortParam = new NetEaseMavenRequestData.SortParam();
        requestData.setSort(List.of(sortParam));

        List<NetEaseMavenRequestData.FilterParam> filterParams = new ArrayList<>();
        // 添加过滤条件
        if (Objects.nonNull(artifactId) && StringUtils.isNotEmpty(artifactId)) {
            filterParams.add(new NetEaseMavenRequestData.FilterParam("name.raw", artifactId));
        }
        if (Objects.nonNull(version) && StringUtils.isNotEmpty(version)) {
            filterParams.add(new NetEaseMavenRequestData.FilterParam("version", version));
        }
        requestData.setFilter(filterParams);

        NetEaseMavenRequestBody requestBody = new NetEaseMavenRequestBody();
        // 构建请求体
        requestBody.setData(List.of(requestData));

        try {
            String responseBody = executeHttpPost(requestUrl, JSON.toJSONString(requestBody));
            // 执行HTTP POST请求
            if (responseBody != null) {
                mavenArtifacts = parseNetEaseResponse(responseBody, version);
                // 解析响应数据
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mavenArtifacts;
    }

    /**
     * 从阿里云镜像搜索Maven依赖
     *
     * @param artifactId ArtifactId
     * @param version    版本
     * @return 搜索结果列表
     */
    private List<MavenArtifact> searchFromAliYun(String artifactId, String version) {
        List<MavenArtifact> mavenArtifacts = new ArrayList<>();
        String baseUrl = MavenRepositoryEnum.ALI_YUN.getUrl();
        AliMavenRequestParam requestParam = new AliMavenRequestParam();
        requestParam.setArtifactId(artifactId);
        requestParam.setVersion(version);
        String requestUrl = baseUrl + requestParam.buildRequestUrl();

        try {
            String responseBody = executeHttpGet(requestUrl);
            // 执行HTTP GET请求
            if (responseBody != null) {
                mavenArtifacts = parseAliYunResponse(responseBody, version);
                // 解析响应数据
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mavenArtifacts;
    }

        /**
         * 从Maven中央仓库搜索依赖
         *
         * @param artifactId ArtifactId
         * @param version    版本
         * @return 搜索结果列表
         */
    private List<MavenArtifact> searchFromMavenCentral(String artifactId, String version) {
        List<MavenArtifact> results = new ArrayList<>();
        String url = String.format(
                // 构建请求URL
                MavenRepositoryEnum.MAVEN_CENTER.getUrl(),
                encodeUrl(artifactId), 100, 1
        );

        try {
            String responseBody = executeHttpGet(url);
            // 执行HTTP GET请求
            if (responseBody != null) {
                results = parseMavenCentralResponse(responseBody, version);
                // 解析响应数据
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 执行HTTP GET请求
     *
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException IO异常
     */
    private String executeHttpGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "IntelliJ IDEA Maven Assistant")
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }
        return null;
    }

    /**
     * 执行HTTP POST请求
     *
     * @param url      请求URL
     * @param jsonBody JSON请求体
     * @return 响应内容
     * @throws IOException IO异常
     */
    private String executeHttpPost(String url, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "IntelliJ IDEA Maven Assistant")
                .header("Accept", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }
        return null;
    }

    /**
     * 解析阿里云镜像的响应数据
     *
     * @param responseBody 响应内容
     * @param version      版本过滤条件
     * @return 解析后的依赖列表
     */
    private List<MavenArtifact> parseAliYunResponse(String responseBody, String version) {
        List<MavenArtifact> results = new ArrayList<>();

        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

            // 检查是否成功
            boolean successful = jsonObject.get("successful").getAsBoolean();
            if (!successful) return results;

            JsonArray objects = jsonObject.getAsJsonArray("object");
            if (objects == null || objects.size() == 0) return results;

            for (int i = 0; i < objects.size(); i++) {
                // 遍历响应数据
                JsonObject obj = objects.get(i).getAsJsonObject();

                String groupId = getJsonString(obj, "groupId");
                // 处理阿里云特殊格式：如果 groupId 是 "#"，跳过这条记录
                if ("#".equals(groupId) || groupId == null || groupId.isEmpty()) {
                    continue;
                }

                String artifactId = getJsonString(obj, "artifactId");
                String artifactVersion = getJsonString(obj, "version");

                // 版本过滤
                if (version != null && !version.isEmpty()) {
                    if (!artifactVersion.toLowerCase().contains(version.toLowerCase())) {
                        continue;
                    }
                }

                MavenArtifact artifact = new MavenArtifact();
                artifact.setGroupId(groupId);
                artifact.setArtifactId(artifactId);
                artifact.setVersion(artifactVersion);
                artifact.setMirror(currentMirror);

                results.add(artifact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 解析网易镜像的响应数据
     *
     * @param responseBody 响应内容
     * @param version      版本过滤条件
     * @return 解析后的依赖列表
     */
    private List<MavenArtifact> parseNetEaseResponse(String responseBody, String version) {
        List<MavenArtifact> results = new ArrayList<>();

        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

            // 获取 result 对象
            JsonObject resultObj = jsonObject.getAsJsonObject("result");
            if (resultObj == null) return results;

            // 检查是否成功
            boolean success = resultObj.get("success").getAsBoolean();
            if (!success) return results;

            // 获取 data 数组
            JsonArray dataArray = resultObj.getAsJsonArray("data");
            if (dataArray == null || dataArray.size() == 0) return results;

            for (int i = 0; i < dataArray.size(); i++) {
                // 遍历响应数据
                JsonObject dataItem = dataArray.get(i).getAsJsonObject();

                String groupId = getJsonString(dataItem, "group");
                String artifactId = getJsonString(dataItem, "name");
                String artifactVersion = getJsonString(dataItem, "version");

                // 版本过滤
                if (version != null && !version.isEmpty()) {
                    if (!artifactVersion.toLowerCase().contains(version.toLowerCase())) {
                        continue;
                    }
                }

                MavenArtifact artifact = new MavenArtifact();
                artifact.setGroupId(groupId);
                artifact.setArtifactId(artifactId);
                artifact.setVersion(artifactVersion);
                artifact.setMirror(currentMirror);

                results.add(artifact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 解析Maven中央仓库的响应数据
     *
     * @param responseBody  响应内容
     * @param searchVersion 版本过滤条件
     * @return 解析后的依赖列表
     */
    private List<MavenArtifact> parseMavenCentralResponse(String responseBody, String searchVersion) {
        List<MavenArtifact> results = new ArrayList<>();

        try {
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonObject responseObj = jsonObject.getAsJsonObject("response");

            if (responseObj == null) return results;

            JsonArray docs = responseObj.getAsJsonArray("docs");
            if (docs == null) return results;

            for (int i = 0; i < docs.size(); i++) {
                // 遍历响应数据
                JsonObject doc = docs.get(i).getAsJsonObject();

                String groupId = getJsonString(doc, "g");
                String artifactId = getJsonString(doc, "a");
                String version = getJsonString(doc, "latestVersion");
                if (version == null || version.isEmpty()) {
                    version = getJsonString(doc, "v");
                }

                if (searchVersion != null && !searchVersion.isEmpty()) {
                    if (!version.toLowerCase().contains(searchVersion.toLowerCase())) {
                        continue;
                    }
                }

                MavenArtifact artifact = new MavenArtifact();
                artifact.setGroupId(groupId);
                artifact.setArtifactId(artifactId);
                artifact.setVersion(version);
                artifact.setMirror(currentMirror);

                results.add(artifact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * 生成Maven依赖XML
     *
     * @param artifact Maven制品信息
     * @return Maven依赖XML字符串
     */
    public String generateDependencyXml(MavenArtifact artifact) {
        if (artifact == null) {
            return "";
        }

        return "<dependency>\n" +
                "    <groupId>" + escapeXml(artifact.getGroupId()) + "</groupId>\n" +
                "    <artifactId>" + escapeXml(artifact.getArtifactId()) + "</artifactId>\n" +
                "    <version>" + escapeXml(artifact.getVersion()) + "</version>\n" +
                "</dependency>";
    }

    /**
     * 复制依赖到剪贴板
     *
     * @param artifact Maven制品信息
     * @return 是否复制成功
     */
    public boolean copyToClipboard(MavenArtifact artifact) {
        if (artifact == null) {
            return false;
        }

        String dependencyXml = generateDependencyXml(artifact);
        if (dependencyXml.isEmpty()) {
            return false;
        }
        SystemHandleUtil.copyToClipboard(dependencyXml);
        return true;
    }

    /**
     * 从JSON对象中获取字符串值
     *
     * @param obj JSON对象
     * @param key 键名
     * @return 字符串值，如果不存在或为null则返回空字符串
     */
    private String getJsonString(JsonObject obj, String key) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            try {
                return obj.get(key).getAsString();
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    /**
     * 将给定的字符串值编码为URL安全格式。
     *
     * @param value 要编码的字符串值，如果为null则返回空字符串
     * @return 编码后的URL安全字符串，使用UTF-8字符集编码
     * @see java.net.URLEncoder#encode(java.lang.String, java.nio.charset.Charset)
     */
    private String encodeUrl(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串中的特殊XML字符进行转义处理。
     * 转义规则如下：
     * - & 转换为 &amp;
     * - < 转换为 &lt;
     * - > 转换为 &gt;
     * - " 转换为 &quot;
     * - ' 转换为 &apos;
     * 注意：该方法不处理制表符(\t)、回车符(\r)和换行符(\n)
     *
     * @param text 需要转义的原始字符串，如果为null则返回空字符串
     * @return 转义后的XML安全字符串
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Maven制品信息类
     */
    @Data
    public static class MavenArtifact {
        private String groupId;
        private String artifactId;
        private String version;
        private String mirror;
    }
}