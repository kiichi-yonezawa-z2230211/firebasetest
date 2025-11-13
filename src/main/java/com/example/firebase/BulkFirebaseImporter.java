package com.example.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 複数のFirestoreコレクションに一括でJSONデータをインポートするクラス
 */
public class BulkFirebaseImporter {

    private static Firestore db;
    
    // コレクションマッピング（JSONファイル名 → Firestoreコレクション名）
    private static final Map<String, String> COLLECTION_MAPPING = new HashMap<String, String>() {{
        put("departmentMaster.json", "departmentMaster");
        put("vendorMaster.json", "vendorMaster");
        put("accountMaster.json", "accountMaster");
        put("segmentMaster.json", "segmentMaster");
        put("taxCategoryMaster.json", "taxCategoryMaster");
        put("closingDateMaster.json", "closingDateMaster");
        put("debtTypeMaster.json", "debtTypeMaster");
        put("freeSegmentMaster.json", "freeSegmentMaster");
        put("itemSegmentMaster.json", "itemSegmentMaster");
        put("errorMessageMaster.json", "errorMessageMaster");
        put("taxRateMaster.json", "taxRateMaster");
        put("voucherNumberMaster.json", "voucherNumberMaster");
        put("costAbsorptionHeader.json", "costAbsorptionHeader");
        put("costAbsorptionDetail.json", "costAbsorptionDetail");
        put("csvImportLayout.json", "csvImportLayout");
        put("costInputBasic.json", "costInputBasic");
        put("costInputAbsorption.json", "costInputAbsorption");
    }};

    public static void main(String[] args) {
        try {
            // プロキシ設定（社内ネットワーク用）
            // 必要に応じて、プロキシホストとポートを設定してください
            // System.setProperty("https.proxyHost", "your-proxy-host");
            // System.setProperty("https.proxyPort", "your-proxy-port");
            // System.setProperty("http.proxyHost", "your-proxy-host");
            // System.setProperty("http.proxyPort", "your-proxy-port");
            
            // Firebase初期化
            initializeFirebase("serviceAccountKey.json");

            // 一括インポート実行
            String dataDirectory = "data/firestore";
            bulkImportAllCollections(dataDirectory);

            System.out.println("\n========================================");
            System.out.println("全コレクションのインポートが完了しました！");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Firebaseを初期化する
     * 
     * @param serviceAccountKeyPath サービスアカウントキーのJSONファイルパス
     * @throws Exception 初期化エラー
     */
    private static void initializeFirebase(String serviceAccountKeyPath) throws Exception {
        FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://test1-384bd-default-rtdb.firebaseio.com")
                .setProjectId("test1-384bd")
                .build();

        FirebaseApp.initializeApp(options);
        
        db = FirestoreClient.getFirestore();
        
        System.out.println("Firebaseの初期化が完了しました\n");
    }

    /**
     * 指定ディレクトリ内の全JSONファイルをFirestoreにインポート
     * 
     * @param directoryPath JSONファイルが格納されているディレクトリパス
     * @throws Exception インポートエラー
     */
    private static void bulkImportAllCollections(String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("指定されたディレクトリが存在しません: " + directoryPath);
        }

        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("インポートするJSONファイルが見つかりませんでした");
            return;
        }

        System.out.println("========================================");
        System.out.println("一括インポート開始");
        System.out.println("対象ファイル数: " + jsonFiles.length);
        System.out.println("========================================\n");

        int successCount = 0;
        int failureCount = 0;

        for (File jsonFile : jsonFiles) {
            String fileName = jsonFile.getName();
            String collectionName = COLLECTION_MAPPING.get(fileName);

            if (collectionName == null) {
                System.out.println("⚠ スキップ: " + fileName + " (マッピング未定義)");
                continue;
            }

            try {
                System.out.println("▶ インポート中: " + fileName + " → " + collectionName);
                int count = importJsonToFirestore(jsonFile.getPath(), collectionName);
                System.out.println("✓ 完了: " + count + " 件のドキュメントをインポート\n");
                successCount++;
            } catch (Exception e) {
                System.err.println("✗ エラー: " + fileName + " - " + e.getMessage() + "\n");
                failureCount++;
            }
        }

        System.out.println("========================================");
        System.out.println("インポート結果");
        System.out.println("成功: " + successCount + " コレクション");
        System.out.println("失敗: " + failureCount + " コレクション");
        System.out.println("========================================");
    }

    /**
     * JSONファイルからFirestoreにデータをインポートする
     * 
     * @param jsonFilePath インポートするJSONファイルのパス
     * @param collectionName インポート先のコレクション名
     * @return インポートしたドキュメント数
     * @throws Exception インポートエラー
     */
    private static int importJsonToFirestore(String jsonFilePath, String collectionName) 
            throws Exception {
        
        Gson gson = new Gson();
        FileReader reader = new FileReader(jsonFilePath);

        // JSONをMapのMapとして読み込む（ドキュメントID → ドキュメントデータ）
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gson.fromJson(reader, type);
        reader.close();

        if (data == null || data.isEmpty()) {
            return 0;
        }

        // Firestoreにデータを書き込む
        int count = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String documentId = entry.getKey();
            Object documentData = entry.getValue();

            // ドキュメントをFirestoreに保存（マージモード）
            db.collection(collectionName)
                .document(documentId)
                .set(documentData, SetOptions.merge())
                .get(); // 同期的に実行

            count++;
        }

        return count;
    }

    /**
     * 特定のコレクションのみインポートする
     * 
     * @param jsonFilePath インポートするJSONファイルのパス
     * @param collectionName インポート先のコレクション名
     * @throws Exception インポートエラー
     */
    public static void importSingleCollection(String jsonFilePath, String collectionName) 
            throws Exception {
        
        if (db == null) {
            throw new IllegalStateException("Firebaseが初期化されていません");
        }

        System.out.println("▶ " + collectionName + " へのインポートを開始...");
        int count = importJsonToFirestore(jsonFilePath, collectionName);
        System.out.println("✓ " + count + " 件のドキュメントをインポートしました");
    }
}
