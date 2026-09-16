Feature: Workload synchronization between services
  As the system
  I need trainer workload to sync from gym-crm-service to report-service
  So that monthly summaries stay accurate

  @integration @workload-sync @positive
  Scenario: Adding a training eventually updates the trainer's monthly summary
    Given a trainer "Jane.IntegDoe" is registered
    And a trainee "John.IntegSmith" is registered
    When "Jane.IntegDoe" adds a training for trainee "John.IntegSmith" on "2026-03-15" for 60 minutes
    Then the response status should be 200
    And within 10 seconds the report service should show 60 minutes for "Jane.IntegDoe" in year 2026 month 3

  @integration @workload-sync @edge-case
  Scenario: Two trainings in the same month accumulate correctly
    Given a trainer "Kate.IntegTrainer" is registered
    And a trainee "Mia.IntegTrainee" is registered
    When "Kate.IntegTrainer" adds a training for trainee "Mia.IntegTrainee" on "2026-04-01" for 30 minutes
    And "Kate.IntegTrainer" adds a training for trainee "Mia.IntegTrainee" on "2026-04-15" for 45 minutes
    Then within 10 seconds the report service should show 75 minutes for "Kate.IntegTrainer" in year 2026 month 4
