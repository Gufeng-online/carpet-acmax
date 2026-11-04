# GitHub 更新与回滚

Carpet-ACMAX v1.5.6（Minecraft 26.2 / Java 25）首次整合服务端假人虚空交易；后续版本的回滚点记录在对应章节。独立客户端虚空交易项目不属于本仓库。

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

## v1.5.7 更新前的版本

2026-10-20 更新前的 main 保存在 `backup/pre-v1.5.7-20261020`，提交为 `2dca863972f9a8e35400097c71d3e23cad78de11`。原来的 `v1.5.6` 发布标签和 Release 仍保留。

另开工作区查看更新前代码：

```sh
git fetch origin --tags
git worktree add ../carpet-acmax-before-v1.5.7 backup/pre-v1.5.7-20261020
```

若要让 main 恢复为此版本，在没有未提交修改的工作区执行：

```sh
git switch main
git pull --ff-only origin main
git restore --source=backup/pre-v1.5.7-20261020 --staged --worktree -- .
git commit -m "Restore code before v1.5.7"
git push origin main
```

## v1.5.8 更新前的版本

2026-10-31 将本地 v1.5.8 同步到 GitHub 前，远端 main 为 v1.5.7，提交为 `f9d82587bf0762a0d0d9ef952dd433790facfd66`，保存为标签 `backup/pre-v1.5.8-20261031`。v1.5.8 使用普通追加提交，旧版发布标签、Release 和成品均保留。

另开工作区查看更新前代码：

```sh
git fetch origin --tags
git worktree add ../carpet-acmax-before-v1.5.8 backup/pre-v1.5.8-20261031
```

在没有未提交修改的工作区，撤销 v1.5.8 提交并保留完整历史：

```sh
git switch main
git pull --ff-only origin main
git revert v1.5.8
git push origin main
```

也可从备份标签恢复文件后创建普通提交：

```sh
git restore --source=backup/pre-v1.5.8-20261031 --staged --worktree -- .
git commit -m "Restore code before v1.5.8"
```

本地推送前备份包含未提交源码、构建配置、说明和已有成品：`../carpet-acmax-backups/carpet-acmax-v1.5.8-before-github-2026-10-31-3c59962d/project-v1.5.8.zip`；同目录的 `history-before-v1.5.8-push.bundle` 保留 Git 历史，`BACKUP.json` 记录 SHA-256。备份文件为首次创建。

## v1.5.9 发布前的版本

2026-11-05 发布前，远端 main 已为 v1.5.9，提交为 `2dfb3526bccc28fa0e3ea4f3e13ad6fa43b97918`。发布标签 `v1.5.9` 指向这一成功 CI 构建提交；更新前状态另存为 `backup/pre-v1.5.9-release-20261105-8841e39f`，后续发布说明和成品同步使用普通追加提交。原有 v1.5.8 发布及标签保留。

另开目录检查发布资料更新前的文件：

```sh
git fetch origin --tags
git worktree add ../carpet-acmax-before-v1.5.9-release backup/pre-v1.5.9-release-20261105-8841e39f
```

若要撤销后续发布资料提交，在确认没有未保存修改后，从备份标签恢复文件并创建普通提交：

```sh
git restore --source=backup/pre-v1.5.9-release-20261105-8841e39f --staged --worktree -- .
git commit -m "Restore files before v1.5.9 release documentation"
```

该备份点仍包含 v1.5.9 功能；若要退回 v1.5.8 功能，使用已有 `v1.5.8` 标签或对应 Release 安装包。文件提交回滚不会自动删除或改变 GitHub Release。

本地同步前备份为 `../carpet-acmax-backups/carpet-acmax-v1.5.8-before-v1.5.9-release-2026-11-05-8841e39f/`，保存本地 v1.5.8 完整项目、工作区文档、远端发布前文件、Git 历史与 SHA-256 核验资料，均为首次创建。
