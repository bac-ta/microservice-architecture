package com.epl.report.services;

import com.epl.report.entities.PrinterEntity;
import com.epl.report.mappers.ReportMappers;
import com.epl.report.repositories.PrinterRepository;
import com.epl.report.v1.Printer;
import com.epl.service.core.Errors;
import com.epl.service.core.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Printers of the warehouse in x-warehouse. */
@RequiredArgsConstructor
@Service
public class PrinterService {

    private static final Set<String> KINDS = Set.of("LABEL", "DOCUMENT");

    private final PrinterRepository printerRepository;

    @Transactional(readOnly = true)
    public List<Printer> list(String kind) {
        return printerRepository.search(RequestContext.current().requireWarehouse(), kind)
                .stream().map(ReportMappers::printer).toList();
    }

    @Transactional(readOnly = true)
    public Printer get(String code) {
        return ReportMappers.printer(find(code));
    }

    @Transactional
    public Printer upsert(Printer request) {
        var warehouseId = RequestContext.current().requireWarehouse();
        var code = Errors.required(request.getCode(), "code");
        var kind = Errors.required(request.getKind(), "kind");
        if (!KINDS.contains(kind)) {
            throw Errors.invalid("kind must be LABEL or DOCUMENT");
        }
        var entity = printerRepository.findById(new PrinterEntity.Id(warehouseId, code)).orElse(null);
        Errors.checkUpsert("Printer", code, entity != null, request.hasAudit(),
                entity == null ? 0 : entity.getVersion(), request.getAudit().getVersion());
        if (entity == null) {
            entity = new PrinterEntity();
            entity.setWarehouseId(warehouseId);
            entity.setCode(code);
        }
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setKind(kind);
        entity.setEndpoint(Errors.required(request.getEndpoint(), "endpoint"));
        entity.setEnabled(request.getEnabled());
        return ReportMappers.printer(printerRepository.saveAndFlush(entity));
    }

    /** Refused (FAILED_PRECONDITION) while devices are paired with it or print jobs reference it. */
    @Transactional
    public void delete(String code) {
        printerRepository.delete(find(code));
        printerRepository.flush();
    }

    private PrinterEntity find(String code) {
        var warehouseId = RequestContext.current().requireWarehouse();
        return printerRepository.findById(new PrinterEntity.Id(warehouseId, Errors.required(code, "code")))
                .orElseThrow(() -> Errors.notFound("Printer", code));
    }
}
