Feature: login en servidor

  """
  Scenario: login malo
    Given driver baseUrl + '/login'
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
    When click("{a}Log out")
    Then waitForUrl(baseUrl + '/login')

"""
