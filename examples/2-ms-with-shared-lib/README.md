## Two microservices with shared library example

Here you can find a common project with 2 microservices connected using Seamless Talk (RabbitMQ) library.

### Some notes

* `library-api` module has configured Swagger UI for testing. You can access it
  using [this link](http://localhost:9002/swagger-ui/index.html)
* check application.yml properties in every module and setup it according to your RabbitMQ credentials
* go into the `shared-lib` module to `api.contracts.FanoutTestContract` interface and look up at `@SeamlessTalkRabbitContract` parameters.
  It is an example of setting up `FANOUT` exchange using this library.
* refers to the previous point, also, you can discover all another possible parameters of `@SeamlessTalkRabbitContract` to know all it's
  features