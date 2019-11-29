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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import org.w3c.dom.Node;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import redis.clients.jedis.Jedis;

public abstract class RedisOperation {

	private final String operationType;
	private String key;

	private final static Map<String, Supplier<RedisOperation>> FACTORY_SUPPLIERS; 


	static {
		FACTORY_SUPPLIERS = Collections.unmodifiableMap(Map.ofEntries(
				
				// VALUE: String
				Map.entry(RedisOperationGet.TYPE, RedisOperationGet::new),
				Map.entry(RedisOperationSet.TYPE, RedisOperationSet::new), 
				Map.entry(RedisOperationDelete.TYPE, RedisOperationDelete::new),
				
				// VALUE: List
				Map.entry(RedisOperationLpush.TYPE, RedisOperationLpush::new),
				Map.entry(RedisOperationRpush.TYPE, RedisOperationLpush::new),
				Map.entry(RedisOperationLpop.TYPE, RedisOperationLpush::new),
				Map.entry(RedisOperationRpop.TYPE, RedisOperationLpush::new),
				Map.entry(RedisOperationLlen.TYPE, RedisOperationLlen::new),
				
				// VALUE: Set
				Map.entry(RedisOperationSadd.TYPE, RedisOperationSadd::new),
				Map.entry(RedisOperationSismember.TYPE, RedisOperationSismember::new),
				Map.entry(RedisOperationSpop.TYPE, RedisOperationSpop::new),
				Map.entry(RedisOperationScard.TYPE, RedisOperationScard::new),
				Map.entry(RedisOperationSrem.TYPE, RedisOperationSrem::new),
				Map.entry(RedisOperationSmembers.TYPE, RedisOperationSmembers::new),
				Map.entry(RedisOperationSunion.TYPE, RedisOperationSunion::new),
				Map.entry(RedisOperationSdiff.TYPE, RedisOperationSdiff::new),
				
				// VALUE: Sorted Set 
								
				
				// VALUE: Hash
								
				
				// VALUE: Bitmap
								
				
				// VALUE: HyperLogLog
								
				
				// VALUE: Other
				Map.entry(RedisOperationKeys.TYPE, RedisOperationKeys::new)));
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
				.get();

		redisOperation.setKey(XMLConfig.get(redisOperationNode, "@key"));

		return redisOperation;
	}

}
