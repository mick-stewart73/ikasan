package org.ikasan.wiretap.service;

import org.ikasan.spec.flow.FlowEvent;
import org.ikasan.spec.housekeeping.HousekeepService;
import org.ikasan.spec.module.ModuleService;
import org.ikasan.spec.search.PagedSearchResult;
import org.ikasan.spec.solr.SolrService;
import org.ikasan.spec.wiretap.WiretapDao;
import org.ikasan.spec.wiretap.WiretapEvent;
import org.ikasan.spec.wiretap.WiretapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;

/**
 * Created by amajewski on 23/09/2017.
 */
public class SolrWiretapServiceImpl implements HousekeepService, SolrService<WiretapEvent>, WiretapService<FlowEvent,PagedSearchResult<WiretapEvent>> {

    /** Logger for this class */
    private static Logger logger = LoggerFactory.getLogger(SolrWiretapServiceImpl.class);

    /**
     * Data access object for the persistence of <code>WiretapFlowEvent</code>
     */
    private WiretapDao wiretapDao;

    /**
     * Container for modules
     */
    private ModuleService moduleService;

    /**
     * Constructor
     *
     * @param wiretapDao - The wire tap DAO
     */
    public SolrWiretapServiceImpl(WiretapDao wiretapDao,ModuleService moduleService) {
        this.wiretapDao = wiretapDao;
        if (wiretapDao == null) {
            throw new IllegalArgumentException("wiretapDao cannot be 'null'");
        }

        this.moduleService = moduleService;
        if(moduleService == null)
        {
            throw new IllegalArgumentException("moduleService cannot be 'null'");
        }
    }

    @Override
    public void save(WiretapEvent wiretapEvent) {
        wiretapDao.save(wiretapEvent);
    }


    /**
     * (non-Javadoc)
     *
     * @see org.ikasan.spec.management.HousekeeperService#housekeepablesExist()
     */
    @Override
    public boolean housekeepablesExist() {
        return this.wiretapDao.housekeepablesExist();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.ikasan.spec.housekeeping.HousekeepService#setHousekeepingBatchSize(Integer)
     */
    @Override
    public void setHousekeepingBatchSize(Integer housekeepingBatchSize) {
        wiretapDao.setBatchHousekeepDelete(true);
        wiretapDao.setHousekeepingBatchSize(housekeepingBatchSize);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.ikasan.spec.housekeeping.HousekeepService#setTransactionBatchSize(Integer)
     */
    @Override
    public void setTransactionBatchSize(Integer transactionBatchSize) {
        wiretapDao.setTransactionBatchSize(transactionBatchSize);
    }

    /**
     * Allows previously stored FlowEvents to be searched for.
     *
     * @param pageNo - page index into the greater result set
     * @param pageSize - how many results to return in the result
     * @param orderBy - The field to order by
     * @param orderAscending - Ascending flag
     * @param moduleNames - Set of names of modules to include in search - must
     *            contain at least one moduleName
     * @param moduleFlow - The name of Flow internal to the Module
     * @param componentName - The name of the component
     * @param eventId - The FlowEvent Id
     * @param payloadId - The Payload Id
     * @param fromDate - Include only events after fromDate
     * @param untilDate - Include only events before untilDate
     * @param payloadContent - The Payload content
     *
     * @throws IllegalArgumentException - if moduleNames is null or empty
     * @return List of <code>WiretapFlowEventHeader</code> representing the result
     *         of the search
     */
    public PagedSearchResult<WiretapEvent> findWiretapEvents(int pageNo, int pageSize, String orderBy, boolean orderAscending, Set<String> moduleNames,
                                                             String moduleFlow, String componentName, String eventId, String payloadId, Date fromDate, Date untilDate, String payloadContent)
    {
        if (pageNo < 0)
        {
            throw new IllegalArgumentException("pageNo must be >= 0");
        }
        if (pageSize < 1)
        {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        return wiretapDao.findWiretapEvents(pageNo, pageSize, orderBy, orderAscending, moduleNames, moduleFlow, componentName, eventId, payloadId, fromDate, untilDate,
                payloadContent);
    }

    @Override
    public PagedSearchResult<WiretapEvent> findWiretapEvents(int pageNo, int pageSize, String orderBy, boolean orderAscending, Set<String> moduleNames, Set<String> moduleFlow, Set<String> componentName, String eventId, String payloadId, Date fromDate, Date untilDate, String payloadContent)
    {
        if (pageNo < 0)
        {
            throw new IllegalArgumentException("pageNo must be >= 0");
        }
        if (pageSize < 1)
        {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        return wiretapDao.findWiretapEvents(pageNo, pageSize, orderBy, orderAscending, moduleNames, moduleFlow, componentName, eventId, payloadId, fromDate, untilDate,
                payloadContent);
    }

    /**
     * Get a wireTap event given its Id
     *
     * @param wiretapEventId - The Id to search with
     * @return The WiretapFlowEvent
     */
    public WiretapEvent getWiretapEvent(Long wiretapEventId)
    {
        WiretapEvent wiretapEvent = wiretapDao.findById(wiretapEventId);
        if (wiretapEvent != null)
        {
            // before returning wiretapFlowEvent, check that we can access the
            // associated module
            // this is an easier security check that access controlling every
            // WiretapFlowEvent individually
            // If the user can 'read' the module, then they are allowed to read
            // its associated WiretapFlowEvents
            moduleService.getModule(wiretapEvent.getModuleName());
        }
        return wiretapEvent;
    }

    @Override
    public void tapEvent(FlowEvent event, String componentName, String moduleName, String flowName, Long timeToLive) {
        throw new UnsupportedOperationException();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.ikasan.spec.housekeeping.HousekeepService#housekeep()
     */

    @Override
    public void housekeep() {
        logger.info("wiretap housekeep called");
        long startTime = System.currentTimeMillis();
        wiretapDao.deleteAllExpired();
        long endTime = System.currentTimeMillis();
        logger.info("wiretap housekeep completed in [" + (endTime - startTime) + " ms]");
    }


}