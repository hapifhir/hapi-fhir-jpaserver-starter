package com.iprd.fhir.utils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Map;

public class ModifiedBodyRequestWrapper extends HttpServletRequestWrapper {
    private final String modifiedBody;
    private final Map<String, String> modifiedHeaders;
    
    public ModifiedBodyRequestWrapper(HttpServletRequest request, String modifiedBody,Map<String, String> modifiedHeaders) {
        super(request);
        this.modifiedBody = modifiedBody;
        this.modifiedHeaders = modifiedHeaders;
        	
    }

    @Override
    public String getHeader(String name) {
        // Check if the modified headers contain the specified header
        if (modifiedHeaders.containsKey(name)) {
            return modifiedHeaders.get(name);
        } else {
            // If the header is not found in the modified headers, delegate to the original request
            return super.getHeader(name);
        }
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(modifiedBody.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// TODO Auto-generated method stub
				
			}
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
