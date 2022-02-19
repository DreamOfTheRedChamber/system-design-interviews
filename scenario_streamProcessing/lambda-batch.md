- [Batch layer architecture](#batch-layer-architecture)
  - [Data model](#data-model)
  - [Storage requirements](#storage-requirements)
    - [DFS (Distributed file systems)](#dfs-distributed-file-systems)
    - [Partition](#partition)
    - [Pail on top of DFS](#pail-on-top-of-dfs)
    - [Recomputation vs incremental algorithm](#recomputation-vs-incremental-algorithm)
  - [Workflow overview](#workflow-overview)
    - [Time bucket](#time-bucket)
    - [Flowchart](#flowchart)
      - [Url normalization](#url-normalization)
    - [User id normalization](#user-id-normalization)
      - [PageView query](#pageview-query)
      - [Unique visitors](#unique-visitors)
      - [Bounce rate](#bounce-rate)

# Batch layer architecture
## Data model

![](../.gitbook/assets/lambda_batch_datamodel.png)

![](../.gitbook/assets/lambda_batch_pageview_model.png)

![](../.gitbook/assets/lambda_batch_pageview_model_person.png)

## Storage requirements

![](../.gitbook/assets/lambda_batch_storage_requirements.png)

### DFS (Distributed file systems)

![](../.gitbook/assets/lambda_batch_storage_distributedFileSystem.png)

### Partition

![](../.gitbook/assets/lambda_batch_storage_verticalpartition.png)

### Pail on top of DFS

![](../.gitbook/assets/lambda_batch_storage_pale.png)

### Recomputation vs incremental algorithm

![](../.gitbook/assets/lambda_batch_recomputation.png)

![](../.gitbook/assets/lambda_batch_incrementalgorithm.png)

![](../.gitbook/assets/lambda_batch_algo_comparison.png)

![](../.gitbook/assets/lambda_batch_algo_comparison2.png)


## Workflow overview

### Time bucket

![](../.gitbook/assets/lambda_batch_pageview_hourly_granulariry.png)

![](../.gitbook/assets/lambda_batch_pageview_bucketCoarser.png)

![](../.gitbook/assets/lambda_batch_pageview_bucket_num.png)

### Flowchart

![](../.gitbook/assets/lambda_batch_workflow.png)

#### Url normalization
![](../.gitbook/assets/lambda_batch_workflow_urlNorm.png)

### User id normalization

![](../.gitbook/assets/lambda_batch_workflow_userIdNorm.png)

#### PageView query

![](../.gitbook/assets/lambda_batch_query_pageviews.png)

![](../.gitbook/assets/lambda_batch_query_bouncerate.png)

#### Unique visitors

![](../.gitbook/assets/lambda_batch_query_uniqueVisitor.png)

#### Bounce rate

![](../.gitbook/assets/lambda_batch_query_real_bounce_rate.png)
