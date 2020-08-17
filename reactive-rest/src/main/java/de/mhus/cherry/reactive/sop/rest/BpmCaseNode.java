/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.reactive.sop.rest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.shiro.subject.Subject;
import org.osgi.service.component.annotations.Component;

import de.mhus.cherry.reactive.model.engine.SearchCriterias;
import de.mhus.cherry.reactive.model.ui.ICase;
import de.mhus.cherry.reactive.model.ui.IEngine;
import de.mhus.cherry.reactive.model.ui.IEngineFactory;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.errors.MException;
import de.mhus.rest.core.CallContext;
import de.mhus.rest.core.api.RestNodeService;
import de.mhus.rest.core.node.ObjectListNode;

@Component(service = RestNodeService.class)
public class BpmCaseNode extends ObjectListNode<ICase, ICase> {

    @Override
    public String[] getParentNodeCanonicalClassNames() {
        return new String[] {ROOT_PARENT};
    }

    @Override
    public String getNodeId() {
        return "bpmcase";
    }

    @Override
    protected List<ICase> getObjectList(CallContext callContext) throws MException {

        Subject subject = AccessUtil.getSubject();
        String username = AccessUtil.getPrincipal(subject);
        Locale locale = AccessUtil.getLocale(subject);

        IEngine engine = M.l(IEngineFactory.class).create(username, locale);

        String propertyNames = callContext.getParameter("names");

        SearchCriterias criterias =
                new SearchCriterias(new MProperties(callContext.getParameters()));
        int page = M.to(callContext.getParameter("page"), 0);
        int size = Math.min(M.to(callContext.getParameter("size"), 100), 1000);
        try {
            return engine.searchCases(
                    criterias, page, size, propertyNames == null ? null : propertyNames.split(","));
        } catch (IOException e) {
            throw new MException(e);
        }
    }

    //	@Override
    //	public Class<ICase> getManagedClass() {
    //		return ICase.class;
    //	}

    @Override
    protected ICase getObjectForId(CallContext context, String id) throws Exception {

        Subject subject = AccessUtil.getSubject();
        String username = AccessUtil.getPrincipal(subject);
        Locale locale = AccessUtil.getLocale(subject);

        IEngine engine = M.l(IEngineFactory.class).create(username, locale);

        String propertyNames = context.getParameter("names");

        return engine.getCase(id, propertyNames == null ? null : propertyNames.split(","));
    }
}
