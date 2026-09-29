Feature: Trainee profile management
  As a trainee
  I want to manage my own profile
  So that my information stays accurate, while others cannot manage it for me

  Background:
    Given a trainee "Emma.Trainee" is registered
    And "Emma.Trainee" is authenticated

  @profile @positive
  Scenario: Trainee updates their own profile
    When "Emma.Trainee" updates their own profile with first name "Emmaline"
    Then the response status should be 200

  @profile @negative @permissions
  Scenario: Trainee cannot update another trainee's profile
    Given a trainee "Frank.Trainee" is registered
    When "Emma.Trainee" updates the profile of trainee "Frank.Trainee" with first name "Hacked"
    Then the response status should be 403

  @profile @positive
  Scenario: Admin can deactivate a trainee
    Given an admin user "admin" is authenticated
    When "admin" deactivates trainee "Emma.Trainee"
    Then the response status should be 200

  @profile @negative @edge-case
  Scenario: Deactivating an already inactive trainee fails (non-idempotent action)
    Given an admin user "admin" is authenticated
    When "admin" deactivates trainee "Emma.Trainee"
    Then the response status should be 200
    When "admin" deactivates trainee "Emma.Trainee"
    Then the response status should be 400

  @profile @positive
  Scenario: Admin can hard-delete a trainee profile
    Given an admin user "admin" is authenticated
    When "admin" deletes trainee "Emma.Trainee"
    Then the response status should be 200
