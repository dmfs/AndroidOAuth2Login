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

package org.dmfs.oauth2.android.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.executors.following.Following;
import org.dmfs.httpessentials.executors.following.policies.FollowRedirectPolicy;
import org.dmfs.httpessentials.executors.following.policies.Secure;
import org.dmfs.httpessentials.executors.retrying.Retrying;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.httpessentials.httpurlconnection.factories.DefaultHttpUrlConnectionFactory;
import org.dmfs.httpessentials.httpurlconnection.factories.decorators.Finite;
import org.dmfs.oauth2.android.BasicOAuth2Authorization;
import org.dmfs.oauth2.android.OAuth2ClientFactory;
import org.dmfs.oauth2.android.errors.AuthorizationCancelledError;
import org.dmfs.oauth2.android.tools.AccessTokenTask;
import org.dmfs.oauth2.android.tools.AsyncTaskResult;
import org.dmfs.oauth2.android.tools.ThrowingAsyncTask;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.oauth2.providers.GoogleAuthorizationProvider;
import org.dmfs.pigeonpost.Dovecote;
import org.dmfs.pigeonpost.localbroadcast.SerializableDovecote;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;


/**
 * A DemoActivity that connects authenticates with a given OAuth2 authority.
 * <p>
 * To make this work, add the following to your {@code local.properties} file
 * <pre>
 * oauth2.clientId=YOU_CLIENT_ID
 * oauth2.clientSecret=YOUR_CLIENT_SECRET
 * </pre>
 */
public class DemoActivity extends AppCompatActivity implements Dovecote.OnPigeonReturnCallback<OAuth2InteractiveGrant.OAuth2GrantState>
{

    private OAuth2Client mClient;

    private Dovecote<OAuth2InteractiveGrant.OAuth2GrantState> mGrantStateDovecote;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // add a content view with a FrameLayout that holds the OAuth2 Fragment
        setContentView(R.layout.activity_demo);

        // Create a factory that returns the OAuth2Client
        OAuth2ClientFactory factory = new GoogleOauth2ClientFactory();

        // get an OAuth2Client
        mClient = factory.oAuth2Client(this);

        // Create a DoveCote that receives the result
        mGrantStateDovecote = new SerializableDovecote<>(this, "grant-state", this);

        if (savedInstanceState == null)
        {
            // start the authorization
            new BasicOAuth2Authorization(
                    factory,
                    new AuthorizationCodeGrant(mClient, new BasicScope("https://www.googleapis.com/auth/carddav")), R.id.oauth2fragment)
                    .withCage(mGrantStateDovecote.cage())
                    .start(this);
        }
    }


    @Override
    protected void onDestroy()
    {
        // get rid of the Dovecote
        mGrantStateDovecote.dispose();
        super.onDestroy();
    }


    @Override
    public void onPigeonReturn(@NonNull OAuth2InteractiveGrant.OAuth2GrantState oAuth2GrantState)
    {
        HttpRequestExecutor executor = new Following(
                new Retrying(
                        new HttpUrlConnectionExecutor(
                                new Finite(new DefaultHttpUrlConnectionFactory(), 5000, 5000))),
                new Secure(new FollowRedirectPolicy(5)));

        new AccessTokenTask(oAuth2GrantState.grant(mClient),
                new ThrowingAsyncTask.OnResultCallback<OAuth2AccessToken>()
                {
                    @Override
                    public void onResult(AsyncTaskResult<OAuth2AccessToken> result)
                    {
                        try
                        {
                            // the OAuth2AccessToken like so:
                            OAuth2AccessToken accessToken = result.value();
                            Log.v("xxxxxxxxxxx", "successfully authorized " + accessToken.accessToken());
                            Toast.makeText(DemoActivity.this, "successfully authorized " + accessToken.accessToken(), Toast.LENGTH_LONG).show();
                        }
                        catch (AuthorizationCancelledError e)
                        {
                            // the user pressed back to cancel the authorization process
                            Log.v("xxxxxxxxxxx", "cancelled ");
                            Toast.makeText(DemoActivity.this, "cancelled", Toast.LENGTH_LONG).show();
                        }
                        catch (ProtocolError e)
                        {
                            // an error occurred,  user probably didn't authorize the client
                            Log.v("xxxxxxxxxxx", "error  " + e.getMessage());
                            Toast.makeText(DemoActivity.this, "error" + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        catch (ProtocolException e)
                        {
                            // there was a protocol error, i.e. the server send an invalid response
                            Log.v("xxxxxxxxxxx", "error  " + e.getMessage());
                            Toast.makeText(DemoActivity.this, "error" + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        catch (Exception e)
                        {
                            // any other exception was thrown during the authorization
                            Log.v("xxxxxxxxxxx", "error  " + e.getMessage());
                            Toast.makeText(DemoActivity.this, "error" + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }
                }).execute(executor);
    }


    private final static class GoogleOauth2ClientFactory implements OAuth2ClientFactory
    {

        @Override
        public OAuth2Client oAuth2Client(Context context)
        {
            // This OAuth2ClientFactory returns a Google client
            return new BasicOAuth2Client(new GoogleAuthorizationProvider(),
                    new BasicOAuth2ClientCredentials(BuildConfig.OAUTH2_CLIENT_ID, BuildConfig.OAUTH2_CLIENT_SECRET),
                    new LazyUri(new Precoded("org.dmfs.oauth2.android.demo:/oauth2redirect")));
        }
    }
}
