- [Badge count](#badge-count)
  - [Question1: Whether to store badge and conversation count separately](#question1-whether-to-store-badge-and-conversation-count-separately)
  - [Question2: Keep consistency between badge and conversation count](#question2-keep-consistency-between-badge-and-conversation-count)
    - [Problems](#problems)
    - [Solution](#solution)
      - [Distributed lock](#distributed-lock)
      - [Transaction](#transaction)
      - [Lua script](#lua-script)
  - [Question3: How to be efficient in large group chat](#question3-how-to-be-efficient-in-large-group-chat)
    - [Problem: Notification storm](#problem-notification-storm)
    - [Solution](#solution-1)

# Badge count

![](../.gitbook/assets/Im_badge_count.png)

![](../.gitbook/assets/Im_badge_count_conversation.png)

## Question1: Whether to store badge and conversation count separately
* In theory, it is possible to calculate badge count from conversation count on the fly. 
* In practice, badge counter is used in a much higher frequency than these internal counters. If it is always calculated on the fly, then it will be a performance penalty. 
* So badge count and conversation count are usually stored separately. 

## Question2: Keep consistency between badge and conversation count
### Problems
* Total unread message increment and unread message against a specific person are two atomic operations. One could fail while the other one succeed. Or other clearing operations are being executed between these two operations.

![](../.gitbook/assets/im_badgeCount_inconsistency_scenario_1.png)

![](../.gitbook/assets/im_badgeCount_inconsistency_scenario_2.png)

### Solution
#### Distributed lock
* MC add, Redis setNX

#### Transaction
* Redis's MULTI, DISCARD, EXEC and WATCH operations. Optimistic lock.

#### Lua script

## Question3: How to be efficient in large group chat
### Problem: Notification storm
* Suppose that there is a 5000 people group and there are 10 persons speaking within the group per second, then QPS for updating unread messges will be 50K; When there are 1000 such groups, the QPS will be 50M

### Solution
* Solution: Aggregate and update
  1. There will be multiple queues A/B/C/... for buffering all incoming requests.
  2. Two components will be pulling from queues
     * Timer: Will be triggered after certain time
     * Flusher: Will be triggered if any of the queue exceed a certain length
  3. Aggregator service will pull msgs from Timer and Flusher, aggregate the read increment and decrement operations
* Cons:
  * Since there is no persistent on queues, if there is a restart, the number of unread messages will be inaccurate

![](../.gitbook/assets/im_badgeCount_aggregator.png)

