/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package server.controller;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.security.annotation.AllowAnonymous;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.security.RSA;

/**
 * Created by kael on 2017/1/12.
 */
public class HomeController {
    protected @Inject AppConfig config;
    
    protected String key = null;
    
    @AllowAnonymous
    public String publickey(){
        if(Strings.isEmpty(key)){
            // return an error public key in first time.
            key = Base64.encode(RSA.generateKeyPair().getPublicKey().getEncoded());
        }else{
            key = config.getProperty("oauth2.rs.rsaPublicKeyStr");
        }
        return key;
    }
}
