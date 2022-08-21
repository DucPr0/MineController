# MineController
A lightweight REST API framework for Java designed to match ASP.NET's syntax.

# What is MineController?
MineController is an attempt at bringing REST Controllers' syntax from ASP.NET over to Java and Kotlin. Simply define your controller functions with the appropriate http requests and routing, and the framework will take care of the rest.

Currently, MineController uses Jetty for the http server. Future plans may include moving to more lightweight web servers.

# How do I use this?
To start making controllers, simply define a class that inherits from BaseController:
```
@RoutePrefix(route = "/sum")
public class SumController extends BaseController {
    @HttpGet(route = "{b}/calculate")
    public BaseResponse getSum(@FromQuery(name = "a") int a, @FromPath(name = "b") int b) {
        return this.createOkObjectResponse(a + b);
    }
}
```
Now, a query to the endpoint /sum/3/calculate?a=5 will return the sum 3 + 5 = 8. @FromBody is also supported, although at most one can be present.

**Important**: The route prefix must be prefixed with "/", and methods' route must not contain "/" at the start. Both types of routes also mustn't have "/" at the end.

Now, to create a server:
```
var server = new HttpServer(port);
var controller = new SumController();
server.mapControllers(Arrays.asList(controller));
server.start();
```

**Notes**:

The "name" for annotations might be clunky, but it is currently impossible to determine parameter names in Java at runtime without the -parameters compile flag, which this framework has no control over.

# Why is it named MINEController?
Plans are to integrate this REST framework with the Spigot API in the future.
