# 协议

## 数据包结构定义

| 项目          | 类型       | 描述                    |
|-------------|----------|-----------------------|
| totalLength | `VarInt` | 以下项目的总长度              |
| packetId    | `VarInt` | 包ID，决定了进行何种操作（将在下面列出） |
| data        | `byte[]` | 数据字节                  |

### 连接

#### 往Server

Client应该连接后主动发送此数据包。  
包ID: **0x00**

| 项目                                    | 类型               | 描述               |
|---------------------------------------|------------------|------------------|
| sessionId                             | `VarLong`        | 会话号              |
| serverName                            | `VarIntString`   | 服务器名称            |
| joinFormatString                      | `VarIntString`   | 玩家进入游戏消息格式       |
| quitFormatString                      | `VarIntString`   | 玩家退出游戏消息格式       |
| msgFormatString                       | `VarIntString`   | 玩家发送消息的消息格式      |
| deathFormatString                     | `VarIntString`   | 玩家死亡的消息格式        |
| kickFormatString                      | `VarIntString`   | 玩家被踢出服务器的消息格式    |
| onlinePlayersCommandsCount            | `VarInt`         | 获取在线玩家列表的指令数     |
| onlinePlayersCommands                 | `VarIntString[]` | 在线玩家指令           |
| onlinePlayersCommandResponseFormat    | `VarIntString`   | 在线玩家列表消息格式       |
| onlinePlayersCommandResponseSeparator | `VarIntString`   | 在线玩家列表消息玩家分隔符    |
| rconCommandPrefix                     | `VarIntString`   | rcon指令前缀         |
| rconCommandResultFormat               | `VarIntString`   | rcon指令返回结果格式     |
| userCommandPrefix                     | `VarIntString`   | 用户指令前缀           |
| userBindPrefix                        | `VarIntString`   | 用户绑定mcid与qq前缀    |
| getUserCommandCount                   | `VarInt`         | 获取\[获取用户指令]的指令条数 |
| getUserCommandsCommand                | `VarIntString[]` | 获取\[获取用户指令]的指令列表 |
| whitelistCorrectMessage               | `VarIntString`   | 白名单uuid修改完成信息    |
| whitelistTryMessage                   | `VarIntString`   | 非白名单玩家尝试进入游戏信息   |

#### 往Client

对往Server的**0x00**包的回应，表示用于连接的数据包格式正确，连接成功。  
包ID: **0x00**

| 项目                | 类型             | 描述            |
|-------------------|----------------|---------------|
| sessionName       | `VarIntString` | 会话名           |
| address           | `VarIntString` | bot收到的连接的远程地址 |
| heartbeatInterval | `VarInt`       | 心跳包间隔         |

<br>


对往Server的**0x00**包的回应，表示连接失败，格式错误，返回错误信息。  
包ID: **0x01**

| 项目           | 类型             | 描述     |
|--------------|----------------|--------|
| errorMessage | `VarIntString` | 错误信息内容 |

### 通信

#### 往Client

消息内容，当群内有消息时发送。  
包ID: **0x10**

| 项目                  | 类型                | 描述         |
|---------------------|-------------------|------------|
| groupId             | `VarLong`         | 群号         |
| groupName           | `VarIntString`    | 群名         |
| groupNickname       | `VarIntString`    | 群昵称（bot指定） |
| senderId            | `VarLong`         | 发送者qq      |
| senderNickname      | `VarIntString`    | 发送者昵称      |
| senderGroupNickname | `VarIntString`    | 发送者在群内的昵称  |
| messageObjectLength | `VarInt`          | 消息对象长度     |
| message             | `MessageObject[]` | 一系列的消息对象   |


其中消息对象定义格式如下：

| 项目            | 类型       | 描述            |
|---------------|----------|---------------|
| messageTypeId | `byte`   | 消息对象的类型号（一字节） |
| messageData   | `byte[]` | 与该类型相关的携带参数   |

消息类型与携带参数：  
类型: **文本**  
类型ID: **0x00**

| 项目        | 类型             | 描述      |
|-----------|----------------|---------|
| plainText | `VarIntString` | 一般的消息内容 |

类型: **@**  
类型ID: **0x01**

| 项目                | 类型             | 描述                                |
|-------------------|----------------|-----------------------------------|
| targetId          | `VarLong`      | 被@的对象QQ                           |
| targetDisplayName | `VarIntString` | 被@的对象在群里显示的@名片（即bot看见的，且最前面自带@符号） |

类型: **@全体成员**  
类型ID: **0x02**

| 项目  | 类型  | 描述  |
|-----|-----|-----|
| 无   | 无   | 无   |

