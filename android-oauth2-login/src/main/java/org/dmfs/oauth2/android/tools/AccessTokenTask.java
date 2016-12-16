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

package org.dmfs.oauth2.android.tools;

import android.os.AsyncTask;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Grant;


/**
 * An {@link AsyncTask} to retrieve to {@link OAuth2AccessToken} of authorizes {@link OAuth2Grant}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class AccessTokenTask extends ThrowingAsyncTask<HttpRequestExecutor, Void, OAuth2AccessToken>
{
    private final OAuth2Grant mGrant;


    public AccessTokenTask(OAuth2Grant grant, OnResultCallback<OAuth2AccessToken> callback)
    {
        super(callback);
        mGrant = grant;
    }


    @Override
    protected OAuth2AccessToken doInBackgroundWithException(HttpRequestExecutor... executors) throws Exception
    {
        return mGrant.accessToken(executors[0]);
    }
}
