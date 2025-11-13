# Firebase Firestore データベース設計

## コレクション構造定義

FirebaseのFirestoreでは、RDBのテーブルは「コレクション」として、レコードは「ドキュメント」として表現されます。

---

## 主なコレクション定義

### 1. cost_input_basic（経費申請基本情報）

**コレクション名**: `costInputBasic`

**ドキュメントID**: `slip_no`（伝票番号）

**フィールド構成**:
```json
{
  "slipNo": "20250101-001",           // 伝票番号（ドキュメントID）
  "affiliationCode": "A001",          // 所属コード
  "burdenDeptCode": "A001",           // 負担部門コード（必須）
  "vendorCode": "V001",               // 支払先コード
  "accountCode": "AC001",             // 勘定科目コード
  "segmentCode": "S001",              // セグメントコード
  "paymentAmount": 100000,            // 支払金額（必須、数値）
  "taxTypeCode": "T01",               // 税区分コード
  "description": "出張費用",          // 摘要（最大50文字）
  "absorptionFlag": true,             // 配賦フラグ
  "registrationDate": "2025-01-15T00:00:00Z",  // 登録日時（Timestamp）
  "updatedAt": "2025-01-15T10:30:00Z"          // 更新日時
}
```

**検証ルール**:
- `burdenDeptCode`, `paymentAmount`: 必須
- `description`: 最大50文字
- `paymentAmount`: 数値型、最大20桁
- マスタ存在チェック: `vendorCode`, `accountCode`, `segmentCode`, `taxTypeCode`

**インデックス**:
- `burdenDeptCode`
- `vendorCode`
- `accountCode`
- `registrationDate`

---

### 2. cost_input_absorption（経費配賦明細）

**コレクション名**: `costInputAbsorption`

**ドキュメントID**: `{slip_no}_{section_code}`（複合キー）

**フィールド構成**:
```json
{
  "slipNo": "20250101-001",           // 伝票番号（外部キー）
  "sectionCode": "A001",              // 部門コード
  "sectionName": "総務部",            // 部門名
  "absorpPrice": 50000,               // 配賦金額
  "registrationDate": "2025-01-15T00:00:00Z"  // 登録日時
}
```

**インデックス**:
- `slipNo`

---

### 3. voucher_number_master（伝票番号管理）

**コレクション名**: `voucherNumberMaster`

**ドキュメントID**: `no`（管理番号）

**フィールド構成**:
```json
{
  "no": "1",
  "currentSlipNo": "20250101-100",    // 現在の伝票番号
  "maxSlipNo": "20250101-999",        // 最大伝票番号
  "minSlipNo": "20250101-001"         // 最小伝票番号
}
```

---

### 4. department_master（負担部門マスタ）

**コレクション名**: `departmentMaster`

**ドキュメントID**: `sctn_id`（部門ID）

**フィールド構成**:
```json
{
  "sctnId": "1",                      // 部門ID
  "corpCd": "01",                     // 会社コード
  "sctnCd": "A001",                   // 部門コード
  "sctnNm": "総務部",                 // 部門名
  "sctnRyaku": "総務",                // 部門略称
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ（1:有効, 0:無効）
}
```

**インデックス**:
- `sctnCd`
- `corpCd`
- `stateFlg`

---

### 5. vendor_master（支払先マスタ）

**コレクション名**: `vendorMaster`

**ドキュメントID**: `sirsak_cd`（支払先コード）

