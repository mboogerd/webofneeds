/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.node.camel.processor.fixed;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import won.node.camel.processor.AbstractCamelProcessor;
import won.node.camel.processor.annotation.FixedMessageProcessor;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.message.processor.camel.WonCamelConstants;
import won.protocol.vocabulary.WONMSG;

/**
 * Processes responses to generated by the system and directed at the remote side.
 */
@Component
@FixedMessageProcessor(
        direction = WONMSG.TYPE_FROM_SYSTEM_STRING,
        messageType = WONMSG.TYPE_SUCCESS_RESPONSE_STRING)
public class SuccessResponseFromSystemToNodeProcessor extends AbstractCamelProcessor
{
  @Override
  public void process(Exchange exchange) throws Exception {
    WonMessage wonMessage = (WonMessage) exchange.getIn().getHeader(WonCamelConstants.MESSAGE_HEADER);
    //prepare the message to pass to the remote node
    final WonMessage newWonMessage = createMessageToSendToRemoteNode(wonMessage);
    //put it into the 'outbound message' header (so the persister doesn't pick up the wrong one).
    exchange.getIn().setHeader(WonCamelConstants.OUTBOUND_MESSAGE_HEADER, newWonMessage);

    //add the information about the corresponding message to the local one
    wonMessage = WonMessageBuilder
            .wrap(wonMessage)
            .setCorrespondingRemoteMessageURI(newWonMessage.getMessageURI())
            .build();

    //put it into the header so the persister will pick it up later
    exchange.getIn().setHeader(WonCamelConstants.MESSAGE_HEADER,wonMessage);
  }

  private WonMessage createMessageToSendToRemoteNode(WonMessage wonMessage) {
    //create the message to send to the remote node
    return WonMessageBuilder
            .setPropertiesForPassingMessageToRemoteNode(
                    wonMessage,
                    wonNodeInformationService
                            .generateEventURI(wonMessage.getReceiverNodeURI()))
            .build();
  }
}
