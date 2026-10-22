package com.example.epl.integration.services;

import com.example.epl.integration.entities.JobRunEntity;
import com.example.epl.integration.mappers.JobMappers;
import com.example.epl.integration.repositories.JobDefinitionRepository;
import com.example.epl.integration.repositories.JobRunRepository;
import com.example.epl.integration.repositories.PurgeHistoryRepository;
import com.example.epl.integration.v1.CreateJobRequest;
import com.example.epl.integration.v1.Job;
import com.example.epl.integration.v1.JobDefinition;
import com.example.epl.integration.v1.ListJobsRequest;
import com.example.epl.integration.v1.ListJobsResponse;
import com.example.epl.integration.v1.ListPurgeHistoryRequest;
import com.example.epl.integration.v1.ListPurgeHistoryResponse;
import com.example.service.core.Errors;
import com.example.service.core.Pages;
import com.example.service.core.RequestContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static com.example.service.core.Protos.localDate;
import static com.example.service.core.Protos.map;
import static com.example.service.core.Protos.uuid;

/** Job runs of the tenant: queue, inspect, and remove a run that has not started. */
@Service
public class JobService {

    private final JobDefinitionRepository definitionRepository;
    private final JobRunRepository runRepository;
    private final PurgeHistoryRepository purgeHistoryRepository;

    public JobService(JobDefinitionRepository definitionRepository, JobRunRepository runRepository,
                      PurgeHistoryRepository purgeHistoryRepository) {
        this.definitionRepository = definitionRepository;
        this.runRepository = runRepository;
        this.purgeHistoryRepository = purgeHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<JobDefinition> definitions() {
        return definitionRepository.findAllByOrderByCode().stream().map(JobMappers::definition).toList();
    }

    @Transactional(readOnly = true)
    public ListJobsResponse list(ListJobsRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var statuses = request.getStatusesList();
        var page = runRepository.search(tenantId, request.getJobCode(), statuses.isEmpty(), statuses.isEmpty() ? Set.of("") : statuses,
                Pages.pageable(request.getPage(), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ListJobsResponse.newBuilder()
                .addAllJobs(page.map(JobMappers::job).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    @Transactional(readOnly = true)
    public Job get(String id) {
        return JobMappers.job(find(id));
    }

    @Transactional
    public Job create(CreateJobRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var code = Errors.required(request.getJobCode(), "job_code");
        var definition = definitionRepository.findById(code).orElseThrow(() -> Errors.notFound("Job", code));
        if (!definition.isEnabled()) {
            throw Errors.failedPrecondition("Job " + code + " is disabled");
        }
        var entity = new JobRunEntity();
        entity.setTenantId(tenantId);
        entity.setJobCode(code);
        entity.setStatus("QUEUED");
        entity.setParameters(map(request.getParameters()));
        return JobMappers.job(runRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String id) {
        var entity = find(id);
        if (!"QUEUED".equals(entity.getStatus())) {
            throw Errors.failedPrecondition("Job run " + id + " is " + entity.getStatus() + "; only a QUEUED run can be deleted");
        }
        runRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public ListPurgeHistoryResponse purgeHistory(ListPurgeHistoryRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var from = localDate(request.getDateFrom());
        var to = localDate(request.getDateTo());
        var page = purgeHistoryRepository.findAllByTenantIdAndStartedAtBetween(tenantId,
                from == null ? Instant.EPOCH : from.atStartOfDay(ZoneOffset.UTC).toInstant(),
                to == null ? Instant.now() : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                Pages.pageable(request.getPage(), Sort.by(Sort.Direction.DESC, "startedAt")));
        return ListPurgeHistoryResponse.newBuilder()
                .addAllRecords(page.map(JobMappers::purgeRecord).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    private JobRunEntity find(String id) {
        var tenantId = RequestContext.current().requireTenant();
        return runRepository.findByTenantIdAndId(tenantId, uuid(id, "id")).orElseThrow(() -> Errors.notFound("Job run", id));
    }
}
