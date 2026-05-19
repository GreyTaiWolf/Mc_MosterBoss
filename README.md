# MobMind / 怪物觉醒

MobMind 是一个 Minecraft 1.21.1 NeoForge Mod 原型。它让原版僵尸随着生存时间逐渐获得 awareness，当足够多的觉醒僵尸聚集后，会诞生一个“觉醒僵尸首领”，并组成带分工的小队。

当前版本不添加自定义模型、贴图或新实体，全部基于原版僵尸、原版装备、发光效果、自定义名称和原版方块行为实现。

## 当前功能

- NeoForge 1.21.1，modid 为 `mobmind`。
- 僵尸 awareness 数据使用 NeoForge Data Attachments 保存，不使用 `SynchedEntityData`。
- awareness 会随僵尸存活时间增长，夜晚和附近同类僵尸会加快增长。
- 1 级首领小队：1 名首领 + 5 名成员。
- 小队角色：2 护卫、1 矿工、1 巡逻、1 建筑工。
- 首领表现：金头盔、发光高亮、自定义名称“觉醒僵尸首领”、体型变大、少量属性增强。
- 玩家攻击首领或被首领/巡逻发现后，小队会共同攻击目标。
- 首领低血量时，小队进入撤退。
- 白天暴露在天空下时，小队会寻找阴影或建造临时庇护区。
- 矿工可以真实破坏安全方块，自动切换镐/铲，并把掉落物放进小队共享背包。
- 建筑工可以用共享背包里的普通方块补庇护区缺口。
- 小队共享背包容量为 108 格，等价于 4 个箱子。

## 构建与运行

需要 Java。当前开发环境可临时使用 IntelliJ JBR：

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

构建：

```powershell
.\gradlew.bat build
```

启动客户端测试：

```powershell
.\gradlew.bat runClient
```

构建成功后，Mod jar 会出现在：

```text
build/libs/mobmind-1.0.0.jar
```

## 快速测试

1. 创建开启作弊的测试世界。
2. 生成至少 6 只普通僵尸，并让它们靠近。
3. 对每只僵尸执行：

```mcfunction
/mobmind debug awareness set <zombie> 100
```

4. 等待最多 10 秒。
5. 预期出现 1 名更大、发光、戴金头盔、名为“觉醒僵尸首领”的首领。

查看僵尸状态：

```mcfunction
/mobmind debug group get <monster>
```

更多使用方法见 [docs/USAGE.md](docs/USAGE.md)。

## 重要规则

- 当前只支持原版普通僵尸，不处理尸壳、溺尸、僵尸村民或其他怪物。
- 所有 AI、首领选举、群体命令和方块修改逻辑只在服务端运行。
- 真实破坏和放置方块默认开启，但会经过 config 和安全规则检查。
- 不建议在重要存档直接测试方块修改功能。

完整规则见 [docs/RULES.md](docs/RULES.md)。

## 文档

- [使用方法](docs/USAGE.md)
- [玩法与开发规则](docs/RULES.md)
- [设计规划](docs/GAME_DESIGN.md)
- [实现状态](docs/IMPLEMENTATION_STATUS.md)
- [路线图](docs/ROADMAP.md)
- [测试指南](docs/TESTING_GUIDE.md)
- [更新日志](CHANGELOG.md)
