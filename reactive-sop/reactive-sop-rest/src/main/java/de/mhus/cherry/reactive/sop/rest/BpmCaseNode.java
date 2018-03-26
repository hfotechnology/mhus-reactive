package de.mhus.cherry.reactive.sop.rest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.reactive.model.engine.SearchCriterias;
import de.mhus.cherry.reactive.model.ui.ICase;
import de.mhus.cherry.reactive.model.ui.IEngine;
import de.mhus.cherry.reactive.model.ui.IEngineFactory;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.rest.AbstractObjectListNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.RestNodeService;
import de.mhus.osgi.sop.api.rest.RestUtil;

@Component(provide=RestNodeService.class)
public class BpmCaseNode extends AbstractObjectListNode<XCase> {

	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID,FOUNDATION_ID};
	}

	@Override
	public String getNodeId() {
		return "bpmcase";
	}

	@Override
	protected List<XCase> getObjectList(CallContext callContext) throws MException {

		AccessApi aaa = MApi.lookup(AccessApi.class);
		AaaContext context = aaa.getCurrent();
		IEngine engine = MApi.lookup(IEngineFactory.class).create(context.getAccountId(), context.getLocale());

		SearchCriterias criterias = new SearchCriterias(new MProperties(callContext.getParameters()));
		int page = M.c(callContext.getParameter("_page"), 0);
		int size = Math.min(M.c(callContext.getParameter("_size"), 100), 1000);
		LinkedList<XCase> out = new LinkedList<>();
		try {
			List<ICase> res = engine.searchCases(criterias, page, size);
			for (ICase item : res) 
				out.add(new XCase(engine, item, false));
		} catch (IOException e) {
			throw new MException(e);
		}
		
		return out;
	}

	@Override
	public Class<XCase> getManagedClass() {
		return XCase.class;
	}

	@Override
	protected XCase getObjectForId(CallContext context, String id) throws Exception {
		
		AccessApi aaa = MApi.lookup(AccessApi.class);
		AaaContext acontext = aaa.getCurrent();
		IEngine engine = MApi.lookup(IEngineFactory.class).create(acontext.getAccountId(), acontext.getLocale());

		ICase item = engine.getCase(id);
		return new XCase(engine, item, true);
	}

}
