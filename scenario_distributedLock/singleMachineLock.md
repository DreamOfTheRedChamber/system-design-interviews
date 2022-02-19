- [Synchronized lock](#synchronized-lock)
  - [Method](#method)
  - [Block](#block)
- [Reentrant lock](#reentrant-lock)

# Synchronized lock

## Method

```text
    @Transactional(rollbackFor = Exception.class)
    public synchronized Integer createOrder() throws Exception
    {
        Product product = null;

        // !!! Manual transaction management is required. Otherwise, 
        TransactionStatus transaction1 = platformTransactionManager.getTransaction(transactionDefinition);
        product = productMapper.selectByPrimaryKey(purchaseProductId);
        if (product==null)
        {
            platformTransactionManager.rollback(transaction1);
            throw new Exception("item："+purchaseProductId+"does not exist");
        }

        // current inventory
        Integer currentCount = product.getCount();
        System.out.println(Thread.currentThread().getName()+"number of inventory："+currentCount);

        // check against inventory
        if (purchaseProductNum > currentCount)
        {
            platformTransactionManager.rollback(transaction1);
            throw new Exception("item"+purchaseProductId+"only has"+currentCount+" inventory，not enough for purchase");
        }

        productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());
        platformTransactionManager.commit(transaction1);

        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        Order order = new Order();
        order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
        order.setOrderStatus(1);//待处理
        order.setReceiverName("xxx");
        order.setReceiverMobile("13311112222");
        order.setCreateTime(new Date());
        order.setCreateUser("xxx");
        order.setUpdateTime(new Date());
        order.setUpdateUser("xxx");
        orderMapper.insertSelective(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setPurchasePrice(product.getPrice());
        orderItem.setPurchaseNum(purchaseProductNum);
        orderItem.setCreateUser("xxx");
        orderItem.setCreateTime(new Date());
        orderItem.setUpdateTime(new Date());
        orderItem.setUpdateUser("xxx");
        orderItemMapper.insertSelective(orderItem);
        platformTransactionManager.commit(transaction);
        return order.getId();
    }
```

## Block

```text
    // OrderService.java

    private Object object = new Object();

    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder() throws Exception{
        Product product = null;
        synchronized(OrderService.class) // synchronized(this)
        {
            TransactionStatus transaction1 = platformTransactionManager.getTransaction(transactionDefinition);
            product = productMapper.selectByPrimaryKey(purchaseProductId);
            if (product==null)
            {
                platformTransactionManager.rollback(transaction1);
                throw new Exception("item："+purchaseProductId+"does not exist");
            }

            // current inventory
            Integer currentCount = product.getCount();
            System.out.println(Thread.currentThread().getName()+"number of inventory："+currentCount);

            // check against inventory
            if (purchaseProductNum > currentCount)
            {
                platformTransactionManager.rollback(transaction1);
                throw new Exception("item"+purchaseProductId+"only has"+currentCount+" inventory，not enough for purchase");
            }

            productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());
            platformTransactionManager.commit(transaction1);

            TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
            Order order = new Order();
            order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
            order.setOrderStatus(1); // Wait to be processed
            order.setReceiverName("xxx");
            order.setReceiverMobile("13311112222");
            order.setCreateTime(new Date());
            order.setCreateUser("xxx");
            order.setUpdateTime(new Date());
            order.setUpdateUser("xxx");
            orderMapper.insertSelective(order);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setPurchasePrice(product.getPrice());
            orderItem.setPurchaseNum(purchaseProductNum);
            orderItem.setCreateUser("xxx");
            orderItem.setCreateTime(new Date());
            orderItem.setUpdateTime(new Date());
            orderItem.setUpdateUser("xxx");
            orderItemMapper.insertSelective(orderItem);
            platformTransactionManager.commit(transaction);
            return order.getId();
        }        
    }
```

# Reentrant lock

```text
    private Lock lock = new ReentrantLock();

    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder() throws Exception{
        Product product = null;

        lock.lock();
        try 
        {
            TransactionStatus transaction1 = platformTransactionManager.getTransaction(transactionDefinition);
            product = productMapper.selectByPrimaryKey(purchaseProductId);
            if (product==null)
            {
                platformTransactionManager.rollback(transaction1);
                throw new Exception("item："+purchaseProductId+"does not exist");
            }

            // current inventory
            Integer currentCount = product.getCount();
            System.out.println(Thread.currentThread().getName()+"number of inventory："+currentCount);

            // check against inventory
            if (purchaseProductNum > currentCount)
            {
                platformTransactionManager.rollback(transaction1);
                throw new Exception("item"+purchaseProductId+"only has"+currentCount+" inventory，not enough for purchase");
            }

            productMapper.updateProductCount(purchaseProductNum,"xxx",new Date(),product.getId());
            platformTransactionManager.commit(transaction1);
        }
        finally 
        {
            lock.unlock();
        }

        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        Order order = new Order();
        order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
        order.setOrderStatus(1);//待处理
        order.setReceiverName("xxx");
        order.setReceiverMobile("13311112222");
        order.setCreateTime(new Date());
        order.setCreateUser("xxx");
        order.setUpdateTime(new Date());
        order.setUpdateUser("xxx");
        orderMapper.insertSelective(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setPurchasePrice(product.getPrice());
        orderItem.setPurchaseNum(purchaseProductNum);
        orderItem.setCreateUser("xxx");
        orderItem.setCreateTime(new Date());
        orderItem.setUpdateTime(new Date());
        orderItem.setUpdateUser("xxx");
        orderItemMapper.insertSelective(orderItem);
        platformTransactionManager.commit(transaction);
        return order.getId();
    }
```