# 综述

<br>

UIKit主要提供了一些**自定义组件辅助类**和**通用组件**。

<br>

如果你需要制作转场动画，可以使用**TransitionController**负责动画数值计算。

如果你需要识别翻页手势，可以使用**TouchController**负责触控采集。

如果你需要实现滚动效果，**ViewGroupMonitor**可以实时提供控件位置信息。

<br>

这里提供了**下拉刷新**组件，基于嵌套滑动实现，列表容器可更替。

这里提供了**图片查看器**组件，高仿iOS相册浏览效果。

***

# 自定义组件辅助类

<br>

#### RectUtil

简介：获取控件位置

接入：调用静态方法

`RectUtil.measure(view, rect, traverse);`

#### ViewMonitor&ViewGroupMonitor

简介：控件位置变化监听

接入：静态方法注册，接口实现监听

`ViewMonitor.globalMonitor(view, listener);`

`ViewMonitor.localMonitor(view, listener);`

`ViewGroupMonitor.globalMonitor(parent, listener);`

`ViewGroupMonitor.localMonitor(parent, listener);`

#### AnimatorManager

简介：简易防突变动画

接入：提供View对象控制动画

`manager.getValue(view);`

`manager.setValue(view, value);`

`manager.getTarget(view);`

`manager.setTarget(view, target);`

#### TransitionController

简介：防突变动画

接入：重载computeScroll方法

`controller.getValue();`

`controller.setValue(value);`

`controller.getTarget();`

`controller.setTarget(target);`

#### TouchController

简介：多指触控采集

接入：重载dispatchTouchEvent、onTouchEvent等方法

    public interface TouchListener {
        boolean down(float downX, float downY);
        boolean move(float moveX, float moveY);
        void up(float velocityX, float velocityY);
        void cancel();
    }

***

# 通用组件Refresh

<br>

#### 初始化配置方法

`view.permit(top, bottom);`

设置**边缘越界滚动许可**，不影响刷新逻辑，但关闭后用户**无法通过手势触发刷新**。默认头部底部均为开启状态。

`view.auto(advance);`

设置加载更多模式，当advance>=0时为**自动加载模式**，代表**倒数第几项**出现在页面内时**触发加载**。默认advance=-1由**手势触发**加载更多。

#### 设置加载监听器

    public interface RefreshTopListener {
        void topRefresh();
    }

    public interface RefreshBottomListener {
        void bottomRefresh();
    }

控件提供**下拉刷新**及**加载更多**功能，接入方自主选择**下拉刷新**或**下拉刷新+加载更多**解决方案（或不设置仅为实现边缘效果）。接入加载更多可集成RefreshManager负责处理**分页列表缓存**逻辑，请注意接入加载更多时需保证列表**数据请求全部由接口发起**。

#### 加载控制类方法

`view.refresh(isTop);`

触发下拉刷新或加载更多，带有**防不必要机制**，返回值表示是否刷新成功。一般用于页面首次获取数据。

`view.callback(isTop, status);`

通知下拉刷新或加载更多回调，请注意**会话管理**防止脏回调。回调类型有NORMAL、END、ERROR，仅接入下拉刷新的场合任选其一即可。

`view.violate(anim);`

**强制触发**下拉刷新，可选触发加载动画。用于代替refresh方法解决手势以外的刷新需求，例如按钮点击刷新等。

#### 自定义加载效果

提供**RefreshHintView**用于自定义加载效果，**控件高度**影响手势刷新的**触发范围**。

    protected abstract int type(); //控件类型

    protected abstract long stay(); //暂留时长

    protected abstract View build(); //初始化控件

    protected abstract void status(int from, int to); //状态变化

    protected abstract void layout(int height); //布局变化

    protected abstract void scroll(float offset); //越界滚动

控件类型分为TOP、BOTTOM、AUTO，类型会影响status方法的触发。

暂留时长影响组件刷新后的**悬停时间**，设为负数可关闭悬停。

借助status、layout、scroll方法实现加载效果，其中layout方法入参为列表**内容可见高度**，scroll方法入参为**越界滚动偏移量**。

#### 列表容器更替性

组件借助**RefreshCompat**类与子容器通讯，该类已内部兼容了RecyclerView及AdapterView，其余容器需**自行实现各接口**。仅需下拉刷新时一般实现ViewEdge.Event接口即可。

组件基于嵌套滑动实现，对嵌套滑动支持性不好的容器，可使用**NestedChildController**来**模拟嵌套滑动**。

***

# 通用组件Salon

敬请期待

***

# 其它

<br>

#### Wrapper

简介：RecyclerView.Adapter包装库

接入：插入RecyclerView、Adapter之间，解耦实现首尾项、左滑删除等功能

#### NotifyManager

简介：DiffUtil更新效果兼容库

接入：使用库方法代替Adapter的更新方法，适配滚动位置等问题

***
