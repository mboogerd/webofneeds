package won.node.facet.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.exception.IllegalMessageForConnectionStateException;
import won.protocol.exception.NoSuchConnectionException;
import won.protocol.exception.WonProtocolException;
import won.protocol.model.Connection;
import won.protocol.model.ConnectionState;
import won.protocol.model.FacetType;
import won.protocol.repository.ConnectionRepository;

import java.net.URI;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gabriel
 * Date: 16.09.13
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class GroupFacetImpl extends Facet {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ConnectionRepository connectionRepository;

  @Override
  public FacetType getFacetType() {
    return FacetType.GroupFacet;
  }

  @Override
  public void textMessageFromOwner(final Connection con, final String message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    logger.info("SEND_TEXT_Message received with facet type GroupFacet from Owner");
    List<Connection> cons = connectionRepository.findByNeedURIAndStateAndTypeURI(con.getNeedURI(),
        ConnectionState.CONNECTED, FacetType.GroupFacet.getURI());

    for (Connection c : cons) {
      final URI remoteConnectionURI = c.getRemoteConnectionURI();
      //inform the other side
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          try {
            needFacingConnectionClient.textMessage(remoteConnectionURI, message);
          } catch (WonProtocolException e) {
            logger.warn("caught WonProtocolException:", e);
          }
        }
      });
    }
  }

  @Override
  public void textMessageFromNeed(final Connection con, final String message) throws NoSuchConnectionException, IllegalMessageForConnectionStateException {
    logger.info("SEND_TEXT_Message received with facet type GroupFacet from Need");
    List<Connection> cons = connectionRepository.findByNeedURIAndStateAndTypeURI(con.getNeedURI(),
        ConnectionState.CONNECTED, FacetType.GroupFacet.getURI());

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ownerFacingConnectionClient.textMessage(con.getConnectionURI(), message);
        } catch (WonProtocolException e) {
          logger.warn("caught WonProtocolException:", e);
        }
      }
    });
    for (Connection c : cons) {
      if(! c.equals(con)) {
        final URI remoteConnectionURI = c.getRemoteConnectionURI();
        //final URI ownerURI = c.getConnectionURI();
        //inform the other side
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            try {
              //ownerFacingConnectionClient.textMessage(ownerURI, message);
              needFacingConnectionClient.textMessage(remoteConnectionURI, message);
            } catch (WonProtocolException e) {
              logger.warn("caught WonProtocolException:", e);
            }
          }
        });
      }
    }

  }
}