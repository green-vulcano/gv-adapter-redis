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
package it.greenvulcano.gvesb.channel.redis;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RedisChannel {

	private final static Logger LOG = LoggerFactory.getLogger(RedisChannel.class);
	private final static Map<String, JedisPool> redisClients = new HashMap<>();

	public static void setup() {
		// finds all channel of type: RedisDBAdapter and builds corresponding uri strings from xpath
		try {

			NodeList redisChannelList = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(), "//Channel[@type='RedisAdapter']");

			LOG.debug("Enabled RedisAdapter channels found: " + redisChannelList.getLength());
			IntStream.range(0, redisChannelList.getLength())
			         .mapToObj(redisChannelList::item)
			         .forEach(RedisChannel::buildRedisClient);

		} catch (XMLConfigException e) {
			LOG.error("Error reading configuration", e);
		}
	}

	public static void shutdown() {		
		redisClients.clear();
	}

	private static void buildRedisClient(Node redisChannelNode) {
		try {
			if (XMLConfig.exists(redisChannelNode, "@endpoint") && XMLConfig.getBoolean(redisChannelNode, "@enabled", true)) {

				LOG.debug("Configuring Redis instance for Channel " + XMLUtils.get_S(redisChannelNode, "@id-channel") 
				                                   + " in System" + XMLUtils.get_S(redisChannelNode.getParentNode(), "@id-system"));

				String uri = PropertiesHandler.expand(XMLUtils.get_S(redisChannelNode, "@endpoint"));
				if (uri!=null) {
				    JedisPool redisConnection = new JedisPool(new JedisPoolConfig(), URI.create(uri));
				    redisClients.put(XPathFinder.buildXPath(redisChannelNode), redisConnection);
				}
			}

		} catch (Exception e) {
			LOG.error("Error configuring RedisClient", e);
		}

	}

	public static Optional<JedisPool> getRedisClient(Node callOperationNode) {
		String xpath = XPathFinder.buildXPath(callOperationNode.getParentNode());
		return Optional.ofNullable(redisClients.get(xpath));
		
	}

   
}