# Google calendar

<!-- MarkdownTOC -->

- [Description](#description)
- [Features](#features)
- [Data modeling](#data-modeling)
- [References](#references)

<!-- /MarkdownTOC -->


## Description

* Comment1: 设计google/outlook calendar create eventinvite usernotify users at specific time or periodically. System design, 都是要分module，分service，构建system，考虑机器怎么scale，数据如何存储； 譬如，可以有一个job，不停地扫DB里的event，发现需要的notification，push到message queue里面；再后面有个notification service 从message queue里subscribe，负责发送email，text message，mobile notification这样 

* Comment2: Design。 和之前面经一样Calendar system，要支持1M用户。我其实开始答得不好，但是后来follow up怎么支持1M用户时我说的方案应该就是面试官想要的。然后这轮后面又在闲聊了。设计日历（不用考虑重复事件，用户少）

* Comment3: Design Google Calendar

5 个回复 


2015-08-16 张老师
这道题有很多维度可以考，我们可以拆解一下：

如何设计calendar的视图呢？（参考如何设计excel）
如何订阅多个feed呢？（参考如何设计subscribe系统）
如何解决多平台的修改冲突呢？（参考如何合并购物车）
等等


2015-08-16 Kevin Chen
感觉好难, 完全没头绪. 
是OO design 还是system design?


2015-08-16 RUIHAN ZOU
如果我对这个产品不熟悉，可不可以反问面试官有没他自己期望的功能，然后通过提示来设计？


2015-08-16 Allen Lin
create event
2, invite user
notifer users at specific time or periodically
Follow up:
if you have a lot of users with the same calendar, how to implment create event, invite user and notify users

老师这样要怎么处理呢


2015-09-04 张老师
可以把所有用户对同一个calendar的操作序列化，之后给大家push同步。

当然，为了用户友好，可以现在本地先update用户的操作，同时和服务器同步，如果数据一致，则ok；如果期间有人修改了calendar造成不同步，则update为最新的

* Comment4: 要求设计一个calendar system。要求1000用户，并可能拓展到100M+（我xxx）。这轮应该是挂了，作死的选了个cassandra

* Comment5: 
Zi Wang
根据看到的面经，这题会分两个侧重点来问，当然get和set是最基本的，
1， 重点要处理各个cell之间的dependency，比如cell(1,3)是用公式算出来的cell(1,3)=cell(0,0)+cell(0,1)+cell(0,2)，我会用两层哈希表表示整个表格(unordered_map> workbook)，然后每个Cell中保存一个unordered_set parents；(所有计算当前Cell需要依赖的cells，上例就是cell(0,0)，cell(0,1)和cell(0,2)) 和 unordered_set children; (所有依赖这个Cell通过公式计算出来的cells)，每次改变cell的值就要对children和parents做相应的改变；
2， 重点是要处理add或delete一整行或一整列，我会用2d数组，vector> workbook, add的话就直接append，delete行的话就直接erase对应的行，delete列的话就根据列下标，对每行进行erase，好写，但是效率有点低，如果大家有更好的想法，可以一起讨论下。


2015-08-26 Zi Wang
根据看到的面经，这题会分两个侧重点来问，当然get和set是最基本的，
1. 重点要处理各个cell之间的dependency，比如cell(1,3)是用公式算出来的cell(1,3)=cell(0,0)+cell(0,1)+cell(0,2)，我会用两层哈希表表示整个表格(unordered_map < int, unordered_map < int, Cell * > > workbook)，然后每个Cell中保存一个unordered_set < Cell * > parents；(所有计算当前Cell需要依赖的cells，上例就是cell(0,0)，cell(0,1)和cell(0,2)) 和 unordered_set < Cell * > children; (所有依赖这个Cell通过公式计算出来的cells)，每次改变cell的值就要对children和parents做相应的改变；
2. 重点是要处理add或delete一整行或一整列，我会用2d数组，vector < vector < Cell * > > workbook, add的话就直接append，delete行的话就直接erase对应的行，delete列的话就根据列下标，对每行进行erase，好写，但是效率有点低，如果大家有更好的想法，可以一起讨论下。


2015-08-26 张老师
解答的很好呀。对于删除一列，可以把每个cell做成一个四向的指针（指向上、下、左、右的cell），这样删除一列可以成为o(k)复杂度，而不是o(mk)


2015-08-27 Liu Mingmin
感觉差不多是这样（话说代码居然没有高亮）

class Excel{
  Map<Integer, Map<Integer, Cell>> sheet = new Map<>();

    public Cell get(int row, int col){
      if(contains(sheet, row, col)){
          return sheet.get(row).get(col);
        }else{
          return null;
        }
    }

    public void set(int row, int col, Value val){
      if(!contains(sheet, row, col)){
                insert(sheet, row, col);
            }else{               
                 Cell cur = sheet.get(row).get(col);

                 cell.setValue(val);

                 breakDependencyOnParents(sheet, row, col);
                 updateChildren(cell);//using dfs
            }
    }
}

class Cell{
  Value val;

    Cell up;
    Cell right;
    Cell down;
    Cell left;

    List<Cell> parents;
    List<Cell> children;
}

2015-08-28 DIMfang
package Excel;

import java.util.HashMap;
import java.util.Map;

public class Excel {
private HashMap> cellsMap = new HashMap>();
private int countRows = -1;
private int countCols = -1;
public String getValue(int row, int col) {
HashMap colsMap= cellsMap.get(row);
if (colsMap == null) {
return "";
}
Cell cell = colsMap.get(col);
if (cell == null) {
return "";
}
return cell.getValue();

}

public void SetValue(Cell cell) {
    int row = cell.getRow();
    int col = cell.getCol();

    HashMap<Integer, Cell> colsMap = cellsMap.get(row);
    if (colsMap == null) {
        colsMap = new HashMap<Integer, Cell>();
        cellsMap.put(row, colsMap);
    }
    colsMap.put(col, cell);
    this.countCols = Math.max(this.countCols, col + 1);
    this.countRows = Math.max(this.countRows, row + 1);
    breakDependencyOnParents(row, col);
    updateChildren(cell);
}

public void breakDependencyOnParents(int row, int col) {
    Cell cell = cellsMap.get(row).get(col);
    if (cell.parent != null) {
        for (Cell cellParent : cell.parent) {
            cellParent.children.remove(cell);
        }

        for (int i = cell.parent.size() - 1; i >= 0; i--) {
            cell.parent.remove(i);
        }
    }
}

public void updateChildren(Cell cell) {
    if (cell.children == null) {
        return;
    }
    for (Cell cellChild : cell.children) {
        int row = cell.getRow();
        int col = cell.getCol();
        for (Cell cellChildParent : cellChild.parent) {
            if (cellChildParent.getRow() == row && cellChildParent.getCol() == col) {
                cellChildParent.setValue(cell.getValue());
            }
        }
        updateChildren(cellChild);
    }

}

public int getCountCols() {
    return countCols;
}

public void setCountCols(int countCols) {
    this.countCols = countCols;
}

public int getCountRows() {
    return countRows;
}

public void setCountRows(int countRows) {
    this.countRows = countRows;
}

@Override
public String toString() {
    StringBuilder sb = new StringBuilder("Table:\n");

    for (int i = 0; i < this.countCols; i++) {
        sb.append("Column: ").append(i).append(":\t");
        for (int j = 0; j < this.countRows; j++) {
            sb.append(getValue(i, j));
            if (j == this.countRows - 1) {
                sb.append("\n");
            } else {
                sb.append(", ");
            }
        }
    }

    return sb.toString();
}
}
大家帮忙看看有啥错误没~


2015-08-28 Adam Smith
我觉得遍历子节点是需要按拓扑顺序，因为子3可能依赖于子1和子2，必须先保证update 1, 2,然后再update 3


2015-09-05 Allie Zhao
有followup到 cloud excel嗎， 多人修改？


2015-09-20 张老师
另外提示一下，这道题背后的思路可以用在『react』之类的框架中，也就是一个数据改变了，触发其他数据的改变，当然本质类似observer模式。

* 


## Features
* Create event. Recurring or once. 
* Reminders. users at specific time or periodically. Notify by emails, SMS to their phones. 
	- Get a daily agenda in your inbox
	- The ability to bulk email all attendees of an event will save you from emailing each attendee individually to remind them about tomorrow’s meeting. Before doing this, make sure everyone is added to your event, otherwise they will not receive your email. 
* View: Annual, monthly, weekly and daily calendar views. 
	- You can choose to view the calendar by day, week, month or a view that presents just the next four days. You can also choose an "agenda" view, which presents all scheduled events as a list rather than as a calendar view.
* Send invitation: 
	- One to one
	- Group calendar
	- Email all of your attendees: 
		+ You can click on the "add guest" option. This opens up a field in which you can type email addresses. Once you save the event, Google Calendar sends emails to invite list. As guests respond to the invite, Google Calendar displays the results with the event listing on your calendar.
* Share calendars
	- Calendars can either be shared with individuals or shared publicly. You can choose one of the permissions like "View free busy", "View Event Details", "Edit event details" and "Manage/Delegate". If your calendar is exposed to unauthorized people, you can revoke the access permission by resetting your private URL.
* Mobile Access
	- Can be accessed on your mobile phone too, using a mobile web browser. We support iPhone, Android, Windows Phone, Blackberry and Nokia smart phones. Even if you have any phone other than the ones listed above, you could still access our mobile applications.
* Automatic birthday / holiday mark

## Data modeling
* 假设有1 billion user
每个user平均每天new一个event
平均每天读10次

那么大概每秒10k的写和100k的读
如果每个user可以使用1M的存储空间，那么total就是1PB，属于大数据了
当然实际使用的情况感觉应该没有这个大，但是potentilly还是可能的, 我感觉实际情
况100T应该是够了 （90%的user不怎么使用calendar）

从这个分析来说， Cassandra handle起来应该没什么问题，是一个不错的选择, 一般
的SQL就不适合处理这么大量了。

先分析core functionality
一般就是CRUD，这个用rest来实现就很好，frontend就先不提了，都是JS的工作
那么这步重要的是设计C＊的schema

我刚才看了一下，主要的大概有这样的信息
user gmail account: String
subject: String
start_at: timestamp
end_at: timestamp
Repeat: 比较复杂，可以用map
Where: string
Description: string
Reminder: map
Guests: set

NOSQL的schema的设计一般是按照query来的， Calendar的查询最典型的就是按照时间
来查询了，所以
partition key: email
clustering key: start_at



## References
* [Massive interview tips](http://massivetechinterview.blogspot.com/2015/10/google-calendar-architecture.html)