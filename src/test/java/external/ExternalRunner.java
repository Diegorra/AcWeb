package external;

import com.intuit.karate.junit5.Karate;

class ExternalRunner {

    @Karate.Test
    Karate testLogin() {
        return Karate.run("login").relativeTo(getClass());
    }

    @Karate.Test
    Karate testDemo(){
        return Karate.run("demo").relativeTo(getClass());
    }
}