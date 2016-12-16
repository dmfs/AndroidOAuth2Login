/*
 * Copyright 2016 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.oauth2.android.errorstates;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;

import java.io.IOException;
import java.net.URI;


/**
 * An {@link OAuth2InteractiveGrant.OAuth2GrantState} that represents an error state.
 *
 * @author Marten Gajda
 */
public final class ErrorOAuth2GrantState implements OAuth2InteractiveGrant.OAuth2GrantState
{
    private final ProtocolError mError;


    public ErrorOAuth2GrantState(ProtocolError error)
    {
        mError = error;
    }


    @Override
    public OAuth2InteractiveGrant grant(OAuth2Client oAuth2Client)
    {
        return new ErrorOAuth2InteractiveGrant(mError);
    }


    /**
     * An {@link OAuth2InteractiveGrant} in an error state.
     *
     * @author Marten Gajda
     */
    private static final class ErrorOAuth2InteractiveGrant implements OAuth2InteractiveGrant
    {
        private final ProtocolError mError;


        public ErrorOAuth2InteractiveGrant(ProtocolError error)
        {
            mError = error;
        }


        @Override
        public URI authorizationUrl()
        {
            throw new IllegalStateException("Can't return authorization url at this stage.");
        }


        @Override
        public OAuth2InteractiveGrant withRedirect(URI uri) throws ProtocolError, ProtocolException
        {
            throw new IllegalStateException("Can't handle redirects at this stage.");
        }


        @Override
        public OAuth2GrantState state() throws UnsupportedOperationException
        {
            return new ErrorOAuth2GrantState(mError);
        }


        @Override
        public OAuth2AccessToken accessToken(HttpRequestExecutor httpRequestExecutor) throws IOException, ProtocolError, ProtocolException
        {
            throw mError;
        }
    }
}
