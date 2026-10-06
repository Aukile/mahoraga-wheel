# 魔虚罗的法阵（Mahoraga Wheel）

> 「布瑠部 由良由良」—— 八握剑·异戒神将·魔虚罗。
> 头顶的法阵会在受击时解析现象、逐渐适应；适应完成时法阵转动，此后免疫该现象。

Forge 1.20.1 模组。核心玩法：**佩戴法阵 → 受击/处于现象中推进适应度 → 100% 转动一圈并获得免疫或增益**。

- Mod ID：`mahoraga_wheel`，当前版本 `1.7.2`
- 运行环境：Minecraft 1.20.1 / Forge `47.4.0` / Curios `5.14.1+1.20.1` / Java 17
- 源码版本标记：`1.7.2-recovered`（Vineflower 反编译恢复，官方 1.20.1 映射）

---

## 0. 两个版本（v1.8 起）

| | 加强版（原物品） | 削弱版（新物品） |
|---|---|---|
| 注册名 | `mahoraga_wheel:mahoraga_wheel`（不变，老存档无缝升级） | `mahoraga_wheel:mahoraga_wheel_weakened` |
| 品质 | EPIC（紫色） | RARE（蓝色，低一档） |
| 显示名/文案 | 魔虚罗的法阵 | 与加强版完全一致（仅品质颜色区分） |
| 机制 | 全部（含下述新适应＋熟练度无上限） | 与 v1.7.2 完全一致（熟练度有上限、无新适应） |

- 代码层面：`MahoragaWheelItem.isEnhanced()` 区分版本，`WeakenedMahoragaWheelItem` 继承并返回 `false`；`AdaptationHandler.findWheel` / `WheelSource` / 饰品栏 tag 对两者通用；新适应与熟练度无上限处处用 `isEnhanced` 设门。
- 两处佩戴位（头盔栏 / 法阵饰品栏）仍只能同时戴一个（不分版本）。
- 加强版独占能力：
  1. **熟练度无上限**（`adaptation.enhancedUncappedMastery`，默认开；削弱版仍受 `masteryMax` 限制）。
  2. **游泳适应**（`C|swim`）：水中游动累计距离升级，每级 +3% 游泳速度，无上限。
  3. **岩浆畅游**（`C|lava`）：需先完成岩浆伤害一度适应（`lavaRequiresAdapt`，默认开）；在岩浆中移动累计升级，每级 +5% 岩浆中移速并可在岩浆中上浮（潜行下潜），无上限。
  4. **滑行**（`C|glide`）：需先完成摔落伤害一度适应；滞空下坠累计约 300 秒（`glideSeconds`，可配）学会；空中按住 Shift 滑行（`glideRequireSneak=false` 可改为自动滑行），视角俯仰控制升降，适应度越高效率越高。
  5. **创造飞行**（`C|fly`）：需先滑行 100%；滑行（含鞘翅滑行）累计约 600 秒（`flySeconds`，可配）学会；佩戴加强版时获得创造式飞行（取下即失效，不影响创造/旁观玩家），界面滑杆可调飞行速度。
  6. **跳跃适应**（`C|jump`）：跳跃累计上升高度升级，每级 +8% 跳跃初速度（约 5 级 ≈ 跳跃提升 I），无上限。
  7. **跑跳适应**（`C|leap`）：疾跑腾空累计距离升级，每级 +2% 跑跳空中移速（与疾跑加速叠加），无上限。
- 新适应全部归入界面「便利」页签，进度条/滑杆/颜色/文案与既有便利适应一致；中英文语言文件已补齐。

---

## 1. 快速上手

1. 在创造模式物品栏「战斗」或「工具与实用物品」中找到**魔虚罗的法阵**（史诗紫色品质，防火）。
2. 佩戴（二选一）：
   - 戴到**头盔栏**；
   - 戴到 Curios 专属**「法阵」饰品栏**（`mahoraga_wheel`）。该栏默认 `size: 0`（开局看不到），可用 `/curios add mahoraga_wheel <玩家> [数量]` 添加，或把配置 `slot.enableCurioSlot` 设为 `true` 让每人开局自带 1 格（需重进世界 / 重启后生效）。
   - 两处同时只能戴一个，逻辑见 `MahoragaWheelItem.canEquip*`。
3. 打开法阵界面：
   - 手持法阵按**右键**；
   - 已佩戴时按 **G**（`key.mahoraga_wheel.open`，可在键位设置中修改）。
