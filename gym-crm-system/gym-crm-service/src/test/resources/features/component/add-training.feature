Feature: Add training
  As a trainer
  I want to record trainings for my trainees
  So that workload and history are tracked correctly

  Background:
    Given a trainee "John.Smith" is registered
    And a trainer "Jane.Doe" is registered
    And "Jane.Doe" is authenticated

  @trainings @positive
  Scenario: Trainer successfully adds a training
    When "Jane.Doe" adds a training for trainee "John.Smith" on "2026-03-15" for 60 minutes
    Then the response status should be 200
    And a workload event should be published for trainer "Jane.Doe"

  @trainings @negative
  Scenario: Adding a training fails when trainee does not exist
    When "Jane.Doe" adds a training for trainee "Ghost.User" on "2026-03-15" for 60 minutes
    Then the response status should be 404

  @trainings @negative @edge-case
  Scenario: Adding a training fails with a negative duration
    When "Jane.Doe" adds a training for trainee "John.Smith" on "2026-03-15" for -10 minutes
    Then the response status should be 400

  @trainings @negative @edge-case
  Scenario: Adding a training fails with a malformed date
    When "Jane.Doe" adds a training for trainee "John.Smith" on "not-a-date" for 60 minutes
    Then the response status should be 400

  @trainings @negative @permissions
  Scenario: Another trainer cannot add a training on someone else's behalf
    Given a trainer "Mark.Other" is registered
    And "Mark.Other" is authenticated
    When "Mark.Other" adds a training as trainer "Jane.Doe" for trainee "John.Smith" on "2026-03-15" for 60 minutes
    Then the response status should be 403
