# Rangelist 加，减，打印
## 描述
写一个class, 能表达出多个range list。写3个方法：add, remove, print。用javascript实现。

![](./oa.jpg)

// A pair of integers define a range, for example: [1, 5). This range includes integers: 1, 2, 3, and 4.
// A range list is an aggregate of these ranges: [1, 5), [10, 11), [100,201) =》1~4，10，100~200

## 实现
实现如下：

```javascript
class RangeList {

  constructor() 
  {
    this.ranges = [];
  }

  add(range) {
    if (range[0] >= range[1]) {
      console.error('Invalid range: start must be less than end.');
      return;
    }
    let newRanges = [];
    let inserted = false;
    for (const r of this.ranges) {
      if (range[1] < r[0]) {
        if (!inserted) {
          newRanges.push(range);
          inserted = true;
        }
        newRanges.push(r);
      } else if (range[0] > r[1]) {
        newRanges.push(r);
      } else {
        range = [Math.min(range[0], r[0]), Math.max(range[1], r[1])];
      }
    }
    if (!inserted) {
      newRanges.push(range);
    }
    this.ranges = newRanges;
  }

  remove(range) {
    if (range[0] >= range[1]) {
      console.error('Invalid range: start must be less than end.');
      return;
    }
    let newRanges = [];
    for (const r of this.ranges) {
      if (range[1] <= r[0] || range[0] >= r[1]) {
        newRanges.push(r);
      } else {
        if (range[0] > r[0]) {
          newRanges.push([r[0], range[0]]);
        }
        if (range[1] < r[1]) {
          newRanges.push([range[1], r[1]]);
        }
      }
    }
    this.ranges = newRanges;
  }

  print() {
    const rangesStr = this.ranges
      .map(range => `[${range[0]}, ${range[1]})`)
      .join(' ');
    console.log(rangesStr);
  }
}

// 示例使用
const rl = new RangeList();
rl.add([1, 5]);
rl.print(); // 输出: [1, 5)
rl.add([10, 20]);
rl.print(); // 输出: [1, 5) [10, 20)
rl.add([20, 20]);
rl.print(); // 输出: [1, 5) [10, 20)
rl.add([20, 21]);
rl.print(); // 输出: [1, 5) [10, 21)
rl.add([2, 4]);
rl.print(); // 输出: [1, 5) [10, 21)
rl.add([3, 8]);
rl.print(); // 输出: [1, 8) [10, 21)
rl.remove([10, 10]);
rl.print(); // 输出: [1, 8) [10, 21)
rl.remove([10, 11]);
rl.print(); // 输出: [1, 8) [11, 21)
rl.remove([15, 17]);
rl.print(); // 输出: [1, 8) [11, 15) [17, 21)
rl.remove([3, 19]);
rl.print(); // 输出: [1, 3) [19, 21)

```
## 链接
* https://www.1point3acres.com/bbs/thread-1001762-1-1.html
* https://www.1point3acres.com/bbs/thread-925807-1-1.html