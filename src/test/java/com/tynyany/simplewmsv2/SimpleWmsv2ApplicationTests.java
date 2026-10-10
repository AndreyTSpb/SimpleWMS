package com.tynyany.simplewmsv2;

import com.tynyany.simplewmsv2.it.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Приложение запускается на чистой базе: схема создана, начальные данные (V001, V002) загружены.
 */
class SimpleWmsv2ApplicationTests extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void contextLoadsAndSeedDataApplied() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM roles", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT login FROM users WHERE userid = 1", String.class)).isEqualTo("admin");
        assertThat(jdbc.queryForObject("SELECT location_code FROM location WHERE locationid = 1", String.class))
                .isEqualTo("PRM-01-01-1-1");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM location WHERE zoneid IN (3, 4)", Integer.class)).isEqualTo(24);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM product", Integer.class)).isGreaterThanOrEqualTo(5);
    }
}
