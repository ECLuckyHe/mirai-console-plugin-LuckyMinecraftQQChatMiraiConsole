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

| 项目                | 类型             | 描述            |
|-------------------|----------------|---------------|
| sessionId         | `VarLong`      | 会话号           |
| serverName        | `VarIntString` | 服务器名称         |
| joinFormatString  | `VarIntString` | 玩家进入游戏消息格式    |
| quitFormatString  | `VarIntString` | 玩家退出游戏消息格式    |
| msgFormatString   | `VarIntString` | 玩家发送消息的消息格式   |
| deathFormatString | `VarIntString` | 玩家死亡的消息格式     |
| kickFormatString  | `VarIntString` | 玩家被踢出服务器的消息格式 |

#### 往Client
对往Server的**0x00**包的回应，表示用于连接的数据包格式正确，连接成功。  
包ID: **0x00**

| 项目           | 类型             | 描述            |
|--------------|----------------|---------------|
| sessionName  | `VarIntString` | 会话名           |
| address      | `VarIntString` | bot收到的连接的远程地址 |
| heartbeatGap | `VarInt`       | 心跳包间隔         |
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

| 项目                  | 类型             | 描述         |
|---------------------|----------------|------------|
| groupId             | `VarLong`      | 群号         |
| groupName           | `VarIntString` | 群名         |
| groupNickname       | `VarIntString` | 群昵称（bot指定） |
| senderId            | `VarLong`      | 发送者qq      |
| senderNickname      | `VarIntString` | 发送者昵称      |
| senderGroupNickname | `VarIntString` | 发送者在群内的昵称  |
| message             | `VarIntString` | 消息内容       |
<br>

消息内容，此消息将原封不动发至游戏中。  
包ID: **0x11**

| 项目     | 类型             | 描述         |
|--------|----------------|------------|
| string | `VarIntString` | 被发送到游戏中的内容 |
<br>

Pong，回应Ping包。  
包ID: **0x20**

| 项目  | 类型        | 描述          |
|-----|-----------|-------------|
| num | `VarLong` | 与Ping包相同的数据 |
<br>

要求发送在线玩家信息数据的数据包。  
包ID: **0x21**

| 项目      | 类型        | 描述      |
|---------|-----------|---------|
| groupId | `VarLong` | 消息来自的群号 |
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

Ping包。  
包ID: **0x20**

| 项目  | 类型        | 描述                                    |
|-----|-----------|---------------------------------------|
| num | `VarLong` | 生成的数字，要求Client回复相同数字，若超时不回复或数字错误则断开连接 |
<br>

在线玩家信息数据，当服务端要求包**0x21**时才发送。  
包ID: **0x21**

| 项目            | 类型             | 描述            |
|---------------|----------------|---------------|
| groupId       | `VarLong`      | 要求获取在线玩家信息的群号 |
| onlinePlayers | `VarInt`       | 在线玩家数（假设是n）   |
| playerName 1  | `VarIntString` | 玩家1的ID        |
| playerName 2  | `VarIntString` | 玩家2的ID        |
| ……            | `VarIntString` | ……            |
| playerName n  | `VarIntString` | 玩家n的ID        |
<br>

关闭连接，当关闭时发送，并携带关闭信息，发送后直接关闭。  
包ID: **0xF0**

| 项目     | 类型             | 描述   |
|--------|----------------|------|
| string | `VarIntString` | 关闭信息 |