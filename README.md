# Seamless Talk (RabbitMQ)

Seamless Talk (RabbitMQ) is a java library for easy and fast developing microservices' communication on the RabbitMQ bus. It wraps
[Spring AMQP](https://spring.io/projects/spring-amqp) mechanics by some automation, making it easier to develop software and waste
less time to deal with RabbitMQ.

Main features:

* queues and exchanges auto-generation and binding, based on your configuration and namings
* java API code generation to communicate between services
* seamless exceptions transferring by RabbitMQ

## Step-by-step guide

This step-by-step guide will quickly get you started on SeamlessTalk (RabbitMQ) basics.

### 0. Preparations

You need to know some basics about this library:

* `Contract` - is an interface marked with @SeamlessTalkRabbitContract annotation
* `Listener` - is a class implementing the contract and marked with @SeamlessTalkRabbitListener, where every interface's overridden method
  must be marked with @RabbitHandler.
* `Generated API` - is a class that is auto-generated by the library, implementing the same contract
* `DTO (Data transfer object)` - a common class for data transferring between modules. I hope you knew it, but still, I have to name it.

Also, you need to have a RabbitMQ instance to connect. For example, you can up a ready docker image. Default settings will be enough for 
this guide, so just use this command below:

``` docker run --name rabbit-management -p 15672:15672 -p 5672:5672 rabbitmq:3-management ```

More information can find at [RabbitMQ DockerHub](https://hub.docker.com/_/rabbitmq)  page.

### 1. Project structure

You need to create a shared library for all your services. It will contain common objects (ex. contracts and DTOs). It needs to exist
because all data will be serialized and deserialized using the full class name, and it shall be the same everywhere.

In this example, it will be called `shared-lib`. Also let's create two microservice modules: `library-api`, and `repository-manager`. The
first one will implement REST controllers to send requests by RabbitMQ to the second one.

Also, you need to define RabbitMQ connection parameters in every microservice's properties. The most common setup will be:

```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

More information can find at [Spring AMQP](https://spring.io/projects/spring-amqp) docs.

### 2. Contracts creation

When the modules have been created, you need to define your contracts. There are a few steps to take it:

* create an interface with the name of the contract. Usually, it ends with '*Contract'. Ex. LibrarySearchContract.
* mark it with @SeamlessTalkRabbitContract and set desired annotation arguments, ex. exchangeType (direct or fanout).
* define contract methods inside. Method parameter means incoming payload type and returns value - outgoing.

#### Important note:

* Due to implementation features of `AMQP`, there is a single payload in the message. So, you can put only one argument to contract methods. So,
  it requires creating a DTO with all arguments you need inside.

For example, let's create a contract `LibraryIOContract` with two methods:

* FileDto get(String path)
* boolean save(FileDto request)

You will have such code:

```java
@SeamlessTalkRabbitContract
public interface LibraryIOContract {

    FileDto get(String path);

    boolean save(FileDto request);
}
```

Where `FileDto` is a common class defined in `share-lib:

```java
@Data
@NoArgsConstructor
@SuperBuilder
public class FileDto {

    private byte[] data;
    private String path;
}
```

I hope you don't have anything against `Lombok` :)

Now you can run the `build` task and see how will be auto-generated API class implementing this contract. Like this:

```java
  @Service
  @Primary
  public class LibraryIOContractRabbitApi extends AbstractRabbitApi implements LibraryIOContract {
  
      private AmqpTemplate amqpTemplate;
  
      private Binding binding;
  
      public LibraryIOContractRabbitApi(AmqpTemplate amqpTemplate, RoutesGenerator routesGenerator) {
          this.amqpTemplate = amqpTemplate;
          this.binding = routesGenerator.getBinding(LibraryIOContract.class);
      }
  
      @Override
      public FileDto get(String payload) {
          return convertSendAndReceive(amqpTemplate, binding.getExchange(), binding.getRoutingKey(), payload);
      }
  
      @Override
      public boolean save(FileDto payload) {
          return convertSendAndReceive(amqpTemplate, binding.getExchange(), binding.getRoutingKey(), payload);
      }
  }
```

The mark that you set up all correctly will be in build logs:

```
  > Task :examples-2-ms-with-shared-lib-shared-lib:compileJava
  Note: Started Seamless Talk rabbit api generation for found contracts: 
    LibraryIOContract
```

Now all preparations in the shared library have been done. Let's go to the microservices.

### 3. Listener creation

You need to create a class implementing the desired contract and mark it with @SeamlessTalkRabbitListener.

Let's create a listener inside the `repository-manager` module. Like this:

```java
@SeamlessTalkRabbitListener
public class LibraryIOListener implements LibraryIOContract {

    @Override
    @RabbitHandler
    public FileDto get(String filePath) {
        return FileDto.builder()
                      .data("answer".getBytes())
                      .path("anotherSomePath")
                      .build();
    }

    @Override
    @RabbitHandler
    public boolean save(FileDto request) {
        throw new NotImplementedException();
    }
}
```

That's all you need to do. Let's try to use it,

### 4. Generated API usage

Let's go to the `library-api` module and create a REST-controller with an autowired `LibrarySearchContract` instance.

```java
@RestController
@RequestMapping("library")
public class LibraryController {

    @Autowired
    private LibrarySearchContract librarySearchContract;

    ...

}
```

After it let's add some methods inside to call our contract functionality:

```java
...
@RequestMapping("/get")
FileDto get(){
    return libraryIOContract.get("somePath");
}

@RequestMapping(path = "/save")
boolean save() {
        return libraryIOContract.save(FileDto.builder()
                    .data("someData".getBytes())
                    .path("somePath")
                    .build());
}
...
```

Let's `run` both microservices and request the `get` REST method.

``` curl -X GET http://localhost:9002/library/get ```

If it returns such response, then you made all correctly, and your
the connection between microservices set up.

```
HTTP/1.1 200
connection: keep-alive
content-type: application/json
date: Tue, 22 Mar 2022 08:37:11 GMT
keep-alive: timeout=60
transfer-encoding: chunked

{"data":"YW5zd2Vy","path":"anotherSomePath"}
```

Also, you can request the `save` REST method and get an error.

```curl -X POST http://localhost:9002/library/save```

```
HTTP/1.1 500 
connection: close
content-length: 0
date: Tue, 22 Mar 2022 08:38:06 GMT
```

Let's look up at logs in `library-api` and see that the `NotImplementedException` thrown in `repository-manager` was
fully transferred to `library-api` module and re-thrown there as usual exception:

```
...
com.coddweaver.seamless.talk.rabbit.exceptions.NotImplementedException: null
        at com.coddweaver.seamless.talk.rabbit.repositorymanager.listeners.LibraryIOListener.get(LibraryIOListener.java:22) ~[main/:na]
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:na]
...
```

### Conclusion

That's all you need to know to create a simple connection between microservices using `SeamlessTalk (RabbitMQ)`. More complex examples you can
find in [examples](https://github.com/coddweaver/seamless-talk-rabbit/tree/main/examples) folder.




