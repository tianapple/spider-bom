package org.spider.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.spider.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 * MyBatis Java配置基础类
 *
 * Created by tianapple on 2017/9/27.
 */
public class MyBatisConfig {
    private static final String Config_Location = "mybatis/config.xml";

    @Autowired
    private Environment env;

    //连接只读数据库时配置为true， 保证安全1
    private static final boolean readOnly = false;
    //等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException， 缺省:30秒
    private static final int connTimeout = 15000;
    //一个连接idle状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
    private static final int idleTimeout = 600000;
    //一个连接的生命时长（毫秒），超时而且没被使用则被释放（retired），缺省:30分钟，建议设置比数据库超时时长少30秒，参考MySQL wait_timeout参数（show variables like '%timeout%';）
    private static final int maxLifetime = 1800000;
    //连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)
    private static final int maxPoolSize = 20;
    private static final int minimumIdle = 4;

    protected String getPrefix() {
        return "";
    }

    protected String getConfigLocation() {
        return Config_Location;
    }

    protected DataSource getDataSource() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setConnectionInitSql("select 1");
        String url = env.getProperty(getPrefix() + "url");
        config.setJdbcUrl(url);
        String userName = env.getProperty(getPrefix() + "username");
        config.setUsername(userName);
        String password = env.getProperty(getPrefix() + "password");
        config.setPassword(password);

        String var = env.getProperty(getPrefix() + "readOnly");
        if (StringUtils.isNullOrEmpty(var)) {
            config.setReadOnly(readOnly);
        } else {
            config.setReadOnly(Boolean.valueOf(var));
        }

        var = env.getProperty(getPrefix() + "connTimeout");
        if (StringUtils.isNullOrEmpty(var)) {
            config.setConnectionTimeout(connTimeout);
        } else {
            config.setConnectionTimeout(Integer.valueOf(var));
        }

        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minimumIdle);
        return new HikariDataSource(config);
    }

    protected SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(dataSource);
        fb.setConfigLocation(new ClassPathResource(getConfigLocation()));
        return fb.getObject();
    }

//    protected MapperScannerConfigurer getConfigurer() {
//        return null;
//    }

}
