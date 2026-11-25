package pe.edu.upc.latia_lati.serviceimplements;

import org.junit.jupiter.api.Test;
import pe.edu.upc.latia_lati.dtos.MonthlyDocumentCountDTO;
import pe.edu.upc.latia_lati.entities.HealthProfile;
import pe.edu.upc.latia_lati.entities.MedicalDocuments;
import pe.edu.upc.latia_lati.exceptions.BusinessRuleException;
import pe.edu.upc.latia_lati.exceptions.ResourceNotFoundException;
import pe.edu.upc.latia_lati.repositories.IHealthProfileRepository;
import pe.edu.upc.latia_lati.repositories.IMedicalDocumentsRepository;
import pe.edu.upc.latia_lati.serviceinterfaces.IHealthProfileService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalDocumentEvolutionTest {
    private final IMedicalDocumentsRepository documents = mock(IMedicalDocumentsRepository.class);
    private final IHealthProfileRepository profiles = mock(IHealthProfileRepository.class);
    private final IHealthProfileService account = mock(IHealthProfileService.class);
    private final MedicalDocumentsServiceImplement service = new MedicalDocumentsServiceImplement(documents, profiles, account);
    private final LocalDate from = LocalDate.of(2025, 12, 1);
    private final LocalDate to = LocalDate.of(2026, 1, 31);

    @Test
    void neitherReportReadsDocumentsFromAnUnavailableProfile() {
        when(account.getCurrentOwnerId()).thenReturn(4L);
        when(profiles.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(90L, 4L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findTimeline(90L, from, to, null));
        assertThrows(ResourceNotFoundException.class, () -> service.countByMonth(90L, from, to, ""));
        verifyNoInteractions(documents);
    }

    @Test
    void bothQueriesReceiveTheSameOwnerPeriodAndTrimmedType() {
        when(account.getCurrentOwnerId()).thenReturn(4L);
        when(profiles.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(8L, 4L))
                .thenReturn(Optional.of(new HealthProfile()));
        when(documents.findTimeline(8L, 4L, from, to, "Receta")).thenReturn(List.of());
        when(documents.countByMonth(8L, 4L, from, to, "Receta")).thenReturn(List.of(
                new Object[]{new BigDecimal("2025"), new BigDecimal("12"), 2L},
                new Object[]{new BigDecimal("2026"), BigDecimal.ONE, 1L}));
        assertTrue(service.findTimeline(8L, from, to, " Receta ").isEmpty());
        List<MonthlyDocumentCountDTO> rows = service.countByMonth(8L, from, to, " Receta ");
        assertEquals(List.of(2025, 2026), rows.stream().map(MonthlyDocumentCountDTO::getYear).toList());
        assertEquals(List.of(12, 1), rows.stream().map(MonthlyDocumentCountDTO::getMonth).toList());
        assertEquals(3L, rows.stream().mapToLong(MonthlyDocumentCountDTO::getTotal).sum());
        verify(documents).findTimeline(8L, 4L, from, to, "Receta");
        verify(documents).countByMonth(8L, 4L, from, to, "Receta");
    }

    @Test
    void invalidIntervalsNeverReachTheDatabase() {
        assertThrows(BusinessRuleException.class, () -> service.findTimeline(8L, to, from, ""));
        assertThrows(BusinessRuleException.class, () -> service.countByMonth(8L, from, from.plusYears(5), ""));
        verifyNoInteractions(documents, profiles);
    }

    @Test
    void createCannotSupplyAnIdOrUseAnotherAccountsProfile() {
        MedicalDocuments document = new MedicalDocuments();
        document.setIdMedicalDocuments(33L);
        assertThrows(BusinessRuleException.class, () -> service.insert(document));
        document.setIdMedicalDocuments(null);
        HealthProfile target = new HealthProfile();
        target.setIdHealthProfile(90L);
        document.setHealthProfile(target);
        when(account.getCurrentOwnerId()).thenReturn(4L);
        when(profiles.findByIdHealthProfileAndOwnerUser_IdUserAndActiveTrue(90L, 4L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.insert(document));
        verifyNoInteractions(documents);
    }
}
