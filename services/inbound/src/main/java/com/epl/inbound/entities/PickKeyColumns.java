package com.epl.inbound.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/** pick_key1..7 (lot no, expiry date …; meaning per tenant in md.picking_key_def). */
@Getter
@Setter
@Embeddable
public class PickKeyColumns {

    @Column(name = "pick_key1", length = 30)
    private String key1;

    @Column(name = "pick_key2", length = 30)
    private String key2;

    @Column(name = "pick_key3", length = 30)
    private String key3;

    @Column(name = "pick_key4", length = 30)
    private String key4;

    @Column(name = "pick_key5", length = 30)
    private String key5;

    @Column(name = "pick_key6", length = 30)
    private String key6;

    @Column(name = "pick_key7", length = 30)
    private String key7;
}
