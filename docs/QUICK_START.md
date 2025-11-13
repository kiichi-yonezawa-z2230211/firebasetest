# クイックスタートガイド

このガイドでは、FirebaseにH2/MySQLのデータを移行する手順を説明します。

## ステップ1: 前提条件の確認

- ✅ Java 11以上がインストールされている
- ✅ Mavenがインストールされている
- ✅ Firebaseプロジェクトが作成済み

## ステップ2: Firebaseサービスアカウントキーを取得

1. [Firebase Console](https://console.firebase.google.com/)を開く
2. プロジェクトを選択
3. ⚙️（設定）→ プロジェクトの設定 → サービスアカウント
4. 「新しい秘密鍵の生成」をクリック
5. ダウンロードしたJSONファイルを `serviceAccountKey.json` としてプロジェクトルートに配置

```
testproject/
├── serviceAccountKey.json  ← ここに配置
├── pom.xml
├── data/
└── src/
```

## ステップ3: 依存関係のインストール

ターミナルでプロジェクトルートに移動し、以下を実行：

```bash
mvn clean install
```

## ステップ4: データのカスタマイズ（オプション）

`data/firestore/` ディレクトリ内のJSONファイルを編集して、インポートするデータをカスタマイズできます。

### 編集可能なファイル一覧

| ファイル名 | コレクション名 | 説明 |
|-----------|--------------|------|
| departmentMaster.json | departmentMaster | 負担部門マスタ |
| vendorMaster.json | vendorMaster | 支払先マスタ |
| accountMaster.json | accountMaster | 勘定科目マスタ |
| segmentMaster.json | segmentMaster | セグメントマスタ |
| taxCategoryMaster.json | taxCategoryMaster | 税区分マスタ |
| closingDateMaster.json | closingDateMaster | 締め日マスタ |
| debtTypeMaster.json | debtTypeMaster | 債務明細タイプマスタ |
| freeSegmentMaster.json | freeSegmentMaster | フリーセグメントマスタ |
| itemSegmentMaster.json | itemSegmentMaster | 科目別フリーセグマスタ |
| errorMessageMaster.json | errorMessageMaster | エラーメッセージマスタ |
| taxRateMaster.json | taxRateMaster | 税率マスタ |
| voucherNumberMaster.json | voucherNumberMaster | 伝票番号管理 |
| costAbsorptionHeader.json | costAbsorptionHeader | 経費配賦ヘッダ |
| costAbsorptionDetail.json | costAbsorptionDetail | 経費配賦明細 |
| csvImportLayout.json | csvImportLayout | CSV取込レイアウト |
| costInputBasic.json | costInputBasic | 経費申請基本情報 |
| costInputAbsorption.json | costInputAbsorption | 経費配賦明細 |

## ステップ5: 一括インポート実行

全17コレクションを一度にインポート：

```bash
mvn exec:java -Dexec.mainClass="com.example.firebase.BulkFirebaseImporter"
```

### 実行結果の確認

成功すると、以下のような出力が表示されます：

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

...（中略）

========================================
インポート結果
成功: 17 コレクション
失敗: 0 コレクション
========================================

========================================
全コレクションのインポートが完了しました！
========================================
```

## ステップ6: Firestoreでデータを確認

1. [Firebase Console](https://console.firebase.google.com/)を開く
2. プロジェクトを選択
3. Firestore Database → データタブ
4. インポートされたコレクションを確認

## よくある質問（FAQ）

### Q1: 特定のコレクションだけをインポートしたい

A: `FirebaseImporter.java` を使用してください。

```java
String jsonFilePath = "data/firestore/departmentMaster.json";
String collectionName = "departmentMaster";
```

実行：
```bash
mvn exec:java -Dexec.mainClass="com.example.firebase.FirebaseImporter"
```

### Q2: データを再インポートすると上書きされる?

A: はい。既存のドキュメントは `SetOptions.merge()` により上書きされます。完全に置き換えたい場合は、Firestoreコンソールからコレクションを削除してから再インポートしてください。

### Q3: エラーが発生した場合は?

A: エラーメッセージを確認し、以下をチェックしてください：

- `serviceAccountKey.json` のパスが正しいか
- JSONファイルの形式が正しいか（有効なJSON形式か）
- Firebaseのサービスアカウントに適切な権限があるか

### Q4: 大量のデータをインポートする場合は?

A: Firestoreには書き込みレート制限があります（1秒あたり最大10,000回）。大量データの場合は、バッチ処理や非同期処理を検討してください。

### Q5: 本番環境での使用方法は?

A: 以下の点に注意してください：

1. **サービスアカウントキーの管理**: 環境変数やシークレット管理サービスを使用
2. **セキュリティルールの設定**: Firebase Consoleで適切なルールを設定
3. **バックアップ**: インポート前に既存データをバックアップ
4. **テスト**: 本番環境でのインポート前に、開発環境でテスト

## トラブルシューティング

### エラー: "java.io.FileNotFoundException: serviceAccountKey.json"

**原因**: サービスアカウントキーファイルが見つからない

**解決策**: 
1. ファイルがプロジェクトルートに配置されているか確認
2. ファイル名が `serviceAccountKey.json` になっているか確認

### エラー: "Permission denied"

**原因**: サービスアカウントに必要な権限がない

**解決策**: 
1. Firebase Consoleでサービスアカウントの権限を確認
2. 「Cloud Firestore 管理者」ロールが付与されているか確認

### エラー: "com.google.gson.JsonSyntaxException"

**原因**: JSONファイルの形式が不正

**解決策**: 
1. JSONファイルをJSONバリデーターで確認
2. カンマ、括弧、引用符が正しいか確認

## 次のステップ

✅ **データ設計を確認**: `docs/FIRESTORE_DATABASE_DESIGN.md` を読む

✅ **セキュリティルールを設定**: Firestoreのセキュリティルールを設定

✅ **インデックスを作成**: 複合クエリ用のインデックスを作成

✅ **アプリケーション開発**: Firestoreを使用したアプリケーションを開発

## サポート

問題が解決しない場合は、以下を確認してください：

- [Firebase公式ドキュメント](https://firebase.google.com/docs/firestore)
- [GitHub Issues](プロジェクトのGitHub Issues URL)
- README.md のトラブルシューティングセクション