4. 挨打 / 待在黑暗里 / 带着负面效果 / 疾跑，屏幕左侧 HUD 会实时显示解析进度。

---

## 2. 适应系统总览

所有进度都存在法阵物品的 NBT 里（见第 8 节），共 6 种记录类型：

| 类型 | Key 规则 | 说明 |
|---|---|---|
| 生物攻击 `ATTACK` | `T|<生物ID>|<伤害类型>` | 按【生物种类 × 伤害类型】记录。同种生物共享（如所有僵尸的近战共用一条），不同种类、不同攻击方式分开记 |
| 环境伤害 `ENVIRONMENT` | `D|<伤害类型>` | 无攻击者的伤害：摔落、火焰、岩浆、溺水、仙人掌等 |
| 存在本身 `EXISTENCE` | `X|T|<生物ID>` | 二度适应，见下 |
| 负面效果 `DEBUFF` | `P|<效果ID>` | 每种负面效果单独一条 |
| 黑暗 `DARK` | `C\|dark` | 便利适应：暗处夜视 |
| 跑步 `RUN` | `C\|run` | 便利适应：疾跑加速（无上限等级） |

`AdaptationData.Kind` 中 `reduction()`（攻击 / 环境 / 负面）表示“有减伤＋免疫开关”；`convenience()`（负面 / 黑暗 / 跑步）表示归入界面「便利」页签。

### 2.1 一度适应：攻击与环境（`AdaptationHandler`）

- **推进时机**：`LivingHurtEvent`（LOW 优先级）。每次受击若伤害类型可适应（不在黑名单、且不带 `minecraft:bypasses_invulnerability`，如虚空 `/kill` 不可适应），就按本次伤害推进对应 Key 的适应度。
- **所需次数**（生物攻击）：
  - `expectedHits = baseHits × (最大生命 / referenceHealth) ^ healthExponent`，再 clamp 到 `[minHits, maxHits]`。
  - 默认：20 血生物约 22 次；100 血约 49 次；200 血约 70 次；500 血约 110 次。
  - 环境伤害固定 `environmentHits`（默认 60 次）。
- **高血量变速**：前 80% 进度快、后 20% 慢，总次数不变。后 20% 占总次数的比例从 0.2（普通生物）按生命对数插值到 `slowShareMax`（默认 0.5，500 血封顶）。
- **伤害修正**：`damageFactor = (本次伤害 / damageReference) ^ damageExponent`，默认参考 3 点伤害、指数 0.3，clamp 到 `[0.7, 2.0]`。伤害越高适应越快（铁傀儡约 1.6 倍、监守者约 2 倍），蹭破皮的 1 点伤害会慢约 30%。
- **随机浮动**：每次增长在平均值的 `1 ± gainRandomness`（默认 ±50%）内随机。
- **熟练度加速**：`masteryMultiplier = 1 + min(masteryMax, masteryPerAdapt × 熟练度)`，默认每点熟练度 +5%，上限 +100%（速度翻倍）。
- **减伤**：适应度超过 `reductionStart`（默认 80%）才开始减伤，80%→100% 线性 0%→100%，100% 且免疫开关开启时：
  - `LivingAttackEvent`（HIGH）直接取消伤害，播放无效化音效＋附魔粒子＋HUD「已适应」；
  - `LivingHurtEvent` 中再次兜底取消。
- **完成**：适应度到 100% 时 `Turns + 1`、熟练度 `Mastery + 1`（跑步升级不加，见下），播放转动音效＋末地烛粒子环，可选 `healOnTurn` 回满本次伤害（默认关，原作转轮再生的味道）。

### 2.2 二度适应：存在本身（`EXISTENCE`）

- **解锁条件**：对某生物的**任意一种攻击**一度适应完成后，才开始积累「该生物的存在本身」。
- **推进**：此后每次被该种生物攻击（含已被无效化的攻击）都推进，10 tick 内重复攻击只记一次；所需次数 = 一度所需次数 × `existenceHitsMultiplier`（默认 2.5，僵尸约 55 次）。
- **效果**：佩戴者对该种生物的伤害提高 `existenceDamageBonus × 进度%`（默认满级 +50%），在 `onDealDamage`（`LivingHurtEvent`）中结算。完成时法阵转动一圈（灵魂火粒子＋低音）。
- 可用 `existence.enableExistence` 整体开关。

