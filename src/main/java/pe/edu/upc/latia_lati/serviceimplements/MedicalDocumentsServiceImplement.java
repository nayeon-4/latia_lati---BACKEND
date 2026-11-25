package pe.edu.upc.latia_lati.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.latia_lati.dtos.MonthlyDocumentCountDTO;
import pe.edu.upc.latia_lati.entities.HealthProfile;
import pe.edu.upc.latia_lati.entities.MedicalDocuments;
import pe.edu.upc.latia_lati.exceptions.BusinessRuleException;
import pe.edu.upc.latia_lati.exceptions.ResourceNotFoundException;
import pe.edu.upc.latia_lati.repositories.IHealthProfileRepository;
import pe.edu.upc.latia_lati.repositories.IMedicalDocumentsRepository;
import pe.edu.upc.latia_lati.serviceinterfaces.IHealthProfileService;
import pe.edu.upc.latia_lati.serviceinterfaces.IMedicalDocumentsService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MedicalDocumentsServiceImplement implements IMedicalDocumentsService {
    private final IMedicalDocumentsRepository mDR;
    private final IHealthProfileRepository hpR;
    private final IHealthProfileService hpS;

    public MedicalDocumentsServiceImplement(IMedicalDocumentsRepository mDR, IHealthProfileRepository hpR,
                                            IHealthProfileService hpS) {
        this.mDR = mDR;
        this.hpR = hpR;
        this.hpS = hpS;
    }

    private HealthProfile ownedProfile(Long profileId, Long ownerId) {
        return hpR.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(profileId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no disponible"));
    }

    private MedicalDocuments ownedDocument(Long id, Long ownerId) {
        return mDR.findByIdMedicalDocumentsAndHealthProfile_OwnerUser_IdUserAndHealthProfile_ActiveTrue(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no disponible"));
    }

    @Override
    @Transactional
    public void insert(MedicalDocuments document) {
        if (document.getIdMedicalDocuments() != null) {
            throw new BusinessRuleException("El ID se genera al registrar el documento");
        }
        document.setHealthProfile(ownedProfile(document.getHealthProfile().getIdHealthProfile(), hpS.getCurrentOwnerId()));
        mDR.save(document);
    }

    @Override
    public List<MedicalDocuments> list() {
        return mDR.buscarDocumentosPorUsuario(hpS.getCurrentOwnerId());
    }

    @Override
    @Transactional
    public void update(MedicalDocuments document) {
        if (document.getIdMedicalDocuments() == null) {
            throw new BusinessRuleException("Se necesita el ID del documento");
        }
        Long ownerId = hpS.getCurrentOwnerId();
        MedicalDocuments saved = ownedDocument(document.getIdMedicalDocuments(), ownerId);
        saved.setHealthProfile(ownedProfile(document.getHealthProfile().getIdHealthProfile(), ownerId));
        saved.setTitle(document.getTitle());
        saved.setDocumentType(document.getDocumentType());
        saved.setDocumentDate(document.getDocumentDate());
        saved.setFileURL(document.getFileURL());
        mDR.save(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mDR.delete(ownedDocument(id, hpS.getCurrentOwnerId()));
    }

    @Override
    public Optional<MedicalDocuments> listId(Long id) {
        return mDR.findByIdMedicalDocumentsAndHealthProfile_OwnerUser_IdUserAndHealthProfile_ActiveTrue(id, hpS.getCurrentOwnerId());
    }

    @Override
    public List<MedicalDocuments> buscarDocumentosPorFecha(LocalDate documentDate) {
        return mDR.findByDocumentDateAndHealthProfile_OwnerUser_IdUserAndHealthProfile_ActiveTrue(documentDate, hpS.getCurrentOwnerId());
    }

    @Override
    public List<MedicalDocuments> buscarDocumentosPorUsuario(Long userId) {
        Long ownerId = hpS.getCurrentOwnerId();
        if (!ownerId.equals(userId)) throw new ResourceNotFoundException("Cuenta no disponible");
        return mDR.buscarDocumentosPorUsuario(ownerId);
    }

    private String validateFilters(LocalDate startDate, LocalDate endDate, String documentType) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BusinessRuleException("La fecha final debe ser igual o posterior a la inicial");
        }
        if (ChronoUnit.MONTHS.between(YearMonth.from(startDate), YearMonth.from(endDate)) >= 60) {
            throw new BusinessRuleException("Elige un intervalo de hasta 60 meses");
        }
        String type = documentType == null ? "" : documentType.trim();
        if (type.length() > 50) throw new BusinessRuleException("El tipo admite hasta 50 caracteres");
        return type;
    }

    @Override
    public List<MedicalDocuments> findTimeline(Long profileId, LocalDate startDate, LocalDate endDate, String documentType) {
        String type = validateFilters(startDate, endDate, documentType);
        Long ownerId = hpS.getCurrentOwnerId();
        ownedProfile(profileId, ownerId);
        return mDR.findTimeline(profileId, ownerId, startDate, endDate, type);
    }

    @Override
    public List<MonthlyDocumentCountDTO> countByMonth(Long profileId, LocalDate startDate, LocalDate endDate, String documentType) {
        String type = validateFilters(startDate, endDate, documentType);
        Long ownerId = hpS.getCurrentOwnerId();
        ownedProfile(profileId, ownerId);
        return mDR.countByMonth(profileId, ownerId, startDate, endDate, type).stream()
                .map(row -> new MonthlyDocumentCountDTO(((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(), ((Number) row[2]).longValue()))
                .toList();
    }
}