类型: **图片**  
类型ID: **0x03**

| 项目     | 类型             | 描述     |
|--------|----------------|--------|
| picUrl | `VarIntString` | 图片链接地址 |

类型: **回复消息**  
类型ID: **0x04**

| 项目                  | 类型             | 描述        |
|---------------------|----------------|-----------|
| fromId              | `VarLong`      | 被回复对象QQ   |
| oldMessagePlainText | `VarIntString` | 被回复的原消息内容 |

类型: **动画表情**  
类型ID: **0x05**

| 项目     | 类型             | 描述       |
|--------|----------------|----------|
| picUrl | `VarIntString` | 动画表情链接地址 |

<br>

消息内容，此消息将原封不动发至游戏中。  
包ID: **0x11**

| 项目     | 类型             | 描述         |
|--------|----------------|------------|
| string | `VarIntString` | 被发送到游戏中的内容 |

<br>

Ping包。  
包ID: **0x20**

| 项目  | 类型        | 描述                                                                                |
|-----|-----------|-----------------------------------------------------------------------------------|
| num | `VarLong` | 生成的随机数字（如果是第一次收到心跳返回包，则再次发送数值为1的心跳包且不要求回复），要求Client回复相同数字加心跳间隔的和，若超时不回复或数字错误则断开连接 |

<br>

要求发送在线玩家信息数据的数据包。  
包ID: **0x21**

| 项目  | 类型  | 描述  |
|-----|-----|-----|
| 无   |     |     |

<br>

要求执行指令（需要已开启RCON）。  
包ID：**0x22**

| 项目      | 类型             | 描述                   |
|---------|----------------|----------------------|
| qq      | `VarLong`      | 发送者的qq（op判断由MC端判断执行） |
| command | `VarIntString` | 指令内容                 |

<br>

要求发布公告，当客户端收到公告后按照格式发送到MC游戏内。  
包ID：**0x23**

| 项目           | 类型             | 描述     |
|--------------|----------------|--------|
| qq           | `VarLong`      | 发送者的qq |
| nickname     | `VarIntString` | 发送者昵称  |
| announcement | `VarIntString` | 公告内容   |
<br>

要求发送用户指令，当客户端收到指令后返回指令执行结果（需要已开启RCON）。  
包ID：**0x24**

| 项目      | 类型             | 描述     |
|---------|----------------|--------|
| qq      | `VarLong`      | 发送者的qq |
| command | `VarIntString` | 指令内容   |

<br>

要求添加用户指令，当客户端收到该数据包后添加一条用户指令，并返回添加结果（需要确认远程op权限）。  
包ID：**0x25**

| 项目          | 类型             | 描述        |
|-------------|----------------|-----------|
| qq          | `VarLong`      | 发送者的qq    |
| name        | `VarIntString` | 该条指令的名字备注 |
| userCommand | `VarIntString` | 用户发送的指令格式 |
| mapCommand  | `VarIntString` | 实际执行的指令   |

<br>

要求删除用户指令，当客户端收到该数据包后删除一条用于指令，并返回删除结果（需要确认远程op权限）。  
包ID：**0x26**

| 项目          | 类型             | 描述        |
|-------------|----------------|-----------|
| qq          | `VarLong`      | 发送者的qq    |
| name        | `VarIntString` | 该条指令的名字备注 |

<br>

要求获取用户指令列表，当客户端收到该数据包后返回所有指令所构成的列表（mcchat指令）。  
包ID：**0x27**

| 项目  | 类型  | 描述  |
|-----|-----|-----|
| 无   | 无   | 无   |

<br>

要求绑定qq和mcid时发送该包，当客户端收到该数据包后返回执行结果。  
后续由在MC端的对应玩家操作。  
包ID：**0x28**

| 项目  | 类型             | 描述       |
|-----|----------------|----------|
| qq  | `VarLong`      | 玩家的qq    |
| id  | `VarIntString` | 玩家的mc id |

<br>

要求获取用户指令列表，当客户端收到该数据包后返回所有指令所构成的列表（普通用户）。  
包ID：**0x29**

| 项目  | 类型  | 描述  |
|-----|-----|-----|
| 无   | 无   | 无   |

<br>

关闭连接，当关闭时发送，并携带关闭信息，发送后直接关闭。  
包ID: **0xF0**

| 项目     | 类型             | 描述   |
|--------|----------------|------|
| string | `VarIntString` | 关闭信息 |

<br>

#### 往Server

当玩家加入时发送该包。  
包ID: **0x10**

