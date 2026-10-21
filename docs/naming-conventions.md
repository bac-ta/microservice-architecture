# Quy tắc đặt tên (EPL)

Áp dụng cho toàn bộ repo. Hệ thống EPL **không dùng lại bất kỳ tên nào của hệ thống cũ**:
không mã màn / mã chương trình cũ (`gwh_mppik_010`, `lpadj`…), không tên bảng / cột cũ
(`GWH_TM_*`, `AV_NUM`, `*_FLG`…), không comment kiểu `Legacy: …`.

## 1. Mã màn hình

```
EPL-IV-120W
│   │  │  └─ kênh:  W = web (PC), H = handheld (máy cầm tay)
│   │  └──── số:    hàng trăm = nhóm chức năng, hàng chục = màn trong nhóm (bước 10)
│   └─────── miền:  2 chữ, mỗi service một miền
└─────────── tiền tố hệ thống
```

| Miền | Ý nghĩa | Service |
|---|---|---|
| `SY` | Hệ thống / IAM | `auth` |
| `MD` | Master data | `masterdata` |
| `IB` | Nhập hàng | `inbound` |
| `IV` | Tồn kho | `inventory` |
| `OB` | Xuất hàng | `outbound` |
| `BL` | Tính phí | `billing` |
| `IT` | Tích hợp | `integration` |
| `RP` | Báo cáo | `report` |

Quy tắc:

* Regex: `^EPL-[A-Z]{2}-[0-9]{3}[WH]$` (được CHECK trong `iam.screen`).
* Mã định danh **một màn**, không phải một chức năng: cùng chức năng thì bản web và handheld
  dùng chung số, chỉ khác kênh (`EPL-IV-110W` / `EPL-IV-110H`).
* Đánh số bước 10 để chèn màn mới vào giữa (`EPL-IV-115W`). Mã đã bỏ thì **không dùng lại**.
* `0xx` để dành cho màn dùng chung của miền (ví dụ `EPL-SY-010W` đăng nhập).

Thêm màn mới:

1. Thêm dòng vào `docs/screen-codes.md`.
2. Thêm `// screens: …` trên các `rpc` mà màn gọi trong `proto/`.
3. Thêm mã vào `NotPorted.error(...)` của RPC tương ứng (khi RPC chưa implement).
4. Seed `iam.screen` và `iam.screen_action` bằng một migration **mới** của `services/auth`
   (không sửa migration đã chạy).

## 2. Service, module, package

| Thứ | Quy tắc | Ví dụ |
|---|---|---|
| Module Gradle | `services:<service>`, `libs:<lib>`; tên service là danh từ tiếng Anh viết thường | `services:inventory` |
| Spring `application.name` | = tên service | `inventory` |
| Java package gốc | `com.example.epl.<service>` | `com.example.epl.inventory` |
| Class main | `<Service>ServiceApplication` | `InventoryServiceApplication` |
| Cấu hình | package `configurations`, class `<Service><Thứ>Configuration` | `InventoryGrpcServerConfiguration` |
| gRPC server | package `grpc`, class `<TênServiceTrongProto>GrpcServer` | `StockGrpcServer` |
| Cổng | HTTP `81NN`, gRPC `91NN`, `NN` theo thứ tự service | inventory `8104` / `9104` |

## 3. Proto / gRPC

| Thứ | Quy tắc | Ví dụ |
|---|---|---|
| File | `proto/src/main/proto/epl/<service>/v1/<service>.proto` | `epl/inventory/v1/inventory.proto` |
| Package | `epl.<service>.v1` | `epl.inventory.v1` |
| `java_package` | `com.example.epl.<service>.v1` | |
| Service | `<Danh từ>Service` | `StockService`, `StocktakeService` |
| RPC | Động từ + danh từ, PascalCase | `SearchArrivals`, `ConfirmShipment` |
| Message | `<Rpc>Request` / `<Rpc>Response`, entity là danh từ | `SearchArrivalsRequest`, `Arrival` |
| Field | `snake_case`; số chứng từ `*_no`, mã `*_code`, ngày `*_date`, số lượng `*_qty` | `arrival_no`, `product_code` |
| Enum | Giá trị `<ENUM>_<VALUE>`, giá trị 0 = `<ENUM>_UNSPECIFIED` | `USER_TYPE_CUSTOMER` |
| Comment RPC | `// screens: EPL-…` (bỏ qua nếu RPC dùng chung cho mọi màn) | |
| Tên đầy đủ của method | `epl.<service>.v1.<Service>/<Rpc>` (dùng trong `iam.action.grpc_method`) | `epl.inventory.v1.StockService/Adjust` |

Đổi contract không tương thích ngược thì tạo `v2`, không sửa `v1`.

## 4. Database

| Thứ | Quy tắc | Ví dụ |
|---|---|---|
| Database | `epl` (một DB, mỗi service một schema) | |
| Schema | viết tắt ngắn của service | `iam`, `md`, `inv` |
| Role | `<schema>_owner` (migration), `<schema>_app` (runtime) | `inv_owner`, `inv_app` |
| Bảng | `snake_case`, số ít | `stock`, `stock_movement`, `stocktake_line` |
| Khoá chính | `id uuid` (bảng ledger lớn: `bigint identity`) | |
| Khoá ngoại | `<bảng>_id` | `tenant_id`, `warehouse_id` |
| Mã nghiệp vụ | `code` trong bảng của nó, `<thứ>_code` ở bảng khác | `product_code` |
| Cờ | `boolean`, tên dạng tính từ / `is_`, không dùng `*_flg` | `on_hold`, `locked` |
| Audit | `created_at/by`, `updated_at/by`, `version` | |
| Index | `ix_<bảng>_<cột/mục đích>` | `ix_stock_available` |
| Migration Flyway | `V<n>__<mô_tả>.sql` theo từng service; **không sửa file đã chạy**, luôn thêm file mới | `V4__screen_seed.sql` |

## 5. Phân quyền (ABAC)

| Thứ | Quy tắc | Ví dụ |
|---|---|---|
| Resource type | `<service>.<resource>` | `inventory.stock`, `billing.charge` |
| Action | `snake_case`, động từ | `read`, `adjust`, `change_pick_key`, `close_month` |
| Role | `UPPER_SNAKE`, nhóm theo nghiệp vụ | `WMS_OPERATOR`, `CUSTOMER_VIEWER` |
| Policy code | `kebab-case` mô tả quy tắc | `adjust-within-limit` |

## 6. Sự kiện (Kafka / outbox)

`<miền nghiệp vụ>.<entity>.<động từ quá khứ>`, viết thường, ví dụ `stock.adjusted`,
`stocktake.confirmed`, `inbound.arrival.confirmed`, `user.grants.changed`.

## 7. Cấu hình và dữ liệu mẫu

* Biến môi trường: `UPPER_SNAKE`, có tiền tố theo nhóm: `DB_*`, `FLYWAY_*`, `GRPC_SERVER_*`,
  `GRPC_<BACKEND>_TARGET`, `HTTP_SERVER_PORT`.
* Header tenant: `x-company`, `x-warehouse`, `x-customer`.
* Mã loại file upload: `UPPER_SNAKE` theo nghiệp vụ (`ARRIVAL`, `SHIPMENT`, `PRODUCT`), không
  dùng mã số.
* Dữ liệu mẫu trong test / tài liệu: company `C001`, warehouse `WH01`, customer `CUST01`,
  product `P001`.
