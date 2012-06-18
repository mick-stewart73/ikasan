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
package org.ikasan.consumer.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.ikasan.spec.component.endpoint.EndpointException;
import org.ikasan.spec.component.endpoint.Producer;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Implementation of a consumer based on the JMS specification.
 *
 * @author Ikasan Development Team
 */
public class GenericJmsProducer<T> implements Producer<T>
{
    /** class logger */
    private static Logger logger = Logger.getLogger(GenericJmsProducer.class);
    
    /** JMS Connection Factory */
    private ConnectionFactory connectionFactory;

    /** JMS Destination instance */
    protected Destination destination;

    /** JMS Connection */
    protected Connection connection;

    /** configured resource id */
    protected String configuredResourceId;
    
    /** JMS consumer configuration - default to vanilla instance */
    protected GenericJmsProducerConfiguration configuration = new GenericJmsProducerConfiguration();
    
    /** session has to be closed prior to connection being closed */
    protected Session session;

    // TODO - remove Spring dependency
    /** message converter */
    protected MessageConverter messageConverter;
    
    /**
     * Constructor where connectionFactory can be created and injected for 
     * use within this class. This is the use case for scenarios where the
     * connectionFactory does not need to be pinned to the executing 
     * thread.
     * Sometimes we need to pin the connectioNFactory to the executing thread
     * for instance, WebLogic security authentication for JMS is a use case for 
     * this requirement.
     * @param connectionFactory
     * @param destination
     */
    public GenericJmsProducer(ConnectionFactory connectionFactory, Destination destination)
    {
        this.connectionFactory = connectionFactory;
        if(connectionFactory == null)
        {
            throw new IllegalArgumentException("connectionFactory cannot be 'null'");
        }
        
        this.destination = destination;
        if(destination == null)
        {
            throw new IllegalArgumentException("destination cannot be 'null'");
        }
    }

    public void invoke(T message) throws EndpointException
    {
        try
        {
            if(this.configuration.getUsername() != null && this.configuration.getUsername().trim().length() > 0)
            {
                connection = connectionFactory.createConnection(this.configuration.getUsername(), this.configuration.getPassword());
            }
            else
            {
                connection = connectionFactory.createConnection();
            }

            this.session = connection.createSession(this.configuration.isTransacted(), this.configuration.getAcknowledgement());
            MessageProducer messageProducer = session.createProducer(destination);
            
            if(message instanceof Message)
            {
                messageProducer.send((Message)message);
            }
            else
            {
                if(this.messageConverter == null)
                {
                    throw new EndpointException("Cannot publish message of type[" 
                        + message.getClass().getName() 
                        + " to JMS without a converter!");
                }

                Message jmsMessage = this.messageConverter.toMessage(message, session);
                messageProducer.send(jmsMessage);
            }
        }
        catch(JMSException e)
        {
            throw new EndpointException(e);
        }
        finally
        {
            if(session != null)
            {
                try
                {
                    session.close();
                    session = null;
                }
                catch (JMSException e)
                {
                    logger.error("Failed to close session", e);
                }
            }

            if(connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (JMSException e)
                {
                    throw new EndpointException(e);
                }
            }
        }
    }
    
    public void setMessageConverter(MessageConverter messageConverter)
    {
        this.messageConverter = messageConverter;
    }
    
    public GenericJmsProducerConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public String getConfiguredResourceId()
    {
        return this.configuredResourceId;
    }

    public void setConfiguration(GenericJmsProducerConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void setConfiguredResourceId(String configuredResourceId)
    {
        this.configuredResourceId = configuredResourceId;
    }

}