| 项目         | 类型             | 描述      |
|------------|----------------|---------|
| playerName | `VarIntString` | 加入的玩家ID |

<br>

当玩家离开时发送该包。  
包ID: **0x11**

| 项目         | 类型             | 描述      |
|------------|----------------|---------|
| playerName | `VarIntString` | 离开的玩家ID |

<br>

当玩家在游戏内发送消息时发送该包。  
包ID：**0x12**

| 项目         | 类型             | 描述        |
|------------|----------------|-----------|
| playerName | `VarIntString` | 发送消息的玩家ID |
| message    | `VarIntString` | 发送的消息内容   |

<br>

当玩家在游戏内死亡时发送该包。  
包ID: **0x13**

| 项目           | 类型             | 描述     |
|--------------|----------------|--------|
| playerName   | `VarIntString` | 死亡玩家ID |
| deathMessage | `VarIntString` | 死亡信息   |

<br>

当玩家被踢出游戏时发送该包。  
包ID: **0x14**

| 项目         | 类型             | 描述     |
|------------|----------------|--------|
| playerName | `VarIntString` | 被踢玩家ID |
| kickReason | `VarIntString` | 踢出原因   |

<br>

Pong，回应Ping包。  
包ID: **0x20**

| 项目  | 类型        | 描述                             |
|-----|-----------|--------------------------------|
| num | `VarLong` | Ping包发送的数据与心跳包间隔之和（若数值为1，则不回复） |

<br>

在线玩家信息数据，当服务端要求包**0x21**时才发送。  
包ID: **0x21**

| 项目            | 类型               | 描述    |
|---------------|------------------|-------|
| onlinePlayers | `VarInt`         | 在线玩家数 |
| playerNames   | `VarIntString[]` | 玩家的ID |

<br>

执行指令返回结果，当服务端要求包**0x22**时才发送。  
包ID：**0x22**

| 项目            | 类型             | 描述     |
|---------------|----------------|--------|
| commandResult | `VarIntString` | 指令执行结果 |

<br>

执行用户指令返回结果，当服务端要求包**0x24**时才发送。  
包ID：**0x24**

| 项目            | 类型             | 描述     |
|---------------|----------------|--------|
| commandResult | `VarIntString` | 指令执行结果 |

<br>

执行添加用户指令返回结果，当服务端要求包**0x25**时才发送。  
包ID：**0x25**

| 项目        | 类型             | 描述   |
|-----------|----------------|------|
| addResult | `VarIntString` | 添加结果 |

<br>

执行删除用户指令返回结果，当服务端要求包**0x26**时才发送。  
包ID：**0x26**

| 项目        | 类型             | 描述   |
|-----------|----------------|------|
| delResult | `VarIntString` | 删除结果 |

<br>

用户指令列表，当服务端要求获取用户指令列表时返回（mcchat指令）。  
包ID：**0x27**

| 项目       | 类型             | 描述        |
|----------|----------------|-----------|
| count    | `VarInt`       | 用户指令总数    |
| name1    | `VarIntString` | 用户指令名1    |
| command1 | `VarIntString` | 用户指令1     |
| mapping1 | `VarIntString` | 用户指令实际指令1 |
| ...      | `...`          | ...       |

<br>

玩家绑定信息返回内容，当服务端要求绑定玩家qq和mcid时返回。  
包ID：**0x28**

| 项目  | 类型             | 描述   |
|-----|----------------|------|
| msg | `VarIntString` | 返回信息 |

<br>

用户指令列表，当服务端要求获取用户指令列表时返回（普通用户）。  
包ID：**0x29**

| 项目       | 类型             | 描述        |
|----------|----------------|-----------|
| count    | `VarInt`       | 用户指令总数    |
| name1    | `VarIntString` | 用户指令名1    |
| command1 | `VarIntString` | 用户指令1     |
| mapping1 | `VarIntString` | 用户指令实际指令1 |
| ...      | `...`          | ...       |

<br>

非白名单用户尝试登录（加了白名单但uuid不一致也会发送）。  
包ID：**0x30**

| 项目         | 类型             | 描述    |
|------------|----------------|-------|
| playerName | `VarIntString` | 登录玩家名 |

<br>

白名单uuid修改完成发送。  
包ID：**0x31**

| 项目         | 类型             | 描述  |
|------------|----------------|-----|
| playerName | `VarIntString` | 玩家名 |

<br>

关闭连接，当关闭时发送，并携带关闭信息，发送后直接关闭。  
包ID: **0xF0**

| 项目     | 类型             | 描述   |
|--------|----------------|------|
| string | `VarIntString` | 关闭信息 |