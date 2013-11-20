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

package won.owner.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;

import java.util.List;

//TODO: This route builder should not be loaded on startup, but it's being loaded..
/**
 * User: sbyim
 * Date: 14.11.13
 */
public class OwnerApplicationListenerRouteBuilder extends RouteBuilder  {


    private List<String> endpoints;

    public OwnerApplicationListenerRouteBuilder(CamelContext camelContext, List<String> endpoints) {
        super(camelContext);
        this.endpoints = endpoints;
    }


    @Override
    public void configure() throws Exception {
       for (int i = 0; i<endpoints.size();i++){
           from(endpoints.get(i))
           .wireTap("bean:messagingService?method=inspectMessage")
           .choice()
                .when(header("methodName").isEqualTo("connect"))
                .to("log:OWNER CONNECT RECEIVED")
                .to("bean:ownerProtocolOwnerService?method=connect")
                .when(header("methodName").isEqualTo("hint"))
                .to("bean:ownerProtocolOwnerService?method=hint")
                .when(header("methodName").isEqualTo("textMessage"))
                .to("bean:ownerProtocolOwnerService?method=textMessage")
                .when(header("methodName").isEqualTo("open"))
                .to("bean:ownerProtocolOwnerService?method=open")
                .when(header("methodName").isEqualTo("close"))
                .to("bean:ownerProtocolOwnerService?method=close")
                .otherwise()
                .to("log:Message Type Not Supported");
       }
    }


}
