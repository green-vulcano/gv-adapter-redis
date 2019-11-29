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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import redis.clients.jedis.Jedis;

public abstract class RedisOperation {

	private final String operationType;
	private String key;

	private final static Map<String, RedisOperationBuilder> BUILDERS = new HashMap<>();
	
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

	public abstract void perform(Jedis redisConnection, String key, GVBuffer gvBuffer) throws GVException, PropertiesHandlerException;
	
	protected static void registerBuilder(String type, RedisOperationBuilder builder) {
	    BUILDERS.put(type, builder);
	}
	
	public static RedisOperation build(Node redisOperationNode) throws XMLConfigException {

		String operationName = redisOperationNode.getLocalName();

		return Optional.ofNullable(BUILDERS.get(operationName))
		               .orElseThrow()
		               .build(redisOperationNode);

	}
	
	public interface RedisOperationBuilder {	    
	    RedisOperation build(Node redisOperationNode) throws XMLConfigException;
	    
	}

}
