package com.flash.app.web.rest;

import com.flash.app.domain.Pc;
import com.flash.app.repository.PcRepository;
import com.flash.app.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.flash.app.domain.Pc}.
 */
@RestController
@RequestMapping("/api")
public class PcResource {

    private final Logger log = LoggerFactory.getLogger(PcResource.class);

    private static final String ENTITY_NAME = "flashAppPc";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PcRepository pcRepository;

    public PcResource(PcRepository pcRepository) {
        this.pcRepository = pcRepository;
    }

    /**
     * {@code POST  /pcs} : Create a new pc.
     *
     * @param pc the pc to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pc, or with status {@code 400 (Bad Request)} if the pc has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pcs")
    public ResponseEntity<Pc> createPc(@RequestBody Pc pc) throws URISyntaxException {
        log.debug("REST request to save Pc : {}", pc);
        if (pc.getId() != null) {
            throw new BadRequestAlertException("A new pc cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Pc result = pcRepository.save(pc);
        return ResponseEntity
            .created(new URI("/api/pcs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /pcs/:id} : Updates an existing pc.
     *
     * @param id the id of the pc to save.
     * @param pc the pc to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pc,
     * or with status {@code 400 (Bad Request)} if the pc is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pc couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pcs/{id}")
    public ResponseEntity<Pc> updatePc(@PathVariable(value = "id", required = false) final String id, @RequestBody Pc pc)
        throws URISyntaxException {
        log.debug("REST request to update Pc : {}, {}", id, pc);
        if (pc.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pc.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pcRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Pc result = pcRepository.save(pc);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, pc.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /pcs/:id} : Partial updates given fields of an existing pc, field will ignore if it is null
     *
     * @param id the id of the pc to save.
     * @param pc the pc to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pc,
     * or with status {@code 400 (Bad Request)} if the pc is not valid,
     * or with status {@code 404 (Not Found)} if the pc is not found,
     * or with status {@code 500 (Internal Server Error)} if the pc couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/pcs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Pc> partialUpdatePc(@PathVariable(value = "id", required = false) final String id, @RequestBody Pc pc)
        throws URISyntaxException {
        log.debug("REST request to partial update Pc partially : {}, {}", id, pc);
        if (pc.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, pc.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!pcRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Pc> result = pcRepository
            .findById(pc.getId())
            .map(existingPc -> {
                if (pc.getMake() != null) {
                    existingPc.setMake(pc.getMake());
                }
                if (pc.getModel() != null) {
                    existingPc.setModel(pc.getModel());
                }
                if (pc.getPrice() != null) {
                    existingPc.setPrice(pc.getPrice());
                }

                return existingPc;
            })
            .map(pcRepository::save);

        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, pc.getId()));
    }

    /**
     * {@code GET  /pcs} : get all the pcs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pcs in body.
     */
    @GetMapping("/pcs")
    public List<Pc> getAllPcs() {
        log.debug("REST request to get all Pcs");
        return pcRepository.findAll();
    }

    /**
     * {@code GET  /pcs/:id} : get the "id" pc.
     *
     * @param id the id of the pc to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pc, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pcs/{id}")
    public ResponseEntity<Pc> getPc(@PathVariable String id) {
        log.debug("REST request to get Pc : {}", id);
        Optional<Pc> pc = pcRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(pc);
    }

    /**
     * {@code DELETE  /pcs/:id} : delete the "id" pc.
     *
     * @param id the id of the pc to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pcs/{id}")
    public ResponseEntity<Void> deletePc(@PathVariable String id) {
        log.debug("REST request to delete Pc : {}", id);
        pcRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }
}
