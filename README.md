# 前言

本文是 Fragment 多返回栈系列的第二篇，主要介绍最新的 Fragment 多返回栈 API 以及新 API 是否仍会出现 `Navigation Fragment` 重建的问题。



让我们开始吧~



随着 [Ian Lake](https://medium.com/@ianhlake) 将这个 2018 年 5 月提的 [issue](https://issuetracker.google.com/issues/80029773) 标记为 `fixed`，Android 终于支持了 Fragment 的多返回栈。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/38d03e2dba094fb7aff50f8b2aae9d00~tplv-k3u1fbpfcp-zoom-1.image)



如果你是第一次接触 Fragment 多返回栈，可以移步以下内容以获取必要的前置知识：

- [【背上Jetpack之OnBackPressedDispatcher】Fragment 返回栈预备篇](https://juejin.cn/post/6844904134529777678)
- [【背上Jetpack之Fragment】从源码的角度看Fragment 返回栈 附多返回栈demo](https://juejin.cn/post/6844904090921779214)
- [【背上Jetpack】绝不丢失的状态 androidx SaveState ViewModel-SaveState 分析](https://juejin.cn/post/6844904097351467015)
- [【背上Jetpack之Fragment】从源码角度看 Fragment 生命周期 AndroidX Fragment1.2.2源码分析](https://juejin.cn/post/6844904086437904398)
- [【译】Fragment 的重大重构 —— 介绍 Fragment 新的状态管理器](https://juejin.cn/post/6863334752162676749)
- [【Fragment多返回栈】开篇，Navigation 所谓的重建问题是什么？](https://juejin.cn/post/6923076959375212552)

# Fragment 支持多返回栈后解决了什么问题？

我们来简单回顾一下 `Navigation` 管理 **「平级界面」** 会遇到什么问题。详细内容可以 [移步这里](https://juejin.cn/post/6923076959375212552)。

我们使用 `Android Studio` 的 `bottom navigation` 模板快速创建一个 project

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/03066c1279c948fdbfa18999441d700c~tplv-k3u1fbpfcp-zoom-1.image)

该 project 由一个 Activity（MainActivity） 以及三个 Fragment（`Home`，`Dashboard`，
`Notifications`）组成，其中每个 tab 对应的父 Fragment 均可跳转到对应的子 Fragment 中： 

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/af28352423ea4e81bbd62e184f6fc698~tplv-k3u1fbpfcp-zoom-1.image)



![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0513936dceac4c2ba514336c89504c0e~tplv-k3u1fbpfcp-zoom-1.image)

每次点击底部 tab，对应的 Fragment 都会重新创建新的实例，这会导致当前 tab 之前存在的状态丢失，效果如下图：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a63b5f0b89c941c1aee8c75c54a089d6~tplv-k3u1fbpfcp-zoom-1.image)



> 上图中，在 Home tab 中点击进入 `HomeChildFragment`，此时点击 `dashboard` tab 并返回，`home` tab 对应的 Fragment 恢复成原来的状态！**这种行为完全不符合用户的预期**。

我们将 navigation 库的版本调整到 `2.4.0`（目前在 alpha 阶段），该问题得到解决。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1ac7f8a69a7d48f0bb6aff5698bfe83f~tplv-k3u1fbpfcp-zoom-1.image)

效果如下：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c5fef01a5e4f4f6d89b8a81892de5c16~tplv-k3u1fbpfcp-zoom-1.image)

# Fragment 支持多返回栈后就不重建了吗？

最近很多小伙伴反馈说使用版本的 Navigation 库仍然存在「重建」的问题。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f67ffc4890f047508cc606f48c977f36~tplv-k3u1fbpfcp-zoom-1.image)



我们使用新版本 Navigation 并点击每个 tab 查看当前 fragment 的实例：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ec6b4be5a3fb48288fa5d31fbb8b96fc~tplv-k3u1fbpfcp-zoom-1.image)

> 上图可以看到，点击 `dashboard` 和 `notifications` tab 时，对应的 fragment 实例发生了变化（重复点击 `home` 并没重建 fragment，这个我们之后源码分析篇讨论）

<mark>**新版本的 Navigation 仍会导致 fragment 的重建！**</mark>

> ☝ 划重点

# 新版本的 Navigation 是如何恢复状态的？

通过前文我们已经知道新版本的 Navigation 仍会有 Fragment 的重建问题，那么如何使重建的 Fragment 能够恢复之前的状态？

答案呼之欲出：Android 的 **SavedState 机制**。更多关于 SavedState 的内容，[可移步](https://juejin.cn/post/6844904097351467015)。

通过加入对 `savedInstanceState` 是否为空的判断日志我们了解到，每次重建 Fragment 时，`savedInstanceState` 不为空：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/688727102c0b48bd8c7dc701ddef7551~tplv-k3u1fbpfcp-zoom-1.image)



假设我们进行如下操作：

1. 在初始化状态点击 `dashboard` tab 进入 `DashboardFragment`，然后进入 `dashboard` 的详情页 `DashboardChildFragment`
2. 之后点击 `notifications` tab 进入到 `NotificationsFragment`
3. 最后点击 `dashboard` 返回

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/31abd32cd5734d7c8a4499432950aa3b~tplv-k3u1fbpfcp-zoom-1.image)



