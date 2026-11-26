package pe.edu.upc.latia_lati.serviceimplements;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upc.latia_lati.entities.*;
import pe.edu.upc.latia_lati.repositories.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatProfileContextServiceTest {
    private final IHealthProfileRepository healthProfileRepository = mock(IHealthProfileRepository.class);
    private final IUsersRepository usersRepository = mock(IUsersRepository.class);
    private final IClinicalRecordRepository clinicalRecordRepository = mock(IClinicalRecordRepository.class);
    private final IEmergencyContactRepository emergencyContactRepository = mock(IEmergencyContactRepository.class);
    private final IMedicalDocumentsRepository medicalDocumentsRepository = mock(IMedicalDocumentsRepository.class);
    private final IFamilyRelationshipRepository familyRelationshipRepository = mock(IFamilyRelationshipRepository.class);
    private final ChatProfileContextService service = new ChatProfileContextService(
            healthProfileRepository,
            usersRepository,
            clinicalRecordRepository,
            emergencyContactRepository,
            medicalDocumentsRepository,
            familyRelationshipRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buildsContextFromSelectedProfileWithoutSendingDocumentUrl() {
        Users owner = new Users();
        owner.setIdUser(7L);
        owner.setEmail("owner@example.test");
        owner.setActive(true);
        authenticate(owner.getEmail());

        HealthProfile profile = new HealthProfile();
        profile.setIdHealthProfile(18L);
        profile.setOwnerUser(owner);
        profile.setBirthDate(LocalDate.of(1990, 1, 1));
        profile.setSex("Femenino");
        profile.setBloodType("O+");
        profile.setPhone("555-0100");

        MedicalCondition condition = new MedicalCondition();
        condition.setNameMedicalCondition("Asma");
        condition.setDescriptionMedicalCondition("Condición respiratoria");
        ClinicalRecord record = new ClinicalRecord();
        record.setRecordType("Control");
        record.setTitle("Control respiratorio");
        record.setDescription("Seguimiento");
        record.setEventDate(new Date(0));
        record.setMedicalCondition(condition);

        ExamResult result = new ExamResult();
        result.setParameterName("Saturación");
        result.setResultValue("98");
        result.setUnit("%");
        record.setExamResults(List.of(result));

        EmergencyContact contact = new EmergencyContact();
        contact.setName("Contacto principal");
        contact.setPhone("555-0199");
        contact.setRelationship("Familiar");
        contact.setPrimaryContact(true);

        MedicalDocuments document = new MedicalDocuments(
                2L,
                "Examen",
                "Resultado de laboratorio",
                "https://private-file-url.example",
                LocalDate.of(2025, 1, 2),
                profile);

        when(usersRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(healthProfileRepository.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(18L, 7L))
                .thenReturn(Optional.of(profile));
        when(clinicalRecordRepository.findByHealthProfile_IdHealthProfileOrderByEventDateDesc(18L))
                .thenReturn(List.of(record));
        when(emergencyContactRepository.findByHealthProfile_IdHealthProfile(18L))
                .thenReturn(List.of(contact));
        when(familyRelationshipRepository
                .findByOriginProfile_IdHealthProfileOrRelativeProfile_IdHealthProfile(18L, 18L))
                .thenReturn(List.of());
        when(medicalDocumentsRepository
                .findByHealthProfile_IdHealthProfileOrderByDocumentDateDesc(18L))
                .thenReturn(List.of(document));

        String context = service.buildContext(18L);

        assertTrue(context.contains("Condición: Asma"));
        assertTrue(context.contains("Saturación = 98"));
        assertTrue(context.contains("555-0199"));
        assertTrue(context.contains("Resultado de laboratorio"));
        assertFalse(context.contains("private-file-url"));
    }

    @Test
    void refusesProfilesNotOwnedByAuthenticatedUserBeforeLoadingHealthData() {
        Users owner = new Users();
        owner.setIdUser(7L);
        owner.setEmail("owner@example.test");
        owner.setActive(true);
        authenticate(owner.getEmail());

        when(usersRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(healthProfileRepository.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(99L, 7L))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.buildContext(99L));

        verify(clinicalRecordRepository, never())
                .findByHealthProfile_IdHealthProfileOrderByEventDateDesc(99L);
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "test", List.of()));
    }
}
