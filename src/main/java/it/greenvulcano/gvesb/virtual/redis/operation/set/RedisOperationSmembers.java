/*******************************************************************************
 * Copyright (c) 2009, 2019 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.virtual.redis.operation.set;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.virtual.redis.operation.RedisOperation;
import redis.clients.jedis.Jedis;


public class RedisOperationSmembers extends RedisOperation {
    
    public static final String TYPE = "smembers";

    public RedisOperationSmembers() {
        super(TYPE);
    }

    @Override
    public void perform(Jedis redisConnection, String key, GVBuffer gvBuffer) throws GVException {  
        
    	String result = redisConnection.smembers(key).toString();
    	
        gvBuffer.setProperty("REDIS_"+TYPE.toUpperCase()+"_REPLY", result);
        gvBuffer.setObject(result);
    }

    public static class Builder implements RedisOperation.RedisOperationBuilder {

        @Override
        public RedisOperation build(Node operationNode) throws XMLConfigException {            
            return new RedisOperationSmembers();
        }
        
    }
    
}
