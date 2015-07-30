/*
 * $Id$  
 * $URL$
 * 
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * 
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing 
 * of individual contributors are as shown in the packaged copyright.txt 
 * file. 
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.ikasan.dashboard.ui.topology.panel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.ikasan.dashboard.ui.framework.constants.SecurityConstants;
import org.ikasan.dashboard.ui.framework.util.DashboardSessionValueConstants;
import org.ikasan.dashboard.ui.framework.util.PolicyLinkTypeConstants;
import org.ikasan.dashboard.ui.mappingconfiguration.component.IkasanSmallCellStyleGenerator;
import org.ikasan.dashboard.ui.topology.component.ActionedExclusionTab;
import org.ikasan.dashboard.ui.topology.component.CategorisedErrorTab;
import org.ikasan.dashboard.ui.topology.component.ErrorOccurrenceTab;
import org.ikasan.dashboard.ui.topology.component.ExclusionsTab;
import org.ikasan.dashboard.ui.topology.component.WiretapTab;
import org.ikasan.dashboard.ui.topology.window.ComponentConfigurationWindow;
import org.ikasan.dashboard.ui.topology.window.ErrorCategorisationWindow;
import org.ikasan.dashboard.ui.topology.window.NewBusinessStreamWindow;
import org.ikasan.dashboard.ui.topology.window.StartupControlConfigurationWindow;
import org.ikasan.dashboard.ui.topology.window.WiretapConfigurationWindow;
import org.ikasan.error.reporting.service.ErrorCategorisationService;
import org.ikasan.exclusion.model.ExclusionEvent;
import org.ikasan.hospital.model.ExclusionEventAction;
import org.ikasan.hospital.service.HospitalManagementService;
import org.ikasan.security.service.authentication.IkasanAuthentication;
import org.ikasan.spec.error.reporting.ErrorReportingService;
import org.ikasan.spec.exclusion.ExclusionManagementService;
import org.ikasan.spec.search.PagedSearchResult;
import org.ikasan.spec.serialiser.SerialiserFactory;
import org.ikasan.systemevent.model.SystemEvent;
import org.ikasan.systemevent.service.SystemEventService;
import org.ikasan.topology.model.BusinessStream;
import org.ikasan.topology.model.BusinessStreamFlow;
import org.ikasan.topology.model.BusinessStreamFlowKey;
import org.ikasan.topology.model.Component;
import org.ikasan.topology.model.Flow;
import org.ikasan.topology.model.Module;
import org.ikasan.topology.model.Server;
import org.ikasan.topology.service.TopologyService;
import org.ikasan.wiretap.dao.WiretapDao;
import org.ikasan.wiretap.service.TriggerManagementService;
import org.springframework.security.core.GrantedAuthority;
import org.vaadin.teemu.VaadinIcons;

import com.ikasan.topology.exception.DiscoveryException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.ValoTheme;

/**
 * 
 * @author Ikasan Development Team
 *
 */
public class TopologyViewPanel extends Panel implements View, Action.Handler
{
	/** running state string constant */
    private static String RUNNING = "running";
    
    /** stopped state string constant */
    private static String STOPPED = "stopped";
    
    /** recovering state string constant */
    private static String RECOVERING = "recovering";
    
    /** stoppedInError state string constant */
    private static String STOPPED_IN_ERROR = "stoppedInError";
    
    /** paused state string constant */
    private static String PAUSED = "paused";
    
	/**
	 * 
	 */
	private  final long serialVersionUID = -6213301218439409056L;
	
	private Logger logger = Logger.getLogger(TopologyViewPanel.class);
	
	private final Action START = new Action("Start");
    private final Action STOP = new Action("Stop");
    private final Action VIEW_DIAGRAM = new Action("View Diagram");
    private final Action CONFIGURE = new Action("Configure");
    private final Action PAUSE = new Action("Pause");
    private final Action START_PAUSE = new Action("Start/Pause");
    private final Action RESUME = new Action("Resume");
    private final Action RESTART = new Action("Re-start");
    private final Action DISABLE = new Action("Disable");
    private final Action DETAILS = new Action("Details");
    private final Action WIRETAP = new Action("Wiretap");
    private final Action ERROR_CATEGORISATION = new Action("Categorise Error");
    private final Action STARTUP_CONTROL = new Action("Startup Type");
    private final Action[] serverActions = new Action[] { DETAILS, ERROR_CATEGORISATION };
    private final Action[] moduleActions = new Action[] { DETAILS, VIEW_DIAGRAM, ERROR_CATEGORISATION };
    private final Action[] flowActionsStopped = new Action[] { START, START_PAUSE, STARTUP_CONTROL, ERROR_CATEGORISATION };
    private final Action[] flowActionsStarted = new Action[] { STOP, PAUSE, STARTUP_CONTROL, ERROR_CATEGORISATION };
    private final Action[] flowActionsPaused = new Action[] { STOP, RESUME, STARTUP_CONTROL, ERROR_CATEGORISATION };
    private final Action[] flowActions = new Action[] { ERROR_CATEGORISATION };
    private final Action[] componentActionsConfigurable = new Action[] { CONFIGURE, WIRETAP, ERROR_CATEGORISATION };
    private final Action[] componentActions = new Action[] { WIRETAP, ERROR_CATEGORISATION };
    private final Action[] actionsEmpty = new Action[]{};

	private Panel topologyTreePanel;
	private Tree moduleTree;
	private ComponentConfigurationWindow componentConfigurationWindow;

	private Panel tabsheetPanel;
	
	private Table businessStreamTable;
	private Table wiretapTable;	
	private Table errorOccurenceTable;
	private Table exclusionsTable;
	private Table actionedExclusionsTable;
	private Table systemEventTable;
	
	private ComboBox businessStreamCombo;
	private ComboBox treeViewBusinessStreamCombo;
	
	private WiretapDao wiretapDao;
	
	private Table wiretapModules = new Table("Modules");
	private Table wiretapFlows = new Table("Flows");
	private Table wiretapComponents = new Table("Components");
	
	private Table errorOccurenceModules = new Table("Modules");
	private Table errorOccurenceFlows = new Table("Flows");
	private Table errorOccurenceComponents = new Table("Components");
	
	private Table actionedExclusionsModules = new Table("Modules");
	private Table actionedExclusionsFlows = new Table("Flows");
	private Table actionedExclusionsComponents = new Table("Components");
	
	private PopupDateField fromDate;
	private PopupDateField toDate;
	private PopupDateField errorFromDate;
	private PopupDateField errorToDate;
	private PopupDateField actionedExclusionFromDate;
	private PopupDateField actionedExclusionToDate;
	private PopupDateField systemEventFromDate;
	private PopupDateField systemEventToDate;
	
//	private TextField eventId;
//	private TextField payloadContent;
//	
	private ErrorReportingService errorReportingService;
	private ExclusionManagementService<ExclusionEvent, String> exclusionManagementService;
	private HospitalManagementService<ExclusionEventAction> hospitalManagementService;
	private TopologyService topologyService;
	
	private SerialiserFactory serialiserFactory;
	
	private BusinessStream businessStream;
	
	private SystemEventService systemEventService;
	private ErrorCategorisationService errorCategorisationService;
	private TriggerManagementService triggerManagementService;
	
	private HashMap<String, String> flowStates = new HashMap<String, String>();
	
