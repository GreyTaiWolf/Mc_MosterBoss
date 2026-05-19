# 更新日志

## 1.0.0 - 当前原型

### Added

- 创建 NeoForge 1.21.1 Mod 项目骨架，modid 为 `mobmind`。
- 添加 `MobMindData` Data Attachment，用于保存 awareness、groupId、leaderUuid、leader、currentOrder、nestPos、leaderLevel、role、roleAssignedAt。
- 添加普通僵尸 awareness 增长逻辑。
- 添加 debug 命令，用于设置/查看 awareness、查看小队状态、快速设置首领标记。
- 添加 1 级觉醒僵尸首领选举。
- 添加首领视觉表现：金头盔、发光、自定义名称、体型变大、生命值和攻击力增强。
- 添加 1 级小队分工：护卫、矿工、巡逻、建筑工。
- 添加群体攻击、保护首领和低血量撤退。
- 添加白天日照避险。
- 添加小队共享背包，容量 108 格。
- 添加矿工真实挖掘：工具切换、挥手动画、方块裂纹、掉落物进入共享背包。
- 添加建筑工补洞逻辑：从共享背包消耗普通方块并填补庇护区外壳。
- 添加 `HiveSavedData` 保存小队共享背包、庇护区锚点、挖掘目标和建造状态。
- 添加方块修改 config 和安全规则。
- 添加项目文档：README、使用方法、规则、设计规划、实现状态、测试指南、路线图。

### Notes

- 当前版本是可测试原型，不是稳定发布版。
- 当前只支持普通原版僵尸。
- 当前真实方块修改默认开启，但建议只在测试世界使用。
