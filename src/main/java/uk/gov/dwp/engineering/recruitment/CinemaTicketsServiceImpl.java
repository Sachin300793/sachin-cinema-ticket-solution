package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private final PaymentService paymentService;
  private final SeatReservationService seatReservationService;
  private final TicketRequestValidator validator;
  private final TicketPriceCalculator calculator;

  public CinemaTicketsServiceImpl(PaymentService paymentService,
                                  SeatReservationService seatReservationService) {
    this.paymentService = paymentService;
    this.seatReservationService = seatReservationService;
    this.validator = new TicketRequestValidator();
    this.calculator = new TicketPriceCalculator();
  }

  @Override
  public String purchaseTickets(final Long accountId, final TicketRequest... ticketRequests)
          throws InvalidBookingException {

    // Step 1: validate — throws InvalidBookingException if anything is wrong
    validator.validate(accountId, ticketRequests);

    // Step 2: calculate total amount and seats
    BigDecimal totalAmount = calculator.calculateTotalAmount(ticketRequests);
    long totalSeats = calculator.calculateTotalSeats(ticketRequests);

    // Step 3: take payment
    paymentService.debitAccount(accountId, totalAmount);

    // Step 4: reserve seats
    seatReservationService.reserveSeats(accountId, totalSeats);

    return String.format(
            "Booking confirmed: %d seat(s) reserved, £%s charged to account %d",
            totalSeats, totalAmount.toPlainString(), accountId
    );
  }
}