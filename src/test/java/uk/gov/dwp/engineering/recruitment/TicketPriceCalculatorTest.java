package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;

class TicketPriceCalculatorTest {

    private TicketPriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TicketPriceCalculator();
    }

    @Test
    @DisplayName("2 adults = £50")
    void givenTwoAdults_whenCalculateAmount_thenFiftyPounds() {
        BigDecimal result = calculator.calculateTotalAmount(new TicketRequest(ADULT, 2));
        assertEquals(new BigDecimal("50"), result);
    }

    @Test
    @DisplayName("1 adult + 1 child = £40")
    void givenOneAdultOneChild_whenCalculateAmount_thenFortyPounds() {
        BigDecimal result = calculator.calculateTotalAmount(
                new TicketRequest(ADULT, 1),
                new TicketRequest(CHILD, 1));
        assertEquals(new BigDecimal("40"), result);
    }

    @Test
    @DisplayName("Infant contributes £0 to total amount")
    void givenInfant_whenCalculateAmount_thenInfantFree() {
        BigDecimal result = calculator.calculateTotalAmount(
                new TicketRequest(ADULT, 1),
                new TicketRequest(INFANT, 1));
        assertEquals(new BigDecimal("25"), result);
    }

    @Test
    @DisplayName("2 adults + 1 child + 1 infant = £65")
    void givenMixedTickets_whenCalculateAmount_thenSixtyFivePounds() {
        BigDecimal result = calculator.calculateTotalAmount(
                new TicketRequest(ADULT, 2),
                new TicketRequest(CHILD, 1),
                new TicketRequest(INFANT, 1));
        assertEquals(new BigDecimal("65"), result);
    }

    @Test
    @DisplayName("Infants are not allocated a seat")
    void givenAdultAndInfant_whenCalculateSeats_thenInfantExcluded() {
        long seats = calculator.calculateTotalSeats(
                new TicketRequest(ADULT, 2),
                new TicketRequest(INFANT, 2));
        assertEquals(2L, seats);
    }

    @Test
    @DisplayName("Adults and children both get seats")
    void givenAdultAndChild_whenCalculateSeats_thenBothAllocatedSeats() {
        long seats = calculator.calculateTotalSeats(
                new TicketRequest(ADULT, 2),
                new TicketRequest(CHILD, 3));
        assertEquals(5L, seats);
    }

    @Test
    @DisplayName("25 adults = £625 and 25 seats")
    void givenMaxAdults_whenCalculate_thenCorrectAmountAndSeats() {
        BigDecimal amount = calculator.calculateTotalAmount(new TicketRequest(ADULT, 25));
        long seats = calculator.calculateTotalSeats(new TicketRequest(ADULT, 25));
        assertEquals(new BigDecimal("625"), amount);
        assertEquals(25L, seats);
    }
}