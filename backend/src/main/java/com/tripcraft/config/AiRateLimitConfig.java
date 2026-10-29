package com.tripcraft.config;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimitConfig implements WebMvcConfigurer {
  private final ConcurrentHashMap<String,Bucket> buckets=new ConcurrentHashMap<>();
  private Bucket bucket(){return Bucket.builder().addLimit(Bandwidth.classic(20, Refill.greedy(20,Duration.ofMinutes(1)))).build();}
  @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new AiInterceptor());}
  private class AiInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest r,HttpServletResponse response,Object handler)throws Exception{
      if(!r.getRequestURI().startsWith("/api/ai/")) return true;
      Bucket b=buckets.computeIfAbsent(r.getRemoteAddr(),k->bucket());
      if(b.tryConsume(1)) return true;
      response.setStatus(429); response.setContentType("application/json");
      response.getWriter().write("{\"code\":\"AI_RATE_LIMIT\",\"message\":\"Please try again in a minute.\"}"); return false;
    }
  }
}
