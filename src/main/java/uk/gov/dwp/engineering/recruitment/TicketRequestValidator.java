package uk.gov.dwp.engineering.recruitment;

import java.util.Arrays;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

public class TicketRequestValidator {

    private static final int MAX_TICKETS = 25;

    public void validate(final Long accountId, final TicketRequest... ticketRequests) {

        if (accountId == null || accountId <= 0) {
            throw new InvalidBookingException("Account ID must be greater than zero");
        }

        if (ticketRequests == null || ticketRequests.length == 0) {
            throw new InvalidBookingException("At least one ticket request must be provided");
        }

        for (TicketRequest request : ticketRequests) {
            if (request == null) {
                throw new InvalidBookingException("Ticket request cannot be null");
            }
            if (request.ticketCount() <= 0) {
                throw new InvalidBookingException("Ticket count must be greater than zero");
            }
        }

        long totalTickets = Arrays.stream(ticketRequests)
                .mapToLong(TicketRequest::ticketCount)
                .sum();

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidBookingException(
                "Cannot purchase more than " + MAX_TICKETS + " tickets at a time"
            );
        }

        boolean hasAdult = Arrays.stream(ticketRequests)
                .anyMatch(r -> r.type() == TicketType.ADULT && r.ticketCount() > 0);

        if (!hasAdult) {
            throw new InvalidBookingException(
                "Child and Infant tickets cannot be purchased without an Adult ticket"
            );
        }

        long adultCount = Arrays.stream(ticketRequests)
                .filter(r -> r.type() == TicketType.ADULT)
                .mapToLong(TicketRequest::ticketCount)
                .sum();

        long infantCount = Arrays.stream(ticketRequests)
                .filter(r -> r.type() == TicketType.INFANT)
                .mapToLong(TicketRequest::ticketCount)
                .sum();

        if (infantCount > adultCount) {
            throw new InvalidBookingException(
                "Number of Infants cannot exceed number of Adults, as each Infant sits on an Adult lap"
            );
        }
    }
}