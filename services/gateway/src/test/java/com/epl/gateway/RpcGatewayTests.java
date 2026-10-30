package com.epl.gateway;

import com.epl.gateway.rpc.RpcRegistry;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "epl.gateway.security.enabled=false")
@AutoConfigureMockMvc
@Import(FakeBackends.class)
class RpcGatewayTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeBackends backends;

    @Test
    void unaryCallIsTranscodedAndForwardsTenantHeaders() throws Exception {
        mvc.perform(post("/rpc/masterdata/v1/product/get-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Company", "C001")
                        .header("X-Warehouse", "WH01")
                        .header("X-Customer", "CUST01")
                        .header("X-Request-Id", "req-42")
                        .content("{\"code\":\"P001\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req-42"))
                .andExpect(jsonPath("$.code").value("P001"))
                .andExpect(jsonPath("$.names[0]").value("Green tea"))
                // default values are printed too, so clients always see every field
                .andExpect(jsonPath("$.storageKind").value(""));

        var headers = backends.lastHeaders.get();
        assertThat(headers.get(key("x-company"))).isEqualTo("C001");
        assertThat(headers.get(key("x-warehouse"))).isEqualTo("WH01");
        assertThat(headers.get(key("x-customer"))).isEqualTo("CUST01");
        assertThat(headers.get(key("x-request-id"))).isEqualTo("req-42");
    }

    @Test
    void grpcErrorBecomesHttpStatus() throws Exception {
        mvc.perform(post("/rpc/masterdata/v1/product/get-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"missing\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product missing not found"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void unimplementedRpcIs501() throws Exception {
        // the fake ProductService does not override ListProducts
        mvc.perform(post("/rpc/masterdata/v1/product/list-products"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code").value("UNIMPLEMENTED"));
    }

    @Test
    void unknownRpcIs404() throws Exception {
        mvc.perform(post("/rpc/masterdata/v1/product/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBodyIs400() throws Exception {
        mvc.perform(post("/rpc/masterdata/v1/product/get-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"no_such_field\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void serverStreamingIsNdjson() throws Exception {
        var body = mvc.perform(post("/rpc/inventory/v1/stock/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\"P001\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON))
                .andReturn().getResponse().getContentAsString();

        assertThat(body.lines()).hasSize(2);
        assertThat(body.lines().toList().get(1)).contains("\"id\":\"lot-2\"");
    }

    @Test
    void errorInTheMiddleOfAStreamIsTheLastLine() throws Exception {
        var lines = mvc.perform(post("/rpc/inventory/v1/stock/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productCode\":\"broken\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString().lines().toList();

        assertThat(lines).hasSize(2);
        assertThat(lines.get(1)).contains("\"code\":\"INTERNAL\"", "disk on fire");
    }

    @Test
    void uploadStreamsTheFile() throws Exception {
        var file = new MockMultipartFile("file", "arrivals.csv", "text/csv", new byte[200_000]);

        mvc.perform(multipart("/files/upload").file(file).param("fileKind", "ARRIVAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("ARRIVAL"))
                .andExpect(jsonPath("$.acceptedRows").value(200_000));
    }

    @Test
    void reportIsDownloadedAsAFile() throws Exception {
        var result = mvc.perform(post("/reports/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportCode\":\"STOCK\",\"format\":\"CSV\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("stock.csv")))
                .andExpect(content().string("code,qty\nP001,12\n"));
    }

    @Test
    void rpcListShowsEveryContract() throws Exception {
        mvc.perform(get("/rpc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].path", hasItem("/rpc/inventory/v1/stock/search")))
                .andExpect(jsonPath("$[?(@.path == '/rpc/inventory/v1/stock/search')].backend").value("inventory"))
                .andExpect(jsonPath("$[?(@.path == '/rpc/inventory/v1/stock/search')].rpc").value("epl.inventory.v1.StockService/Search"));
    }

    @Test
    void rpcPathIsLowercaseWithHyphens() {
        assertThat(RpcRegistry.pathOf("epl.masterdata.v1.ProductService/GetProduct")).isEqualTo("/masterdata/v1/product/get-product");
        assertThat(RpcRegistry.pathOf("epl.auth.v1.AuthService/GetJwks")).isEqualTo("/auth/v1/auth/get-jwks");
        assertThat(RpcRegistry.pathOf("epl.inventory.v1.StocktakeService/ConfirmCount")).isEqualTo("/inventory/v1/stocktake/confirm-count");
        assertThat(RpcRegistry.pathOf("user.UserService/ListAPIKeys")).isEqualTo("/user/user/list-api-keys");
    }

    private static Metadata.Key<String> key(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}
