package com.tripcraft.service;

import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.client.SimpleClientHttpRequestFactory; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient;
import java.math.BigDecimal; import java.util.*; import java.util.regex.*;

@Service public class AiService {
  private final JdbcTemplate db; private final PlanBuilderService plans; private final String key,model; private final RestClient client; private final ObjectMapper mapper;
  public AiService(JdbcTemplate db,PlanBuilderService plans,ObjectMapper mapper,@Value("${llm.api-key:}")String key,@Value("${llm.model:}")String model,@Value("${llm.base-url:https://api.openai.com/v1}")String base){this.db=db;this.plans=plans;this.mapper=mapper;this.key=key;this.model=model;SimpleClientHttpRequestFactory f=new SimpleClientHttpRequestFactory();f.setConnectTimeout(5000);f.setReadTimeout(8000);client=RestClient.builder().baseUrl(base).requestFactory(f).build();}
  public boolean configured(){return key!=null&&!key.isBlank()&&model!=null&&!model.isBlank();}
  public String getModel(){return model;}

  public Map<String,Object> budget(Map<String,Object> raw,Long user){
    long started=System.currentTimeMillis();
    Map<String,Object> input=extract(raw);
    try{
      if(input.get("followUpQuestion")!=null) return input;
      int people=((Number)input.get("people")).intValue();
      int days=((Number)input.get("days")).intValue();
      BigDecimal budget=new BigDecimal(String.valueOf(input.get("budget")));
      List<Map<String,Object>> built=plans.build(budget,people,days,(String)input.get("startingCity"));
      String provider="rule-based";
      List<String> llmExplanations=new ArrayList<>();

      if(configured()&&!built.isEmpty()){
        try{
          String plansJson=mapper.writeValueAsString(built);
          Map<?,?> response=client.post().uri("/chat/completions")
            .header("Authorization","Bearer "+key)
            .body(Map.of(
              "model",model,
              "temperature",0.3,
              "messages",List.of(
                Map.of("role","system","content","You are an expert travel planner. Explain each supplied database-backed travel package in 1 concise sentence highlighting why it fits the traveler's budget and pace. Return a JSON object with key 'explanations' as an array of strings in matching order."),
                Map.of("role","user","content","Travel plans:\n"+plansJson)
              )
            )).retrieve().body(Map.class);
          if(response!=null&&response.containsKey("choices")){
            List<?> choices=(List<?>)response.get("choices");
            if(!choices.isEmpty()){
              Map<?,?> msg=(Map<?,?>)((Map<?,?>)choices.get(0)).get("message");
              String content=String.valueOf(msg.get("content"));
              try{
                Map<?,?> parsed=mapper.readValue(content,Map.class);
                if(parsed.get("explanations") instanceof List<?> list){
                  for(Object item:list) llmExplanations.add(String.valueOf(item));
                }
              }catch(Exception ex){
                if(content!=null&&!content.isBlank()){
                  llmExplanations.add(content.trim());
                }
              }
              provider="llm-backed";
            }
          }
        }catch(Exception ignored){}
      }

      String[] tiers={"Budget","Standard","Best value"};
      for(int i=0;i<built.size();i++){
        Map<String,Object> x=built.get(i);
        x.put("tier",tiers[i % tiers.length]);
        if(!llmExplanations.isEmpty()&&i<llmExplanations.size()){
          x.put("explanation",llmExplanations.get(i));
        } else {
          String pkgTitle=String.valueOf(x.get("title"));
          String destName=String.valueOf(x.get("destinationName"));
          x.put("explanation","Curated "+destName+" itinerary ("+pkgTitle+") balanced for your "+people+"-person group within budget.");
        }
      }

      Map<String,Object> result=new LinkedHashMap<>();
      result.put("provider",provider);
      result.put("filters",input);
      result.put("estimateNote","Prices are estimates until booking is confirmed.");
      result.put("plans",built);
      if(built.isEmpty()) result.put("message","This budget is too low for the selected group. Try fewer days, a cheaper hotel, or a nearer destination.");
      log(user,input,result,provider,true,started);
      return result;
    }catch(Exception e){
      log(user,input,Map.of(),"fallback",false,started);
      throw e;
    }
  }

