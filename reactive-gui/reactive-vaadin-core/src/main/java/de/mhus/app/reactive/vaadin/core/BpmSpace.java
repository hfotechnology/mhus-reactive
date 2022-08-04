/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.app.reactive.vaadin.core;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.shiro.subject.Subject;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Tree;
import com.vaadin.v7.ui.VerticalLayout;

import de.mhus.app.reactive.model.engine.EngineMessage;
import de.mhus.app.reactive.model.engine.PCase.STATE_CASE;
import de.mhus.app.reactive.model.engine.PNode.STATE_NODE;
import de.mhus.app.reactive.model.engine.PNode.TYPE_NODE;
import de.mhus.app.reactive.model.engine.SearchCriterias;
import de.mhus.app.reactive.model.ui.IEngine;
import de.mhus.app.reactive.model.ui.IEngineFactory;
import de.mhus.app.reactive.vaadin.widgets.NodeItem;
import de.mhus.app.reactive.vaadin.widgets.Refreshable;
import de.mhus.app.reactive.vaadin.widgets.VCaseList;
import de.mhus.app.reactive.vaadin.widgets.VNodeDetails;
import de.mhus.app.reactive.vaadin.widgets.VNodeList;
import de.mhus.app.reactive.vaadin.widgets.VRuntimeDetails;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.cfg.CfgTimeInterval;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.vaadin.SearchField;
import de.mhus.lib.vaadin.desktop.GuiLifecycle;
import de.mhus.lib.vaadin.desktop.Navigable;

@SuppressWarnings("deprecation")
public class BpmSpace extends VerticalLayout implements GuiLifecycle, Navigable {

    private static CfgTimeInterval CFG_REFRESH_INTERVAL =
            new CfgTimeInterval(BpmSpace.class, "refreshInterval", "20s");
    private static Log log = Log.getLog(BpmSpace.class);
    private static final long serialVersionUID = 1L;
    private static final String I_DEFAULT = "Default";
    private static final String I_UNASSIGNED = "Unassigned";
    private static final String I_ASSIGNED = "Assigned";
    private static final String DEFAULT_MENU_SELECTION = I_UNASSIGNED;
    private static final String I_ALL_NODES = "All nodes";
    private static final String I_ACTIVE_CASES = "Active cases";
    private static final String I_ALL_CASES = "All cases";

    @SuppressWarnings("unused")
    private BpmSpaceService service;

    private IEngine engine;
    private HorizontalLayout page;

    private String currentSelection;

    @SuppressWarnings("unused")
    private Component currentView;

    private Refreshable currentRefreshable;

    private Map<String, Component[]> contentCache;
    private String currentFilter;
    private WidgetActivity activity;

    public BpmSpace(BpmSpaceService bpmSpaceService) {
        this.service = bpmSpaceService;
        this.activity = new WidgetActivity(this);
    }

    @Override
    public String navigateTo(String selection, String filter) {
        Tree tree = null;
        try {
            tree = (Tree) (((VerticalLayout) (page.getComponent(0))).getComponent(0));
        } catch (Exception e) {
            log.w("failed changing menu selection", e);
            return null;
        }

        if (selection != null && tree.containsId(selection)) {

            currentRefreshable = null;

            switch (selection) {
                case I_UNASSIGNED:
                    {
                        SearchCriterias criterias = new SearchCriterias();
                        criterias.unassigned = true;
                        criterias.nodeState = STATE_NODE.WAITING;
                        criterias.type = TYPE_NODE.USER;
                        criterias.due = 0;
                        String[] properties = new String[] {"*"};
                        Component[] cached = contentCache.get(selection);
                        if (cached == null) {
                            cached = getNodeListView(criterias, properties);
                            if (cached == null) return null;
                            contentCache.put(selection, cached);
                        } else if (cached[1] instanceof VNodeList) {
                            ((VNodeList) cached[1]).doReload();
                            currentRefreshable = (Refreshable) cached[1];
                        }
                        currentView = cached[0];
                        setContent(cached[0]);
                    }
                    break;
                case I_ASSIGNED:
                    {
                        initEngine();
                        if (engine == null) return null;
                        SearchCriterias criterias = new SearchCriterias();
                        criterias.assigned = engine.getUser();
                        criterias.nodeState = STATE_NODE.WAITING;
                        criterias.type = TYPE_NODE.USER;
                        String[] properties = new String[] {"*"};
                        Component[] cached = contentCache.get(selection);
                        if (cached == null) {
                            cached = getNodeListView(criterias, properties);
                            if (cached == null) return null;
                            contentCache.put(selection, cached);
                        } else if (cached[1] instanceof VNodeList) {
                            ((VNodeList) cached[1]).doReload();
                            currentRefreshable = (Refreshable) cached[1];
                        }
                        currentView = cached[0];
                        setContent(cached[0]);
                    }
                    break;
                case I_ALL_NODES:
                    {
                        SearchCriterias criterias = new SearchCriterias();
                        String[] properties = new String[] {"*"};
                        setContent(getNodeListView(criterias, properties)[0]);
                    }
                    break;
                case I_ALL_CASES:
                    {
                        SearchCriterias criterias = new SearchCriterias();
                        String[] properties = new String[] {"*"};
                        setContent(getCaseListView(criterias, properties)[0]);
                    }
                    break;
                case I_ACTIVE_CASES:
                    {
                        SearchCriterias criterias = new SearchCriterias();
                        criterias.caseState = STATE_CASE.RUNNING;
                        String[] properties = new String[] {"*"};
                        setContent(getCaseListView(criterias, properties)[0]);
                    }
                    break;
            }

            currentSelection = selection;
            currentFilter = filter;
            tree.select(selection);

            if (tree.getParent(selection) != null && !tree.isExpanded(tree.getParent(selection)))
                tree.expandItem(tree.getParent(selection));

            if (MString.isSet(filter)) {
                // TODO				currentView.setFilter(filter);
            }
            return null;

        } else {
            log.w("failed changing menu selection, no such element found", selection);
            return null;
        }
    }

