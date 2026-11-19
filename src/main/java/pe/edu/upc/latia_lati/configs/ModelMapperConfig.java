package pe.edu.upc.latia_lati.configs;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.edu.upc.latia_lati.dtos.RoleDTO;
import pe.edu.upc.latia_lati.dtos.ClinicalRecordDTO;
import pe.edu.upc.latia_lati.dtos.ClinicalRecordDocumentDTO;
import pe.edu.upc.latia_lati.dtos.FamilyRelationshipDTO;
import pe.edu.upc.latia_lati.dtos.HealthProfileDTO;
import pe.edu.upc.latia_lati.dtos.MedicalDocumentsDTO;
import pe.edu.upc.latia_lati.entities.Role;
import pe.edu.upc.latia_lati.entities.ClinicalRecord;
import pe.edu.upc.latia_lati.entities.ClinicalRecordDocument;
import pe.edu.upc.latia_lati.entities.FamilyRelationship;
import pe.edu.upc.latia_lati.entities.HealthProfile;
import pe.edu.upc.latia_lati.entities.MedicalDocuments;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // ---------- Role ----------
        mapper.typeMap(Role.class, RoleDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getUser().getIdUser(),
                            RoleDTO::setIdUser);
                });

        mapper.typeMap(RoleDTO.class, Role.class)
                .addMappings(m -> {
                    m.skip(Role::setUser);
                });

        // ---------- ClinicalRecord ----------
        mapper.typeMap(ClinicalRecord.class, ClinicalRecordDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getHealthProfile().getIdHealthProfile(),
                            ClinicalRecordDTO::setIdHealthProfile);
                    m.map(src -> src.getMedicalCondition().getIdMedicalCondition(),
                            ClinicalRecordDTO::setIdCondition);
                });
        mapper.typeMap(ClinicalRecordDTO.class, ClinicalRecord.class)
                .addMappings(m -> {
                    m.skip(ClinicalRecord::setHealthProfile);
                    m.skip(ClinicalRecord::setMedicalCondition);
                });

        // ---------- HealthProfile ----------
        mapper.typeMap(HealthProfile.class, HealthProfileDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getOwnerUser().getIdUser(),
                            HealthProfileDTO::setIdOwnerUser);
                    m.map(src -> src.getHolderUser().getIdUser(),
                            HealthProfileDTO::setIdHolderUser);
                });
        mapper.typeMap(HealthProfileDTO.class, HealthProfile.class)
                .addMappings(m -> {
                    m.skip(HealthProfile::setOwnerUser);
                    m.skip(HealthProfile::setHolderUser);
                });

        // ---------- ClinicalRecordDocument ----------
        mapper.typeMap(ClinicalRecordDocument.class, ClinicalRecordDocumentDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getClinicalRecord().getIdClinicalRecord(),
                            ClinicalRecordDocumentDTO::setClinicalRecordId);
                    m.map(src -> src.getMedicalDocument().getIdMedicalDocuments(),
                            ClinicalRecordDocumentDTO::setMedicalDocumentId);
                });
        mapper.typeMap(ClinicalRecordDocumentDTO.class, ClinicalRecordDocument.class)
                .addMappings(m -> {
                    m.skip(ClinicalRecordDocument::setClinicalRecord);
                    m.skip(ClinicalRecordDocument::setMedicalDocument);
                });

        // ---------- MedicalDocuments ----------
        mapper.typeMap(MedicalDocuments.class, MedicalDocumentsDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getHealthProfile().getIdHealthProfile(),
                            MedicalDocumentsDTO::setIdHealthProfile);
                });
        mapper.typeMap(MedicalDocumentsDTO.class, MedicalDocuments.class)
                .addMappings(m -> {
                    m.skip(MedicalDocuments::setHealthProfile);
                });

        // ---------- FamilyRelationship ----------
        mapper.typeMap(FamilyRelationship.class, FamilyRelationshipDTO.class)
                .addMappings(m -> {
                    m.map(src -> src.getOriginProfile().getIdHealthProfile(),
                            FamilyRelationshipDTO::setIdOriginProfile);
                    m.map(src -> src.getRelativeProfile().getIdHealthProfile(),
                            FamilyRelationshipDTO::setIdRelativeProfile);
                });
        mapper.typeMap(FamilyRelationshipDTO.class, FamilyRelationship.class)
                .addMappings(m -> {
                    m.skip(FamilyRelationship::setOriginProfile);
                    m.skip(FamilyRelationship::setRelativeProfile);
                });

        return mapper;
    }
}