  private Map<String,Object> extract(Map<String,Object> raw){
    Map<String,Object> x=new LinkedHashMap<>(raw);
    String prompt=String.valueOf(raw.getOrDefault("prompt",""));
    if(!prompt.isBlank()&&configured()&&raw.get("budget")==null){
      try{Map<String,Object> parsed=parseWithLlm(prompt);x.putAll(parsed);}catch(Exception ignored){}
    }
    if(!prompt.isBlank()){
      if(raw.get("budget")==null){
        Matcher m=Pattern.compile("(?:budget[:\\s]*|rs\\.?\\s*|₹\\s*)([0-9][0-9,]+)|([0-9][0-9,]+)\\s*(?:budget|rs|inr|₹)",Pattern.CASE_INSENSITIVE).matcher(prompt);
        if(m.find()){
          String b=m.group(1)!=null?m.group(1):m.group(2);
          x.put("budget",new BigDecimal(b.replace(",","")));
        }
      }
      if(raw.get("people")==null){
        Matcher m=Pattern.compile("(\\d+)\\s*(?:people|persons|travelers|guests|members)|family\\s+of\\s+(\\d+)",Pattern.CASE_INSENSITIVE).matcher(prompt);
        if(m.find()){
          String p=m.group(1)!=null?m.group(1):m.group(2);
          x.put("people",Integer.valueOf(p));
        }
      }
      if(raw.get("days")==null){
        Matcher m=Pattern.compile("(\\d+)\\s*days?|(\\d+)\\s*nights?",Pattern.CASE_INSENSITIVE).matcher(prompt);
        if(m.find()){
          String d=m.group(1)!=null?m.group(1):m.group(2);
          x.put("days",Integer.valueOf(d));
        } else if(Pattern.compile("\\bweekend\\b",Pattern.CASE_INSENSITIVE).matcher(prompt).find()){
          x.put("days",2);
        }
      }
      Matcher dest=Pattern.compile("(?:to|in|visit|near|around|from)\\s+([A-Za-z ]+?)(?:,|;|\n|\\.|\\s+for|\\s+with|\\s+budget|$)",Pattern.CASE_INSENSITIVE).matcher(prompt);
      if(dest.find()){
        x.put("startingCity",dest.group(1).trim());
      }
    }
    if(x.get("budget")==null||x.get("people")==null||x.get("days")==null){
      x.clear();
      x.put("followUpQuestion","What budget, group size, and trip length should I plan for?");
    }
    return x;
  }

  @SuppressWarnings("unchecked")
  private Map<String,Object> parseWithLlm(String prompt)throws Exception{
    Exception last=null;
    for(int attempt=0;attempt<2;attempt++){
      try{
        Map<String,Object> response=client.post().uri("/chat/completions")
          .header("Authorization","Bearer "+key)
          .body(Map.of(
            "model",model,
            "temperature",0,
            "response_format",Map.of("type","json_object"),
            "messages",List.of(
              Map.of("role","system","content","Extract JSON with numeric budget, people, days, optional startingCity, interests, constraints. Ask no questions and never invent missing values. Return JSON only."),
              Map.of("role","user","content",attempt==0?prompt:prompt+"\nReturn valid JSON only with numeric budget, people, and days.")
            )
          )).retrieve().body(Map.class);
        List<?> choices=(List<?>)response.get("choices");
        Map<?,?> message=(Map<?,?>)((Map<?,?>)choices.get(0)).get("message");
        return mapper.readValue(String.valueOf(message.get("content")),Map.class);
      }catch(Exception e){last=e;}
    }
    throw last;
  }

  private void log(Long user,Map<String,Object> request,Map<String,Object> response,String provider,boolean ok,long started){
    try{
      db.update("INSERT INTO ai_logs(user_id,endpoint,request_summary,response_summary,provider,success,latency_ms) VALUES(?,?,?,?,?,?,?)",
        user,"/api/ai/budget-plan",mapper.writeValueAsString(request),mapper.writeValueAsString(response),provider,ok,System.currentTimeMillis()-started);
    }catch(Exception ignored){}
  }
}
