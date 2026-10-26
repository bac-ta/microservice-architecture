package com.epl.integration.mappers;

import com.epl.integration.entities.JobDefinitionEntity;
import com.epl.integration.entities.JobRunEntity;
import com.epl.integration.entities.PurgeHistoryEntity;
import com.epl.integration.v1.Job;
import com.epl.integration.v1.JobDefinition;
import com.epl.integration.v1.PurgeRecord;

import static com.epl.service.core.Protos.emptyIfNull;
import static com.epl.service.core.Protos.struct;
import static com.epl.service.core.Protos.text;
import static com.epl.service.core.Protos.timestamp;

public final class JobMappers {

    private JobMappers() {
    }

    public static JobDefinition definition(JobDefinitionEntity entity) {
        return JobDefinition.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setSchedule(emptyIfNull(entity.getSchedule()))
                .setEnabled(entity.isEnabled())
                .setDescription(emptyIfNull(entity.getDescription()))
                .build();
    }

    public static Job job(JobRunEntity entity) {
        return Job.newBuilder()
                .setId(text(entity.getId()))
                .setJobCode(entity.getJobCode())
                .setStatus(entity.getStatus())
                .setStartedAt(timestamp(entity.getStartedAt()))
                .setFinishedAt(timestamp(entity.getFinishedAt()))
                .setMessage(emptyIfNull(entity.getMessage()))
                .setParameters(struct(entity.getParameters()))
                .setCreatedAt(timestamp(entity.getCreatedAt()))
                .setCreatedBy(emptyIfNull(entity.getCreatedBy()))
                .build();
    }

    public static PurgeRecord purgeRecord(PurgeHistoryEntity entity) {
        return PurgeRecord.newBuilder()
                .setTableName(entity.getTableName())
                .setDeletedRows(entity.getDeletedRows())
                .setResult(entity.getResult())
                .setStartedAt(timestamp(entity.getStartedAt()))
                .setFinishedAt(timestamp(entity.getFinishedAt()))
                .setErrorMessage(emptyIfNull(entity.getErrorMessage()))
                .build();
    }
}
