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
package de.mhus.cherry.reactive.model.activity;

import java.util.Map;

import de.mhus.cherry.reactive.model.util.IndexValuesProvider;

/**
 * Interface for pools.
 * @author mikehummel
 *
 * @param <P>
 */
public interface APool<P extends APool<?>> extends AElement<P>, IndexValuesProvider {

	Map<String, Object> exportParamters();
	
	void importParameters(Map<String, Object> parameters);
	
	void initializeCase(Map<String, Object> parameters) throws Exception;
	
	void closeCase();

}
