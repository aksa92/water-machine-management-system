package com.campus.water.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger/OpenAPI配置类
 * 功能：配置API文档生成和展示
 * 用途：
 *   1. 自动生成API文档：基于代码注解
 *   2. 在线API测试：提供交互式测试界面
 *   3. 接口规范定义：统一API响应格式和错误码
 *   4. 权限控制：JWT token验证配置
 * 访问地址：
 *   - Swagger UI: http://localhost:8080/swagger-ui/index.html
 *   - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 核心注解：
 *   - @Configuration: 声明为配置类
 *   - @Profile: 指定生效的环境（开发/测试环境）
 */
@Configuration
@Profile({"dev", "test"}) // 只在开发测试环境启用，生产环境关闭
public class SwaggerConfig {

    @Value("${spring.application.name:校园直饮矿化水物联网运维平台}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 创建OpenAPI配置
     * @return OpenAPI配置实例
     * 功能：定义API文档的基本信息、安全方案、服务器等
     * 结构：
     *   - info: API基本信息（标题、版本、描述等）
     *   - servers: API服务器地址
     *   - components: 可重用的组件（安全方案、响应模型等）
     *   - security: 全局安全要求
     */
    @Bean
    public OpenAPI campusWaterOpenAPI() {
        return new OpenAPI()
                // 1. API基本信息
                .info(buildApiInfo())

                // 2. API服务器配置
                .servers(buildServers())

                // 3. 安全方案配置（JWT Bearer Token）
                .components(buildComponents())
                .addSecurityItem(buildSecurityRequirement())

                // 4. 全局标签（可在此定义，也可以在Controller上使用@Tag）
                // .tags(buildTags())

                // 5. 外部文档链接
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("项目GitHub仓库")
                        .url("https://github.com/campus-water/water-management-system"));
    }

    /**
     * 构建API基本信息
     * @return Info对象
     * 功能：定义API的标题、版本、描述、联系方式等
     */
    private Info buildApiInfo() {
        return new Info()
                .title("校园直饮矿化水物联网运维平台 API")
                .version("v1.0.0")
                .description("""
                    ## 项目概述
                    
                    校园直饮矿化水物联网运维平台是一个集设备监控、数据统计、告警管理、运维调度于一体的智能化管理系统。
                    
                    ## 功能模块
                    
                    ### 1. 设备管理
                    - 设备状态监控（在线/离线/故障）
                    - 设备信息维护
                    - 设备区域分配
                    
                    ### 2. 数据统计
                    - 用水量统计（按设备/区域/时间）
                    - 告警统计分析
                    - 设备使用率统计
                    - 水质数据统计
                    
                    ### 3. 告警管理
                    - 实时告警监控
                    - 告警处理流程
                    - 告警统计分析
                    
                    ### 4. 工单管理
                    - 维修工单创建
                    - 工单分配和跟踪
                    - 维修结果反馈
                    
                    ### 5. 用户管理
                    - 多角色权限控制（学生/维修人员/管理员）
                    - 个人信息管理
                    - 饮水记录查询
                    
                    ## 接口规范
                    
                    ### 响应格式
                    ```json
                    {
                      "code": 200,          // 状态码
                      "msg": "success",     // 消息
                      "data": {}            // 数据
                    }
                    ```
                    
                    ### 状态码说明
                    - 200: 成功
                    - 400: 请求参数错误
                    - 401: 未授权
                    - 403: 禁止访问
                    - 404: 资源不存在
                    - 500: 服务器内部错误
                    """)
                .termsOfService("https://campus-water.com/terms")
                .contact(buildContact())
                .license(buildLicense());
    }

    /**
     * 构建联系信息
     * @return Contact对象
     * 功能：提供项目联系人和联系方式
     */
    private Contact buildContact() {
        return new Contact()
                .name("开发团队")
                .url("https://campus-water.com")
                .email("dev@campus-water.com")
                .extensions(new java.util.HashMap<>() {{
                    put("技术支持", "support@campus-water.com");
                    put("产品反馈", "feedback@campus-water.com");
                }});
    }

    /**
     * 构建许可证信息
     * @return License对象
     * 功能：定义API的使用许可证
     */
    private License buildLicense() {
        return new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                .identifier("Apache-2.0");
    }

    /**
     * 构建服务器配置
     * @return 服务器列表
     * 功能：定义API的访问地址，支持多环境
     */
    private List<Server> buildServers() {
        return Arrays.asList(
                // 本地开发环境
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("本地开发环境")
                        .variables(new java.util.HashMap<>() {{
                            put("port", new io.swagger.v3.oas.models.servers.ServerVariable()
                                    ._default(serverPort)
                                    .description("服务器端口"));
                        }}),

                // 测试环境
                new Server()
                        .url("https://test.campus-water.com")
                        .description("测试环境")
                        .variables(new java.util.HashMap<>() {{
                            put("env", new io.swagger.v3.oas.models.servers.ServerVariable()
                                    ._default("test")
                                    .description("环境标识")
                                    ._enum(Arrays.asList("test", "staging")));
                        }}),

                // 生产环境
                new Server()
                        .url("https://api.campus-water.com")
                        .description("生产环境")
                        .variables(new java.util.HashMap<>() {{
                            put("version", new io.swagger.v3.oas.models.servers.ServerVariable()
                                    ._default("v1")
                                    .description("API版本")
                                    ._enum(Arrays.asList("v1", "v2")));
                        }})
        );
    }

