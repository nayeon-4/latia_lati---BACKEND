package pe.edu.upc.latia_lati.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.latia_lati.entities.EmergencyContact;
import pe.edu.upc.latia_lati.entities.HealthProfile;

import java.util.List;

@Repository
public interface IEmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    // HU68 (simple): contactos marcados como principales
    public List<EmergencyContact> findByPrimaryContactTrue();

    // HU69 (join): ficha de emergencia de cada perfil de salud activo con su contacto principal
    // El nombre del paciente sale de users (dueño del perfil)
@Query(value = "SELECT hp.id_health_profile, u.first_name, u.last_name, hp.blood_type, ec.name, ec.phone, ec.relationship " +
        "FROM health_profiles hp " +
        "INNER JOIN users u " +
        "ON u.id_user = hp.id_owner_user " +
        "INNER JOIN emergency_contacts ec " +
        "ON ec.id_health_profile = hp.id_health_profile " +
        "WHERE ec.primary_contact = true AND hp.active = true " +
        "ORDER BY u.last_name, u.first_name",
        nativeQuery = true)
public List<Object[]> getEmergencyCards();

    // Reporte: cantidad de contactos de emergencia por parentesco
    @Query(value = "SELECT COALESCE(ec.relationship, 'Sin parentesco'), COUNT(ec.id) " +
            "FROM emergency_contacts ec " +
            "GROUP BY ec.relationship " +
            "ORDER BY COUNT(ec.id) DESC",
            nativeQuery = true)
    public List<Object[]> getTotalContactsByRelationship();

    // Reporte: contactos principales y secundarios de cada perfil de salud activo
    @Query(value = "SELECT COALESCE(u.first_name || ' ' || u.last_name, 'Perfil ' || hp.id_health_profile), " +
            "SUM(CASE WHEN ec.primary_contact = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN ec.primary_contact = true THEN 0 ELSE 1 END) " +
            "FROM health_profiles hp " +
            "INNER JOIN emergency_contacts ec " +
            "ON ec.id_health_profile = hp.id_health_profile " +
            "LEFT JOIN users u " +
            "ON u.id_user = hp.id_holder_user " +
            "WHERE hp.active = true " +
            "GROUP BY hp.id_health_profile, u.first_name, u.last_name " +
            "ORDER BY hp.id_health_profile",
            nativeQuery = true)
    public List<Object[]> getTotalContactsByProfile();
}
