/*
 * The MIT License
 *
 * Copyright (c) 2010 Pablo Fernandez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.scribejava.httpclient.okhttp;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MockCall implements Call {

    private final Collection<Callback> callbacks = new ArrayList<>();
    private boolean canceled;

    @Override
    public void enqueue(Callback responseCallback) {
        callbacks.add(responseCallback);
    }

    @Override
    public void cancel() {
        canceled = true;
        for (Callback callback : callbacks) {
            callback.onFailure(this, new IOException("Canceled"));
        }
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public Request request() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Response execute() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isExecuted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MockCall clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Timeout timeout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
