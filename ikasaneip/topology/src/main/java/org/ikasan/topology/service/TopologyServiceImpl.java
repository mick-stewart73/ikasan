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
package org.ikasan.topology.service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import org.ikasan.security.service.authentication.IkasanAuthentication;
import org.ikasan.topology.dao.TopologyDao;
import org.ikasan.topology.model.BusinessStream;
import org.ikasan.topology.model.BusinessStreamFlow;
import org.ikasan.topology.model.Component;
import org.ikasan.topology.model.Filter;
import org.ikasan.topology.model.FilterComponent;
import org.ikasan.topology.model.FilterComponentKey;
import org.ikasan.topology.model.Flow;
import org.ikasan.topology.model.Module;
import org.ikasan.topology.model.Notification;
import org.ikasan.topology.model.RoleFilter;
import org.ikasan.topology.model.Server;

import org.ikasan.topology.exception.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Ikasan Development Team
 *
 */
public class TopologyServiceImpl implements TopologyService
{
	private static Logger logger = LoggerFactory.getLogger(TopologyServiceImpl.class);


	private TopologyDao topologyDao;

	private RestTemplate restTemplate;
	/**
	 * Constructor
	 * 
	 * @param topologyDao
	 */
	public TopologyServiceImpl(TopologyDao topologyDao)
	{
		this.topologyDao = topologyDao;
		if(this.topologyDao == null)
		{
			throw new IllegalArgumentException("topologyDao cannot be null!");
		}

        restTemplate = new RestTemplate();
		restTemplate.setMessageConverters(
				Arrays.asList(
						new MappingJackson2HttpMessageConverter()
						,new StringHttpMessageConverter()));

	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllServers()
	 */
	@Override
	public List<Server> getAllServers()
	{
		return this.topologyDao.getAllServers();
	}

	/* (non-Javadoc)
	 * @see TopologyService#save(org.ikasan.topology.model.Server)
	 */
	@Override
	public void save(Server server)
	{
		this.topologyDao.save(server);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllModules()
	 */
	@Override
	public List<Module> getAllModules()
	{
		return this.topologyDao.getAllModules();
	}

	/* (non-Javadoc)
	 * @see TopologyService#save(org.ikasan.topology.model.Module)
	 */
	@Override
	public void save(Module module)
	{
		this.topologyDao.save(module);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllFlows()
	 */
	@Override
	public List<Flow> getAllFlows()
	{
		return this.topologyDao.getAllFlows();
	}

	/* (non-Javadoc)
	 * @see TopologyService#save(org.ikasan.topology.model.Flow)
	 */
	@Override
	public void save(Flow flow)
	{
		this.topologyDao.save(flow);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllBusinessStreams()
	 */
	@Override
	public List<BusinessStream> getAllBusinessStreams()
	{
		return this.topologyDao.getAllBusinessStreams();
	}


	/* (non-Javadoc)
	 * @see TopologyService#save(org.ikasan.topology.model.Component)
	 */
	@Override
	public void save(Component component)
	{
		this.topologyDao.save(component);
	}
	
	/* (non-Javadoc)
	 * @see TopologyService#saveBusinessStream(org.ikasan.topology.model.BusinessStream)
	 */
	@Override
	public void saveBusinessStream(BusinessStream businessStream)
	{
		this.topologyDao.saveBusinessStream(businessStream);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getBusinessStreamsByUserId(java.lang.Long)
	 */
	@Override
	public List<BusinessStream> getBusinessStreamsByUserId(Long userId)
	{
		return this.topologyDao.getBusinessStreamsByUserId(userId);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getFlowsByServerIdAndModuleId(java.lang.Long, java.lang.Long)
	 */
	@Override
	public List<Flow> getFlowsByServerIdAndModuleId(Long serverId, Long moduleId)
	{
		return this.topologyDao.getFlowsByServerIdAndModuleId(serverId, moduleId);
	}

	/* (non-Javadoc)
	 * @see TopologyService#deleteBusinessStreamFlow(org.ikasan.topology.model.BusinessStreamFlow)
	 */
	@Override
	public void deleteBusinessStreamFlow(BusinessStreamFlow businessStreamFlow)
	{
		this.topologyDao.deleteBusinessStreamFlow(businessStreamFlow);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getModuleByName(java.lang.String)
	 */
	@Override
	public Module getModuleByName(String name)
	{
		return this.topologyDao.getModuleByName(name);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getBusinessStreamsByUserId(java.util.List)
	 */
	@Override
	public List<BusinessStream> getBusinessStreamsByUserId(List<Long> ids)
	{
		return this.topologyDao.getBusinessStreamsByUserId(ids);
	}

	/* (non-Javadoc)
	 * @see TopologyService#discover()
	 */
	@Override
	public void discover(List<Server> servers, IkasanAuthentication authentication) throws DiscoveryException
	{
    	// Firstly sort out the server module relationships
		for(Server server: servers)
		{
			List<Module> modules = this.topologyDao.getAllModules();
			
			Set<Module> moduleSet = new HashSet<Module>();
			
			for(Module module: modules)
			{	

				String url = server.getUrl() + ":" + server.getPort()
						+ module.getContextRoot().replace(" ", "%20")
						+ "/rest/discovery/flows/"
						+ module.getName().replace(" ", "%20");

				try
				{
					logger.info("Module Discovery REST ["+url+"]");

					HttpEntity request = initRequest(authentication.getName(), (String)authentication.getCredentials());
					ResponseEntity<List<Flow>> flowResponse = restTemplate.exchange(new URI(url), HttpMethod.GET,request,new ParameterizedTypeReference<List<Flow>>() {});

                    logger.info("Successfully discovered module using URL: " + url
                        + ". Server =  " + server);

                    module.setServer(server);

                    moduleSet.add(module);

                    server.setModules(moduleSet);

                    this.topologyDao.save(server);
			    }
			    catch(Exception e)
			    {
			    	// We may not find the module on the server so just move on to the next module.
			    	logger.info("Caught exception attempting to discover module with the following URL: " + url
			    			+ ". Ignoring and moving on to next module. Exception message: " + e.getMessage());
			    }

			}
		}
		
		// Now sort out the flows on servers
		servers.forEach(server ->
            this.topologyDao.getAllModules().forEach(module -> discoverOverRest(authentication,server,module))
        );
		this.cleanup();
	}



    /**
     * Gets Flow and component information over REST from given module on given server using provider authentication
     *
     * @param authentication credentials to be used by REST
     * @param server server details
     * @param module module information
     */
	private void discoverOverRest(IkasanAuthentication authentication, Server server, Module module)
    {
        String url = server.getUrl() + ":" + server.getPort()
            + module.getContextRoot().replace(" ", "%20")
            + "/rest/discovery/flows/"
            + module.getName().replace(" ", "%20");
        ResponseEntity<List<Flow>> flowResponse;
        try
        {
            logger.info("Flow Discovery REST [" + url + "]");
            HttpEntity request = initRequest(authentication.getName(), (String) authentication.getCredentials());
            flowResponse = restTemplate.exchange(new URI(url), HttpMethod.GET, request, new ParameterizedTypeReference<List<Flow>>()
            {
            });
            logger.info("Successfully discovered flows using URL: " + url + ". Server =  " + server);
            discover(server, module, flowResponse.getBody());
        }
        catch (Exception e)
        {
            // We may not find the module on the server so just move on to the next module.
            logger.info("Caught exception attempting to discover module with the following URL: " + url
                + ". Ignoring and moving on to next module. Exception message: " + e.getMessage());
        }
    }

    /**
     * Creates the Module metadata in IkasanModule DB table for the module or updates existing metadata
     *
     * @param server
     * @param applicationName
     * @param module
     */
    public void initialiseModuleMetaData(Server server, String applicationName, Module module)
    {
        try
        {
            Optional<Server> existingServer = Optional.ofNullable(server);

            org.ikasan.topology.model.Module existingModule = getModuleByName(module.getName());
            if (existingModule == null)
            {
                logger.info("module does not exist [" + module.getName() + "], creating...");
                existingModule = new org.ikasan.topology.model.Module(module.getName(), applicationName,
                    module.getDescription() == null ? "" : module.getDescription() , module.getVersion(), null, null);
                if (existingServer.isPresent())
                {
                    existingModule.setServer(existingServer.get());
                }
                save(existingModule);
                createMetadata(module, existingModule);
            }
            else
            {
                updateMetadata(module, existingModule);
                if (existingServer.isPresent())
                {
                    logger.info(
                        "Updating [" + module.getName() + "] server instance to  [" + existingServer.get().getUrl() + "].");
                    existingModule.setServer(existingServer.get());
                    save(existingModule);
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Error encountered while performing local discovery.", e);
        }
    }

    /**
     * Helper method to store meta-data about module into sa topology related tables. This method only executes when
     * meta-data about the module was never stored before.
     *
     * @param module module runtime instance
     * @param moduleDB topology view of the module
     */
    private void createMetadata(Module module, org.ikasan.topology.model.Module moduleDB)
    {
        try
        {
            module.getFlows().forEach(topologyFlow ->
            {
                topologyFlow.setModule(moduleDB);

                // We only want to add distinct components per flow. The convenience API repeats components
                // if they appear in multiple routes in a Flow.
                topologyFlow.setComponents(new HashSet<>(distinctComponents(topologyFlow.getName(), topologyFlow.getComponents())));

                topologyFlow.getComponents().forEach(component -> component.setFlow(topologyFlow));

                save(topologyFlow);
                logger.info("Saving flow with components [" + topologyFlow.getName() + "]");
            });
        }
        catch (Exception e)
        {
            logger.warn("Error encountered while performing local discovery.", e);
        }
    }


    /**
     * Helper method to store meta-data about module into sa topology related tables. This method only executes when
     * existing meta-data and new meta-data need to be reconciled.
     *
     * @param module module runtime instance
     * @param existingModule topology view of the module
     */
    private void updateMetadata(Module module, org.ikasan.topology.model.Module existingModule)
    {
        try
        {
            discover(existingModule.getServer(), existingModule, new ArrayList(module.getFlows()));
        }
        catch (Exception e)
        {
            logger.warn("Error encountered while performing local discovery.", e);
        }
    }

	@Override
	public void discover(Server server, Module module, List<Flow> flows) throws DiscoveryException
	{
		logger.info("Discovery - updating module ["+module.getName()+"] flows information.");

		Long serverId = server != null ? server.getId(): null;

		Set<Flow> flowSet = discoverFlows(serverId, module, flows);

		module.addFlows(flowSet);

		this.topologyDao.save(module);

		logger.info("Discovery - updated module ["+module.getName()+"] flows information.");

		List<String> discoveredFlowNames = discoveredFlowNames(flows);

		this.cleanUpFlows(module, serverId, module.getId(), discoveredFlowNames);
	}

	protected Set<Flow> discoverFlows(Long serverId, Module module, List<Flow> flowList)
	{
		Set<Flow> flowSet = new HashSet<>();

		for(Flow flow: flowList)
        {
            List<String> discoveredComponentNames = discoveredComponentNames(flow);

            List<Component> components = distinctComponents(flow.getName(), flow.getComponents());
            logger.debug("Loading dbFlow using: serverId= " + serverId + " moduleId = " + module.getId()
                    + " flow name = " + flow.getName());

            Flow dbFlow = this.topologyDao.getFlowByServerIdModuleIdAndFlowname(serverId, module.getId(), flow.getName());

            logger.debug("Loaded dbFlow: " + dbFlow);

            if(dbFlow != null)
            {
                dbFlow.setOrder(flow.getOrder());
                dbFlow.setConfigurable(flow.isConfigurable());
                dbFlow.setConfigurationId(flow.getConfigurationId());
                flow = dbFlow;
            }
            else
            {
                flow.setModule(module);
                save(flow);
            }

            flow.setModule(module);

            flowSet.add(flow);

            Set<Component> componentSet = new HashSet<Component>();

            for(Component component: components)
            {
                component = getComponent(flow.getComponents(), component);
                component.setFlow(flow);

                logger.debug("Saving component: " + component.getName());

                this.topologyDao.save(component);

                componentSet.add(component);
            }

            flow.setComponents(componentSet);

            this.cleanUpComponents(serverId, module.getId(), flow.getName(), discoveredComponentNames);
        }
		return flowSet;
	}

	private List<String> discoveredFlowNames(List<Flow> flows)
    {
		return flows.stream().map(flow -> flow.getName()).collect(Collectors.toList());
	}

	private List<String> discoveredComponentNames(Flow flow)
    {
		return flow.getComponents().stream().map(component -> component.getName()).distinct().collect(Collectors.toList());
	}

    private List<Component> distinctComponents(String flowName, Set<Component> components)
    {
        logger.debug("Filtering distinct components for flow[{}]. Before:[{}].", flowName, components.size());
        List<Component> filtered = components.stream().filter( distinctByKey(component -> component.getName())).collect(Collectors.toList());
        logger.debug("After: [{}]", filtered.size());
        return filtered;
    }

    private <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
    {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    protected void cleanup(){
        this.cleanUpComponents();
        this.cleanUpFlows();
        this.cleanUpModules();
    }

	protected void cleanUpModules()
	{
		List<Module> modules = this.topologyDao.getAllModules();

		for(Module module: modules)
		{
			if(module.getServer() == null)
			{
				this.topologyDao.delete(module);
			}
		}
	}

	protected void cleanUpFlows()
	{
		List<Flow> flows = this.topologyDao.getAllFlows();

		for(Flow flow: flows)
		{
			if(flow.getModule() == null)
			{
				// we need to delete any references to the flow before deleting it.
				this.topologyDao.deleteBusinessStreamFlowByFlowId(flow.getId());
				this.topologyDao.delete(flow);
			}
		}
	}

	protected void cleanUpComponents()
	{
		List<Component> components = this.topologyDao.getAllComponents();

		for(Component component: components)
		{
			if(component.getFlow() == null)
			{
				// we need to delete any references to the component before deleting it.
				this.topologyDao.deleteFilterComponentsByComponentId(component.getId());
				this.topologyDao.delete(component);
			}
		}
	}

	protected void cleanUpFlows(Module module, Long serverId, Long moduleId, List<String> discoveredFlowNames)
	{
		logger.info("Discovery - cleaning up old flow references from [" + module.getName() + "].");
		List<Flow> dbFlows = this.topologyDao.getFlowsByServerIdAndModuleId(serverId, moduleId);
		dbFlows.stream().filter(flow -> !discoveredFlowNames.contains(flow.getName()))
				.forEach(flow ->
		{
			Set<Component> copyComponents = new HashSet<Component>();
			for (Component component : flow.getComponents())
			{
				copyComponents.add(component);
			}
			for (Component component : copyComponents)
			{
				flow.getComponents().remove(component);
				component.setFlow(null);
				this.topologyDao.delete(component);
			}
			module.getFlows().remove(flow);
			flow.getModule().getFlows().remove(flow);
			flow.setModule(null);
			// we need to delete any references to the flow before deleting it.
			this.topologyDao.deleteBusinessStreamFlowByFlowId(flow.getId());
			this.topologyDao.delete(flow);
		});
		logger.info("Discovery - old flow references cleaned up [" + module.getName() + "].");
	}
	
	protected void cleanUpComponents(Long serverId, Long moduleId, String flowName, List<String> discoveredComponentNames)
	{
		logger.debug("Getting components to delete:" + serverId + " " + moduleId + " " + flowName +" discoveredComponent["+discoveredComponentNames+"]" );

		List<Component> components = this.topologyDao.getComponentsByServerIdModuleIdAndFlownameAndComponentNameNotIn
				(serverId, moduleId, flowName, discoveredComponentNames);
				
		Set<Component> copyComponents =  new HashSet<Component>();
		
		for(Component component: components)
		{
			copyComponents.add(component);
		}
		
		for(Component component: copyComponents)
		{
			logger.debug("Cleaning up component:" + component);
			
			component.getFlow().getComponents().remove(component);
			component.setFlow(null);
			
			// we need to delete any references to the component before deleting it. 
			this.topologyDao.deleteFilterComponentsByComponentId(component.getId());
			
			this.topologyDao.delete(component);
		}
	}
	
	protected Component getComponent(Set<Component> components, Component component)
	{
		Iterator<Component> componentsItr = components.iterator();
		
		while(componentsItr.hasNext())
		{
			Component c = componentsItr.next();
			
			if(c.getName().trim().equals(component.getName().trim()))
			{
				c.setConfigurationId(component.getConfigurationId());
				return c;
			}
		}
		
		return component;
	}


	private HttpEntity initRequest(String user, String password){
		HttpHeaders headers = new HttpHeaders();
		if(user!=null && password !=null){
			String credentials = user + ":" +password;
			String encodedCridentials =  new String(Base64.encodeBase64(credentials.getBytes()));
			headers.set(HttpHeaders.AUTHORIZATION, "Basic "+encodedCridentials);
		}
		return new HttpEntity(headers);

	}
	/* (non-Javadoc)
	 * @see TopologyService#deleteBusinessStream(org.ikasan.topology.model.BusinessStream)
	 */
	@Override
	public void deleteBusinessStream(BusinessStream businessStream)
	{
		this.topologyDao.deleteBusinessStream(businessStream);
	}

	/* (non-Javadoc)
	 * @see TopologyService#createFilter(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public Filter createFilter(String name, String description, String createdBy, Set<Component> components)
	{
		Filter filter = new Filter(name, description, createdBy);
		filter.setName(name);
		filter.setDescription(description);
		
		this.topologyDao.saveFilter(filter);
		
		Set<FilterComponent> filterComponents = new HashSet<FilterComponent>();
		
		for(Component component: components)
		{
			FilterComponent fc = new FilterComponent(new FilterComponentKey(filter.getId(), component.getId()));
			fc.setComponent(component);
			
			filterComponents.add(fc);
		}
		
		filter.setComponents(filterComponents);
		
		this.topologyDao.saveFilter(filter);
		
		return filter;
	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllFilters()
	 */
	@Override
	public List<Filter> getAllFilters()
	{
		return this.topologyDao.getAllFilters();
	}

	/* (non-Javadoc)
	 * @see TopologyService#saveFilter(org.ikasan.topology.model.Filter)
	 */
	@Override
	public void saveFilter(Filter filter)
	{
		filter.setUpdatedDateTime(new Date(System.currentTimeMillis()));
		this.topologyDao.saveFilter(filter);
	}

	/* (non-Javadoc)
	 * @see TopologyService#deleteFilter(org.ikasan.topology.model.Filter)
	 */
	@Override
	public void deleteFilter(Filter filter)
	{
		this.topologyDao.deleteFilter(filter);
	}

	/* (non-Javadoc)
	 * @see TopologyService#saveRoleFilter(org.ikasan.topology.model.RoleFilter)
	 */
	@Override
	public void saveRoleFilter(RoleFilter roleFilter)
	{
		this.topologyDao.saveRoleFilter(roleFilter);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getRoleFilters(java.lang.Long)
	 */
	@Override
	public List<RoleFilter> getRoleFilters(List<Long> roleIds)
	{
		return this.topologyDao.getRoleFiltersByRoleId(roleIds);
	}

	/* (non-Javadoc)
	 * @see TopologyService#deleteRoleFilter(org.ikasan.topology.model.RoleFilter)
	 */
	@Override
	public void deleteRoleFilter(RoleFilter roleFilter)
	{
		this.topologyDao.deleteRoleFilter(roleFilter);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getRoleFilterByFilterId(java.lang.Long)
	 */
	@Override
	public RoleFilter getRoleFilterByFilterId(Long filterId)
	{
		return this.topologyDao.getRoleFilterByFilterId(filterId);
	}

	/* (non-Javadoc)
	 * @see TopologyService#deleteFilterComponents(java.lang.Long)
	 */
	@Override
	public void deleteFilterComponents(Long filterId)
	{
		this.topologyDao.deleteFilterComponentsByFilterId(filterId);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getFilterByName(java.lang.String)
	 */
	@Override
	public Filter getFilterByName(String name)
	{
		return this.topologyDao.getFilterByName(name);
	}

	/* (non-Javadoc)
	 * @see TopologyService#save(org.ikasan.topology.model.Notification)
	 */
	@Override
	public void save(Notification notification)
	{
		this.topologyDao.save(notification);
	}

	/* (non-Javadoc)
	 * @see TopologyService#delete(org.ikasan.topology.model.Notification)
	 */
	@Override
	public void delete(Notification notification)
	{
		this.topologyDao.delete(notification);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getNotificationByName(java.lang.String)
	 */
	@Override
	public Notification getNotificationByName(String name)
	{
		return this.topologyDao.getNotificationByName(name);
	}

	/* (non-Javadoc)
	 * @see TopologyService#getAllNotifications()
	 */
	@Override
	public List<Notification> getAllNotifications()
	{
		return this.topologyDao.getAllNotifications();
	}
}
