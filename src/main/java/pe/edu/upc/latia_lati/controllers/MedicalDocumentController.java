package pe.edu.upc.latia_lati.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.upc.latia_lati.dtos.MedicalDocumentsDTO;
import pe.edu.upc.latia_lati.dtos.MonthlyDocumentCountDTO;
import pe.edu.upc.latia_lati.entities.HealthProfile;
import pe.edu.upc.latia_lati.entities.MedicalDocuments;
import pe.edu.upc.latia_lati.exceptions.ResourceNotFoundException;
import pe.edu.upc.latia_lati.serviceinterfaces.IMedicalDocumentsService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicaldocuments")
public class MedicalDocumentController {
    private final IMedicalDocumentsService mdS;
    private final ModelMapper modelMapper;

    public MedicalDocumentController(IMedicalDocumentsService mdS, ModelMapper modelMapper) {
        this.mdS = mdS;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<MedicalDocumentsDTO>> listar() {
        return ResponseEntity.ok(mdS.list().stream().map(this::toDTO).toList());
    }

    @PostMapping
    public ResponseEntity<MedicalDocumentsDTO> registrar(@Valid @RequestBody MedicalDocumentsDTO dto) {
        MedicalDocuments document = toEntity(dto);
        mdS.insert(document);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(document.getIdMedicalDocuments()).toUri();
        return ResponseEntity.created(location).body(toDTO(document));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalDocumentsDTO> buscarPorId(@PathVariable Long id) {
        MedicalDocuments document = mdS.listId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no disponible"));
        return ResponseEntity.ok(toDTO(document));
    }

    @PutMapping
    public ResponseEntity<MedicalDocumentsDTO> actualizar(@Valid @RequestBody MedicalDocumentsDTO dto) {
        MedicalDocuments document = toEntity(dto);
        mdS.update(document);
        return ResponseEntity.ok(toDTO(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mdS.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<MedicalDocumentsDTO>> obtenerPorFecha(@RequestParam LocalDate documentDate) {
        return ResponseEntity.ok(mdS.buscarDocumentosPorFecha(documentDate).stream().map(this::toDTO).toList());
    }

    @GetMapping("/doc-por-usuario")
    public ResponseEntity<List<MedicalDocumentsDTO>> obtenerPorUsuario(@RequestParam Long userId) {
        return ResponseEntity.ok(mdS.buscarDocumentosPorUsuario(userId).stream().map(this::toDTO).toList());
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<MedicalDocumentsDTO>> findTimeline(@RequestParam Long profileId,
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "") String documentType) {
        return ResponseEntity.ok(mdS.findTimeline(profileId, startDate, endDate, documentType)
                .stream().map(this::toDTO).toList());
    }

    @GetMapping("/report/monthly")
    public ResponseEntity<List<MonthlyDocumentCountDTO>> countByMonth(@RequestParam Long profileId,
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "") String documentType) {
        return ResponseEntity.ok(mdS.countByMonth(profileId, startDate, endDate, documentType));
    }

    private MedicalDocumentsDTO toDTO(MedicalDocuments document) {
        return modelMapper.map(document, MedicalDocumentsDTO.class);
    }

    private MedicalDocuments toEntity(MedicalDocumentsDTO dto) {
        MedicalDocuments document = modelMapper.map(dto, MedicalDocuments.class);
        HealthProfile profile = new HealthProfile();
        profile.setIdHealthProfile(dto.getIdHealthProfile());
        document.setHealthProfile(profile);
        return document;
    }
}
