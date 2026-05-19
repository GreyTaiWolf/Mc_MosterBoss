# MobMind 使用方法

## 安装

1. 使用 Minecraft 1.21.1。
2. 安装对应版本的 NeoForge。
3. 将 `build/libs/mobmind-1.0.0.jar` 放入游戏或服务器的 `mods` 文件夹。
4. 启动游戏或服务端。

## 开发运行

如果系统 PATH 中没有 Java，可以先临时设置：

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

构建：

```powershell
.\gradlew.bat build
```

启动客户端：

```powershell
.\gradlew.bat runClient
```

## awareness 如何获得

僵尸会在服务端每 20 tick 计算一次 awareness 增长。

增长来源：

- 僵尸存活越久，awareness 会缓慢增加。
- 夜晚增长更快。
- 附近 16 格内同类普通僵尸越多，增长越快。

awareness 保存在每只怪物自己的 NeoForge Data Attachment 中。它不是原版实体同步数据，也不会使用 `SynchedEntityData`。

## 首领诞生条件

当前 1 级首领条件：

- 只检查原版普通僵尸。
- 半径 32 格内至少有 6 只符合条件的僵尸。
- 每只候选僵尸 awareness 至少为 100。
- 候选僵尸必须存活、未分组、不是其他首领。
- 每 10 秒在服务端扫描一次。

满足条件后，awareness 最高的僵尸会成为首领，另外 5 只僵尸成为成员。

## 1 级小队组成

- 1 名首领。
- 2 名护卫。
- 1 名矿工。
- 1 名巡逻。
- 1 名建筑工。

1 级首领最多控制 6 只僵尸，包括首领自己。多余僵尸会保持未分组，等待后续选举或未来升级系统。

## 调试命令

设置 awareness：

```mcfunction
/mobmind debug awareness set <zombie> 100
```

查看 awareness：

```mcfunction
/mobmind debug awareness get <zombie>
```

重置 awareness：

```mcfunction
/mobmind debug awareness reset <zombie>
```

查看小队、角色、命令和共享背包状态：

```mcfunction
/mobmind debug group get <monster>
```

快速设置首领标记，用于视觉测试：

```mcfunction
/mobmind debug leader set <monster> true
```

## 实战测试流程

1. 生成 6 只僵尸。
2. 对每只设置 awareness 到 100。
3. 等待 10 秒左右。
4. 观察首领是否变大、发光、戴金头盔。
5. 攻击首领，观察护卫和其他成员是否保护首领。
6. 把时间设置为白天，让小队暴露在天空下，观察是否寻找阴影。
7. 观察矿工是否切换镐/铲、挥手、显示方块破坏裂纹。
8. 用 `/mobmind debug group get <monster>` 查看共享背包数量变化。

## 配置

服务端配置文件通常位于：

```text
run/config/mobmind-server.toml
```

当前配置：

```toml
enableMobBlockBreaking = true
enableMobBlockPlacing = true
maxBlocksChangedPerGroupPerMinute = 24
```

建议在重要存档中把方块破坏和放置关闭，先在测试世界观察行为。