步骤 1 执行完毕时 Saved State 与返回栈的状态：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f7078daeeb1940d7ba2994935ae1bcf0~tplv-k3u1fbpfcp-zoom-1.image)

步骤 2 执行完毕时 Saved State 与返回栈的状态：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/68b790a1ccc74474b53155f389d35d7f~tplv-k3u1fbpfcp-zoom-1.image)

> 步骤 2 执行时，Fragment Manager 会调用新 API `fragmentManager.saveBackStack("dashboard")` 将 `dashboard` 这个子返回栈（姑且这样称呼它）存入 Saved State

步骤 3 执行完毕时 Saved State 与返回栈的状态：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/461b6dd120734dfdbb77925985764e5a~tplv-k3u1fbpfcp-zoom-1.image)

> 步骤 3 执行时，Fragment Manager 会调用新 API `fragmentManager.saveBackStack("notifications")`，`fragmentManager.restoreBackStack("dashboard")` 保存 notifications 的子返回栈，恢复 dashboard 的子返回栈。

---

**这样表述你明白了吗？** 😉



具体的实现逻辑我们将在源码篇介绍，敬请期待。

---

# 一点广告（内推时间）

我们是 **快手主站技术部团队**，负责快手主站和部分独立业务的工程研发。团队整合客户端、服务端、Web端，实现与业务更灵活高效的合作，通过稳定的基础平台、高效的业务开发、前沿的技术探索，支撑快手核心业务的快速发展。

**基本福利**：

- 全额六险一金，周末双休，加班双倍工资
- 16 薪起 + 2000 房补 + 包三餐
- 工作环境舒适，园区内部星巴克、罗森、赛百味
- 16 寸顶配 MacBook Pro，每日下午茶，无限量免费冰淇淋咖啡奶茶

**基本要求**：

- 本科（大牛可无视）
- [JD 列表](https://zhaopin.kuaishou.cn/recruit/e/#/official/social/?workLocationCode=domestic)

从 JD 中挑选岗位，发简历和意向的岗位链接到我微信：**Flywith24**

**承诺**：

- 实时跟踪并反馈面试进度
- 帮忙改简历
- Android 开发者可以免费进入我的知识星球

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/55b5f95e3c2f4258bccf709bd8899283~tplv-k3u1fbpfcp-zoom-1.image)

---

# 关于我

人总是喜欢做能够获得正反馈（成就感）的事情，如果感觉本文内容对你有帮助的话，麻烦点亮一下👍，这对我很重要哦～


我是 [Flywith24](https://flywith24.gitee.io/)，**人只有通过和别人的讨论，才能知道我们自己的经验是否是真实的**，加我微信交流，让我们共同进步。

- [掘金](https://juejin.im/user/57c7f6870a2b58006b1cfd6c)
- [小专栏](https://xiaozhuanlan.com/u/3967271263)
- [Github](https://github.com/Flywith24) 
- 微信：**Flywith24** 


