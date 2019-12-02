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
package it.greenvulcano.gvesb.virtual.redis.operation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.gvesb.virtual.redis.operation.generic.RedisOperationKeys;
import it.greenvulcano.gvesb.virtual.redis.operation.hash.RedisOperationHdel;
import it.greenvulcano.gvesb.virtual.redis.operation.hash.RedisOperationHget;
import it.greenvulcano.gvesb.virtual.redis.operation.hash.RedisOperationHlen;
import it.greenvulcano.gvesb.virtual.redis.operation.hash.RedisOperationHset;
import it.greenvulcano.gvesb.virtual.redis.operation.list.RedisOperationLlen;
import it.greenvulcano.gvesb.virtual.redis.operation.list.RedisOperationLpop;
import it.greenvulcano.gvesb.virtual.redis.operation.list.RedisOperationLpush;
import it.greenvulcano.gvesb.virtual.redis.operation.list.RedisOperationRpop;
import it.greenvulcano.gvesb.virtual.redis.operation.list.RedisOperationRpush;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSadd;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationScard;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSismember;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSmembers;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSpop;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSrem;
import it.greenvulcano.gvesb.virtual.redis.operation.set.RedisOperationSunion;
import it.greenvulcano.gvesb.virtual.redis.operation.string.RedisOperationDelete;
import it.greenvulcano.gvesb.virtual.redis.operation.string.RedisOperationGet;
import it.greenvulcano.gvesb.virtual.redis.operation.string.RedisOperationSet;
import redis.clients.jedis.Jedis;

public abstract class RedisOperation {

	private final String operationType;
	private String key;
	
    private final static Map<String, RedisOperationBuilder> FACTORY_SUPPLIERS; 

    static {
    	
    	HashMap<String, RedisOperationBuilder> map = new HashMap<>();
    	
    	// Generic
    	map.put(RedisOperationKeys.TYPE, new RedisOperationKeys.Builder());
    	
    	// Hash 
    	map.put(RedisOperationHdel.TYPE, new RedisOperationHdel.Builder());
    	map.put(RedisOperationHget.TYPE, new RedisOperationHget.Builder());
    	map.put(RedisOperationHlen.TYPE, new RedisOperationHlen.Builder());
    	map.put(RedisOperationHset.TYPE, new RedisOperationHset.Builder());
    	
    	// List
    	map.put(RedisOperationLlen.TYPE, new RedisOperationLlen.Builder());
    	map.put(RedisOperationLpop.TYPE, new RedisOperationLpop.Builder());
    	map.put(RedisOperationLpush.TYPE, new RedisOperationLpush.Builder());
    	map.put(RedisOperationRpop.TYPE, new RedisOperationRpop.Builder());
    	map.put(RedisOperationRpush.TYPE, new RedisOperationRpush.Builder());
    	
    	// Set
    	map.put(RedisOperationSadd.TYPE, new RedisOperationSadd.Builder());
    	map.put(RedisOperationScard.TYPE, new RedisOperationScard.Builder());
    	map.put(RedisOperationSismember.TYPE, new RedisOperationSismember.Builder());
    	map.put(RedisOperationSmembers.TYPE, new RedisOperationSmembers.Builder());
    	map.put(RedisOperationSpop.TYPE, new RedisOperationSpop.Builder());
    	map.put(RedisOperationSrem.TYPE, new RedisOperationSrem.Builder());
    	map.put(RedisOperationSunion.TYPE, new RedisOperationSunion.Builder());
    	
    	// String
    	map.put(RedisOperationGet.TYPE, new RedisOperationGet.Builder());
    	map.put(RedisOperationSet.TYPE, new RedisOperationSet.Builder());
    	map.put(RedisOperationDelete.TYPE, new RedisOperationDelete.Builder());
    	
        FACTORY_SUPPLIERS = Collections.unmodifiableMap(map);
        
    }

	protected RedisOperation(String operationType) {
		this.operationType = operationType;
	}

	public String getOperationType() {
		return operationType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public abstract void perform(Jedis redisConnection, String key, GVBuffer gvBuffer) throws GVException;

	public static RedisOperation build(Node redisOperationNode) throws XMLConfigException {

		String operationName = redisOperationNode.getLocalName();

        RedisOperation redisOperation = Optional.ofNullable(FACTORY_SUPPLIERS.get(operationName))
                                                .orElseThrow(()->new XMLConfigException("Invalid operation "+operationName))
                                                .build(redisOperationNode);
		 
		redisOperation.setKey(XMLConfig.get(redisOperationNode, "@key"));

		return redisOperation;
	}
	
	public interface RedisOperationBuilder {	    
	    RedisOperation build(Node redisOperationNode) throws XMLConfigException;
	    
	}
}
