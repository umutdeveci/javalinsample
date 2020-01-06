# javalinsample

A simple API that provides account information, withdrawal/deposit operations and transfers between accounts. 
For simplicity sake, all accounts considered to have same currency.

Requirements: Maven and at least Java 11.

Run `./run.sh` to build and run the project.
Or just use `mvn clean verify` to build the project. The executable jar is named 
`javalinsample-1.0-jar-with-dependencies.jar`. You can run the jar with 
`java -jar target/javalinsample-1.0-jar-with-dependencies.jar`

NOTE: The tests sometimes fail because of a bug with mockito not being able to differentiate between `Context.pathParam`
overloads, which one of them has signature of `String Context.pathParam(String)` and the other one having 
`Validator Context.pathParam(String, Class)` signature. But for some reason, in random situations mockito thinks second 
method is the method to be invoked instead, which returns a `Validator`, not `String`, so throws a 
`org.mockito.exceptions.misusing.WrongTypeOfReturnValue` exception. If the tests fail with this error, just run again.