package com.flash.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.flash.app.IntegrationTest;
import com.flash.app.domain.Pc;
import com.flash.app.repository.PcRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link PcResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PcResourceIT {

    private static final String DEFAULT_MAKE = "AAAAAAAAAA";
    private static final String UPDATED_MAKE = "BBBBBBBBBB";

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final Integer DEFAULT_PRICE = 1;
    private static final Integer UPDATED_PRICE = 2;

    private static final String ENTITY_API_URL = "/api/pcs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private PcRepository pcRepository;

    @Autowired
    private MockMvc restPcMockMvc;

    private Pc pc;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pc createEntity() {
        Pc pc = new Pc().make(DEFAULT_MAKE).model(DEFAULT_MODEL).price(DEFAULT_PRICE);
        return pc;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Pc createUpdatedEntity() {
        Pc pc = new Pc().make(UPDATED_MAKE).model(UPDATED_MODEL).price(UPDATED_PRICE);
        return pc;
    }

    @BeforeEach
    public void initTest() {
        pcRepository.deleteAll();
        pc = createEntity();
    }

    @Test
    void createPc() throws Exception {
        int databaseSizeBeforeCreate = pcRepository.findAll().size();
        // Create the Pc
        restPcMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pc)))
            .andExpect(status().isCreated());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeCreate + 1);
        Pc testPc = pcList.get(pcList.size() - 1);
        assertThat(testPc.getMake()).isEqualTo(DEFAULT_MAKE);
        assertThat(testPc.getModel()).isEqualTo(DEFAULT_MODEL);
        assertThat(testPc.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    void createPcWithExistingId() throws Exception {
        // Create the Pc with an existing ID
        pc.setId("existing_id");

        int databaseSizeBeforeCreate = pcRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPcMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pc)))
            .andExpect(status().isBadRequest());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllPcs() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        // Get all the pcList
        restPcMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pc.getId())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE)));
    }

    @Test
    void getPc() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        // Get the pc
        restPcMockMvc
            .perform(get(ENTITY_API_URL_ID, pc.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pc.getId()))
            .andExpect(jsonPath("$.make").value(DEFAULT_MAKE))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE));
    }

    @Test
    void getNonExistingPc() throws Exception {
        // Get the pc
        restPcMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingPc() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        int databaseSizeBeforeUpdate = pcRepository.findAll().size();

        // Update the pc
        Pc updatedPc = pcRepository.findById(pc.getId()).get();
        updatedPc.make(UPDATED_MAKE).model(UPDATED_MODEL).price(UPDATED_PRICE);

        restPcMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPc.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPc))
            )
            .andExpect(status().isOk());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
        Pc testPc = pcList.get(pcList.size() - 1);
        assertThat(testPc.getMake()).isEqualTo(UPDATED_MAKE);
        assertThat(testPc.getModel()).isEqualTo(UPDATED_MODEL);
        assertThat(testPc.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    void putNonExistingPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(
                put(ENTITY_API_URL_ID, pc.getId()).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pc))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pc))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pc)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePcWithPatch() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        int databaseSizeBeforeUpdate = pcRepository.findAll().size();

        // Update the pc using partial update
        Pc partialUpdatedPc = new Pc();
        partialUpdatedPc.setId(pc.getId());

        partialUpdatedPc.price(UPDATED_PRICE);

        restPcMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPc.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPc))
            )
            .andExpect(status().isOk());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
        Pc testPc = pcList.get(pcList.size() - 1);
        assertThat(testPc.getMake()).isEqualTo(DEFAULT_MAKE);
        assertThat(testPc.getModel()).isEqualTo(DEFAULT_MODEL);
        assertThat(testPc.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    void fullUpdatePcWithPatch() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        int databaseSizeBeforeUpdate = pcRepository.findAll().size();

        // Update the pc using partial update
        Pc partialUpdatedPc = new Pc();
        partialUpdatedPc.setId(pc.getId());

        partialUpdatedPc.make(UPDATED_MAKE).model(UPDATED_MODEL).price(UPDATED_PRICE);

        restPcMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPc.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPc))
            )
            .andExpect(status().isOk());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
        Pc testPc = pcList.get(pcList.size() - 1);
        assertThat(testPc.getMake()).isEqualTo(UPDATED_MAKE);
        assertThat(testPc.getModel()).isEqualTo(UPDATED_MODEL);
        assertThat(testPc.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    void patchNonExistingPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, pc.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pc))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(pc))
            )
            .andExpect(status().isBadRequest());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPc() throws Exception {
        int databaseSizeBeforeUpdate = pcRepository.findAll().size();
        pc.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPcMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(pc)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Pc in the database
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePc() throws Exception {
        // Initialize the database
        pcRepository.save(pc);

        int databaseSizeBeforeDelete = pcRepository.findAll().size();

        // Delete the pc
        restPcMockMvc.perform(delete(ENTITY_API_URL_ID, pc.getId()).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Pc> pcList = pcRepository.findAll();
        assertThat(pcList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
