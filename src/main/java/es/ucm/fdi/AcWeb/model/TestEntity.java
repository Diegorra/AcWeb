package es.ucm.fdi.AcWeb.model;



//Clase que mapea un test, no es persistida en BD
public class TestEntity {

    private String testKey;

    private String[] requires = new String[0];

    private String[] provides = new String[0];

    private boolean independentPreprocessing;
    private boolean independentSimilarity;

    private boolean testCanceled;
    private float progress;
}