    @Override
    public void onShowSpace(boolean firstTime) {}

    public void setContent(Component content) {
        if (content == null) return;
        try {
            Component oldContent = page.getComponent(1);
            if (oldContent != content) {
                page.replaceComponent(oldContent, content);
                page.setExpandRatio(content, 1);
            }
        } catch (IndexOutOfBoundsException e) {
            page.addComponent(content, 1);
            page.setExpandRatio(content, 1);
        }
    }

    @Override
    public void doInitialize() {

        //    	Refresher refresher = new Refresher();
        //    	refresher.setRefreshInterval(10000);
        //    	refresher.addContextClickListener(new ContextClickListener() {
        //			private static final long serialVersionUID = 1L;
        //
        //			@Override
        //			public void contextClick(ContextClickEvent event) {
        //				System.out.println("Refresher");
        //			}
        //		});

        contentCache = new HashMap<>();

        page = new HorizontalLayout();
        page.setSizeFull();
        //        page.addComponent(refresher);

        VerticalLayout menu = buildMenu();


        page.addComponent(menu);
        page.setExpandRatio(menu, 0);
        //		page.setMargin(true);

        navigateTo(DEFAULT_MENU_SELECTION, null);

        addComponent(page);
    }

    private VerticalLayout buildMenu() {

        VerticalLayout menu = new VerticalLayout();

        menu.setSizeFull();
        menu.setWidth(200f, Unit.PIXELS);
        menu.setMargin(new MarginInfo(true, false, true, false));
        menu.setStyleName("leftmenu");
        final Tree tree = new Tree();

        // structure
        tree.addItem(I_DEFAULT);

        tree.addItem(I_UNASSIGNED);
        tree.setParent(I_UNASSIGNED, I_DEFAULT);
        tree.setChildrenAllowed(I_UNASSIGNED, false);

        tree.addItem(I_ASSIGNED);
        tree.setParent(I_ASSIGNED, I_DEFAULT);
        tree.setChildrenAllowed(I_ASSIGNED, false);

        tree.addItem(I_ALL_NODES);
        tree.setParent(I_ALL_NODES, I_DEFAULT);
        tree.setChildrenAllowed(I_ALL_NODES, false);

        tree.addItem(I_ACTIVE_CASES);
        tree.setParent(I_ACTIVE_CASES, I_DEFAULT);
        tree.setChildrenAllowed(I_ACTIVE_CASES, false);

        tree.addItem(I_ALL_CASES);
        tree.setParent(I_ALL_CASES, I_DEFAULT);
        tree.setChildrenAllowed(I_ALL_CASES, false);

        // end

        tree.setNullSelectionAllowed(false);

        tree.addValueChangeListener(
                new ValueChangeListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        Object selected = event.getProperty().getValue();
                        if (selected != null && !selected.equals(currentSelection)) {
                            String value = String.valueOf(selected);
                            // ensure ACS does not stay in selected state
                            navigateTo(value, null);
                        }
                    }
                });

        tree.setSizeFull();
        tree.expandItemsRecursively(I_DEFAULT);
        menu.addComponent(tree);
        menu.setExpandRatio(tree, 1);

        return menu;
    }

    private Component[] getNodeListView(SearchCriterias criterias, String[] properties) {
        initEngine();
        if (engine == null) return null;

        VNodeList list = new VNodeList();
        list.configure(engine, activity, criterias.clone(), properties);

        VerticalLayout l = new VerticalLayout();
        SearchField searchText = new SearchField(null);
        // searchText.addKnownFacetName("search");
        for (String id : SearchCriterias.keys()) searchText.addKnownFacetName(id + ":");
        searchText.setWidth("100%");
        searchText.setListener(
                new SearchField.Listener() {

                    @Override
                    public void doFilter(SearchField searchField) {
                        SearchCriterias c = criterias.clone();
                        c.order = list.getSearchCriterias().order;
                        c.orderAscending = list.getSearchCriterias().orderAscending;
                        IProperties s = searchField.createFilterRequest().toProperties();
                        c.parse(s);
                        // System.out.println("Search: " + c);
                        list.setSearchCriterias(c);
                    }
                });

        l.addComponent(searchText);
        l.setExpandRatio(searchText, 0);

        l.addComponent(list);
        l.setExpandRatio(list, 1);

        addComponent(l);
        setExpandRatio(l, 1);

        l.setSizeFull();

        currentRefreshable = list;
        return new Component[] {l, list};
    }

    protected void showNodeDetails(NodeItem item) {
        VNodeDetails panel =
                new VNodeDetails() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onCancel() {
                        System.out.println("Cancel");
                        showNodeList();
                    }
                };

        panel.configure(engine, item);

        setContent(panel);
    }

    private Component[] getCaseListView(SearchCriterias criterias, String[] properties) {
        initEngine();
        if (engine == null) return null;
        VCaseList list = new VCaseList(activity);

        VerticalLayout l = new VerticalLayout();
        SearchField searchText = new SearchField(null);
        // searchText.addKnownFacetName("search");
        for (String id : SearchCriterias.keys()) searchText.addKnownFacetName(id + ":");
        searchText.setWidth("100%");
        searchText.setListener(
                new SearchField.Listener() {

                    @Override
                    public void doFilter(SearchField searchField) {
                        SearchCriterias c = criterias.clone();
                        c.order = list.getSearchCriterias().order;
                        c.orderAscending = list.getSearchCriterias().orderAscending;
                        IProperties s = searchField.createFilterRequest().toProperties();
                        c.parse(s);
                        // System.out.println("Search: " + c);
                        list.setSearchCriterias(c);
                    }
                });

        l.addComponent(searchText);
        l.setExpandRatio(searchText, 0);

        l.addComponent(list);
        l.setExpandRatio(list, 1);

        list.configure(engine, activity, criterias.clone(), properties);
        addComponent(l);
        setExpandRatio(l, 1);

        l.setSizeFull();
        currentRefreshable = list;
        return new Component[] {l, list};
    }

    private void initEngine() {
        Subject subject = Aaa.getSubject();
        String username = Aaa.getPrincipal(subject);
        Locale locale = Aaa.getLocale(subject);
        engine = M.l(IEngineFactory.class).create(username, locale);
    }

    protected void showRuntime(List<EngineMessage[]> runtime) throws Exception {

        initEngine();
        if (engine == null) return;
        VRuntimeDetails list =
                new VRuntimeDetails() {
                    private static final long serialVersionUID = 1L;
                };

        VerticalLayout l = new VerticalLayout();

        l.addComponent(list);
        l.setExpandRatio(list, 1);

        list.configure(engine, runtime);
        addComponent(l);
        setExpandRatio(l, 1);

        l.setSizeFull();

        setContent(l);
    }

    public void showNodeList() {
        if (currentSelection != null) navigateTo(currentSelection, currentFilter);
        else setContent(new Label(""));
    }

    @Override
    public void doDestroy() {
        contentCache = null;
        page.detach();
        page = null;
    }

    @Override
    public void doCreateMenu(MenuItem[] menu) {
        menu[0].setEnabled(true);
        menu[0].setText("Engine");
        menu[0].setVisible(true);

        menu[0].addItem(
                "Execute ...",
                new MenuBar.Command() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void menuSelected(MenuItem selectedItem) {
                        doExecute(null);
                    }
                });
    }

    protected void doExecute(String uri) {
        ExecuteProcessForm form = new ExecuteProcessForm(this, uri);
        setContent(form);
    }

    public IEngine getEngine() {
        if (engine == null) initEngine();
        return engine;
    }

    public void doRefresh() {
        if (currentRefreshable != null) currentRefreshable.doRefresh();
    }

    public Log log() {
        return log;
    }
}
