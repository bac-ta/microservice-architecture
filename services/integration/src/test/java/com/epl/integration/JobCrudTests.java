package com.epl.integration;

import com.epl.integration.v1.CreateJobRequest;
import com.epl.integration.v1.DeleteJobRequest;
import com.epl.integration.v1.GetJobRequest;
import com.epl.integration.v1.JobServiceGrpc;
import com.epl.integration.v1.ListJobsRequest;
import com.epl.integration.v1.ListPurgeHistoryRequest;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.google.protobuf.Empty;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
class JobCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "intg");
    }

    @Autowired
    private GrpcTestServer grpc;

    @Test
    void queueInspectAndDeleteJobRuns() {
        var jobs = JobServiceGrpc.newBlockingStub(grpc.tenantChannel());
        assertThat(jobs.listJobDefinitions(Empty.getDefaultInstance()).getDefinitionsList())
                .extracting("code").contains("PURGE", "EDI_SEND");

        var queued = jobs.createJob(CreateJobRequest.newBuilder().setJobCode("EDI_SEND")
                .setParameters(Struct.newBuilder().putFields("partner", Value.newBuilder().setStringValue("ACME").build())).build());
        assertThat(queued.getStatus()).isEqualTo("QUEUED");
        assertThat(queued.getCreatedBy()).isEqualTo("tester");
        assertThat(jobs.getJob(GetJobRequest.newBuilder().setId(queued.getId()).build())
                .getParameters().getFieldsOrThrow("partner").getStringValue()).isEqualTo("ACME");
        assertThat(jobs.listJobs(ListJobsRequest.newBuilder().setJobCode("EDI_SEND").addStatuses("QUEUED").build()).getJobsList())
                .extracting("id").containsExactly(queued.getId());

        jobs.deleteJob(DeleteJobRequest.newBuilder().setId(queued.getId()).build());
        assertThatThrownBy(() -> jobs.getJob(GetJobRequest.newBuilder().setId(queued.getId()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));

        assertThatThrownBy(() -> jobs.createJob(CreateJobRequest.newBuilder().setJobCode("NOPE").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
        assertThat(jobs.listPurgeHistory(ListPurgeHistoryRequest.getDefaultInstance()).getRecordsList()).isEmpty();
    }

    @Test
    void jobRunsNeedATenant() {
        var noTenant = JobServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", null));
        assertThatThrownBy(() -> noTenant.listJobs(ListJobsRequest.getDefaultInstance()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}