	public TopologyViewPanel(TopologyService topologyService, ComponentConfigurationWindow componentConfigurationWindow,
			 WiretapDao wiretapDao, ErrorReportingService errorReportingService, ExclusionManagementService<ExclusionEvent, String> exclusionManagementService,
			 SerialiserFactory serialiserFactory, HospitalManagementService<ExclusionEventAction> hospitalManagementService, SystemEventService systemEventService,
			 ErrorCategorisationService errorCategorisationService, TriggerManagementService triggerManagementService)
	{
		this.topologyService = topologyService;
		if(this.topologyService == null)
		{
			throw new IllegalArgumentException("topologyService cannot be null!");
		}
		this.componentConfigurationWindow = componentConfigurationWindow;
		if(this.componentConfigurationWindow == null)
		{
			throw new IllegalArgumentException("componentConfigurationWindow cannot be null!");
		}
		this.wiretapDao = wiretapDao;
		if(this.wiretapDao == null)
		{
			throw new IllegalArgumentException("wiretapDao cannot be null!");
		}
		this.errorReportingService = errorReportingService;
		if(this.errorReportingService == null)
		{
			throw new IllegalArgumentException("errorReportingService cannot be null!");
		}
		this.exclusionManagementService = exclusionManagementService;
		if(this.exclusionManagementService == null)
		{
			throw new IllegalArgumentException("exclusionManagementService cannot be null!");
		}
		this.serialiserFactory = serialiserFactory;
		if(this.serialiserFactory == null)
		{
			throw new IllegalArgumentException("serialiserFactory cannot be null!");
		}
		this.hospitalManagementService = hospitalManagementService;
		if(this.hospitalManagementService == null)
		{
			throw new IllegalArgumentException("hospitalManagementService cannot be null!");
		}
		this.systemEventService = systemEventService;
		if(this.systemEventService == null)
		{
			throw new IllegalArgumentException("systemEventService cannot be null!");
		}
		this.errorCategorisationService = errorCategorisationService;
		if(this.errorCategorisationService == null)
		{
			throw new IllegalArgumentException("errorCategorisationService cannot be null!");
		}
		this.triggerManagementService = triggerManagementService;
		if(this.triggerManagementService == null)
		{
			throw new IllegalArgumentException("triggerManagementService cannot be null!");
		}

		init();
	}

	protected void init()
	{
		this.tabsheetPanel = new Panel("Topology Stuff");
		this.tabsheetPanel.setStyleName("dashboard");
		this.tabsheetPanel.setSizeFull();
		
		this.createModuleTreePanel();
		
		this.setWidth("100%");
		this.setHeight("100%");

		HorizontalSplitPanel hsplit = new HorizontalSplitPanel();
		hsplit.setStyleName(ValoTheme.SPLITPANEL_LARGE);

		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSizeFull();
		leftLayout.setMargin(true);
		leftLayout.addComponent(this.topologyTreePanel);
		hsplit.setFirstComponent(leftLayout);
		HorizontalLayout rightLayout = new HorizontalLayout();
		rightLayout.setSizeFull();
		rightLayout.setMargin(true);
		rightLayout.addComponent(this.tabsheetPanel);
		hsplit.setSecondComponent(rightLayout);
		hsplit.setSplitPosition(30, Unit.PERCENTAGE);

		this.setContent(hsplit);
	}
	
	protected void createTabSheet()
	{			
		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeFull();

		final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
	        	.getAttribute(DashboardSessionValueConstants.USER);
		
		Collection<GrantedAuthority> auths = (Collection<GrantedAuthority>)authentication.getAuthorities();
		
		for(GrantedAuthority auth: auths)
		{
			logger.info("Auth: " + auth.getAuthority());
		}
	    	
    	if(authentication != null 
    			&& (authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					|| authentication.hasGrantedAuthority(SecurityConstants.VIEW_BUSINESS_STREAM_AUTHORITY)))
    	{
			VerticalLayout tab1 = new VerticalLayout();
			tab1.setSizeFull();
			
			tab1.addComponent(createBusinessStreamPanel());
			tabsheet.addTab(tab1, "Business Stream");
    	}
    	
    	if(authentication != null 
    			&& (authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					|| authentication.hasGrantedAuthority(SecurityConstants.VIEW_WIRETAP_AUTHORITY)))
    	{
    		VerticalLayout tab2 = new VerticalLayout();
    		tab2.setSizeFull();
    		
    		WiretapTab wiretapTab = new WiretapTab
					(this.wiretapDao, this.treeViewBusinessStreamCombo);
			tab2.addComponent(wiretapTab.createWiretapLayout());
			
    		tabsheet.addTab(tab2, "Wiretaps");
    	}
    	
    	if(authentication != null 
    			&& (authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					|| authentication.hasGrantedAuthority(SecurityConstants.VIEW_ERRORS_AUTHORITY)))
    	{
    		VerticalLayout tab3 = new VerticalLayout();
    		tab3.setSizeFull();
    		
    		ErrorOccurrenceTab errorOccurrenceTab = new ErrorOccurrenceTab
					(this.errorReportingService, this.treeViewBusinessStreamCombo);
			tab3.addComponent(errorOccurrenceTab.createCategorisedErrorLayout());
			
    		tabsheet.addTab(tab3, "Errors");
    	}
    	
    	if(authentication != null 
    			&& (authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					|| authentication.hasGrantedAuthority(SecurityConstants.VIEW_EXCLUSION_AUTHORITY)))
    	{
    		final VerticalLayout tab4 = new VerticalLayout();
    		tab4.setSizeFull();
    		final ExclusionsTab actionedExclusionsTab = new ExclusionsTab(this.errorReportingService, 
    				this.exclusionManagementService, this.hospitalManagementService, this.topologyService, 
    				this.serialiserFactory, this.treeViewBusinessStreamCombo);
    		
    		tab4.addComponent(actionedExclusionsTab.createLayout());
    		tabsheet.addTab(tab4, "Exclusions");
    		
    		tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
 	           
                public void selectedTabChange(SelectedTabChangeEvent event) 
                {
                	if(authentication != null 
                			&& (authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
                					|| authentication.hasGrantedAuthority(SecurityConstants.VIEW_EXCLUSION_AUTHORITY)))
                	{
                		actionedExclusionsTab.refreshExcludedEventsTable();
                	}
                }
            });
    	}
		
    	final VerticalLayout tab5 = new VerticalLayout();
		tab5.setSizeFull();
		ActionedExclusionTab actionedExclusionTab = new ActionedExclusionTab
				(this.exclusionManagementService, this.hospitalManagementService,
						this.errorReportingService, this.topologyService, this.serialiserFactory,
						this.treeViewBusinessStreamCombo);
		tab5.addComponent(actionedExclusionTab.createLayout());
		tabsheet.addTab(tab5, "Actioned Exclusions");
	
		
		final VerticalLayout tab6 = new VerticalLayout();
		tab6.setSizeFull();
		tab6.addComponent(this.createSystemEventPanel());
		tabsheet.addTab(tab6, "System Events");
		
		
		// Graph stuff was a demo. Commenting out for now.
