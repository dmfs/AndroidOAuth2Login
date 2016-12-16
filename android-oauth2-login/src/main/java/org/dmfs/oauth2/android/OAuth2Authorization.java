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

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.pigeonpost.Cage;
import org.dmfs.pigeonpost.Pigeon;


/**
 * @author Marten Gajda
 */
public interface OAuth2Authorization
{
    /**
     * Create an updated {@link OAuth2Authorization} with the given pigeon {@link Cage} to retrieve the authorization result.
     *
     * @param resultPigeonCage
     *         A {@link Cage} that returns {@link Pigeon}s that can carry an {@link OAuth2InteractiveGrant.OAuth2GrantState}.
     *
     * @return An {@link OAuth2Authorization} with the given {@link Cage}.
     */
    OAuth2Authorization withCage(Cage<OAuth2InteractiveGrant.OAuth2GrantState> resultPigeonCage);

    /**
     * Start this {@link OAuth2Authorization}.
     *
     * @param fragmentActivity
     *         The hosting {@link FragmentActivity}.
     */
    void start(@NonNull FragmentActivity fragmentActivity);
}
