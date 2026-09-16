Feature: Authentication and authorization
  As the gym CRM system
  I need to enforce authentication and role-based access
  So that only authorized users can access protected data

  Background:
    Given a trainee "Alice.Trainee" is registered
    And a trainer "Bob.Trainer" is registered

  @auth @nfr @positive
  Scenario: Successful login returns a valid JWT token
    Given "Alice.Trainee" is authenticated
    Then the response should contain a valid JWT token

  @auth @nfr @negative
  Scenario: Login fails with wrong password
    When "Alice.Trainee" logs in with password "WrongPassword"
    Then the response status should be 401

  @auth @nfr @negative
  Scenario: Login fails for non-existent user
    When "Ghost.User" logs in with password "anything"
    Then the response status should be 401

  @auth @nfr @negative @edge-case
  Scenario: Account gets locked after 3 failed login attempts
    Given "Alice.Trainee" fails to log in with wrong password 3 times
    When "Alice.Trainee" logs in with password "WrongPassword"
    Then the response status should be 401
    And the response message should contain "locked"

  @auth @nfr @permissions @negative
  Scenario: Trainee cannot access another trainee's profile
    Given a trainee "Carol.Trainee" is registered
    And "Alice.Trainee" is authenticated
    When "Alice.Trainee" requests the profile of trainee "Carol.Trainee"
    Then the response status should be 403

  @auth @nfr @permissions @positive
  Scenario: Admin can access any trainee's profile
    Given an admin user "admin" is authenticated
    When "admin" requests the profile of trainee "Alice.Trainee"
    Then the response status should be 200

  @auth @nfr @negative
  Scenario: Request without a token is rejected
    When an unauthenticated request is made to get profile of trainee "Alice.Trainee"
    Then the response status should be 401
