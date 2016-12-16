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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.oauth2.android.OAuth2ClientFactory;
import org.dmfs.oauth2.android.errors.AuthorizationCancelledError;
import org.dmfs.oauth2.android.errorstates.ErrorOAuth2GrantState;
import org.dmfs.oauth2.android.errorstates.ExceptionOAuth2GrantState;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.pigeonpost.Cage;

import java.io.Serializable;
import java.net.URI;


/**
 * A fragment that handles the UI part an interactive grant.
 *
 * @author Marten Gajda
 */
public final class InteractiveGrantFragment extends Fragment implements View.OnKeyListener
{
    public final static String ARG_OAUTH2_CLIENT_FACTORY = "oauth2_client_factory";
    public final static String ARG_OAUTH2_GRANT_STATE = "oauth2_grant_state";
    public final static String ARG_RESULT_STATE_PIGEON_CAGE = "result_state_pigeon_cage";

    private OAuth2Client mClient;
    private OAuth2InteractiveGrant mGrant;
    private WebView mWebView;
    private FrameLayout mRootView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // we need this fragment to be retained, otherwise it looses the status
        setRetainInstance(true);
        // restore client and grant
        Bundle arguments = getArguments();
        mClient = ((OAuth2ClientFactory) arguments.getSerializable(ARG_OAUTH2_CLIENT_FACTORY)).oAuth2Client(getActivity());
        mGrant = ((OAuth2InteractiveGrant.OAuth2GrantState) arguments.getSerializable(ARG_OAUTH2_GRANT_STATE)).grant(mClient);
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        if (mWebView == null)
        {
            // create and configure the WebView
            // Note using just getActivity would will leak the activity.
            mWebView = new WebView(getActivity().getApplicationContext());
            mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setOnKeyListener(this);
            mWebView.setWebViewClient(mGrantWebViewClient);
            mWebView.loadUrl(mGrant.authorizationUrl().toASCIIString());

            // wipe cookies to enforce a new login
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            }
            else
            {
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getActivity());
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }
        }
        return mWebView;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (mWebView != null)
        {
            mWebView.onResume();
        }
    }


    @Override
    public void onPause()
    {
        if (mWebView != null)
        {
            mWebView.onPause();
        }
        super.onPause();
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }


    @Override
    public void onDestroy()
    {
        if (mWebView != null)
        {
            mWebView.destroy();
        }
        super.onDestroy();
    }


    private final WebViewClient mGrantWebViewClient = new WebViewClient()
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            URI newUrl = URI.create(url);
            URI redirectUri = mClient.redirectUri();
            if (redirectUri.getRawAuthority().equals(newUrl.getRawAuthority()) && redirectUri.getScheme()
                    .equals(newUrl.getScheme()) && redirectUri.getRawPath().equals(newUrl.getRawPath()))
            {
                try
                {
                    sendState(mGrant.withRedirect(newUrl).state());
                }
                catch (final ProtocolError e)
                {
                    sendState(new ErrorOAuth2GrantState(e));
                }
                catch (ProtocolException e)
                {
                    sendState(new ExceptionOAuth2GrantState(e));
                }
                return true;
            }
            return false;
        }
    };


    private void sendState(OAuth2InteractiveGrant.OAuth2GrantState state)
    {
        // get the Cage and send a Pigeon with the result
        Cage<Serializable> serializableCage = getArguments().getParcelable(ARG_RESULT_STATE_PIGEON_CAGE);
        serializableCage.pigeon(state).send(getActivity());
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK)
            {
                if (mWebView.canGoBack())
                {
                    // user went back a step
                    mWebView.goBack();
                }
                else
                {
                    // the user cancelled the authorization flow
                    sendState(new ErrorOAuth2GrantState(new AuthorizationCancelledError("Authorization cancelled by user.")));
                }
                return true;
            }
        }
        if (event.getAction() == KeyEvent.ACTION_UP)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK)
            {
                // also capture the up event
                return true;
            }
        }
        return false;
    }
}
