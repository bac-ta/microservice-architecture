package com.epl.masterdata.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/** Primary key (warehouse_id, code) of warehouse-scoped tables. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseCodeId implements Serializable {

    private UUID warehouseId;
    private String code;
}
