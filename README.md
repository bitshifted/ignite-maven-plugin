# ignite-maven-plugin
Maven plugin for creating app deployment packages

## Building from source
### Prerequisites

The following is required to build the plugin:
* JDK version 8 or higher
* Maven version 3.X.X

### Building the plugin

Simply run the following command:
```shell
mvn install
```

Plugin will be installed in your private repository.

### Running and testing the plugin

To run the plugin, use the following command:
```shell
mvn co.bitshifted:ignite-maven-plugin:<current-version>:ignite
```

You can also use shorter version:
```shell
mvn install ignite:ignite
```

But, this requires that you add the following to your local `settings.xml` file in the `pluginGroups` section:
```xml
<pluginGroups>
    <pluginGroup>co.bitshifted</pluginGroup>
</pluginGroups>
```
