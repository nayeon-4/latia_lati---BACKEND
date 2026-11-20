package pe.edu.upc.latia_lati.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.latia_lati.entities.FamilyRelationship;
import pe.edu.upc.latia_lati.entities.HealthProfile;

import java.util.List;

@Repository
public interface IFamilyRelationshipRepository extends JpaRepository<FamilyRelationship, Long> {
    List<FamilyRelationship> findByOriginProfile_IdHealthProfileOrRelativeProfile_IdHealthProfile(
            Long originProfileId,
            Long relativeProfileId);
    // HU46/HU49: verificar si ya existe la misma relación
    boolean existsByOriginProfile_IdHealthProfileAndRelativeProfile_IdHealthProfile(
            Long originProfileId,
            Long relativeProfileId);

    // Reporte: cantidad de relaciones familiares por tipo de parentesco
    @Query(value = "SELECT fr.relationship_type, COUNT(fr.id) " +
            "FROM family_relationships fr " +
            "GROUP BY fr.relationship_type " +
            "ORDER BY COUNT(fr.id) DESC",
            nativeQuery = true)
    List<Object[]> getTotalRelationshipsByType();

    // Reporte: cantidad de familiares registrados por cada perfil de salud activo
    @Query(value = "SELECT COALESCE(u.first_name || ' ' || u.last_name, 'Perfil ' || hp.id_health_profile), COUNT(fr.id) " +
            "FROM health_profiles hp " +
            "INNER JOIN family_relationships fr " +
            "ON fr.id_origin_profile = hp.id_health_profile " +
            "LEFT JOIN users u " +
            "ON u.id_user = hp.id_holder_user " +
            "WHERE hp.active = true " +
            "GROUP BY hp.id_health_profile, u.first_name, u.last_name " +
            "ORDER BY COUNT(fr.id) DESC",
            nativeQuery = true)
    List<Object[]> getTotalRelativesByProfile();
}
