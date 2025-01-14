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

package won.protocol.model;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * Encapsulates a jena dataset for storing it in a relational db.
 */
@Entity
@Table(name = "rdf_datasets")
public class DatasetHolder
{
  private static final int DEFAULT_BYTE_ARRAY_SIZE = 500;

  @Transient
  private final Logger logger = LoggerFactory.getLogger(getClass());

  //the URI of the dataset
  @Id
  @Column( name = "datasetURI", unique = true)
  private URI uri;

  //the model as a byte array
  @Lob @Column( name = "dataset", nullable = false, length = 10000000)
  private byte[] datasetBytes;

  //for multiple accesses to model, cache it.
  @Transient
  private Dataset cachedDataset;

  DatasetHolder(){}

  public DatasetHolder(final URI uri, final Dataset dataset) {
    this.uri = uri;
    setDataset(dataset);
    this.cachedDataset = dataset;
  }

  public URI getUri() {
    return uri;
  }

  public void setUri(final URI uri) {
    this.uri = uri;
  }

  byte[] getDatasetBytes() {
    return datasetBytes;
  }

  void setDatasetBytes(final byte[] datasetBytes) {
    this.datasetBytes = datasetBytes;
    this.cachedDataset = null;
  }

  /**
   * Careful, expensive operation: writes dataset to string.
   * @param dataset
   */
  public void setDataset(Dataset dataset) {
    assert this.uri != null : "uri must not be null";
    assert this.datasetBytes != null : "model must not be null";
    ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BYTE_ARRAY_SIZE);
    synchronized(this){
      RDFDataMgr.write(out, dataset, Lang.TRIG);
      this.datasetBytes = out.toByteArray();
      this.cachedDataset = dataset;
      if (logger.isDebugEnabled()){
        logger.debug("wrote dataset {} to byte array of length {}", this.uri, this.datasetBytes.length);
      }
    }
  }

  /**
   * Careful, expensive operation: reads dataset from string.
   * @return
   */
  public Dataset getDataset(){
    assert this.uri != null : "uri must not be null";
    assert this.datasetBytes != null : "model must not be null";
    synchronized (this) {
      if (this.cachedDataset != null) return cachedDataset;
      Dataset dataset = DatasetFactory.createMem();
      InputStream is = new ByteArrayInputStream(this.datasetBytes);
      try {
        RDFDataMgr.read(dataset, is,  this.uri.toString(), Lang.TRIG);
      } catch (Exception e) {
        logger.warn("could not read dataset {} from byte array. Byte array is null: {}, has length {}",
          new Object[]{this.uri,
            this.datasetBytes == null,
            this.datasetBytes == null ? -1 : this.datasetBytes.length}
        );
        logger.warn("caught exception while reading dataset", e);
      }
      this.cachedDataset = dataset;
      return dataset;
    }
  }
}