//		final VerticalLayout tab7 = new VerticalLayout();
//		tab7.setSizeFull();
//		tab7.addComponent(this.initGraph());
//		tabsheet.addTab(tab7, "Graph");
		
		final VerticalLayout tab8 = new VerticalLayout();
		tab8.setSizeFull();
		CategorisedErrorTab categorisedErrorTab = new CategorisedErrorTab
				(this.errorCategorisationService, this.treeViewBusinessStreamCombo, this.serialiserFactory);
		tab8.addComponent(categorisedErrorTab.createCategorisedErrorLayout());
		tabsheet.addTab(tab8, "Categorised Errors");		

		this.tabsheetPanel.setContent(tabsheet);
	}

	protected void createModuleTreePanel()
	{
		this.topologyTreePanel = new Panel("Topology");
		this.topologyTreePanel.setStyleName("dashboard");
		this.topologyTreePanel.setSizeFull();

		this.moduleTree = new Tree();
		this.moduleTree.setSizeFull();
		this.moduleTree.addActionHandler(this);
		this.moduleTree.setDragMode(TreeDragMode.NODE);
		this.moduleTree.setItemStyleGenerator(new ItemStyleGenerator() 
		{
			@Override
			public String getStyle(Tree source, Object itemId)
			{
				if(itemId instanceof Flow)
				{
					Flow flow = (Flow)itemId;
					
					String state = flowStates.get(flow.getModule().getName() + "-" + flow.getName());
	            	
					logger.info("State = " + state);
					
	    			if(state != null && state.equals(RUNNING))
	    			{
	    				return "greenicon";
	    			}
	    			else if(state != null && state.equals(RECOVERING))
	    			{
	    				return "orangeicon";
	    			}
	    			else if (state != null && state.equals(STOPPED))
	    			{
	    				return "redicon";
	    			}
	    			else if (state != null && state.equals(STOPPED_IN_ERROR))
	    			{
	    				return "redicon";
	    			}
	    			else if (state != null && state.equals(PAUSED))
	    			{
	    				return "yellowicon";
	    			}
				}				
				
				return "";
			}
		});
		
		GridLayout layout = new GridLayout(1, 3);
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth("100%");
		
		this.treeViewBusinessStreamCombo = new ComboBox("Business Stream");
				
		this.treeViewBusinessStreamCombo.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) 
            {
                if(event.getProperty() != null && event.getProperty().getValue() != null)
                {
                	businessStream  = (BusinessStream)event.getProperty().getValue();
                	
                	logger.info("Value changed to business stream: " + businessStream.getName());
                
                	moduleTree.removeAllItems();
                	
                	final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
            	        	.getAttribute(DashboardSessionValueConstants.USER);
            		
            		if(authentication != null 
                			&& authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
                			&& businessStream.getName().equals("All"))
                	{
                		List<Server> servers = TopologyViewPanel.this.topologyService.getAllServers();
                		
                		for(Server server: servers)
                		{
                			Set<Module> modules = server.getModules();
                			
                			refreshFlowStates(modules);

                			TopologyViewPanel.this.moduleTree.addItem(server);
                			TopologyViewPanel.this.moduleTree.setItemCaption(server, server.getName());
                			TopologyViewPanel.this.moduleTree.setChildrenAllowed(server, true);
                			TopologyViewPanel.this.moduleTree.setItemIcon(server, VaadinIcons.SERVER);

                	        for(Module module: modules)
                	        {
                	        	TopologyViewPanel.this.moduleTree.addItem(module);
                	        	TopologyViewPanel.this.moduleTree.setItemCaption(module, module.getName());
                	        	TopologyViewPanel.this.moduleTree.setParent(module, server);
                	        	TopologyViewPanel.this.moduleTree.setChildrenAllowed(module, true);
                	        	TopologyViewPanel.this.moduleTree.setItemIcon(module, VaadinIcons.ARCHIVE);
                	            
                	            Set<Flow> flows = module.getFlows();
                	
                	            for(Flow flow: flows)
                	            {
                	            	TopologyViewPanel.this.moduleTree.addItem(flow);
                	            	TopologyViewPanel.this.moduleTree.setItemCaption(flow, flow.getName());
                	            	TopologyViewPanel.this.moduleTree.setParent(flow, module);
                	            	TopologyViewPanel.this.moduleTree.setChildrenAllowed(flow, true);
                	            	
                	            	String state = flowStates.get(flow.getModule().getName() + "-" + flow.getName());
                	            	                	            	
                	            	TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
                	                
                	                Set<Component> components = flow.getComponents();
                	
                	                for(Component component: components)
                	                {
                	                	TopologyViewPanel.this.moduleTree.addItem(component);
                	                	TopologyViewPanel.this.moduleTree.setParent(component, flow);
                	                	TopologyViewPanel.this.moduleTree.setItemCaption(component, component.getName());
                	                	TopologyViewPanel.this.moduleTree.setChildrenAllowed(component, false);
                	                	
                	                	if(component.isConfigurable())
                	                	{
                	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG);
                	                	}
                	                	else
                	                	{
                	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG_O);
                	                	}
                	                }
                	            }
                	        }
                		}
                	}
                	else if(authentication != null 
                			&& !authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
                			&& businessStream.getName().equals("All"))
                	{
                		List<BusinessStream> businessStreams = topologyService.getAllBusinessStreams();
            			
            			for(BusinessStream businessStream: businessStreams)
            			{
            				if(authentication.canAccessLinkedItem(PolicyLinkTypeConstants.BUSINESS_STREAM_LINK_TYPE, businessStream.getId()))
            				{
            					for(BusinessStreamFlow bsFlow: businessStream.getFlows())
            		        	{
            		        		Server server = bsFlow.getFlow().getModule().getServer();
            		        		Module module = bsFlow.getFlow().getModule();
            		        		Flow flow = bsFlow.getFlow();
            		        		
            		        		if(!moduleTree.containsId(server))
            		        		{
            		            		moduleTree.addItem(server);
            		                    moduleTree.setItemCaption(server, server.getName());
            		                    moduleTree.setChildrenAllowed(server, true);
            		                    moduleTree.setItemIcon(server, VaadinIcons.SERVER);
            		        		}
            		                
            		                moduleTree.addItem(module);
            		                moduleTree.setItemCaption(module, module.getName());
            		                moduleTree.setParent(module, server);
            		                moduleTree.setChildrenAllowed(module, true);
            		                moduleTree.setItemIcon(module, VaadinIcons.ARCHIVE);
            		                
            		                moduleTree.addItem(flow);
            		                moduleTree.setItemCaption(flow, flow.getName());
            		                moduleTree.setParent(flow, module);
            		                moduleTree.setChildrenAllowed(flow, true);
            		                
            		                TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
            		                
            		                Set<Component> components = flow.getComponents();
            		
            		                for(Component component: components)
            		                {
            		                	moduleTree.addItem(component);
            		                	moduleTree.setParent(component, flow);
            		                	moduleTree.setItemCaption(component, component.getName());
            		                	moduleTree.setChildrenAllowed(component, false);
            		                	
            		                	if(component.isConfigurable())
                	                	{
                	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG);
                	                	}
                	                	else
                	                	{
                	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG_O);
                	                	}
            		                }
            		        	}
            	        	}
            			}
                	}
                	else
                	{
	                	for(BusinessStreamFlow bsFlow: businessStream.getFlows())
	                	{
	                		Server server = bsFlow.getFlow().getModule().getServer();
	                		Module module = bsFlow.getFlow().getModule();
	                		Flow flow = bsFlow.getFlow();
	                		
	                		if(!moduleTree.containsId(server))
	                		{
		                		moduleTree.addItem(server);
		                        moduleTree.setItemCaption(server, server.getName());
		                        moduleTree.setChildrenAllowed(server, true);
		                        moduleTree.setItemIcon(server, VaadinIcons.SERVER);
	                		}
	                        
	                        moduleTree.addItem(module);
	    	                moduleTree.setItemCaption(module, module.getName());
	                        moduleTree.setParent(module, server);
	    	                moduleTree.setChildrenAllowed(module, true);
	    	                moduleTree.setItemIcon(module, VaadinIcons.ARCHIVE);
	                        
	                        moduleTree.addItem(flow);
	    	                moduleTree.setItemCaption(flow, flow.getName());
	                        moduleTree.setParent(flow, module);
	    	                moduleTree.setChildrenAllowed(flow, true);
	    	                
	    	                TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	    	                
	    	                Set<Component> components = flow.getComponents();
	    	
	    	                for(Component component: components)
	    	                {
	    	                	moduleTree.addItem(component);
	    	                	moduleTree.setParent(component, flow);
	    	                	moduleTree.setItemCaption(component, component.getName());
	    	                	moduleTree.setChildrenAllowed(component, false);
	    	                	
	    	                	if(component.isConfigurable())
        	                	{
        	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG);
        	                	}
        	                	else
        	                	{
        	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG_O);
        	                	}
	    	                }
	                	}
                	}        	
                }
            }
        });

		layout.addComponent(this.treeViewBusinessStreamCombo);
		
		Button discoverButton = new Button("Discover");
		discoverButton.setStyleName(ValoTheme.BUTTON_SMALL);
		
		discoverButton.addClickListener(new Button.ClickListener() 
    	{
            @SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) 
            {
            	final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
			        	.getAttribute(DashboardSessionValueConstants.USER);
            	
            	try
				{
					topologyService.discover(authentication);
				}
            	catch (DiscoveryException e)
				{
					Notification.show("An error occurred trying to auto discover modules: " 
							+ e.getMessage(), Type.ERROR_MESSAGE);
				}
            	
            	Notification.show("Auto discovery complete!");
            }
        });
		
		Button refreshButton = new Button("Refresh");
		refreshButton.setStyleName(ValoTheme.BUTTON_SMALL);
		refreshButton.addClickListener(new Button.ClickListener() 
    	{
            @SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) 
            {
				refreshTree();
            }
        });
		
		GridLayout buttonLayout = new GridLayout(2, 1);
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(discoverButton);
		buttonLayout.addComponent(refreshButton);
		
		layout.addComponent(buttonLayout);
		layout.addComponent(this.moduleTree);

		this.topologyTreePanel.setContent(layout);
	}

	protected Layout createBusinessStreamPanel()
	{
		this.businessStreamTable = new Table();
		this.businessStreamTable.addContainerProperty("Flow Name", String.class,  null);
		this.businessStreamTable.addContainerProperty("", Button.class,  null);
		this.businessStreamTable.setSizeFull();
//		this.businessStreamTable.setCellStyleGenerator(new IkasanCellStyleGenerator());
		this.businessStreamTable.addStyleName(ValoTheme.TABLE_SMALL);
		this.businessStreamTable.setDragMode(TableDragMode.ROW);
		this.businessStreamTable.setDropHandler(new DropHandler()
		{
			@Override
			public void drop(final DragAndDropEvent dropEvent)
			{
				// criteria verify that this is safe
				logger.info("Trying to drop: " + dropEvent);
				
				final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
			        	.getAttribute(DashboardSessionValueConstants.USER);
				
				if(authentication != null 
		    			&& (!authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
		    					&& !authentication.hasGrantedAuthority(SecurityConstants.MODIFY_BUSINESS_STREAM_AUTHORITY)))
		    	{
					Notification.show("You do not have the privilege to modify a business stream.");
					return;
		    	}

				final DataBoundTransferable t = (DataBoundTransferable) dropEvent
	                        .getTransferable();

				if(t.getItemId() instanceof Flow)
				{
					final Flow sourceContainer = (Flow) t
							.getItemId();
					logger.info("sourceContainer.getText(): "
							+ sourceContainer.getName());
					
					final BusinessStream businessStream = (BusinessStream)TopologyViewPanel.this.businessStreamCombo.getValue();
					BusinessStreamFlowKey key = new BusinessStreamFlowKey();
					key.setBusinessStreamId(businessStream.getId());
					key.setFlowId(sourceContainer.getId());
					final BusinessStreamFlow businessStreamFlow = new BusinessStreamFlow(key);
					businessStreamFlow.setFlow(sourceContainer);
					businessStreamFlow.setOrder(TopologyViewPanel.this.businessStreamTable.getItemIds().size());
					
					if(!businessStream.getFlows().contains(businessStreamFlow))
					{
						businessStream.getFlows().add(businessStreamFlow);
						
						TopologyViewPanel.this.topologyService.saveBusinessStream(businessStream);
						
						Button deleteButton = new Button();
    					Resource deleteIcon = VaadinIcons.CLOSE_CIRCLE_O;
    					deleteButton.setIcon(deleteIcon);
    					deleteButton.setStyleName(ValoTheme.BUTTON_LINK);    					

    					
    					deleteButton.setStyleName(ValoTheme.BUTTON_LINK);
    					deleteButton.setData(businessStreamFlow);
    					
    					// Add the delete functionality to each role that is added
    					deleteButton.addClickListener(new Button.ClickListener() 
    			        {
    			            public void buttonClick(ClickEvent event) 
    			            {		
    			            	logger.info("Attempting to remove businessStreamFlow: " + businessStreamFlow);
    			            	logger.info("Number of flows before: " + businessStream.getFlows().size());
    			            	businessStream.getFlows().remove(businessStreamFlow);
    			            	logger.info("Number of flows after: " + businessStream.getFlows().size());
    			            	
    			            	TopologyViewPanel.this.topologyService.deleteBusinessStreamFlow(businessStreamFlow);
    			            	TopologyViewPanel.this.topologyService.saveBusinessStream(businessStream);
    			            	
    			            	businessStreamTable.removeItem(businessStreamFlow.getFlow());
    			            }
    			        });
						
						businessStreamTable.addItem(new Object[]{sourceContainer.getName(), deleteButton}, sourceContainer);
					}
				}
				else if(t.getItemId() instanceof Module)
				{
					final Module sourceContainer = (Module) t
							.getItemId();
					logger.info("sourceContainer.getText(): "
							+ sourceContainer.getName());
					
					for(Flow flow: sourceContainer.getFlows())
					{
						
						final BusinessStream businessStream = (BusinessStream)TopologyViewPanel.this.businessStreamCombo.getValue();
						BusinessStreamFlowKey key = new BusinessStreamFlowKey();
						key.setBusinessStreamId(businessStream.getId());
						key.setFlowId(flow.getId());
						final BusinessStreamFlow businessStreamFlow = new BusinessStreamFlow(key);
						businessStreamFlow.setFlow(flow);
						businessStreamFlow.setOrder(TopologyViewPanel.this.businessStreamTable.getItemIds().size());
						
						if(!businessStream.getFlows().contains(businessStreamFlow))
						{
							businessStream.getFlows().add(businessStreamFlow);
							
							TopologyViewPanel.this.topologyService.saveBusinessStream(businessStream);
							    					
							Button deleteButton = new Button();
							Resource deleteIcon = VaadinIcons.CLOSE_CIRCLE_O;
							
	    					deleteButton.setIcon(deleteIcon);
	    					deleteButton.setStyleName(ValoTheme.BUTTON_LINK);
	    					deleteButton.setData(businessStreamFlow);
	    					
	    					// Add the delete functionality to each role that is added
	    					deleteButton.addClickListener(new Button.ClickListener() 
	    			        {
	    			            public void buttonClick(ClickEvent event) 
	    			            {		
	    			            	logger.info("Attempting to remove businessStreamFlow: " + businessStreamFlow);
	    			            	logger.info("Number of flows before: " + businessStream.getFlows().size());
	    			            	businessStream.getFlows().remove(businessStreamFlow);
	    			            	logger.info("Number of flows after: " + businessStream.getFlows().size());
	    			            	
	    			            	TopologyViewPanel.this.topologyService.deleteBusinessStreamFlow(businessStreamFlow);
	    			            	TopologyViewPanel.this.topologyService.saveBusinessStream(businessStream);
	    			            	
	    			            	businessStreamTable.removeItem(businessStreamFlow.getFlow());
	    			            }
	    			        });
							
							businessStreamTable.addItem(new Object[]{flow.getName(), deleteButton}, flow);
						}
					}
				}
				else
				{
					Notification.show("Only modules or flows can be dragged to this table.");
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion()
			{
				return AcceptAll.get();
			}
		});

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSizeFull();
		
		GridLayout controlsLayout = new GridLayout(5, 1);
		controlsLayout.setColumnExpandRatio(0, .2f);
		controlsLayout.setColumnExpandRatio(1, .3f);
		controlsLayout.setColumnExpandRatio(2, .05f);
		controlsLayout.setColumnExpandRatio(3, .05f);
		controlsLayout.setColumnExpandRatio(4, .4f);
		
		controlsLayout.setSizeFull();
		Label businessStreamLabel = new Label("Business Stream");
		this.businessStreamCombo = new ComboBox();
		
		this.businessStreamCombo.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if(event.getProperty() != null && event.getProperty().getValue() != null)
                {
                	final BusinessStream businessStream  = (BusinessStream)event.getProperty().getValue();
                	
                	logger.info("Value changed to business stream: " + businessStream.getName());
                	businessStreamTable.removeAllItems();
                	logger.info("Removed all items from table.");

                	for(final BusinessStreamFlow businessStreamFlow: businessStream.getFlows())
                	{
                		logger.info("Adding flow: " + businessStreamFlow);
                		Button deleteButton = new Button();
                		Resource deleteIcon = VaadinIcons.CLOSE_CIRCLE_O;
                		
    					deleteButton.setIcon(deleteIcon);
    					deleteButton.setStyleName(ValoTheme.BUTTON_LINK);
    					
    					// Add the delete functionality to each role that is added
    					deleteButton.addClickListener(new Button.ClickListener() 
    			        {
    			            public void buttonClick(ClickEvent event) 
    			            {		
    			            	logger.info("Attempting to remove businessStreamFlow: " + businessStreamFlow);
    			            	logger.info("Number of flows before: " + businessStream.getFlows().size());
    			            	businessStream.getFlows().remove(businessStreamFlow);
    			            	logger.info("Number of flows after: " + businessStream.getFlows().size());
    			            	
    			            	TopologyViewPanel.this.topologyService.deleteBusinessStreamFlow(businessStreamFlow);
    			            	TopologyViewPanel.this.topologyService.saveBusinessStream(businessStream);
    			            	
    			            	businessStreamTable.removeItem(businessStreamFlow.getFlow());
    			            }
    			        });
    					
    					logger.info("Adding flow: " + businessStreamFlow.getFlow());
    					
    					final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
    				        	.getAttribute(DashboardSessionValueConstants.USER);
    					
    					if(authentication != null 
    			    			&& (!authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    			    					&& !authentication.hasGrantedAuthority(SecurityConstants.MODIFY_BUSINESS_STREAM_AUTHORITY)))
    			    	{
    						deleteButton.setVisible(false);
    			    	}
    					
                		businessStreamTable.addItem(new Object[]{businessStreamFlow.getFlow().getName(), deleteButton}, businessStreamFlow.getFlow());
                	}
                }
            }
        });
		
		controlsLayout.addComponent(businessStreamLabel, 0, 0);
		controlsLayout.addComponent(businessStreamCombo, 1, 0);
		
		Button newButton = new Button("New");
		newButton.setStyleName(ValoTheme.BUTTON_LINK);
    	newButton.addClickListener(new Button.ClickListener() 
    	{
            public void buttonClick(ClickEvent event) 
            {
            	final NewBusinessStreamWindow newBusinessStreamWindow = new NewBusinessStreamWindow();
            	UI.getCurrent().addWindow(newBusinessStreamWindow);
            	
            	newBusinessStreamWindow.addCloseListener(new Window.CloseListener() {
                    // inline close-listener
                    public void windowClose(CloseEvent e) {
                    	TopologyViewPanel.this.topologyService.saveBusinessStream(newBusinessStreamWindow.getBusinessStream());
                    	
                    	TopologyViewPanel.this.businessStreamCombo.addItem(newBusinessStreamWindow.getBusinessStream());
                    	TopologyViewPanel.this.businessStreamCombo.setItemCaption(newBusinessStreamWindow.getBusinessStream(), 
                    			newBusinessStreamWindow.getBusinessStream().getName());
                    	
                    	TopologyViewPanel.this.businessStreamCombo.select(newBusinessStreamWindow.getBusinessStream());
                    	
                    	TopologyViewPanel.this.businessStreamTable.removeAllItems();
                    }
                });
            }
        });
    	
    	final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
	        	.getAttribute(DashboardSessionValueConstants.USER);
		
		if(authentication != null 
    			&& (!authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					&& !authentication.hasGrantedAuthority(SecurityConstants.CREATE_BUSINESS_STREAM_AUTHORITY)))
    	{
			newButton.setVisible(false);
    	}
    	
    	controlsLayout.addComponent(newButton, 2, 0);
    	
    	Button deleteButton = new Button("Delete");
    	deleteButton.setStyleName(ValoTheme.BUTTON_LINK);
    	deleteButton.addClickListener(new Button.ClickListener() 
    	{
            public void buttonClick(ClickEvent event) 
            {
            	
            }
        });
    	
    	if(authentication != null 
    			&& (!authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY)
    					&& !authentication.hasGrantedAuthority(SecurityConstants.DELETE_BUSINESS_STREAM_AUTHORITY)))
    	{
    		deleteButton.setVisible(false);
    	}

    	controlsLayout.addComponent(deleteButton, 3, 0);
    	
    	layout.addComponent(controlsLayout);
    	layout.setExpandRatio(controlsLayout, .07f);
		layout.addComponent(this.businessStreamTable);
		layout.setExpandRatio(this.businessStreamTable, .93f);
		
		return layout;
	}
	
	protected Layout createSystemEventPanel()
	{
		this.systemEventTable = new Table();
		this.systemEventTable.setSizeFull();
		this.systemEventTable.setCellStyleGenerator(new IkasanSmallCellStyleGenerator());
		this.systemEventTable.addContainerProperty("Subject", String.class,  null);
		this.systemEventTable.setColumnExpandRatio("Subject", .3f);
		this.systemEventTable.addContainerProperty("Action", String.class,  null);
		this.systemEventTable.setColumnExpandRatio("Action", .4f);
		this.systemEventTable.addContainerProperty("Actioned By", String.class,  null);
		this.systemEventTable.setColumnExpandRatio("Actioned By", .15f);
		this.systemEventTable.addContainerProperty("Timestamp", String.class,  null);
		this.systemEventTable.setColumnExpandRatio("Timestamp", .15f);
		
		this.systemEventTable.setStyleName("wordwrap-table");
		
		this.systemEventTable.addItemClickListener(new ItemClickEvent.ItemClickListener() 
		{
		    @Override
		    public void itemClick(ItemClickEvent itemClickEvent) 
		    {
//		    	ExclusionEvent exclusionEvent = (ExclusionEvent)itemClickEvent.getItemId();
//		    	ErrorOccurrence errorOccurrence = (ErrorOccurrence)errorReportingService.find(exclusionEvent.getErrorUri());
//		    	ExclusionEventAction action = hospitalManagementService.getExclusionEventActionByErrorUri(exclusionEvent.getErrorUri());
//		    	ExclusionEventViewWindow exclusionEventViewWindow = new ExclusionEventViewWindow(exclusionEvent, errorOccurrence, serialiserFactory
//		    			, action, hospitalManagementService, topologyService);
//		    	
//		    	exclusionEventViewWindow.addCloseListener(new Window.CloseListener()
//		    	{
//		            // inline close-listener
//		            public void windowClose(CloseEvent e) 
//		            {
//		            	refreshExcludedEventsTable();
//		            }
//		        });
//		    
//		    	UI.getCurrent().addWindow(exclusionEventViewWindow);
		    }
		});
		
		
		Button searchButton = new Button("Search");
		searchButton.setStyleName(ValoTheme.BUTTON_SMALL);
		searchButton.addClickListener(new Button.ClickListener() 
    	{
            @SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) 
            {
            	systemEventTable.removeAllItems();
            	
            	PagedSearchResult<SystemEvent> systemEvents = systemEventService.listSystemEvents(0, 10000, "timestamp", true, null, null, systemEventFromDate.getValue()
            			, systemEventToDate.getValue(), null);
            	
            	for(SystemEvent systemEvent: systemEvents.getPagedResults())
            	{
            		SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            	    String timestamp = format.format(systemEvent.getTimestamp());
            	    
            		systemEventTable.addItem(new Object[]{systemEvent.getSubject(), systemEvent.getAction()
        				, systemEvent.getActor(), timestamp}, systemEvent);
            	}
            }
        });
		

		GridLayout layout = new GridLayout(1, 2);			
		
		GridLayout dateSelectLayout = new GridLayout(2, 2);
		dateSelectLayout.setColumnExpandRatio(0, 0.25f);
		dateSelectLayout.setSpacing(true);
		dateSelectLayout.setWidth("50%");
		this.systemEventFromDate = new PopupDateField("From date");
		this.systemEventFromDate.setResolution(Resolution.MINUTE);
		this.systemEventFromDate.setValue(this.getMidnightToday());
		dateSelectLayout.addComponent(this.systemEventFromDate, 0, 0);
		this.systemEventToDate = new PopupDateField("To date");
		this.systemEventToDate.setResolution(Resolution.MINUTE);
		this.systemEventToDate.setValue(this.getTwentyThreeFixtyNineToday());
		dateSelectLayout.addComponent(this.systemEventToDate, 1, 0);
		
		dateSelectLayout.addComponent(searchButton, 0, 1, 1, 1);

		HorizontalLayout hSearchLayout = new HorizontalLayout();
		hSearchLayout.setHeight(75 , Unit.PIXELS);
		hSearchLayout.setWidth("100%");
		hSearchLayout.addComponent(dateSelectLayout);
		layout.addComponent(hSearchLayout);
		HorizontalLayout hErrorTable = new HorizontalLayout();
		hErrorTable.setWidth("100%");
		hErrorTable.setHeight(600, Unit.PIXELS);
		hErrorTable.addComponent(this.systemEventTable);
		layout.addComponent(hErrorTable);
		layout.setSizeFull();
		
		return layout;
	}

	/* (non-Javadoc)
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event)
	{
		refresh();
	}
	
	protected void refresh()
	{
		this.createTabSheet();
		this.refreshTree();
	}
	
	protected void refreshTree()
	{
		this.moduleTree.removeAllItems();
		
		final IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
	        	.getAttribute(DashboardSessionValueConstants.USER);
		
		if(authentication != null 
    			&& authentication.hasGrantedAuthority(SecurityConstants.ALL_AUTHORITY))
    	{
			List<Server> servers = this.topologyService.getAllServers();
			
			for(Server server: servers)
			{
				Set<Module> modules = server.getModules();
	
				this.moduleTree.addItem(server);
	            this.moduleTree.setItemCaption(server, server.getName());
	            this.moduleTree.setChildrenAllowed(server, true);
	            this.moduleTree.setItemIcon(server, VaadinIcons.SERVER);
	
		        for(Module module: modules)
		        {
		        	refreshFlowStates(modules);
		        	
		            this.moduleTree.addItem(module);
		            this.moduleTree.setItemCaption(module, module.getName());
		            this.moduleTree.setParent(module, server);
		            this.moduleTree.setChildrenAllowed(module, true);
		            this.moduleTree.setItemIcon(module, VaadinIcons.ARCHIVE);
		            
		            Set<Flow> flows = module.getFlows();
		
		            for(Flow flow: flows)
		            {
		                this.moduleTree.addItem(flow);
		                this.moduleTree.setItemCaption(flow, flow.getName());
	                    this.moduleTree.setParent(flow, module);
		                this.moduleTree.setChildrenAllowed(flow, true);
		    			
		                TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
		                
		                Set<Component> components = flow.getComponents();
		                
		                for(Component component: components)
		                {
		                	this.moduleTree.addItem(component);
		                	this.moduleTree.setParent(component, flow);
		                	this.moduleTree.setItemCaption(component, component.getName());
		                	this.moduleTree.setChildrenAllowed(component, false);
		                	
		                	if(component.isConfigurable())
    	                	{
    	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG);
    	                	}
    	                	else
    	                	{
    	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG_O);
    	                	}
		                }
		            }
		        }
			}
			
			List<BusinessStream> businessStreams = this.topologyService.getAllBusinessStreams();
			
			if(this.businessStreamCombo != null)
			{
				this.businessStreamCombo.removeAllItems();
				
				for(BusinessStream businessStream: businessStreams)
				{
					this.businessStreamCombo.addItem(businessStream);
					this.businessStreamCombo.setItemCaption(businessStream, businessStream.getName());
				}
			}
			
			this.treeViewBusinessStreamCombo.removeAllItems();
			
			BusinessStream businessStreamAll = new BusinessStream();
			businessStreamAll.setName("All");
			
			this.treeViewBusinessStreamCombo.addItem(businessStreamAll);
			this.treeViewBusinessStreamCombo.setItemCaption(businessStreamAll, businessStreamAll.getName());
			
			for(BusinessStream businessStream: businessStreams)
			{
				this.treeViewBusinessStreamCombo.addItem(businessStream);
				this.treeViewBusinessStreamCombo.setItemCaption(businessStream, businessStream.getName());
			}
			
			this.treeViewBusinessStreamCombo.setValue(businessStreamAll);
    	}
		else
		{
			List<BusinessStream> businessStreams = this.topologyService.getAllBusinessStreams();
			
			if(this.businessStreamCombo != null)
			{
				this.businessStreamCombo.removeAllItems();
			}
			
			this.treeViewBusinessStreamCombo.removeAllItems();
			
			BusinessStream businessStreamAll = new BusinessStream();
			businessStreamAll.setName("All");
			
			this.treeViewBusinessStreamCombo.addItem(businessStreamAll);
			this.treeViewBusinessStreamCombo.setItemCaption(businessStreamAll, businessStreamAll.getName());			
			this.treeViewBusinessStreamCombo.setValue(businessStreamAll);
			
			for(BusinessStream businessStream: businessStreams)
			{
				if(authentication.canAccessLinkedItem(PolicyLinkTypeConstants.BUSINESS_STREAM_LINK_TYPE, businessStream.getId()))
				{
					for(BusinessStreamFlow bsFlow: businessStream.getFlows())
		        	{
		        		Server server = bsFlow.getFlow().getModule().getServer();
		        		Module module = bsFlow.getFlow().getModule();
		        		Flow flow = bsFlow.getFlow();
		        		
		        		if(!moduleTree.containsId(server))
		        		{
		            		moduleTree.addItem(server);
		                    moduleTree.setItemCaption(server, server.getName());
		                    moduleTree.setChildrenAllowed(server, true);
		                    		        		}
		                
		                moduleTree.addItem(module);
		                moduleTree.setItemCaption(module, module.getName());
		                moduleTree.setParent(module, server);
		                moduleTree.setChildrenAllowed(module, true);
		                moduleTree.setItemIcon(module, VaadinIcons.ARCHIVE);
		                
		                moduleTree.addItem(flow);
		                moduleTree.setItemCaption(flow, flow.getName());
		                moduleTree.setParent(flow, module);
		                moduleTree.setChildrenAllowed(flow, true);

		                TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
		                
		                Set<Component> components = flow.getComponents();
		
		                for(Component component: components)
		                {
		                	moduleTree.addItem(component);
		                	moduleTree.setParent(component, flow);
		                	moduleTree.setItemCaption(component, component.getName());
		                	moduleTree.setChildrenAllowed(component, false);
		                	
		                	if(component.isConfigurable())
    	                	{
    	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG);
    	                	}
    	                	else
    	                	{
    	                		TopologyViewPanel.this.moduleTree.setItemIcon(component, VaadinIcons.COG_O);
    	                	}
		                }
		                
		                this.businessStreamCombo.addItem(businessStream);
		    			this.businessStreamCombo.setItemCaption(businessStream, businessStream.getName());
		    			
		    			this.treeViewBusinessStreamCombo.addItem(businessStream);
		    			this.treeViewBusinessStreamCombo.setItemCaption(businessStream, businessStream.getName());
		        	}
	        	}
        	}		
		}
	
		for (Iterator<?> it = this.moduleTree.rootItemIds().iterator(); it.hasNext();) 
		{
			this.moduleTree.expandItemsRecursively(it.next());
		}
		
		for (Iterator<?> it = this.moduleTree.getItemIds().iterator(); it.hasNext();) 
		{
			Object nextItem = it.next();
			if(nextItem instanceof Module)
			{
				this.moduleTree.collapseItemsRecursively(nextItem);
			}
		}
	}

	protected boolean actionFlow(Flow flow, String action)
	{		
		IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
	        	.getAttribute(DashboardSessionValueConstants.USER);
    	
    	HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(authentication.getName(), (String)authentication.getCredentials());
    	
    	ClientConfig clientConfig = new ClientConfig();
    	clientConfig.register(feature) ;
    	
    	Client client = ClientBuilder.newClient(clientConfig);
		
    	String url = "http://" + flow.getModule().getServer().getUrl() + ":" + flow.getModule().getServer().getPort()
				+ flow.getModule().getContextRoot() 
				+ "/rest/moduleControl/controlFlowState/"
				+ flow.getModule().getName() 
	    		+ "/"
	    		+ flow.getName();
    	
	    WebTarget webTarget = client.target(url);
	    Response response = webTarget.request().put(Entity.entity(action, MediaType.APPLICATION_OCTET_STREAM));
	    
	    if(response.getStatus()  == 200)
	    {
	    	Notification.show(flow.getName() + " flow " + action + "!");
	    }  
	    else
	    {
	    	response.bufferEntity();
	        
	        String responseMessage = response.readEntity(String.class);
	        
	    	Notification.show(responseMessage, Type.ERROR_MESSAGE);
	    	return false;
	    }
	    
	    return true;
	}
	

	// TODO removing below functionality. We need to cache flow state data somewhere due to performance.
	protected void refreshFlowStates(Set<Module> modules)
	{
//		for(Module module: modules)
//		{
//			this.flowStates.putAll(this.getFlowStates(module));
//		}
	}

//	protected String getFlowState(Flow flow)
//	{
//		String url = "http://" + flow.getModule().getServer().getUrl() + ":" + flow.getModule().getServer().getPort() 
//				+ flow.getModule().getContextRoot() 
//				+ "/rest/moduleControl/flowState/"
//				+ flow.getModule().getName() + "/"
//				+ flow.getName();
//		
//		IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
//	        	.getAttribute(DashboardSessionValueConstants.USER);
//    	
//    	HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(authentication.getName(), (String)authentication.getCredentials());
//    	
//    	ClientConfig clientConfig = new ClientConfig();
//    	clientConfig.register(feature) ;
//    	
//    	Client client = ClientBuilder.newClient(clientConfig);
//    	
//	    WebTarget webTarget = client.target(url);
//	    
//	    return webTarget.request().get(String.class);
//	}
//	
//	@SuppressWarnings("unchecked")
//	protected HashMap<String, String> getFlowStates(Module module)
//	{
//		HashMap<String, String> results = new HashMap<String, String>();
//		try
//		{
//			String url = "http://" + module.getServer().getUrl() + ":" + module.getServer().getPort() 
//					+ module.getContextRoot() 
//					+ "/rest/moduleControl/flowStates/"
//					+ module.getName();
//			
//			IkasanAuthentication authentication = (IkasanAuthentication)VaadinService.getCurrentRequest().getWrappedSession()
//		        	.getAttribute(DashboardSessionValueConstants.USER);
//	    	
//	    	HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(authentication.getName(), (String)authentication.getCredentials());
//	    	
//	    	ClientConfig clientConfig = new ClientConfig();
//	    	clientConfig.register(feature) ;
//	    	
//	    	Client client = ClientBuilder.newClient(clientConfig);
//	    	
//	    	WebTarget webTarget = client.target(url);
//		    
//	    	results = (HashMap<String, String>)webTarget.request().get(HashMap.class);
//		}
//		catch(Exception e)
//		{
//			return new HashMap<String, String>();
//		}
//	    
//	    return results;
//	}
	
	/* (non-Javadoc)
	 * @see com.vaadin.event.Action.Handler#getActions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Action[] getActions(Object target, Object sender)
	{     
		logger.info("Getting action: " + target + " " + sender);
		
		if(target instanceof Server)
        {
            return serverActions;
        }
		else if(target instanceof Module)
        {
            return moduleActions;
        }
		else if(target instanceof Flow)
        {
			Flow flow = ((Flow)target);
			
			String state = flowStates.get(flow.getModule().getName() + "-" + flow.getName());
			if(state != null && state.equals(RUNNING))
			{
				return this.flowActionsStarted;
			}
			else if(state != null && (state.equals(RUNNING) || state.equals(RECOVERING)))
			{
				return this.flowActionsStarted;
			}
			else if (state != null &&(state.equals(STOPPED) || state.equals(STOPPED_IN_ERROR)))
			{
				return this.flowActionsStopped;
			}
			else if (state != null && state.equals(PAUSED))
			{
				return this.flowActionsPaused;
			}
			else
			{
				return this.flowActions;
			}
        }
		else if(target instanceof Component)
        {
			if(((Component)target).isConfigurable())
			{
				return componentActionsConfigurable;
			}
			else
			{
				return componentActions;
			}
        }
        
           return actionsEmpty;

	}

	/* (non-Javadoc)
	 * @see com.vaadin.event.Action.Handler#handleAction(com.vaadin.event.Action, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target)
	{	        
        Tree senderTree = ((Tree)sender);
        
        if(target != null && target instanceof Component)
        {
        	if(action.equals(CONFIGURE))
        	{
        		this.componentConfigurationWindow.populate(((Component)target));
        		UI.getCurrent().addWindow(this.componentConfigurationWindow);
        	}
        	if(action.equals(WIRETAP))
        	{
        		UI.getCurrent().addWindow(new WiretapConfigurationWindow((Component)target
        			, triggerManagementService));
        	}
        	if(action.equals(ERROR_CATEGORISATION))
        	{
        		Component component = (Component)target;
        		
        		UI.getCurrent().addWindow(new ErrorCategorisationWindow(component.getFlow().getModule().getServer(),
        				component.getFlow().getModule(), component.getFlow(), component, errorCategorisationService));
        	}
        }
        else if(target != null && target instanceof Flow)
        {
        	Flow flow = ((Flow)target);
        	
	        if(action.equals(START))
	        {
	     		if(this.actionFlow(flow, "start"))
	     		{
	     			TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	     		}
	        }
	        else if(action.equals(STOP))
	        {
	        	if(this.actionFlow(flow, "stop"))
	        	{
	        		TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	        	}
	        }
	        else if(action.equals(PAUSE))
	        {
	        	if(this.actionFlow(flow, "pause"))
	        	{
	        		TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	        	}
	        }
	        else if(action.equals(RESUME))
	        {
	        	if(this.actionFlow(flow, "resume"))
	        	{
	        		TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	        	}
	        }
	        else if(action.equals(START_PAUSE))
	        {       	
	        	if(this.actionFlow(flow, "startPause"))
	        	{
	        		TopologyViewPanel.this.moduleTree.setItemIcon(flow, VaadinIcons.AUTOMATION);
	        	}
	        }
	        else if(action.equals(STARTUP_CONTROL))
	        {       	
	        	UI.getCurrent().addWindow(new StartupControlConfigurationWindow());
	        }
	        else if(action.equals(ERROR_CATEGORISATION))
        	{
        		Flow component = (Flow)target;
        		
        		UI.getCurrent().addWindow(new ErrorCategorisationWindow(flow.getModule().getServer(),
        				flow.getModule(), flow, null, errorCategorisationService));
        	}
	        
	        this.refreshFlowStates(flow.getModule().getServer().getModules());
        }
        else if(target != null && target instanceof Module)
        {
        	if(action.equals(ERROR_CATEGORISATION))
        	{
        		Module module = (Module)target;
        		
        		UI.getCurrent().addWindow(new ErrorCategorisationWindow(module.getServer(),
        				module, null, null, errorCategorisationService));
        	}
        }
        else if(target != null && target instanceof Server)
        {
        	if(action.equals(ERROR_CATEGORISATION))
        	{
        		Server server = (Server)target;
        		
        		UI.getCurrent().addWindow(new ErrorCategorisationWindow(server,
        				null, null, null, errorCategorisationService));
        	}
        }
	}
	
	protected Date getMidnightToday()
	{
		Calendar date = new GregorianCalendar();
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime();
	}
	
	protected Date getTwentyThreeFixtyNineToday()
	{
		Calendar date = new GregorianCalendar();
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 23);
		date.set(Calendar.MINUTE, 59);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime();
	}
	
	
	// Graph stuff below was in as a demo. Just commenting out for now.

//    private SimpleGraphRepositoryImpl graphRepo;
//    private GraphExplorer<?, ?> graph;
//    private CssLayout layout;
//	
//
//    public Layout initGraph() {
//    	graphRepo = createGraphRepository();    	
//    	VerticalLayout content = new VerticalLayout();
//    	layout = new CssLayout();
//    	layout.setSizeFull();
//    	ComboBox select = createLayoutSelect();
//    	content.addComponent(select);
//    	content.addComponent(layout);
//    	content.setExpandRatio(layout, 1);
//    	content.setSizeFull();
//    	refreshGraph();
//    	
//    	return content;
//    }
//    
//    private SimpleGraphRepositoryImpl createGraphRepository() {
//    	SimpleGraphRepositoryImpl repo = new SimpleGraphRepositoryImpl();
//    	repo.addNode("node1", "Node 1").setIcon(VaadinIcons.COG);
//    	repo.setHomeNodeId("node1");
//    	
//    	repo.addNode("node2", "Node 2").setIcon(VaadinIcons.COG);
//    	repo.addNode("node3", "Node 3").setIcon(VaadinIcons.COG);
//    	repo.addNode("node4", "Node 4").setIcon(VaadinIcons.COG);
//
//    	repo.addNode("node10", "Node 10").setIcon(VaadinIcons.COG);
//    	repo.addNode("node11", "Node 11").setIcon(VaadinIcons.COG);
//    	repo.addNode("node12", "Node 12").setIcon(VaadinIcons.COG);
//    	repo.addNode("node13", "Node 13").setIcon(VaadinIcons.COG);
//    	repo.addNode("node14", "Node 14").setIcon(VaadinIcons.COG);
//    	repo.addNode("node15", "Node 15").setIcon(VaadinIcons.COG);
//    	repo.addNode("node16", "Node 16").setIcon(VaadinIcons.COG);
//    	repo.addNode("node17", "Node 17").setIcon(VaadinIcons.COG);
//    	repo.addNode("node18", "Node 18").setIcon(VaadinIcons.COG);
//    	repo.addNode("node19", "Node 19").setIcon(VaadinIcons.COG);
//    	repo.addNode("node20", "Node 20").setIcon(VaadinIcons.COG);
//    	repo.addNode("node21", "Node 21").setIcon(VaadinIcons.COG);
//    	repo.addNode("node22", "Node 22").setIcon(VaadinIcons.COG);
//    	repo.addNode("node23", "Node 23").setIcon(VaadinIcons.COG);
//    	repo.addNode("node24", "Node 24").setIcon(VaadinIcons.COG);
//    	repo.addNode("node25", "Node 25").setIcon(VaadinIcons.COG);
//
//    	repo.joinNodes("node1", "node2", "edge12", "Edge 1-2");
//    	repo.joinNodes("node1", "node3", "edge13", "Edge 1-3");
//    	repo.joinNodes("node3", "node4", "edge34", "Edge 3-4");
//
//    	repo.joinNodes("node2", "node10", "edge210", "Edge type A");
//    	repo.joinNodes("node2", "node11", "edge211", "Edge type A");
//    	repo.joinNodes("node2", "node12", "edge212", "Edge type A");
//    	repo.joinNodes("node2", "node13", "edge213", "Edge type A");
//    	repo.joinNodes("node2", "node14", "edge214", "Edge type A");
//    	repo.joinNodes("node2", "node15", "edge215", "Edge type A");
//    	repo.joinNodes("node2", "node16", "edge216", "Edge type A");
//    	repo.joinNodes("node2", "node17", "edge217", "Edge type A");
//    	repo.joinNodes("node2", "node18", "edge218", "Edge type A");
//    	repo.joinNodes("node2", "node19", "edge219", "Edge type A");
//    	repo.joinNodes("node2", "node20", "edge220", "Edge type A");
//    	repo.joinNodes("node2", "node21", "edge221", "Edge type A");
//    	repo.joinNodes("node2", "node22", "edge222", "Edge type B");
//    	repo.joinNodes("node2", "node23", "edge223", "Edge type B");
//    	repo.joinNodes("node2", "node24", "edge224", "Edge type B");
//    	repo.joinNodes("node2", "node25", "edge225", "Edge type C");
//    	
//    	return repo;
//    }
//
//    private ComboBox createLayoutSelect() {
//    	final ComboBox select = new ComboBox("Select layout algorithm");
//    	select.addItem("FR");
//    	select.addItem("Circle");
//    	select.addItem("ISOM");
//    	select.addValueChangeListener(new ValueChangeListener() {
//			
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				if ("FR".equals(select.getValue())) {
//					graph.setLayoutEngine(new JungFRLayoutEngine());
//				} else if ("Circle".equals(select.getValue())) {
//					graph.setLayoutEngine(new JungCircleLayoutEngine());					
//				} if ("ISOM".equals(select.getValue())) {
//					graph.setLayoutEngine(new JungISOMLayoutEngine());
//				}
//				refreshGraph();
//			}
//		});
//    	return select;
//    }
//    
//    private void refreshGraph() {
//    	layout.removeAllComponents();
//        graph = new GraphExplorer<NodeImpl, ArcImpl>(graphRepo);
//        graph.setSizeFull();
//        layout.addComponent(graph);
//    }
}