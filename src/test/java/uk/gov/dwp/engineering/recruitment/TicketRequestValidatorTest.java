package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

class TicketRequestValidatorTest {

    private TicketRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TicketRequestValidator();
    }

    @Test
    @DisplayName("Valid request with adult only passes validation")
    void givenValidAdultOnlyRequest_whenValidate_thenNoExceptionThrown() {
        assertDoesNotThrow(() ->
                validator.validate(1L, new TicketRequest(ADULT, 1)));
    }

    @Test
    @DisplayName("Valid mixed request passes validation")
    void givenValidMixedRequest_whenValidate_thenNoExceptionThrown() {
        assertDoesNotThrow(() ->
                validator.validate(1L,
                        new TicketRequest(ADULT, 2),
                        new TicketRequest(CHILD, 1),
                        new TicketRequest(INFANT, 1)));
    }

    @Test
    @DisplayName("Null account ID fails validation")
    void givenNullAccountId_whenValidate_thenThrows() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> validator.validate(null, new TicketRequest(ADULT, 1)));
        assertEquals("Account ID must be greater than zero", ex.getMessage());
    }

    @Test
    @DisplayName("Zero account ID fails validation")
    void givenZeroAccountId_whenValidate_thenThrows() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> validator.validate(0L, new TicketRequest(ADULT, 1)));
        assertEquals("Account ID must be greater than zero", ex.getMessage());
    }

    @Test
    @DisplayName("Exceeding 25 tickets fails validation")
    void givenMoreThan25Tickets_whenValidate_thenThrows() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> validator.validate(1L, new TicketRequest(ADULT, 26)));
        assertEquals("Cannot purchase more than 25 tickets at a time", ex.getMessage());
    }

    @Test
    @DisplayName("No adult ticket fails validation")
    void givenNoAdultTicket_whenValidate_thenThrows() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> validator.validate(1L, new TicketRequest(CHILD, 2)));
        assertEquals(
            "Child and Infant tickets cannot be purchased without an Adult ticket",
            ex.getMessage());
    }

    @Test
    @DisplayName("More infants than adults fails validation")
    void givenMoreInfantsThanAdults_whenValidate_thenThrows() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> validator.validate(1L,
                        new TicketRequest(ADULT, 1),
                        new TicketRequest(INFANT, 2)));
        assertEquals(
            "Number of Infants cannot exceed number of Adults, as each Infant sits on an Adult lap",
            ex.getMessage());
    }
}