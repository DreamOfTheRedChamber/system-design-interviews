- [Cron to put into message queue after expiration](#cron-to-put-into-message-queue-after-expiration)
  - [Flowchart](#flowchart)
  - [Cons](#cons)

# Cron to put into message queue after expiration

## Flowchart
* Task producers set timer to send tasks to queue after certain time (30minutes). 
* The most popular timer implementation is based on Cron job. 
  * Cron and crontab: https://www.hostgator.com/help/article/what-are-cron-jobs

![Flowchart](../.gitbook/assets/delayQueue_cronjob.png)

## Cons
* Could not support high concurrent volume because most timer middleware don't support it. 