**フィールド構成**:
```json
{
  "sirsakCd": "V001",                 // 支払先コード
  "corpCd": "01",                     // 会社コード
  "sirsakNm": "株式会社ABC",          // 支払先名
  "sirsakAddr1": "東京都千代田区1-1-1", // 住所
  "tel": "03-1234-5678",              // 電話番号
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `sirsakCd`
- `corpCd`
- `stateFlg`

---

### 6. account_master（勘定科目マスタ）

**コレクション名**: `accountMaster`

**ドキュメントID**: `cal_itm_cd`（勘定科目コード）

**フィールド構成**:
```json
{
  "calItmCd": "AC001",                // 勘定科目コード
  "corpCd": "01",                     // 会社コード
  "calItmNm": "旅費交通費",           // 勘定科目名
  "syzeiTaiKb": "1",                  // 消費税対象区分
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `calItmCd`
- `corpCd`
- `stateFlg`

---

### 7. segment_master（セグメントマスタ）

**コレクション名**: `segmentMaster`

**ドキュメントID**: `segment_id`（セグメントID）

**フィールド構成**:
```json
{
  "segmentId": "S001",                // セグメントID
  "segmentName": "営業部門",          // セグメント名
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `segmentId`
- `stateFlg`

---

### 8. tax_category_master（税区分マスタ）

**コレクション名**: `taxCategoryMaster`

**ドキュメントID**: `cd_vle`（コード値）

**フィールド構成**:
```json
{
  "cdVle": "T01",                     // コード値
  "lngName": "課税",                  // 正式名称
  "srtName": "課",                    // 略称
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `stateFlg`

---

### 9. closing_date_master（締め日マスタ）

**コレクション名**: `closingDateMaster`

**ドキュメントID**: `{corp_code}_{target_yearmonth}`（複合キー）

**フィールド構成**:
```json
{
  "corpCode": "01",                   // 会社コード
  "targetYearmonth": "202501",        // 対象年月
  "bookDate": "2025-01-31",           // 計上日
  "payDate": "2025-02-10",            // 支払日
  "approveLimitDate": "2025-02-05",   // 承認期限日
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `corpCode`
- `targetYearmonth`

---

### 10. debt_type_master（債務明細タイプマスタ）

**コレクション名**: `debtTypeMaster`

**ドキュメントID**: `debt_type_code`（債務タイプコード）

**フィールド構成**:
```json
{
  "debtTypeCode": "D01",              // 債務タイプコード
  "debtTypeName": "未払",             // 債務タイプ名
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

---

### 11. free_segment_master（フリーセグメントマスタ）

**コレクション名**: `freeSegmentMaster`

**ドキュメントID**: `segment_id`（セグメントID）

**フィールド構成**:
```json
{
  "segmentId": "F001",                // セグメントID
  "segmentName": "本社セグメント",    // セグメント名
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

---

### 12. item_segment_master（科目別フリーセグマスタ）

**コレクション名**: `itemSegmentMaster`

**ドキュメントID**: `item_segment_id`（科目セグメントID）

**フィールド構成**:
```json
{
  "itemSegmentId": "1",               // 科目セグメントID
  "accountCode": "AC001",             // 勘定科目コード
  "segmentCode": "F001",              // セグメントコード
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

**インデックス**:
- `accountCode`
- `segmentCode`

---

### 13. cost_absorption_header（経費配賦ヘッダ）

**コレクション名**: `costAbsorptionHeader`

**ドキュメントID**: `slip_no`（伝票番号）

**フィールド構成**:
```json
{
  "slipNo": "20250101-001",           // 伝票番号
  "deptPositionCode": "D001",         // 部門役職コード
  "deptName": "営業本部",             // 部門名
  "selShopKbn": "01",                 // 店舗区分
  "selShopKbnNm": "本社",             // 店舗区分名
  "absorptionPattern": "1",           // 配賦パターン
  "csvFileName": "import202501.csv",  // CSVファイル名
  "registrationDate": "2025-01-15T00:00:00Z"  // 登録日時
}
```

**インデックス**:
- `slipNo`
- `registrationDate`

---

### 14. cost_absorption_detail（経費配賦明細）

**コレクション名**: `costAbsorptionDetail`

**ドキュメントID**: `{slip_no}_{section_code}`（複合キー）

**フィールド構成**:
```json
{
  "slipNo": "20250101-001",           // 伝票番号
  "sectionCode": "A001",              // 部門コード
  "sectionName": "総務部",            // 部門名
  "absorpPrice": 50000,               // 配賦金額
  "registrationDate": "2025-01-15T00:00:00Z"  // 登録日時
}
```

**インデックス**:
- `slipNo`

---

### 15. error_message_master（エラーメッセージマスタ）

**コレクション名**: `errorMessageMaster`

**ドキュメントID**: `error_code`（エラーコード）

**フィールド構成**:
```json
{
  "errorCode": "ERR001",              // エラーコード
  "errorMessage": "必須項目が未入力です",  // エラーメッセージ
  "stateFlg": 1                       // 状態フラグ
}
```

---

### 16. csv_import_layout（CSV取込レイアウト）

**コレクション名**: `csvImportLayout`

**ドキュメントID**: `import_id`（取込ID）

**フィールド構成**:
```json
{
  "importId": "1",                    // 取込ID
  "vendorCode": "V001",               // 支払先コード
  "invoiceNo": "INV001",              // インボイス番号
  "accountCode": "AC001",             // 勘定科目コード
  "description": "出張費",            // 摘要
  "paymentAmount": 100000,            // 支払金額
  "taxTypeCode": "T01",               // 税区分コード
  "burdenDeptCode": "A001",           // 負担部門コード
  "segmentCode": "S001",              // セグメントコード
  "approvalNo": "APP001",             // 承認番号
  "withholdingAmount": 0,             // 源泉徴収額
  "registrationDate": "2025-01-15T00:00:00Z"  // 登録日時
}
```

---

### 17. tax_rate_master（税率マスタ）

**コレクション名**: `taxRateMaster`

**ドキュメントID**: `tax_rate_code`（税率コード）

**フィールド構成**:
```json
{
  "taxRateCode": "TR01",              // 税率コード
  "taxRate": 10,                      // 税率（%）
  "startDt": "2024-01-01",            // 開始日
  "endDt": "2099-12-31",              // 終了日
  "stateFlg": 1                       // 状態フラグ
}
```

---

## Firebase固有の設計要素

### セキュリティルール
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // マスタデータは読み取り専用
    match /departmentMaster/{docId} {
      allow read: if request.auth != null;
      allow write: if request.auth.token.admin == true;
    }
    
    // 経費申請データは認証ユーザーのみアクセス可能
    match /costInputBasic/{docId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 複合インデックス
Firebaseコンソールで以下のインデックスを作成：
- `costInputBasic`: [`burdenDeptCode` (ASC), `registrationDate` (DESC)]
- `costInputAbsorption`: [`slipNo` (ASC), `sectionCode` (ASC)]
- `departmentMaster`: [`corpCd` (ASC), `stateFlg` (ASC)]

### データ型マッピング
- **VARCHAR** → `string`
- **INT/BIGINT** → `number`
- **DATE/TIMESTAMP** → `timestamp` または ISO8601文字列
- **BOOLEAN** → `boolean`

### 制約の実装
- **NOT NULL**: クライアント側またはCloud Functionsで検証
- **外部キー**: クライアント側でマスタ存在チェック
- **桁数制限**: Cloud Functionsまたはセキュリティルールで検証

### 備考
- RDBの「外部キー制約」はFirestoreにないため、アプリケーション層で整合性を保証
- 複合主キーは `{key1}_{key2}` 形式でドキュメントIDを生成
- マスタの有効/無効は `stateFlg` フィールドで管理（1:有効, 0:無効）
- トランザクション処理が必要な場合は、Firestore Transactionsを使用
