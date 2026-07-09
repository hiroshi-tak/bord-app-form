# Whiteboard App

## 概要

複数ユーザーが同じホワイトボードへリアルタイムで描画できるWebアプリケーションです。

JWT認証によるログイン機能を実装し、WebSocketを利用したリアルタイム描画・カーソル共有・Undo/Redoに対応しています。また、管理者画面ではユーザーやボードの管理を行うことができます。

### 主な機能

| 機能        | 内容           |
| --------- | ------------ |
| ユーザー登録    | ユーザー作成       |
| ログイン      | JWT Cookie認証 |
| ログアウト     | Cookie削除     |
| ボード作成     | タイトル指定       |
| ボード一覧     | 参加可能         |
| 描画        | Canvas       |
| リアルタイム同期  | WebSocket    |
| カーソル共有    | リアルタイム表示     |
| Undo/Redo | 描画履歴         |
| 消しゴム      | 部分削除         |
| 管理者画面     | ユーザー管理・ボード管理 |

### 認証仕様

- 認証方式：JWT（HttpOnly Cookie）
- AccessToken有効期限：1時間
- RefreshTokenによる再発行対応

### 制約

- WebSocket同時接続数：最大5ユーザー（アプリケーション全体）

### 画面イメージ

### ボード一覧

![bordlist](docs/images/bordlist.png)

### ホワイトボード描画

![bordcanvas](docs/images/bordcanvas.png)

## 環境構築
```bash
git clone <repository>
cd bord-app-form

docker compose up --build
```

## DBリセット
```bash
docker compose down -v
docker compose up --build 
``` 

## 開発環境

- トップ画面:http://localhost:5173

## 使用技術

| 分類        | 技術              |
| --------- | --------------- |
| Frontend  | Vue 3           |
| Build     | Vite            |
| Language  | TypeScript      |
| CSS       | TailwindCSS     |
| Backend   | Spring Boot     |
| Language  | Java 21         |
| Security  | Spring Security |
| Auth      | JWT             |
| DB        | PostgreSQL 16   |
| ORM       | Spring Data JPA |
| Realtime  | WebSocket       |
| Container | Docker Compose  |


## システム構成図
```mermaid
graph TD
    Browser["Browser (Vue App)"] --> |HTTP/HTTPS| Nginx

    Nginx -->|REST API| SpringBoot
    Nginx -->|WebSocket| SpringBoot

    SpringBoot -->|Spring Data JPA| PostgreSQL
```

## シーケンス図

###　ユーザー登録

```mermaid
sequenceDiagram

actor User
participant Frontend
participant Backend
participant DB

User->>Frontend: ユーザー名・パスワード入力
Frontend->>Backend: POST /api/auth/register
Backend->>DB: ユーザー重複確認
DB-->>Backend: 結果

alt 未登録
    Backend->>DB: ユーザー保存
    DB-->>Backend: OK
    Backend-->>Frontend: 登録成功
else 重複
    Backend-->>Frontend: エラー
end

Frontend-->>User: 結果表示
```

### ログイン

```mermaid
sequenceDiagram

actor User
participant Frontend
participant Backend
participant DB

User->>Frontend: ログイン
Frontend->>Backend: POST /api/auth/login

Backend->>DB: ユーザー検索
DB-->>Backend: User

Backend->>Backend: パスワード照合

alt 認証成功
    Backend->>Backend: JWT生成
    Backend-->>Frontend: AccessToken Cookie<br>RefreshToken Cookie
    Frontend-->>User: ボード一覧表示
else 認証失敗
    Backend-->>Frontend: エラー
    Frontend-->>User: エラー表示
end
```

### ボード作成

```mermaid
sequenceDiagram

actor User
participant Frontend
participant Backend
participant DB

User->>Frontend: ボードタイトル入力

Frontend->>Backend: POST /api/boards

Backend->>DB: ボード保存

DB-->>Backend: 保存成功

Backend-->>Frontend: OK

Frontend->>Backend: GET /api/boards

Backend->>DB: ボード一覧取得

DB-->>Backend: 一覧

Backend-->>Frontend: ボード一覧

Frontend-->>User: 一覧更新
```

### リアルタイム描画

```mermaid
sequenceDiagram

actor UserA
actor UserB

participant FrontendA
participant Backend
participant FrontendB

UserA->>FrontendA: 描画開始

FrontendA->>Backend: WebSocket<br>stroke:add

Backend->>FrontendB: stroke:add

FrontendB->>UserB: 描画更新

UserA->>FrontendA: カーソル移動

FrontendA->>Backend: cursor:move

Backend->>FrontendB: cursor:move

FrontendB->>UserB: 赤カーソル更新
```

## ER図
```mermaid
erDiagram 

    User ||--o{ Board : Has

    User {
        int id PK
        string username
        string password
        string role
    }

    Board {
        int id PK
        int owner_id FK
        string title
        datetime createdAt
    }

```

## API

### 認証 API

| Method | Endpoint | 説明 | 認証 |
|---|---|---|---|
| POST | /api/auth/register | ユーザー登録 | 不要 |
| POST | /api/auth/login | ログイン（JWT Cookie発行） | 不要 |
| POST | /api/auth/logout | ログアウト（Cookie削除） | 不要 |
| POST | /api/auth/refresh | AccessToken再発行 | 不要 |
| GET | /api/me | ログインユーザー情報取得 | 必要 |


### Board API

| Method | Endpoint | 説明 | 認証 |
|---|---|---|---|
| POST | /api/boards | ボード作成 | 必要 |
| GET | /api/boards | ボード一覧取得 | 必要 |
| GET | /api/boards/{id} | ボード詳細取得 | 必要 |


### Admin API

| Method | Endpoint | 説明 | 認証 |
|---|---|---|---|
| GET | /api/admin/users | ユーザー一覧 | ADMIN |
| DELETE | /api/admin/users/{id} | ユーザー削除 | ADMIN |
| GET | /api/admin/boards | ボード一覧 | ADMIN |
| DELETE | /api/admin/boards/{id} | ボード削除 | ADMIN |


### WebSocket

| Endpoint | 説明 |
|---|---|
| /ws/board?boardId={id} | リアルタイム描画通信 |

### Swagger UI

Swagger UI を利用して API の仕様を確認できます。

- ローカル環境

  - http://localhost:8080/swagger-ui/index.html

- EC2環境

  - http://<サーバーIP>/swagger-ui/index.html
