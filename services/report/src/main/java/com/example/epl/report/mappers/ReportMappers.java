package com.example.epl.report.mappers;

import com.example.epl.common.v1.Audit;
import com.example.epl.report.entities.PrinterEntity;
import com.example.epl.report.entities.ReportDefinitionEntity;
import com.example.epl.report.v1.Printer;
import com.example.epl.report.v1.ReportDefinition;

import java.util.Map;

import static com.example.service.core.Protos.timestamp;

public final class ReportMappers {

    private ReportMappers() {
    }

    /** The printer table keeps only updated_at / updated_by. */
    public static Printer printer(PrinterEntity entity) {
        return Printer.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setKind(entity.getKind())
                .setEndpoint(entity.getEndpoint())
                .setEnabled(entity.isEnabled())
                .setAudit(Audit.newBuilder()
                        .setUpdatedAt(timestamp(entity.getUpdatedAt()))
                        .setUpdatedBy(entity.getUpdatedBy() == null ? "" : entity.getUpdatedBy())
                        .setVersion(entity.getVersion()))
                .build();
    }

    public static ReportDefinition reportDefinition(ReportDefinitionEntity entity) {
        var builder = ReportDefinition.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setProcessCode(entity.getProcessCode())
                .addAllFormats(entity.getFormats());
        for (var parameter : entity.getParameters()) {
            builder.addParameters(parameter instanceof Map<?, ?> object ? String.valueOf(object.get("name")) : String.valueOf(parameter));
        }
        return builder.build();
    }
}
