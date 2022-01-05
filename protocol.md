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

| 项目          | 类型             | 描述            |
|-------------|----------------|---------------|
| sessionName | `VarIntString` | 会话名           |
| address     | `VarIntString` | bot收到的连接的远程地址 |
<br>


对往Server的**0x00**包的回应，表示连接失败，格式错误，返回错误信息。  
包ID: **0x01**

| 项目           | 类型             | 描述     |
|--------------|----------------|--------|
| errorMessage | `VarIntString` | 错误信息内容 |