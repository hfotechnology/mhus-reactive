/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.reactive.engine.util;

import java.io.IOException;
import java.util.UUID;

import de.mhus.cherry.reactive.engine.Engine;
import de.mhus.cherry.reactive.model.engine.PCase;
import de.mhus.cherry.reactive.model.engine.PCase.STATE_CASE;
import de.mhus.cherry.reactive.model.engine.PCaseInfo;
import de.mhus.cherry.reactive.model.engine.PNode;
import de.mhus.cherry.reactive.model.engine.PNode.STATE_NODE;
import de.mhus.cherry.reactive.model.engine.PNode.TYPE_NODE;
import de.mhus.cherry.reactive.model.engine.PNodeInfo;
import de.mhus.cherry.reactive.model.engine.Result;
import de.mhus.cherry.reactive.model.engine.SearchCriterias;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.schedule.CronJob;
import de.mhus.lib.errors.NotFoundException;

public class EngineUtil {

	/**
	 * Calculate the next execution time for this trigger
	 * @param timer
	 * @return time or -1
	 */
	public static long getNextScheduledTime(String timer) {
		
		if (MString.isEmptyTrim(timer))
			return -1;

		long time = MPeriod.toTime(timer, -1);
		if (time < 0) {
			CronJob.Definition def = new CronJob.Definition(timer);
			if(def.isDisabled()) {
				return -1;
			}
			return def.calculateNext(System.currentTimeMillis());
		} else {
			return System.currentTimeMillis() + time;
		}
	}

//	public static String getProcessCanonicalName(AProcess process) {
//		ProcessDescription desc = process.getClass().getAnnotation(ProcessDescription.class);
//		if (desc == null) return null;
//		String name = desc.name();
//		if (MString.isEmpty(name)) name = process.getClass().getCanonicalName();
//		return name + ":" + desc.version();
//	}

	public static PCase getCase(Engine engine, String id) throws NotFoundException, IOException {
		if (MValidator.isUUID(id))
			return engine.getCase(UUID.fromString(id));
		SearchCriterias c = new SearchCriterias();
		c.custom = id;
		Result<PCaseInfo> res = engine.storageSearchCases(c);
		for (PCaseInfo info : res) {
			if (info.getState() != STATE_CASE.CLOSED && info.getState() != STATE_CASE.SUSPENDED) {
				res.close();
				return engine.getCase(info.getId());
			}
		}
		res.close();
		
		res = engine.storageSearchCases(c);
		for (PCaseInfo info : res) {
			res.close();
			return engine.getCase(info.getId());
		}
		res.close();
		
		return null;
	}
	
	public static PCaseInfo getCaseInfo(Engine engine, String id) throws Exception {
		if (MValidator.isUUID(id))
			return engine.storageGetCaseInfo(UUID.fromString(id));
		SearchCriterias c = new SearchCriterias();
		c.custom = id;
		Result<PCaseInfo> res = engine.storageSearchCases(c);
		for (PCaseInfo info : res) {
			if (info.getState() != STATE_CASE.CLOSED && info.getState() != STATE_CASE.SUSPENDED) {
				res.close();
				return info;
			}
		}
		res.close();
		
		res = engine.storageSearchCases(c);
		for (PCaseInfo info : res) {
			res.close();
			return info;
		}
		res.close();
		
		return null;
	}


	public static PNode getFlowNode(Engine engine, String id) throws NotFoundException, IOException {
		if (MValidator.isUUID(id))
			return engine.getFlowNode(UUID.fromString(id));
		SearchCriterias c = new SearchCriterias();
		c.custom = id;
		Result<PNodeInfo> res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			if (info.getState() != STATE_NODE.CLOSED && info.getState() != STATE_NODE.SUSPENDED && info.getState() != STATE_NODE.WAITING && info.getType() != TYPE_NODE.RUNTIME) {
				res.close();
				return engine.getFlowNode(info.getId());
			}
		}
		res.close();

		res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			if (info.getState() != STATE_NODE.SUSPENDED && info.getType() != TYPE_NODE.RUNTIME) {
				res.close();
				return engine.getFlowNode(info.getId());
			}
		}
		res.close();

		res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			res.close();
			return engine.getFlowNode(info.getId());
		}
		res.close();
		
		return null;
	}

	public static PNodeInfo getFlowNodeInfo(Engine engine, String id) throws Exception {
		if (MValidator.isUUID(id))
			return engine.storageGetFlowNodeInfo(UUID.fromString(id));
		SearchCriterias c = new SearchCriterias();
		c.custom = id;
		Result<PNodeInfo> res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			if (info.getState() != STATE_NODE.CLOSED && info.getState() != STATE_NODE.SUSPENDED && info.getState() != STATE_NODE.WAITING && info.getType() != TYPE_NODE.RUNTIME) {
				res.close();
				return info;
			}
		}
		res.close();

		res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			if (info.getState() != STATE_NODE.SUSPENDED && info.getType() != TYPE_NODE.RUNTIME) {
				res.close();
				return info;
			}
		}
		res.close();

		res = engine.storageSearchFlowNodes(c);
		for (PNodeInfo info : res) {
			res.close();
			return info;
		}
		res.close();
		
		return null;
	}

}
