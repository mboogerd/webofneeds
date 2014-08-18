package won.protocol.message;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import won.protocol.util.RdfUtils;
import won.protocol.util.WonRdfUtils;


/**
 * User: ypanchenko
 * Date: 04.08.2014
 */
public class WonMessage
{

  private Dataset messageContent;
  //private Model messageMetadata;
  //private URI messageEventURI;
  private MessageEvent messageEvent;


  //private Resource msgBnode;
  // private Signature signature;


  public WonMessage() {
    initMessageMetadata();
  }

  public WonMessage(MessageEvent messageEvent, Dataset messageContent) {
    //this.messageEventURI = messageEventURI;
    this.messageEvent = messageEvent;
    this.messageContent = messageContent;
    //initMessageMetadata();
  }

  private void initMessageMetadata() {
    Model messageMetadata = ModelFactory.createDefaultModel();
    messageMetadata.setNsPrefix(WONMSG.DEFAULT_PREFIX, WONMSG.BASE_URI);
    Resource msgBnode = messageMetadata.createResource();
    Resource messageEventResource = messageMetadata.createResource(messageEvent.getMessageURI().toString());
    msgBnode.addProperty(WONMSG.MESSAGE_POINTER_PROPERTY, messageEventResource);
    //TODO own message event signature
  }


  public Dataset getMessageContent() {
    return messageContent;
  }

  public Model getMessageContent(String contentResourceUri) {
    String ngName = getNamedGraphNameForUri(contentResourceUri);
    return messageContent.getNamedModel(ngName);
  }

  public Dataset getMessageWithSignature(String contentResourceUri) {
    Dataset dataset = DatasetFactory.createMem();
    String ngName = getNamedGraphNameForUri(contentResourceUri);
    dataset.addNamedModel(ngName, messageContent.getNamedModel(ngName));
    RdfUtils.addPrefixMapping(dataset.getDefaultModel(), messageContent.getNamedModel(ngName));
    RdfUtils.addPrefixMapping(dataset.getDefaultModel(), messageContent.getDefaultModel());
    //TODO signature into default graph
    return dataset;
  }

  private String getNamedGraphNameForUri(final String resourceUri) {
    String ngName = resourceUri;
    // we commonly use resource url + #data for the name of named graph
    // with this resource content
    if (messageContent.getNamedModel(resourceUri) == null) {
      ngName = resourceUri + WonRdfUtils.NAMED_GRAPH_SUFFIX;
    }
    return ngName;
  }

  public void setMessageContent(Dataset messageContent) {
    this.messageContent = messageContent;
  }

  public MessageEvent getMessageEvent() {
    return messageEvent;
  }

  public void setMessageEvent(final MessageEvent messageEvent) {
    this.messageEvent = messageEvent;
  }
}