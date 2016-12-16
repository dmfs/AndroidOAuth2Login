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

/**
 * An {@link AsyncTaskResult} that carries an {@link Exception}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class ThrowingAsyncTaskResult<T> implements AsyncTaskResult<T>
{
    private final Exception mException;


    public ThrowingAsyncTaskResult(Exception exception)
    {
        mException = exception;
    }


    @Override
    public T value() throws Exception
    {
        throw mException;
    }
}
