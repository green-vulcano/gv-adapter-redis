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

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.buffer.GVException;
import redis.clients.jedis.Jedis;

public class RedisOperationKeys extends RedisOperation{

    public static final String TYPE = "keys";
    
    RedisOperationKeys() {
        super(TYPE);
    }

    @Override
    public void perform(Jedis redisConnection, String key, GVBuffer gvBuffer) throws GVException {
      Set<String> keys = redisConnection.keys(key);
      
      JSONArray result = new JSONArray();
      for (String k : keys) {
          JSONObject entry = new JSONObject();
           entry.put("key", k)
                .put("type", redisConnection.type(k));
           
           result.put(entry);
          
      }
      
      gvBuffer.setProperty("REDIS_KEYS_REPLY", Integer.toString(keys.size()));
      gvBuffer.setObject(result.toString());
    }
}
