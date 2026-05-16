Feature: Cinema ticket purchasing
  As a cinema customer
  I want to purchase tickets
  So that I can watch a film with my family

  Background:
    Given I have a valid account with id 1

  Scenario: Adult purchases tickets successfully
    When I request 2 ADULT tickets
    Then payment of 50 is taken
    And 2 seats are reserved
    And the booking is confirmed

  Scenario: Adult and child purchase tickets together
    When I request 2 ADULT and 1 CHILD tickets
    Then payment of 65 is taken
    And 3 seats are reserved
    And the booking is confirmed

  Scenario: Infant pays nothing and gets no seat
    When I request 2 ADULT and 1 CHILD and 1 INFANT tickets
    Then payment of 65 is taken
    And 3 seats are reserved
    And the booking is confirmed

  Scenario: Cannot purchase more than 25 tickets
    When I request 26 ADULT tickets
    Then an invalid booking error is thrown with message "Cannot purchase more than 25 tickets at a time"

  Scenario: Cannot purchase child ticket without an adult
    When I request 2 CHILD tickets without an adult
    Then an invalid booking error is thrown with message "Child and Infant tickets cannot be purchased without an Adult ticket"

  Scenario: Cannot purchase infant ticket without an adult
    When I request 1 INFANT ticket without an adult
    Then an invalid booking error is thrown with message "Child and Infant tickets cannot be purchased without an Adult ticket"

  Scenario: Cannot purchase with invalid account id
    Given I have an invalid account with id 0
    When I request 1 ADULT ticket with invalid account
    Then an invalid booking error is thrown with message "Account ID must be greater than zero"

  Scenario: Infants cannot exceed number of adults
    When I request 1 ADULT and 2 INFANT tickets
    Then an invalid booking error is thrown with message "Number of Infants cannot exceed number of Adults, as each Infant sits on an Adult lap"