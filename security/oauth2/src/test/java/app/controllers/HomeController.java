/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.controllers;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.security.UserPrincipal;
import leap.core.security.annotation.AllowAnonymous;
import leap.oauth2.as.OAuth2AuthzServerConfigurator;
import leap.web.Request;

public class HomeController {

    
    
    public String checkLoginState(String username) {
        if(null == username) {
            return "OK";
        }
        
        UserPrincipal user = Request.current().getUser();
        if(user.getLoginName().equals(username)){
            return "OK";
        }else{
            return "Failed";
        }
    }
    
    
}
