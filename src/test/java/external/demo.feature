Feature: Demo completa de la aplicación

  Scenario: Registrar usuario, loggin, crear analisis y correr test, logout y eliminar cuenta
    #Registramos un nuevo usuario
    Given driver baseUrl + '/register'
    And input('#username', 'diego')
    And input('#password', 'aa')
    When submit().click("{button}")
    Then waitForUrl(baseUrl + '/login')

    #Nos logeamos con el usuario creado
    Given driver baseUrl + '/login'
    And input('#username', 'diego')
    And input('#password', 'aa')
    When submit().click("{button}Sign in")
    Then waitForUrl(baseUrl)

    #Creamos un nuevo análisis
    Given driver baseUrl
    When click("{a}New Analysis")
    When method get
    Then status 200
    Then waitForUrl(baseUrl + '/analysis')

    #Cargamos fuentes
    Given driver baseUrl + '/analysis'
    And multipart file myFile = { read: 'sample_aa.zip',filename: 'sample_aa.zip', contentType: 'multipart/form-data'  }
    When submit().click("{button}Upload sources")
    When method post
    Then status 200

    #Corremos test
    Given driver baseUrl + '/analysis/975/Zip_ncd_sim'
    When click("{a}NCD sim")
    When method get
    Then status 200

    #Hacemos logout
    When click("{a}Log out")
    Then waitForUrl(baseUrl + '/login')
