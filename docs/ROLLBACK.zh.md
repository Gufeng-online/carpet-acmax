# GitHub 更新与回滚

本次更新为 Carpet-ACMAX v1.5.6（Minecraft 26.2 / Java 25），包含服务端假人虚空交易。独立客户端虚空交易项目不属于本仓库。

更新前的 main 保存在标签 `backup/pre-v1.5.6-20261005`，提交为 `31bf26b5385c27c16988a027bfc83f82e9aceae6`。更新采用普通追加提交，保留原有完整历史。

## 查看或构建旧版

在另一个目录建立旧版工作区，当前工作区不会受影响：

```sh
git fetch origin --tags
git worktree add ../carpet-acmax-before-v1.5.6 backup/pre-v1.5.6-20261005
```

## 将 main 恢复为更新前的代码

在没有未提交修改的工作区执行以下命令。它新建一个恢复提交，将项目文件恢复为该标签对应版本，并保留更新后的历史以便再次恢复：

```sh
git switch main
git pull --ff-only origin main
git restore --source=backup/pre-v1.5.6-20261005 --staged --worktree -- .
git commit -m "Restore code before v1.5.6 integration"
git push origin main
```

Git 回滚只恢复仓库文件，不恢复世界、玩家背包或本机 config；需要恢复存档时请使用对应世界备份。
