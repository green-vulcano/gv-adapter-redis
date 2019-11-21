# GreenVulcano VCL Adapter for Redis (Alpha)

This is the implementation of a GreenVulcano VCL Adapter for the Redis database platform. It's meant to run as an Apache Karaf bundle.

## Getting started

### Installation

First, you need to have installed Java Development Kit (JDK) 11 or above.

Then, you need to have installed Apache Karaf 4.2.x. Please refer to the following links for further reference: [Apache Karaf](http://karaf.apache.org/manual/latest/)

Next, you need to install the GreenVulcano engine on the Apache Karaf container. Please refer to [this link](https://greenvulcano.github.io/gv-documentation/pages/installation/Installation/#installation) for further reference.

In order to install the bundle in Apache Karaf to use it for a GreenVulcano application project, you need to install its dependencies. Open the Apache Karaf terminal by running the Karaf executable and type the following command:

```shell
karaf@root()> bundle:install -s mvn:org.apache.commons/commons-pool2/2.6.0
karaf@root()> bundle:install -s -l 82 mvn:redis.clients/jedis/3.1.0
karaf@root()> bundle:install -s -l 92 mvn:it.greenvulcano.gvesb.adapter/gvvcl-redis/4.1.0-SNAPSHOT
```

### Using the VCL adapter in your GreenVulcano project

In order to use the features of the Redis adapter in your GreenVulcano project, you need to define a proper System-Channel-Operation set of nodes. You can do that by manually editing the GVCore.xml file, or by using DeveloperStudio. In that case, you will have to download the dtds/ folder on this repository and replace it with the one in your current project.

### Declaring the System-Channel-Operation for the Redis database

Let's assume you want to interact with your database in "localhost". By default, that's gonna be at ```127.0.0.1:6379```.
Insert the ```<redis-call>``` XML node in the ```<Systems></Systems>``` section of file ```GVCore.xml```. Here's an example:

```xml
<System id-system="MySystem" system-activation="on">
    <Channel enabled="true" endpoint="redis://localhost:6379" id-channel="redis-channel" type="RedisAdapter">
    	<redis-call name="aRedisCallOp" type="call">
      	 <set key="test1" value="test1value"/>
           <get key="test1"/>
      </redis-call>
	</Channel>
</System>
```

Some constraints apply to these XML nodes.

- The ```<Channel>``` XML node must comply with the following syntax:
    - ```endpoint``` must contain a URI string correctly referencing the hostname and the port of an operational Redis server.
- The ```<redis-call>``` XML node must comply with the following syntax:
    - ```type``` must be declared and set equal to ```"call"```;
    - ```name``` must be declared: it defines the name of the Operation node;
    - ```uri``` optional: if you want to specify a Redis connection different from the channel endpoint;

- The ```redis-call``` node supports four types of operation:
    - ```get```  to retrieve data from Redis;
    - ```set``` to put into Redis the GVBuffer data as value for the specified key;
    - ```delete``` to delete the specified key;
    - ```keys``` to retrieve the keys matching the specified pattern;

When we're done defining our System node, we can now use it in a Service-Operation, such as:

```xml
<Services>
            <Description>This section contains a list of all services provided by GreenVulcano ESB</Description>
            <Service group-name="DEFAULT_GRP" id-service="testService"
                     service-activation="on" statistics="off">
                <Operation class="it.greenvulcano.gvesb.core.flow.GVFlowWF"
                           loggerLevel="ALL" name="testOperation"
                           operation-activation="on" type="operation">
                    <Flow first-node="RedisCall">
                        <GVOperationNode class="it.greenvulcano.gvesb.core.flow.GVOperationNode"
                                         id="RedisCall" id-channel="redis-channel"
                                         id-system="REDIS" input="input"
                                         next-node-id="terminated" op-type="call"
                                         operation-name="ciao" point-x="231" point-y="150"
                                         type="flow-node"/>
                        <GVEndNode class="it.greenvulcano.gvesb.core.flow.GVEndNode"
                                   id="terminated" op-type="end" output="input"
                                   point-x="435" point-y="150" type="flow-node"/>
                    </Flow>
                </Operation>
            </Service>
</Services>
```
