package com.epl.report;

import com.epl.report.v1.DeletePrinterRequest;
import com.epl.report.v1.GetPrinterRequest;
import com.epl.report.v1.ListPrintersRequest;
import com.epl.report.v1.ListReportsRequest;
import com.epl.report.v1.Printer;
import com.epl.report.v1.PrinterServiceGrpc;
import com.epl.report.v1.ReportServiceGrpc;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(EplServiceTestConfiguration.class)
class ReportCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "rpt");
    }

    @Autowired
    private GrpcTestServer grpc;

    /**
     * Definitions are maintained by epl_admin, not by the service: seed a default and a CUST01 override.
     * The parameter makes Spring start the context, which runs the rpt migrations, first.
     */
    @BeforeAll
    static void seedDefinitions(@Autowired GrpcTestServer ignored) {
        EplTestDatabase.execute("""
                INSERT INTO rpt.report_definition (code, name, process_code, formats, parameters, template_ref, updated_by)
                VALUES ('PICKING_LIST', 'Picking list', 'OUTBOUND', '{PDF}', '["batch_no"]', 's3://t/default', 'test')
                ON CONFLICT DO NOTHING;
                INSERT INTO rpt.report_definition (company_id, warehouse_id, customer_id, code, name, process_code, formats,
                                                   parameters, template_ref, updated_by)
                SELECT t.company_id, t.warehouse_id, t.customer_id, 'PICKING_LIST', 'Picking list (CUST01)', 'OUTBOUND',
                       '{PDF,XLSX}', '[{"name":"batch_no"}]', 's3://t/cust01', 'test'
                FROM iam.tenant t WHERE t.customer_code = 'CUST01'
                ON CONFLICT DO NOTHING;
                """);
    }

    @Test
    void mostSpecificReportDefinitionWins() {
        var cust01 = ReportServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var reports = cust01.listReports(ListReportsRequest.newBuilder().setProcessCode("OUTBOUND").build()).getReportsList();
        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getName()).isEqualTo("Picking list (CUST01)");
        assertThat(reports.get(0).getFormatsList()).containsExactly("PDF", "XLSX");
        assertThat(reports.get(0).getParametersList()).containsExactly("batch_no");

        var cust02 = ReportServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", "CUST02"));
        assertThat(cust02.listReports(ListReportsRequest.getDefaultInstance()).getReports(0).getName()).isEqualTo("Picking list");
    }

    @Test
    void printers() {
        var printers = PrinterServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", null));
        var created = printers.upsertPrinter(Printer.newBuilder().setCode("LBL01").setName("Dock label printer")
                .setKind("LABEL").setEndpoint("10.0.0.5:9100").setEnabled(true).build());
        assertThat(created.getAudit().getUpdatedBy()).isEqualTo("tester");
        var updated = printers.upsertPrinter(created.toBuilder().setEnabled(false).build());
        assertThat(updated.getEnabled()).isFalse();
        assertThat(printers.listPrinters(ListPrintersRequest.newBuilder().setKind("LABEL").build()).getPrintersList()).hasSize(1);

        printers.deletePrinter(DeletePrinterRequest.newBuilder().setCode("LBL01").build());
        assertThatThrownBy(() -> printers.getPrinter(GetPrinterRequest.newBuilder().setCode("LBL01").build()))
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
    }
}
