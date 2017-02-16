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

package org.dmfs.oauth2.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.dmfs.oauth2.android.fragment.InteractiveGrantFragment;
import org.dmfs.oauth2.android.fragment.OAuth2GrantProxyFragment;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.pigeonpost.Cage;


/**
 * Basic implementation of an {@link OAuth2Authorization}.
 *
 * @author Marten Gajda
 */
public final class BasicOAuth2Authorization implements OAuth2Authorization
{
    private final int mFragmentHostId;
    private final Bundle mArguments;


    public BasicOAuth2Authorization(OAuth2ClientFactory oAuth2CientFactory, OAuth2InteractiveGrant interactiveGrant, int fragmentHostId)
    {
        this(new Bundle(), fragmentHostId);
        mArguments.putSerializable(InteractiveGrantFragment.ARG_OAUTH2_CLIENT_FACTORY, oAuth2CientFactory);
        mArguments.putSerializable(InteractiveGrantFragment.ARG_OAUTH2_GRANT_STATE, interactiveGrant.state());
    }


    private BasicOAuth2Authorization(Bundle arguments, int fragmentHostId)
    {
        mArguments = arguments;
        mFragmentHostId = fragmentHostId;
    }


    @Override
    public OAuth2Authorization withCage(Cage<OAuth2InteractiveGrant.OAuth2GrantState> resultStatePigeon)
    {
        Bundle arguments = new Bundle(mArguments);
        arguments.putParcelable(InteractiveGrantFragment.ARG_RESULT_STATE_PIGEON_CAGE, resultStatePigeon);
        return new BasicOAuth2Authorization(arguments, mFragmentHostId);
    }


    @Override
    public void start(@NonNull FragmentActivity fragmentActivity)
    {
        OAuth2GrantProxyFragment oAuth2GrantProxyFragment = new OAuth2GrantProxyFragment();
        oAuth2GrantProxyFragment.setArguments(mArguments);
        fragmentActivity.getSupportFragmentManager().beginTransaction().add(mFragmentHostId, oAuth2GrantProxyFragment).commit();
    }
}
