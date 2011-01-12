/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.antx.config.resource.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.resource.Resource;
import com.alibaba.antx.config.resource.ResourceNotFoundException;
import com.alibaba.antx.config.resource.ResourceURI;
import com.alibaba.antx.config.resource.util.ApacheIndexPageParser;
import com.alibaba.antx.config.resource.util.IndexPageParser;
import com.alibaba.antx.config.resource.util.ResourceContext;
import com.alibaba.antx.config.resource.util.ResourceKey;
import com.alibaba.antx.config.resource.util.SvnIndexPageParser;

public class HttpResource extends Resource {
    private byte[] content;
    private String charset;
    private String contentType;

    public HttpResource(HttpSession session, ResourceURI uri) {
        super(session, uri);
    }

    public Resource getRelatedResource(String suburi) {
        return new HttpResource((HttpSession) getSession(), getURI().getSubURI(suburi));
    }

    public String getContentType() {
        load();
        return contentType;
    }

    public String getCharset() {
        load();
        return charset;
    }

    public byte[] getContent() {
        load();
        return content;
    }

    private void load() {
        if (content != null) {
            return;
        }

        GetMethod httpget = new GetMethod(getURI().toString());
        httpget.setDoAuthentication(true);
        InputStream stream = null;

        try {
            ResourceContext.get().setCurrentURI(getURI().getURI());

            ((HttpSession) getSession()).getClient().executeMethod(httpget);

            if (httpget.getStatusCode() != 200) {
                throw new ResourceNotFoundException(HttpStatus.getStatusText(httpget.getStatusCode()));
            }

            // 成功就清除，以避免重复提示输入密码
            ResourceContext.get().getVisitedURIs().remove(new ResourceKey(new ResourceURI(getURI().getURI())));

            content = httpget.getResponseBody();
            charset = httpget.getResponseCharSet();

            Header contentTypeHeader = httpget.getResponseHeader("Content-Type");
            contentType = contentTypeHeader == null ? null : contentTypeHeader.getValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException(e);
        } finally {
            ResourceContext.get().setCurrentURI(null);

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }

            httpget.releaseConnection();
        }
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getContent());
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    public boolean isDirectory() {
        return getURI().guessDirectory();
    }

    public List list() {
        if (isDirectory()) {
            String overridingCharset = getURI().getOption("charset");
            IndexPageParser[] parsers = new IndexPageParser[] { new SvnIndexPageParser(),
                    new ApacheIndexPageParser(overridingCharset) };
            List items = null;

            for (int i = 0; i < parsers.length; i++) {
                IndexPageParser parser = parsers[i];

                items = parser.parse(this);

                if (items != null) {
                    break;
                }
            }

            if (items != null) {
                for (ListIterator i = items.listIterator(); i.hasNext();) {
                    IndexPageParser.Item item = (IndexPageParser.Item) i.next();
                    Resource resource = new HttpResource((HttpSession) getSession(), getURI().getSubURI(item.getName(),
                            item.isDirectory()));

                    i.set(resource);
                }
            }

            return items;
        }

        return null;
    }
}
