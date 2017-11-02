#综述
UIKit主要提供了一些**自定义组件辅助类**和**通用组件**。

如果你需要制作转场动画，可以使用**TransitionController**负责动画数值计算。
如果你需要识别翻页手势，可以使用**TouchController**负责触控采集。
如果你需要实现滚动效果，**ViewGroupMonitor**可以实时提供控件位置信息。

这里提供了**下拉刷新**组件，基于嵌套滑动实现，列表容器可更替。
这里提供了**图片查看器**组件，高仿iOS相册浏览效果。
***
#自定义组件辅助类
####RectUtil
功能：获取控件位置
接入：调用静态方法
`RectUtil.measure(view, rect, traverse);`

####ViewMonitor&ViewGroupMonitor
简介：控件位置变化监听
接入：静态方法注册，接口实现监听
`ViewMonitor.globalMonitor(view, listener);`
`ViewMonitor.localMonitor(view, listener);`
`ViewGroupMonitor.globalMonitor(parent, listener);`
`ViewGroupMonitor.localMonitor(parent, listener);`

####AnimatorManager
简介：简易防突变动画
接入：提供View对象控制动画
`manager.getValue(view);`
`manager.setValue(view, value);`
`manager.getTarget(view);`
`manager.setTarget(view, target);`

####TransitionController
功能：防突变动画
接入：重载computeScroll方法
`controller.getValue();`
`controller.setValue(value);`
`controller.getTarget();`
`controller.setTarget(target);`

####TouchController
简介：多指触控采集
接入：重载dispatchTouchEvent、onTouchEvent等方法

public interface TouchListener {
boolean down(float downX, float downY);
boolean move(float moveX, float moveY);
void up(float velocityX, float velocityY);
void cancel();
}
***
#通用组件
####Refresh
简介：下拉刷新
接入：参考SwipeRefreshLayout设计

public interface RefreshTopListener {
void topRefresh();
}

public interface RefreshBottomListener {
void bottomRefresh();
}

####Salon
简介：图片查看器
接入：中间层适配图片库，提供图片链接展开组件
`salon.display(index, urls, views);`
***
#其它
####Wrapper
简介：RecyclerView.Adapter包装库
接入：插入RecyclerView、Adapter之间，解耦实现首尾项、左滑删除等功能

####NotifyManager
简介：DiffUtil更新效果兼容库
接入：使用库方法代替Adapter的更新方法，适配滚动位置等问题
***
