package com.epl.auth.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** One iam.user_attribute row, e.g. max_adjust_qty = 500 (used by ABAC conditions). */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class UserAttribute {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** JSON value, so conditions can compare numbers: "500" is stored as 500, "OPS" as "OPS". */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attr_value", nullable = false)
    private JsonNode value;

    /** copied into the JWT "attrs" claim */
    @Column(name = "in_token", nullable = false)
    private boolean inToken;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;

    public UserAttribute(String value, String updatedBy) {
        setText(value);
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    /** The value as the API shows it (map<string, string>). */
    public String getText() {
        return value.isTextual() ? value.asText() : value.toString();
    }

    public void setText(String text) {
        JsonNode node;
        try {
            node = JSON.readTree(text);
        } catch (JsonProcessingException e) {
            node = null;
        }
        this.value = node != null && (node.isNumber() || node.isBoolean()) ? node : TextNode.valueOf(text);
    }
}
