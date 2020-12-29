package org.ikasan.monitor;

import org.ikasan.monitor.notifier.*;
import org.ikasan.spec.dashboard.DashboardRestService;
import org.ikasan.spec.monitor.Monitor;
import org.ikasan.spec.monitor.Notifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties
public class IkasanMonitorAutoConfiguration
{

    private final Environment environment;

    public IkasanMonitorAutoConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean(destroyMethod = "destroy")
    public MonitorFactory monitorFactory(){
        return new MonitorFactoryImpl();
    }

    @Bean
    public NotifierFactory notifierFactory(){
        return new NotifierFactoryImpl();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnBean({MonitorFactory.class})
    @ConditionalOnMissingBean
    public Monitor monitor(MonitorFactory monitorFactory, Optional<List<Notifier>> notifiers){

        Monitor monitor = monitorFactory.getMonitor();
        notifiers.ifPresent(monitor::setNotifiers);
        String environmentName = environment.getProperty("environment");
        if ( environmentName!=null && !environmentName.isEmpty() ) {
            monitor.setEnvironment(environmentName);
        }
        return monitor;
    }

    @Bean
    @ConditionalOnBean({NotifierFactory.class})
    @ConditionalOnProperty(prefix = "ikasan.dashboard.extract", name = "enabled", havingValue = "true")
    public Notifier dashboardNotifier(NotifierFactory notifierFactory, DashboardRestService flowCacheStateRestService){
        return notifierFactory.getDashboardNotifier(flowCacheStateRestService);
    }

    @Bean
    @ConditionalOnBean({NotifierFactory.class})
    @ConditionalOnProperty(prefix = "ikasan.monitor.notifier.mail", name = "enabled", havingValue = "true")
    public Notifier emailNotifier(NotifierFactory notifierFactory, EmailNotifierConfiguration emailNotifierConfiguration){
        EmailNotifier emailNotifier = (EmailNotifier) notifierFactory.getEmailNotifier();
        emailNotifier.setConfiguration(emailNotifierConfiguration);
        return emailNotifier;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "ikasan.monitor.notifier.mail")
    public EmailNotifierConfiguration emailNotifierConfiguration(){
        return new EmailNotifierConfiguration();
    }
}