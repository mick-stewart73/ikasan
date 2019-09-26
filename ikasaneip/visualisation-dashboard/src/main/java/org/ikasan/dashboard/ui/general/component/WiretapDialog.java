package org.ikasan.dashboard.ui.general.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import org.ikasan.dashboard.ui.util.DateFormatter;
import org.ikasan.solr.model.IkasanSolrDocument;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;

public class WiretapDialog extends AbstractEntityViewDialog<IkasanSolrDocument>
{
    private TextField moduleNameTf;
    private TextField componentNameTf;
    private TextField flowNameTf;
    private TextField eventIdTf;
    private TextField dateTimeTf;

    private StreamResource streamResource;
    private  FileDownloadWrapper buttonWrapper;


    public WiretapDialog()
    {
        moduleNameTf = new TextField(getTranslation("text-field.module-name", UI.getCurrent().getLocale(), null));
        flowNameTf = new TextField(getTranslation("text-field.flow-name", UI.getCurrent().getLocale(), null));
        componentNameTf = new TextField(getTranslation("text-field.component-name", UI.getCurrent().getLocale(), null));
        eventIdTf = new TextField(getTranslation("text-field.event-id", UI.getCurrent().getLocale(), null));
        dateTimeTf = new TextField(getTranslation("text-field.date-time", UI.getCurrent().getLocale(), null));
    }

    @Override
    public Component getEntityDetailsLayout()
    {
        H3 userProfileLabel = new H3(getTranslation("label.wiretap-event-details", UI.getCurrent().getLocale(), null));

        FormLayout formLayout = new FormLayout();

        moduleNameTf.setReadOnly(true);
        formLayout.add(moduleNameTf);

        componentNameTf.setReadOnly(true);
        formLayout.add(componentNameTf);

        flowNameTf.setReadOnly(true);
        formLayout.add(flowNameTf);

        eventIdTf.setReadOnly(true);
        formLayout.add(eventIdTf);

        dateTimeTf.setReadOnly(true);
        formLayout.add(dateTimeTf);

        formLayout.setSizeFull();

        Button downloadButton = new TableButton(VaadinIcon.DOWNLOAD.create());
        downloadButton.getElement().setProperty("title",  getTranslation("help-message.download-wiretap", UI.getCurrent().getLocale(), null));

        this.streamResource = new StreamResource("wiretap.txt"
            , () -> new ByteArrayInputStream(super.juicyAceEditor.getValue().getBytes() ));

        buttonWrapper = new FileDownloadWrapper(this.streamResource);
        buttonWrapper.wrapComponent(downloadButton);

        VerticalLayout layout = new VerticalLayout();
        layout.add(userProfileLabel, formLayout, buttonWrapper);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, buttonWrapper);

        return layout;
    }

    @Override
    public void populate(IkasanSolrDocument wiretapEvent)
    {
        this.moduleNameTf.setValue(wiretapEvent.getModuleName());
        this.flowNameTf.setValue(wiretapEvent.getFlowName());
        this.componentNameTf.setValue(wiretapEvent.getComponentName());
        this.eventIdTf.setValue(wiretapEvent.getEventId());
        this.dateTimeTf.setValue(DateFormatter.getFormattedDate(wiretapEvent.getTimestamp()));

        super.open(wiretapEvent.getEvent());
    }
}
