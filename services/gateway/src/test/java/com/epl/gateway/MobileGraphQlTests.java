package com.epl.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.introspection.IntrospectionQuery;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "epl.gateway.security.enabled=false")
@AutoConfigureMockMvc
@Import(FakeBackends.class)
class MobileGraphQlTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FakeBackends backends;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void queryCallsTheRpcAndForwardsTenantHeaders() throws Exception {
        var headers = new HttpHeaders();
        headers.add("X-Company", "C001");
        headers.add("X-Warehouse", "WH01");
        headers.add("X-Customer", "CUST01");
        headers.add("X-Request-Id", "req-42");

        graphql("""
                query { masterdata { product { getProduct(input: {code: "P001"}) { code names storageKind } } } }""", headers)
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.masterdata.product.getProduct.code").value("P001"))
                .andExpect(jsonPath("$.data.masterdata.product.getProduct.names[0]").value("Green tea"))
                .andExpect(jsonPath("$.data.masterdata.product.getProduct.storageKind").value(""));

        var forwarded = backends.lastHeaders.get();
        assertThat(forwarded.get(key("x-company"))).isEqualTo("C001");
        assertThat(forwarded.get(key("x-warehouse"))).isEqualTo("WH01");
        assertThat(forwarded.get(key("x-customer"))).isEqualTo("CUST01");
        assertThat(forwarded.get(key("x-request-id"))).isEqualTo("req-42");
    }

    @Test
    void selectedFieldsAreSentAsFieldMask() throws Exception {
        graphql("""
                query { masterdata { product { getProduct(input: {code: "P001"}) {
                  code again: code names audit { version __typename } ext __typename
                  ...more
                } } } }
                fragment more on Product { hsCode }""", new HttpHeaders())
                .andExpect(jsonPath("$.errors").doesNotExist());

        // aliases and fragments merged, sub-fields by path, JSON scalars (Struct) whole
        assertThat(backends.lastHeaders.get().get(key("x-field-mask"))).isEqualTo("code,names,audit.version,ext,hsCode");
    }

    @Test
    void grpcErrorBecomesGraphQlError() throws Exception {
        graphql("""
                query { masterdata { product { getProduct(input: {code: "missing"}) { code } } } }""", new HttpHeaders())
                .andExpect(jsonPath("$.errors[0].message").value("Product missing not found"))
                .andExpect(jsonPath("$.errors[0].extensions.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errors[0].extensions.requestId").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].path[2]").value("getProduct"));
    }

    @Test
    void writeRpcsAreMutations() throws Exception {
        // the fake ProductService does not implement UpsertProduct
        graphql("""
                mutation { masterdata { product { upsertProduct(input: {code: "P001"}) { code } } } }""", new HttpHeaders())
                .andExpect(jsonPath("$.errors[0].extensions.code").value("UNIMPLEMENTED"));
    }

    @Test
    void severalQueriesInOneRequest() throws Exception {
        graphql("""
                query {
                  masterdata { product {
                    a: getProduct(input: {code: "P001"}) { code }
                    b: getProduct(input: {code: "P002"}) { code }
                    c: getProduct(input: {code: "missing"}) { code }
                  } }
                }""", new HttpHeaders())
                .andExpect(jsonPath("$.data.masterdata.product.a.code").value("P001"))
                .andExpect(jsonPath("$.data.masterdata.product.b.code").value("P002"))
                // one failing field does not fail the others
                .andExpect(jsonPath("$.data.masterdata.product.c").isEmpty())
                .andExpect(jsonPath("$.errors[0].extensions.code").value("NOT_FOUND"));
    }

    @Test
    void queryFieldsCallTheirBackendsInParallel() throws Exception {
        var started = System.nanoTime();

        graphql("""
                query { masterdata { product {
                  a: getProduct(input: {code: "slow-1"}) { code }
                  b: getProduct(input: {code: "slow-2"}) { code }
                  c: getProduct(input: {code: "slow-3"}) { code }
                } } }""", new HttpHeaders())
                .andExpect(jsonPath("$.data.masterdata.product.c.code").value("slow-3"));

        // 3 x 300 ms one after the other would take 900 ms
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofMillis(700));
    }

    @Test
    void tooManyRpcCallsAreRejectedBeforeAnyCall() throws Exception {
        backends.lastHeaders.set(null);
        var fields = IntStream.rangeClosed(1, 21)
                .mapToObj(i -> "p" + i + ": getProduct(input: {code: \"P" + i + "\"}) { code }")
                .collect(Collectors.joining(" "));

        graphql("query { masterdata { product { " + fields + " } } }", new HttpHeaders())
                .andExpect(jsonPath("$.errors[0].message").value("Too many RPC calls in one request: 21 > 20"))
                .andExpect(jsonPath("$.data").doesNotExist());
        assertThat(backends.lastHeaders.get()).isNull();
    }

    @Test
    void introspectionFitsTheDepthLimit() throws Exception {
        graphql(IntrospectionQuery.INTROSPECTION_QUERY, new HttpHeaders())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.__schema.queryType.name").value("Query"));
    }

    @Test
    void persistedQueryIsSentByHashOnceKnown() throws Exception {
        var query = "query { masterdata { product { getProduct(input: {code: \"APQ\"}) { code } } } }";
        var hashOnly = Map.<String, Object>of("extensions",
                Map.of("persistedQuery", Map.of("version", 1, "sha256Hash", sha256(query))));

        // unknown hash: the client then sends the query with its hash
        send(hashOnly, new HttpHeaders()).andExpect(jsonPath("$.errors[0].message").value("PersistedQueryNotFound"));
        send(Map.of("query", query, "extensions", hashOnly.get("extensions")), new HttpHeaders())
                .andExpect(jsonPath("$.data.masterdata.product.getProduct.code").value("APQ"));

        send(hashOnly, new HttpHeaders())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.masterdata.product.getProduct.code").value("APQ"));
    }

    @Test
    void schemaIsGeneratedFromTheProtos() throws Exception {
        mvc.perform(get("/m/graphql/schema"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("getProduct(input: GetProductRequestInput): Product")))
                .andExpect(content().string(containsString("upsertProduct(")))
                .andExpect(content().string(containsString("type MasterdataProductMutations")))
                // server-streaming RPCs are not mapped
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("export("))))
                // epl.gateway.graphql.exclude: server-to-server RPCs
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("resolveTenant("))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("batchCheck("))));
    }

    private ResultActions graphql(String query, HttpHeaders headers) throws Exception {
        return send(Map.of("query", query), headers);
    }

    private ResultActions send(Map<String, Object> body, HttpHeaders headers) throws Exception {
        return GraphQlRequests.graphql(mvc, objectMapper.writeValueAsString(body), headers).andExpect(status().isOk());
    }

    private static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static Metadata.Key<String> key(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}
