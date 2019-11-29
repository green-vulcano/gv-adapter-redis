package it.greenvulcano.gvesb.virtual.redis.operation;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import redis.clients.jedis.Jedis;


public class RedisOperationHget extends RedisOperation {

    public static final String TYPE = "hget";
    
    private final String field;
    
    static {
        RedisOperation.registerBuilder(TYPE, new Builder());
    }
    
    public RedisOperationHget(String operationType, String field) {

        super(operationType);
        this.field = field;
    }

    
    @Override
    public void perform(Jedis redisConnection, String key, GVBuffer gvBuffer) throws GVException, PropertiesHandlerException {

      String fieldValue = PropertiesHandler.expand(field, gvBuffer);          
      String retievedValue = redisConnection.hget(key, fieldValue);
      
      gvBuffer.setObject(retievedValue);

    }
    
    
    public static class Builder implements RedisOperation.RedisOperationBuilder {

        @Override
        public RedisOperation build(Node operationNode) throws XMLConfigException {

            String type =  operationNode.getLocalName();
            String configuredField = XMLConfig.get(operationNode, "field"); 
            
            return new RedisOperationHget(type, configuredField);
        }
        
    }
    
   
}
