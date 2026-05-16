package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import java.util.Arrays;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;

public class TicketPriceCalculator {

    private static final BigDecimal ADULT_PRICE = new BigDecimal("25");
    private static final BigDecimal CHILD_PRICE = new BigDecimal("15");
    private static final BigDecimal INFANT_PRICE = BigDecimal.ZERO;

    public BigDecimal calculateTotalAmount(final TicketRequest... ticketRequests) {
        return Arrays.stream(ticketRequests)
                .map(r -> priceFor(r.type()).multiply(BigDecimal.valueOf(r.ticketCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long calculateTotalSeats(final TicketRequest... ticketRequests) {
        // Infants do not get a seat — they sit on an Adult's lap
        return Arrays.stream(ticketRequests)
                .filter(r -> r.type() != TicketType.INFANT)
                .mapToLong(TicketRequest::ticketCount)
                .sum();
    }

    private BigDecimal priceFor(final TicketType type) {
        return switch (type) {
            case ADULT  -> ADULT_PRICE;
            case CHILD  -> CHILD_PRICE;
            case INFANT -> INFANT_PRICE;
        };
    }
}