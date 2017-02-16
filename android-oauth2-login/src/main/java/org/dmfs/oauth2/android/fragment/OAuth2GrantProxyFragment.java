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

package org.dmfs.oauth2.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.oauth2.android.OAuth2ClientFactory;
import org.dmfs.oauth2.android.activities.AuthProxyActivity;
import org.dmfs.oauth2.android.errors.AuthorizationCancelledError;
import org.dmfs.oauth2.android.errorstates.ErrorOAuth2GrantState;
import org.dmfs.oauth2.android.errorstates.ExceptionOAuth2GrantState;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.pigeonpost.Cage;
import org.dmfs.pigeonpost.Pigeon;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;

import java.io.Serializable;


/**
 * A headless fragment serves as a proxy to the {@link AuthProxyActivity}. It launches {@link AuthProxyActivity} with the right parameters and receives any
 * result. The result is then send with a {@link Pigeon}.
 *
 * @author Marten Gajda
 */
public final class OAuth2GrantProxyFragment extends Fragment
{
    public final static String ARG_OAUTH2_CLIENT_FACTORY = "oauth2_client_factory";
    public final static String ARG_OAUTH2_GRANT_STATE = "oauth2_grant_state";
    public final static String ARG_RESULT_STATE_PIGEON_CAGE = "result_state_pigeon_cage";
    public final static String KEY_STATE_STARTED = "org.dmfs.started";

    private OAuth2Client mClient;
    private OAuth2InteractiveGrant mGrant;
    private boolean mStarted;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // restore client and grant
        Bundle arguments = getArguments();
        mClient = ((OAuth2ClientFactory) arguments.getSerializable(ARG_OAUTH2_CLIENT_FACTORY)).oAuth2Client(getActivity());
        mGrant = ((OAuth2InteractiveGrant.OAuth2GrantState) arguments.getSerializable(ARG_OAUTH2_GRANT_STATE)).grant(mClient);
        if (savedInstanceState != null)
        {
            mStarted = savedInstanceState.getBoolean(KEY_STATE_STARTED);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (!mStarted)
        {
            Uri authUrl = Uri.parse(mGrant.authorizationUrl().toASCIIString());
            Intent authIntent;
            if (Build.VERSION.SDK_INT >= 15)
            {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha));
                builder.setShowTitle(true);
                CustomTabsIntent customTabsIntent = builder.build();
                authIntent = customTabsIntent.intent.setData(authUrl);
            }
            else
            {
                authIntent = new Intent(Intent.ACTION_VIEW, authUrl);
            }
            startActivityForResult(new Intent(getActivity(), AuthProxyActivity.class).putExtra(AuthProxyActivity.EXTRA_AUTH_INTENT, authIntent), 1);
            mStarted = true;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            try
            {
                sendState(mGrant.withRedirect(new LazyUri(new Precoded(data.getDataString()))).state());
            }
            catch (ProtocolError e)
            {
                sendState(new ErrorOAuth2GrantState(e));
            }
            catch (ProtocolException e)
            {
                sendState(new ExceptionOAuth2GrantState(e));
            }
        }
        else
        {
            sendState(new ErrorOAuth2GrantState(new AuthorizationCancelledError("Authorization cancelled by user.")));
        }
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_STATE_STARTED, mStarted);
    }


    private void sendState(OAuth2InteractiveGrant.OAuth2GrantState state)
    {
        // get the Cage and send a Pigeon with the result
        Cage<Serializable> serializableCage = getArguments().getParcelable(ARG_RESULT_STATE_PIGEON_CAGE);
        serializableCage.pigeon(state).send(getActivity());
    }
}
