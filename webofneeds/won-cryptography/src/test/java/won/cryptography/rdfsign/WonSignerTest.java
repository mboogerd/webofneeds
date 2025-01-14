package won.cryptography.rdfsign;

import com.hp.hpl.jena.query.Dataset;
import de.uni_koblenz.aggrimm.icp.crypto.sign.algorithm.algorithm.SignatureAlgorithmFisteus2010;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import won.cryptography.utils.TestSigningUtils;
import won.cryptography.utils.TestingKeys;
import won.protocol.util.RdfUtils;
import won.protocol.vocabulary.SFSIG;

import java.util.List;

/**
 * User: ypanchenko
 * Date: 14.07.2014
 */
public class WonSignerTest
{

  private static final String RESOURCE_FILE = "/won-signed-messages/create-need-msg.trig";

  private static final String NEED_CORE_DATA_URI =
    "http://localhost:8080/won/resource/need/3144709509622353000/core/#data";
  private static final String NEED_CORE_DATA_SIG_URI =
    "http://localhost:8080/won/resource/need/3144709509622353000/core/#data-sig";

  private static final String EVENT_ENV1_URI =
    "http://localhost:8080/won/resource/event/7719577021233193000#data";
  private static final String EVENT_ENV1_SIG_URI =
    "http://localhost:8080/won/resource/event/7719577021233193000#data-sig";

  private static final String EVENT_ENV2_URI =
    "http://localhost:8080/won/resource/event/7719577021233193000#envelope-s7gl";
  private static final String EVENT_ENV2_SIG_URI =
    "http://localhost:8080/won/resource/event/7719577021233193000#envelope-s7gl-sig";



  private TestingKeys keys;

  @Before
  public void init() throws Exception {
    keys = new TestingKeys(TestSigningUtils.KEYS_FILE);
  }

  @Test
  public void testSignCreatedNeed() throws Exception {

    // create dataset that contains need core data graph
    Dataset testDataset = TestSigningUtils.prepareTestDatasetFromNamedGraphs(RESOURCE_FILE,
                                                                             new String[]{NEED_CORE_DATA_URI});

    // sign it
    WonSigner signer = new WonSigner(testDataset, new SignatureAlgorithmFisteus2010());
    signer.sign(keys.getPrivateKey(TestSigningUtils.needCertUri), TestSigningUtils.needCertUri, NEED_CORE_DATA_URI);

    // write for debugging
    //TestSigningUtils.writeToTempFile(testDataset);

    // extract names of the named graphs
    List<String> namesList = RdfUtils.getModelNames(testDataset);
    // do some checks to make sure the signatures are added
    Assert.assertEquals(2, namesList.size());
    Assert.assertTrue(namesList.contains(NEED_CORE_DATA_URI));
    Assert.assertTrue(namesList.contains(NEED_CORE_DATA_SIG_URI));
    int triplesCounter = TestSigningUtils.countTriples(testDataset.getNamedModel(NEED_CORE_DATA_SIG_URI)
                                                                  .listStatements());
    Assert.assertEquals(11, triplesCounter);
    String sigValue = TestSigningUtils.getObjectOfPredAsString(testDataset.getNamedModel(NEED_CORE_DATA_SIG_URI),
                                                               SFSIG.HAS_SIGNATURE_VALUE.getURI());
    // even with the same key the signature for the same input is different each time due to the random
    // integer used by the elliptic curve signing algorithm, therefore, we cannot really test here properly
    // if the signature is correct
    Assert.assertTrue(sigValue.length() > 75);
  }

