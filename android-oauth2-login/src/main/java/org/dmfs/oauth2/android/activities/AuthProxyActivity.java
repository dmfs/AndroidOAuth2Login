/*
 * Copyright 2017 dmfs GmbH
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

package org.dmfs.oauth2.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public final class AuthProxyActivity extends Activity
{
    public final static String EXTRA_AUTH_INTENT = "org.dmfs.extras.auth_intent";
    private boolean mAuthStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mAuthStarted = savedInstanceState.getBoolean("auth", false);
        }
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        // TODO can we use getCallingActivity == null to decide that?
        if (getIntent().getBooleanExtra("org.dmfs.auth.completed", false))
        {
            // the current intent is already completed, we can't do that another time
            // notify user about error and quit
            // TODO: show an activity that explains what happened
            Toast.makeText(this, "Authentication flow already completed or interrupted. Please close this tab.", Toast.LENGTH_LONG).show();
            finish();
        }
        setIntent(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("auth", mAuthStarted);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (!mAuthStarted)
        {
            Intent authIntent = getIntent().getParcelableExtra(EXTRA_AUTH_INTENT);
            startActivity(authIntent);
            // reset URI so we can distinguish any result from the cancelled state
            mAuthStarted = true;
        }
        else
        {
            setResult(getIntent().getData() != null ? RESULT_OK : RESULT_CANCELED, getIntent());
            finish();
        }
    }
}
