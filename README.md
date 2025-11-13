# Firebase JSONインポートプロジェクト

FirebaseのFirestoreデータベースにJSONファイルからデータをインポートするJavaアプリケーションです。

H2/MySQLのテーブル定義をFirebase Firestoreに移行するための完全なツールセットを提供します。

## 必要なもの

- Java 11以上
- Maven
- Firebaseプロジェクト
- Firebase Admin SDKのサービスアカウントキー

## セットアップ手順

### 1. Firebaseサービスアカウントキーの取得

1. [Firebase Console](https://console.firebase.google.com/)にアクセス
2. プロジェクトを選択
3. 設定（⚙️）→ プロジェクトの設定 → サービスアカウント
4. 「新しい秘密鍵の生成」をクリック
5. ダウンロードしたJSONファイルをプロジェクトルートに配置
6. ファイル名を `serviceAccountKey.json` にリネーム（推奨）

## プロジェクト構成

```
testproject/
├── pom.xml                                 # Maven設定
├── serviceAccountKey.json                  # Firebaseサービスアカウントキー（要配置）
├── docs/
│   └── FIRESTORE_DATABASE_DESIGN.md       # Firestoreデータベース設計書
├── data/
│   ├── import-data.json                   # サンプルデータ（単一コレクション用）
│   └── firestore/                         # Firestoreインポート用JSONファイル
│       ├── departmentMaster.json          # 負担部門マスタ
│       ├── vendorMaster.json              # 支払先マスタ
│       ├── accountMaster.json             # 勘定科目マスタ
│       ├── segmentMaster.json             # セグメントマスタ
│       ├── taxCategoryMaster.json         # 税区分マスタ
│       ├── closingDateMaster.json         # 締め日マスタ
│       ├── debtTypeMaster.json            # 債務明細タイプマスタ
│       ├── freeSegmentMaster.json         # フリーセグメントマスタ
│       ├── itemSegmentMaster.json         # 科目別フリーセグマスタ
│       ├── errorMessageMaster.json        # エラーメッセージマスタ
│       ├── taxRateMaster.json             # 税率マスタ
│       ├── voucherNumberMaster.json       # 伝票番号管理
│       ├── costAbsorptionHeader.json      # 経費配賦ヘッダ
│       ├── costAbsorptionDetail.json      # 経費配賦明細
│       ├── csvImportLayout.json           # CSV取込レイアウト
│       ├── costInputBasic.json            # 経費申請基本情報
│       └── costInputAbsorption.json       # 経費配賦明細
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── firebase/
                        ├── FirebaseImporter.java       # 単一コレクションインポーター
                        └── BulkFirebaseImporter.java   # 一括インポーター
```

## データベース設計

詳細なFirestoreコレクション設計については、以下のドキュメントを参照してください：

📄 **[Firestoreデータベース設計書](docs/FIRESTORE_DATABASE_DESIGN.md)**

### 主なコレクション（全17種類）

1. **costInputBasic** - 経費申請基本情報
2. **costInputAbsorption** - 経費配賦明細
3. **voucherNumberMaster** - 伝票番号管理
4. **departmentMaster** - 負担部門マスタ
5. **vendorMaster** - 支払先マスタ
6. **accountMaster** - 勘定科目マスタ
7. **segmentMaster** - セグメントマスタ
8. **taxCategoryMaster** - 税区分マスタ
9. **closingDateMaster** - 締め日マスタ
10. **debtTypeMaster** - 債務明細タイプマスタ
11. **freeSegmentMaster** - フリーセグメントマスタ
12. **itemSegmentMaster** - 科目別フリーセグマスタ
13. **costAbsorptionHeader** - 経費配賦ヘッダ
14. **costAbsorptionDetail** - 経費配賦明細
15. **errorMessageMaster** - エラーメッセージマスタ
16. **csvImportLayout** - CSV取込レイアウト
17. **taxRateMaster** - 税率マスタ

## セットアップ手順

### 1. Firebaseサービスアカウントキーの取得

1. [Firebase Console](https://console.firebase.google.com/)にアクセス
2. プロジェクトを選択
3. 設定（⚙️）→ プロジェクトの設定 → サービスアカウント
4. 「新しい秘密鍵の生成」をクリック
5. ダウンロードしたJSONファイルをプロジェクトルートに配置
6. ファイル名を `serviceAccountKey.json` にリネーム（推奨）

### 2. 依存関係のインストール

```bash
mvn clean install
```

## 使用方法

### 方法1: 全コレクションを一括インポート（推奨）

全17コレクションのデータを一度にインポートします。

```bash
# コンパイル
mvn compile

# 一括インポート実行
mvn exec:java -Dexec.mainClass="com.example.firebase.BulkFirebaseImporter"
```

**実行結果の例：**
```
Firebaseの初期化が完了しました

========================================
一括インポート開始
対象ファイル数: 17
========================================

▶ インポート中: departmentMaster.json → departmentMaster
✓ 完了: 3 件のドキュメントをインポート

▶ インポート中: vendorMaster.json → vendorMaster
✓ 完了: 2 件のドキュメントをインポート

...

========================================
インポート結果
成功: 17 コレクション
失敗: 0 コレクション
========================================
```

### 方法2: 単一コレクションをインポート

`FirebaseImporter.java` の設定を編集してください：

```java
// サービスアカウントキーのパス
initializeFirebase("serviceAccountKey.json");

// インポート先のコレクション名とデータファイルのパス
String jsonFilePath = "data/firestore/departmentMaster.json";
String collectionName = "departmentMaster";
```

実行：

```bash
mvn exec:java -Dexec.mainClass="com.example.firebase.FirebaseImporter"
```

## データのカスタマイズ

### JSONファイルの編集

`data/firestore/` 配下の各JSONファイルを編集することで、インポートするデータをカスタマイズできます。

**例: departmentMaster.json**

```json
{
  "1": {
    "sctnId": "1",
    "corpCd": "01",
    "sctnCd": "A001",
    "sctnNm": "総務部",
    "sctnRyaku": "総務",
    "startDt": "2024-01-01",
    "endDt": "2099-12-31",
    "stateFlg": 1
  }
}
```

### 新しいコレクションの追加

1. `data/firestore/` に新しいJSONファイルを作成
2. `BulkFirebaseImporter.java` の `COLLECTION_MAPPING` に追加

```java
put("yourNewCollection.json", "yourCollectionName");
```

## インポートするJSONデータの形式

### オブジェクト形式（推奨）
キーがドキュメントIDになります：

```json
{
  "user1": {
    "name": "田中太郎",
    "email": "tanaka@example.com",
    "age": 30
  },
  "user2": {
    "name": "佐藤花子",
    "email": "sato@example.com",
    "age": 25
  }
}
```

### 配列形式
ドキュメントIDが自動生成されます（`importJsonArrayToFirestore`メソッドを使用）：

```json
[
  {
    "name": "田中太郎",
    "email": "tanaka@example.com",
    "age": 30
  },
  {
    "name": "佐藤花子",
    "email": "sato@example.com",
    "age": 25
  }
]
```

配列形式を使用する場合は、`FirebaseImporter.java`の`main`メソッドで以下のように変更してください：

```java
// importJsonToFirestore(jsonFilePath, collectionName);
importJsonArrayToFirestore(jsonFilePath, collectionName);
```


`FirebaseImporter.java` の以下の部分を編集してください：

```java
// サービスアカウントキーのパス
initializeFirebase("serviceAccountKey.json");

// インポート先のコレクション名とデータファイルのパス
String jsonFilePath = "data/import-data.json";
String collectionName = "users"; // インポート先のコレクション名を変更
```

### 4. インポートするJSONデータの形式

#### オブジェクト形式（推奨）
キーがドキュメントIDになります：

```json
{
  "user1": {
    "name": "田中太郎",
    "email": "tanaka@example.com",
    "age": 30
  },
  "user2": {
    "name": "佐藤花子",
    "email": "sato@example.com",
    "age": 25
  }
}
```

#### 配列形式
ドキュメントIDが自動生成されます（`importJsonArrayToFirestore`メソッドを使用）：

```json
[
  {
    "name": "田中太郎",
    "email": "tanaka@example.com",
    "age": 30
  },
  {
    "name": "佐藤花子",
    "email": "sato@example.com",
    "age": 25
  }
]
```

配列形式を使用する場合は、`FirebaseImporter.java`の`main`メソッドで以下のように変更してください：

```java
// importJsonToFirestore(jsonFilePath, collectionName);
importJsonArrayToFirestore(jsonFilePath, collectionName);
```

## 主な機能

### BulkFirebaseImporter（一括インポーター）
- ✅ 17コレクションを一度にインポート
- ✅ 自動的なコレクションマッピング
- ✅ インポート結果の詳細レポート
- ✅ エラーハンドリングと継続処理

### FirebaseImporter（単一インポーター）
- ✅ Firebase Admin SDKを使用した安全な認証
- ✅ JSONファイルからFirestoreへのデータインポート
- ✅ オブジェクト形式と配列形式のJSONに対応
- ✅ ドキュメントIDの指定または自動生成
- ✅ マージモードでの上書き保存

## トラブルシューティング

### エラー: "The Application Default Credentials are not available"
→ サービスアカウントキーのパスが正しいか確認してください

### エラー: "Permission denied"
→ Firebaseコンソールで、サービスアカウントに適切な権限が付与されているか確認してください

### データがインポートされない
→ コレクション名とJSONファイルのパスが正しいか確認してください

### ファイルが見つからない
→ `data/firestore/` ディレクトリとJSONファイルが存在するか確認してください

## Firestoreでのデータ確認

1. [Firebase Console](https://console.firebase.google.com/)にアクセス
2. プロジェクトを選択
3. Firestore Database → データタブ
4. インポートしたコレクションを選択してデータを確認

## RDBとの違い

### 外部キー制約
- **RDB**: データベース側で自動チェック
- **Firestore**: アプリケーション層で整合性を保証

### 複合主キー
- **RDB**: 複数カラムで主キーを構成
- **Firestore**: `{key1}_{key2}` 形式でドキュメントIDを生成

### トランザクション
- **RDB**: ACID特性を標準サポート
- **Firestore**: Firestore Transactionsを使用

### マスタの有効/無効
- 両方とも `stateFlg` フィールドで管理（1:有効, 0:無効）

## セキュリティに関する注意

⚠️ **重要**: `serviceAccountKey.json` はGitにコミットしないでください！

`.gitignore` に以下を追加することを推奨します：

```
serviceAccountKey.json
*.json
!data/*.json
!data/firestore/*.json
```

## 次のステップ

1. ✅ **データ設計書を確認**: `docs/FIRESTORE_DATABASE_DESIGN.md`
2. ✅ **サンプルデータをインポート**: `BulkFirebaseImporter` を実行
3. ✅ **データをカスタマイズ**: `data/firestore/` のJSONファイルを編集
4. ✅ **セキュリティルールを設定**: Firebase Consoleで設定
5. ✅ **インデックスを作成**: 複合インデックスを設定

## ライセンス

MIT License

