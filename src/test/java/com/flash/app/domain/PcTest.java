package com.flash.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.flash.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PcTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Pc.class);
        Pc pc1 = new Pc();
        pc1.setId("id1");
        Pc pc2 = new Pc();
        pc2.setId(pc1.getId());
        assertThat(pc1).isEqualTo(pc2);
        pc2.setId("id2");
        assertThat(pc1).isNotEqualTo(pc2);
        pc1.setId(null);
        assertThat(pc1).isNotEqualTo(pc2);
    }
}
