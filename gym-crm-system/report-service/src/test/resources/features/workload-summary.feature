Feature: Trainer workload summary
  As a client of the report service
  I want to retrieve a trainer's monthly training summary
  So that I can see accumulated workload

  @summary @positive
  Scenario: Summary is returned after a workload event is processed
    Given a workload event is published for trainer "Jane.Doe" on "2026-03-15" for 60 minutes with action "ADD"
    Then within 5 seconds the summary should contain 60 minutes for year 2026 month 3

  @summary @negative
  Scenario: Requesting summary for unknown trainer returns 404
    When the summary for trainer "Ghost.Trainer" is requested
    Then the response status should be 404