### 2.3 便利适应（`ConvenienceHandler`，每秒 tick 一次）

| 项目 | 触发条件 | 效果 | 完成奖励 |
|---|---|---|---|
| 黑暗 `C\|dark` | 眼睛处亮度 ≤ `darkLightLevel`（默认 7），累计 `darkSeconds`（默认 480 秒，受熟练度加速） | 按适应度获得夜视强度（满级 = `darkMaxStrength`，默认与夜视药水相同；客户端 Mixin 注入 `LightTexture` 实现） | 转动一圈（荧光粒子）＋熟练度 +1 |
| 负面效果 `P\|…` | 带着可适应的有害、非瞬间效果，累计 `debuffSeconds`（默认 40 秒；等级越高越快；受熟练度加速）。`debuffOneAtATime=true` 时只推进进度最高的那一种 | 超过 80% 后按比例**缩短新上身效果的持续时间**（反射改 `MobEffectInstance` 时长），100%＋免疫开时**直接 DENY 该效果**（`MobEffectEvent.Applicable`）并播无效化特效 | 转动一圈（村民开心粒子）＋熟练度 +1 |
| 跑步 `C\|run` | 疾跑中（非乘客、非飞行、非旁观），累计方块数。第 n 级需 `runBlocksBase × (1 + runBlocksGrowth × (n-1))` 格（默认 250 格起，每级 +10%） | 每级疾跑速度 `+runSpeedPerLevel`（默认 +1%），通过 `MOVEMENT_SPEED` 的 `MULTIPLY_BASE` 瞬时修饰器实现，**只在疾跑时生效**，无上限 | 每次升级转动一圈（电火花粒子），**不加熟练度** |

三项各有独立开关与上限配置（`convenience.*`），完成/升级都消耗耐久（`durabilityPerConvenience`）。

---

## 3. 熟练度、转动与耐久

- **熟练度 `Mastery`**：完成一次一度适应 / 黑暗适应 / 负面效果适应时 +1；存在适应完成与跑步升级不加。作用是全局加速（见 2.1），显示在物品 Tooltip 与界面头部。
- **转动 `Turns`**：任何一次适应完成（含跑步每次升级）都 +1。转动次数驱动：
  - 头顶法阵模型的旋转动画（`WheelRenderHelper`，每转 `degreesPerTurn` 度、动画 `turnSeconds` 秒，easeOutBack 回弹）；
  - 界面内法阵图标的旋转＋紫水晶风铃音；
  - 服务端粒子环＋`wheel_turn` 音效（不同适应类型粒子/音调不同）。
- **耐久**：除 `exemptPlayers`（默认 `Ankrya`，不区分大小写）外，所有佩戴者（玩家或生物）都会掉耐久：
  - 每次完成适应 `durabilityPerAdapt`（默认 10，即满耐久 100 可完成 10 次）；
  - 每次受击推进 `durabilityPerHit`（默认 0）；
  - 每次便利完成/跑步升级 `durabilityPerConvenience`（默认 10）。
  - 耐久耗尽法阵碎裂（播损坏事件＋HUD 提示），可在铁砧用**金锭**修复（`isValidRepairItem`）。

---

## 4. 法阵界面（`WheelScreen`，右键 / G 打开）

- **头部**：旋转的法阵图标、标题、熟练度（及加速 %）、转动次数、耐久（`∞` 表示豁免或不可损）。
- **页签**：全部 / 攻击（含环境） / 存在 / 便利。
- **每行**：现象名（跑步附带 `Lv.`）、适应度条＋百分比、**滑杆**、（减伤类）**免疫开关**。
  - 滑杆 = 手动压低该条目的实际生效值（减伤 / 缩短 / 增伤 / 夜视 / 加速），最高只能拉到当前适应度对应的上限；**拉到最右 = 自动跟随上限**。拖动松开后发 `SettingPacket` 到服务端（`mode=1` 定值 / `mode=2` 自动）。
  - 免疫开关只有减伤类（攻击 / 环境 / 负面）且 100% 时可用；开启后该现象被完全无效化，滑杆锁定变灰（`mode=0` 发包）。
- **按钮**：「佩戴」（优先进法阵饰品栏，否则进头盔栏；已佩戴则置灰）与「清空适应」（点两次确认，只清 `Adapt` 记录，保留熟练度与转动数）。
- 列表支持滚轮/拖拽滚动条，有开场滑入、进度条扫光、完成条闪烁等动画；按 G 或 Esc 关闭。

