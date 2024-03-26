- [Robustness](#robustness)
  - [No need to be highly available](#no-need-to-be-highly-available)
  - [Num of machines](#num-of-machines)

# Robustness
## No need to be highly available
* The scheduler and downloader is an offline system and could be restarted if needed. 
* However, they need to recover from failure such as timeout or parsing failures. Different types of exceptions need to be taken care of. 

## Num of machines
* From the previous analysis, the write throughput is around 800 RPS. A single scheduler machine will be enough. 

