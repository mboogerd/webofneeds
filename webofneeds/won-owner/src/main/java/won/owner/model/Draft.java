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

/*
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt', which is part of this source code package.
 */

package won.owner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.net.URI;

/**
 * I used wonuser as table name because user is Postgres keyword - http://www.postgresql.org/message-id/Pine.NEB.4.10.10008291649550.4357-100000@scimitar.caravan.com
 *
 */
@Entity
@Table(
		name = "needDraft",
		uniqueConstraints = @UniqueConstraint(columnNames = {"id","draftURI"})
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft
{

  @Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

  @Column( name = "draftURI", unique = true)
  private URI draftURI;

  @Column( length=10000)
  private String content;

  public Draft(){
  }

  public Draft(URI draftURI, String content){
       this.draftURI = draftURI;
    this.content = content;
  }

  public Long getId() {
    return id;
  }


  public URI getDraftURI() {
    return draftURI;
  }



  public void setDraftURI(final URI draftURI) {
    this.draftURI = draftURI;
  }

  public void setId(final Long id) {
    this.id = id;
  }
  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }


}