## 5. HUD 与提示

- 服务端通过 `HudMessagePacket` 把「解析中 / 适应完成 / 已适应 / 碎裂 / 清空」等推送到佩戴者客户端，`AdaptationHud` 以 overlay 形式绘制在屏幕上（默认中左，可配锚点 `anchor`、偏移、缩放 `scale`、停留秒数、最大行数，可整体开关）。
- 物品 Tooltip：紫色 lore、灰色简介、金色「转动 × 次 · 熟练度 ×」、适应条目（默认前 4 条，按住 Shift 看全部最多 40 条，不同类型不同颜色，跑步显示等级、完成打 ✔），以及“右键打开界面”提示。

## 6. 按键、渲染与音效

- **按键**：`G` 打开界面（手持右键同样可开）。无其他按键。
- **头顶渲染**：`WheelHeadLayer` 给所有带头模型的生物（含各玩家皮肤）头顶加法阵；饰品栏佩戴时走 `WheelCurioRenderer`。两者都调 `WheelRenderHelper.render`：跟随头部旋转、悬浮正弦 bob、按 `Turns` 转动。物品模型为 `assets/mahoraga_wheel/models/item/mahoraga_wheel.json`。
- **音效**（`ModSounds` + `sounds.json`）：`wheel_turn`（转动）、`wheel_adapted`、`wheel_nullify`（无效化），现有 ogg 资源为 `wheel_turn.ogg`。

## 7. 配置说明（`WheelConfig`）

Common（服务端/存档生效）：

- `slot.enableCurioSlot`（默认 false）：是否开局自动给每人 1 格法阵饰品栏。
- `adaptation.*`：`baseHits=22`，`referenceHealth=20`，`healthExponent=0.5`，`minHits=8`，`maxHits=400`，`slowShareMax=0.5`，`slowShareHealth=500`，`environmentHits=60`，`gainRandomness=0.5`，`reductionStart=80`，`masteryPerAdapt=0.05`，`masteryMax=1.0`，`damageReference=3`，`damageExponent=0.3`，`damageFactorMin=0.7`，`damageFactorMax=2.0`，`healOnTurn=false`，`showMessages=true`，`maxEntries=128`，`blacklist=[虚空, /kill, 世界边界]`。
- `existence.*`：`enableExistence=true`，`existenceHitsMultiplier=2.5`，`existenceDamageBonus=0.5`。
- `convenience.*`：黑暗（`enableDarkVision=true`，`darkLightLevel=7`，`darkSeconds=480`，`darkMaxStrength=1.0`）、负面（`enableDebuff=true`，`debuffSeconds=40`，`debuffOneAtATime=true`，`debuffBlacklist=[]`）、跑步（`enableRunning=true`，`runBlocksBase=250`，`runBlocksGrowth=0.1`，`runSpeedPerLevel=0.01`）。
- `durability.*`：`maxDurability=100`，`durabilityPerAdapt=10`，`durabilityPerHit=0`，`durabilityPerConvenience=10`，`exemptPlayers=["Ankrya"]`。

Client（客户端实时生效）：`render.degreesPerTurn=360`（45 = 像动画里只“咔”过一根辐条），`render.turnSeconds=1.05`；`hud.*`（`enabled/anchor/offsetX/offsetY/scale/seconds/maxLines`）。

## 8. 数据与网络（给后续开发看）

- **NBT**（`AdaptationData`）：物品 tag 根下 `Turns:int`、`Mastery:int`、`Adapt:compound`。每条 `Adapt.<key>`：`A` 适应度 float、`T` 最后时间 long、`D` 伤害/效果类型 string、`N` 生物名 JSON（可选）、`E` 是否生物 bool、`S` 是否存在适应 bool、`L` 跑步等级 int、`U` 用户滑杆值 float（无 = 自动）、`NI` 非免疫标记（无 = 免疫开）、`M` 上限 float、`K` 滑杆缩放 float。
  - `maxEntries` 超限时修剪：`C|*` 永不删除，其余优先删“未完成且最久未触发”的。
  - 旧版 `E|*` / `X|E|*` 开头的残留 Key 会在受击时自动 `purgeLegacy`。
