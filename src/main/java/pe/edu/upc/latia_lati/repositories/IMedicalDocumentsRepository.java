package pe.edu.upc.latia_lati.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.latia_lati.entities.MedicalDocuments;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IMedicalDocumentsRepository extends JpaRepository<MedicalDocuments, Long> {
    Optional<MedicalDocuments> findByIdMedicalDocumentsAndHealthProfile_OwnerUser_IdUserAndHealthProfile_ActiveTrue(
            Long documentId, Long ownerId);

    List<MedicalDocuments> findByDocumentDateAndHealthProfile_OwnerUser_IdUserAndHealthProfile_ActiveTrue(
            LocalDate documentDate, Long ownerId);

    @Query(value = """
            SELECT md.* FROM medical_documents md
            JOIN health_profiles hp ON hp.id_health_profile = md.id_health_profile
            WHERE hp.id_owner_user = :userId AND hp.active = true
            ORDER BY md.document_date DESC NULLS LAST, md.id_medical_documents DESC
            """, nativeQuery = true)
    List<MedicalDocuments> buscarDocumentosPorUsuario(@Param("userId") Long userId);

    // Ambas consultas cuentan exactamente los mismos documentos y usan la fecha del examen o informe.
    String EVOLUTION_FILTER = """
            FROM medical_documents md
            JOIN health_profiles hp ON hp.id_health_profile = md.id_health_profile
            WHERE hp.id_health_profile = :profileId AND hp.id_owner_user = :ownerId AND hp.active = true
              AND md.document_date BETWEEN :startDate AND :endDate
              AND strpos(lower(md.document_type), lower(:documentType)) > 0
            """;

    @Query(value = "SELECT md.* " + EVOLUTION_FILTER
            + " ORDER BY md.document_date DESC, md.id_medical_documents DESC", nativeQuery = true)
    List<MedicalDocuments> findTimeline(@Param("profileId") Long profileId, @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("documentType") String documentType);

    @Query(value = """
            SELECT date_part('year', md.document_date), date_part('month', md.document_date), COUNT(*)
            """ + EVOLUTION_FILTER + " GROUP BY 1, 2 ORDER BY 1, 2", nativeQuery = true)
    List<Object[]> countByMonth(@Param("profileId") Long profileId, @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
            @Param("documentType") String documentType);
}
