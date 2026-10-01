See [the plugin](https://github.com/NiFeather/FeatherMorph)

![cover](./assets/cover.png)

# FeatherMorphClient · Minecraft 26.2

[FeatherMorph](https://github.com/NiFeather/FeatherMorph) 的客户端模组，本仓库是它在 **Minecraft 26.2 / Fabric** 上的移植版本。

- 上游仓库：<https://github.com/NiFeather/FeatherMorphClient>（移植基线为默认分支 `26.1/main`）
- 本仓库内容：26.2 移植后的完整源码、构建脚本与移植说明
- 与上游的差别只在「适配 26.2 的原版 API 变更」，玩法与配置项沿用上游

### 功能

- [x] 在客户端显示自身伪装
- [x] 伪装选择界面
- [x] 技能快捷键
- [x] 一键切换自身可见
- [x] 与 EntityCulling / Entity Model Features / Entity Texture Features 的兼容处理

### 依赖关系

运行本模组至少需要：

- **Minecraft 26.2** + **Fabric Loader 0.19.5** 或更高（游戏本体需要 **Java 25**）
- [Cloth Config](https://modrinth.com/mod/cloth-config)（`>= 26.2.155`，配置界面依赖）
- [Fabric API](https://modrinth.com/mod/fabric-api)（`0.161.0+26.2` 或更高）

可选（安装后自动启用对应兼容逻辑）：

- [Mod Menu](https://modrinth.com/mod/modmenu) `>= 20.0.3`
- [Entity Model Features](https://modrinth.com/mod/entity-model-features) `3.3.8+`
- [Entity Texture Features](https://modrinth.com/mod/entitytexturefeatures) `7.2.4+`
- [EntityCulling](https://modrinth.com/mod/entityculling) `1.11.2+`

NeoForge 模块自上游 26.1 起已移出构建（`settings.gradle.kts` 中被注释），本仓库同样只构建 `common` + `fabric`。

### 下载与安装

到本仓库的 [Releases](https://github.com/sd-dt/FeatherMorphClient-26.2/releases) 下载 `feathermorph_client-fabric-26.2-*.jar`，放进整合包的 `mods` 目录即可。这是客户端模组，服务端无需安装；但需要服务端装有 [FeatherMorph](https://github.com/NiFeather/FeatherMorph) 插件。

### 构建

```bash
git clone https://github.com/sd-dt/FeatherMorphClient-26.2
cd FeatherMorphClient-26.2

./gradlew build        # Windows: gradlew.bat build
```

产物位于 `fabric/build/libs/feathermorph_client-fabric-26.2-<版本>.jar`。

构建环境（本仓库已锁定）：

| 项 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loom | 1.17.21 |
| Gradle | 9.7.1 |
| Java 编译工具链 | 25 |
| 运行 Gradle 的 JVM | 21+（Loom 1.17 要求） |

> `gradle.properties` 里的 `deploy_mods_dir` 是为「构建后自动把 jar 覆盖安装到本地整合包」加的小功能，**留空即关闭**。新克隆的环境请清空它，或改成你自己的 `mods` 目录。

### 26.2 移植说明

26.1 → 26.2 的原版 API 变更，以及本移植逐项做了什么（包括几个只在运行时才暴露出来的 mixin 陷阱），整理在：

- [`docs/移植说明-26.2.md`](docs/移植说明-26.2.md)

简述：注册常量迁到 `EntityTypes`、`Gui`/`Hud` 重组、渲染改用 `SubmitNodeCollector`（`MultiBufferSource` 被移除）、`Entity#getId()` 变成会抛异常的检查、碰撞箱计算拆成两个重载并被缓存化，等等。

### Credits

- [NiFeather/FeatherMorph](https://github.com/NiFeather/FeatherMorph)、[NiFeather/FeatherMorphClient](https://github.com/NiFeather/FeatherMorphClient)：模组与插件原作者
- [Identity Mod](https://github.com/Draylar/identity)：客户端伪装的实现思路来源
- [VeinMiner](https://github.com/2008Choco/VeinMiner)：客户端 ↔ 服务端通信的参考
- [osu-framework](https://github.com/ppy/osu-framework)：Drawable / Bindable / `@Resolved` 依赖系统的参考

### 许可

[MIT](./LICENSE)，沿用上游许可；再分发时请保留原作者署名与许可证全文。
