Feature: Workload message processing
  As the report service
  I need to correctly process incoming workload events, including invalid or concurrent ones
  So that trainer summaries stay accurate and the system never crashes on bad input

  @messaging @positive
  Scenario: A valid ADD event increases the monthly summary
    Given a workload event is published for trainer "Alice.Coach" on "2026-05-10" for 45 minutes with action "ADD"
    Then within 5 seconds the summary should contain 45 minutes for year 2026 month 5

  @messaging @positive @edge-case
  Scenario: A DELETE event decreases the monthly summary but never goes below zero
    Given a workload event is published for trainer "Bob.Coach" on "2026-06-01" for 30 minutes with action "ADD"
    And a workload event is published for trainer "Bob.Coach" on "2026-06-05" for 60 minutes with action "DELETE"
    Then within 5 seconds the summary should contain 0 minutes for year 2026 month 6

  @messaging @negative
  Scenario: An invalid message (blank trainer username) is routed to the dead letter queue
    When an invalid workload event with a blank trainer username is published
    Then within 5 seconds the message should be routed to the dead letter queue

  @messaging @concurrency @edge-case
  Scenario: Concurrent events for the same trainer accumulate correctly without lost updates
    When 5 workload events are published concurrently for trainer "Kate.Coach" on "2026-07-01" for 20 minutes each with action "ADD"
    Then within 5 seconds the summary should contain 100 minutes for year 2026 month 7
