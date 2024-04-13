- [Hot key](#hot-key)

# Hot key
* Solutions
  * Note: All 1-4 bullet points could be used separately.
  * Detect hot key (step2/3)
    * The one showed in the flowchart is a dynamic approach. There are several ways to decide hot keys:
      * Within proxy layer
      * Within client
      * Use redis shipped commands ./redis-cli --hotkeys
  * Randomly hash to multiple nodes instead of only one (step4)
  * Enable local cache for hot keys (step5)
  * Circuit breaker kicks in if detecting cache failure (step6)
  * References: [https://juejin.im/post/6844903765733015559](https://juejin.im/post/6844903765733015559)

```
   ┌───────────────┐                                                                                    
   │               │                                                                                    
   │    Client     │                                                                                    
   │               │                                                                                    
   │               │                                                                                    
   └───────────────┘                                                                                    
     │    │     │                                                                                       
     │    │     │                                                                                       
     │    │     │                                                               ┌──────────────────────┐
     │    │     │                                                               │ Configuration center │
     │    │     │    ─ ─ ─ ─ ─ ─ ─ step0. subscribe to hot key changes ─ ─ ─ ─ ▶│                      │
     │    │     │   │                                                           │   (e.g. Zookeeper)   │
     │  Step1:  │                                                               └┬─────────────────────┘
     │ Requests │   │                                                            │          ▲           
     │ come in  │                                                                │          │           
     │    │     │   │                                                            │          │           
     │    │     │   ┌─────────────Step3. Hot key change is published─────────────┘          │           
     │    │     │   │                                                                       │           
     │    │     │   │                                                                       │           
     │    │     │   │                                                                     Yes           
     │    │     │   │                                                                       │           
     ▼    ▼     ▼   ▼                                                                       │           
   ┌─────────────────────────────────┐                                                      │           
   │           App Cluster           │                                                      │           
   │                                 │    step 2:    ┌─────────────────────────┐       .─────────.      
   │ ┌ ─ ─ ─ ┐  ┌ ─ ─ ─ ┐  ┌ ─ ─ ─ ┐ │   aggregate   │    Stream processing    │      ╱           ╲     
   │   local      local      local   ├───to detect ─▶│                         │────▶(Is it hot key)    
   │ │ cache │  │ cache │  │ cache │ │    hot keys   │      (e.g. Flink)       │      `.         ,'     
   │  ─ ─ ─ ─    ─ ─ ─ ─    ─ ─ ─ ─  │               └─────────────────────────┘        `───────'       
   │     ▲                           │                                                                  
   │ ┌ ─ ╬ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐ │                                                                  
   │     ║  step 6. circuit breaker  │                                                                  
   │ │   ║                         │ │                                                                  
   │  ─ ─║─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │                                                                  
   └─────╬───────────────────────────┘                                                                  
         ║          │                  step4. For the same hot key,                                     
         ║          │                 randomly map to multiple nodes                                    
┌──────────────┐    │                        instead of only 1                                          
│ Step5. Cache │    └───────────────┬──────────────────────────────────────┐                            
│hot key within│                    │                                      │                            
│ local cache  │                    │                                      │                            
└──────────────┘         ┌──────────▼──────────────────────────────────────▼──────────┐                 
                         │  ┌ ─ ─ ─ ─ ─ ─ ─     ┌ ─ ─ ─ ─ ─ ─ ─    ┌ ─ ─ ─ ─ ─ ─ ─    │                 
                         │    distributed  │      distributed  │     distributed  │   │                 
                         │  │ cache node A      │ cache node B     │ cache node C     │                 
                         │   ─ ─ ─ ─ ─ ─ ─ ┘     ─ ─ ─ ─ ─ ─ ─ ┘    ─ ─ ─ ─ ─ ─ ─ ┘   │                 
                         │                                                            │                 
                         │                       Cache Cluster                        │                 
                         └────────────────────────────────────────────────────────────┘
```

