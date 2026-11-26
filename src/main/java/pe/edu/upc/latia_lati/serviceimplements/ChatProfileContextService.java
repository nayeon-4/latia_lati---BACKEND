package pe.edu.upc.latia_lati.serviceimplements;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.latia_lati.entities.*;
import pe.edu.upc.latia_lati.repositories.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class ChatProfileContextService {
    private final IHealthProfileRepository healthProfileRepository;
    private final IUsersRepository usersRepository;
    private final IClinicalRecordRepository clinicalRecordRepository;
    private final IEmergencyContactRepository emergencyContactRepository;
    private final IMedicalDocumentsRepository medicalDocumentsRepository;
    private final IFamilyRelationshipRepository familyRelationshipRepository;

    public ChatProfileContextService(
            IHealthProfileRepository healthProfileRepository,
            IUsersRepository usersRepository,
            IClinicalRecordRepository clinicalRecordRepository,
            IEmergencyContactRepository emergencyContactRepository,
            IMedicalDocumentsRepository medicalDocumentsRepository,
            IFamilyRelationshipRepository familyRelationshipRepository) {
        this.healthProfileRepository = healthProfileRepository;
        this.usersRepository = usersRepository;
        this.clinicalRecordRepository = clinicalRecordRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.medicalDocumentsRepository = medicalDocumentsRepository;
        this.familyRelationshipRepository = familyRelationshipRepository;
    }

    @Transactional(readOnly = true)
    public String buildContext(Long profileId) {
        HealthProfile profile = healthProfileRepository
                .findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(profileId, authenticatedUserId())
                .orElseThrow(() -> new AccessDeniedException("No tienes acceso a este perfil."));

        StringBuilder context = new StringBuilder("PERFIL ACTIVO\n");
        if (profile.getBirthDate() != null) {
            context.append("Edad aproximada: ")
                    .append(Period.between(profile.getBirthDate(), LocalDate.now()).getYears())
                    .append(" años\n");
        }
        append(context, "Sexo", profile.getSex());
        append(context, "Grupo sanguíneo", profile.getBloodType());
        append(context, "Teléfono del perfil", profile.getPhone());

        appendClinicalRecords(context, profileId);
        appendEmergencyContacts(context, profileId);
        appendFamilyRelationships(context, profileId);
        appendMedicalDocuments(context, profileId);
        return context.toString();
    }

    private void appendClinicalRecords(StringBuilder context, Long profileId) {
        List<ClinicalRecord> records = clinicalRecordRepository
                .findByHealthProfile_IdHealthProfileOrderByEventDateDesc(profileId);
        context.append("\nHISTORIAL CLÍNICO\n");
        if (records.isEmpty()) {
            context.append("No hay registros clínicos.\n");
            return;
        }

        for (ClinicalRecord record : records) {
            context.append("- Fecha: ").append(value(record.getEventDate()))
                    .append("; tipo: ").append(value(record.getRecordType()))
                    .append("; título: ").append(value(record.getTitle())).append('\n');
            append(context, "  Descripción", record.getDescription());
            append(context, "  Profesional", record.getProfessionalName());
            MedicalCondition condition = record.getMedicalCondition();
            if (condition != null) {
                append(context, "  Condición", condition.getNameMedicalCondition());
                append(context, "  Detalle de condición", condition.getDescriptionMedicalCondition());
            }

            for (ExamResult result : record.getExamResults()) {
                context.append("  Examen: ").append(value(result.getParameterName()))
                        .append(" = ").append(value(result.getResultValue()));
                appendInline(context, "unidad", result.getUnit());
                context.append('\n');
            }

            for (MedicationTreatment treatment : record.getMedicationTreatments()) {
                appendTreatment(context, treatment);
            }
        }
    }

    private void appendTreatment(StringBuilder context, MedicationTreatment treatment) {
        Medications medication = treatment.getMedication();
        context.append("  Tratamiento: ")
                .append(medication == null ? "Medicamento no especificado" : value(medication.getNameMedications()));
        if (medication != null) {
            appendInline(context, "forma", medication.getDosageForm());
            appendInline(context, "concentración", medication.getStrength());
        }
        appendInline(context, "dosis", treatment.getDose());
        appendInline(context, "vía", treatment.getAdministrationRoute());
        appendInline(context, "desde", treatment.getStartDate());
        appendInline(context, "hasta", treatment.getEndDate());
        context.append('\n');

        for (MedicationSchedule schedule : treatment.getSchedules()) {
            context.append("    Horario: ").append(value(schedule.getReminderTime()))
                    .append("; intervalo (horas): ").append(value(schedule.getIntervalHours()))
                    .append('\n');
        }
    }

    private void appendEmergencyContacts(StringBuilder context, Long profileId) {
        List<EmergencyContact> contacts = emergencyContactRepository
                .findByHealthProfile_IdHealthProfile(profileId);
        context.append("\nCONTACTOS DE EMERGENCIA\n");
        if (contacts.isEmpty()) {
            context.append("No hay contactos registrados.\n");
            return;
        }
        for (EmergencyContact contact : contacts) {
            context.append("- ").append(value(contact.getName()));
            appendInline(context, "teléfono", contact.getPhone());
            appendInline(context, "parentesco", contact.getRelationship());
            if (Boolean.TRUE.equals(contact.getPrimaryContact())) {
                context.append("; contacto principal");
            }
            context.append('\n');
        }
    }

    private void appendFamilyRelationships(StringBuilder context, Long profileId) {
        List<FamilyRelationship> relationships =
                familyRelationshipRepository
                        .findByOriginProfile_IdHealthProfileOrRelativeProfile_IdHealthProfile(
                                profileId, profileId);
        context.append("\nVÍNCULOS FAMILIARES\n");
        if (relationships.isEmpty()) {
            context.append("No hay vínculos familiares registrados.\n");
            return;
        }
        for (FamilyRelationship relationship : relationships) {
            append(context, "- Parentesco registrado", relationship.getRelationshipType());
        }
        context.append("No se incluyen datos médicos de otros perfiles familiares.\n");
    }

    private void appendMedicalDocuments(StringBuilder context, Long profileId) {
        List<MedicalDocuments> documents = medicalDocumentsRepository
                .findByHealthProfile_IdHealthProfileOrderByDocumentDateDesc(profileId);
        context.append("\nDOCUMENTOS MÉDICOS (solo metadatos)\n");
        if (documents.isEmpty()) {
            context.append("No hay documentos registrados.\n");
            return;
        }
        for (MedicalDocuments document : documents) {
            context.append("- ").append(value(document.getTitle()))
                    .append("; tipo: ").append(value(document.getDocumentType()))
                    .append("; fecha: ").append(value(document.getDocumentDate()))
                    .append('\n');
        }
    }

    private Long authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Inicia sesión para consultar el perfil.");
        }

        String username = authentication.getName();
        Users user = usersRepository.findByEmail(username)
                .or(() -> usersRepository.findByUsername(username))
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActive()))
                .orElseThrow(() -> new AccessDeniedException("La cuenta no está disponible."));
        return user.getIdUser();
    }

    private void append(StringBuilder context, String label, Object value) {
        if (value != null && !value.toString().isBlank()) {
            context.append(label).append(": ").append(value).append('\n');
        }
    }

    private void appendInline(StringBuilder context, String label, Object value) {
        if (value != null && !value.toString().isBlank()) {
            context.append("; ").append(label).append(": ").append(value);
        }
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "no registrado" : value.toString();
    }
}
