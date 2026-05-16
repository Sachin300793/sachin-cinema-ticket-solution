package uk.gov.dwp.engineering.recruitment.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import uk.gov.dwp.engineering.recruitment.CinemaTicketsServiceImpl;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

public class TicketStepDefs {

    // Cucumber creates a new instance of TicketStepDefs for each scenario,
    // so initialising here is equivalent to @Before — no annotation needed
    private final PaymentService paymentService = mock(PaymentService.class);
    private final SeatReservationService seatReservationService = mock(SeatReservationService.class);
    private final CinemaTicketsServiceImpl service =
            new CinemaTicketsServiceImpl(paymentService, seatReservationService);

    private Long accountId;
    private String bookingResult;
    private InvalidBookingException thrownException;

    // -----------------------------------------------------------------------
    // Given
    // -----------------------------------------------------------------------

    @Given("I have a valid account with id {long}")
    public void iHaveAValidAccountWithId(long id) {
        this.accountId = id;
    }

    @Given("I have an invalid account with id {long}")
    public void iHaveAnInvalidAccountWithId(long id) {
        this.accountId = id;
    }

    // -----------------------------------------------------------------------
    // When
    // -----------------------------------------------------------------------

    @When("I request {int} ADULT tickets")
    public void iRequestAdultTickets(int count) {
        if (count <= 25) {
            stubPaymentAndSeats(accountId,
                    new BigDecimal(count * 25), (long) count);
        }
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(ADULT, count));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} ADULT and {int} CHILD tickets")
    public void iRequestAdultAndChildTickets(int adults, int children) {
        int amount = (adults * 25) + (children * 15);
        long seats = adults + children;
        stubPaymentAndSeats(accountId, new BigDecimal(amount), seats);
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(ADULT, adults),
                    new TicketRequest(CHILD, children));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} ADULT and {int} CHILD and {int} INFANT tickets")
    public void iRequestAdultChildAndInfantTickets(int adults, int children, int infants) {
        int amount = (adults * 25) + (children * 15);
        long seats = adults + children; // infants get no seat
        stubPaymentAndSeats(accountId, new BigDecimal(amount), seats);
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(ADULT, adults),
                    new TicketRequest(CHILD, children),
                    new TicketRequest(INFANT, infants));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} ADULT and {int} INFANT tickets")
    public void iRequestAdultAndInfantTickets(int adults, int infants) {
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(ADULT, adults),
                    new TicketRequest(INFANT, infants));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} CHILD tickets without an adult")
    public void iRequestChildTicketsWithoutAnAdult(int count) {
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(CHILD, count));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} INFANT ticket without an adult")
    public void iRequestInfantTicketWithoutAnAdult(int count) {
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(INFANT, count));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    @When("I request {int} ADULT ticket with invalid account")
    public void iRequestAdultTicketWithInvalidAccount(int count) {
        try {
            bookingResult = service.purchaseTickets(accountId,
                    new TicketRequest(ADULT, count));
        } catch (InvalidBookingException e) {
            thrownException = e;
        }
    }

    // -----------------------------------------------------------------------
    // Then
    // -----------------------------------------------------------------------

    @Then("payment of {int} is taken")
    public void paymentIsTaken(int amount) {
        verify(paymentService).debitAccount(accountId, new BigDecimal(amount));
    }

    @And("{int} seats are reserved")
    public void seatsAreReserved(int seats) {
        verify(seatReservationService).reserveSeats(accountId, (long) seats);
    }

    @And("the booking is confirmed")
    public void theBookingIsConfirmed() {
        assertNotNull(bookingResult);
        assertNull(thrownException);
    }

    @Then("an invalid booking error is thrown with message {string}")
    public void anInvalidBookingErrorIsThrownWithMessage(String expectedMessage) {
        assertNotNull(thrownException,
                "Expected InvalidBookingException but none was thrown");
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private void stubPaymentAndSeats(Long accountId, BigDecimal amount, long seats) {
        when(paymentService.debitAccount(accountId, amount))
                .thenReturn(ResponseEntity.ok("OK"));
        when(seatReservationService.reserveSeats(accountId, seats))
                .thenReturn(ResponseEntity.ok("OK"));
    }
}