- **网络**（`ModNetwork`，`mahoraga_wheel:main`，版本 `2`）：`0 HudMessagePacket` S→C（key＋文本组件，HUD 去重按键刷新）；`1 SettingPacket` C→S（`source,key,mode,value,immune`）；`2 ActionPacket` C→S（`EQUIP=0` 佩戴 / `RESET=1` 清空）。
- **佩戴解析**：`WheelSource`（`MAIN_HAND/OFF_HAND/WORN`）统一 resolve 界面与发包操作的是哪一件法阵；`findWheel` 优先头盔栏、其次饰品栏。

## 9. 项目结构

```text
F:\mcr\wheel
├─ build.gradle / settings.gradle / gradle.properties   Forge 1.20.1-47.4.0，Curios 依赖
├─ build-local.ps1                                       本地构建脚本（自动找 Java 17 + Gradle 缓存）
├─ src/main/java/com/mahoraga/wheel/
│  ├─ MahoragaWheelMod.java      Mod 入口：注册物品/音效/配置/事件/网络/客户端
│  ├─ MahoragaWheelItem.java     法阵物品（加强版）：双佩戴位、右键开界面、Tooltip、耐久/修复
│  ├─ WeakenedMahoragaWheelItem.java 削弱版：同机制、RARE 品质，无新适应
│  ├─ AdaptationData.java        NBT 存取：进度/转动/熟练度/滑杆/免疫/修剪/排序
│  ├─ AdaptationHandler.java     一度＋二度适应：受击推进、减伤/免疫、增伤、转动特效、HUD
│  ├─ ConvenienceHandler.java    便利适应：黑暗/负面/跑步/游泳/岩浆/滑行/飞行/跳跃/跑跳 tick、修饰器、时长反射修改
│  ├─ WheelConfig.java           Common + Client 配置定义与安全读取
│  ├─ WheelSource.java           主手/副手/已佩戴 三来源解析
│  ├─ ModItems / ModSounds / CurioSlotCondition.java
│  ├─ client/  WheelScreen（界面）/ AdaptationHud / WheelKeys(G) / ClientSetup /
│  │           WheelHeadLayer / WheelCurioRenderer / WheelRenderHelper
│  ├─ network/ ModNetwork / HudMessagePacket / SettingPacket / ActionPacket
│  └─ mixin/   LightTextureMixin（黑暗适应的夜视注入）
├─ src/main/resources/
│  ├─ META-INF/mods.toml  依赖声明（forge/minecraft/curios）
│  ├─ assets/mahoraga_wheel/  lang(zh_cn/en_us) / models/item / sounds / textures(gui/item/slot)
│  ├─ data/  Curios 饰品栏定义（size 0）与 tag
│  └─ mahoraga_wheel.mixins.json / refmap / pack.mcmeta
└─ run/  本地测试存档（config/logs/mods/saves…）
```

## 10. 构建与测试

- 构建：`.\build-local.ps1`（默认任务 `clean build genIntellijRuns`；自动使用 `JAVA_HOME` 或 `.tools\jdk17-zulu` 下的 Java 17，Gradle 缓存优先 `F:\.gradle`）。也可手动指定：`.\build-local.ps1 -JdkHome C:\path\to\jdk-17`。
- 客户端运行：Gradle 的 `runClient`，工作目录为 `run/`。
- 注意 `minecraft.runs` 中配置了开发环境 refmap 重映射（`mixin.env.remapRefMap=true`），不要随意删除，否则 Mixin 在开发环境不生效。

## 11. 后续加内容时的扩展点（以新增一种便利适应为例）

1. `AdaptationData`：定新 Key（如 `C|water`），必要时在 `Kind` 加枚举并处理 `of()` / `reduction()` / `convenience()`；
2. `ConvenienceHandler`：加 `tickXxx`（在 `onLivingTick` 的每秒分支调用）＋ `complete/progress` 推送，注意跑步式“升级”与黑暗式“满级”两种完成语义；
3. `WheelConfig`：在 `convenience` 分组加开关与数值；
4. `SettingPacket.currentMax/scale` 与 `WheelScreen.maxOf/scaleOf/labelKey/nameColor`：补新类型的上限、缩放、标签、颜色、进度条样式；
5. `lang/zh_cn.json` + `en_us.json`：补 `mahoraga_wheel.adapt.<x>`、`gui.mahoraga_wheel.label.<x>(.tip)`、`gui.mahoraga_wheel.kind.<x>`、`message.mahoraga_wheel.*`；
6. 如需客户端表现（夜视/粒子/属性修饰器），参考 `LightTextureMixin` / `playTurn` / `updateRunModifier`。
