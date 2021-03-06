package org.ikasan.dashboard.ui.administration.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.ikasan.dashboard.ui.administration.filter.RoleFilter;
import org.ikasan.dashboard.ui.general.component.AbstractCloseableResizableDialog;
import org.ikasan.dashboard.ui.general.component.FilteringGrid;
import org.ikasan.dashboard.ui.util.SystemEventConstants;
import org.ikasan.dashboard.ui.util.SystemEventLogger;
import org.ikasan.security.model.Policy;
import org.ikasan.security.model.Role;
import org.ikasan.security.service.SecurityService;

import java.util.Collection;
import java.util.List;

public class SelectRoleForPolicyDialog extends AbstractCloseableResizableDialog
{
    private Policy policy;
    private SecurityService securityService;
    private SystemEventLogger systemEventLogger;
    private FilteringGrid<Role> roleFilteringGrid;

    public SelectRoleForPolicyDialog(Policy policy, SecurityService securityService, SystemEventLogger systemEventLogger,
                                     FilteringGrid<Role> roleFilteringGrid)
    {
        this.policy = policy;
        if(this.policy == null)
        {
            throw new IllegalArgumentException("policy cannot be null!");
        }
        this.securityService = securityService;
        if(this.securityService == null)
        {
            throw new IllegalArgumentException("securityService cannot be null!");
        }
        this.systemEventLogger = systemEventLogger;
        if(this.systemEventLogger == null)
        {
            throw new IllegalArgumentException("systemEventLogger cannot be null!");
        }
        this.roleFilteringGrid = roleFilteringGrid;
        if(this.roleFilteringGrid == null)
        {
            throw new IllegalArgumentException("roleFilteringGrid cannot be null!");
        }

        init();
    }

    private void init()
    {
        super.title.setText(getTranslation("label.select-role", UI.getCurrent().getLocale()));
        H3 selectRoleLabel = new H3(getTranslation("label.select-role", UI.getCurrent().getLocale()));

        List<Role> roleList = this.securityService.getAllRoles();
        roleList.removeAll(policy.getRoles());

        RoleFilter roleFilter = new RoleFilter();

        FilteringGrid<Role> roleGrid = new FilteringGrid<>(roleFilter);
        roleGrid.setItems(roleList);
        roleGrid.setSizeFull();;

        roleGrid.setClassName("my-grid");
        roleGrid.addColumn(Role::getName).setHeader(getTranslation("table-header.role-name", UI.getCurrent().getLocale(), null)).setKey("name").setSortable(true).setFlexGrow(2);
        roleGrid.addColumn(Role::getDescription).setHeader(getTranslation("table-header.role-description", UI.getCurrent().getLocale(), null)).setKey("description").setSortable(true).setFlexGrow(5);

        roleGrid.addItemDoubleClickListener((ComponentEventListener<ItemDoubleClickEvent<Role>>) roleItemDoubleClickEvent ->
        {
            policy.getRoles().add(roleItemDoubleClickEvent.getItem());

            this.securityService.savePolicy(policy);

            String action = String.format("Policy [%s] added to role [%s].", policy.getName(), roleItemDoubleClickEvent.getItem().getName());

            this.systemEventLogger.logEvent(SystemEventConstants.DASHBOARD_PRINCIPAL_ROLE_CHANGED_CONSTANTS, action,null);

            Collection<Role> items = this.roleFilteringGrid.getItems();
            items.add(roleItemDoubleClickEvent.getItem());

            this.roleFilteringGrid.setItems(items);
            this.roleFilteringGrid.getDataProvider().refreshAll();

            roleGrid.getItems().remove(roleItemDoubleClickEvent.getItem());
            roleGrid.getDataProvider().refreshAll();
        });

        HeaderRow hr = roleGrid.appendHeaderRow();
        roleGrid.addGridFiltering(hr, roleFilter::setNameFilter, "name");
        roleGrid.addGridFiltering(hr, roleFilter::setDescriptionFilter, "description");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(selectRoleLabel, roleGrid);

        this.content.add(layout);
        super.setWidth("700px");
        super.setHeight("600px");
    }
}
