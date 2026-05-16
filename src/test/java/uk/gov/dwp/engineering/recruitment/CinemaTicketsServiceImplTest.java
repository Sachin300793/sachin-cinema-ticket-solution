package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@ExtendWith(MockitoExtension.class)
class CinemaTicketsServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private CinemaTicketsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CinemaTicketsServiceImpl(paymentService, seatReservationService);
    }

    // -----------------------------------------------------------------------
    // Happy path tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Adult only: correct payment and seat reservation made")
    void givenAdultTicketsOnly_whenPurchase_thenCorrectPaymentAndSeatsReserved() {
        when(paymentService.debitAccount(1L, new BigDecimal("50")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 2L))
                .thenReturn(ResponseEntity.ok("OK"));

        String result = service.purchaseTickets(1L,
                new TicketRequest(ADULT, 2));

        verify(paymentService).debitAccount(1L, new BigDecimal("50"));
        verify(seatReservationService).reserveSeats(1L, 2L);
        assertEquals("Booking confirmed: 2 seat(s) reserved, £50 charged to account 1", result);
    }

    @Test
    @DisplayName("Adult and Child: correct payment of £65 and 3 seats reserved")
    void givenAdultAndChildTickets_whenPurchase_thenCorrectPaymentAndSeatsReserved() {
        when(paymentService.debitAccount(1L, new BigDecimal("65")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 3L))
                .thenReturn(ResponseEntity.ok("OK"));

        String result = service.purchaseTickets(1L,
                new TicketRequest(ADULT, 2),
                new TicketRequest(CHILD, 1));

        verify(paymentService).debitAccount(1L, new BigDecimal("65"));
        verify(seatReservationService).reserveSeats(1L, 3L);
        assertEquals("Booking confirmed: 3 seat(s) reserved, £65 charged to account 1", result);
    }

    @Test
    @DisplayName("Adult, Child and Infant: infant pays nothing and gets no seat")
    void givenAdultChildAndInfantTickets_whenPurchase_thenInfantFreeAndNoSeat() {
        when(paymentService.debitAccount(1L, new BigDecimal("65")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 3L))
                .thenReturn(ResponseEntity.ok("OK"));

        String result = service.purchaseTickets(1L,
                new TicketRequest(ADULT, 2),
                new TicketRequest(CHILD, 1),
                new TicketRequest(INFANT, 1));

        // Infant adds £0 to payment and 0 to seat count
        verify(paymentService).debitAccount(1L, new BigDecimal("65"));
        verify(seatReservationService).reserveSeats(1L, 3L);
        assertEquals("Booking confirmed: 3 seat(s) reserved, £65 charged to account 1", result);
    }

    @Test
    @DisplayName("Maximum 25 tickets purchased successfully")
    void givenMaxTickets_whenPurchase_thenSucceeds() {
        when(paymentService.debitAccount(1L, new BigDecimal("625")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 25L))
                .thenReturn(ResponseEntity.ok("OK"));

        service.purchaseTickets(1L, new TicketRequest(ADULT, 25));

        verify(paymentService).debitAccount(1L, new BigDecimal("625"));
        verify(seatReservationService).reserveSeats(1L, 25L);
    }

    @Test
    @DisplayName("Single adult ticket: £25 charged and 1 seat reserved")
    void givenSingleAdultTicket_whenPurchase_thenCorrectPaymentAndOneSeatReserved() {
        when(paymentService.debitAccount(1L, new BigDecimal("25")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 1L))
                .thenReturn(ResponseEntity.ok("OK"));

        service.purchaseTickets(1L, new TicketRequest(ADULT, 1));

        verify(paymentService).debitAccount(1L, new BigDecimal("25"));
        verify(seatReservationService).reserveSeats(1L, 1L);
    }

    // -----------------------------------------------------------------------
    // Validation: account ID
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Null account ID throws InvalidBookingException")
    void givenNullAccountId_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(null, new TicketRequest(ADULT, 1)));

        assertEquals("Account ID must be greater than zero", ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Zero account ID throws InvalidBookingException")
    void givenZeroAccountId_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(0L, new TicketRequest(ADULT, 1)));

        assertEquals("Account ID must be greater than zero", ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Negative account ID throws InvalidBookingException")
    void givenNegativeAccountId_whenPurchase_thenThrowsInvalidBookingException() {
        assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(-1L, new TicketRequest(ADULT, 1)));

        verifyNoInteractions(paymentService, seatReservationService);
    }

    // -----------------------------------------------------------------------
    // Validation: ticket requests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Null ticket requests throws InvalidBookingException")
    void givenNullTicketRequests_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L, (TicketRequest[]) null));

        assertEquals("At least one ticket request must be provided", ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Empty ticket requests throws InvalidBookingException")
    void givenEmptyTicketRequests_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L));

        assertEquals("At least one ticket request must be provided", ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("More than 25 tickets throws InvalidBookingException")
    void givenMoreThan25Tickets_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L, new TicketRequest(ADULT, 26)));

        assertEquals("Cannot purchase more than 25 tickets at a time", ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("26 tickets across types throws InvalidBookingException")
    void givenMoreThan25TicketsAcrossTypes_whenPurchase_thenThrowsInvalidBookingException() {
        assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L,
                        new TicketRequest(ADULT, 20),
                        new TicketRequest(CHILD, 6)));

        verifyNoInteractions(paymentService, seatReservationService);
    }

    // -----------------------------------------------------------------------
    // Validation: adult required
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Child ticket without Adult throws InvalidBookingException")
    void givenChildTicketWithoutAdult_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L, new TicketRequest(CHILD, 1)));

        assertEquals(
                "Child and Infant tickets cannot be purchased without an Adult ticket",
                ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Infant ticket without Adult throws InvalidBookingException")
    void givenInfantTicketWithoutAdult_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L, new TicketRequest(INFANT, 1)));

        assertEquals(
                "Child and Infant tickets cannot be purchased without an Adult ticket",
                ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Child and Infant without Adult throws InvalidBookingException")
    void givenChildAndInfantWithoutAdult_whenPurchase_thenThrowsInvalidBookingException() {
        assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L,
                        new TicketRequest(CHILD, 2),
                        new TicketRequest(INFANT, 1)));

        verifyNoInteractions(paymentService, seatReservationService);
    }

    // -----------------------------------------------------------------------
    // Validation: infants cannot exceed adults
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("More infants than adults throws InvalidBookingException")
    void givenMoreInfantsThanAdults_whenPurchase_thenThrowsInvalidBookingException() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.purchaseTickets(1L,
                        new TicketRequest(ADULT, 1),
                        new TicketRequest(INFANT, 2)));

        assertEquals(
                "Number of Infants cannot exceed number of Adults, as each Infant sits on an Adult lap",
                ex.getMessage());
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Equal infants and adults is valid")
    void givenEqualInfantsAndAdults_whenPurchase_thenSucceeds() {
        when(paymentService.debitAccount(1L, new BigDecimal("50")))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(1L, 2L))
                .thenReturn(ResponseEntity.ok("OK"));

        // 2 adults, 2 infants — valid, infants sit on adult laps
        service.purchaseTickets(1L,
                new TicketRequest(ADULT, 2),
                new TicketRequest(INFANT, 2));

        verify(paymentService).debitAccount(1L, new BigDecimal("50"));
        verify(seatReservationService).reserveSeats(1L, 2L);
    }
}