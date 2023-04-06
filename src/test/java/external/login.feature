Feature: login en servidor

  """
  Scenario: login malo en plantilla
    Given driver baseUrl + '/'
    And input('#username', 'dummy')
    And input('#password', 'world')
    When submit().click(".form-signin button")
    Then match html('.error') contains 'Error en nombre de usuario o contraseña'

  @login_b
  Scenario: login correcto como b
    Given driver baseUrl + '/login'
    And input('#username', 'b')
    And input('#password', 'aa')
    When submit().click(".form-signin button")
    Then waitForUrl(baseUrl + '/')

  @login_a
  Scenario: login correcto como a
    Given driver baseUrl + '/login'
    And input('#username', 'a')
    And input('#password', 'aa')
    When submit().click(".form-signin button")
    Then waitForUrl(baseUrl + '/')

  Scenario: logout after login
    Given driver baseUrl + '/login'
    And input('#username', 'a')
    And input('#password', 'aa')
    When submit().click(".form-signin button")
    Then waitForUrl(baseUrl + '/')
    When submit().click("{button}logout")
    Then waitForUrl(baseUrl + '/login')

  Scenario: login malo
    Given driver baseUrl + '/login'
    And input('#username', 'hello')
    And input('#password', 'world')
    When submit().click('{button}Sign in')
    Then match html('.error') contains 'Error en nombre de usuario o contraseña'
"""
