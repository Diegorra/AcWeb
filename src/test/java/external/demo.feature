Feature: Demo completa de la aplicación

  Scenario: Registrar usuario, loggin, crear analisis y correr test, logout y eliminar cuenta
    #Registramos un nuevo usuario

    #Nos logeamos con el usuario creado
    Given driver baseUrl + '/login'
    And input('#username', 'diego')
    And input('#password', 'aa')
    When submit().click("{button}Sign in")
    Then waitForUrl(baseUrl)

    #Creamos un nuevo análisis

    #Cargamos fuentes

    #Corremos test

    #Hacemos logout

