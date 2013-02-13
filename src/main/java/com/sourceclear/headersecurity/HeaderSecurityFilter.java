package com.sourceclear.headersecurity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourceclear.headersecurity.serialization.ImmutableListDeserializer;
import com.sourceclear.headersecurity.serialization.ImmutableMapDeserializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 */
public class HeaderSecurityFilter implements Filter {
  
  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Gson GSON = new GsonBuilder()
          .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
          .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer())
          .create();  
  
  
  private static final String DEFAULT_CONFIG_NAME = "headerSecurity.conf";
  
  private static final String CONFIG_PARAM_NAME = "configFile";
  
  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
  private HeaderSecurityConfig config;
  
  private HeaderSecurityInjector injector;
  
  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\  
  
  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
  
  //------------------------ Implements: Filter
  
  public void init(FilterConfig fc) throws ServletException {
    try {
      String configFileName = DEFAULT_CONFIG_NAME;
      
      String configParam = fc.getInitParameter(CONFIG_PARAM_NAME);
      if (configParam != null) {
        configFileName = configParam;
      }
      
      InputStream is = fc.getServletContext().getResourceAsStream("/WEB-INF/" + configFileName);
      Reader reader = new InputStreamReader(is);
      config = GSON.fromJson(reader, HeaderSecurityConfig.class);
      injector = new HeaderSecurityInjector(config);
    } catch (Throwable t) {
      throw new ServletException("Couldn't initialize CspFilter", t);
    }
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
    injector.inject(response);
    fc.doFilter(request, response);
  }

  public void destroy() {}
  
  
  //------------------------ Overrides:
  
  //---------------------------- Abstract Methods -----------------------------
  
  //---------------------------- Utility Methods ------------------------------
  
  //---------------------------- Property Methods -----------------------------     
}
