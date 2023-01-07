package top.simba1949;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author anthony
 * @date 2023/1/6
 */
@SpringBootApplication
public class JdbcApplication {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JdbcApplication.class, args);
    }

    @Bean
    public JdbcTemplate createJdbcTemplate(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(dataSource);
        return jdbcTemplate;
    }
}
