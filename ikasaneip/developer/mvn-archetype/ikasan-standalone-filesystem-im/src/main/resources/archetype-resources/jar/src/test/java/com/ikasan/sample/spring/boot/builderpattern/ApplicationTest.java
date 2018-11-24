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
package com.ikasan.sample.spring.boot.builderpattern;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.ikasan.component.endpoint.filesystem.messageprovider.FileConsumerConfiguration;
import org.ikasan.component.endpoint.filesystem.producer.FileProducer;

import org.ikasan.component.endpoint.filesystem.producer.FileProducerConfiguration;
import org.ikasan.component.endpoint.jms.spring.consumer.SpringMessageConsumerConfiguration;
import org.ikasan.spec.configuration.ConfiguredResource;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.ikasan.testharness.flow.jms.MessageListenerVerifier;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.jms.TextMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test class supports the <code>Application</code> class.
 *
 * @author Ikasan Development Team
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest
{
    /** logger */
    private static Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    private static String FILE_PRODUCER_FILE_NAME = "testProducer.out";

    private static String FILE_CONSUMER_FILE_NAME = "testConsumer.txt";

    private EmbeddedActiveMQBroker broker;

    @Resource
    private Module<Flow> moduleUnderTest;

    @Resource
    private JmsListenerEndpointRegistry registry;

    @Resource
    private JmsTemplate jmsTemplate;

    public IkasanFlowTestRule flowTestRule = new IkasanFlowTestRule();

    @Before
    public void setup(){

        broker =  new EmbeddedActiveMQBroker();
        broker.start();

    }

    @After public void shutdown() throws IOException
    {
        flowTestRule.stopFlow();

        Files.deleteIfExists(FileSystems.getDefault().getPath(FILE_PRODUCER_FILE_NAME));
        Files.deleteIfExists(FileSystems.getDefault().getPath(FILE_CONSUMER_FILE_NAME));
        broker.stop();
    }

    @Test
    @DirtiesContext
    public void sourceFileFlow_flow() throws Exception
    {
        flowTestRule.withFlow((Flow) moduleUnderTest.getFlow("${sourceFlowName}"));

        // create file to be consumed
        String content = "Hello World !!";
        Files.write(Paths.get(FILE_CONSUMER_FILE_NAME), content.getBytes());
        List filenames = new ArrayList<String>();
        filenames.add(FILE_CONSUMER_FILE_NAME);

        Flow flow = moduleUnderTest.getFlow("sourceFileFlow");

        ConfiguredResource<FileConsumerConfiguration> configuredResource = ((ConfiguredResource)flow.getFlowElement("File Consumer").getFlowComponent());
        FileConsumerConfiguration configuration = configuredResource.getConfiguration();
        configuration.setFilenames(filenames);
        configuration.setCronExpression("*/1 * * * * ?");

        // Get MessageListenerVerifier and start the listener
        final MessageListenerVerifier messageListenerVerifierTarget = new MessageListenerVerifier(broker.getVmURL(), "jms.topic.test", registry);

        messageListenerVerifierTarget.start();

        //Setup component expectations
        flowTestRule.consumer("File Consumer")
            .filter("My Filter")
            .converter("File Converter")
            .producer("JMS Producer");


        flowTestRule.startFlow();
        flowTestRule.sleep(1000L);
        flowTestRule.fireScheduledConsumer();

        // wait for a brief while to let the flow complete
        flowTestRule.sleep(2000L);

        // start flow
        flow.start();
        assertEquals("running", flow.getState());

        // check for published event
        int count = 0;
        while (messageListenerVerifierTarget.getCaptureResults().size() == 0)
        {
            pause(200);
            assertFalse("Timed out waiting for event", count++ == 10);
        }

        flow.stop();
        assertEquals("stopped", flow.getState());

        // Set expectation
        assertTrue(messageListenerVerifierTarget.getCaptureResults().size()>=1);
        assertEquals(((TextMessage)messageListenerVerifierTarget.getCaptureResults().get(0)).getText(),
                FILE_CONSUMER_FILE_NAME);
    }

    @Test
    @DirtiesContext
    public void targetFileFlow_test_file_delivery() throws Exception
    {
        flowTestRule.withFlow((Flow) moduleUnderTest.getFlow("${targetFlowName}"));


        // Prepare test data
        String message = "Random Text";
        logger.info("Sending a JMS message.[" + message + "]");
        jmsTemplate.convertAndSend("private.file.queue.test", message);

        // get targetFileFlow from context
        Flow flow = moduleUnderTest.getFlow("targetFileFlow");

        // update producer with file producer name
        FileProducerConfiguration producerConfiguration = ((FileProducer) flow.getFlowElement("File Producer")
                .getFlowComponent()).getConfiguration();
        producerConfiguration.setFilename(FILE_PRODUCER_FILE_NAME);

        // update flow consumer with file producer name
        SpringMessageConsumerConfiguration jmsConfiguration = ((ConfiguredResource<SpringMessageConsumerConfiguration>)flow.getFlowElement("JMS Consumer").getFlowComponent()).getConfiguration();
        jmsConfiguration.setDestinationJndiName("private.file.queue.test");

        //Setup component expectations
        flowTestRule.consumer("JMS Consumer")
            .producer("File Producer");

        // give flow time
        pause(2000);
        assertEquals("running", flow.getState());
        flow.stop();
        assertEquals("stopped", flow.getState());

        File result = FileSystems.getDefault().getPath(FILE_PRODUCER_FILE_NAME).toFile();

        assertTrue("File does not exist.", result.exists());
        assertEquals("Generated file, has different content.", message,
                new String(Files.readAllBytes(result.toPath())));
    }

}
