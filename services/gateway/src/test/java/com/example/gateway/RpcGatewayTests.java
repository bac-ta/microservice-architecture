package com.example.gateway;

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
        mvc.perform(post("/rpc/epl.masterdata.v1.ProductService/GetProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Company", "530014")
                        .header("X-Warehouse", "BOND")
                        .header("X-Customer", "BOND01")
                        .header("X-Request-Id", "req-42")
                        .content("{\"code\":\"P001\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "req-42"))
                .andExpect(jsonPath("$.code").value("P001"))
                .andExpect(jsonPath("$.names[0]").value("Green tea"))
                // default values are printed too, so clients always see every field
                .andExpect(jsonPath("$.storageKind").value(""));

        var headers = backends.lastHeaders.get();
        assertThat(headers.get(key("x-company"))).isEqualTo("530014");
        assertThat(headers.get(key("x-warehouse"))).isEqualTo("BOND");
        assertThat(headers.get(key("x-customer"))).isEqualTo("BOND01");
        assertThat(headers.get(key("x-request-id"))).isEqualTo("req-42");
    }

    @Test
    void grpcErrorBecomesHttpStatus() throws Exception {
        mvc.perform(post("/rpc/epl.masterdata.v1.ProductService/GetProduct")
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
        mvc.perform(post("/rpc/epl.masterdata.v1.ProductService/ListProducts"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code").value("UNIMPLEMENTED"));
    }

    @Test
    void unknownRpcIs404() throws Exception {
        mvc.perform(post("/rpc/epl.masterdata.v1.ProductService/Nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidBodyIs400() throws Exception {
        mvc.perform(post("/rpc/epl.masterdata.v1.ProductService/GetProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"no_such_field\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void serverStreamingIsNdjson() throws Exception {
        var body = mvc.perform(post("/rpc/epl.inventory.v1.StockService/Export")
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
        var lines = mvc.perform(post("/rpc/epl.inventory.v1.StockService/Export")
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

        mvc.perform(multipart("/files/upload").file(file).param("fileKind", "01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("01"))
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
                .andExpect(jsonPath("$[*].path", hasItem("/rpc/epl.inventory.v1.StockService/Search")))
                .andExpect(jsonPath("$[?(@.path == '/rpc/epl.inventory.v1.StockService/Search')].backend").value("inventory"));
    }

    private static Metadata.Key<String> key(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}
