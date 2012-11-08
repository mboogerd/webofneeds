/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package won.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import won.protocol.exception.ConnectionAlreadyExistsException;
import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.model.*;
import won.protocol.need.NeedProtocolNeedService;
import won.protocol.owner.OwnerProtocolOwnerService;
import won.protocol.repository.ConnectionRepository;
import won.protocol.repository.NeedRepository;
import won.protocol.service.MatcherFacingNeedCommunicationService;
import won.protocol.service.NeedFacingNeedCommunicationService;
import won.protocol.service.OwnerFacingNeedCommunicationService;

import java.net.URI;
import java.util.concurrent.ExecutorService;


/**
 * User: fkleedorfer
 * Date: 02.11.12
 */
@Component
public class NeedCommunicationServiceImpl implements
    OwnerFacingNeedCommunicationService,
    NeedFacingNeedCommunicationService,
    MatcherFacingNeedCommunicationService
{
  final Logger logger = LoggerFactory.getLogger(NeedCommunicationServiceImpl.class);

  /**
   * Client talking to the owner side via the owner protocol
   */
  private OwnerProtocolOwnerService ownerProtocolOwnerService;
  /**
   * Client talking another need via the need protocol
   */
  private NeedProtocolNeedService needProtocolNeedService;

  private URIService URIService;

  private ExecutorService executorService;

  @Autowired
  private NeedRepository needRepository;
  @Autowired
  private ConnectionRepository connectionRepository;

  @Override
  public void hint(final URI needURI, final URI otherNeed, final double score, final URI originator) throws NoSuchNeedException, IllegalMessageForNeedStateException
  {
    logger.info("HINT received for need {} referring to need {} with score {} from originator {}",new Object[]{needURI.toString(),otherNeed.toString(),score,originator.toString()});
    //Load need (throws exception if not found)
    Need need = DataAccessUtils.loadNeed(needRepository, needURI);
    if (! isNeedActive(need)) throw new IllegalMessageForNeedStateException(needURI, NeedMessage.HINT.name(), need.getState());
    executorService.execute(new Runnable()
    {
      @Override
      public void run()
      {
        //TODO: somewhere, we'll have to use the need's owner URI to determine where to send the request to..
        //should we access the database again in the implementation of the owner protocol owner client?
        ownerProtocolOwnerService.hintReceived(needURI, otherNeed, score, originator);
      }
    });
  }

  @Override
  public URI connectTo(final URI needURI, final URI otherNeedURI, final String message) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException
  {
    logger.info("CONNECT_TO received for need {} referring to need {} with message '{}'",new Object[]{needURI.toString(),otherNeedURI.toString(),message});
    //Load need (throws exception if not found)
    Need need = DataAccessUtils.loadNeed(needRepository, needURI);
    if (! isNeedActive(need)) throw new IllegalMessageForNeedStateException(needURI, NeedMessage.CONNECT_TO.name(), need.getState());
    //Create new connection object
    Connection con = new Connection();
    con.setNeedURI(needURI);
    con.setState(ConnectionState.REQUEST_SENT);
    con.setRemoteNeedURI(otherNeedURI);
    //save connection (this creates a new id)
    con = connectionRepository.saveAndFlush(con);
    //create and set new uri
    con.setConnectionURI(URIService.createConnectionURI(con));
    con = connectionRepository.saveAndFlush(con);

    final Connection connectionForRunnable = con;
    //send to need
    executorService.execute(new Runnable()
    {
      @Override
      public void run()
      {
        URI remoteConnectionURI = needProtocolNeedService.connectionRequested(otherNeedURI, needURI, connectionForRunnable.getConnectionURI(), message);
        connectionForRunnable.setRemoteConnectionURI(remoteConnectionURI);
        connectionRepository.saveAndFlush(connectionForRunnable);
      }
    });
    return con.getConnectionURI();
  }


  @Override
  public URI connectionRequested(final URI needURI, final URI otherNeedURI, final URI otherConnectionURI, final String message) throws NoSuchNeedException, IllegalMessageForNeedStateException, ConnectionAlreadyExistsException
  {
    logger.info("CONNECTION_REQUESTED received for need {} referring to need {} (connection {}) with message '{}'",new Object[]{needURI.toString(),otherNeedURI.toString(), otherConnectionURI,message});
    //Load need (throws exception if not found)
    Need need = DataAccessUtils.loadNeed(needRepository,needURI);
    if (! isNeedActive(need)) throw new IllegalMessageForNeedStateException(needURI, NeedMessage.CONNECTION_REQUESTED.name(), need.getState());
    //Create new connection object on our side
    Connection con = new Connection();
    con.setNeedURI(needURI);
    con.setState(ConnectionState.REQUEST_RECEIVED);
    con.setRemoteNeedURI(otherNeedURI);
    con.setRemoteConnectionURI(otherConnectionURI);
    //save connection (this creates a new URI)
    con = connectionRepository.saveAndFlush(con);
    //create and set new uri
    con.setConnectionURI(URIService.createConnectionURI(con));
    con = connectionRepository.saveAndFlush(con);

    //TODO: do we save the connection message? where? as a chat message?

    final Connection connectionForRunnable = con;
    executorService.execute(new Runnable()
    {
      @Override
      public void run()
      {
        ownerProtocolOwnerService.connectionRequested(needURI, otherNeedURI, connectionForRunnable.getConnectionURI(), message);
      }
    });

    //return the URI of the new connection
    return con.getConnectionURI();
  }


  private boolean isNeedActive(final Need need) {
    return NeedState.ACTIVE == need.getState();
  }

  public void setOwnerProtocolOwnerService(final OwnerProtocolOwnerService ownerProtocolOwnerService)
  {
    this.ownerProtocolOwnerService = ownerProtocolOwnerService;
  }

  public void setNeedRepository(final NeedRepository needRepository)
  {
    this.needRepository = needRepository;
  }

  public void setConnectionRepository(final ConnectionRepository connectionRepository)
  {
    this.connectionRepository = connectionRepository;
  }

  public void setNeedProtocolNeedService(final NeedProtocolNeedService needProtocolNeedService)
  {
    this.needProtocolNeedService = needProtocolNeedService;
  }

  public void setURIService(final URIService URIService)
  {
    this.URIService = URIService;
  }

  public void setExecutorService(final ExecutorService executorService)
  {
    this.executorService = executorService;
  }
}
