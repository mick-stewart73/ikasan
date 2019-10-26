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
package org.ikasan.component.endpoint.consumer.api.endpoint;

import org.ikasan.component.endpoint.consumer.api.spec.EndpointEventProvider;
import org.ikasan.component.endpoint.consumer.api.spec.Endpoint;
import org.ikasan.spec.event.ExceptionListener;
import org.ikasan.spec.event.ForceTransactionRollbackException;
import org.ikasan.spec.event.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TechEndpoint contract implementation as a runnable thread.
 *
 * @author Ikasan Development Team
 */
public class TechEndpointRunnableThread implements Endpoint
{
    /** Logger instance */
    private static Logger logger = LoggerFactory.getLogger(TechEndpointRunnableThread.class);

    /** message listener instance */
    private MessageListener messageListener;

    /** exception listener instance */
    private ExceptionListener exceptionListener;

    /** control the thread execution */
    private volatile boolean running;

    protected EndpointEventProvider<?> eventProvider;

    public void run()
    {
        setRunning(true);

        if(messageListener == null)
        {
            throw new IllegalStateException("messageListener cannot be 'null");
        }

        if(exceptionListener == null)
        {
            throw new IllegalStateException("exceptionListener cannot be 'null");
        }

        try
        {
            execute();
        }
        catch(Throwable t)
        {
            logger.error(t.getMessage(), t);
        }
    }

    private void execute()
    {
        Object event = null;
        while(isRunning() && (event = eventProvider.getEvent()) != null)
        {
            try
            {
                this.messageListener.onMessage(event);
            }
            catch (ForceTransactionRollbackException thrownByRecoveryManager)
            {
                eventProvider.rollback();
            }
            catch (Throwable throwable)
            {
                if(this.exceptionListener == null)
                {
                    throw throwable;
                }

                this.exceptionListener.onException(throwable);
            }
        }

        if(event == null)
        {
            logger.info("Endpoint stopped, no more events." );
        }
    }

    @Override
    public void setMessageListener(MessageListener messageListener)
    {
        this.messageListener = messageListener;
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener)
    {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public void setEventProvider(EndpointEventProvider eventProvider)
    {
        this.eventProvider = eventProvider;
    }

    @Override
    public void stop()
    {
        setRunning(false);
    }

    protected void setRunning(boolean running)
    {
        this.running = running;
    }

    protected boolean isRunning()
    {
        return this.running;
    }
}
