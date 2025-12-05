package com.campus.water.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * MyBatis配置类
 * 功能：配置MyBatis与Spring Boot的集成，包括数据源、事务管理、Mapper扫描等
 * 用途：
 *   1. 数据源配置：连接池、连接参数
 *   2. MyBatis核心配置：SqlSessionFactory、事务管理器
 *   3. Mapper扫描：自动注册MyBatis映射接口
 *   4. 插件配置：分页插件、性能监控插件等
 * 核心注解：
 *   - @Configuration: 声明为配置类
 *   - @MapperScan: 指定Mapper接口的扫描路径
 *   - @EnableTransactionManagement: 启用声明式事务管理
 */
@Configuration
@MapperScan(basePackages = "com.campus.water.mapper") // 扫描Mapper接口
@EnableTransactionManagement // 启用事务管理
public class MyBatisConfig {

    // ========== 数据源配置 ==========

    /**
     * 创建HikariCP数据源
     * @return 数据源实例
     * 功能：配置数据库连接池
     * 优点：
     *   - HikariCP是目前性能最好的连接池
     *   - 自动从application.yml读取配置
     *   - 支持连接泄漏检测、空闲连接回收
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari") // 绑定配置前缀
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        // 设置连接池监控配置（可在application.yml中覆盖）
        dataSource.setPoolName("CampusWaterHikariPool");
        dataSource.setMaximumPoolSize(20); // 最大连接数
        dataSource.setMinimumIdle(5);      // 最小空闲连接
        dataSource.setIdleTimeout(600000); // 空闲连接超时时间（10分钟）
        dataSource.setConnectionTimeout(30000); // 连接超时时间（30秒）
        dataSource.setMaxLifetime(1800000); // 连接最大生命周期（30分钟）

        // 连接测试配置
        dataSource.setConnectionTestQuery("SELECT 1"); // MySQL测试语句
        dataSource.setValidationTimeout(5000); // 验证超时时间

        // 连接泄漏检测
        dataSource.setLeakDetectionThreshold(60000); // 60秒泄漏检测阈值

        return dataSource;
    }

    // ========== MyBatis核心配置 ==========

    /**
     * 创建SqlSessionFactory
     * @param dataSource 数据源
     * @return SqlSessionFactory实例
     * 功能：MyBatis的核心工厂类，用于创建SqlSession
     * 配置项：
     *   - 数据源绑定
     *   - Mapper XML文件位置
     *   - 类型别名包扫描
     *   - 全局配置文件
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();

        // 1. 设置数据源
        sessionFactory.setDataSource(dataSource);

        // 2. 设置Mapper XML文件位置（支持Ant风格路径）
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(
                resolver.getResources("classpath:mapper/**/*.xml")
        );

        // 3. 设置类型别名包扫描路径（实体类包）
        sessionFactory.setTypeAliasesPackage("com.campus.water.entity");

        // 4. 创建Configuration对象，设置全局配置
        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true); // 下划线转驼峰
        configuration.setUseGeneratedKeys(true); // 使用JDBC的getGeneratedKeys获取主键
        configuration.setUseColumnLabel(true); // 使用列标签代替列名
        configuration.setCacheEnabled(true); // 启用二级缓存

        // 5. 设置日志实现
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);

        // 6. 设置默认的执行器类型
        configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.SIMPLE);

        // 7. 设置配置
        sessionFactory.setConfiguration(configuration);

        // 8. 设置插件（分页插件、性能监控插件等）
        sessionFactory.setPlugins(
                // 分页插件
                pageInterceptor(),
                // 性能分析插件（开发环境使用）
                // performanceInterceptor()
        );

        return sessionFactory.getObject();
    }

    /**
     * 分页插件配置
     * @return 分页拦截器
     * 功能：自动处理分页查询，支持MySQL、Oracle等多种数据库
     * 特性：
     *   - 自动识别数据库类型
     *   - 支持多种分页方式
     *   - 线程安全
     */
    @Bean
    public com.github.pagehelper.PageInterceptor pageInterceptor() {
        com.github.pagehelper.PageInterceptor pageInterceptor =
                new com.github.pagehelper.PageInterceptor();

        Properties properties = new Properties();

        // 1. 分页合理化：pageNum<=0时查询第一页，pageNum>总页数时查询最后一页
        properties.setProperty("reasonable", "true");

        // 2. 支持通过Mapper接口参数传递分页参数
        properties.setProperty("supportMethodsArguments", "true");

        // 3. 自动检测数据库类型
        properties.setProperty("autoRuntimeDialect", "true");

        // 4. 分页参数默认值
        properties.setProperty("pageSizeZero", "true"); // pageSize=0时返回全部结果
        properties.setProperty("params", "count=countSql");

        // 5. 开启分页返回对象中的统计信息
        properties.setProperty("rowBoundsWithCount", "true");

        // 6. 总是返回PageInfo对象
        properties.setProperty("returnPageInfo", "always");

        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    /**
     * 性能分析插件（开发环境使用）
     * @return 性能分析拦截器
     * 功能：记录SQL执行时间，帮助优化慢查询
     * 注意：生产环境建议关闭或设置较高的阈值
     */
    /*
    @Bean
    @Profile({"dev", "test"}) // 只在开发测试环境启用
    public com.github.pagehelper.sql.SqlStatsInterceptor performanceInterceptor() {
        com.github.pagehelper.sql.SqlStatsInterceptor interceptor =
            new com.github.pagehelper.sql.SqlStatsInterceptor();

        Properties properties = new Properties();
        properties.setProperty("maxTime", "1000"); // 最大执行时间阈值（毫秒）
        properties.setProperty("format", "true");  // 格式化SQL输出

        interceptor.setProperties(properties);
        return interceptor;
    }
    */

    // ========== 事务管理配置 ==========

    /**
     * 创建事务管理器
     * @param dataSource 数据源
     * @return 平台事务管理器
     * 功能：管理数据库事务，支持声明式事务
     * 注解支持：@Transactional
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    // ========== 其他配置 ==========

    /**
     * MyBatis属性配置
     * @return MyBatis配置属性
     * 功能：集中管理MyBatis相关配置
     */
    @Bean
    @ConfigurationProperties(prefix = "mybatis.configuration")
    public org.apache.ibatis.session.Configuration mybatisConfiguration() {
        return new org.apache.ibatis.session.Configuration();
    }
}