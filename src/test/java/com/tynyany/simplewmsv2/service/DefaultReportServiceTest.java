package com.tynyany.simplewmsv2.service;

import com.tynyany.simplewmsv2.config.WmsProperties;
import com.tynyany.simplewmsv2.service.ReportService.BatchStockRow;
import com.tynyany.simplewmsv2.service.ReportService.ExpiryState;
import com.tynyany.simplewmsv2.service.ReportService.ProductStockRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultReportServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 10);

    @Test
    void expiryStateClassifiesDates() {
        assertThat(DefaultReportService.expiryState(null, TODAY, 30)).isEqualTo(ExpiryState.NONE);
        assertThat(DefaultReportService.expiryState(TODAY.minusDays(1), TODAY, 30)).isEqualTo(ExpiryState.EXPIRED);
        assertThat(DefaultReportService.expiryState(TODAY, TODAY, 30)).isEqualTo(ExpiryState.SOON);
        assertThat(DefaultReportService.expiryState(TODAY.plusDays(29), TODAY, 30)).isEqualTo(ExpiryState.SOON);
        assertThat(DefaultReportService.expiryState(TODAY.plusDays(30), TODAY, 30)).isEqualTo(ExpiryState.OK);
    }

    @Test
    void aggregateByProductSplitsStorageReceivingAndShipping() {
        WmsProperties.Locations system = new WmsProperties.Locations();
        system.setReceivingId(1);
        system.setShippingId(2);
        List<BatchStockRow> rows = List.of(
                row("074519", 1, 101, 30, 10, ExpiryState.OK),
                row("074519", 2, 102, 40, 0, ExpiryState.SOON),
                row("074519", 3, 1, 15, 0, ExpiryState.NONE),
                row("074519", 2, 2, 5, 0, ExpiryState.SOON),
                row("029952", 4, 103, 20, 20, ExpiryState.NONE));

        List<ProductStockRow> result = DefaultReportService.aggregateByProduct(rows, system);

        assertThat(result).hasSize(2);
        ProductStockRow glue = result.get(0);
        assertThat(glue.productCode()).isEqualTo("074519");
        assertThat(glue.total()).isEqualTo(90);
        assertThat(glue.storage()).isEqualTo(70);
        assertThat(glue.reserved()).isEqualTo(10);
        assertThat(glue.free()).isEqualTo(60);
        assertThat(glue.receiving()).isEqualTo(15);
        assertThat(glue.shipping()).isEqualTo(5);
        assertThat(glue.worstExpiry()).isEqualTo(ExpiryState.SOON);
        assertThat(result.get(1).free()).isZero();
    }

    @Test
    void escapeLikeEscapesWildcards() {
        assertThat(DefaultReportService.escapeLike("50%_a\\b")).isEqualTo("50\\%\\_a\\\\b");
    }

    private static BatchStockRow row(String code, int batch, int location, int qty, int reserved, ExpiryState state) {
        return new BatchStockRow(code, "товар " + code, batch, "", state, location, "L" + location, "Z", qty, reserved);
    }
}
