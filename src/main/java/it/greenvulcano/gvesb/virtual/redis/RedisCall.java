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
package it.greenvulcano.gvesb.virtual.redis;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.channel.redis.RedisChannel;
import it.greenvulcano.gvesb.virtual.*;
import it.greenvulcano.gvesb.virtual.redis.operation.RedisOperation;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RedisCall implements CallOperation {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RedisCall.class);

    private OperationKey key = null;
    private String name;
    private String uri;
       
    private final Set<RedisOperation> operations = new LinkedHashSet<>();

    private Optional<JedisPool> redisChannelConnection;
    
    @Override
    public void init(Node node) throws InitializationException {

        logger.debug("Initializing redis-call...");

        try {
            // gets node name to show in logger
            name = XMLConfig.get(node, "@name");
            uri = XMLConfig.get(node, "@uri");

            redisChannelConnection = RedisChannel.getRedisClient(node);
            
            // gets child elements of redis-call
            NodeList redisOperation = XMLConfig.getNodeList(node, "./*");
            for (int i = 0; i < redisOperation.getLength(); i++) {
                
                operations.add(RedisOperation.build(redisOperation.item(i)));
            }

            if (operations.isEmpty()) {
                throw new NoSuchElementException("No Redis operation found for redis-call " + name);
            }

            logger.debug("Configured {} Redis operations for call {} ", operations.size(), name);

        } catch (Exception e) {

            throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][] { { "message", e.getMessage() } }, e);

        }

    }

    @Override
    public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {

        try (Jedis connection = redisChannelConnection.map(JedisPool::getResource)
                                                      .orElseGet(() -> buildLocalConnection(gvBuffer))) {

            logger.debug("Preparing Redis call: " + name);
            
            for (RedisOperation operation : operations) {
              
                String key = PropertiesHandler.expand(operation.getKey(), gvBuffer);                
                logger.debug("Executing Redis operation {} on key {} ", operation.getOperationType(), key);
                
                operation.perform(connection, key, gvBuffer);
                                
            }

        } catch (Exception exc) {
            throw new CallException("GV_CALL_SERVICE_ERROR",
                                    new String[][] { { "service", gvBuffer.getService() },
                                                     { "system", gvBuffer.getSystem() },
                                                     { "tid", gvBuffer.getId().toString() },
                                                     { "message", exc.getMessage() } },
                                    exc);
        }
        return gvBuffer;
    }
    
    private Jedis buildLocalConnection(GVBuffer gvBuffer) {
        try {
            URI redisURI = URI.create(PropertiesHandler.expand(uri, gvBuffer));
            return new Jedis(redisURI);
        } catch (PropertiesHandlerException e) {
            throw new IllegalArgumentException("Invalid Redis URI "+uri, e);
        }
    }

    @Override
    public void cleanUp() {

        // do nothing
    }

    @Override
    public void destroy() {

        // do nothing
    }

    @Override
    public void setKey(OperationKey operationKey) {

        this.key = operationKey;
    }

    @Override
    public OperationKey getKey() {

        return key;
    }

    @Override
    public String getServiceAlias(GVBuffer gvBuffer) {

        return gvBuffer.getService();
    }

}
