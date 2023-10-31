package com.bank.config;

import com.alibaba.fastjson.JSONObject;
import com.bank.common.Result;
import com.bank.model.SysUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class SecurityFilterConfig {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 解决跨域
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 允许向该服务器提交请求的URI，*表示全部允许
        config.addAllowedOriginPattern("*");
        // 允许访问的头信息,*表示全部
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.setMaxAge(18000L);
        // 允许提交请求的方法，*表示全部允许
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("*/**", config);
        return new CorsFilter(source);
    }

    /**
     * 对个别接口访问放行引入HttpSecurity组件，来管理请求
     *
     * @return 一个安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        //
        httpSecurity.cors();
        httpSecurity.authorizeHttpRequests().antMatchers(
                "/captcha",
                "/doc.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/**",
                "/api/**",
                "/addCounter"
                ).permitAll().anyRequest().authenticated();
        httpSecurity.addFilterAt(new UsernamePasswordAuthenticationFilter() {
            @Override
            public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
                System.out.println("开始认证！");
                try {
                    Map params = objectMapper.readValue(request.getInputStream(), Map.class);
                    String username = (String) params.get("username");
                    String password = (String) params.get("password");
                    System.out.println("用户&密码：" + username + "=========" + password);
                    UsernamePasswordAuthenticationToken upToken = UsernamePasswordAuthenticationToken
                            .unauthenticated(username, password);
                    return authenticationManager.authenticate(upToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            /**
             * 认证成功之后调用的方法
             * @param request
             * @param response
             * @param chain
             * @param authResult
             * @throws IOException
             */
            @Override
            protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
                // 1.认证成功之后，给该用户办法TOKEN，并该TOKEN存入REDIS
                // 2.并设置
                UUID token = UUID.randomUUID();
                String finalToken = token.toString();
                System.out.println("uuid:" + finalToken);
                finalToken = finalToken.replace("-", "");
                // 以token为key，以用户名称 + 登录时间为value，存入redis
                long currentTime = System.currentTimeMillis();
                // 获取用户名称
                SysUser sysUser = (SysUser) authResult.getPrincipal();
                String username = sysUser.getUsername();
                System.out.println("username:" + username);
                // 引入RedisTemplate,将TOKEN,USERNAME，当前时间存入redis
                HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
                hashOperations.put(finalToken, "currentTime", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                hashOperations.put(finalToken, "username", username);
                // 过期时间
                redisTemplate.expire(finalToken, 30, TimeUnit.SECONDS);
                Map<String, Object> map = hashOperations.entries(finalToken);
                map.entrySet().forEach(System.out::println);

                // 将生成的TOKEN返回给调用端（客户端、第三方）
                Result result = new Result("200", "认证成功", finalToken);
                // 转JSON
                String json = JSONObject.toJSONString(result);
                // 设置响应类型为 application/json
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().print(json);
                response.getWriter().close();
            }

            /**
             * 认证失败之后调用的方法
             * @param request
             * @param response
             * @param failed
             * @throws IOException
             */
            @Override
            protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
                super.unsuccessfulAuthentication(request, response, failed);
            }
        }, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.addFilter(new BasicAuthenticationFilter(authenticationManager) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                // 判断有没有带TOKEN，以及TOKEN有没有效
                Enumeration<String> enumeration = request.getHeaderNames();
                while (enumeration.hasMoreElements()) {
                    String headerName = enumeration.nextElement();
                    System.out.println(headerName + "===" + request.getHeader(headerName));
                }
                // 不是postman，使用request.getParameter()来获取TOKEN
                String token = request.getParameter("authorization");
                if (StringUtils.hasLength(token)) {
                    token = token.substring("Bearer ".length());
                    System.out.println(token);
                    // 根据token，到redis库查询token是否有效
                    HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
                    Map<String, Object> map = hashOperations.entries(token);
                    if (!map.isEmpty()) {
                        UsernamePasswordAuthenticationToken upaToken = new UsernamePasswordAuthenticationToken(null, null, new ArrayList<>());
                        SecurityContextHolder.getContext().setAuthentication(upaToken);
                    } else {
                        Result result = new Result("403", "未授权!", "-1");
                        String jsonString = JSONObject.toJSONString(result);
                        response.getWriter().print(jsonString);
                        response.getWriter().close();
                        return;
                    }
                }
                super.doFilterInternal(request, response, chain);
            }
        });
        httpSecurity.csrf().disable();
        return httpSecurity.build();
    }
}