  @Test
  // test signing the event that already contains graph with corresponding graph signature
  public void testSignCreatedNeedOwnerEvent() throws Exception {
    // create dataset that contains need core data graph, its signature, and message envelope generated by the owner
    Dataset testDataset = TestSigningUtils.prepareTestDatasetFromNamedGraphs(RESOURCE_FILE,
                                                                             new String[]{NEED_CORE_DATA_URI,
                                                                                          NEED_CORE_DATA_SIG_URI,
                                                                                          EVENT_ENV1_URI});

    // sign it
    WonSigner signer = new WonSigner(testDataset, new SignatureAlgorithmFisteus2010());
    signer.sign(keys.getPrivateKey(TestSigningUtils.needCertUri), TestSigningUtils.needCertUri, new String[]{EVENT_ENV1_URI});

    // write for debugging
    //TestSigningUtils.writeToTempFile(testDataset);

    // extract names of the named graphs
    List<String> namesList = RdfUtils.getModelNames(testDataset);
    // do some checks to make sure the signatures are added
    Assert.assertEquals(4, namesList.size());
    Assert.assertTrue(namesList.contains(EVENT_ENV1_URI));
    Assert.assertTrue(namesList.contains(EVENT_ENV1_SIG_URI));
    int triplesCounter = TestSigningUtils.countTriples(testDataset.getNamedModel(EVENT_ENV1_SIG_URI).listStatements());
    Assert.assertEquals(11, triplesCounter);
    String sigValue = TestSigningUtils.getObjectOfPredAsString(testDataset.getNamedModel(EVENT_ENV1_SIG_URI),
                                                               "http://icp.it-risk.iwvi.uni-koblenz.de/ontologies/signature" +
                                                                 ".owl#hasSignatureValue");
    // even with the same key the signature for the same input is different each time due to the random
    // integer used by the elliptic curve signing algorithm, therefore, we cannot really test here properly
    // if the signature is correct
    Assert.assertTrue(sigValue.length() > 75);

  }

  @Test
  public void testSignCreatedNeedNodeEvent() throws Exception {

    // create dataset that contains need core data graph, its signature, message envelope generated by the owner,
    // its signature, and message envelope generated by the node
    Dataset testDataset = TestSigningUtils.prepareTestDatasetFromNamedGraphs(RESOURCE_FILE,
                                                                             new String[]{
                                                                               NEED_CORE_DATA_URI,
                                                                               NEED_CORE_DATA_SIG_URI,
                                                                               EVENT_ENV1_URI, EVENT_ENV1_SIG_URI,
                                                                               EVENT_ENV2_URI
                                                                             });

    // sign it
    WonSigner signer = new WonSigner(testDataset, new SignatureAlgorithmFisteus2010());
    signer.sign(keys.getPrivateKey(TestSigningUtils.nodeCertUri), TestSigningUtils.nodeCertUri,
                new String[]{EVENT_ENV2_URI});

    // write for debugging
    TestSigningUtils.writeToTempFile(testDataset);
    // verify
    WonVerifier verifier = new WonVerifier(testDataset);
    boolean verified = verifier.verify(keys.getPublicKeys());
    SignatureVerificationResult result = verifier.getVerificationResult();
    Assert.assertTrue(result.getMessage(), verified);

    // extract names of the named graphs
    List<String> namesList = RdfUtils.getModelNames(testDataset);
    // do some checks to make sure the signatures are added
    Assert.assertEquals(6, namesList.size());
    Assert.assertTrue(namesList.contains(EVENT_ENV2_URI));
    Assert.assertTrue(namesList.contains(EVENT_ENV2_SIG_URI));
    int triplesCounter = TestSigningUtils.countTriples(testDataset.getNamedModel(EVENT_ENV2_SIG_URI).listStatements());
    Assert.assertEquals(11, triplesCounter);
    String sigValue = TestSigningUtils.getObjectOfPredAsString(testDataset.getNamedModel(EVENT_ENV2_SIG_URI),
                                                               "http://icp.it-risk.iwvi.uni-koblenz.de/ontologies/signature" +
                                                                 ".owl#hasSignatureValue");
    // even with the same key the signature for the same input is different each time due to the random
    // integer used by the elliptic curve signing algorithm, therefore, we cannot really test here properly
    // if the signature is correct
    Assert.assertTrue(sigValue.length() > 75);



  }


}
