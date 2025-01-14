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

package won.bot.framework.eventbot.action.impl.matcher;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.model.FacetType;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.WonRdfUtils;

import java.net.URI;
import java.util.List;

/**
 * BaseEventBotAction that sends a hint message to the first need in the context to the second.
 */
public class MatchNeedsAction extends BaseEventBotAction
{
  public MatchNeedsAction(final EventListenerContext eventListenerContext)
  {
    super(eventListenerContext);
  }

  @Override
  protected void doRun(Event event) throws Exception{
    List<URI> needs = getEventListenerContext().getBotContext().listNeedUris();
    URI need1 = needs.get(0);
    URI need2 = needs.get(1);
    logger.debug("matching needs {} and {}",need1,need2);
    logger.debug("getEventListnerContext():"+getEventListenerContext());
    logger.debug("getMatcherService(): "+getEventListenerContext().getMatcherProtocolNeedServiceClient());
    getEventListenerContext().getMatcherProtocolNeedServiceClient().hint(
            need1,
            need2,
            1.0,
            URI.create("http://example.com/matcher"),
            null,
            createWonMessage(need1, need2, 1.0, URI.create("http://example.com/matcher")));
  }

  private WonMessage createWonMessage(URI needURI, URI otherNeedURI, double score, URI originator)
    throws WonMessageBuilderException {

    WonNodeInformationService wonNodeInformationService =
      getEventListenerContext().getWonNodeInformationService();

    URI localWonNode = WonRdfUtils.NeedUtils.getWonNodeURIFromNeed(
      getEventListenerContext().getLinkedDataSource().getDataForResource(needURI), needURI);

    return WonMessageBuilder
      .setMessagePropertiesForHint(
        wonNodeInformationService.generateEventURI(
          localWonNode),
        needURI,
        FacetType.OwnerFacet.getURI(),
        localWonNode,
        otherNeedURI,
        FacetType.OwnerFacet.getURI(),
        originator,
        score)
      .build();
  }

}