    /**
     * 构建组件配置
     * @return Components对象
     * 功能：定义可重用的组件，如安全方案、响应模型等
     */
    private Components buildComponents() {
        return new Components()
                // 1. JWT Bearer Token安全方案
                .addSecuritySchemes("Bearer Token", buildJwtSecurityScheme())

                // 2. API Key安全方案（备用）
                .addSecuritySchemes("API Key", buildApiKeySecurityScheme())

                // 3. 通用响应模型
                .addSchemas("ResultVO", buildResultVOSchema())
                .addSchemas("PageResult", buildPageResultSchema())

                // 4. 通用请求头
                .addParameters("X-User-Id", buildUserIdHeader())
                .addParameters("X-User-Type", buildUserTypeHeader());
    }

    /**
     * 构建JWT安全方案
     * @return SecurityScheme对象
     * 功能：定义JWT Bearer Token的认证方式
     */
    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                    JWT Bearer Token认证
                    
                    ### 获取Token
                    1. 调用登录接口获取token
                    2. 在请求头中添加：Authorization: Bearer {token}
                    
                    ### Token格式
                    ```json
                    {
                      "sub": "用户ID",
                      "username": "用户名",
                      "userType": "用户类型",
                      "iat": 签发时间,
                      "exp": 过期时间
                    }
                    ```
                    """)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }

    /**
     * 构建API Key安全方案
     * @return SecurityScheme对象
     * 功能：备用认证方式，用于第三方系统集成
     */
    private SecurityScheme buildApiKeySecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("API Key认证，用于第三方系统集成");
    }

    /**
     * 构建通用响应模型
     * @return Schema对象
     * 功能：定义统一的API响应格式
     */
    private io.swagger.v3.oas.models.media.Schema<?> buildResultVOSchema() {
        return new io.swagger.v3.oas.models.media.Schema<>()
                .type("object")
                .addProperties("code", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("integer")
                        .description("状态码")
                        .example(200))
                .addProperties("msg", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("string")
                        .description("消息")
                        .example("success"))
                .addProperties("data", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("object")
                        .description("数据")
                        .nullable(true))
                .addProperties("timestamp", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("string")
                        .format("date-time")
                        .description("时间戳")
                        .example("2024-01-01T12:00:00Z"))
                .description("通用响应格式")
                .required(Arrays.asList("code", "msg", "timestamp"));
    }

    /**
     * 构建分页响应模型
     * @return Schema对象
     * 功能：定义统一的分页响应格式
     */
    private io.swagger.v3.oas.models.media.Schema<?> buildPageResultSchema() {
        return new io.swagger.v3.oas.models.media.Schema<>()
                .type("object")
                .addProperties("total", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("integer")
                        .description("总记录数")
                        .example(100))
                .addProperties("pages", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("integer")
                        .description("总页数")
                        .example(10))
                .addProperties("pageNum", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("integer")
                        .description("当前页码")
                        .example(1))
                .addProperties("pageSize", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("integer")
                        .description("每页大小")
                        .example(10))
                .addProperties("list", new io.swagger.v3.oas.models.media.Schema<>()
                        .type("array")
                        .description("数据列表")
                        .items(new io.swagger.v3.oas.models.media.Schema<>()))
                .description("分页响应格式")
                .required(Arrays.asList("total", "pages", "pageNum", "pageSize", "list"));
    }

    /**
     * 构建用户ID请求头
     * @return Parameter对象
     * 功能：定义用户ID请求头的规范
     */
    private io.swagger.v3.oas.models.parameters.Parameter buildUserIdHeader() {
        return new io.swagger.v3.oas.models.parameters.Parameter()
                .name("X-User-Id")
                .in(io.swagger.v3.oas.models.parameters.Parameter.In.HEADER.toString())
                .description("用户ID（登录后由系统分配）")
                .required(false)
                .schema(new io.swagger.v3.oas.models.media.Schema<>()
                        .type("string")
                        .example("STU20240001"));
    }

    /**
     * 构建用户类型请求头
     * @return Parameter对象
     * 功能：定义用户类型请求头的规范
     */
    private io.swagger.v3.oas.models.parameters.Parameter buildUserTypeHeader() {
        return new io.swagger.v3.oas.models.parameters.Parameter()
                .name("X-User-Type")
                .in(io.swagger.v3.oas.models.parameters.Parameter.In.HEADER.toString())
                .description("用户类型：student/repairer/admin")
                .required(false)
                .schema(new io.swagger.v3.oas.models.media.Schema<>()
                        .type("string")
                        .example("student"));
    }

    /**
     * 构建安全要求
     * @return SecurityRequirement对象
     * 功能：定义需要认证的接口范围
     */
    private SecurityRequirement buildSecurityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Token");
    }

    /**
     * 构建API标签
     * @return 标签列表
     * 功能：组织API接口到不同的标签组
     */
    /*
    private List<io.swagger.v3.oas.models.tags.Tag> buildTags() {
        return Arrays.asList(
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("用户管理")
                        .description("用户认证、注册、个人信息等接口"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("设备管理")
                        .description("设备状态、信息、区域分配等接口"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("数据统计")
                        .description("用水量、告警、设备状态等统计接口"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("告警管理")
                        .description("告警创建、处理、查询等接口"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("工单管理")
                        .description("维修工单创建、分配、处理等接口"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("水质管理")
                        .description("水质数据查询、分析等接口")
        );
    }
    */
}